package org.apidb.apicommon.datasetPresenter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link LoadedDatasetSource} that treats a named set of datasets as loaded regardless of
 * what the delegate says, and otherwise defers to it.
 *
 * Needed because some DatasetPresenters deliberately contribute only attributes, leaving
 * their questions hardcoded in the anchor file — the custom expression presenters do exactly
 * this, with a "setting these to false so we don't inject questions" comment. Skipping such a
 * presenter leaves the hardcoded question referencing an attribute nobody generated, which
 * fails model load. Excepting the dataset runs its injectors and supplies the attribute.
 *
 * Use sparingly: an exception asserts "inject this presenter even though the data is absent",
 * so any template it injects that queries missing tables will fail later, in a less obvious
 * place. It is the right tool only when the presenter's output is metadata rather than data.
 */
public class AlwaysLoadedDatasets implements LoadedDatasetSource {

  private final LoadedDatasetSource _delegate;
  private final Set<String> _alwaysLoaded;

  AlwaysLoadedDatasets(LoadedDatasetSource delegate, Set<String> alwaysLoaded) {
    _delegate = delegate;
    _alwaysLoaded = Collections.unmodifiableSet(new LinkedHashSet<String>(alwaysLoaded));
  }

  @Override
  public boolean isLoaded(String nameOrPattern) {
    if (nameOrPattern == null) return false;

    if (_alwaysLoaded.contains(nameOrPattern)) return true;

    // a pattern presenter counts as excepted when the pattern covers an excepted dataset
    if (nameOrPattern.indexOf('%') >= 0) {
      LikePattern pattern = new LikePattern(nameOrPattern);
      for (String excepted : _alwaysLoaded) {
        if (pattern.matches(excepted)) return true;
      }
    }

    return _delegate.isLoaded(nameOrPattern);
  }

  @Override
  public String describe() {
    return _delegate.describe() + "; " + _alwaysLoaded.size()
        + " dataset(s) always injected: " + String.join(", ", _alwaysLoaded);
  }
}
