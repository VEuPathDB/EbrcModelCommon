package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class GenomicsEDAStudy extends DatasetInjector {


  @Override
  public void injectTemplates() {
      // boolean injectStudy = getPropValueAsBoolean("injectStudy");
      // if(!injectStudy){
      //     return;
      // }

      injectCardQuestions();
      injectProjectAvailability();
  }

    private void injectCardQuestions() {
        String presenterId = getPropValue("presenterId");
        String cardQuestions = "UNION select '" + presenterId + "' as dataset_presenter_id, 'cardQuestions' as property, '{ ";
        //cardQuestions = cardQuestions + getCardQuestionString();
        cardQuestions = cardQuestions + " }' as value from dual";
        //System.err.println("cardQuestionsSql=" + cardQuestions);
        setPropValue("cardQuestionsSql",cardQuestions);
        injectTemplate("injectDatasetQuestions");
    }

    private void injectProjectAvailability() {
        String presenterId = getPropValue("presenterId");
        String projectAvailabilityProp = getPropValue("projectAvailability");

        String projectAvailability = "UNION select '" + presenterId + "' as dataset_presenter_id, 'projectAvailability' as property, '" + projectAvailabilityProp + "' as value from dual";
        setPropValue("projectAvailabilitySql",projectAvailability);
        injectTemplate("injectProjectAvailability");
    }


  @Override
  public void addModelReferences() {
      String className = this.getClass().getSimpleName();
      setPropValue("templateInjectorClassName", className);
  }

  @Override
  public String[][] getPropertiesDeclaration() {

      String [][] declaration = {
                                //  {"injectStudy", ""},
                                 {"isPublic", ""},
                                //  {"studyAbbreviation", ""},
                                 {"studyCategories", ""},
                                 {"studyAccess", ""},
                                 {"policyUrl", ""},
                                 {"cardHeadline", ""},
                                 {"cardPoints", ""},
                                //  {"requestProtectionLevel", ""},
                                //  {"requestAccessFields", ""},
                                //  {"requestEmail", ""},
                                //  {"requestEmailBody", ""},
                                //  {"requestNeedsApproval", ""},

      };

    return declaration;
  }

}
