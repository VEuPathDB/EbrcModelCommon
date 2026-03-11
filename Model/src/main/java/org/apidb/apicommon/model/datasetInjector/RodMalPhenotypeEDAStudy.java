package org.apidb.apicommon.model.datasetInjector;

public class RodMalPhenotypeEDAStudy extends PhenotypeEDAStudy {

    @Override
    public void addModelReferences() {
        super.addModelReferences();
        addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "RodMalPhenotype");
    }

}
