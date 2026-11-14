package org.apidb.apicommon.datasetPresenter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
/**
 * A set of DatasetPresenters. A DatasetPresenterSet has one or more
 * DatasetPresenters. DatasetPresenters have one or more
 * DatasetInjectorConstructors. The whole tree is a model made of simple bean
 * objects created by parsing the XML users use to specify the model.
 * 
 * At processing time the tree is transformed into a DatasetInjectorSet.
 * 
 * @author steve
 * 
 */
public class DatasetPresenterSet {

  private Map<String, DatasetPresenter> _presenters = new LinkedHashMap<String, DatasetPresenter>();
  private Map<String, InternalDataset> _internalDatasets = new LinkedHashMap<String, InternalDataset>();
  private Set<String> _namePatterns = new HashSet<String>();

  private Map<String,Map<String,String>> _propertiesFromFiles = new HashMap<String,Map<String,String>>();
  private Set<String> _duplicateDatasetNames = new HashSet<String>();
  private Set<String> _presentersNotLoaded = new LinkedHashSet<String>();

  /**
   * Add a DatasetPresenter to this set.
   * 
   * Called at Model construction
   */
  public void addDatasetPresenter(DatasetPresenter presenter) {
    String name = presenter.getDatasetName();
    if (_presenters.containsKey(name))
      throw new UserException("DatasetPresenter already exists with name: "
          + name);
    if (_internalDatasets.containsKey(name))
      throw new UserException("InternalDataset already exists with name: "
          + name);
    _presenters.put(name, presenter);
    String pattern = presenter.getDatasetNamePattern();
    if (pattern != null) {
      if (_namePatterns.contains(pattern))
        throw new UserException("datasetNamePattern already exists: " + pattern);
      _namePatterns.add(pattern);
    }
  }

  public void addInternalDataset(InternalDataset internalDataset) {

    String name = internalDataset.getName();
    if (_presenters.containsKey(name))
      throw new UserException("DatasetPresenter already exists with name: "
          + name);
    if (_internalDatasets.containsKey(name))
      throw new UserException("InternalDataset already exists with name: "
          + name);
    _internalDatasets.put(name, internalDataset);

    String pattern = internalDataset.getDatasetNamePattern();
    if (pattern != null) {
      if (_namePatterns.contains(pattern))
        throw new UserException("datasetNamePattern already exists: " + pattern);
      _namePatterns.add(pattern);
    }
  }

  /**
   * Add the members of a DatasetPresenterSet to this set (during model
   * construction).
   */
  void addDatasetPresenterSet(DatasetPresenterSet datasetPresenterSet) {
    for (DatasetPresenter presenter : datasetPresenterSet.getDatasetPresenters().values()) {
      addDatasetPresenter(presenter);
    }
    for (InternalDataset internalDataset : datasetPresenterSet.getInternalDatasets().values()) {
      addInternalDataset(internalDataset);
    }
  }

  /**
   * Add DatasetInjector subclasses constructable by this set to a
   * DatasetInjectorSet. Traverse the tree to find all DatasetPresenters and in
   * turn their DatasetInjectorConstructors. The latter each construct a
   * DatasetInjector subclass which is added to the DatasetInjectorSet
   * 
   * Called at processing time.
   */
  void addToDatasetInjectorSet(DatasetInjectorSet datasetInjectorSet) {
    String currentProjectId = getCurrentProjectId();
    for (DatasetPresenter presenter : _presenters.values()) {
      Set<DatasetInjectorConstructor> dics = presenter.getDatasetInjectorConstructors();
      boolean hasMultipleInjectors = dics.size() > 1;
      for (DatasetInjectorConstructor dic : dics) {
        // A presenter with multiple injectors (one per project variant of the same
        // underlying dataset, e.g. a dual host+pathogen transcriptome study) must
        // only inject the one matching the project currently being built. All of
        // them otherwise collapse onto this presenter's single shared package name
        // (see DatasetPresenter.getId()), and the last one processed silently wins
        // over the rest - e.g. HostDB's settings winning on a PlasmoDB build.
        // Single-injector presenters (the common case) are unaffected.
        if (hasMultipleInjectors) {
          String injectorProjectId = dic.getPropValue("projectName");
          if (injectorProjectId != null && currentProjectId != null
              && !injectorProjectId.equalsIgnoreCase(currentProjectId))
            continue;
        }
        datasetInjectorSet.addDatasetInjector(dic.getDatasetInjector());
      }
    }
  }

