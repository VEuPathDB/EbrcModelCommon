package org.apidb.apicommon.datasetPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of DatasetInjector subclasses. This set has the information needed to
 * construct a parallel TemplateInstanceSet. At processing time it iterates
 * through its members asking them to construct the template instances they need
 * to inject.
 * 
 * @author steve
 * 
 */
public class DatasetInjectorSet {

  private List<DatasetInjector> datasetInjectors = new ArrayList<DatasetInjector>();
  private TemplateInstanceSet templateInstanceSet;

  /**
   * Add a member to this set.
   * 
   * Called at processing time.
   */
  void addDatasetInjector(DatasetInjector datasetInjector) {
    datasetInjectors.add(datasetInjector);
    datasetInjector.setDatasetInjectorSet(this);
  }

  /**
   * Transform this DatasetInjectorSet into a TemplateInstanceSet. Calls each
   * injector and asks it construct TemplateInstances and add them to the
   * TemplateInstanceSet
   * 
   * Called at processing time.
   * 
   */
  TemplateInstanceSet getTemplateInstanceSet() {
    if (templateInstanceSet == null) {
      templateInstanceSet = new TemplateInstanceSet();
      for (DatasetInjector datasetInjector : datasetInjectors) {
        datasetInjector.injectTemplates();
      }
    }
    return templateInstanceSet;
  }

  /**
   * Inject a template instance into the TemplateInstanceSet this
   * DatasetInjectorSet is constructing.
   * 
   * Called at processing time.
   */
  void injectTemplateInstance(TemplateInstance templateInstance) {
    templateInstanceSet.addTemplateInstance(templateInstance);
  }
  
}
