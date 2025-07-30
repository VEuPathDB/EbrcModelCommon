package org.apidb.apicommon.datasetPresenter;

import java.util.*;

import org.apache.log4j.Logger;

import org.gusdb.fgputil.xml.NamedValue;
import org.gusdb.fgputil.xml.Text;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * A specification for adding a dataset to the presentation layer. A simple data
 * holder that contains a set of properties and a set of
 * DatasetInjectorConstructors. At processing time it is transformed into a set
 * of DatasetInjector subclasses.
 * 
 * @author steve
 * 
 */
public class DatasetPresenter {
  private static final Logger LOG = Logger.getLogger(DatasetPresenter.class);

  // use prop values for properties that might be injected into templates.
  Map<String, String> propValues = new HashMap<>();

  // use instance variables for properties that have no chance of being
  // injected.
  private String categoryOverride;
  private String protocol;
  private String usage;
  private String acknowledgement;
  private String caveat;
  private String releasePolicy;
  private String datasetNamePattern;
  private String type;
  private String subtype;
  private Boolean isSpeciesScope;
  private String projectId;
  private boolean foundInDb = false;
  private float maxHistoryBuildNumber = -1000;

  private final Set<DatasetInjectorConstructor> datasetInjectorConstructors = new HashSet<>();
  private final List<String> contactIds = new ArrayList<String>(); // includes primary
  private String primaryContactId;
  private List<Contact> contacts;
  private final List<Publication> publications = new ArrayList<Publication>();
  private final List<History> histories = new ArrayList<>();
  private final List<HyperLink> links = new ArrayList<>();
  private final Map<String, Datasource> datasources = new HashMap<String, Datasource>(); // expanded from pattern if we have one
  private String override = null;

  private final List<String> datasetNamesFromPattern  = new ArrayList<String>();

    void addDatasetNameToList(String datasetName) {
        this.datasetNamesFromPattern.add(datasetName);
    }

  void setFoundInDb() {
    foundInDb = true;
  }
  
  boolean getFoundInDb() {
    return foundInDb;
  }
  
  public void setName(String datasetName) {
    propValues.put("datasetName", datasetName);
  }

  public String getDatasetName() {
    return propValues.get("datasetName");
  }

    public String getId() {
	return "DS_" + getDigest();
    }

    public String getDigest() {
	return DigestUtils.sha1Hex(getDatasetName()).substring(0,10);
    }

    public String getFullDigest() {
        return DigestUtils.sha1Hex(getDatasetName());
    }

  String getPropValue(String propName) {
    return propValues.get(propName);
  }

  public void setDatasetDescrip(Text datasetDescrip) {
    propValues.put("datasetDescrip", datasetDescrip.getText());
  }

  public String getDatasetDescrip() {
    return propValues.get("datasetDescrip");
  }

  public void setDatasetDisplayName(Text datasetDisplayName) {
    propValues.put("datasetDisplayName", datasetDisplayName.getText());
  }

  public String getDatasetDisplayName() {
    return propValues.get("datasetDisplayName");
  }

  public void setShortAttribution(Text shortAttribution) {
    propValues.put("shortAttribution", shortAttribution.getText());
  }

  public String getShortAttribution() {
    return propValues.get("shortAttribution");
  }

  public void setDatasetShortDisplayName(Text datasetShortDisplayName) {
    propValues.put("datasetShortDisplayName", datasetShortDisplayName.getText());
  }

  public String getDatasetShortDisplayName() {
    return propValues.get("datasetShortDisplayName");
  }

  public void setSummary(Text summary) {
    propValues.put("summary", summary.getText());
  }

