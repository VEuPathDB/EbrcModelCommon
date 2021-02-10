package org.apidb.apicommon.model.datasetInjector;

import java.util.Map;
import java.util.HashMap;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class NcbiTaxonomy extends DatasetInjector {

  private static final boolean SKIP_ORGANISM_DEFAULT_REFS = true;

  @Override
  public void injectTemplates() {

    if (SKIP_ORGANISM_DEFAULT_REFS) return;

    Map<String, Map<String, String>> globalProps = getGlobalDatasetProperties();

    Map<String, String> refOrgs = new HashMap<String, String>();
    Map<String, String> refOrgsAnnot = new HashMap<String, String>();

    Map<String, String> iedbOrgs = new HashMap<String, String>();
    Map<String, String> tfbsOrgs = new HashMap<String, String>();
    Map<String, String> ecOrgs = new HashMap<String, String>();

    // Find all reference organisms and inject for organism param default
    for (Map.Entry<String, Map<String, String>> propSetMap : globalProps.entrySet()) {

      String propSetName = propSetMap.getKey();
      Map<String, String> propSet = propSetMap.getValue();

      if (propSetName.endsWith("_epitope_IEDB_RSRC")) {

        String organismAbbrev = getPropOrThrow(propSetName, propSet, "organismAbbrev");
        String orgPropsKey = organismAbbrev + "_RSRC";
        Map<String, String> orgProps = getOrgPropsOrThrow(globalProps, orgPropsKey);

        String projectName = getPropOrThrow(orgPropsKey, orgProps, "projectName");
        String organismFullName = getPropOrThrow(orgPropsKey, orgProps, "organismFullName");
        boolean isReferenceStrain = "true".equalsIgnoreCase(orgProps.get("isReferenceStrain"));

        if (isReferenceStrain) {
          addOrganism(iedbOrgs, projectName, organismFullName);
        }
      }

      if (propSet.containsKey("projectName") && propSetName.startsWith(propSet.get("projectName"))) {

        String projectName = getPropOrThrow(propSetName, propSet, "projectName");

        if (!projectName.equals("UniDB")) {
          injectTemplate("projectIdForPrimaryKey");
        }

        String organismAbbrev = getPropOrThrow(propSetName, propSet, "organismAbbrev");
        String orgPropsKey = projectName + ":" + organismAbbrev + "_RSRC";
        Map<String, String> orgProps = getOrgPropsOrThrow(globalProps, orgPropsKey);

        String organismFullName = getPropOrThrow(orgPropsKey, orgProps, "organismFullName");
        boolean isReferenceStrain = "true".equalsIgnoreCase(orgProps.get("isReferenceStrain"));

        if (propSetName.endsWith("_Llinas_TransFactorBindingSites_GFF2_RSRC") && isReferenceStrain) {
          addOrganism(tfbsOrgs, projectName, organismFullName);
        }

        if (propSetName.endsWith("_ECAssociations_RSRC") && isReferenceStrain) {
          addOrganism(ecOrgs, projectName, organismFullName);
        }

        if ("true".equalsIgnoreCase(propSet.get("isReferenceStrain"))) {

          organismFullName = getPropOrThrow(propSetName, propSet, "organismFullName");

          if ("true".equalsIgnoreCase(propSet.get("isAnnotatedGenome"))) {
            addOrganism(refOrgsAnnot, projectName, organismFullName);
          }

          addOrganism(refOrgs, projectName, organismFullName);
        }
      }
    }

    // Reference Organism Defaults

    // All Annotated Reference Organisms
    for (Map.Entry<String, String> refOrg : refOrgsAnnot.entrySet()) {
      setPropValue("projectName", refOrg.getKey());

      if (!refOrg.getKey().equals("EuPathDB")) {
      setPropValue("referenceOrganisms", refOrg.getValue());
      injectTemplate("referenceOrganisms");
      System.out.println("Injecting annot ref orgs: " + refOrg.getKey() + "\t" + refOrg.getValue()); } }
    
    // All Reference Organisms
    for (Map.Entry<String, String> refOrg : refOrgs.entrySet()) {
      setPropValue("projectName", refOrg.getKey()); setPropValue("referenceOrganisms", refOrg.getValue());
      System.out.println("Injecting ref orgs: " + refOrg.getKey() + "\t" + refOrg.getValue());

      if (refOrg.getKey().equals("EuPathDB")) { // injectTemplate("genomicOrganismOverridePortal"); } else {
        injectTemplate("genomicOrganismOverride");
      }
    }

    for (Map.Entry<String, String> refOrg : tfbsOrgs.entrySet()) {
      setPropValue("projectName", refOrg.getKey());
      setPropValue("referenceOrganisms", refOrg.getValue());

      if (refOrg.getKey().equals("EuPathDB")) {
        injectTemplate("geneTfbsOrganismOverridePortal");
      }
      else {
        injectTemplate("geneTfbsOrganismOverride");
      }
    }

    for (Map.Entry<String, String> refOrg : ecOrgs.entrySet()) {
      setPropValue("projectName", refOrg.getKey());
      setPropValue("referenceOrganisms", refOrg.getValue());

      if (refOrg.getKey().equals("EuPathDB")) {
        injectTemplate("geneEcOrganismOverridePortal");
      }
      else {
        injectTemplate("geneEcOrganismOverride");
      }
    }

    for (Map.Entry<String, String> refOrg : iedbOrgs.entrySet()) {
      setPropValue("projectName", refOrg.getKey());
      setPropValue("referenceOrganisms", refOrg.getValue());

      if (refOrg.getKey().equals("EuPathDB")) {
        injectTemplate("geneEpitopeOrganismOverridePortal");
      }
      else {
        injectTemplate("geneEpitopeOrganismOverride");
      }
    }
  }

  private static String getPropOrThrow(String propSetName, Map<String, String> propSet, String key) {
    String value = propSet.get(key);
    if (key == null) {
      throw new RuntimeException("Prop set for '" + propSetName + "' does not contain property '" + key + "'.");
    }
    return value;
  }

  private static Map<String, String> getOrgPropsOrThrow(Map<String, Map<String, String>> globalProps, String orgPropsKey) {
    Map<String,String> orgProps = globalProps.get(orgPropsKey);
    if (orgProps == null) {
      throw new RuntimeException("Global dataset properties does not contain an organism propset with key '" + orgPropsKey + "'.");
    }
    return orgProps;
  }

  private static void addOrganism(Map<String, String> orgMap, String projectName, String organismFullName) {
    if (orgMap.containsKey(projectName)) {
      String orgsString = orgMap.get(projectName);
      orgsString = orgsString + "," + organismFullName;
      orgMap.put(projectName, orgsString);
      orgMap.put("EuPathDB", orgsString);
    }
    else {
      orgMap.put(projectName, organismFullName);
      orgMap.put("EuPathDB", organismFullName);
    }
  }

  @Override
  public void addModelReferences() {
    addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
        "GeneQuestions.GenesByTaxon");
    addWdkReference("SequenceRecordClasses.SequenceRecordClass", "question",
        "GenomicSequenceQuestions.SequencesByTaxon");
    //addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByTaxon");
    addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "attribute", "overview");

    addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "Taxonomy");
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }

}
