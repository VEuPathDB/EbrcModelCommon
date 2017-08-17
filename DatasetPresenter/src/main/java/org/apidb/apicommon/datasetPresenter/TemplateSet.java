package org.apidb.apicommon.datasetPresenter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A set of Templates, typically those parsed from a templates file.
 * 
 * @author steve
 * 
 */
public class TemplateSet {

  Map<String, Template> nameToTemplate = new HashMap<String, Template>();
  Map<String, AnchorFile> anchorFiles = new HashMap<String, AnchorFile>();

  /**
   * Add a Template to this set. Must have a unique template name.
   * 
   * @param template
   */
  void addTemplate(Template template, String templatesFilePath) {
    if (nameToTemplate.containsKey(template.getName()))
      throw new UserException("Duplicate template found: " + template.getName()
          + Template.nl + "Templates file: " + templatesFilePath);
    nameToTemplate.put(template.getName(), template);

    for (String anchorFileName : template.getAnchorFileNames()) {
      if (!anchorFiles.containsKey(anchorFileName)) {
        AnchorFile anchorFile = new AnchorFile(anchorFileName);
        anchorFiles.put(anchorFileName, anchorFile);
      }
      AnchorFile anchorFile = anchorFiles.get(anchorFileName);
      anchorFile.addPointingTemplate(template);
    }
  }

  Template getTemplateByName(String name) {
    return nameToTemplate.get(name);
  }

  /**
   * Get the set of anchor file name mentioned by all the Templates in this set.
   * (An anchor file may contain anchors for more than one Template.)
   * 
   */
  Collection<AnchorFile> getAnchorFiles() {
    return anchorFiles.values();
  }

  /**
   * Get the names of the Templates in this set that belong in the provided
   * anchorFileName.
   * 
   * @param anchorFileName
   */
  Set<String> getTemplateNamesByAnchorFileName(String anchorFileName) {
    return anchorFiles.get(anchorFileName).getPointingTemplateNames();
  }

  int getSize() {
    return nameToTemplate.size();
  }
}
