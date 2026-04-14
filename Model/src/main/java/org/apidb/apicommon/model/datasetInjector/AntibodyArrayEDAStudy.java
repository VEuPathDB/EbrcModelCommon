package org.apidb.apicommon.model.datasetInjector;

public class AntibodyArrayEDAStudy extends GenomicsEDAStudy {

    protected String getInternalQuestionName() {
        return "GeneQuestions.GenesByAntibodyArrayEdaSubset_" + getDatasetName();
    }

    @Override
    public void setEdaEntityAbbrev() {
        setPropValue("edaEntityAbbrev", "AntbdyAr");
    }

    @Override
    public void injectTemplates() {
        super.injectTemplates();
        String projectName = getPropValue("projectName");
        setPropValue("includeProjects", projectName + ",UniDB");

        String datasetDisplayName = getPropValue("datasetDisplayName");
        String trimmedDatasetDisplayName = datasetDisplayName.replaceAll("<[^>]*>", "");
        setPropValue("datasetDisplayName", trimmedDatasetDisplayName);

        injectTemplate("antibodyArrayEdaQuestion");
        injectTemplate("antibodyArrayEdaAttributeQueriesNumeric");
        injectTemplate("antibodyArrayEdaAttributeRef");
        injectTemplate("antibodyArrayEdaAttributeCategory");
        injectTemplate("antibodyArrayEdaGeneTableSql");
        injectTemplate("antibodyArrayDataTableGeneTableSql");

        setPropValue("questionName", getInternalQuestionName());
        setPropValue("searchCategory", "searchCategory-T-test-2-sample-unequal-variance");
        injectTemplate("internalGeneSearchCategory");
    }

    @Override
    public void addModelReferences() {
        super.addModelReferences();

        addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByAntibodyArrayEdaSubset_" + this.getDatasetName());
        addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "EdaAntibodyArrayDatasets");
        addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "EdaAntibodyArrayGraphsDataTable");
    }

}
