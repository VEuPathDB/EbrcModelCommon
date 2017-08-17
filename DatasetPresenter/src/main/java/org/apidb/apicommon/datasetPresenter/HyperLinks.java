package org.apidb.apicommon.datasetPresenter;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class HyperLinks {
  
  Map<String, List<HyperLink>> hyperlinks = new HashMap<String, List<HyperLink>>();
  String xmlFileName;
  
    public void addHyperLink(HyperLink link) {
        String type = link.getType();
        String subType = link.getSubtype();

        String key = type + "." + subType;


        if(type == null && subType == null) {
            throw new UserException("Invalid XML file " + xmlFileName);
        }

        if(hyperlinks.containsKey(key)) {
            hyperlinks.get(key).add(link);
        }
        else {
            List<HyperLink> links = new ArrayList<HyperLink>();
            links.add(link);
            hyperlinks.put(key, links);
        }
    }
  
  List<HyperLink> getHyperLinksFromTypeSubtype(String key) {
      if(hyperlinks.containsKey(key)) {
          return hyperlinks.get(key);
      }
      return new ArrayList<HyperLink>();
  }
  
  void setXmlFileName(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  }
  
  String getXmlFileName() {
    return this.xmlFileName;
  }

}
