package org.apidb.apicommon.model.datasetInjector;


public class MetabolicPathways extends DBXRefs {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {
    addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByEcNumber");
    addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GeneByLocusTag");
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }

  
}
