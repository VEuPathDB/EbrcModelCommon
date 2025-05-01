package org.apidb.apicommon.model.datasetInjector;

public class PhenotypeEDAStudy extends GenomicsEDAStudy {

    protected String getInternalQuestionName() {
        return "GeneQuestions.GenesByPhenotypeEdaSubset_" + getDatasetName();
    }

    @Override
    public void injectTemplates() {
        String projectName = getPropValue("projectName");
        setPropValue("includeProjects", projectName + ",UniDB");

        String datasetDisplayName = getPropValue("datasetDisplayName");
        String trimmedDatasetDisplayName = datasetDisplayName.replaceAll("<[^>]*>", "");
        setPropValue("datasetDisplayName", trimmedDatasetDisplayName);        

	// COMMENT for now; TO BE FIXED
        // injectTemplate("phenotypeEdaQuestion");

        setPropValue("questionName", getInternalQuestionName());
        setPropValue("searchCategory", "searchCategory-phenotype-quantitative");
        injectTemplate("internalGeneSearchCategory");
    }

    @Override
    public void addModelReferences() {
        super.addModelReferences();

        addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByPhenotypeEdaSubset_" + this.getDatasetName());
    }

}
