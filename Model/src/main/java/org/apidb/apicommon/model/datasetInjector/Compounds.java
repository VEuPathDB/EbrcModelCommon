package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class Compounds extends DatasetInjector {

  @Override
  public void injectTemplates() {
  }

  @Override
  public void addModelReferences() {
    addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "question", "PathwayQuestions.PathwaysByCompounds");
    addWdkReference("PathwayRecordClasses.PathwayRecordClass", "question", "PathwayQuestions.PathwaysByCompoundIds");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "Properties");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "IupacNames");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "Definition");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "SecondaryIds");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "Synonyms");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "Structures");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "table", "CompoundsMetabolicPathways");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByCompoundID");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByText");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByEcReaction");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByPathway");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByMolecularFormula");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByMolecularWeight");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByGeneID");
    addWdkReference("CompoundRecordClasses.CompoundRecordClass", "question", "CompoundQuestions.CompoundsByPathwayID");
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}

