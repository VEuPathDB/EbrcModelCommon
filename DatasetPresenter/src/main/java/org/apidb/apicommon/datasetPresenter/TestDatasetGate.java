package org.apidb.apicommon.datasetPresenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * JUnit tests for the dataset gate's effect on a DatasetPresenterSet: which presenters
 * survive, and how the skipped ones are reported.
 */
public class TestDatasetGate {

  /** a LoadedDatasetSource that knows a fixed set of loaded dataset names */
  private static class FakeLoadedDatasets implements LoadedDatasetSource {

    private final Set<String> _loaded;

    FakeLoadedDatasets(String... loaded) {
      _loaded = new HashSet<String>(Arrays.asList(loaded));
    }

    @Override
    public boolean isLoaded(String nameOrPattern) {
      if (nameOrPattern == null) return false;
      if (_loaded.contains(nameOrPattern)) return true;
      LikePattern pattern = new LikePattern(nameOrPattern);
      for (String name : _loaded) {
        if (pattern.matches(name)) return true;
      }
      return false;
    }

    @Override
    public String describe() {
      return "fake gate with " + _loaded.size() + " loaded datasets";
    }
  }

  private static DatasetPresenter presenter(String name) {
    DatasetPresenter presenter = new DatasetPresenter();
    presenter.setName(name);
    return presenter;
  }

  private static DatasetPresenter patternPresenter(String name, String pattern) {
    DatasetPresenter presenter = presenter(name);
    presenter.setDatasetNamePattern(pattern);
    return presenter;
  }

  @Test
  public void keepsLoadedAndDropsUnloaded() {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(presenter("lmajFriedlin_sanger_BACEnds_clonedInsertEnds_RSRC"));
    set.addDatasetPresenter(presenter("tgonME49_Sanger_BAC_ends_clonedInsertEnds_RSRC"));

    set.retainLoadedDatasets(
        new FakeLoadedDatasets("lmajFriedlin_sanger_BACEnds_clonedInsertEnds_RSRC"));

    assertEquals(1, set.getSize());
    assertTrue(set.getDatasetPresenters()
        .containsKey("lmajFriedlin_sanger_BACEnds_clonedInsertEnds_RSRC"));
    assertEquals(1, set.getPresentersNotLoaded().size());
    assertTrue(set.getPresentersNotLoaded()
        .contains("tgonME49_Sanger_BAC_ends_clonedInsertEnds_RSRC"));
  }

  @Test
  public void patternPresenterSurvivesOnAnyMatch() {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(patternPresenter("_massSpec_Phosphoproteome_RSRC",
        "%_massSpec_Phosphoproteome_RSRC"));

    set.retainLoadedDatasets(new FakeLoadedDatasets("tgonME49_massSpec_Phosphoproteome_RSRC"));

    assertEquals(1, set.getSize());
    assertTrue(set.getPresentersNotLoaded().isEmpty());
  }

  @Test
  public void patternPresenterIsDroppedWhenNothingMatches() {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(patternPresenter("_massSpec_Phosphoproteome_RSRC",
        "%_massSpec_Phosphoproteome_RSRC"));

    set.retainLoadedDatasets(new FakeLoadedDatasets("tgonME49_primary_genome_RSRC"));

    assertEquals(0, set.getSize());
    // the pattern, not the name, is what failed to match, so it is what gets reported
    assertTrue(set.getPresentersNotLoaded().contains("%_massSpec_Phosphoproteome_RSRC"));
  }

  @Test
  public void defaultSourceKeepsEverything() {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(presenter("anything_RSRC"));
    set.addDatasetPresenter(presenter("anything_else_RSRC"));

    set.retainLoadedDatasets(new AllDatasetsLoaded());

    assertEquals(2, set.getSize());
    assertTrue(set.getPresentersNotLoaded().isEmpty());
  }

  @Test
  public void skippedNamesGoToAFileNotTheLog() throws Exception {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(presenter("absent_one_RSRC"));
    set.addDatasetPresenter(presenter("absent_two_RSRC"));
    set.retainLoadedDatasets(new FakeLoadedDatasets("something_else_RSRC"));

    File report = new File(Files.createTempDirectory("gate").toFile(), "sub/report.txt");
    set.reportPresentersNotLoaded(report);

    assertTrue(report.isFile());
    List<String> lines = Files.readAllLines(report.toPath(), StandardCharsets.UTF_8);
    assertTrue(lines.contains("absent_one_RSRC"));
    assertTrue(lines.contains("absent_two_RSRC"));
    // comments explain why the file exists, so a reader does not mistake it for errors
    assertTrue(lines.get(0).startsWith("#"));
  }

  @Test
  public void reportingSurvivesAnUnwritableTarget() {
    DatasetPresenterSet set = new DatasetPresenterSet();
    set.addDatasetPresenter(presenter("absent_RSRC"));
    set.retainLoadedDatasets(new FakeLoadedDatasets("present_RSRC"));

    // must not throw: a reporting failure cannot be allowed to break the build
    set.reportPresentersNotLoaded(new File("/proc/cannot/write/here.txt"));
    set.reportPresentersNotLoaded(null);

    assertFalse(set.getPresentersNotLoaded().isEmpty());
  }
}
