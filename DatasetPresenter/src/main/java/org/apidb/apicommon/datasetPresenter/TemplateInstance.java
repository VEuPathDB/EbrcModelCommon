package org.apidb.apicommon.datasetPresenter;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * An instance of a template: a template name and a set of property values to substitute into the template.
 * @author steve
 *
 */
public class TemplateInstance {
  private String templateName;
  private Map<String, String> propValues;
  
  /**
   * Create a TemplateInstance.
   * @param templateName The name of the template to instantiate.
   * @param propValues The set of property values to substitute into that template's macros.
   */
  public TemplateInstance(String templateName, Map<String, String> propValues) {
    this.templateName = templateName;
    this.propValues = new HashMap<String, String>(propValues);
  }
  
  String getTemplateName() {
    return templateName;
  }
  
  Set<String> getPropKeys() {
    return propValues.keySet();
  }
  
  String getPropValue(String key) {
    return propValues.get(key);
  }
}
