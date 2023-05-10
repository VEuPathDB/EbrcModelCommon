package org.apidb.apicommon.model.datasetInjector;

public class Popset extends SnpChip {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {

      // add all references from SnpChip first
      // super.addModelReferences();

      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByPopsetId");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByTaxon");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByHost");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByIsolationSource");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByProduct");
      // addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByGenotypeNumber");
      // addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByRFLPGenotype");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByStudy");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetByCountry");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetIsolatesByText");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "question", "PopsetQuestions.PopsetsBySimilarity");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "table", "Reference");
      addWdkReference("PopsetRecordClasses.PopsetRecordClass", "attribute", "overview");

      addWdkReference("SampleRecordClasses.SampleRecordClass", "table", "Datasets");
      addWdkReference("SampleRecordClasses.SampleRecordClass", "table", "Characteristics");
      addWdkReference("SampleRecordClasses.SampleRecordClass", "table", "ProcessedSample");
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }

}
