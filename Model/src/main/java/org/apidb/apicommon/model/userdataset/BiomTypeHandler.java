package org.apidb.apicommon.model.userdataset;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.*;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BiomTypeHandler extends UserDatasetTypeHandler {
  public final static String NAME = "BIOM";
  private final static String VERSION = "1.0, 2.0, or 2.1";
  public final static String DISPLAY = "BIOM";

  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset, DataSource appDbDataSource) {
    return new UserDatasetCompatibility(true, "");
  }

  @Override
  public UserDatasetType getUserDatasetType() {
    return UserDatasetTypeFactory.getUserDatasetType(NAME, VERSION);
  }
  
  @Override
  public String getDisplay() {
	return DISPLAY;
  }

  @Override
  public String[] getInstallInAppDbCommand(UserDataset userDataset,
                                           Map<String, Path> fileNameToTempFileMap,
                                           String projectId,
                                           Path workingDir) {

      Path metadataJson = fileNameToTempFileMap.get("metadata.json");
      if (metadataJson == null) {
          throw new RuntimeException("Failed to get: metadata.json");
      }
      Path dataTsv = fileNameToTempFileMap.get("data.tsv");
      if (dataTsv == null) {
          throw new RuntimeException("Failed to get: data.tsv");
      }
    try {
      final UserDatasetMeta meta = userDataset.getMeta();
      final JSONObject metaObject = new JSONObject();
      final Path metaJsonTmpFile = Path.of(workingDir.toString(), "tmp-meta.json");
      metaObject.put("name", meta.getName());
      metaObject.put("description", meta.getDescription());
      metaObject.put("summary", meta.getSummary());
      Files.write(metaJsonTmpFile, metaObject.toString().getBytes(StandardCharsets.UTF_8));
      final String imageTag = Optional.ofNullable(System.getenv("ISA_IMAGE_TAG"))
          .filter(tagEnv -> !tagEnv.isBlank())
          .orElse("latest");
      String[] cmd = {"singularity", "run",
          "--bind", workingDir + ":/work",
          "--bind", constructGusConfigBinding(),
          "--bind", constructOracleHomeBinding(),
          "docker://veupathdb/dataset-installer-isasimple:" + imageTag,
          "loadStudyFromBiom.bash",
          metadataJson.toString(),
          dataTsv.toString(),
          userDataset.getUserDatasetId().toString(),
          metaJsonTmpFile.toString()};
      return cmd;
    } catch (WdkModelException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Set<String> getInstallInAppDbFileNames(UserDataset userDataset) {
    try {
      return userDataset.getFiles().values().stream()
          .map(file -> file.getFileName())
          .collect(Collectors.toSet());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String[] getUninstallInAppDbCommand(Long userDatasetId, String projectId) {
    final String imageTag = Optional.ofNullable(System.getenv("ISA_IMAGE_TAG"))
        .filter(tagEnv -> !tagEnv.isBlank())
        .orElse("latest");
    String[] cmd = {"singularity", "run",
        "--bind", constructGusConfigBinding(),
        "--bind", constructOracleHomeBinding(),
        "docker://veupathdb/dataset-installer-isasimple:" + imageTag,
        "deleteStudy.pl",
        userDatasetId.toString()};
    return cmd;
  }

  private String constructGusConfigBinding() {
    return String.format("%s/config/%s/gus.config:/gusApp/gus_home/config/gus.config",
        System.getenv("GUS_HOME"), System.getenv("PROJECT_ID"));
  }

  private String constructOracleHomeBinding() {
    return System.getenv("ORACLE_HOME") + "/network/admin:/opt/oracle/instantclient_21_6/network/admin";
  }

  @Override
  public String[] getRelevantQuestionNames(UserDataset userDataset) {
    String[] empty = {};
    return empty;
  }
}
