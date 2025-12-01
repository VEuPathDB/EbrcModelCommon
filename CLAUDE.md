# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EbrcModelCommon contains shared data type definitions and structured searches for VEuPathDB (Vector and Eukaryotic Pathogen Database) sites. It provides common infrastructure for representing datasets, managing user datasets, and injecting data into WDK (Workflow Development Kit) model XML files.

This project is specialized by:
- [ApiCommonModel](https://github.com/VEuPathDB/ApiCommonModel)
- [OrthoMCLModel](https://github.com/VEuPathDB/OrthoMCLModel)

## Build System

This is a multi-module Maven project with Ant build support:

### Maven Build
```bash
# Build all modules
mvn clean install

# Build specific module
cd DatasetPresenter && mvn clean install
cd Model && mvn clean install
```

### Ant Build (Legacy)
```bash
# Requires environment variables: GUS_HOME, PROJECT_HOME
# Requires WEBAPP_PROP_FILE with property: webappTargetDir=<path>

# Build entire project
bld EbrcModelCommon

# Note: Changes to WDK model XML files require Tomcat restart
```

## Project Structure

### Two Main Modules

1. **DatasetPresenter** - Core dataset presentation framework
   - Location: `DatasetPresenter/src/main/java/org/apidb/apicommon/datasetPresenter/`
   - Parses dataset XML configurations and manages dataset metadata
   - Provides `DatasetPresenter` class: holds dataset properties, contacts, publications, history
   - Provides `DatasetInjector` abstract class: injects datasets into WDK presentation layer via templates

2. **Model** - Dataset-specific implementations and WDK definitions
   - Location: `Model/src/main/java/org/apidb/apicommon/model/`
   - Contains concrete `DatasetInjector` subclasses for specific dataset types (GeneOntology, UniProt, InterPro, etc.)
   - Contains `UserDatasetTypeHandler` implementations for user-uploaded datasets (ISASimpleTypeHandler, BiomTypeHandler)
   - WDK model XML files: `Model/lib/wdk/`
   - Dataset template files: `Model/lib/dst/`

### Key Directories

- `Model/bin/` - Scripts for dataset processing and tuning manager operations:
  - `propertiesFromDatasets` - Extract dataset properties
  - `jbrowseFromDatasets` - Generate JBrowse configurations
  - `buildDatasetPresentersTT` - Build tuning tables for dataset presenters
  - `updateResourcesWithPubmed` - Update dataset references with PubMed data

- `Model/lib/wdk/` - WDK model XML definitions
  - `model/records/` - Record class definitions (datasets, datasources, user datasets)
  - `model/questions/` - Search definitions and queries
  - `ontology/` - Category ontologies for organizing searches

- `Model/lib/xml/` - Configuration files
  - `datasetPresenters/global.xml` - Global dataset presenter definitions
  - `tuningManager/*.xml` - Database tuning table configurations
  - `datasetClass/classes.xml` - Dataset class API definitions

- `Model/lib/dst/` - Dataset template files (DST format)
  - Templates use `${propertyName}` syntax for variable substitution
  - Templates are injected into WDK XML anchor files at build time

## WDK XML Schema

WDK model XML files in `Model/lib/wdk/` are **middleware configuration** that define how the WDK (Workflow Development Kit) converts RDBMS data into objects. These XML files act as an ORM-like layer, mapping database queries to object representations.

The schema is defined at: https://github.com/VEuPathDB/WDK/blob/master/Model/lib/rng/wdkModel.rng

### Key XML Elements

The schema defines the root `<wdkModel>` element which can contain:

- **`<modelName>`** - Model identification (displayName, version, releaseDate, buildNumber)
- **`<import>`** - Import external model XML files
- **`<paramSet>`** - Parameter definitions for searches:
  - `stringParam`, `numberParam`, `dateParam`, `enumParam`, `filterParam`, `datasetParam`
  - Parameters can have validation rules and display options (select, checkbox, treeBox, typeAhead)
- **`<querySet>`** - SQL and process queries that retrieve data from RDBMS:
  - Query types: attribute, table, ID, transform, utility, summary, vocabulary
  - Support for complex SQL with multiple columns and nested queries
  - Define the SQL-to-object mapping for data retrieval
- **`<recordClassSet>`** - Object definitions representing data entities (genes, datasets, etc.):
  - **Attributes** - Define 1-to-1 properties (single values per record, mapped from query columns)
  - **Tables** - Model tabular data (1-to-many relationships, collections of related data)
  - Also includes views and primary key definitions
  - Defines the structure of objects created from database data
  - Specifies how data is displayed and accessed in the application
- **`<questionSet>`** - User-facing search interfaces:
  - Links parameters to queries to create complete search workflows
  - Defines search categories and display properties
  - Represents the user's entry point to query the RDBMS through objects
- **Platform-specific configuration** - Uses `includeProjects`/`excludeProjects` attributes for site-specific features

### Middleware Flow: Database → Objects

The WDK XML configuration defines a complete middleware layer:
1. **Queries** (`<querySet>`) - SQL statements that retrieve data from the RDBMS
2. **Records** (`<recordClassSet>`) - Object definitions that structure the query results
3. **Questions** (`<questionSet>`) - User interfaces that execute queries and return record objects
4. **Parameters** (`<paramSet>`) - Input values that filter and customize queries

Example flow: User selects parameters → Question executes query with parameters → Query retrieves RDBMS data → Records structure data as objects → Application displays objects to user

When modifying WDK XML files, ensure they conform to this schema structure. The root element should be `<wdkModel>` and typically imports are used to organize related definitions.

## Architecture Concepts

### Dataset Injection Pattern

1. **DatasetPresenter** (XML) → defines dataset metadata, display properties, contacts, publications
2. **DatasetInjector** (Java) → declares required properties, adds WDK model references, injects templates
3. **Templates** (DST files) → text fragments with property placeholders injected into WDK XML
4. **Template Instances** → tuple of (property values, template) created by injector

Key method flow in DatasetInjector subclasses:
- `getPropertiesDeclaration()` - Declare required properties
- `addModelReferences()` - Add WDK references (questions, tables, etc.) via `addWdkReference()`
- `injectTemplates()` - Inject template instances via `injectTemplate(templateName)`

Example: `GeneOntology.java` adds references to GO search questions and tables.

### User Dataset Type Handlers

Located in `Model/src/main/java/org/apidb/apicommon/model/userdataset/`

User dataset handlers process user-uploaded data:
- `ISASimpleTypeHandler` - ISA (Investigation/Study/Assay) format datasets
- `BiomTypeHandler` - BIOM format microbiome data

Each handler implements:
- `getCompatibility()` - Validate user dataset
- `getInstallInAppDbCommand()` - Generate installation commands for app database

### VDI (Virtual Data Institute) Schema and userDatasetRecord

A new `<recordClass>` named **`userDatasetRecord`** is being created to model the VDI Control Schema, which tracks information about user-uploaded datasets. The database schema is defined at:
https://github.com/VEuPathDB/VdiSchema/blob/main/Main/lib/sql/Postgres/createVdiControlTables.sql

**VDI Schema Overview:**

The VDI schema uses PostgreSQL tables with parameterized naming (`VDI_CONTROL_:VAR1` prefix). Key table groups:

1. **Core Dataset Tables:**
   - `dataset` - Primary metadata (dataset_id, owner, type_name, type_version, category, is_public, accessibility, deleted_status, creation_date)
   - `dataset_meta` - Descriptive information (name, summary, description, program_name, project_name, attribution)
   - `sync_control` - Synchronization timestamps for shares, data, and metadata updates

2. **Installation Tracking:**
   - `dataset_install_message` - Installation status per install_type (status, message, updated timestamp)
   - `dataset_install_activity` - Install process heartbeats to track active installs and detect interruptions

3. **Access Control:**
   - `dataset_visibility` - Maps dataset_id to user_id for owners and accepted share offers
   - `dataset_project` - Associates datasets with project_ids

4. **Rich Metadata Tables:**
   - `dataset_publication` - External IDs (PubMed, DOI), citations, is_primary flag
   - `dataset_contact` - Author/contact information (is_primary, name, email, affiliation, country)
   - `dataset_organism` - Experimental or host organisms (species, strain)
   - `dataset_dependency` - Dependencies with identifiers and versions
   - `dataset_hyperlink` - Related URLs with descriptions
   - `dataset_funding_award` - Funding agency and award numbers
   - `dataset_characteristics` - Study design, participant ages, sample years
   - Categorical tables: `dataset_country`, `dataset_species`, `dataset_disease`, `dataset_sample_type`
   - External identifiers: `dataset_doi`, `dataset_bioproject_id`, `dataset_link`

5. **Convenience View:**
   - `AvailableUserDatasets` - Shows datasets visible to a user that are fully installed and not deleted

**Dataset Lifecycle:**
- `deleted_status`: 0 = Active, 1 = Deleted and Uninstalled, 2 = Deleted but not yet Uninstalled
- `accessibility`: 'public', 'protected', 'private'
- Installation tracked through heartbeat mechanism in `dataset_install_activity`

**Mapping VDI Schema to WDK Record Class:**

The `userDatasetRecord` will model the VDI schema using:

- **Attributes (1-to-1 properties):** Data from `dataset` and `dataset_meta` tables will become record attributes since each dataset has exactly one set of core metadata (e.g., dataset_id, owner, type_name, name, summary, description, is_public, accessibility, creation_date).

- **Tables (1-to-many relationships):** The supporting metadata tables will become WDK tables since a dataset can have multiple of each:
  - Publications (`dataset_publication`) - A dataset can have multiple publications
  - Contacts (`dataset_contact`) - Multiple authors/contacts per dataset
  - Organisms (`dataset_organism`) - Multiple experimental or host organisms
  - Hyperlinks (`dataset_hyperlink`) - Multiple related URLs
  - Countries, species, diseases - Multiple categorical values
  - Dependencies, funding awards, sample types, etc.

This provides an object-oriented interface where the record represents a single user dataset with scalar properties (attributes) and collections of related data (tables).

**Implementation Pattern - Creating UserDatasetRecordClass:**

The implementation should follow the pattern established in `datasetRecords.xml` and `datasetQueries.xml`, creating two new parallel files:

1. **`userDatasetRecords.xml`** - Defines the record class structure:
   - `<recordClass name="UserDatasetRecordClass">` - Main record class definition
   - `<primaryKey>` - References an alias query (likely using `dataset_id` from VDI schema)
   - `<idAttribute>` - Display attribute for the primary key
   - `<reporter>` elements - Standard reporters (attributesTabular, tableTabular, fullRecord, xml, json)
   - `<attributeQueryRef>` elements - Reference queries from userDatasetQueries.xml for 1-to-1 properties:
     - Core dataset metadata (dataset_id, owner, type_name, type_version, category, etc.)
     - Dataset descriptive info (name, summary, description, etc.)
     - Accessibility and status (is_public, accessibility, deleted_status, creation_date)
     - Each `<attributeQueryRef>` contains `<columnAttribute>` elements mapping query columns to attributes
   - `<table>` elements - Reference table queries for 1-to-many relationships:
     - Publications (`<table name="Publications" queryRef="UserDatasetTables.Publications">`)
     - Contacts (`<table name="Contacts" queryRef="UserDatasetTables.Contacts">`)
     - Organisms, Hyperlinks, Countries, Species, etc.
     - Each table contains `<columnAttribute>`, `<linkAttribute>`, or `<textAttribute>` elements

2. **`userDatasetQueries.xml`** - Defines SQL queries to retrieve data from VDI Control Schema:
   - `<querySet name="UserDatasetAttributes" queryType="attribute">` - Attribute queries:
     - SQL queries joining `dataset` and `dataset_meta` tables
     - Queries for sync_control, install status, visibility, etc.
     - Each `<sqlQuery>` has `<column>` elements and `<sql>` with the query
   - `<querySet name="UserDatasetTables" queryType="table">` - Table queries:
     - SQL queries for each supporting metadata table
     - `dataset_publication`, `dataset_contact`, `dataset_organism`, etc.
     - Queries should join on `dataset_id` foreign key

**Key Patterns from DatasetRecordClass:**

- **Attribute Queries**: Join core tables, return one row per dataset_id
  ```xml
  <attributeQueryRef ref="UserDatasetAttributes.CoreMetadata">
    <columnAttribute name="dataset_id" internal="true"/>
    <columnAttribute name="owner" displayName="Owner"/>
    <columnAttribute name="type_name" displayName="Type"/>
    <!-- etc. -->
  </attributeQueryRef>
  ```

- **Table Queries**: Return multiple rows per dataset_id for 1-to-many relationships
  ```xml
  <table name="Publications" displayName="Publications"
         queryRef="UserDatasetTables.Publications">
    <columnAttribute name="dataset_id" internal="true"/>
    <columnAttribute name="pmid" displayName="PubMed ID"/>
    <linkAttribute name="pubmed_link" displayName="Link">
      <!-- link markup -->
    </linkAttribute>
  </table>
  ```

- **Link Attributes**: Use `<linkAttribute>` for URLs, with `<displayText>` and `<url>` child elements
- **Internal Attributes**: Use `internal="true"` for columns needed for joins but not displayed
- **Project Filtering**: Use `includeProjects` / `excludeProjects` attributes for site-specific features

Both files must be imported in `Model/lib/wdk/ebrcModelCommon.xml`:
```xml
<import file="model/records/userDatasetRecords.xml"/>
<import file="model/records/userDatasetQueries.xml"/>
```

### Tuning Manager

The tuning manager (`Model/lib/xml/tuningManager/`) defines database materialized views (tuning tables) that optimize queries. These are built from dataset metadata and external data sources.

Common tuning tables:
- `DatasetPresenter` - Main dataset metadata table
- `StudyIdDatasetId` - Maps EDA study IDs to dataset IDs
- Various record-specific attribute tables

## Dataset Classes

Dataset classes are defined in `Model/lib/xml/datasetClass/classes.xml`. Each class represents a type of data (e.g., RNA-Seq, microarray, proteomics) and specifies:
- Properties available for that class
- How the data integrates with the site
- Display templates and visualization options

## Dependencies

- **WDK (Workflow Development Kit)** - Core dependency providing model infrastructure
- **ReFlow** - Workflow management
- **FgpUtil** - Utility libraries (core, xml, cli, db)
- **Apache Commons** (Digester, CLI, Codec)
- **Jackson** - JSON processing
- **Log4j** - Logging

## Development Workflow

1. To add a new dataset type:
   - Create a `DatasetInjector` subclass in `Model/src/main/java/org/apidb/apicommon/model/datasetInjector/`
   - Implement the three abstract methods
   - Add dataset presenter XML entry in `Model/lib/xml/datasetPresenters/global.xml`
   - Create templates in `Model/lib/dst/` if needed
   - Build and restart Tomcat

2. To modify WDK model definitions:
   - Edit XML files in `Model/lib/wdk/`
   - Rebuild project with `bld EbrcModelCommon`
   - Restart Tomcat instance (changes require reload)

3. To add/modify user dataset handlers:
   - Create/edit handler in `Model/src/main/java/org/apidb/apicommon/model/userdataset/`
   - Implement required UserDatasetTypeHandler methods
   - Register handler in WDK configuration
