package org.apidb.apicommon.model.datasetInjector;

public class CellularLocalizationEDAStudy extends GenomicsEDAStudy {

    protected String getInternalQuestionName() {
        return "GeneQuestions.GenesByCellularLocalizationEdaSubset_" + getDatasetName();
    }

    @Override
    public void setEdaEntityAbbrev() {
        setPropValue("edaEntityAbbrev", "lopitDat");
    }

    @Override
    public void injectTemplates() {
        super.injectTemplates();
        String projectName = getPropValue("projectName");
        setPropValue("includeProjects", projectName + ",UniDB");

        String datasetDisplayName = getPropValue("datasetDisplayName");
        String trimmedDatasetDisplayName = datasetDisplayName.replaceAll("<[^>]*>", "");
        setPropValue("datasetDisplayName", trimmedDatasetDisplayName);

        // Inject all templates in order
        injectTemplate("cellularLocalizationEdaQuestion");
        injectTemplate("cellularLocalizationEdaGeneTableSql");
        injectTemplate("cellularLocalizationDataTableGeneTableSql");
        injectTemplate("cellularLocalizationEdaAttributeQueriesNumeric");
        injectTemplate("cellularLocalizationEdaAttributeQueriesString");
        injectTemplate("cellularLocalizationEdaAttributeRef");
        injectTemplate("cellularLocalizationEdaAttributeCategory");

        setPropValue("questionName", getInternalQuestionName());
        setPropValue("searchCategory", "searchCategory-cellular-localization-quantitative");
        injectTemplate("internalGeneSearchCategory");
    }

    @Override
    public void addModelReferences() {
        super.addModelReferences();

        addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByCellularLocalizationEdaSubset_" + this.getDatasetName());
    }

}
