package org.apidb.apicommon.datasetPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A set of TemplateInstances that can transform themselves into a text version
 * (with macros substituted). This set is constructed from information found in
 * one or more DatasetPresenterSets, which is independent of the creation of the
 * Templates themselves. (That is why TemplateInstances refer only to Template
 * names rather than Templates.)
 * 
 * @author steve
 * 
 */
public class TemplateInstanceSet {

  private Map<String, List<TemplateInstance>> templateInstancesByTemplateName = new HashMap<String, List<TemplateInstance>>();
  private List<TemplateInstance> templateInstances = new ArrayList<TemplateInstance>();

  /**
   * Add a TemplateInstance to the set.
   * 
   * @param templateInstance
   */
  void addTemplateInstance(TemplateInstance templateInstance) {
    templateInstances.add(templateInstance);
    String templateName = templateInstance.getTemplateName();
    if (!templateInstancesByTemplateName.containsKey(templateName)) {
      templateInstancesByTemplateName.put(templateName,
          new ArrayList<TemplateInstance>());
    }
    templateInstancesByTemplateName.get(templateName).add(templateInstance);
  }

  /**
   * Get this set of TemplateInstances as text (the concatenation of each).
   */
  String getTemplateInstancesAsText(Template template, String anchorFileName) {
    return template.getInstancesAsText(templateInstancesByTemplateName.get(template.getName()), anchorFileName);
  }
  
  /**
   * Get a list of TemplateInstances in this set that are instances of the provided Template.
   * @param templateName
   */
  List<TemplateInstance> getTemplateInstances(String templateName) {
    return templateInstancesByTemplateName.get(templateName);
  }

  /**
   * Get the set of template names found in the instances of this set.
   */
  Set<String> getTemplateNamesUsed() {
    return templateInstancesByTemplateName.keySet();
  }

}
