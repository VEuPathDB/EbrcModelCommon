package org.apidb.apicommon.datasetPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.log4j.Logger;
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

  private static final Logger LOG = Logger.getLogger(DatasetInjector.class);

  private final Map<String, String> _propValues = new HashMap<>();
  private String _datasetName;
  private String _datasourceName;
  private String _projectName;
  private String _displayCategory;
  private String _category;
  private Contact _primaryContact;
  private DatasetInjectorSet _datasetInjectorSet;
  private final Map<String, ModelReference> _modelReferences = new HashMap<>();
  private Map<String, Map<String, String>> _globalDatasetProperties;

  private TemplateSet _templateSet;

  private static final String[] EMPTY_ARRAY = new String[0];


  /**
   * Subclasses use this method to declare the properties they require. These
   * properties must be the union of all the properties required by the
   * templates the subclass injects. Standard properties supplied by the
   * containing <code>DatasetPresenter</code> (e.g. "datasetName") must be
   * declared if they are required by any of the templates.
   * <p>
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
   * #addWdkReference(String,String,String}
   * {@link #addModelReference(String, String). *
   */
  protected abstract void addModelReferences();

  /**
   * Subclasses call this method to inject template instances into the
   * presentation layer. To do so they add a call in this method to
   * {@link #injectTemplate(String)}. The template will have available all
   * properties declared by <code>getPropertiesDeclaration</code>.
   * <p>
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

   */
  protected void setPropValue(String key, String value) {
    _propValues.put(key, value);
  }


    protected Map<String, String> getPropValues() {
        return _propValues;
    }


    protected TemplateSet getTemplateSet() {
        return _templateSet;
    }

    protected void setTemplateSet(TemplateSet templateSet) {
        _templateSet = templateSet;
    }


    protected String getTemplateInstanceText(String templateName) {
      TemplateSet templateSet = getTemplateSet();

      Template template = templateSet.getTemplateByName(templateName);
      TemplateInstance templateInstance = new TemplateInstance(templateName,
                                                               Collections.unmodifiableMap(_propValues));


      template.validateTemplateText();
      template.validatePropertiesInstance(_propValues);

      return(template.getInstanceAsText(templateInstance));
    }


  protected String getPropValue(String key) {
    return _propValues.get(key);
  }

  protected boolean getPropValueAsBoolean(String key) {
      String pv = getPropValue(key);

      if(pv != null && !pv.equalsIgnoreCase("true") && !pv.equalsIgnoreCase("false")) {
          throw new UserException("Required true/false value for boolean prop " + key + " for dataset " + _datasetName);
      }

    return Boolean.parseBoolean(pv);
  }

  protected String getDatasetName() {
    return _datasetName;
  }

  protected Contact getPrimaryContact() {
    return _primaryContact;
  }

  /**
   * Subclasses should call this method inside {@link #injectTemplates()} to
   * inject a template. The template will be passed all this injector's property
   * values. Optionally provide additional hard-coded property values by calling
   * addPropValue().
   */
  protected void injectTemplate(String templateName) {
    TemplateInstance templateInstance = new TemplateInstance(templateName,
        Collections.unmodifiableMap(_propValues));
    _datasetInjectorSet.injectTemplateInstance(templateInstance);
  }

  /**
   * Subclasses should call this method inside {@link #addModelReferences()} to
   * make a WDK reference.
   *
   */
  protected void addWdkReference(String recordClass, String type, String name) {
      addWdkReference(recordClass, type, name, EMPTY_ARRAY, null, 0);
  }


    protected void addWdkReference(String recordClass, String type, String name, String[] scopes, String iri, int orderNumber) {
    ModelReference ref = new ModelReference(recordClass, type, name,
                                            _datasetName, scopes, iri, orderNumber);
    String key = recordClass + type + name;
    if (_modelReferences.containsKey(key)) {
      throw new UserException("Dataset " + _datasetName
          + " already contains a model reference for " + type + ", " + name);
    }
    _modelReferences.put(key, ref);
  }

  /**
   * Subclasses should call this method inside {@link #addModelReferences()} to
   * make a reference to a model object that is not in the WDK (eg, GBrowse).
   */
    protected void addModelReference(String type, String name) {
        addModelReference(type, name, EMPTY_ARRAY, null);
    }


    protected void addModelReference(String type, String name, String[] scopes, String iri) {
        ModelReference ref = new ModelReference(type, name, _datasetName, scopes, iri);
    String key = type + name;
    if (_modelReferences.containsKey(key)) {
      throw new UserException("Dataset " + _datasetName
          + " already contains a model reference for " + type + ", " + name);
    }
    _modelReferences.put(key, ref);
  }


  protected List<ModelReference> getModelReferences() {
    return new ArrayList<ModelReference>(_modelReferences.values());
  }

  /**
   * Add property values.
   */
  protected void addPropValues(Map<String, String> propValues) {
    _propValues.putAll(propValues);

    // validate against declaration
    String[][] propsDeclaration = getPropertiesDeclaration();
    for (String[] decl : propsDeclaration) {
      if (!propValues.containsKey(decl[0])) {
        throw new UserException("A datasetInjector for class "
            + this.getClass().getName() + " in DatasetPresenter " + _datasetName
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
   */
  void setDatasetName(String datasetName) {
    _datasetName = datasetName;
  }

  /**
   * Set the name of the dataset that is being injected.
   */
  void setPrimaryContact(Contact primaryContact) {
    _primaryContact = primaryContact;
  }

  /**
   * Set the parent DatasetInjectorSet
   */
  void setDatasetInjectorSet(DatasetInjectorSet datasetInjectorSet) {
    _datasetInjectorSet = datasetInjectorSet;
  }

  /***
   * return the first word before the underscore in a dataset name for organism
   * specific .. this is always the orgAbbrev
   * 
   */
  protected String getOrganismAbbrevFromDatasetName() {
    if (_datasetName.isEmpty()) {
      return "";
    }

    try {
      String[] tokens = _datasetName.split("_");
      String organismAbbrev = tokens[0];
     
      if(organismAbbrev.isEmpty()){
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
      LOG.error("Unable to get organism abbrev from dataset name", e);
      return "";
    }

    // These conditions are a band-aid until we can read the organism xml file
    // in EUPathDatasets
    /*
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
    if (_datasetName.isEmpty()) {
      return "";
    }

    try {
      String[] tokens = _datasetName.split("_");
      String organismAbbrev = tokens[0];
     
      if(organismAbbrev.isEmpty()){
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
      LOG.error("Unable to get organism abbrev display from dataset name", e);
      return "";
    }

  }

 protected String getOrganismNamesForFilesFromDatasetName() {
    if (_datasetName.isEmpty()) {
      return "";
    }

    try {
      String[] tokens = _datasetName.split("_");
      String organismAbbrev = tokens[0];
     
      if(organismAbbrev.isEmpty()){
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
            return orgProps.get("organismNameForFiles");
          }
        } 
        return organismAbbrev;
      } 
    } catch (Exception e) {
      LOG.error("Unable to get organism abbrev display from dataset name", e);
      return "";
    }

  }

  protected void setOrganismAbbrevFromDatasetName() {
    String organismAbbrev = this.getOrganismAbbrevFromDatasetName();
    setPropValue("organismAbbrev", organismAbbrev);
    setPropValue("organismAbbrevInternal", organismAbbrev);

    String organismAbbrevDisplay = this.getOrganismAbbrevDisplayFromDatasetName();
    setPropValue("organismAbbrevDisplay", organismAbbrevDisplay);

    String organismNameForFiles = this.getOrganismNamesForFilesFromDatasetName();
    setPropValue("organismNameForFiles", organismNameForFiles);
  }

  /**
  protected void setOrganismAbbrevInternalFromDatasetName() {
    String[] datasetWords = this.datasetName.split("_");
  }
  */

  protected String cleanString(String dirtyText) {
      return dirtyText.replaceAll("\\<.*?\\>", "");
  }

  protected void setGraphDatasetName() {
    String graphDatasetName = _datasetName.replace("-", "");
    setPropValue("graphDatasetName", graphDatasetName);
  }
  
  void setGlobalDatasetProperties(Map<String, Map<String, String>> globalDatasetProps) {
    _globalDatasetProperties = globalDatasetProps;
  }

  protected Map<String, Map<String, String>> getGlobalDatasetProperties() {
    return _globalDatasetProperties;
  }

  protected void setShortAttribution() {
    String shortAttribution = getPropValue("shortAttribution");

    if (shortAttribution == null || shortAttribution.isEmpty()) {
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

        if(!dataset.startsWith(organismAbbrev) || _datasetName.equals(dataset)) {
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

    Map<String, String> exptProps = globalProps.get(_datasetName);
    if (exptProps == null) {
      throw new RuntimeException("No global props exist for dataset '" + _datasetName + "'.");
    }
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
        throw new UserException ("No sample names found for dataset " + _datasetName);
    }
    // remove duplicates (NGS and CNV have dup experiment and samples)
    Set<String> distinctSamples = new HashSet<>();
    distinctSamples.addAll(sampleNames);
    sampleNames.clear();
    sampleNames.addAll(distinctSamples);

    Collections.sort(sampleNames, new NaturalOrderComparator());
    return sampleNames;
  }

    public String getDatasourceName() {
        return _datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this._datasourceName = datasourceName;
    }

    public String getProjectName() {
        return _projectName;
    }

    public void setProjectName(String projectId) {
        this._projectName = projectId;
    }

    public String getDisplayCategory() {
        return _displayCategory;
    }

    public void setDisplayCategory(String displayCategory) {
        this._displayCategory = displayCategory;
    }

    public String getCategory() {
        return _category;
    }

    public void setCategory(String category) {
        this._category = category;
    }
}
