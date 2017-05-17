package org.apidb.apicommon.datasetPresenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  // use prop values for properties that might be injected into templates.
  Map<String, String> propValues = new HashMap<String, String>();

  // use instance variables for properties that have no chance of being
  // injected.
  private String displayCategory;
  private String protocol;
  private String usage;
  private String acknowledgement;
  private String caveat;
  private String releasePolicy;
  private DatasetInjector datasetInjector;
  private String datasetNamePattern;
  private String type;
  private String subtype;
  private Boolean isSpeciesScope;
  private boolean foundInDb = false;
  private int maxHistoryBuildNumber = -1;

  private DatasetInjectorConstructor datasetInjectorConstructor;
  private List<String> contactIds = new ArrayList<String>(); // includes primary
  private String primaryContactId;
  private List<Contact> contacts;
  private List<Publication> publications = new ArrayList<Publication>();
  private List<History> histories = new ArrayList<History>();
  private List<HyperLink> links = new ArrayList<HyperLink>();
  private Map<String, NameTaxonPair> nameTaxonPairs = new HashMap<String, NameTaxonPair>(); // expanded from pattern if we have one
  private String override = null;

  private List<String> datasetNamesFromPattern  = new ArrayList<String>();

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
	return "DS_" + DigestUtils.sha1Hex(getDatasetName()).substring(0,10);
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

  public void addNameTaxonPair(NameTaxonPair pair) {
    nameTaxonPairs.put(pair.getName(), pair);
  }
  
  public void removeNameTaxonPair(String name) {
    nameTaxonPairs.remove(name);
  }
  
  boolean containsNameTaxonPair(String name) {
    return nameTaxonPairs.containsKey(name);
  }

  public Collection<NameTaxonPair> getNameTaxonPairs() {
    return nameTaxonPairs.values();
  }

  public void setProjectName(String projectName) {
    propValues.put("projectName", projectName);
  }

  public void setOrganismShortName(String organismShortName) {
    propValues.put("organismShortName", organismShortName);
  }

  public void setBuildNumberIntroduced(Integer buildNumberIntroduced) {
     propValues.put("buildNumberIntroduced", buildNumberIntroduced.toString());
  }

  public Integer getBuildNumberIntroduced() {
    if (propValues.get("buildNumberIntroduced") == null)
      return null;
    return new Integer(propValues.get("buildNumberIntroduced"));
  }

  public void setBuildNumberRevised(Integer buildNumberRevised) {
     propValues.put("buildNumberRevised", buildNumberRevised.toString());
  }

  public Integer getBuildNumberRevised() {
    if (propValues.get("buildNumberRevised") == null)
      return null;
    return new Integer(propValues.get("buildNumberRevised"));
  }

  public void setDisplayCategory(Text displayCategory) {
    this.displayCategory = displayCategory.getText();
  }

  public String getDisplayCategory() {
    return displayCategory;
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

    // validate that the history elements have increasing build numbers 
    if (history.getBuildNumber() <= maxHistoryBuildNumber) 
      throw new UserException("DatasetPresenter with name \""
			      + getDatasetName()
			      + "\" has a <history> element that with an out-of-order build number: " + history.getBuildNumber());

    // first history element
    if (histories.size() == 0 ) {
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
  public void setDatasetInjector(DatasetInjectorConstructor datasetInjector) {
    if (datasetInjectorConstructor != null) throw new UserException("Adding more than one datasetInjector to datasetPresenter " + getDatasetName());
    datasetInjectorConstructor = datasetInjector;
    datasetInjector.inheritDatasetProps(this);
  }

  protected DatasetInjector getDatasetInjector() {
    if (datasetInjector == null && datasetInjectorConstructor != null) {
      datasetInjector = datasetInjectorConstructor.getDatasetInjector();
      datasetInjector.addModelReferences();
    }
    return datasetInjector;
  }

  void setDefaultDatasetInjector(
      Map<String, Map<String, String>> defaultDatasetInjectors) {
    if (type == null
        || defaultDatasetInjectors == null
        || !defaultDatasetInjectors.containsKey(type)
        || !defaultDatasetInjectors.get(type).containsKey(subtype)
        || datasetInjectorConstructor != null)
      return;
    DatasetInjectorConstructor constructor = new DatasetInjectorConstructor();
    constructor.setClassName(defaultDatasetInjectors.get(type).get(subtype));
    setDatasetInjector(constructor);
  }

  public List<ModelReference> getModelReferences() {
    List<ModelReference> answer = new ArrayList<ModelReference>();
    DatasetInjector di = getDatasetInjector();
    if (di != null) {
      answer = di.getModelReferences();
    }
    return answer;
  }

  public DatasetInjectorConstructor getDatasetInjectorConstructor() {
    return datasetInjectorConstructor;
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
        if (datasetInjectorConstructor != null) {
            datasetInjectorConstructor.addProp(presenterId);
        }
    }

  
  /**
   * Add properties parsed from datasetProperties files, passed in as a map, keyed on dataset name
   * @param datasetNamesToProperties
   */
  void addPropertiesFromFile(Map<String,Map<String,String>> datasetNamesToProperties, Set<String> duplicateDatasetNames) {
      String datasetKey = propValues.containsKey("projectName") && !propValues.get("projectName").equals("@PROJECT_ID@") ? propValues.get("projectName") + ":" + getDatasetName() : getDatasetName();

    if (duplicateDatasetNames.contains(datasetKey)) throw new UserException("datasetPresenter '" + getDatasetName()
        + "' is attempting to use properties from dataset '" + datasetKey + "' but that dataset is not unique in the dataset properties files");
    
    // add the global dataset properties to each datasetInjectorConstructor so they can be passed to each injector.
    // there might be a way to do this without duplicating that info across injector constructors, but it is not obvious, and this will work
    if (datasetInjectorConstructor != null) datasetInjectorConstructor.setGlobalDatasetProperties(datasetNamesToProperties);
    
    if (!datasetNamesToProperties.containsKey(datasetKey)) return;
    
    Map<String,String> propsFromFile = datasetNamesToProperties.get(datasetKey);
    
    for (String key : propsFromFile.keySet()) {
      if (key.equals("datasetLoaderName")) continue;  // the dataset name; redundant
      if (key.equals("projectName")) continue;  // redundant
      if (propValues.containsKey(key) ) throw new UserException("datasetPresenter '" + getDatasetName()
          + "' has a property duplicated from dataset property file provided by the dataset class: " + key);
      propValues.put(key, propsFromFile.get(key));
      if (datasetInjectorConstructor != null) {
        if (datasetInjectorConstructor.getPropValues().containsKey(key)) throw new UserException("a templateInjector in datasetPresenter '" + getDatasetName()
            + "' has a property duplicated from dataset property file provided by the dataset class: " + key);

        // Other properties are not valid when using pattern
        datasetInjectorConstructor.addProp(new NamedValue(key, propsFromFile.get(key)));
      }
    }
  }

    void addCategoriesForPattern(Map<String,Map<String,String>> datasetNamesToProperties, Set<String> duplicateDatasetNames) {
      if(propValues.containsKey("datasetClassCategory")) return;
      if(datasetNamesFromPattern.size() < 1) return;

      String datasetKey = datasetNamesFromPattern.get(0);

      if (!datasetNamesToProperties.containsKey(datasetKey)) return;

      Map<String,String> propsFromFile = datasetNamesToProperties.get(datasetKey);
    
      for (String key : propsFromFile.keySet()) {
          if(!key.equals("datasetClassCategory")) continue;

          NamedValue property = new NamedValue(key, propsFromFile.get(key));

          addProp(property);
          if (datasetInjectorConstructor != null) {
              datasetInjectorConstructor.addProp(property);
          }

      }
    }

}
