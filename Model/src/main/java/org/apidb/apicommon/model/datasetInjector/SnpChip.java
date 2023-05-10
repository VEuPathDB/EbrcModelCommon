package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class SnpChip extends DatasetInjector {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {

      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesBySnps");

      addWdkReference("SnpChipRecordClasses.SnpChipRecordClass", "question", "SnpChipQuestions.SnpBySourceId");
      addWdkReference("SnpChipRecordClasses.SnpChipRecordClass", "question", "SnpChipQuestions.SnpsByGeneId");
      addWdkReference("SnpChipRecordClasses.SnpChipRecordClass", "question", "SnpChipQuestions.SnpsByLocation");
      addWdkReference("SnpChipRecordClasses.SnpChipRecordClass", "question", "SnpChipQuestions.SnpsByStrain");
      addWdkReference("SnpChipRecordClasses.SnpChipRecordClass", "question", "SnpChipQuestions.SnpsByIsolatePattern");

  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}
