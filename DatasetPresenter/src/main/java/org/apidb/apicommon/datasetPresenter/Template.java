package org.apidb.apicommon.datasetPresenter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text template, containing embedded macros, with the ability to substitute
 * values into those macros and inject the resulting text into a stream. The
 * Template has a set of property names which specify the macros allowed in the
 * template. It also has anchorFileName which is the path, relative to
 * $PROJECT_HOME, of the file that contains the anchor for this template. <br><br>
 * The project filtering works like this:
 * <ul>
 * <li>A template's anchorFileName is allowed to have zero or one <code>${projectName}</code>
 * macros.</li>
 * <li>If there is one then we will be using project name as a filter on
 * anchor files</li>
 * <li>The {@link #expandRawAnchorFileName(String)} method converts the
 * ${projectName} macro to a * and does a glob search for anchor files that match. For
 * each file name found it extracts the projectName and makes the association
 * between anchorFile->project. This association is stored
 * in the {@link #anchorFileNameToProject} hash.  If the raw anchor file name had no <code>${projectName}</code>
 * macro then the <code>null</code> project is associated with it (ie, don't use project filtering).</li>
 * <li>{@link #getAnchorFileNames()} returns all the anchor files found for this Template. These are used by
 * the {@link org.apidb.apicommon.datasetPresenter.TemplatesInjector} as a set of
 * all anchor files to process.</li>
 * <li>When the TemplatesInjector processes an anchor file it asks the
 * {@link org.apidb.apicommon.datasetPresenter.TemplatesInstanceSet} to find each
 * instance of the anchor's template. It provides the anchorFileName</li>
 * <li>The TemplateInstanceSet in turn calls
 * {@link #getInstancesAsText(List, String)} to get the Template's instances as
 * text, providing the anchorFileName.</li>
 * <li>The Template uses the {@link #anchorFileNameToProject} hash to find the project
 * associated with the anchor file name. If there is one it filters away
 * TemplateInstances whose projectName property value does not match the project
 * name</li>
 * </ul>
 * 
 * @author steve
 * 
 */
public class Template {

  public static final String TEMPLATE_ANCHOR = "TEMPLATE_ANCHOR";
  public static final String MACRO_START = "\\$\\{";
  public static final String MACRO_END = "\\}";

  static final String nl = System.getProperty("line.separator");

  private Set<String> props = new HashSet<String>();
  private String templateText;

  /**
   * the anchor file name provided by the template file
   */
  private String rawAnchorFileName;
  /**
   * a set of file names. this results if the rawAnchorFileName contains the
   * ${projectName} macro.
   */
  private Map<String, String> anchorFileNameToProject = new HashMap<String, String>();
  private String name;
  private String templateFilePath;

  /**
   * 
   * @param templateFilePath
   *          The full path to the file that this template was parsed out of.
   */
  public Template(String templateFilePath) {
    this.templateFilePath = templateFilePath;
  }

  void setName(String name) {
    this.name = name;
  }

  /**
   * Add a property name. This allows that name to be used as a macro in the
   * template text.
   * 
   * @param prop
   */
  void addProp(String prop) {
    props.add(prop);
  }

  // for testing
  void setProps(Set<String> props) {
    this.props = props;
  }

  /**
   * Set the template text, which is the text that will be injected, after
   * macros are substituted.
   * 
   * @param templateText
   */
  void setTemplateText(String templateText) {
    this.templateText = templateText;
  }

  /**
   * Set the name of the file, relative to $PROJECT_HOME, in which to find the
   * anchor for this template. Ultimately this file will be transformed into
   * $GUS_HOME with all templates instantiated into it.
   * 
   * @param anchorFileName
   */
  void setAnchorFileName(String anchorFileName) {
    this.rawAnchorFileName = anchorFileName;
    String[] splitPath = anchorFileName.split("/lib/");
    String msgPrefix = "In templates file '" + templateFilePath
        + "' template '" + name + "' contains anchorFileName '"
        + rawAnchorFileName + "' ";
    if (splitPath.length != 2)
      throw new UserException(
          msgPrefix
              + "which is not in the form xxxx/lib/yyyy (where xxxx is a path and yyyy is a path)");

    expandRawAnchorFileName(msgPrefix);
  }

  /**
   * If the raw anchor file name includes ${projectName}, expand it into the set
   * of files that match that. save the result in anchorFileNameProject to serve
   * as filters for instances of this template
   * 
   * 
   * @param msgPrefix
   */
  private void expandRawAnchorFileName(String msgPrefix) {

    String project_home = System.getenv("PROJECT_HOME");

    if (!rawAnchorFileName.contains("$")) {
      setAnchorFileNameProject(rawAnchorFileName, null);
      return;
    }

    // replace clumsy project name macro with simple macro, and confirm only one
    // instance of it
    String rawAnchorFileName2 = rawAnchorFileName.replaceFirst(
        "\\$\\{projectName\\}", "PROJECTNAME");
    if (rawAnchorFileName2.contains("$"))
      throw new UserException(
          msgPrefix
              + "which includes an invalid macro.  The only macro allowed is a single occurence of ${projectName})");

    // convert to local filesystem path and split on PROJECTNAME macro
    Path tempPath = FileSystems.getDefault().getPath(project_home,
        rawAnchorFileName2);
    String temp = tempPath.toString();
    String[] splitPath = temp.split("PROJECTNAME");
    String[] splitRaw = rawAnchorFileName2.split("PROJECTNAME");
    Path path = FileSystems.getDefault().getPath(splitPath[0]);
    String glob = "*" + splitPath[1];
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, glob)) {
      for (Path p : ds) {
        String pathName = p.toString().replaceAll("\\\\", "/"); // GO UNIX!
        String pathName2 = pathName.replaceFirst(project_home + "/", "");
        String projectName = pathName2.replaceFirst(splitRaw[0], "").replaceFirst(
            splitRaw[1], "");
        setAnchorFileNameProject(pathName2, projectName);
      }
    } catch (IOException e) {
      throw new UserException(
          msgPrefix
              + "which throws an error when trying to expand ${projectName} into matching files",
          e);
    }
  }

  /*
   * Getters
   */
  String getName() {
    return name;
  }

  Set<String> getProps() {
    return props;
  }

  String getTemplateText() {
    return templateText;
  }

  // used for unit testing
  String getFirstTargetFileName() {
    return AnchorFile.getTargetFileName(rawAnchorFileName);
  }

  // used for unit testing
  String getRawAnchorFileName() {
    return rawAnchorFileName;
  }

  String getAnchorFileProject(String anchorFileName) {
    return anchorFileNameToProject.get(anchorFileName);
  }

  /**
   * provide the list of anchor files that refer to this template
   * 
   * @return
   */
  Collection<String> getAnchorFileNames() {
    return Collections.unmodifiableCollection(anchorFileNameToProject.keySet());
  }

  /**
   * Confirm that the macros in this template's text use only names found in
   * this template's properties list.
   */
  void validateTemplateText() {
    String answer = getTemplateText();
    for (String key : props) {
      answer = answer.replaceAll(MACRO_START + key + MACRO_END, "FOUND");
    }
    Pattern patt = Pattern.compile(MACRO_START + "\\w*" + MACRO_END);
    Matcher m = patt.matcher(answer);
    if (m.find())
      throw new UserException(
          "Template "
              + getName()
              + " in template file "
              + templateFilePath
              + " contains an invalid macro.  The instantiated text is this.  Look for the invalid macro inside it: "
              + answer);

  }

  /**
   * Get a TemplateInstance as a text string. Substitute the TemplateInstance's
   * property values into the template's text.
   * 
   * @param instance
   * @return This template's text with macros substituting using the
   *         TemplateInstance's property values.
   */
  String getInstanceAsText(TemplateInstance instance) {
    String textInstance = getTemplateText();
    for (String key : instance.getPropKeys()) {
      textInstance = textInstance.replaceAll(MACRO_START + key + MACRO_END,
          instance.getPropValue(key));
    }
    return textInstance;
  }

  /**
   * Get a list of TemplateInstances as a concatenation of the text string of
   * each.
   * 
   * @param templateInstances
   * @return A text string that is the concatenation of the text instances of
   *         the provided TemplateInstances
   * @see #getInstanceAsText(TemplateInstance)
   */
  String getInstancesAsText(List<TemplateInstance> templateInstances,
      String anchorFileName) {
    StringBuffer buf = new StringBuffer();

    String projectName = anchorFileNameToProject.get(anchorFileName);
    // getting null pointer when template exists in dst file but not called
    // anywhere
    if (templateInstances != null) {
      for (TemplateInstance instance : templateInstances) {

        // apply projectName filter, if we have one
        if (projectName != null) {
          String instanceProject = instance.getPropValue("projectName");
          if (instanceProject == null)
            throw new UserException(
                getAnchorFileErrMsgPrefix()
                    + "which references ${projectName}, but that property is not supplied in this instance: "
                    + nl + getInstanceAsText(instance));
          if (!instanceProject.equals(projectName))
            continue;
        }

        buf.append(getInstanceAsText(instance) + nl);
      }
    }
    return buf.toString();
  }

  private String getAnchorFileErrMsgPrefix() {
    return "In templates file '" + templateFilePath + "' template '" + name
        + "' contains anchorFileName '" + rawAnchorFileName + "' ";
  }
  
   void setAnchorFileNameProject(String anchorFileName, String project) {
    anchorFileNameToProject.put(anchorFileName, project);
  }

  /**
   * Inject the text produced by {@link #getInstancesAsText(List)} into a text
   * stream.
   * 
   * @param instances
   * @param targetTextAsStream
   * @return the target text from the stream as a String with macros substituted
   *         in.
   * @see #injectTextIntoStream(String, InputStream)
   */
  String injectInstancesIntoStream(List<TemplateInstance> instances,
      InputStream targetTextAsStream, String anchorFileName) {
    return injectTextIntoStream(getInstancesAsText(instances, anchorFileName),
        targetTextAsStream);
  }

  /**
   * Scan through the provided target text and place the provided injectable
   * text into the stream on the line after an anchor that references this
   * template's name
   * 
   * @param textToInject
   * @param targetTextAsStream
   * @return the target text from the stream as a String with macros substituted
   *         in.
   */
  String injectTextIntoStream(String textToInject,
      InputStream targetTextAsStream) {
    Scanner scanner = new Scanner(targetTextAsStream);
    StringBuffer buf = new StringBuffer();
    Pattern patt = Pattern.compile(TEMPLATE_ANCHOR + "\\s+" + name);
    try {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        buf.append(line + nl);
        Matcher m = patt.matcher(line);
        if (m.find())
          buf.append(nl + textToInject + nl);
      }
    } finally {
      scanner.close();
    }
    return buf.toString();
  }

  /**
   * Validate that the provided set of property values includes all properties
   * required by this template.
   * 
   * @param propValues
   * @return true if valid
   */
  boolean validatePropertiesInstance(Map<String, String> propValues) {
    return propValues.keySet().containsAll(getProps());
  }

}
