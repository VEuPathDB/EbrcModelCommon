package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class PdbProteins extends DatasetInjector {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {
      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByPdbSimilarity"); 
      addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "PdbSimilarities"); 
      addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "Ssgcid"); 
  }

  @Override
  // second column is for documentation
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}
