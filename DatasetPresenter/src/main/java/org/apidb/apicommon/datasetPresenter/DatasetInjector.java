package org.apidb.apicommon.datasetPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apidb.apicommon.comparator.NaturalOrderComparator;

/**
 * An abstract superclass of DatasetInjectors. Contains the information needed
 * to inject a dataset into the presentation layer. That information is: a
 * bundle of property values and a set of templates (that require those
 * properties). A tuple (propertyValues, template) is called a
 * "template instance." This injector supplies one or more such tuples to the
 * containing DatasetInjectorSet.
 * 
 * @author steve
 * 
 */
public abstract class DatasetInjector {

  private Map<String, String> propValues = new HashMap<String, String>();
  private String datasetName;
  private Contact primaryContact;
  private DatasetInjectorSet datasetInjectorSet;
  private Map<String, ModelReference> modelReferences = new HashMap<String, ModelReference>();
  private Map<String, Map<String, String>> globalDatasetProperties;

  /**
   * Subclasses use this method to declare the properties they require. These
   * properties must be the union of all the properties required by the
   * templates the subclass injects. Standard properties supplied by the
   * containing <code>DatasetPresenter</code> (e.g. "datasetName") must be
   * declared if they are required by any of the templates.
   * 
   * The values for these properties are supplied in three ways:
   * <ul>
   * <li>Standard properties required by a DatasetPresenter are inherited. These
   * are supplied by the user in the <datasetPresenter> element in the dataset
   * presenter XML file.
   * <li>
   * 
   * @see DatasetPresenter
   * @return an array of String pairs: (property name, documentation for that
   *         property)
   */
  protected abstract String[][] getPropertiesDeclaration();

  protected String[][] combinePropertiesDeclarations(String[][] one,
      String[][] two) {
    String[][] combined = new String[one.length + two.length][2];

    for (int i = 0; i < one.length; i++) {
      combined[i] = one[i];
    }
    for (int i = 0; i < two.length; i++) {
      combined[i + one.length] = two[i];
    }

    return combined;
  }

  /**
   * Subclasses call this method to add model references to the presentation
   * layer. To do so they add a call in this method to either {@link
   * #addWdkReference(String,String,String} or
   * {@link #addModelReference(String, String). *
   */
  protected abstract void addModelReferences();

  /**
   * Subclasses call this method to inject template instances into the
   * presentation layer. To do so they add a call in this method to
   * {@link #injectTemplate(String)}. The template will have available all
   * properties declared by <code>getPropertiesDeclaration</code>.
   * 
   * Optionally they provide additional hard-coded properties to templates by
   * calling {@link #setPropValue(String, String)} before calling
   * <code>injectTemplate()</code>.
   */
  protected abstract void injectTemplates();

  /**
   * Hard-code a property value to be passed to a template. Typically properties
   * are specified by the user in the datasetPresenter XML file. This will be
   * made available
   * 
   * @param key
   * @param value
   */
  protected void setPropValue(String key, String value) {
    propValues.put(key, value);
  }


    protected Map<String, String> getPropValues() {
        return this.propValues;
    }

  protected String getPropValue(String key) {
    return propValues.get(key);
  }

  protected boolean getPropValueAsBoolean(String key) {
      String pv = getPropValue(key);

      if(pv != null && !pv.toLowerCase().equals("true") && !pv.toLowerCase().equals("false")) {
          throw new UserException("Required true/false value for boolean prop " + key + " for dataset " + this.datasetName);
      }

    return Boolean.parseBoolean(pv);
  }

  protected String getDatasetName() {
    return datasetName;
  }

  protected Contact getPrimaryContact() {
    return this.primaryContact;
  }

  /**
   * Subclasses should call this method inside {@link #injectTemplates()} to
   * inject a template. The template will be passed all this injector's property
   * values. Optionally provide additional hard-coded property values by calling
   * addPropValue().
   */
  protected void injectTemplate(String templateName) {
    TemplateInstance templateInstance = new TemplateInstance(templateName,
        Collections.unmodifiableMap(propValues));
    datasetInjectorSet.injectTemplateInstance(templateInstance);
  }

  /**
   * Subclasses should call this method inside {@link #addModelReferences()} to
   * make a WDK reference.
   * 
   * @param recordClass
   * @param type
   * @param name
   */
  protected void addWdkReference(String recordClass, String type, String name) {
    ModelReference ref = new ModelReference(recordClass, type, name,
        datasetName);
    String key = recordClass + type + name;
    if (modelReferences.containsKey(key)) {
      throw new UserException("Dataset " + datasetName
          + " already contains a model reference for " + type + ", " + name);
    }
    modelReferences.put(key, ref);
  }

