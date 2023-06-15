package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class Pathways extends DatasetInjector {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {
    addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "MetabolicPathways");
    addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByMetabolicPathway");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "question", "PathwayQuestions.PathwaysByPathwayID");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "question", "PathwayQuestions.PathwaysByGeneList");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "question", "PathwayQuestions.PathwaysByCompounds");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "MetabolicPathways");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByPathway");

    //don't show xrefs table for MPMP pathways
    String datasetName = getDatasetName();
    if (!datasetName.contains("MPMP")) {
        addWdkReference("PathwayRecordClasses.PathwayRecordClass", "table", "PathwayReactionsXrefs");
    }
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}

