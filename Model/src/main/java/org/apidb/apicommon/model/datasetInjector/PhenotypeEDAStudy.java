package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class PhenotypeEDAStudy extends GenomicsEDAStudy {

  @Override
  public void injectTemplates() {
      super.injectTemplates();

  }

  @Override
  public void addModelReferences() {
      super.addModelReferences();

      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByPhenotypeEdaSubset_" + this.getDatasetName());
  }


}
