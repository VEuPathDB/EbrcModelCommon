package org.apidb.apicommon.datasetPresenter;

public class ModelReference {
  private String recordClassName;
  private String targetName;
  private String targetType;
  
  public ModelReference(String recordClassName, String targetType, String targetName, String datasetName) {
    this(targetType, targetName, datasetName);
    this.recordClassName = recordClassName;
    if (recordClassName == null) {
      throw new UserException("Dataset " + datasetName + " contains a WDK model reference with a NULL record class name");
    }
  }
  
  public ModelReference(String targetType, String targetName, String datasetName) {
    if (targetName == null) {
      throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target name");
    }
    if (targetType == null) {
      throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target type");
    }
    this.targetName = targetName;
    this.targetType = targetType;
  }
  
  String getRecordClassName() {
    return recordClassName;
  }
  
  String getTargetName() {
    return targetName;
  }

  String getTargetType() {
    return targetType;
  }
}
