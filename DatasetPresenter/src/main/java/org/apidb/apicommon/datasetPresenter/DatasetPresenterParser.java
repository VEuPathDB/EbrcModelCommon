package org.apidb.apicommon.datasetPresenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.xml.NamedValue;
import org.gusdb.fgputil.xml.Text;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.fgputil.xml.XmlValidator;
import org.xml.sax.SAXException;

/**
 * Parse an XML representation of a DatasetPresenterSet into java objects. The
 * XML schema is described in lib/rng/datasetPresenter.rng.
 */
public class DatasetPresenterParser extends XmlParser {

  private final Digester _digester;
  
  public DatasetPresenterParser() {
    _digester = configureDigester();
  }

  private static Digester configureDigester() {
    Digester digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("datasetPresenters", DatasetPresenterSet.class);

    configureNode(digester, "datasetPresenters/datasetPresenter",
        DatasetPresenter.class, "addDatasetPresenter");

    configureNode(digester, "datasetPresenters/datasetPresenter/displayName",
        Text.class, "setDatasetDisplayName");
    digester.addCallMethod("datasetPresenters/datasetPresenter/displayName",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/shortAttribution",
        Text.class, "setShortAttribution");
    digester.addCallMethod("datasetPresenters/datasetPresenter/shortAttribution",
        "setText", 0);

    configureNode(digester,
        "datasetPresenters/datasetPresenter/shortDisplayName", Text.class,
        "setDatasetShortDisplayName");
    digester.addCallMethod(
        "datasetPresenters/datasetPresenter/shortDisplayName", "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/description",
        Text.class, "setDatasetDescrip");
    digester.addCallMethod("datasetPresenters/datasetPresenter/description",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/caveat",
        Text.class, "setCaveat");
    digester.addCallMethod("datasetPresenters/datasetPresenter/caveat",
        "setText", 0);

    configureNode(digester,
        "datasetPresenters/datasetPresenter/displayCategory", Text.class,
        "setDisplayCategory");
    digester.addCallMethod(
        "datasetPresenters/datasetPresenter/displayCategory", "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/protocol",
        Text.class, "setProtocol");
    digester.addCallMethod("datasetPresenters/datasetPresenter/protocol",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/usage",
        Text.class, "setUsage");
    digester.addCallMethod("datasetPresenters/datasetPresenter/usage",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/releasePolicy",
        Text.class, "setReleasePolicy");
    digester.addCallMethod("datasetPresenters/datasetPresenter/releasePolicy",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/summary",
        Text.class, "setSummary");
    digester.addCallMethod("datasetPresenters/datasetPresenter/summary",
        "setText", 0);

    configureNode(digester,
        "datasetPresenters/datasetPresenter/acknowledgement", Text.class,
        "setAcknowledgement");
    digester.addCallMethod(
        "datasetPresenters/datasetPresenter/acknowledgement", "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/primaryContactId",
        Text.class, "setPrimaryContactId");
    digester.addCallMethod("datasetPresenters/datasetPresenter/primaryContactId",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/contactId",
        Text.class, "addContactId");
    digester.addCallMethod("datasetPresenters/datasetPresenter/contactId",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/pubmedId",
        Publication.class, "addPublication");
    digester.addCallMethod("datasetPresenters/datasetPresenter/pubmedId",
        "setPubmedId", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/history",
        History.class, "addHistory");
    digester.addCallMethod("datasetPresenters/datasetPresenter/history",
        "setComment", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/link",
        HyperLink.class, "addLink");

    configureNode(digester, "datasetPresenters/datasetPresenter/link/url",
        Text.class, "setUrl");
    digester.addCallMethod("datasetPresenters/datasetPresenter/link/url",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/link/text",
        Text.class, "setText");
    digester.addCallMethod("datasetPresenters/datasetPresenter/link/text",
        "setText", 0);

    configureNode(digester, "datasetPresenters/datasetPresenter/link/description",
        Text.class, "setDescription");
    digester.addCallMethod("datasetPresenters/datasetPresenter/link/description",
        "setText", 0);

    configureNode(digester,
        "datasetPresenters/datasetPresenter/templateInjector",
        DatasetInjectorConstructor.class, "setDatasetInjector");

    configureNode(digester,
        "datasetPresenters/datasetPresenter/templateInjector/prop",
        NamedValue.class, "addProp");
    digester.addCallMethod(
        "datasetPresenters/datasetPresenter/templateInjector/prop", "setValue",
        0);
    
    configureNode(digester, "datasetPresenters/internalDataset",
        InternalDataset.class, "addInternalDataset");

    return digester;
  }

