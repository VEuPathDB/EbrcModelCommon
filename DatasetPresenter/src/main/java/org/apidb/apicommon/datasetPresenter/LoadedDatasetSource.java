package org.apidb.apicommon.datasetPresenter;

/**
 * Answers "does this instance have the dataset a DatasetPresenter presents?".
 *
 * The presenters XML directory is deliberately a superset of what any one instance has
 * loaded, so presenters for absent datasets must be skipped rather than injected. What
 * counts as loaded is not knowable from the presenters or the dataset XML alone — the
 * workflow's root graph decides, and apidb.Datasource is its output — so the answer is
 * supplied by an implementation of this interface rather than computed inline.
 *
 * The default implementation, {@link AllDatasetsLoaded}, answers yes to everything and
 * touches no database, which is required: the website build cannot make database calls in
 * normal operation. A developer building against a partially loaded database opts in to
 * {@link DatasourceTableDatasets} instead; see {@link LoadedDatasetSources}.
 */
public interface LoadedDatasetSource {

  /**
   * @param nameOrPattern a DatasetPresenter's datasetNamePattern if it has one, else its
   *          dataset name. Interpreted with SQL LIKE semantics either way, matching how
   *          {@link DatasetPresenterSetLoader} resolves the same choice.
   * @return whether at least one loaded dataset matches
   */
  boolean isLoaded(String nameOrPattern);

  /**
   * @return a short description of this source, for the build log
   */
  String describe();
}
