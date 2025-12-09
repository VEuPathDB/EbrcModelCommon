package org.apidb.apicommon.model.datasetInjector;

public class AntibodyArrayEDAStudy extends GenomicsEDAStudy {

    protected String getInternalQuestionName() {
        return "GeneQuestions.GenesByAntibodyArrayEdaSubset_" + getDatasetName();
    }

    @Override
    public void injectTemplates() {
        String projectName = getPropValue("projectName");
        setPropValue("includeProjects", projectName + ",UniDB");

        String datasetDisplayName = getPropValue("datasetDisplayName");
        String trimmedDatasetDisplayName = datasetDisplayName.replaceAll("<[^>]*>", "");
        setPropValue("datasetDisplayName", trimmedDatasetDisplayName);

        injectTemplate("antibodyArrayEdaQuestion");

        setPropValue("questionName", getInternalQuestionName());
        setPropValue("searchCategory", "searchCategory-T-test-2-sample-unequal-variance");
        injectTemplate("internalGeneSearchCategory");
    }

    @Override
    public void addModelReferences() {
        super.addModelReferences();

        addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByAntibodyArrayEdaSubset_" + this.getDatasetName());
    }

}
