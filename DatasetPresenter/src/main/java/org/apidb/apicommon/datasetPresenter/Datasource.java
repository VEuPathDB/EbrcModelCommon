package org.apidb.apicommon.datasetPresenter;

// class to capture info from apidb.datasource table
public class Datasource {
  String name;
  Integer datasourceId;
  String projectId;

  public Datasource(Integer datasourceId, String name, String projectId) {
    this.name = name;
    this.datasourceId = datasourceId;
    this.projectId = projectId;
  }
  
  String getName() {
    return name;
  }
  
  Integer getDatasourceId() {
    return datasourceId;
  }

  String getProjectId() { return projectId; }
}
