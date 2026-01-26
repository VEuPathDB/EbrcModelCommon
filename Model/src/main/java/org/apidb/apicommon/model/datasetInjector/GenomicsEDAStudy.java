package org.apidb.apicommon.model.datasetInjector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

import java.util.Arrays;

public abstract class GenomicsEDAStudy extends DatasetInjector {

  @Override
  public void injectTemplates() {
      setPublicAccessProperties();
      setEdaStudyInternalAbbrev();
      setEdaEntityAbbrev();
  }


  public void setOrganismListForPartitionedTables() {
      setOrganismAbbrevFromDatasetName();
      String organismAbbrev = getPropValue("organismAbbrev");
      
      setPropValue("orgListForPartitionedTables", convertToSqlInClause(organismAbbrev));
  }

  public String convertToSqlInClause(String orgList) {
    // Trim the list to avoid leading/trailing spaces and handle empty case
    if (orgList == null || orgList.trim().isEmpty()) {
        return "('')"; // or return empty parentheses "()" depending on your needs
    }

    String[] orgs = orgList.split(",");

    String sqlInClause = "(" + Arrays.stream(orgs)
        .map(org -> "'" + org.trim() + "'") // add quotes around each org
        .collect(Collectors.joining(",")) + ")";

    return sqlInClause;
  }

  @Override
  public void addModelReferences() {
      setPublicAccessProperties();
      setEdaStudyInternalAbbrev();
      setEdaEntityAbbrev();
      String className = this.getClass().getSimpleName();
      setPropValue("templateInjectorClassName", className);
  }

  public void setPublicAccessProperties() {
      setPropValue("isPublic", "true");
      setPropValue("studyAccess", "public");
  }

  public void setEdaStudyInternalAbbrev() {
      String datasetName = getDatasetName();
      String stableId = "s" + sha1First10(datasetName);
      setPropValue("edaStudyStableId", stableId);
  }

  public abstract void setEdaEntityAbbrev();

  private String sha1First10(String input) {
      try {
          MessageDigest md = MessageDigest.getInstance("SHA-1");
          byte[] digest = md.digest(input.getBytes());
          StringBuilder sb = new StringBuilder();
          for (byte b : digest) {
              sb.append(String.format("%02x", b));
          }
          return sb.substring(0, 10);
      } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException("SHA-1 algorithm not available", e);
      }
  }

  @Override
  public String[][] getPropertiesDeclaration() {

      String [][] declaration = {
//                                 {"isPublic", ""},
//                                 {"studyAccess", ""},
      };

    return declaration;
  }

}

