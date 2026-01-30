package org.apidb.apicommon.model.datasetInjector;

public class PhenotypeEDAStudy extends GenomicsEDAStudy {

    protected String getInternalQuestionName() {
        return "GeneQuestions.GenesByPhenotypeEdaSubset_" + getDatasetName();
    }

    @Override
    public void setEdaEntityAbbrev() {
        setPropValue("edaEntityAbbrev", "gnPhntyD");
    }

    @Override
    public void injectTemplates() {
        super.injectTemplates();
        String projectName = getPropValue("projectName");
        setPropValue("includeProjects", projectName + ",UniDB");

        String datasetDisplayName = getPropValue("datasetDisplayName");
        String trimmedDatasetDisplayName = datasetDisplayName.replaceAll("<[^>]*>", "");
        setPropValue("datasetDisplayName", trimmedDatasetDisplayName);        

        injectTemplate("phenotypeEdaQuestion");
        injectTemplate("phenotypeEdaGeneTableSql");
        injectTemplate("phenotypeDataTableGeneTableSql");
        injectTemplate("phenotypeEdaAttributeQueriesNumeric");
        injectTemplate("phenotypeEdaAttributeQueriesString");
        injectTemplate("phenotypeEdaAttributeRef");
        injectTemplate("phenotypeEdaAttributeCategory");

        setPropValue("questionName", getInternalQuestionName());
        setPropValue("searchCategory", "searchCategory-phenotype-molecular");
        injectTemplate("internalGeneSearchCategory");
    }

    @Override
    public void addModelReferences() {
        super.addModelReferences();

        addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByPhenotypeEdaSubset_" + this.getDatasetName());
    }

}
