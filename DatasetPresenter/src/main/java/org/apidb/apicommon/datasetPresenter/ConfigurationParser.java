package org.apidb.apicommon.datasetPresenter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.gusdb.fgputil.xml.Text;
import org.gusdb.fgputil.xml.XmlParser;
import org.xml.sax.SAXException;

/**
 * Parses tuning manager XML properties file.  Note no XML validation (e.g. via
 * RNG schema) is performed on the input file.
 */
public class ConfigurationParser extends XmlParser {

  private final Digester _digester;

  public ConfigurationParser() {
    _digester = configureDigester();
  }

  private static Digester configureDigester() {
    Digester digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("tuningProps", Configuration.class);

    configureNode(digester, "tuningProps/password",
        Text.class, "setPassword");
    digester.addCallMethod("tuningProps/password",
        "setText", 0);
    
    configureNode(digester, "tuningProps/schema",
        Text.class, "setUsername");
    digester.addCallMethod("tuningProps/schema",
        "setText", 0);

    return digester;
  }

  Configuration parseFile(String xmlFileName) {
    try {
      Configuration config = (Configuration) _digester.parse(new File(xmlFileName));
      if (config.getPassword() == null) throw new UserException("Could not parse password out of tuning manager prop XML file " + xmlFileName);
      if (config.getUsername() == null) throw new UserException("Could not parse schema out of tuning manager prop XML file " + xmlFileName);
      return config;
    }
    catch (IOException | SAXException ex) {
      throw new UnexpectedException(ex);
    }
  }

}
