package org.apidb.apicommon.datasetPresenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parse a templates file, construct the set of Templates and add them to a
 * target TemplateSet. A templates file contains one or more template in this
 * format:
 * 
 * <pre>
 * [templateStart]
 * name=
 * anchorFile=
 * # a comment
 * prop=
 * prop=
 * >templateTextStart< 
 * (template text here, including macros)
 * >templateTextEnd<
 * </pre>
 * 
 * The part between <code>[templateStart]</code> and
 * <code>>templateTextStart<</code> is the "prelude." The part between the
 * <code>>templateTextStart<</code> and the <code>>templateTextStart<</code> is
 * the "template text." The template text is the text we will inject, including
 * macros that need to be substituted.
 * 
 * @author steve
 * 
 */
public class TemplatesParser {

  public static final String TEMPLATE_START = "[templateStart]";
  public static final String TEMPLATE_TEXT_START = ">templateTextStart<";
  public static final String TEMPLATE_TEXT_END = ">templateTextEnd<";
  static final String nl = System.getProperty("line.separator");

  /**
   * Parse all .dst files in the provided directory and add the constructed
   * Templates to the target TemplatesSet.
   * 
   * @see #parseTemplatesFile(TemplateSet, String)
   * @param targetTemplateSet
   *          A TemplateSet to add the constructed Templates to.
   * @param templatesDirPath
   *          Full path to a directory containing one or more .dst files
   */
  static void parseTemplatesDir(TemplateSet targetTemplateSet,
      String templatesDirPath) {
    List<File> templateFiles = getTemplateFilesInDir(templatesDirPath);
    for (File templateFile : templateFiles) {
      parseTemplatesFile(targetTemplateSet, templatesDirPath + "/" + templateFile.getName());
    }
  }

  static List<File> getTemplateFilesInDir(String templatesDirPath) {
    File dir = new File(templatesDirPath);
    List<File> templateFiles = new ArrayList<File>();

    for (File file : dir.listFiles()) {
      if (file.isFile() && file.getName().endsWith(".dst"))
        templateFiles.add(file);
    }
    return templateFiles;
  }

  /**
   * Open the provided templates file, parse it, and add the Templates to the
   * provided set. Scan the file passing each individual template section to
   * {@link #processTemplate(TemplateSet, String, String)}. Ignore comment lines
   * and blank lines (except never ignore lines inside the template text).
   * 
   * @param targetTemplateSet
   *          A TemplateSet to add the constructed Templates to.
   * @param templatesFilePath
   *          Full path to a templates file to parse.
   */
  static void parseTemplatesFile(TemplateSet targetTemplateSet,
      String templatesFilePath) {
    BufferedReader in = null;

    try {
      try {
        in = new BufferedReader(new FileReader(templatesFilePath));

        StringBuffer templateStrBuf = new StringBuffer();
        boolean foundFirst = false;
        while (in.ready()) {
          String line = in.readLine();
          if (line == null)
            break; // to dodge findbugs error
          if (!foundFirst) {
            line = line.trim();
            if (line.startsWith("#"))
              continue;
            if (line.length() == 0)
              continue;
            if (line.equals(TEMPLATE_START)) {
              foundFirst = true;
              templateStrBuf = new StringBuffer();
            } else {
              throw new UserException("Templates file " + templatesFilePath
                  + " must start with " + TEMPLATE_START);
            }
          } else {
            if (line.trim().equals(TEMPLATE_START)) {
              processTemplate(targetTemplateSet, templateStrBuf.toString(),
                  templatesFilePath);
              templateStrBuf = new StringBuffer();
            } else {
              templateStrBuf.append(line + Template.nl);
            }
          }
        }
        processTemplate(targetTemplateSet, templateStrBuf.toString(),
            templatesFilePath);

      } catch (FileNotFoundException ex) {
        throw new UserException("Templates file " + templatesFilePath
            + " not found");
      } finally {
        if (in != null)
          in.close();
      }
    } catch (IOException ex) {
      throw new UnexpectedException(ex);
    }
  }

