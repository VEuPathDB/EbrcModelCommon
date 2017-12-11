package org.apidb.apicommon.datasetPresenter;

public class ModelReference {
    private String recordClassName;
    private String targetName;
    private String targetType;
    private String[] scopes;
    private String categoryIri;
  
    public ModelReference(String recordClassName, String targetType, String targetName, String datasetName, String[] scopes, String categoryIri) {
        this.recordClassName = recordClassName;
        this.targetType = targetType;
        this.targetName = targetName;
        this.scopes = scopes;
        this.categoryIri = categoryIri;

        if (recordClassName == null) {
            throw new UserException("Dataset " + datasetName + " contains a WDK model reference with a NULL record class name");
        }
        if (targetName == null) {
            throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target name");
        }
        if (targetType == null) {
            throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target type");
        }
    }


    public ModelReference(String targetType, String targetName, String datasetName, String[] scopes, String categoryIri) {
        this.targetName = targetName;
        this.targetType = targetType;
        this.scopes = scopes;
        this.categoryIri = categoryIri;        

        if (targetName == null) {
            throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target name");
        }
        if (targetType == null) {
            throw new UserException("Dataset " + datasetName + " contains a model reference with a NULL target type");
        }
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

    String getCategoryIri() {
        return categoryIri;
    }

    String[] getScopes() {
        return scopes;
    }
}
