package org.apidb.apicommon.datasetPresenter;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

  private Map<String, DatasetPresenter> presenters = new LinkedHashMap<String, DatasetPresenter>();
  private Map<String, InternalDataset> internalDatasets = new LinkedHashMap<String, InternalDataset>();
  private Set<String> namePatterns = new HashSet<String>();

    private Map<String,Map<String,String>> propertiesFromFiles = new HashMap<String,Map<String,String>>();
    private Set<String> duplicateDatasetNames = new HashSet<String>();

    


  /**
   * Add a DatasetPresenter to this set.
   * 
   * Called at Model construction
   */
  public void addDatasetPresenter(DatasetPresenter presenter) {
    String name = presenter.getDatasetName();
    if (presenters.containsKey(name))
      throw new UserException("DatasetPresenter already exists with name: "
          + name);
    if (internalDatasets.containsKey(name))
      throw new UserException("InternalDataset already exists with name: "
          + name);
    presenters.put(name, presenter);
    String pattern = presenter.getDatasetNamePattern();
    if (pattern != null) {
      if (namePatterns.contains(pattern))
        throw new UserException("datasetNamePattern already exists: " + pattern);
      namePatterns.add(pattern);
    }
  }

  public void addInternalDataset(InternalDataset internalDataset) {

    String name = internalDataset.getName();
    if (presenters.containsKey(name))
      throw new UserException("DatasetPresenter already exists with name: "
          + name);
    if (internalDatasets.containsKey(name))
      throw new UserException("InternalDataset already exists with name: "
          + name);
    internalDatasets.put(name, internalDataset);

    String pattern = internalDataset.getDatasetNamePattern();
    if (pattern != null) {
      if (namePatterns.contains(pattern))
        throw new UserException("datasetNamePattern already exists: " + pattern);
      namePatterns.add(pattern);
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
    for (DatasetPresenter presenter : presenters.values()) {
      if (presenter.getDatasetInjectorConstructor() != null) 
        datasetInjectorSet.addDatasetInjector(presenter.getDatasetInjectorConstructor().getDatasetInjector());
    }
  }

  int getSize() {
    return presenters.size();
  }

  Map<String, DatasetPresenter> getDatasetPresenters() {
    return Collections.unmodifiableMap(presenters);
  }
  
  DatasetPresenter getDatasetPresenter(String name) {
    return presenters.get(name);
  }

  Map<String, InternalDataset> getInternalDatasets() {
    return Collections.unmodifiableMap(internalDatasets);
  }

  void validateContactIds(String contactsFileName) {
    ContactsFileParser parser = new ContactsFileParser();
    String project_home = System.getenv("PROJECT_HOME");
    Contacts contacts = parser.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
    for (DatasetPresenter presenter : presenters.values()) {
      presenter.getContacts(contacts);
    }
  }
  
  void handleOverrides() {
    for (DatasetPresenter datasetPresenter : presenters.values()) {
      String override = datasetPresenter.getOverride();
      if (override != null) {
        String datasetName = datasetPresenter.getDatasetName();
        String partialErrMsg = "DatasetPresenter with name " + datasetName + " contains override=\"" + override + "\"";
        DatasetPresenter overriddenDp = getDatasetPresenter(override);
        InternalDataset overriddenIntD = internalDatasets.get(override);
        if (overriddenDp != null) {
          if (!overriddenDp.containsNameTaxonPair(datasetName)) throw new UserException(partialErrMsg + " but the overridden DatasetPresenter does not match \"" + datasetName + "\"");
          overriddenDp.removeNameTaxonPair(datasetName);
        } else if (overriddenIntD != null) {
          if (!overriddenIntD.containsNameFromDb(datasetName)) throw new UserException(partialErrMsg + " but the overridden InternalDataset does not match \"" + datasetName + "\""); 
        } else {
          throw new UserException(partialErrMsg + " but no DatasetPresenter or InternalDataset has that name" );
        }
      }
    }        
  }
  
  void addPropertiesFromFiles(Map<String,Map<String,String>> datasetNamesToProperties, Set<String> duplicateDatasetNames) {
    for (DatasetPresenter datasetPresenter : presenters.values()) {
      datasetPresenter.addPropertiesFromFile(datasetNamesToProperties, duplicateDatasetNames);
    }
  }

    void addCategoriesForPattern() {
        for (DatasetPresenter datasetPresenter : presenters.values()) {
            if(datasetPresenter.getDatasetNamePattern() != null && !datasetPresenter.getDatasetNamePattern().equals("")) {
                datasetPresenter.addCategoriesForPattern(propertiesFromFiles, duplicateDatasetNames);
            }
        }
    }

  void addIdentifierProperty() {
    for (DatasetPresenter datasetPresenter : presenters.values()) {
        datasetPresenter.addIdentityProperty();
    }
  }



  // //////////////////// Static methods //////////////////

  static DatasetPresenterSet createFromPresentersDir(String presentersDir, String globalXmlFile) {
    File pres = new File(presentersDir);
    if (!pres.isDirectory())
      throw new UserException("Presenters dir " + presentersDir
          + " must be an existing directory");

    // get the presenters into memory
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    DatasetPresenterSet dps = dpp.parseDir(presentersDir, globalXmlFile);
    
    // add properties from dataset prop files to presenters

    DatasetPropertiesParser propParser = new DatasetPropertiesParser();
    propParser.parseAllPropertyFiles(dps.propertiesFromFiles, dps.duplicateDatasetNames);
    dps.addPropertiesFromFiles(dps.propertiesFromFiles, dps.duplicateDatasetNames);

    // add presenterId
    dps.addIdentifierProperty();    

    return dps;
  }

}