  /**
   * The GUS project (e.g. "PlasmoDB") this build is running for, derived from
   * GUS_HOME's conventional path shape (.../<ProjectId>/<webapp>/gus_home) rather
   * than requiring a new CLI/system property just for this filter. Returns null
   * if GUS_HOME isn't set or doesn't have the expected shape, in which case
   * multi-injector presenters fall back to injecting every declared injector
   * (today's behavior) rather than silently dropping all of them.
   */
  private static String getCurrentProjectId() {
    String gusHome = System.getenv("GUS_HOME");
    if (gusHome == null) return null;
    File webappDir = new File(gusHome).getParentFile();
    File projectDir = webappDir == null ? null : webappDir.getParentFile();
    return projectDir == null ? null : projectDir.getName();
  }

  int getSize() {
    return _presenters.size();
  }

  Map<String, DatasetPresenter> getDatasetPresenters() {
    return Collections.unmodifiableMap(_presenters);
  }
  
  DatasetPresenter getDatasetPresenter(String name) {
    return _presenters.get(name);
  }

  Map<String, InternalDataset> getInternalDatasets() {
    return Collections.unmodifiableMap(_internalDatasets);
  }

  // TODO: figure out where this method should be called; probable input value is:
  //   System.getenv("PROJECT_HOME") + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test";
  void validateContactIds(String contactsFileName) {
    ContactsFileParser parser = new ContactsFileParser();
    Contacts contacts = parser.parseFile(contactsFileName);
    for (DatasetPresenter presenter : _presenters.values()) {
      presenter.getContacts(contacts);
    }
  }
  
  void handleOverrides() {
    for (DatasetPresenter datasetPresenter : _presenters.values()) {
      String override = datasetPresenter.getOverride();
      if (override != null) {
        String datasetName = datasetPresenter.getDatasetName();
        String partialErrMsg = "DatasetPresenter with name " + datasetName + " contains override=\"" + override + "\"";
        DatasetPresenter overriddenDp = getDatasetPresenter(override);
        InternalDataset overriddenIntD = _internalDatasets.get(override);
        if (overriddenDp != null) {
          if (!overriddenDp.containsDatasource(datasetName)) System.err.println("WARN:  " + partialErrMsg + " but the overridden dataset is not found in this instance");
          overriddenDp.removeDatasource(datasetName);
        } else if (overriddenIntD != null) {
          if (!overriddenIntD.containsNameFromDb(datasetName)) System.err.println("WARN:  " + partialErrMsg + " but the overridden InternalDataset is not found in this instance"); 
        } else {
            System.err.println("WARN:  " + partialErrMsg + " but no DatasetPresenter or InternalDataset has that name" ); 
        }
      }
    }        
  }
  
  /**
   * Drop presenters whose dataset this instance has not loaded, according to the supplied
   * source. The presenters XML directory is a legitimate superset of any one instance, so
   * these are not errors — but they must go before anything walks the set, because
   * constructing a DatasetInjector for an absent dataset fails on the props the dataset
   * would have supplied.
   *
   * Filtering here, once, rather than at each point of use is deliberate: every consumer
   * (dataset injectors, contact validation, model references, the loader) then inherits
   * the gate without knowing it exists, and a consumer added later cannot forget it.
   *
   * Skipped presenters are always reported. A gate that dropped them silently would turn
   * a loud build failure into a website that builds green with searches missing.
   */
  void retainLoadedDatasets(LoadedDatasetSource loadedDatasets) {
    Iterator<Map.Entry<String, DatasetPresenter>> presenters = _presenters.entrySet().iterator();
    while (presenters.hasNext()) {
      DatasetPresenter presenter = presenters.next().getValue();

      // a presenter carries either a name or a datasetNamePattern; resolve the same way
      // DatasetPresenterSetLoader.getPresenterValuesFromDatasourceTable does
      String pattern = presenter.getDatasetNamePattern();
      String nameOrPattern = pattern == null ? presenter.getDatasetName() : pattern;

      if (!loadedDatasets.isLoaded(nameOrPattern)) {
        _presentersNotLoaded.add(nameOrPattern);
        presenters.remove();
        if (pattern != null) _namePatterns.remove(pattern);
      }
    }

  }

