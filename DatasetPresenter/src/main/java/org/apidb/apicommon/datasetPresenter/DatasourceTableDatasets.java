package org.apidb.apicommon.datasetPresenter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A {@link LoadedDatasetSource} backed by apidb.Datasource, which is what the workflow's
 * root graph actually loaded. Opt-in only — see {@link LoadedDatasetSources}.
 *
 * Every dataset name is read once up front and matched in memory, rather than issuing a
 * LIKE query per presenter, because presenters number in the thousands.
 *
 * Connection details come from the site's own model-config.xml (the database the model
 * will run against), following the same precedence as WDK's ModelConfigDB: an explicit
 * connectionUrl wins, then host + database name. LDAP resolution is not reimplemented
 * here; a site configured only for LDAP should pass an explicit JDBC URL instead.
 */
public class DatasourceTableDatasets implements LoadedDatasetSource {

  private static final String DATASOURCE_SQL = "select name from apidb.datasource";

  private final Set<String> _loadedDatasetNames;
  private final String _description;

  /**
   * @param jdbcUrlOverride an explicit JDBC URL, or null to take one from model-config.xml
   */
  DatasourceTableDatasets(String jdbcUrlOverride) {
    File modelConfig = findModelConfig();
    Element appDb = parseAppDbElement(modelConfig);

    String url = jdbcUrlOverride != null ? jdbcUrlOverride : connectionUrlFrom(appDb, modelConfig);
    String login = required(appDb, "login", modelConfig);
    String password = required(appDb, "password", modelConfig);

    _loadedDatasetNames = readDatasetNames(url, login, password);
    _description = "dataset gate on " + url + " as " + login + ": "
        + _loadedDatasetNames.size() + " datasets in apidb.Datasource";
  }

  @Override
  public boolean isLoaded(String nameOrPattern) {
    if (nameOrPattern == null) return false;

    // Fast path: an exact hit is a match under LIKE semantics too, and most presenters
    // carry a plain name. Only fall back to a scan when there is no exact match.
    if (_loadedDatasetNames.contains(nameOrPattern)) return true;

    LikePattern pattern = new LikePattern(nameOrPattern);
    for (String loadedName : _loadedDatasetNames) {
      if (pattern.matches(loadedName)) return true;
    }
    return false;
  }

  @Override
  public String describe() {
    return _description;
  }

  private static Set<String> readDatasetNames(String url, String login, String password) {
    Set<String> names = new HashSet<String>();
    // A gate that was explicitly asked for must not degrade into "inject everything" when
    // the database is unreachable; that would reproduce the failure it exists to prevent.
    try (Connection connection = DriverManager.getConnection(url, login, password);
         Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(DATASOURCE_SQL)) {
      while (rs.next()) {
        names.add(rs.getString(1));
      }
    }
    catch (SQLException e) {
      throw new UserException("Dataset gate is enabled but apidb.Datasource could not be"
          + " read from " + url + " as " + login + ": " + e.getMessage(), e);
    }
    if (names.isEmpty())
      throw new UserException("Dataset gate is enabled but apidb.Datasource at " + url
          + " holds no datasets; refusing to skip every DatasetPresenter");
    return names;
  }

  /**
   * Locate the site's model-config.xml under $GUS_HOME/config.
   *
   * Found by looking for the file rather than by composing a path from the project name,
   * because the environment is not a dependable source here: rebuilder discards everything
   * but a short whitelist, so WDK_MODEL is absent under rebuilder even though it is set for
   * wb. WDK_MODEL and PROJECT are consulted only to disambiguate a gus_home that somehow
   * configures more than one project.
   */
  private static File findModelConfig() {
    String gusHome = System.getenv("GUS_HOME");
    if (gusHome == null)
      throw new UserException("Dataset gate is enabled but GUS_HOME is not set; it supplies"
          + " the location of model-config.xml");

    File configDir = new File(gusHome, "config");
    for (String hint : new String[] { System.getenv("WDK_MODEL"), System.getenv("PROJECT") }) {
      if (hint == null || hint.trim().isEmpty()) continue;
      File hinted = new File(configDir, hint.trim() + "/model-config.xml");
      if (hinted.isFile()) return hinted;
    }

    List<File> found = new ArrayList<File>();
    File[] projectDirs = configDir.listFiles();
    if (projectDirs != null) {
      for (File projectDir : projectDirs) {
        File modelConfig = new File(projectDir, "model-config.xml");
        if (modelConfig.isFile()) found.add(modelConfig);
      }
    }

    if (found.size() == 1) return found.get(0);

    if (found.isEmpty())
      throw new UserException("Dataset gate is enabled but no model-config.xml was found"
          + " under " + configDir + ". It is written by 'conifer configure', which rebuilder"
          + " runs after this build step, so a from-scratch rebuilder cannot use the gate on"
          + " its first pass.");

    throw new UserException("Dataset gate is enabled but " + configDir + " holds "
        + found.size() + " model-config.xml files (" + found + "); set WDK_MODEL to name the"
        + " project, or give the gate an explicit jdbc: URL");
  }

  private static Element parseAppDbElement(File modelConfig) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(modelConfig);
      NodeList appDbNodes = document.getElementsByTagName("appDb");
      if (appDbNodes.getLength() == 0)
        throw new UserException("No <appDb> element in " + modelConfig);
      return (Element) appDbNodes.item(0);
    }
    catch (UserException e) {
      throw e;
    }
    catch (Exception e) {
      throw new UserException("Could not read " + modelConfig + ": " + e.getMessage(), e);
    }
  }

  private static String connectionUrlFrom(Element appDb, File modelConfig) {
    String connectionUrl = attribute(appDb, "connectionUrl");
    if (connectionUrl != null) return connectionUrl;

    String host = attribute(appDb, "dbHost");
    String dbName = attribute(appDb, "dbIdentifier");
    if (host != null && dbName != null) {
      String port = attribute(appDb, "dbPort");
      return "jdbc:postgresql://" + host + (port == null ? "" : ":" + port) + "/" + dbName;
    }

    throw new UserException("The <appDb> in " + modelConfig + " supplies neither a"
        + " connectionUrl nor dbHost + dbIdentifier. It is presumably configured for LDAP"
        + " lookup, which the dataset gate does not perform; enable the gate with an"
        + " explicit JDBC URL instead of 'on'.");
  }

  private static String required(Element appDb, String name, File modelConfig) {
    String value = attribute(appDb, name);
    if (value == null)
      throw new UserException("The <appDb> in " + modelConfig + " has no " + name
          + " attribute, which the dataset gate needs to connect");
    return value;
  }

  private static String attribute(Element element, String name) {
    String value = element.getAttribute(name);
    return value == null || value.trim().isEmpty() ? null : value.trim();
  }
}
