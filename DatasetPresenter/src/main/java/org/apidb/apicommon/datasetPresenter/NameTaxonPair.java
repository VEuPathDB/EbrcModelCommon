package org.apidb.apicommon.datasetPresenter;

public class NameTaxonPair {
  String name;
  Integer taxonId;
  
  public NameTaxonPair(String name, Integer taxonId) {
    this.name = name;
    this.taxonId = taxonId;
  }
  
  String getName() {
    return name;
  }
  
  Integer getTaxonId() {
    return taxonId;
  }

}
