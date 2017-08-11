/**
 * 
 */
package org.apidb.apicommon.datasetPresenter;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * This injector will try to discover the injector(s) that can do the actual
 * work based on the properties given to it.
 * 
 * By default, it will only discover the injectors under
 * "org.apidb.apicommomn.model.datasetInjector" package, but you could change
 * the packages it scans by adding the semi-colon separated list of packages in
 * the optional "discover.package" property. However, please note that if you
 * specify the "discover.package" property, then it won't scan in the default
 * package unless you have the default package in the list of packages you have
 * for property.
 * 
 * It will call the DatasetInjector.discover() method to test if an injector is
 * a match, if no injector can be matched/discovered, it will thrown an
 * UserException.
 * 
 * @author jerric
 * 
 */
public class DiscoverableDatasetInjector extends DatasetInjector {

  private static final String PROP_DISCOVER_PACKAGE = "discover.package";

  private static final String DEFAULT_PACKAGE = "org.apidb.apicommon.model.datasetInjector";

  private final List<DatasetInjector> injectors = new ArrayList<>();

  /**
   * Try to discover the actual injector with the given propValues. If no
   * injector can be discovered, an UserException will be thrown out.
   * 
   * @see org.apidb.apicommon.datasetPresenter.DatasetInjector#addPropValues(java.util.Map)
   */
  @Override
  protected void addPropValues(Map<String, String> propValues) {
    String packageString = propValues.get(PROP_DISCOVER_PACKAGE);
    if (packageString == null)
      packageString = DEFAULT_PACKAGE;

    FilterBuilder filterBuilder = new FilterBuilder();
    Set<URL> urls = new HashSet<>();
    for (String pack : packageString.split("[\\,\\;]")) {
      filterBuilder.includePackage(pack);
      urls.addAll(ClasspathHelper.forPackage(pack));
    }

    ConfigurationBuilder configBuilder = new ConfigurationBuilder();
    configBuilder.filterInputsBy(filterBuilder).addUrls(urls).setScanners(
        new SubTypesScanner(), new ResourcesScanner());

    Reflections reflections = new Reflections(configBuilder);
    Set<Class<? extends DatasetInjector>> classes = reflections.getSubTypesOf(DatasetInjector.class);
    for (Class<? extends DatasetInjector> injectorClass : classes) {
      if (Modifier.isAbstract(injectorClass.getModifiers()))
        continue;

      try {
        DatasetInjector injector = injectorClass.newInstance();
        injector.setDatasetName(getDatasetName());
        injector.setPrimaryContact(getPrimaryContact());

        if (injector.isDiscoverable() && injector.discover(propValues)) {
          injector.addPropValues(propValues);
          injectors.add(injector);
        }
      } catch (InstantiationException | IllegalAccessException ex) {
        throw new UserException(
            "Unable to create instance of " + injectorClass, ex);
      }
    }

    // if no injectors are discovered, raise an error
    if (injectors.size() == 0)
      throw new UserException(
          "No injectors can be found with the given property values.");
  }

  /**
   * It doesn't declare any properties, but it overrides the addPropValues
   * method, so this method won't be used anyway.
   * 
   * @see org.apidb.apicommon.datasetPresenter.DatasetInjector#getPropertiesDeclaration
   *      ()
   */
  @Override
  protected String[][] getPropertiesDeclaration() {
    return new String[0][0];
  }

  /**
   * it will delegate this function to the discovered injectors.
   * 
   * @see org.apidb.apicommon.datasetPresenter.DatasetInjector#addModelReferences()
   */
  @Override
  protected void addModelReferences() {
    for (DatasetInjector injector : injectors) {
      injector.addModelReferences();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apidb.apicommon.datasetPresenter.DatasetInjector#injectTemplates()
   */
  @Override
  protected void injectTemplates() {
    for (DatasetInjector injector : injectors) {
      injector.injectTemplates();
    }
  }

  public Collection<DatasetInjector> getInjectors() {
    return injectors;
  }
}
