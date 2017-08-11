package org.apidb.apicommon.model.datasetInjector;

import java.util.Map;
import java.util.HashMap;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class NcbiTaxonomy extends DatasetInjector {


  @Override
  public void injectTemplates() {

    Map<String, Map<String, String>> globalProps = getGlobalDatasetProperties();

    Map<String, String> refOrgs = new HashMap<String, String>();
    Map<String, String> refOrgsAnnot = new HashMap<String, String>();

    Map<String, String> iedbOrgs = new HashMap<String, String>();
    Map<String, String> tfbsOrgs = new HashMap<String, String>();
    Map<String, String> ecOrgs = new HashMap<String, String>();

    // Find all reference organisms and inject for organism param default
    for (Map.Entry<String, Map<String, String>> props : globalProps.entrySet()) {
        Map<String, String> map = props.getValue();


        if(props.getKey().endsWith("_epitope_IEDB_RSRC")) {
            String organismAbbrev = map.get("organismAbbrev");
            String orgPropsKey = organismAbbrev + "_RSRC";

            // TODO:  this should be removed once the dataset files are correct.  Currently some FUngi ones are missing organism datasets
            if(globalProps.get(orgPropsKey) != null) {

                String organismFullName = globalProps.get(orgPropsKey).get("organismFullName");
                
                String iedbProjectName = globalProps.get(orgPropsKey).get("projectName");

                if(globalProps.get(orgPropsKey).get("isReferenceStrain").toLowerCase().equals("true")) {
                    if(iedbOrgs.containsKey(iedbProjectName)) {
                        String iedbOrgsString = iedbOrgs.get(iedbProjectName);
                        iedbOrgsString = iedbOrgsString + "," + organismFullName;
                        iedbOrgs.put(iedbProjectName, iedbOrgsString);
                        iedbOrgs.put("EuPathDB", iedbOrgsString);
                    }
                    else {
                        iedbOrgs.put(iedbProjectName, organismFullName);
                        iedbOrgs.put("EuPathDB", organismFullName);
                    }
                }
            }
        }

        if(map.containsKey("projectName") && props.getKey().startsWith(map.get("projectName")) ) {

            String projectName = map.get("projectName");


            if(props.getKey().endsWith("_Llinas_TransFactorBindingSites_GFF2_RSRC")) {
                String organismAbbrev = map.get("organismAbbrev");
                String orgPropsKey = projectName + ":" + organismAbbrev + "_RSRC";
                String organismFullName = globalProps.get(orgPropsKey).get("organismFullName");

                if(globalProps.get(orgPropsKey).get("isReferenceStrain").toLowerCase().equals("true")) {
                    if(tfbsOrgs.containsKey(projectName)) {
                        String tfbsOrgsString = tfbsOrgs.get(projectName);
                        tfbsOrgsString = tfbsOrgsString + "," + organismFullName;
                        tfbsOrgs.put(projectName, tfbsOrgsString);
                        tfbsOrgs.put("EuPathDB", tfbsOrgsString);
                    }
                    else {
                        tfbsOrgs.put(projectName, organismFullName);
                        tfbsOrgs.put("EuPathDB", organismFullName);
                    }
                }
            }


            if(props.getKey().endsWith("_ECAssociations_RSRC")) {
                String organismAbbrev = map.get("organismAbbrev");
                String orgPropsKey = projectName + ":" + organismAbbrev + "_RSRC";
                String organismFullName = globalProps.get(orgPropsKey).get("organismFullName");

                if(globalProps.get(orgPropsKey).get("isReferenceStrain").toLowerCase().equals("true")) {
                    if(ecOrgs.containsKey(projectName)) {
                        String ecOrgsString = ecOrgs.get(projectName);
                        ecOrgsString = ecOrgsString + "," + organismFullName;
                        ecOrgs.put(projectName, ecOrgsString);
                        ecOrgs.put("EuPathDB", ecOrgsString);
                    }
                    else {
                        ecOrgs.put(projectName, organismFullName);
                        ecOrgs.put("EuPathDB", organismFullName);
                    }
                }
            }

            if(map.containsKey("isReferenceStrain") && 
               map.get("isReferenceStrain").toLowerCase().equals("true")) {
            

                String organismFullName = map.get("organismFullName");

                if(map.get("isAnnotatedGenome").toLowerCase().equals("true")) {
                    if(refOrgsAnnot.containsKey(projectName)) {
                        String refOrgsAnnotString = refOrgsAnnot.get(projectName);
                        
                        refOrgsAnnotString = refOrgsAnnotString + "," + organismFullName;
                        refOrgsAnnot.put(projectName, refOrgsAnnotString);
                        refOrgsAnnot.put("EuPathDB", refOrgsAnnotString);
                    }
                    else {
                        refOrgsAnnot.put(projectName, organismFullName);
                        refOrgsAnnot.put("EuPathDB", organismFullName);
                    }
                }
                
                if(refOrgs.containsKey(projectName)) {
                    String refOrgsString = refOrgs.get(projectName);
                    
                    refOrgsString = refOrgsString + "," + organismFullName;
                    refOrgs.put(projectName, refOrgsString);
                    refOrgs.put("EuPathDB", refOrgsString);
                }
                else {
                    refOrgs.put(projectName, organismFullName);
                    refOrgs.put("EuPathDB", organismFullName);
                }
            }
        }
    }



    /**  Reference Organism Defaults
    // All Annotated Reference Organisms
    for (Map.Entry<String, String> refOrg : refOrgsAnnot.entrySet()) {
        setPropValue("projectName", refOrg.getKey());

        if(!refOrg.getKey().equals("EuPathDB")) {
            setPropValue("referenceOrganisms", refOrg.getValue());
                        injectTemplate("referenceOrganisms");
            //        System.out.println("Injecting annot ref orgs:  "  + refOrg.getKey() + "\t" + refOrg.getValue());
        }
    }

    // All Reference Organisms
    for (Map.Entry<String, String> refOrg : refOrgs.entrySet()) {
        setPropValue("projectName", refOrg.getKey());
        setPropValue("referenceOrganisms", refOrg.getValue());

        //        System.out.println("Injecting ref orgs:  "  + refOrg.getKey() + "\t" + refOrg.getValue());

        if(refOrg.getKey().equals("EuPathDB")) {
            //            injectTemplate("genomicOrganismOverridePortal");
        }
        else {
            injectTemplate("genomicOrganismOverride");
        }
    }

    for (Map.Entry<String, String> refOrg : tfbsOrgs.entrySet()) {
        setPropValue("projectName", refOrg.getKey());
        setPropValue("referenceOrganisms", refOrg.getValue());

        if(refOrg.getKey().equals("EuPathDB")) {
            //            injectTemplate("geneTfbsOrganismOverridePortal");
        }
        else {
            injectTemplate("geneTfbsOrganismOverride");
        }

    }

    for (Map.Entry<String, String> refOrg : ecOrgs.entrySet()) {
        setPropValue("projectName", refOrg.getKey());
        setPropValue("referenceOrganisms", refOrg.getValue());

        if(refOrg.getKey().equals("EuPathDB")) {
            //            injectTemplate("geneEcOrganismOverridePortal");
        }
        else {
            injectTemplate("geneEcOrganismOverride");
        }


    }


    for (Map.Entry<String, String> refOrg : iedbOrgs.entrySet()) {
        setPropValue("projectName", refOrg.getKey());
        setPropValue("referenceOrganisms", refOrg.getValue());

        if(refOrg.getKey().equals("EuPathDB")) {
            //            injectTemplate("geneEpitopeOrganismOverridePortal");
        }
        else {
            injectTemplate("geneEpitopeOrganismOverride");
        }
    }
    **/


  }

  @Override
  public void addModelReferences() {
      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByTaxon"); 
      addWdkReference("SequenceRecordClasses.SequenceRecordClass", "question", "GenomicSequenceQuestions.SequencesByTaxon"); 
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByTaxon"); 
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
