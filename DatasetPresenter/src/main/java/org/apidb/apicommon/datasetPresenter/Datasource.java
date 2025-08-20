package org.apidb.apicommon.datasetPresenter;

// class to capture info from apidb.datasource table
public class Datasource {
  String name;
  Integer datasourceId;
  String projectId;
  Integer taxonId;

  public Datasource(Integer datasourceId, String name, String projectId, Integer taxonId) {
    this.name = name;
    this.datasourceId = datasourceId;
    this.projectId = projectId;
    this.taxonId = taxonId;
  }
  
  String getName() {
    return name;
  }
  
  Integer getDatasourceId() {
    return datasourceId;
  }

  String getProjectId() { return projectId; }

  Integer getTaxonId() { return taxonId; }
}
