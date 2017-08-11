package org.apidb.apicommon.datasetPresenter;

import java.util.HashSet;
import java.util.Set;

public class InternalDataset {
  private String name;
  private String namePattern;
  private Set<String> namesMatchedInDb = new HashSet<String>();
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void setDatasetNamePattern(String pattern) {
    this.namePattern = pattern;
  }
  
  String getName() {
    return name;
  }
  
  String getDatasetNamePattern() {
    return namePattern;
  }
  
  void addNameFromDb(String name) {
    namesMatchedInDb.add(name);
  }
  
  boolean containsNameFromDb(String name) {
    return namesMatchedInDb.contains(name);
  }
}
