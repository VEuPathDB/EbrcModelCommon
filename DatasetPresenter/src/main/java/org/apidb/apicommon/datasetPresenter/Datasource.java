package org.apidb.apicommon.datasetPresenter;

// class to capture info from apidb.datasource table
public class Datasource {
  String name;
  Integer datasourceId;

  public Datasource(Integer datasourceId, String name) {
    this.name = name;
    this.datasourceId = this.datasourceId;
  }
  
  String getName() {
    return name;
  }
  
  Integer getDatasourceId() {
    return datasourceId;
  }

}