  /**
   * Subclasses should call this method inside {@link #addModelReferences()} to
   * make a reference to a model object that is not in the WDK (eg, GBrowse).
   * 
   * @param recordClass
   * @param type
   * @param name
   */
  protected void addModelReference(String type, String name) {
    ModelReference ref = new ModelReference(type, name, datasetName);
    String key = type + name;
    if (modelReferences.containsKey(key)) {
      throw new UserException("Dataset " + datasetName
          + " already contains a model reference for " + type + ", " + name);
    }
    modelReferences.put(key, ref);
  }

  List<ModelReference> getModelReferences() {
    return new ArrayList<ModelReference>(modelReferences.values());
  }

  /**
   * Add property values.
   * 
   * @param propValues
   */
  protected void addPropValues(Map<String, String> propValues) {
    this.propValues.putAll(propValues);

    // validate against declaration
    String[][] propsDeclaration = getPropertiesDeclaration();
    for (String[] decl : propsDeclaration) {
      if (!propValues.containsKey(decl[0])) {
        throw new UserException("A datasetInjector for class "
            + this.getClass().getName() + " in DatasetPresenter " + datasetName
            + " is missing the required property " + decl[0]);
      }
    }
  }

  /**
   * This method is used to discover if a datasetInjector is a potential match
   * given the current properties. The default behavior is that if all the
   * declared properties are present, then the injector is considered a match.
   * Subclasses can override this method to change the discover behavior or add
   * additional checks.
   * 
   * @param propValues
   * @return    true if the injector is a match with given param values.
   */
  public boolean discover(Map<String, String> propValues) {
    String[][] propsDeclaration = getPropertiesDeclaration();
    boolean hasProp = true;
    for (String[] decl : propsDeclaration) {
      if (!propValues.containsKey(decl[0])) {
        hasProp = false;
        break;
      }
    }
    // if all the declared properties are present, consider this dataset
    // injector a potential match; otherwise, not match.
    return hasProp;
  }
  
  public boolean isDiscoverable() {
    return false;
  }

  /**
   * Set the name of the dataset that is being injected.
   * 
   * @param datasetName
   */
  void setDatasetName(String datasetName) {
    this.datasetName = datasetName;
  }

  /**
   * Set the name of the dataset that is being injected.
   * 
   * @param primaryContact
   */
  void setPrimaryContact(Contact primaryContact) {
    this.primaryContact = primaryContact;
  }

  /**
   * Set the parent DatasetInjectorSet.
   * 
   * @param datasetInjectorSet
   */
  void setDatasetInjectorSet(DatasetInjectorSet datasetInjectorSet) {
    this.datasetInjectorSet = datasetInjectorSet;
  }

  /***
   * return the first word before the underscore in a dataset name for organism
   * specific .. this is always the orgAbbrev
   * 
   */
  protected String getOrganismAbbrevFromDatasetName() {
    if (this.datasetName.equals("")) {
      return "";
    }

    try {
      String[] tokens = this.datasetName.split("_");
      String organismAbbrev = tokens[0];
     
      if(organismAbbrev.equals("")){
        return "";
      } else {

        String projectName = getPropValue("projectName");
        Map<String, Map<String, String>> globalProps = this.getGlobalDatasetProperties();

        for(int i = 0; i < tokens.length - 1; i++) {
          String orgPropsKey = projectName + ":" + organismAbbrev + "_RSRC";

          Map<String, String> orgProps = globalProps.get(orgPropsKey);
          if (orgProps == null) {
            organismAbbrev +=  "_" + tokens[i+1];
          } else {
            return organismAbbrev;
          }
        } 
        return organismAbbrev;
      } 
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "";
    }

    // These conditions are a band-aid until we can read the organism xml file
    // in EUPathDatasets
    /**
    if (this.datasetName.substring(0, 4).equals("gass")) {
      return "G.l.";
    }

    if (this.datasetName.substring(0, 5).equals("Phyra")) {
      return "P.r.";
    }

    if (this.datasetName.substring(0, 5).equals("Hyaar")) {
      return "H.a.";
    }

    return this.datasetName.substring(0, 1).toUpperCase() + "."
        + this.datasetName.substring(1, 2).toLowerCase() + ".";
    */
  }


  protected String getOrganismAbbrevDisplayFromDatasetName() {
    if (this.datasetName.equals("")) {
      return "";
    }

    try {
      String[] tokens = this.datasetName.split("_");
      String organismAbbrev = tokens[0];
     
      if(organismAbbrev.equals("")){
        return "";
      } else {

        String projectName = getPropValue("projectName");
        Map<String, Map<String, String>> globalProps = this.getGlobalDatasetProperties();

        for(int i = 0; i < tokens.length - 1; i++) {
          String orgPropsKey = projectName + ":" + organismAbbrev + "_RSRC";

          Map<String, String> orgProps = globalProps.get(orgPropsKey);
          if (orgProps == null) {
            organismAbbrev +=  "_" + tokens[i+1];
          } else {
           String[] orgName =  orgProps.get("organismFullName").split(" ");
            String orgAbbrevDisplay = String.valueOf(orgName[0].charAt(0));

	    orgAbbrevDisplay += ".";

            for(int j = 1; j < orgName.length; j++) {
                orgAbbrevDisplay += " " + orgName[j];
            }

            return orgAbbrevDisplay;
          }
        } 
        return organismAbbrev;
      } 
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return "";
    }

  }