  /**
   * @return where to list the skipped presenters, or null if GUS_HOME is not set
   */
  static File skippedReportFile() {
    String gusHome = System.getenv("GUS_HOME");
    return gusHome == null ? null : new File(gusHome + "/lib/wdk/presentersNotLoaded.txt");
  }

  /**
   * Report the skipped presenters: the count on stderr, the names in a file.
   *
   * The count goes in the build log because a silent gate is worse than no gate — a build
   * that quietly drops presenters looks successful while the website loses searches. The
   * names go to a file because there can be thousands of them, and burying the rest of the
   * build log to list them defeats the purpose of reporting at all.
   */
  void reportPresentersNotLoaded(File reportFile) {
    if (_presentersNotLoaded.isEmpty()) return;

    String summary = "Dataset gate: skipping " + _presentersNotLoaded.size()
        + " DatasetPresenter(s) whose dataset is not loaded in this instance";

    if (reportFile == null) {
      // no GUS_HOME to write under; the names are all we can offer
      System.err.println(summary + ":" + System.lineSeparator() + "  "
          + String.join(System.lineSeparator() + "  ", _presentersNotLoaded));
      return;
    }

    try {
      File parent = reportFile.getParentFile();
      if (parent != null) parent.mkdirs();
      try (PrintWriter out = new PrintWriter(reportFile)) {
        out.println("# DatasetPresenters skipped because their dataset is not in this");
        out.println("# instance's apidb.Datasource. Presenters are a superset by design;");
        out.println("# these are not errors.");
        for (String name : _presentersNotLoaded) {
          out.println(name);
        }
      }
      System.err.println(summary + "; names listed in " + reportFile);
    }
    catch (IOException e) {
      // reporting must not break the build, but it must not disappear either
      System.err.println(summary + " (could not write " + reportFile + ": " + e.getMessage()
          + "):" + System.lineSeparator() + "  "
          + String.join(System.lineSeparator() + "  ", _presentersNotLoaded));
    }
  }

  Set<String> getPresentersNotLoaded() {
    return Collections.unmodifiableSet(_presentersNotLoaded);
  }

  void addPropertiesFromFiles(Map<String,Map<String,String>> datasetNamesToProperties, Set<String> duplicateDatasetNames) {
    for (DatasetPresenter datasetPresenter : _presenters.values()) {
      datasetPresenter.addPropertiesFromFile(datasetNamesToProperties, duplicateDatasetNames);
    }
  }

    void addCategoriesForPattern() {
        for (DatasetPresenter datasetPresenter : _presenters.values()) {
            if(datasetPresenter.getDatasetNamePattern() != null && !datasetPresenter.getDatasetNamePattern().equals("")) {
                datasetPresenter.addCategoriesForPattern(_propertiesFromFiles);
            }
        }
    }

  void addIdentifierProperty() {
    for (DatasetPresenter datasetPresenter : _presenters.values()) {
        datasetPresenter.addIdentityProperty();
    }
  }



  // //////////////////// Static methods //////////////////

  static DatasetPresenterSet createFromPresentersDir(String presentersDir, String globalXmlFile) {
    return createFromPresentersDir(presentersDir, globalXmlFile,
        LoadedDatasetSources.fromSiteConfig());
  }

  static DatasetPresenterSet createFromPresentersDir(String presentersDir, String globalXmlFile,
      LoadedDatasetSource loadedDatasets) {
    File pres = new File(presentersDir);
    if (!pres.isDirectory())
      throw new UserException("Presenters dir " + presentersDir
          + " must be an existing directory");

    // get the presenters into memory
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    DatasetPresenterSet dps = dpp.parseDir(presentersDir, globalXmlFile);

    // drop presenters for datasets this instance has not loaded, before anything walks
    // the set. Off by default, so a normal build is unchanged.
    System.err.println(loadedDatasets.describe());
    dps.retainLoadedDatasets(loadedDatasets);
    dps.reportPresentersNotLoaded(skippedReportFile());

    // add properties from dataset prop files to presenters

    DatasetPropertiesParser propParser = new DatasetPropertiesParser();
    propParser.parseAllPropertyFiles(dps._propertiesFromFiles, dps._duplicateDatasetNames);
    dps.addPropertiesFromFiles(dps._propertiesFromFiles, dps._duplicateDatasetNames);

    // add presenterId
    dps.addIdentifierProperty();    

    return dps;
  }

}
