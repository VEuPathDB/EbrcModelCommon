package org.apidb.apicommon.datasetPresenter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.xml.NamedValue;

/**
 * A constructor of a DatasetInjector subclass. At model construction time, this
 * object is given the name of the DatasetInjector subclass and its property
 * values. At processing time it is called to construct and initialize the
 * DatasetInjector subclass.
 */

public class DatasetInjectorConstructor {
  final static String nl = System.lineSeparator();

  private String datasetInjectorClassName;
  private final Map<String, String> propValues = new HashMap<String, String>();
  private String datasetName;
  private String datasourceName;
  private String projectName;
  private String displayCategory;
  private String category;
  private Contact primaryContact;
  private Map<String,Map<String, String>> globalDatasetProperties;

  /**
   * Set the name of the DatasetInjector subclass to construct. Must be a
   * subclass of DatasetInjector.
   * <p>
   * Called at model construction time.
   */
  public void setClassName(String datasetInjectorClassName) {
    this.datasetInjectorClassName = datasetInjectorClassName;
  }

  /*
   * Called at model construction time
   */
  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  /*
   * Called at model construction time
   */
  public void setProjectName(String projectName) {
    propValues.put("projectName", projectName);
  }

  /**
   * Add a property value to pass to the constructed DatasetInjector subclass.
   * Property values added here in addition to those inherited by the containing
   * DatasetPresenter. Must not conflict with existing properties.
   * <p>
   * Called at model construction time.
   * 
   */
  public void addProp(NamedValue propValue) {
    if (propValues.containsKey(propValue.getName())) {
      throw new UserException("A datasetInector in datasetPresenter '"
          + datasetName + "' has redundant property: " + propValue.getName());
    }
    propValues.put(propValue.getName(), propValue.getValue());
  }

  /**
   * Inherit property values from a DatasetPresenter (typically the containing
   * one). Must not conflict with existing properties.
   * <p>
   * Called at model construction time.
   * 
   * @param datasetPresenter
   *          DatasetPresenter to inherit from.
   */
  void inheritDatasetProps(DatasetPresenter datasetPresenter) {
    this.datasetName = datasetPresenter.getDatasetName();
    for (String key : datasetPresenter.getPropValues().keySet()) {
      if (propValues.containsKey(key))
        throw new UserException("In DatasetPresenter " + datasetName
            + " DatasetInjector for class " + "in dataset ");
      propValues.put(key, datasetPresenter.getPropValues().get(key));
    }
  }

  /**
   * Provide the property values added to this object.
   * <p>
   * Called at processing time.
   * 
   * @return Map of key-value pairs
   */
  Map<String, String> getPropValues() {
    return propValues;
  }
  
  String getPropValue(String propName) {
    return propValues.get(propName);    
  }
  
  String getDatasetInjectorClassName() {
    return datasetInjectorClassName;
  }
  
  String getClassName() {
    return datasetInjectorClassName;
  }

  void setPrimaryContact(Contact primaryContact) {
    this.primaryContact = primaryContact;
  }

  void setGlobalDatasetProperties(Map<String, Map<String, String>> globalDatasetProperties) {
    this.globalDatasetProperties = globalDatasetProperties;
  }

    Map<String, Map<String, String>> getGlobalDatasetProperties() {
        return this.globalDatasetProperties;
  }


  /**
   * Use reflection to construct a subclass of DatasetInjector. Initialize the
   * subclass's property values with those from this object.
   * <p>
   * Called at processing time.
   */
  DatasetInjector getDatasetInjector() {
    DatasetInjector di;
    Class<? extends DatasetInjector> injectorClass;
    try {
      injectorClass = Class.forName(datasetInjectorClassName).asSubclass(
          DatasetInjector.class);
      di = injectorClass.getDeclaredConstructor().newInstance();
    }
    catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
        IllegalArgumentException | InvocationTargetException |
        NoSuchMethodException | SecurityException ex) {
      throw new UserException("Can't find DatasetInjector subclass with name '"
          + datasetInjectorClassName + "'", ex);
    }

    di.addPropValues(propValues);
    di.setDatasetName(datasetName);
    di.setDatasourceName(datasourceName);
    di.setProjectName(projectName);
    di.setCategory(category);
    di.setDisplayCategory(displayCategory);
    di.setPrimaryContact(primaryContact);
    di.setGlobalDatasetProperties(globalDatasetProperties);
    return di;
  }

    public String getDisplayCategory() {
        return displayCategory;
    }

    public void setDisplayCategory(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
