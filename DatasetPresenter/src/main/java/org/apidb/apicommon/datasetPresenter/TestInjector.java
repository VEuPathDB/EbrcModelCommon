package org.apidb.apicommon.datasetPresenter;

public class TestInjector extends DatasetInjector {

  @Override
  public void injectTemplates() {
    injectTemplate("test3_template1");
    injectTemplate("test3_template2");
  }

  @Override
  public void addModelReferences() {
    addWdkReference("GeneRecord", "question", "someQuestion");
    addModelReference("track", "someTrack");
  }
  
  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }
}
