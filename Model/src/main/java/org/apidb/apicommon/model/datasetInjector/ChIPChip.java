package org.apidb.apicommon.model.datasetInjector;

public class ChIPChip extends GenomicsEDAStudy {

  @Override
  public void setEdaEntityAbbrev() {
      setPropValue("edaEntityAbbrev", "sample");
  }


  @Override
  public void injectTemplates() {
    super.injectTemplates();
    setPropValue ("organismAbbrevDisplay", getOrganismAbbrevDisplayFromDatasetName());
    String featureName = getPropValue("datasetDisplayName").replace(' ', '_');
    setPropValue ("featureName", featureName);

    String projectName = getPropValue("projectName");
    setPropValue("includeProjects", projectName + ",UniDB");

    setPropValue("includeProjectsExcludeEuPathDB", projectName + ",UniDB");

    setPropValue("searchCategory", "Epigenomics");

    String cleanDatasetName = getDatasetName().replace('.', '_');
    setPropValue ("cleanDatasetName", cleanDatasetName);

    setPropValue("questionName", "GeneQuestions.GenesByChIPchip"+ cleanDatasetName);

    //    injectTemplate("internalGeneSearchCategory");

    String key = getPropValue("key");
    if (key.length() != 0) {
        setPropValue ("key", " - " + key);
    }

    injectTemplate("chipChipSmoothed");

    if (getPropValueAsBoolean("hasCalledPeaks")) {
        injectTemplate("chipchipFilterParam");
        injectTemplate("chipchipParamQueries");
        injectTemplate("chipchipQuery");
        injectTemplate("chipchipQuestion");
	injectTemplate("chipchipCategories");
    }

    //String presenterId = getPropValue("presenterId");
    setPropValue("summary", getPropValue("summary").replaceAll("\n", " "));
    setPropValue("summary", getPropValue("summary").replaceAll(" +", " "));
    injectTemplate("jbrowseChipChipExperSampleBuildProps");

  }

  @Override
  public void addModelReferences() {

      String cleanDatasetName = getDatasetName().replace('.', '_');
      setPropValue ("cleanDatasetName", cleanDatasetName);

      if (getPropValueAsBoolean("hasCalledPeaks")) {
	  addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByChIPchip"+ cleanDatasetName);
      }
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}
