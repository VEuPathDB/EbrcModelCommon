package org.apidb.apicommon.datasetPresenter;

/**
 * The default {@link LoadedDatasetSource}: every presenter is treated as loaded and no
 * database is contacted. This is the behavior the build has always had, and it is what
 * runs unless a developer explicitly opts in to a dataset gate.
 */
public class AllDatasetsLoaded implements LoadedDatasetSource {

  @Override
  public boolean isLoaded(String nameOrPattern) {
    return true;
  }

  @Override
  public String describe() {
    return "no dataset gate: every DatasetPresenter will be injected";
  }
}
