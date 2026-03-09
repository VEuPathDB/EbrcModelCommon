package org.apidb.apicommon.model.datasetInjector;

public class PhenotypeWithImagesEDAStudy extends PhenotypeEDAStudy {

    @Override
    public void setEdaEntityAbbrev() {
        setPropValue("edaEntityAbbrev", "gnPhntID");
    }


}