  public String getSummary() {
    return propValues.get("summary");
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setSubtype(String subtype) {
    this.subtype = subtype;
  }

  public String getSubtype() {
    return subtype;
  }

  public void setIsSpeciesScope(Boolean isSpeciesScope) {
    this.isSpeciesScope = isSpeciesScope;
  }

  public Boolean getIsSpeciesScope() {
    return isSpeciesScope;
  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  public String getProjectId() {
    return projectId;
  }

  public void setDatasetNamePattern(String pattern) {
    if (!pattern.contains("%") || pattern.contains("*"))
      throw new UserException(
          "Dataset "
              + getDatasetName()
              + " contains an illegal datasetNamePattern attribute.  It must contain one or more SQL wildcard (%) and no other type of wildcards");
    propValues.put("datasetNamePattern", pattern);
    datasetNamePattern = pattern;
  }

  public String getDatasetNamePattern() {
    return datasetNamePattern;
  }
  
  public void setOverride(String datasetName) {
    this.override = datasetName;
  }
  
  public String getOverride() {
    return override;
  }

  public void addDatasource(Datasource ds) {
    datasources.put(ds.getName(), ds);
  }
  
  public void removeDatasource(String name) {
    datasources.remove(name);
  }
  
  boolean containsDatasource(String name) {
    return datasources.containsKey(name);
  }

  public Collection<Datasource> getDatasources() {
    return datasources.values();
  }

  public void setOrganismShortName(String organismShortName) {
    propValues.put("organismShortName", organismShortName);
  }

  public void setBuildNumberIntroduced(Float buildNumberIntroduced) {
     propValues.put("buildNumberIntroduced", buildNumberIntroduced.toString());
  }

  public Float getBuildNumberIntroduced() {
    if (propValues.get("buildNumberIntroduced") == null)
      return null;
    return Float.valueOf(propValues.get("buildNumberIntroduced"));
  }

  public void setBuildNumberRevised(Float buildNumberRevised) {
     propValues.put("buildNumberRevised", buildNumberRevised.toString());
  }

  public Float getBuildNumberRevised() {
    if (propValues.get("buildNumberRevised") == null)
      return null;
    return Float.valueOf(propValues.get("buildNumberRevised"));
  }

  public void setCategoryOverride(Text categoryOverride) {
    this.categoryOverride = categoryOverride.getText();
  }

  public String getCategoryOverride() {
    return categoryOverride;
  }

  public void setCaveat(Text caveat) {
    this.caveat = caveat.getText();
  }

  public String getCaveat() {
    return caveat;
  }

  public void setReleasePolicy(Text releasePolicy) {
    this.releasePolicy = releasePolicy.getText();
  }

  public String getReleasePolicy() {
    return releasePolicy;
  }

  public void setProtocol(Text protocol) {
    this.protocol = protocol.getText();
  }

  public String getProtocol() {
    return protocol;
  }

  public void setUsage(Text usage) {
    this.usage = usage.getText();
  }

  public String getUsage() {
    return usage;
  }

  public void setAcknowledgement(Text acknowledgement) {
    this.acknowledgement = acknowledgement.getText();
  }

  public String getAcknowledgement() {
    return acknowledgement;
  }

  public void addContactId(Text contactId) {
    contactIds.add(contactId.getText());
  }

  public void setPrimaryContactId(Text contactId) {
    primaryContactId = contactId.getText();
    contactIds.add(contactId.getText());
  }

  public List<String> getContactIds() {
    return contactIds;
  }

    public Contact getPrimaryContact() {

        for(int i = 0; i < this.contacts.size(); i++) {
            Contact contact = this.contacts.get(i);

            if(contact.getIsPrimary()) {
                return contact;
            }
        }

        throw new UserException("Primary Contact not found in List of Contacts");
    }


    public List<Contact> getContacts() {
        return this.contacts;
    }

  public List<Contact> getContacts(Contacts allContacts) {
    if (contacts == null) {
      contacts = new ArrayList<Contact>();
      for (String contactId : contactIds) {
        Contact contact = allContacts.get(contactId);

        if (contact == null) {
          String datasetName = propValues.get("datasetName");
          throw new UserException("Dataset name " + datasetName
              + " has a contactId " + contactId
              + " that has no corresponding contact in contacts file "
              + allContacts.getContactsFileName());
        }
        Contact contactCopy = (Contact) contact.clone();
        contacts.add(contactCopy);
        if (contactId.equals(primaryContactId))
          contactCopy.setIsPrimary(true);

      }
    }
    return contacts;
  }

  public void addPublication(Publication publication) {
    publications.add(publication);
  }

  public List<Publication> getPublications() {
    return publications;
  }

  public void addHistory(History history) {
    LOG.debug("\n\n*******history.getBuildNumber() " + history.getBuildNumber() );
    LOG.debug("\n\n*******maxHistoryBuildNumber" + maxHistoryBuildNumber);

    // validate that the history elements have increasing build numbers 
    if (history.getBuildNumber() <= maxHistoryBuildNumber) 
      throw new UserException("DatasetPresenter with name \""
			      + getDatasetName()
			      + "\" has a <history> element that with an out-of-order build number: " + history.getBuildNumber());

    // first history element
    if (histories.size() == 0 ) {
       LOG.debug("\n\n******* history size is 0");
       setBuildNumberIntroduced(history.getBuildNumber());
    } 
    // other history elements
    else {
      if (history.getComment() == null || history.getComment().trim().length() == 0) 
	throw new UserException("DatasetPresenter with name \""
				+ getDatasetName()
				+ "\" has a <history> element that is missing a comment (only the first history element may omit the comment)");
      propValues.put("buildNumberRevised", history.getBuildNumber().toString());
    }
    maxHistoryBuildNumber = history.getBuildNumber();
    histories.add(history);
  }

  public List<History> getHistories() {
    return histories; 
  }

  public void addLink(HyperLink link) {
    links.add(link);
  }

  public List<HyperLink> getLinks() {
    return links;
  }

  /**
   * Add a DatasetInjector.
   * 
   * @param datasetInjector
   */
  public void addDatasetInjector(DatasetInjectorConstructor datasetInjector) {
    datasetInjectorConstructors.add(datasetInjector);
    datasetInjector.inheritDatasetProps(this);
  }

  protected List<DatasetInjector> getDatasetInjectors() {
    List<DatasetInjector> datasetInjectors = new ArrayList<>();
    for (DatasetInjectorConstructor dic : datasetInjectorConstructors) {
      DatasetInjector datasetInjector = dic.getDatasetInjector();
      datasetInjector.addModelReferences();
      datasetInjectors.add(datasetInjector);
    }
    return datasetInjectors;
  }

  void setDefaultDatasetInjector(
      Map<String, Map<String, String>> defaultDatasetInjectors) {
    if (type == null
        || defaultDatasetInjectors == null
        || !defaultDatasetInjectors.containsKey(type)
        || !defaultDatasetInjectors.get(type).containsKey(subtype)
        || !datasetInjectorConstructors.isEmpty())
      return;
    DatasetInjectorConstructor constructor = new DatasetInjectorConstructor();
    constructor.setClassName(defaultDatasetInjectors.get(type).get(subtype));
    addDatasetInjector(constructor);
  }

  public List<ModelReference> getModelReferences() {
    List<ModelReference> refs = new ArrayList<ModelReference>();
    List<DatasetInjector> injectors = getDatasetInjectors();
    for (DatasetInjector i : injectors) {
      refs.addAll(i.getModelReferences());
    }
    return refs;
  }

  public Set<DatasetInjectorConstructor> getDatasetInjectorConstructors() {
    return datasetInjectorConstructors;
  }

  /**
   * Called by digester.
   * @param propValue
   */
  public void addProp(NamedValue propValue) {
    if (propValues.containsKey(propValue.getName())) {
      throw new UserException("datasetPresenter '" + getDatasetName()
          + "' has redundant property: " + propValue.getName());
    }
    propValues.put(propValue.getName(), propValue.getValue());
  }

  public Map<String, String> getPropValues() {
    return propValues;
  }


    void addIdentityProperty() {
        NamedValue presenterId = new NamedValue("presenterId", getId());
        addProp(presenterId);
        for (DatasetInjectorConstructor dic : datasetInjectorConstructors)
            dic.addProp(presenterId);
    }

  
  /**
   * Add properties parsed from datasetProperties files, passed in as a map, keyed on dataset name
   * @param datasetNamesToProperties
   */
  void addPropertiesFromFile(Map<String,Map<String,String>> datasetNamesToProperties, Set<String> duplicateDatasetNames) {
      String datasetKey = getDatasetName();

    if (duplicateDatasetNames.contains(datasetKey)) throw new UserException("datasetPresenter '" + getDatasetName()
        + "' is attempting to use properties from dataset '" + datasetKey + "' but that dataset is not unique in the dataset properties files");
    
    // add the global dataset properties to each datasetInjectorConstructor so they can be passed to each injector.
    // there might be a way to do this without duplicating that info across injector constructors, but it is not obvious, and this will work
    for (DatasetInjectorConstructor dic : datasetInjectorConstructors) dic.setGlobalDatasetProperties(datasetNamesToProperties);
    
    if (!datasetNamesToProperties.containsKey(datasetKey)) return;
    
    Map<String,String> propsFromFile = datasetNamesToProperties.get(datasetKey);
    
    for (String key : propsFromFile.keySet()) {
      if (key.equals("datasetLoaderName")) continue;  // the dataset name; redundant
      if (key.equals("projectName")) continue;  // redundant
      if (propValues.containsKey(key) ) throw new UserException("datasetPresenter '" + getDatasetName()
          + "' has a property duplicated from dataset property file provided by the dataset class: " + key);
      propValues.put(key, propsFromFile.get(key));
      for (DatasetInjectorConstructor dic: datasetInjectorConstructors) {
        if (dic.getPropValues().containsKey(key)) throw new UserException("a templateInjector in datasetPresenter '" + getDatasetName()
            + "' has a property duplicated from dataset property file provided by the dataset class: " + key);

        // Other properties are not valid when using pattern
        dic.addProp(new NamedValue(key, propsFromFile.get(key)));
      }
    }
  }

    void addCategoriesForPattern(Map<String,Map<String,String>> datasetNamesToProperties) {
      if(propValues.containsKey("datasetClassCategory")) return;
      if(datasetNamesFromPattern.size() < 1) return;

      String datasetKey = datasetNamesFromPattern.get(0);

      if (!datasetNamesToProperties.containsKey(datasetKey)) return;

      Map<String,String> propsFromFile = datasetNamesToProperties.get(datasetKey);
    
      for (String key : propsFromFile.keySet()) {
          if(!key.equals("datasetClassCategory")) continue;

          NamedValue property = new NamedValue(key, propsFromFile.get(key));

          addProp(property);
          for (DatasetInjectorConstructor dic : datasetInjectorConstructors) {
              dic.addProp(property);
          }

      }
    }

  DatasetInjector findInjectorByName(String name){
    List<DatasetInjector> dis = getDatasetInjectors();

    if (dis.isEmpty()) return null;

    // if just one injector, and it doesn't declare a datasourceName, it inherits the presenter name
    if (dis.size() == 1) {
      DatasetInjector di = dis.get(0);
      String nm = di.getDatasourceName();
      if (nm == null) return di;
    }

    // compare name against injectors name
    for (DatasetInjector di : dis) {
      String nm = di.getDatasourceName();
      if (nm == null) throw new UserException ("When multiple injectors, not allowed to have an injector with no datasourceName attribute");
      if (nm.equals(name)) return di;
    }

    // we didn't find the requested name
    throw new UserException("In presenter " + getId() +
            " no injector found with name: " + name);
  }

  public void validate () {
    if (datasetInjectorConstructors.size() > 1) {
      String msg = "This presenter has more than one injector. ";
      if (getPropValue("datasetClassCategory") != null && !getPropValue("datasetClassCategory").isEmpty())
        throw new UserException(msg + "It is not allowed to acquire a datasetClassCategory from the prop file.");
      if (categoryOverride != null && !categoryOverride.isEmpty())
        throw new UserException(msg + "It it not allowed to have a 'categoryOverride' attribute.");
      if (datasetNamesFromPattern.size() != datasetInjectorConstructors.size())
        throw new UserException(msg + "The number of injectors must match the number of dataset names in the pattern");
      for (DatasetInjectorConstructor dic : datasetInjectorConstructors) {
        if (dic.getDatasetInjector().getCategoryOverride().isEmpty())
          throw new UserException(msg + "Each injector is required to have a categoryOverride");
        String nm = dic.getDatasetInjector().getDatasourceName();
        if (!datasetNamesFromPattern.contains(nm))
          throw new UserException(msg + "Datasource name '" + nm + "' from one of the injectors is not found in the names from pattern");
      }
    }
  }
}