  /**
   * Parse an individual template string, check that its template text includes
   * only valid macros, construct a Template, and add it to the provided
   * TemplateSet.
   * 
   * @see #parseSingleTemplateString(String, String)
   */
  static void processTemplate(TemplateSet targetTemplateSet,
      String templateStr, String templatesFilePath) {
    Template template = parseSingleTemplateString(templateStr,
        templatesFilePath);
    template.validateTemplateText();
    targetTemplateSet.addTemplate(template, templatesFilePath);
  }

  /**
   * Parse a single template string, and construct a Template from it.
   * 
   * @param templateInputString
   *          The template string extracted from the templates file.
   * @param templateFilePath
   *          The path to the templates file.
   */
  static Template parseSingleTemplateString(String templateInputString,
      String templateFilePath) {
    String[] parts = splitTemplateString(templateInputString, templateFilePath);
    Template template = new Template(templateFilePath);
    parsePrelude(parts[0], template, templateFilePath);
    template.setTemplateText(parts[1]);
    template.validateTemplateText();
    return template;
  }

  /**
   * Split a template string into two parts, the prelude and the template text.
   * 
   * @param templateInputString
   *          The template string. Must end in the line
   *          {@link #TEMPLATE_TEXT_END}.
   * @param templateFilePath
   *          The file path of the file which this template string came from.
   * @return A String array where the first element is the prelude and the
   *         second element is the template text.
   */
  static String[] splitTemplateString(String templateInputString,
      String templateFilePath) {
    Scanner scanner = new Scanner(templateInputString);
    StringBuffer prelude = new StringBuffer();
    StringBuffer templateText = null;
    try {

      // prelude and template text
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (templateText == null) {
          line = line.trim();
          if (line.startsWith("#"))
            continue;
          if (line.length() == 0)
            continue;
          if (line.equals(TEMPLATE_TEXT_START)) {
            templateText = new StringBuffer();
          } else {
            prelude.append(line + nl);
          }
        } else {
          if (line.equals(TEMPLATE_TEXT_END))
            break;
          templateText.append(line + nl);
        }
      }

      // past end of template text
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.startsWith("#"))
          continue;
        if (line.length() == 0)
          continue;
        throw new UserException("Template file '" + templateFilePath
            + "' has an invalid template.  The line '" + line
            + "' is outside a " + TEMPLATE_TEXT_END);
      }

      String[] answer = { prelude.toString(), templateText.toString() };
      return answer;
    } finally {
      scanner.close();
    }
  }

  /**
   * Parse the prelude portion of a template string.
   * 
   * @param prelude
   *          The prelude as a string
   * @param template
   *          The template to add the prelude fields to.
   * @param templateFilePath
   *          The path of the file the template was found in.
   */
  static void parsePrelude(String prelude, Template template,
      String templateFilePath) {
    Scanner scanner = new Scanner(prelude);

    try {
      // first line should be name=
      String line = scanner.nextLine();
      String[] a = line.split("=");
      if (!a[0].equals("name") || a.length != 2)
        throw new UserException("In file '" + templateFilePath + "' the line '"
            + line + "' should be a name=");
      template.setName(a[1]);

      // second line should be anchorFile=
      line = scanner.nextLine();
      a = line.split("=");
      if (!a[0].equals("anchorFile") || a.length != 2)
        throw new UserException("In file '" + templateFilePath + "' the line '"
            + line + "' should be a anchorFile=");
      template.setAnchorFileName(a[1]);

      // the rest should be prop=
      while (scanner.hasNextLine()) {
        line = scanner.nextLine();
        a = line.split("=");
        if (!a[0].equals("prop") || a.length != 2)
          throw new UserException("In file '" + templateFilePath
              + "' the line '" + line + "' should be a prop=");
        template.addProp(a[1]);
      }
    } finally {
      scanner.close();
    }
  }

}