  void validateXmlFile(String xmlFileName) {
    try {
      XmlValidator validator = new XmlValidator(GusHome.getGusHome() + "/lib/rng/datasetPresenter.rng");
      File xmlFile = new File(xmlFileName);
      URL url = xmlFile.toURI().toURL();
      if (!validator.validate(url)) {
        throw new UserException("Invalid XML file " + xmlFileName);
      }
    }
    catch (IOException | SAXException ex) {
      throw new UnexpectedException(ex);
    }
  }

  DatasetPresenterSet parseFile(String xmlFileName) {

    DatasetPresenterSet datasetPresenterSet = null;
    try {
      validateXmlFile(xmlFileName);
      File xmlFile = new File(xmlFileName);
      datasetPresenterSet = (DatasetPresenterSet) _digester.parse(xmlFile);
    }
    catch (IOException | SAXException ex) {
      throw new UnexpectedException(ex);
    }
    return datasetPresenterSet;
  }

  DatasetPresenterSet parseDir(String presenterXmlDirPath, String globalXmlFile) {
    DatasetPresenterSet bigDatasetPresenterSet = new DatasetPresenterSet();
    List<File> presenterFiles = getPresenterXmlFilesInDir(presenterXmlDirPath);
    for (File presenterFile : presenterFiles) {
      bigDatasetPresenterSet.addDatasetPresenterSet(parseFile(presenterXmlDirPath
          + "/" + presenterFile.getName()));
    }
    if (globalXmlFile != null) bigDatasetPresenterSet.addDatasetPresenterSet(parseFile(globalXmlFile));

    return bigDatasetPresenterSet;
  }

  static List<File> getPresenterXmlFilesInDir(String presentersDirPath) {
    File dir = new File(presentersDirPath);
    List<File> presenterFiles = new ArrayList<File>();

    for (File file : dir.listFiles()) {
      if (file.isFile() && file.getName().endsWith(".xml"))
        presenterFiles.add(file);
    }
    return presenterFiles;
  }
  
  static Map<String, Map<String, String>> parseDefaultInjectorsFile(String fileName) {
    
    if (fileName == null) return null;
    
    BufferedReader in = null;

    Map<String, Map<String, String>> index = new HashMap<String, Map<String, String>>();
    try {
      try {
        in = new BufferedReader(new FileReader(fileName));
        while (in.ready()) {
          String line = in.readLine();
          if (line == null)
            break; // to dodge findbugs error
          line = line.trim();
          String[] columns = line.split("\t");
          if (columns.length != 3)
            throw new UserException("Default injectors file " + fileName + " does not have three columns in this row: " + System.getProperty("line.separator") + line + System.getProperty("line.separator"));

          if(columns[1].equals(""))
              columns[1] = null;

          // This should not happen ... but just in case
          if(columns[0].equals(""))
              columns[0] = null;

          if (index.containsKey(columns[0])) {
            if (index.get(columns[0]).containsKey(columns[1]))
              throw new UserException("Default Injectors file " + fileName
                  + " contains duplicate type and subtype: " + columns[0]
                  + ", " + columns[1]);
            index.get(columns[0]).put(columns[1], columns[2]);
          } else {
            Map<String, String> m = new HashMap<String, String>();
            m.put(columns[1], columns[2]);
            index.put(columns[0], m);
          }
        }

      } catch (FileNotFoundException ex) {
        throw new UserException("Default Injectors file " + fileName
            + " not found");
      } finally {
        if (in != null)
          in.close();
      }
    } catch (IOException ex) {
      throw new UnexpectedException(ex);
    }
    return index;
  }

}
