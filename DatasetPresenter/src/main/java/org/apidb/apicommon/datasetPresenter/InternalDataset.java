package org.apidb.apicommon.datasetPresenter;

import java.util.HashSet;
import java.util.Set;

public class InternalDataset {

  private String _name;
  private String _namePattern;
  private Set<String> _namesMatchedInDb = new HashSet<String>();

  public void setName(String name) {
    _name = name;
  }

  public void setDatasetNamePattern(String pattern) {
    _namePattern = pattern;
  }

  String getName() {
    return _name;
  }

  String getDatasetNamePattern() {
    return _namePattern;
  }

  void addNameFromDb(String name) {
    _namesMatchedInDb.add(name);
  }

  boolean containsNameFromDb(String name) {
    return _namesMatchedInDb.contains(name);
  }
}