  protected void setOrganismAbbrevFromDatasetName() {
    String organismAbbrev = this.getOrganismAbbrevFromDatasetName();
    setPropValue("organismAbbrev", organismAbbrev);
    setPropValue("organismAbbrevInternal", organismAbbrev);

    String organismAbbrevDisplay = this.getOrganismAbbrevDisplayFromDatasetName();
    setPropValue("organismAbbrevDisplay", organismAbbrevDisplay);
  }

  /**
  protected void setOrganismAbbrevInternalFromDatasetName() {
    String[] datasetWords = this.datasetName.split("_");
  }
  */

  protected String cleanString(String dirtyText) {
      String cleanText = dirtyText.replaceAll("\\<.*?\\>", "");
      return cleanText;
  }

  protected void setGraphDatasetName() {
    String graphDatasetName = this.datasetName.replace("-", "");
    setPropValue("graphDatasetName", graphDatasetName);
  }
  
  void setGlobalDatasetProperties(Map<String, Map<String, String>> globalDatasetProps) {
    this.globalDatasetProperties = globalDatasetProps;
  }

  protected Map<String, Map<String, String>> getGlobalDatasetProperties() {
    return globalDatasetProperties;
  }

  protected void setShortAttribution() {
    String shortAttribution = getPropValue("shortAttribution");

    if (shortAttribution == null || shortAttribution.equals("")) {
      Contact primaryContact = getPrimaryContact();

      String contactName = primaryContact.getName();
      int lastSpace = contactName.lastIndexOf(" ");

      setPropValue("shortAttribution",
          "(" + contactName.substring(lastSpace + 1) + ")");
    } else {
      setPropValue("shortAttribution", "(" + shortAttribution + ")");
    }
  }


  protected List<String> getSampleList() {

    Map<String, Map<String, String>> globalProps = getGlobalDatasetProperties();
    Iterator<String> globalPropsKeys = globalProps.keySet().iterator();

    Map<String, List<String>> exptSamples = new HashMap<String, List<String>>();

    String organismAbbrev = getPropValue("organismAbbrev");

    while (globalPropsKeys.hasNext()) {
        String dataset = globalPropsKeys.next();

        if(!dataset.startsWith(organismAbbrev) || this.datasetName.equals(dataset)) {
            continue;
        }

        Map<String, String> datasetProps = globalProps.get(dataset);

        if (datasetProps.containsKey("experimentName")) {
            String sampleOrganismAbbrev = datasetProps.get("organismAbbrev");
            String sampleDatasetClassCategory = datasetProps.get("datasetClassCategory");
            String experimentName = datasetProps.get("experimentName");

            String sampleExptKey = sampleOrganismAbbrev + "_" + sampleDatasetClassCategory + "_" + experimentName;

            if(!exptSamples.containsKey(sampleExptKey)) {
                exptSamples.put(sampleExptKey, new ArrayList<String>());
            }

            List<String> samples = exptSamples.get(sampleExptKey);

            if (datasetProps.containsKey("sampleName")) {
                samples.add(datasetProps.get("sampleName"));
            } 
            if (datasetProps.containsKey("snpStrainAbbrev")) {
                samples.add(datasetProps.get("snpStrainAbbrev"));
            }
        }
    }

    Map<String, String> exptProps = globalProps.get(this.datasetName);
    String exptName = "";
    if (exptProps.containsKey("experimentName")) {
        exptName = exptProps.get("experimentName");
    } 
    if (exptProps.containsKey("name")) {
        exptName = exptProps.get("name");
    } 

    String datasetClassCategory = getPropValue("datasetClassCategory");

    String exptKey = organismAbbrev + "_" + datasetClassCategory + "_" + exptName;

    List<String> sampleNames = exptSamples.get(exptKey);

    if (sampleNames.isEmpty()) {
        throw new UserException ("No sample names found for dataset " + this.datasetName);
    }
    // remove duplicates (NGS and CNV have dup experiment and samples)
    Set<String> distinctSamples = new HashSet<>();
    distinctSamples.addAll(sampleNames);
    sampleNames.clear();
    sampleNames.addAll(distinctSamples);

    Collections.sort(sampleNames, new NaturalOrderComparator());
    return sampleNames;
  }
}
