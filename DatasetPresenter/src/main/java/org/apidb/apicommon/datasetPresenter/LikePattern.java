package org.apidb.apicommon.datasetPresenter;

import java.util.regex.Pattern;

/**
 * A SQL LIKE pattern, evaluated in memory.
 *
 * Deliberately mirrors the semantics of the "ds.NAME like ?" query in
 * {@link DatasetPresenterSetLoader#getDatasourceTableStmt}, so that a dataset gate
 * applied at build time can never disagree with the database about whether a
 * DatasetPresenter matches a dataset: '%' spans any run of characters, '_' matches
 * exactly one, and every other character is literal.
 *
 * Note that '_' remains a single-character wildcard even though dataset names contain
 * literal underscores throughout. That is looser than most authors intend, but it is
 * what the database does; tightening it here in isolation would let injection skip a
 * presenter the loader matched.
 */
public class LikePattern {

  private final String _like;
  private final Pattern _regex;

  LikePattern(String likePattern) {
    _like = likePattern;
    _regex = Pattern.compile(toRegex(likePattern));
  }

  /**
   * Translate a SQL LIKE pattern into an equivalent regular expression.
   */
  static String toRegex(String likePattern) {
    StringBuilder regex = new StringBuilder();
    for (char c : likePattern.toCharArray()) {
      switch (c) {
        case '%':
          regex.append(".*");
          break;
        case '_':
          regex.append('.');
          break;
        default:
          if ("\\.[]{}()*+-?^$|".indexOf(c) >= 0) regex.append('\\');
          regex.append(c);
      }
    }
    return regex.toString();
  }

  boolean matches(String datasetName) {
    return datasetName != null && _regex.matcher(datasetName).matches();
  }

  @Override
  public String toString() {
    return _like;
  }
}
