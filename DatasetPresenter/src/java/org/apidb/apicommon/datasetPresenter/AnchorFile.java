package org.apidb.apicommon.datasetPresenter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnchorFile {
  String name;
  Set<Template> pointingTemplates = new HashSet<Template>();
  Set<String> pointingTemplateNames = new HashSet<String>();

   AnchorFile(String name) {
    this.name = name;
  }
  
  String getName() { return name; }

  void addPointingTemplate(Template template) {
    pointingTemplates.add(template);
    pointingTemplateNames.add(template.getName());
  }
  
  /**
   * Get templates that refer to this anchor file.
   * @return
   */
  Set<Template> getPointingTemplates() {
    return Collections.unmodifiableSet(pointingTemplates);
  }
  
  /**
   * Get the names of templates that refer to this anchor file.
   * @return
   */
  Set<String> getPointingTemplateNames() {
    return Collections.unmodifiableSet(pointingTemplateNames);
  }

  /**
   * Convert an anchor file name (project_home) to the parallel target file name (gus_home)
   * @param anchorFileName
   * @return
   */
  String getTargetFileName() {
    return getTargetFileName(name);
  }
  
  static String getTargetFileName(String anchorFileName) {
    String[] splitPath = anchorFileName.split("/");
    String preLib = "";
    String postLib = "";
    boolean seenLib = false;
    boolean hasPerl = false;

    for(int i = 0; i < splitPath.length; i++) {
        if(splitPath[i].equals("lib")) {
            seenLib = true;
            continue;
        }

        if(splitPath[i].equals("perl")) {
            hasPerl = true;
            continue;
        }
        if(seenLib) {
            postLib = postLib + "/" + splitPath[i];
        } else {
            preLib = preLib + "/" + splitPath[i];
        }
    }

    String prefix = "lib";
    if(hasPerl) {
        prefix = prefix + "/perl";
        postLib = preLib + postLib;
    }
    
    return prefix + postLib;
  }

}
