package org.apidb.apicommon.datasetPresenter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Chooses the {@link LoadedDatasetSource} for this build.
 *
 * The gate is off unless a developer turns it on, so a normal build makes no database calls
 * and injects exactly the presenters it always did. It is turned on with a JVM system
 * property:
 *
 * <pre>
 *   -Dpresenter.dataset.gate=on                                # appDb from model-config.xml
 *   -Dpresenter.dataset.gate=jdbc:postgresql://host:5432/mydb  # or name one explicitly
 *   -Dpresenter.dataset.gate=off                               # (the default)
 *
 *   -Dpresenter.dataset.gate.always=ds_one_RSRC,ds_two_RSRC    # optional exceptions
 * </pre>
 *
 * The exceptions list injects those presenters even though their data is absent, for
 * presenters that deliberately contribute only attributes while their questions stay
 * hardcoded in an anchor file; see {@link AlwaysLoadedDatasets}.
 *
 * A system property rather than an environment variable because the two ways a site is built
 * do not share an environment, and only a property reaches both:
 *
 * <ul>
 * <li>rebuilder discards the environment on startup (keeping only PWD, USER, LANG,
 * SUDO_USER, HOME, SSH_AUTH_SOCK and GITHUB_*), but its --gusjvmopts flag is parsed
 * afterwards, so <code>rebuilder --gusjvmopts '-Dpresenter.dataset.gate=on'</code> arrives
 * intact.</li>
 * <li>wb does not scrub, so GUSJVMOPTS exported from the site's etc/setenv reaches it.</li>
 * </ul>
 *
 * Either way the value lands here via GUSJVMOPTS, which the presenterInjectTemplates wrapper
 * forwards to the JVM. An environment variable read directly by this class was tried first:
 * it worked under wb and silently did nothing under rebuilder, which is exactly the class of
 * failure this gate exists to prevent.
 *
 * Credentials are deliberately not passed here: a command line is world-readable in ps
 * output, so the appDb login and password come from model-config.xml.
 */
public class LoadedDatasetSources {

  public static final String GATE_PROPERTY = "presenter.dataset.gate";
  public static final String ALWAYS_PROPERTY = "presenter.dataset.gate.always";

  private LoadedDatasetSources() {}

  static LoadedDatasetSource fromSiteConfig() {
    return forProperties(System.getProperty(GATE_PROPERTY), System.getProperty(ALWAYS_PROPERTY));
  }

  /**
   * @param gate value of presenter.dataset.gate
   * @param always comma- or whitespace-separated dataset names to inject regardless
   */
  static LoadedDatasetSource forProperties(String gate, String always) {
    LoadedDatasetSource source = forSetting(gate);

    Set<String> alwaysLoaded = parseAlways(always);
    return alwaysLoaded.isEmpty() ? source : new AlwaysLoadedDatasets(source, alwaysLoaded);
  }

  static Set<String> parseAlways(String always) {
    Set<String> names = new LinkedHashSet<String>();
    if (always == null) return names;

    for (String name : always.split("[,\\s]+")) {
      if (!name.trim().isEmpty()) names.add(name.trim());
    }
    return names;
  }

  /**
   * @param setting null, empty, "off" or "false" for no gate; "on" or "true" to gate on the
   *          appDb named in model-config.xml; or an explicit "jdbc:..." URL
   */
  static LoadedDatasetSource forSetting(String setting) {
    if (setting == null || setting.trim().isEmpty()) return new AllDatasetsLoaded();

    String value = setting.trim();
    if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false"))
      return new AllDatasetsLoaded();

    if (value.startsWith("jdbc:")) return new DatasourceTableDatasets(value);

    if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true"))
      return new DatasourceTableDatasets(null);

    throw new UserException("-D" + GATE_PROPERTY + " is '" + value + "', which is not"
        + " understood. Use 'on' to gate on the appDb in model-config.xml, a 'jdbc:...' URL"
        + " to name a database explicitly, or 'off' (or omit it) for no gate.");
  }
}
