package org.apidb.apicommon.datasetPresenter;

import org.gusdb.fgputil.xml.NamedValue;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for the datasetInjector package
 *
 * @author steve
 */
public class TestDatasetInjectorPackage {

  private static final String nl = System.getProperty("line.separator");

  private static final String validPrelude = "name=rnaSeqCoverageTrack" + nl
    + "anchorFile=ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf" + nl
    + "prop=datasetName" + nl
    + "#a comment" + nl
    + nl
    + "prop=datasetDisplayName" + nl;

  private static final String validPreludeTrimmed = "name=rnaSeqCoverageTrack" + nl
    + "anchorFile=ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf" + nl
    + "prop=datasetName" + nl
    + "prop=datasetDisplayName" + nl;

  private static final String validTemplateText = "[${datasetName}]" + nl
    + "feature      = NextGenSeq:${datasetName}" + nl;

  @Test
  public void test_Template_validateTemplateText() {
    var template = new Template("dontcare");
    var props = new HashSet<String>();
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);
    template.validateTemplateText();
  }

  // invalid template: contains macro without a property
  @Test
  public void test_Template_validateTemplateText2() {
    var template = new Template("dontcare");
    var props = new HashSet<String>();
    assertThrows(UserException.class, () -> {
      template.setProps(props);
      template.setTemplateText(validTemplateText);
      template.validateTemplateText();
    });
  }

  @Test
  public void test_Template_injectTextIntoStream() {
    var template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    var targetText = "line 1" + nl
      + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl
      + "line 3" + nl;
    var targetTextAsStream = new ByteArrayInputStream(targetText.getBytes());
    var answer = template.injectTextIntoStream("WOOHOO" + nl,
      targetTextAsStream);
    assertEquals(answer, "line 1" + nl
      + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl
      + nl
      + "WOOHOO" + nl
      + nl
      + "line 3" + nl);
  }

  @Test
  public void test_Template_injectInstancesIntoStream() {

    // make template
    var template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    var props = new HashSet<String>();
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);

    // make template instances
    var propValues1 = new HashMap<String, String>();
    propValues1.put("datasetName", "HAPPY");
    TemplateInstance templateInstance1 = new TemplateInstance(
      template.getName(), propValues1);

    var propValues2 = new HashMap<String, String>();
    propValues2.put("datasetName", "SAD");
    var templateInstance2 = new TemplateInstance(template.getName(),
      propValues2);

    var templateInstances = new ArrayList<TemplateInstance>();
    templateInstances.add(templateInstance1);
    templateInstances.add(templateInstance2);

    // make target text
    var targetText = "line 1" + nl
      + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl
      + "line 3" + nl;
    var targetTextAsStream = new ByteArrayInputStream(targetText.getBytes());

    // inject instances into target
    var answer = template.injectInstancesIntoStream(templateInstances,
      targetTextAsStream, "dontcare");

    // format expected result
    var inj1 = "[HAPPY]" + nl + "feature      = NextGenSeq:HAPPY" + nl;
    var inj2 = "[SAD]" + nl + "feature      = NextGenSeq:SAD" + nl;
    var expected = "line 1" + nl
      + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl
      + nl
      + inj1 + nl
      + inj2 + nl
      + nl
      + "line 3" + nl;
    assertEquals(answer, expected);
  }

  @Test
  public void test_Template_getInstancesAsText() {

    // make template
    var template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    template.setAnchorFileNameProject("hello_MicroDB", "MicroDB");
    template.setAnchorFileNameProject("hello_MacroDB", "MacroDB");
    var props = new HashSet<String>();
    props.add("projectName");
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);

    // make template instances
    var propValues1 = new HashMap<String, String>();
    propValues1.put("datasetName", "HAPPY");
    propValues1.put("projectName", "MacroDB");
    TemplateInstance templateInstance1 = new TemplateInstance(
      template.getName(), propValues1);

    var propValues2 = new HashMap<String, String>();
    propValues2.put("datasetName", "SAD");
    propValues2.put("projectName", "MicroDB");
    TemplateInstance templateInstance2 = new TemplateInstance(
      template.getName(), propValues2);

    var templateInstances = new ArrayList<TemplateInstance>();
    templateInstances.add(templateInstance1);
    templateInstances.add(templateInstance2);

    // make target text
    String targetText = "line 1"
      + nl
      + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->"
      + nl
      + "line 3"
      + nl;
    InputStream targetTextAsStream = new ByteArrayInputStream(
      targetText.getBytes());

    // inject instances into target
    String answer = template.injectInstancesIntoStream(templateInstances,
      targetTextAsStream, "hello_MicroDB");

    assertTrue(answer.contains("SAD"));
    assertFalse(answer.contains("HAPPY"));
  }

  @Test
  public void test_Template_setAnchorFileName() {
    var template = new Template("dontknow");
    template.setAnchorFileName(
      "ApiCommonModel/DatasetPresenter/lib/test/${projectName}.conf");
    assertEquals("PlasmoDB", template.getAnchorFileProject(
      "ApiCommonModel/DatasetPresenter/lib/test/PlasmoDB.conf"));
    assertEquals("ToxoDB", template.getAnchorFileProject(
      "ApiCommonModel/DatasetPresenter/lib/test/ToxoDB.conf"));
  }

  // test: parse of template prelude
  @Test
  public void test_TemplatesParser_parsePrelude() {
    var template = new Template("dontknow");
    TemplatesParser.parsePrelude(validPreludeTrimmed, template, "dontknow");
    assertEquals("rnaSeqCoverageTrack", template.getName());
    assertEquals("ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf",
      template.getRawAnchorFileName());
    assertEquals("lib/gbrowse/WhateverDB.conf",
      template.getFirstTargetFileName());
    assertEquals(2, template.getProps().size());
    assertTrue(template.getProps().contains("datasetName"));
  }

  @Test
  public void test_TemplatesParser_splitTemplateString() {

    var answer = TemplatesParser.splitTemplateString(validPrelude
      + TemplatesParser.TEMPLATE_TEXT_START + nl
      + validTemplateText
      + TemplatesParser.TEMPLATE_TEXT_END + nl, "fakeFilePath");

    assertEquals(answer[0], validPreludeTrimmed);
    assertEquals(answer[1], validTemplateText);
  }

  @Test
  public void test_TemplatesParser_splitTemplateString_2() {
    assertThrows(UserException.class, () -> TemplatesParser.splitTemplateString(
      validPrelude + TemplatesParser.TEMPLATE_TEXT_START+ nl
      + validTemplateText + TemplatesParser.TEMPLATE_TEXT_END + nl
      + "JUNK" + nl, "fakeFilePath"));
  }

  @Test
  public void test_TemplatesParser_parseTemplatesFile() {
    var proj_home = System.getenv("PROJECT_HOME");
    var templateSet = new TemplateSet();
    TemplatesParser.parseTemplatesFile(templateSet, proj_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_templates.dst");

    assertNotNull(templateSet.getTemplateByName("test3_template1"));
    assertNotNull(templateSet.getTemplateByName("test3_template2"));
    var t1 = templateSet.getTemplateByName("test3_template1");
    var t2 = templateSet.getTemplateByName("test3_template2");
    assertEquals(t1.getTemplateText(),
      "12345" + nl + "${projectName}" + nl + "67890" + nl);
    assertEquals(t2.getTemplateText(),
      "Hello Everybody ${datasetShortDisplayName} Happy Birthday" + nl);
    assertTrue(templateSet.getTemplateNamesByAnchorFileName(
      "ApiCommonModel/DatasetPresenter/lib/test/test3_anchors.txt")
      .contains("test3_template1"));
    assertTrue(templateSet.getTemplateNamesByAnchorFileName(
      "ApiCommonModel/DatasetPresenter/lib/test/test3_anchors.txt")
      .contains("test3_template2"));
  }

  @Test
  public void test_TemplatesParser_getTemplateFilesInDir() {
    String proj_home = System.getenv("PROJECT_HOME");
    List<File> templateFiles = TemplatesParser.getTemplateFilesInDir(
      proj_home + "/ApiCommonModel/DatasetPresenter/testData");
    assertTrue(templateFiles.size() >= 2);
    for (File file : templateFiles) {
      assertTrue(file.getName().endsWith(".dst"));
    }
  }

  @Test
  public void test_TemplatesParser_parseDir() {
    var project_home = System.getenv("PROJECT_HOME");
    var templateSet = new TemplateSet();
    TemplatesParser.parseTemplatesDir(templateSet,
      project_home + "/ApiCommonModel/DatasetPresenter/testData");
    assertTrue(templateSet.getSize() >= 4);
  }

  @Test
  public void test_DatasetInjector_setClass() {
    var dic = new DatasetInjectorConstructor();
    var className = "org.apidb.apicommon.datasetPresenter.TestInjector";
    dic.setClassName(className);
    var c = dic.getDatasetInjector().getClass();
    var name = c.getName();
    assertEquals(name, className);
  }

  @Test
  public void test_DatasetInjector_inheritDatasetProps() {
    var di = new DatasetInjectorConstructor();
    var dp = new DatasetPresenter();
    di.addProp(new NamedValue("size", "too_small"));
    di.addProp(new NamedValue("color", "ugly"));
    dp.addProp(new NamedValue("weight", "negative"));
    di.inheritDatasetProps(dp);
    assertEquals(3, di.getPropValues().keySet().size());
  }

  @Test
  public void test_DatasetInjectorSet_getTemplateInstances() {

    // build model
    var dps = new DatasetPresenterSet();

    var dp1 = new DatasetPresenter();
    dp1.setName("happy");
    var di1 = new DatasetInjectorConstructor();
    di1.setClassName("org.apidb.apicommon.datasetPresenter.TestInjector");
    dp1.setDatasetInjector(di1);
    dps.addDatasetPresenter(dp1);

    var dp2 = new DatasetPresenter();
    dp2.setName("sad");
    var di3 = new DatasetInjectorConstructor();
    di3.setClassName("org.apidb.apicommon.datasetPresenter.TestInjector");
    dp2.setDatasetInjector(di3);
    dps.addDatasetPresenter(dp2);

    // run "inject" to produce a set of template instances
    var dis = new DatasetInjectorSet();
    dps.addToDatasetInjectorSet(dis);

    var fakeTemplate1Instances = dis.getTemplateInstanceSet()
      .getTemplateInstances("test3_template1");
    assertEquals(2, fakeTemplate1Instances.size());
    assertEquals("happy",
      fakeTemplate1Instances.get(0).getPropValue("datasetName"));
    assertEquals("sad",
      fakeTemplate1Instances.get(1).getPropValue("datasetName"));

    var fakeTemplate2Instances = dis.getTemplateInstanceSet()
      .getTemplateInstances("test3_template2");
    assertEquals(2, fakeTemplate2Instances.size());
    assertEquals("happy",
      fakeTemplate2Instances.get(0).getPropValue("datasetName"));
    assertEquals("sad",
      fakeTemplate2Instances.get(1).getPropValue("datasetName"));

  }

  @Test
  public void test_DatasetPresenterParser_parseFile() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");
    var dps = dpp.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");

    assertEquals(2, dps.getSize());

    var dp1 = dps.getDatasetPresenters().get("Stunnenberg_RNA-Seq_RSRC");
    var dp2 = dps.getDatasetPresenters().get("Very_Happy_RSRC");

    assertNotNull(dp1);
    assertNotNull(dp2);
    assertEquals("14", dp1.getPropValue("buildNumberIntroduced"));
    assertEquals("In good spirits", dp2.getPropValue("datasetDisplayName"));
    assertEquals("good", dp2.getPropValue("datasetShortDisplayName"));
    assertEquals("ToxoDB", dp2.getPropValue("projectName"));
    assertEquals("17", dp2.getPropValue("buildNumberIntroduced"));
    assertEquals("Well life is groovy, no?",
      dp2.getPropValue("datasetDescrip"));
    assertEquals("grooves", dp2.getPropValue("summary"));
    assertEquals("a caveat", dp2.getCaveat());
    assertEquals("an acknowledgement", dp2.getAcknowledgement());
    assertEquals("a protocol", dp2.getProtocol());
    assertEquals("a displayCategory", dp2.getDisplayCategory());
    assertEquals("a releasePolicy", dp2.getReleasePolicy());
    assertEquals(2, dp2.getContactIds().size());
    assertEquals(2, dp2.getPublications().size());
    assertEquals(2, dp2.getLinks().size());
    assertEquals("bugs.bunny", dp2.getContactIds().get(1));
    assertEquals("54321", dp2.getPublications().get(1).getPubmedId());
    assertEquals("someplace.com", dp2.getLinks().get(1).getUrl());
    assertEquals("exciting", dp2.getLinks().get(1).getText());
    assertNotNull(dp1.getDatasetInjectorConstructor());
    assertNotNull(dp2.getDatasetInjectorConstructor());
    assertEquals(2, dp1.getModelReferences().size());
    assertEquals("GeneRecord",
      dp1.getModelReferences().get(0).getRecordClassName());
    assertEquals("question", dp1.getModelReferences().get(0).getTargetType());
    assertEquals("someQuestion",
      dp1.getModelReferences().get(0).getTargetName());
    var dic = dp2.getDatasetInjectorConstructor();
    assertEquals("org.apidb.apicommon.datasetPresenter.TestInjector",
      dp2.getDatasetInjectorConstructor().getDatasetInjectorClassName());
    assertEquals("true", dic.getPropValue("isSingleStrand"));
    assertEquals(1, dps.getInternalDatasets().size());
    InternalDataset intD = dps.getInternalDatasets().get("dontcare");
    assertNotNull(intD);
    assertEquals("reallyDontCare", intD.getDatasetNamePattern());
  }

  @Test
  public void test_DatasetPresenterParser_validateXmlFile() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");
    dpp.validateXmlFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
  }

  @Test
  public void test_DatasetPresenterParser_parseDir() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");
    var dps = dpp.parseDir(
      project_home + "/ApiCommonModel/DatasetPresenter/testData", null);

    assertTrue(dps.getSize() >= 4);
  }

  @Test
  public void test_TemplatesInjector_processDatasetPresenterSet() {
    var project_home = System.getenv("PROJECT_HOME");
    var gus_home = System.getenv("GUS_HOME");
    var templatesFilePath = project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_templates.dst";
    var dpp = new DatasetPresenterParser();
    var dps = dpp.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    var templateSet = new TemplateSet();

    TemplatesParser.parseTemplatesFile(templateSet, templatesFilePath);

    var templatesInjector = new TemplatesInjector(dps, templateSet);

    var expected = new File(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_answer.txt");
    var got = new File(gus_home + "/lib/test/test3_anchors.txt");
    if (got.exists())
      assertTrue(got.delete());

    templatesInjector.processDatasetPresenterSet(project_home, gus_home);

    assertEquals(got.length(), expected.length());  // hard to imagine they could be the same size and not identical.
  }

  @Test
  public void test_TemplatesInjector_getCmdLine() {
    var args = new String[]{ "-presentersDir", "lib/xml/datasetPresenters",
      "-templatesDir", "lib/dst", "-contactsXmlFile", "someFile" };
    var cl = TemplatesInjector.getCmdLine(args);
    assertEquals("lib/xml/datasetPresenters", cl.getOptionValue("presentersDir"));
    assertEquals("lib/dst", cl.getOptionValue("templatesDir"));
  }

  @Test
  public void test_TemplatesInjector_parseAndProcess() {
    var gus_home = System.getenv("GUS_HOME");

    TemplatesInjector.parseAndProcess(gus_home + "/lib/test",
      gus_home + "/lib/test", null, gus_home
        + "/lib/xml/datasetPresenters/contacts/contacts.xml");  // if it doesn't throw an exception we are good
  }

  @Test
  public void test_ConfigurationParser_parseFile() {
    var parser = new ConfigurationParser();
    var project_home = System.getenv("PROJECT_HOME");
    var config = parser.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/tuningProps.xml.test");
    assertEquals("nonayerbusiness", config.getPassword());
    assertEquals("prince", config.getUsername());
  }

  @Test
  public void test_ContactsFileParser_parseFile() {
    var parser = new ContactsFileParser();
    var project_home = System.getenv("PROJECT_HOME");
    var contacts = parser.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
    assertEquals("Bugs Bunny", contacts.get("bugs.bunny").getName());
  }

  @Test
  public void test_ContactsFileParser_validateXmlFile() {
    var parser = new ContactsFileParser();
    var project_home = System.getenv("PROJECT_HOME");
    parser.validateXmlFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
  }

  // passes if there are no exceptions thrown
  @Test
  public void test_DatasetPresenterSet_validateContactIds() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");

    var dps = dpp.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    dps.validateContactIds(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
  }

  @Test
  public void test_DatasetPresenter_getContacts() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");
    var dps = dpp.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    var parser = new ContactsFileParser();
    var allContacts = parser.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
    var dp2 = dps.getDatasetPresenters().get("Very_Happy_RSRC");
    var contacts = dp2.getContacts(allContacts);
    var contact1 = contacts.get(0);
    var contact2 = contacts.get(1);

    assertEquals("Elmer Fudd", contact1.getName());
    assertTrue(contact1.getIsPrimary());
    assertEquals("Bugs Bunny", contact2.getName());
    assertFalse(contact2.getIsPrimary());
  }

  @Test
  public void test_DatasetPresenterParser_parseDefaultInjectorsFile() {
    var project_home = System.getenv("PROJECT_HOME");
    var map = DatasetPresenterParser.parseDefaultInjectorsFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/defaultInjectors.tab");

    assertEquals(2, map.size());
    assertEquals("org.apidb.apicommon.datasetPresenter.RnaSeqInjector",
      map.get("rnaSeq").get("paired"));
    assertEquals("org.apidb.apicommon.datasetPresenter.TestInjector",
      map.get("test").get("happy"));
  }

  @Test
  public void test_DatasetPresenter_addDefaultDatasetInjector() {
    var project_home = System.getenv("PROJECT_HOME");
    var map = DatasetPresenterParser.parseDefaultInjectorsFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/defaultInjectors.tab");
    var dp = new DatasetPresenter();

    dp.setType("rnaSeq");
    dp.setSubtype("paired");
    dp.setDefaultDatasetInjector(map);

    assertEquals("org.apidb.apicommon.datasetPresenter.RnaSeqInjector",
      dp.getDatasetInjectorConstructor().getClassName());
  }

  @Test
  public void test_DatasetPropertiesParser_parseFile() {
    var gus_home = System.getenv("GUS_HOME");
    var answer = new HashMap<String, Map<String, String>>();
    var duplicateDatasetNames = new HashSet<String>();

    DatasetPropertiesParser.parseFile(
      gus_home + "/lib/prop/datasetProperties/test1.prop" + "", answer,
      duplicateDatasetNames);

    assertEquals(2, answer.size());
    assertEquals("GeneDB",
      answer.get("PlasmoDB:pberANKA_primary_genome_RSRC").get("name"));
    assertEquals("5823",
      answer.get("PlasmoDB:pberANKA_primary_genome_features_RSRC")
        .get("ncbiTaxonId"));
  }

  @Test
  public void test_DatasetPropertiesParser_parseAllPropertyFiles() {
    var dpp = new DatasetPropertiesParser();
    var propertiesFromFiles = new HashMap<String, Map<String, String>>();
    var duplicateDatasetNames = new HashSet<String>();

    dpp.parseAllPropertyFiles(propertiesFromFiles, duplicateDatasetNames);

    assertEquals(4, propertiesFromFiles.size());
    assertEquals("GeneDB",
      propertiesFromFiles.get("PlasmoDB:pberANKA_primary_genome_RSRC")
        .get("name"));
    assertEquals("HappyDB",
      propertiesFromFiles.get("PlasmoDB:pberANKA_secondary_genome_RSRC")
        .get("projectName"));
    assertEquals("5823",
      propertiesFromFiles.get("PlasmoDB:pberANKA_primary_genome_features_RSRC")
        .get("ncbiTaxonId"));
  }

  @Test
  public void test_DatasetPresenterParser_addPropertiesFromFile() {
    var dpp = new DatasetPresenterParser();
    var project_home = System.getenv("PROJECT_HOME");
    var dps = dpp.parseFile(project_home
      + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    var propParser = new DatasetPropertiesParser();
    var propertiesFromFiles = new HashMap<String, Map<String, String>>();
    var duplicateDatasetNames = new HashSet<String>();

    propParser.parseAllPropertyFiles(propertiesFromFiles,
      duplicateDatasetNames);
    dps.addPropertiesFromFiles(propertiesFromFiles, duplicateDatasetNames);

    var dp1 = dps.getDatasetPresenters()
      .get("Stunnenberg_RNA-Seq_RSRC");

    assertEquals("SuperDB", dp1.getPropValue("projectName2"));
  }
}
