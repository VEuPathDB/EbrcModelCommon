package org.apidb.apicommon.datasetPresenter;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.gusdb.fgputil.xml.NamedValue;
import org.junit.Test;

/**
 * JUnit tests for the datasetInjector package
 * 
 * @author steve
 * 
 */
public class TestDatasetInjectorPackage {

  private static final String nl = System.getProperty("line.separator");

  private static final String validPrelude = "name=rnaSeqCoverageTrack" + nl
      + "anchorFile=ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf" + nl
      + "prop=datasetName" + nl + "#a comment" + nl + nl
      + "prop=datasetDisplayName" + nl;

  private static final String validPreludeTrimmed = "name=rnaSeqCoverageTrack"
      + nl + "anchorFile=ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf"
      + nl + "prop=datasetName" + nl + "prop=datasetDisplayName" + nl;

  private static final String validTemplateText = "[${datasetName}]" + nl
      + "feature      = NextGenSeq:${datasetName}" + nl;

  @Test
  public void test_Template_validateTemplateText() {
    Template template = new Template("dontcare");
    Set<String> props = new HashSet<String>();
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);
    template.validateTemplateText();
  }

  // invalid template: contains macro without a property
  @Test(expected = UserException.class)
  public void test_Template_validateTemplateText2() {
    Template template = new Template("dontcare");
    Set<String> props = new HashSet<String>();
    template.setProps(props);
    template.setTemplateText(validTemplateText);
    template.validateTemplateText();
  }

  @Test
  public void test_Template_injectTextIntoStream() {
    Template template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    String targetText = "line 1" + nl
        + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl + "line 3"
        + nl;
    InputStream targetTextAsStream = new ByteArrayInputStream(
        targetText.getBytes());
    String answer = template.injectTextIntoStream("WOOHOO" + nl,
        targetTextAsStream);
    assertTrue(answer.equals("line 1" + nl
        + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl + nl
        + "WOOHOO" + nl + nl + "line 3" + nl));
  }

  @Test
  public void test_Template_injectInstancesIntoStream() {

    // make template
    Template template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    Set<String> props = new HashSet<String>();
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);

    // make template instances
    Map<String, String> propValues1 = new HashMap<String, String>();
    propValues1.put("datasetName", "HAPPY");
    TemplateInstance templateInstance1 = new TemplateInstance(
        template.getName(), propValues1);

    Map<String, String> propValues2 = new HashMap<String, String>();
    propValues2.put("datasetName", "SAD");
    TemplateInstance templateInstance2 = new TemplateInstance(
        template.getName(), propValues2);

    List<TemplateInstance> templateInstances = new ArrayList<TemplateInstance>();
    templateInstances.add(templateInstance1);
    templateInstances.add(templateInstance2);

    // make target text
    String targetText = "line 1" + nl
        + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl + "line 3"
        + nl;
    InputStream targetTextAsStream = new ByteArrayInputStream(
        targetText.getBytes());

    // inject instances into target
    String answer = template.injectInstancesIntoStream(templateInstances,
        targetTextAsStream, "dontcare");

    // format expected result
    String inj1 = "[HAPPY]" + nl + "feature      = NextGenSeq:HAPPY" + nl;
    String inj2 = "[SAD]" + nl + "feature      = NextGenSeq:SAD" + nl;
    String expected = "line 1" + nl
        + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl + nl + inj1
        + nl + inj2 + nl + nl + "line 3" + nl;
    assertTrue(answer.equals(expected));
  }

  @Test
  public void test_Template_getInstancesAsText() {

    // make template
    Template template = new Template("dontcare");
    template.setName("rnaSeqFoldChangeQuestion");
    template.setAnchorFileNameProject("hello_MicroDB", "MicroDB");
    template.setAnchorFileNameProject("hello_MacroDB", "MacroDB");
    Set<String> props = new HashSet<String>();
    props.add("projectName");
    props.add("datasetName");
    template.setProps(props);
    template.setTemplateText(validTemplateText);

    // make template instances
    Map<String, String> propValues1 = new HashMap<String, String>();
    propValues1.put("datasetName", "HAPPY");
    propValues1.put("projectName", "MacroDB");
    TemplateInstance templateInstance1 = new TemplateInstance(
        template.getName(), propValues1);

    Map<String, String> propValues2 = new HashMap<String, String>();
    propValues2.put("datasetName", "SAD");
    propValues2.put("projectName", "MicroDB");
    TemplateInstance templateInstance2 = new TemplateInstance(
        template.getName(), propValues2);

    List<TemplateInstance> templateInstances = new ArrayList<TemplateInstance>();
    templateInstances.add(templateInstance1);
    templateInstances.add(templateInstance2);

    // make target text
    String targetText = "line 1" + nl
        + "<!-- TEMPLATE_ANCHOR rnaSeqFoldChangeQuestion -->" + nl + "line 3"
        + nl;
    InputStream targetTextAsStream = new ByteArrayInputStream(
        targetText.getBytes());

    // inject instances into target
    String answer = template.injectInstancesIntoStream(templateInstances,
        targetTextAsStream, "hello_MicroDB");
    
    assertTrue(answer.contains("SAD"));
    assertTrue(!answer.contains("HAPPY"));
  }

  @Test
  public void test_Template_setAnchorFileName() {
    Template template = new Template("dontknow");
    template.setAnchorFileName("ApiCommonModel/DatasetPresenter/lib/test/${projectName}.conf");
    assertTrue(template.getAnchorFileProject("ApiCommonModel/DatasetPresenter/lib/test/PlasmoDB.conf").equals("PlasmoDB"));
    assertTrue(template.getAnchorFileProject("ApiCommonModel/DatasetPresenter/lib/test/ToxoDB.conf").equals("ToxoDB"));
  }
  
  // test: parse of template prelude
  @Test
  public void test_TemplatesParser_parsePrelude() {
    Template template = new Template("dontknow");
    TemplatesParser.parsePrelude(validPreludeTrimmed, template, "dontknow");
    assertTrue(template.getName().equals("rnaSeqCoverageTrack"));
    assertTrue(template.getRawAnchorFileName().equals(
        "ApiCommonModel/Model/lib/gbrowse/WhateverDB.conf"));
    assertTrue(template.getFirstTargetFileName().equals(
        "lib/gbrowse/WhateverDB.conf"));
    assertTrue(template.getProps().size() == 2);
    assertTrue(template.getProps().contains("datasetName"));
  }

  @Test
  public void test_TemplatesParser_splitTemplateString() {

    String[] answer = TemplatesParser.splitTemplateString(validPrelude
        + TemplatesParser.TEMPLATE_TEXT_START + nl + validTemplateText
        + TemplatesParser.TEMPLATE_TEXT_END + nl, "fakeFilePath");

    assertTrue(answer[0].equals(validPreludeTrimmed));
    assertTrue(answer[1].equals(validTemplateText));
  }
  
  @Test(expected = UserException.class)
  public void test_TemplatesParser_splitTemplateString_2() {
    TemplatesParser.splitTemplateString(validPrelude
        + TemplatesParser.TEMPLATE_TEXT_START + nl + validTemplateText
        + TemplatesParser.TEMPLATE_TEXT_END + nl + "JUNK" + nl,
        "fakeFilePath");
  }

  @Test
  public void test_TemplatesParser_parseTemplatesFile() {
    String proj_home = System.getenv("PROJECT_HOME");
    TemplateSet templateSet = new TemplateSet();
    TemplatesParser.parseTemplatesFile(templateSet, proj_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_templates.dst");

    assertTrue(templateSet.getTemplateByName("test3_template1") != null);
    assertTrue(templateSet.getTemplateByName("test3_template2") != null);
    Template t1 = templateSet.getTemplateByName("test3_template1");
    Template t2 = templateSet.getTemplateByName("test3_template2");
    assertTrue(t1.getTemplateText().equals("12345" + nl + "${projectName}" + nl + "67890" + nl));
    assertTrue(t2.getTemplateText().equals(
        "Hello Everybody ${datasetShortDisplayName} Happy Birthday" + nl));
    assertTrue(templateSet.getTemplateNamesByAnchorFileName("ApiCommonModel/DatasetPresenter/lib/test/test3_anchors.txt").contains(
        "test3_template1"));
    assertTrue(templateSet.getTemplateNamesByAnchorFileName("ApiCommonModel/DatasetPresenter/lib/test/test3_anchors.txt").contains(
        "test3_template2"));
  }
  
  @Test
  public void test_TemplatesParser_getTemplateFilesInDir() {
    String proj_home = System.getenv("PROJECT_HOME");
    List<File> templateFiles = TemplatesParser.getTemplateFilesInDir(proj_home + "/ApiCommonModel/DatasetPresenter/testData");
    assertTrue(templateFiles.size() >= 2);
    for (File file : templateFiles) {
      assertTrue(file.getName().endsWith(".dst"));
    }
  }
  
  @Test
  public void test_TemplatesParser_parseDir() {
    String project_home = System.getenv("PROJECT_HOME");
    TemplateSet templateSet = new TemplateSet();
    TemplatesParser.parseTemplatesDir(templateSet, project_home
        + "/ApiCommonModel/DatasetPresenter/testData");
    assertTrue(templateSet.getSize() >= 4);
  }
  


  @Test
  public void test_DatasetInjector_setClass() {
    DatasetInjectorConstructor dic = new DatasetInjectorConstructor();
    String className = "org.apidb.apicommon.datasetPresenter.TestInjector";
    dic.setClassName(className);
    Class<?> c = dic.getDatasetInjector().getClass();
    String name = c.getName();
    assertTrue(name.equals(className));
  }

  @Test
  public void test_DatasetInjector_inheritDatasetProps() {
    DatasetInjectorConstructor di = new DatasetInjectorConstructor();
    DatasetPresenter dp = new DatasetPresenter();
    di.addProp(new NamedValue("size", "too_small"));
    di.addProp(new NamedValue("color", "ugly"));
    dp.addProp(new NamedValue("weight", "negative"));
    di.inheritDatasetProps(dp);
    assertTrue(di.getPropValues().keySet().size() == 3);
  }

  @Test
  public void test_DatasetInjectorSet_getTemplateInstances() {

    // build model
    DatasetPresenterSet dps = new DatasetPresenterSet();

    DatasetPresenter dp1 = new DatasetPresenter();
    dp1.setName("happy");
    DatasetInjectorConstructor di1 = new DatasetInjectorConstructor();
    di1.setClassName("org.apidb.apicommon.datasetPresenter.TestInjector");
    dp1.setDatasetInjector(di1);
    dps.addDatasetPresenter(dp1);

    DatasetPresenter dp2 = new DatasetPresenter();
    dp2.setName("sad");
    DatasetInjectorConstructor di3 = new DatasetInjectorConstructor();
    di3.setClassName("org.apidb.apicommon.datasetPresenter.TestInjector");
    dp2.setDatasetInjector(di3);
    dps.addDatasetPresenter(dp2);

    // run "inject" to produce a set of template instances
    DatasetInjectorSet dis = new DatasetInjectorSet();
    dps.addToDatasetInjectorSet(dis);

    List<TemplateInstance> fakeTemplate1Instances = dis.getTemplateInstanceSet().getTemplateInstances(
        "test3_template1");
    assertTrue(fakeTemplate1Instances.size() == 2);
    assertTrue(fakeTemplate1Instances.get(0).getPropValue("datasetName").equals(
        "happy"));
    assertTrue(fakeTemplate1Instances.get(1).getPropValue("datasetName").equals(
        "sad"));

    List<TemplateInstance> fakeTemplate2Instances = dis.getTemplateInstanceSet().getTemplateInstances(
        "test3_template2");
    assertTrue(fakeTemplate2Instances.size() == 2);
    assertTrue(fakeTemplate2Instances.get(0).getPropValue("datasetName").equals(
        "happy"));
    assertTrue(fakeTemplate2Instances.get(1).getPropValue("datasetName").equals(
        "sad"));

  }

  @Test
  public void test_DatasetPresenterParser_parseFile() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");

    DatasetPresenterSet dps = dpp.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    assertTrue(dps.getSize() == 2);
    DatasetPresenter dp1 = dps.getDatasetPresenters().get("Stunnenberg_RNA-Seq_RSRC");
    DatasetPresenter dp2 = dps.getDatasetPresenters().get("Very_Happy_RSRC");
    assertTrue(dp1 != null);
    assertTrue(dp2 != null);
    assertTrue(dp1.getPropValue("buildNumberIntroduced").equals("14"));
    assertTrue(dp2.getPropValue("datasetDisplayName").equals("In good spirits"));
    assertTrue(dp2.getPropValue("datasetShortDisplayName").equals("good"));
    assertTrue(dp2.getPropValue("projectName").equals("ToxoDB"));
    assertTrue(dp2.getPropValue("buildNumberIntroduced").equals("17"));
    assertTrue(dp2.getPropValue("datasetDescrip").equals("Well life is groovy, no?"));
    assertTrue(dp2.getPropValue("summary").equals("grooves"));
    assertTrue(dp2.getCaveat().equals("a caveat"));
    assertTrue(dp2.getAcknowledgement().equals("an acknowledgement"));
    assertTrue(dp2.getProtocol().equals("a protocol"));
    assertTrue(dp2.getDisplayCategory().equals("a displayCategory"));
    assertTrue(dp2.getReleasePolicy().equals("a releasePolicy"));
    assertTrue(dp2.getContactIds().size() == 2);
    assertTrue(dp2.getPublications().size() == 2);
    assertTrue(dp2.getLinks().size() == 2);
    assertTrue(dp2.getContactIds().get(1).equals("bugs.bunny"));
    assertTrue(dp2.getPublications().get(1).getPubmedId().equals("54321"));
    assertTrue(dp2.getLinks().get(1).getUrl().equals("someplace.com"));
    assertTrue(dp2.getLinks().get(1).getText().equals("exciting"));
    assertTrue(dp1.getDatasetInjectorConstructor() != null);
    assertTrue(dp2.getDatasetInjectorConstructor() != null);
    assertTrue(dp1.getModelReferences().size() == 2);
    assertTrue(dp1.getModelReferences().get(0).getRecordClassName().equals("GeneRecord"));
    assertTrue(dp1.getModelReferences().get(0).getTargetType().equals("question"));
    assertTrue(dp1.getModelReferences().get(0).getTargetName().equals("someQuestion"));
    DatasetInjectorConstructor dic = dp2.getDatasetInjectorConstructor();
    assertTrue(dp2.getDatasetInjectorConstructor().getDatasetInjectorClassName().equals("org.apidb.apicommon.datasetPresenter.TestInjector"));
    assertTrue(dic.getPropValue("isSingleStrand").equals("true"));
    assertTrue(dps.getInternalDatasets().size() == 1);
    InternalDataset intD = dps.getInternalDatasets().get("dontcare");
    assertTrue(intD != null);
    assertTrue(intD.getDatasetNamePattern().equals("reallyDontCare"));
  }

  @Test
  public void test_DatasetPresenterParser_validateXmlFile() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");
    dpp.validateXmlFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
  }
  
  @Test
  public void test_DatasetPresenterParser_parseDir() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");

    DatasetPresenterSet dps = dpp.parseDir(project_home
        + "/ApiCommonModel/DatasetPresenter/testData", null);
    assertTrue(dps.getSize() >= 4);
  }
  
  @Test
  public void test_TemplatesInjector_processDatasetPresenterSet() {

    String project_home = System.getenv("PROJECT_HOME");
    String gus_home = System.getenv("GUS_HOME");
    String templatesFilePath = project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_templates.dst";

    DatasetPresenterParser dpp = new DatasetPresenterParser();
    DatasetPresenterSet dps = dpp.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    TemplateSet templateSet = new TemplateSet();
    TemplatesParser.parseTemplatesFile(templateSet, templatesFilePath);
    
    TemplatesInjector templatesInjector = new TemplatesInjector(dps, templateSet);
    
    File expected = new File(project_home + "/ApiCommonModel/DatasetPresenter/testData/test3_answer.txt");
    File got = new File(gus_home + "/lib/test/test3_anchors.txt");
    if (got.exists()) got.delete();
    
    templatesInjector.processDatasetPresenterSet(project_home, gus_home);

    assertTrue(got.length() == expected.length());  // hard to imagine they could be the same size and not identical.
  }
  
  @Test
  public void test_TemplatesInjector_getCmdLine() {
    String[] args = { "-presentersDir", "lib/xml/datasetPresenters", "-templatesDir", "lib/dst", "-contactsXmlFile", "someFile"};
    CommandLine cl = TemplatesInjector.getCmdLine(args);
    assertTrue(cl.getOptionValue("presentersDir").equals("lib/xml/datasetPresenters"));
    assertTrue(cl.getOptionValue("templatesDir").equals("lib/dst"));
  }
  
  @Test
  public void test_TemplatesInjector_parseAndProcess() {
    String gus_home = System.getenv("GUS_HOME");
    
    TemplatesInjector.parseAndProcess(gus_home + "/lib/test", gus_home + "/lib/test", null, gus_home + "/lib/xml/datasetPresenters/contacts/contacts.xml");  // if it doesn't throw an exception we are good
  }
  
  @Test 
  public void test_ConfigurationParser_parseFile() {
    ConfigurationParser parser = new ConfigurationParser();
    String project_home = System.getenv("PROJECT_HOME");
    Configuration config = parser.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/tuningProps.xml.test");
    assertTrue(config.getPassword().equals("nonayerbusiness"));
    assertTrue(config.getUsername().equals("prince"));
  }
  
  @Test 
  public void test_ContactsFileParser_parseFile() {
    ContactsFileParser parser = new ContactsFileParser();
    String project_home = System.getenv("PROJECT_HOME");
    Contacts contacts = parser.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
    assertTrue(contacts.get("bugs.bunny").getName().equals("Bugs Bunny"));
  }
  
  @Test
  public void test_ContactsFileParser_validateXmlFile() {
    ContactsFileParser parser = new ContactsFileParser();
    String project_home = System.getenv("PROJECT_HOME");
    parser.validateXmlFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
  }

  // passes if there are no exceptions thrown
  @Test
  public void test_DatasetPresenterSet_validateContactIds() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");

    DatasetPresenterSet dps = dpp.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    dps.validateContactIds(project_home + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
  }
  
  @Test
  public void test_DatasetPresenter_getContacts() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");

    DatasetPresenterSet dps = dpp.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    ContactsFileParser parser = new ContactsFileParser();
    Contacts allContacts = parser.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/contacts.xml.test");
    DatasetPresenter dp2 = dps.getDatasetPresenters().get("Very_Happy_RSRC");
    List<Contact> contacts = dp2.getContacts(allContacts);
    Contact contact1 = contacts.get(0);
    Contact contact2 = contacts.get(1);
    assertTrue(contact1.getName().equals("Elmer Fudd"));
    assertTrue(contact1.getIsPrimary());
    assertTrue(contact2.getName().equals("Bugs Bunny"));
    assertTrue(!contact2.getIsPrimary());
    
  }
  
  @Test
  public void test_DatasetPresenterParser_parseDefaultInjectorsFile() {
    String project_home = System.getenv("PROJECT_HOME");

    Map<String, Map<String, String>> map = DatasetPresenterParser.parseDefaultInjectorsFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/defaultInjectors.tab");
   assertTrue(map.size() == 2);
   assertTrue(map.get("rnaSeq").get("paired").equals("org.apidb.apicommon.datasetPresenter.RnaSeqInjector"));
   assertTrue(map.get("test").get("happy").equals("org.apidb.apicommon.datasetPresenter.TestInjector"));
  }
  
  @Test
  public void test_DatasetPresenter_addDefaultDatasetInjector() {
    String project_home = System.getenv("PROJECT_HOME");

    Map<String, Map<String, String>> map = DatasetPresenterParser.parseDefaultInjectorsFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/defaultInjectors.tab");
    DatasetPresenter dp = new DatasetPresenter();
    dp.setType("rnaSeq");
    dp.setSubtype("paired");
    dp.setDefaultDatasetInjector(map);
    assertTrue(dp.getDatasetInjectorConstructor().getClassName().equals("org.apidb.apicommon.datasetPresenter.RnaSeqInjector"));
  }
  
  @Test
  public void test_DatasetPropertiesParser_parseFile() {
    String gus_home = System.getenv("GUS_HOME");
    Map<String,Map<String,String>> answer = new HashMap<String,Map<String,String>>();
    Set<String> duplicateDatasetNames = new HashSet<String>();
    DatasetPropertiesParser.parseFile(gus_home + "/lib/prop/datasetProperties/test1.prop" +
    		"", answer, duplicateDatasetNames);
    assertTrue(answer.size() == 2);
    assertTrue(answer.get("PlasmoDB:pberANKA_primary_genome_RSRC").get("name").equals("GeneDB"));
    assertTrue(answer.get("PlasmoDB:pberANKA_primary_genome_features_RSRC").get("ncbiTaxonId").equals("5823"));
  }
  
  @Test
  public void test_DatasetPropertiesParser_parseAllPropertyFiles() {
    DatasetPropertiesParser dpp = new DatasetPropertiesParser();
    Map<String,Map<String,String>> propertiesFromFiles = new HashMap<String,Map<String,String>>();
    Set<String> duplicateDatasetNames = new HashSet<String>();
    dpp.parseAllPropertyFiles(propertiesFromFiles, duplicateDatasetNames);
    assertTrue(propertiesFromFiles.size() == 4);
    assertTrue(propertiesFromFiles.get("PlasmoDB:pberANKA_primary_genome_RSRC").get("name").equals("GeneDB"));
    assertTrue(propertiesFromFiles.get("PlasmoDB:pberANKA_secondary_genome_RSRC").get("projectName").equals("HappyDB"));
    assertTrue(propertiesFromFiles.get("PlasmoDB:pberANKA_primary_genome_features_RSRC").get("ncbiTaxonId").equals("5823"));
  }
  
  @Test
  public void test_DatasetPresenterParser_addPropertiesFromFile() {
    DatasetPresenterParser dpp = new DatasetPresenterParser();
    String project_home = System.getenv("PROJECT_HOME");

    DatasetPresenterSet dps = dpp.parseFile(project_home
        + "/ApiCommonModel/DatasetPresenter/testData/test3_presenterSet.xml");
    DatasetPropertiesParser propParser = new DatasetPropertiesParser();
    Map<String,Map<String,String>> propertiesFromFiles = new HashMap<String,Map<String,String>>();
    Set<String> duplicateDatasetNames = new HashSet<String>();
    propParser.parseAllPropertyFiles(propertiesFromFiles, duplicateDatasetNames);
    dps.addPropertiesFromFiles(propertiesFromFiles, duplicateDatasetNames);
    DatasetPresenter dp1 = dps.getDatasetPresenters().get("Stunnenberg_RNA-Seq_RSRC");
    assertTrue(dp1.getPropValue("projectName2").equals("SuperDB"));
  }
}
