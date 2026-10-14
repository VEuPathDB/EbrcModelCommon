package org.apidb.apicommon.datasetPresenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * JUnit tests for {@link LikePattern}, which must agree with SQL LIKE so that a build-time
 * dataset gate and the database never disagree about whether a presenter matches.
 */
public class TestLikePattern {

  @Test
  public void percentSpansAnyRun() {
    LikePattern pattern = new LikePattern("%_massSpec_Boothroyd_Phosphoproteome_RSRC");
    assertTrue(pattern.matches("tgonME49_massSpec_Boothroyd_Phosphoproteome_RSRC"));
    assertTrue(pattern.matches("tgonGT1_massSpec_Boothroyd_Phosphoproteome_RSRC"));
    assertFalse(pattern.matches("tgonME49_massSpec_Someone_Else_RSRC"));
  }

  @Test
  public void percentMatchesEmptyRun() {
    assertTrue(new LikePattern("%abc").matches("abc"));
    assertTrue(new LikePattern("abc%").matches("abc"));
  }

  @Test
  public void underscoreMatchesExactlyOneCharacter() {
    // '_' is a single-character wildcard in SQL LIKE, and stays one here on purpose
    assertTrue(new LikePattern("a_c").matches("abc"));
    assertTrue(new LikePattern("a_c").matches("axc"));
    assertFalse(new LikePattern("a_c").matches("ac"));
    assertFalse(new LikePattern("a_c").matches("abbc"));
  }

  @Test
  public void plainNameMatchesItself() {
    String name = "lmajFriedlin_sanger_BACEnds_clonedInsertEnds_RSRC";
    assertTrue(new LikePattern(name).matches(name));
    assertFalse(new LikePattern(name).matches(name + "_extra"));
  }

  @Test
  public void anchorsWholeString() {
    assertFalse(new LikePattern("abc").matches("xabcx"));
    assertTrue(new LikePattern("%abc%").matches("xabcx"));
  }

  @Test
  public void regexMetacharactersAreLiteral() {
    assertTrue(new LikePattern("a.c").matches("a.c"));
    assertFalse(new LikePattern("a.c").matches("abc"));

    assertTrue(new LikePattern("tcruCLBrenerEsmeraldo-like_CHORI105_BACEnds_clonedInsertEnds_RSRC")
        .matches("tcruCLBrenerEsmeraldo-like_CHORI105_BACEnds_clonedInsertEnds_RSRC"));

    assertTrue(new LikePattern("a+b").matches("a+b"));
    assertFalse(new LikePattern("a+b").matches("aab"));
    assertTrue(new LikePattern("a(b)c").matches("a(b)c"));
    assertTrue(new LikePattern("a[bc]d").matches("a[bc]d"));
    assertFalse(new LikePattern("a[bc]d").matches("abd"));
    assertTrue(new LikePattern("a$b^c|d").matches("a$b^c|d"));
    assertTrue(new LikePattern("a\\b").matches("a\\b"));
  }

  @Test
  public void translationIsReadable() {
    assertEquals(".*_x_RSRC".replace("_", "."), LikePattern.toRegex("%_x_RSRC"));
  }

  @Test
  public void nullNameNeverMatches() {
    assertFalse(new LikePattern("%").matches(null));
  }

  @Test
  public void gateIsOffWhenUnset() {
    assertTrue(LoadedDatasetSources.forSetting(null) instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forSetting("") instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forSetting("  ") instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forSetting("off") instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forSetting("FALSE") instanceof AllDatasetsLoaded);
  }

  @Test(expected = UserException.class)
  public void unrecognizedGateSettingIsRejected() {
    LoadedDatasetSources.forSetting("yes-please");
  }

  @Test
  public void gatePropertyIsParsed() {
    assertTrue(LoadedDatasetSources.forProperties(null, null) instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forProperties("off", null) instanceof AllDatasetsLoaded);
    assertTrue(LoadedDatasetSources.forProperties("  ", null) instanceof AllDatasetsLoaded);
  }

  @Test
  public void alwaysPropertyIsSplitOnCommasAndWhitespace() {
    assertEquals(0, LoadedDatasetSources.parseAlways(null).size());
    assertEquals(0, LoadedDatasetSources.parseAlways("   ").size());

    Set<String> two = LoadedDatasetSources.parseAlways(" a_RSRC, b_RSRC ");
    assertEquals(2, two.size());
    assertTrue(two.contains("a_RSRC"));
    assertTrue(two.contains("b_RSRC"));

    assertEquals(3, LoadedDatasetSources.parseAlways("a_RSRC b_RSRC,c_RSRC").size());
  }

  @Test
  public void exceptionsWrapTheGateEvenWhenItIsOff() {
    // gate off + exceptions still yields a source that answers yes for the excepted names,
    // which matters because "off" means "inject everything" rather than "inject nothing"
    LoadedDatasetSource source = LoadedDatasetSources.forProperties("off", "a_RSRC");
    assertTrue(source instanceof AlwaysLoadedDatasets);
    assertTrue(source.isLoaded("a_RSRC"));
    assertTrue(source.isLoaded("anything_else_RSRC"));
  }

  @Test
  public void exceptedDatasetsCountAsLoaded() {
    LoadedDatasetSource nothingLoaded = new LoadedDatasetSource() {
      @Override public boolean isLoaded(String nameOrPattern) { return false; }
      @Override public String describe() { return "nothing loaded"; }
    };

    Set<String> excepted = new HashSet<String>(
        Arrays.asList("pfal3D7_microarrayExpression_Derisi_TimeSeries_RSRC"));
    LoadedDatasetSource source = new AlwaysLoadedDatasets(nothingLoaded, excepted);

    assertTrue(source.isLoaded("pfal3D7_microarrayExpression_Derisi_TimeSeries_RSRC"));
    assertFalse(source.isLoaded("some_other_RSRC"));
    // a pattern presenter covering an excepted dataset is kept too
    assertTrue(source.isLoaded("%_microarrayExpression_Derisi_TimeSeries_RSRC"));
    assertFalse(source.isLoaded("%_nothing_like_it_RSRC"));
    assertTrue(source.describe().contains("Derisi"));
  }

  @Test
  public void allDatasetsLoadedAcceptsEverything() {
    LoadedDatasetSource source = new AllDatasetsLoaded();
    assertTrue(source.isLoaded("anything_RSRC"));
    assertTrue(source.isLoaded("%_pattern_RSRC"));
  }
}
