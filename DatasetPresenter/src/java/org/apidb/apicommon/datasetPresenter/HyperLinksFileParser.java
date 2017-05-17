package org.apidb.apicommon.datasetPresenter;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.digester.Digester;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.xml.Text;
import org.gusdb.fgputil.xml.XmlParser;
import org.gusdb.fgputil.xml.XmlValidator;
import org.xml.sax.SAXException;

/**
 * Parse an XML representation of a DatasetPresenterSet into java objects. The
 * XML schema is described in lib/rng/datasetPresenter.rng.
 */
public class HyperLinksFileParser extends XmlParser {

  private final Digester _digester;

  public HyperLinksFileParser() {
    _digester = configureDigester();
  }

  private static Digester configureDigester() {
    Digester digester = new Digester();
    digester.setValidating(false);

    digester.addObjectCreate("links", HyperLinks.class);

    configureNode(digester, "links/link", HyperLink.class, "addHyperLink");

    configureNode(digester, "links/link/url", Text.class, "setUrl");
    digester.addCallMethod("links/link/url", "setText", 0);

    configureNode(digester, "links/link/text", Text.class, "setText");
    digester.addCallMethod("links/link/text", "setText", 0);

    configureNode(digester, "links/link/description", Text.class, "setDescription");
    digester.addCallMethod("links/link/description", "setText", 0);

    return digester;
  }

  HyperLinks parseFile(String xmlFileName) {

    HyperLinks links = null;
    try {
      validateXmlFile(xmlFileName);
      links = (HyperLinks) _digester.parse(new File(xmlFileName));

      links.setXmlFileName(xmlFileName);
    } catch (IOException | SAXException ex) {
      throw new UnexpectedException(ex);
    }
    return links;
  }
  
  void validateXmlFile(String xmlFileName) {
    try {
      XmlValidator validator = new XmlValidator(GusHome.getGusHome() + "/lib/rng/links.rng");
      File xmlFile = new File(xmlFileName);
      URL url = xmlFile.toURI().toURL();
      if (!validator.validate(url)) {
        throw new UserException("Invalid XML file " + xmlFileName);
      }
    } catch (IOException | SAXException ex) {
      throw new UnexpectedException(ex);
    }
  }


}
