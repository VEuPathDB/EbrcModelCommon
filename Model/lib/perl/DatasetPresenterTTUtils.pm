package EbrcModelCommon::Model::DatasetPresenterTTUtils;

use strict;
use warnings;

require Exporter;
our @ISA = qw(Exporter);
our @EXPORT_OK = qw(
  getProjectListFromDb
  getModelProjectBase
  getPresenterFilePaths
  getGitTimestamp
);

=head1 NAME

DatasetPresenterTTUtils - Shared utilities for DatasetPresenter tuning table scripts

=head1 SYNOPSIS

  use DatasetPresenterTTUtils qw(getProjectListFromDb getPresenterFilePaths);

  my @projects = getProjectListFromDb($dbh, $instance, $PROJECT_HOME);
  my @filePaths = getPresenterFilePaths($PROJECT_HOME, \@projects);

=head1 DESCRIPTION

This module provides shared functionality for scripts that work with DatasetPresenter
tuning tables. It encapsulates the logic for discovering projects from the database,
determining which model projects to use, and locating all required resource files.

=head1 FUNCTIONS

=head2 getProjectListFromDb($dbh, $instance, $PROJECT_HOME)

Queries the database to determine which projects are available.
Returns an array of hashrefs, each containing:
  - project_id: The project ID (e.g., "PlasmoDB")
  - lower_project_id: Lowercase version (e.g., "plasmodb")
  - model_project_base: Which model project to use (ApiCommonModel, MicrobiomeModel, ClinEpiModel)

Arguments:
  $dbh - Database handle
  $instance - Database instance string
  $PROJECT_HOME - PROJECT_HOME environment variable

=cut

sub getProjectListFromDb {
  my ($dbh, $instance, $PROJECT_HOME) = @_;

  my $sql;
  if ($instance =~ /\/eda/i) {
    $sql = <<SQL;
    select 'EDA' as project_id, lower('EDA') as lower_project_id
SQL
  } elsif ($PROJECT_HOME =~ /\/eupa/i) {
    $sql = <<SQL;
    SELECT project_id, lower(project_id) as lower_project_id
      FROM (SELECT DISTINCT project_id FROM GenomicSeqAttributes) t1
SQL
  } elsif ($PROJECT_HOME =~ /\/uni/i || $instance =~ /genomics/) {
    $sql = <<SQL;
    SELECT project_id, lower(project_id) as lower_project_id
      FROM (SELECT DISTINCT project_id from webready.GenomicSeqAttributes_p) t1
    UNION
    SELECT 'UniDB' AS project_id, 'unidb' as lower_project_id
SQL
  } else {
    $sql = <<SQL;
    SELECT project_id, lower(project_id) as lower_project_id
      FROM (
        SELECT DISTINCT pi.name as project_id from Core.ProjectInfo pi, SRes.ExternalDatabase ed
        WHERE ed.row_project_id = pi.project_id
      ) t1
SQL
  }

  my $projectQuery = $dbh->prepare($sql);
  $projectQuery->execute() or die "Error getting list of project IDs";

  my @projects;
  while (my ($project_id, $lower_project_id) = $projectQuery->fetchrow_array()) {
    my $model_project_base = getModelProjectBase($lower_project_id);
    push @projects, {
      project_id => $project_id,
      lower_project_id => $lower_project_id,
      model_project_base => $model_project_base,
    };
  }

  $projectQuery->finish();

  return @projects;
}

=head2 getModelProjectBase($lower_project_id)

Determines which model project base to use for a given project.

Arguments:
  $lower_project_id - Lowercase project ID

Returns:
  String: "MicrobiomeModel", "ClinEpiModel", or "ApiCommonModel"

=cut

sub getModelProjectBase {
  my ($lower_project_id) = @_;

  if ($lower_project_id =~ /eda/ || $lower_project_id =~ /microbiome/) {
    return "MicrobiomeModel";
  } elsif ($lower_project_id =~ /clinepidb/) {
    return "ClinEpiModel";
  } else {
    return "ApiCommonModel";
  }
}

=head2 getPresenterFilePaths($PROJECT_HOME, \@projects)

Returns an array of all resource file paths that would be read by the
buildDatasetPresentersTT script.

Arguments:
  $PROJECT_HOME - PROJECT_HOME environment variable
  \@projects - Array reference of project hashrefs from getProjectListFromDb()

Returns:
  Array of file paths in the order they would be accessed

=cut

sub getPresenterFilePaths {
  my ($PROJECT_HOME, $projects_ref) = @_;

  my @filePaths;
  my $primary_model_base;

  # Add project-specific XML files
  # Uses "last one wins" logic from original script for determining model base
  foreach my $project (@$projects_ref) {
    my $project_id = $project->{project_id};
    my $lower_project_id = $project->{lower_project_id};
    my $model_project_base = $project->{model_project_base};

    if ($lower_project_id =~ /eda/) {
      # EDA databases include all three project types
      push @filePaths, "$PROJECT_HOME/MicrobiomePresenters/Model/lib/xml/datasetPresenters/MicrobiomeDB.xml";
      push @filePaths, "$PROJECT_HOME/ClinEpiPresenters/Model/lib/xml/datasetPresenters/ClinEpiDB.xml";
      push @filePaths, "$PROJECT_HOME/ApiCommonPresenters/Model/lib/xml/datasetPresenters/VectorBase.xml";
      $primary_model_base = $model_project_base;
    } elsif ($lower_project_id =~ /microbiome/) {
      push @filePaths, "$PROJECT_HOME/MicrobiomePresenters/Model/lib/xml/datasetPresenters/${project_id}.xml";
      $primary_model_base = $model_project_base;
    } elsif ($lower_project_id =~ /clinepidb/) {
      push @filePaths, "$PROJECT_HOME/ClinEpiPresenters/Model/lib/xml/datasetPresenters/${project_id}.xml";
      $primary_model_base = $model_project_base;
    } else {
      push @filePaths, "$PROJECT_HOME/ApiCommonPresenters/Model/lib/xml/datasetPresenters/${project_id}.xml";
      $primary_model_base = $model_project_base;
    }
  }

  # Add shared resource files
  push @filePaths, "$PROJECT_HOME/EbrcModelCommon/Model/lib/xml/datasetPresenters/global.xml";
  push @filePaths, "$PROJECT_HOME/EbrcModelCommon/DatasetPresenter/lib/xml/datasetPresenters/datasetPresenters.dtd";
  push @filePaths, "$PROJECT_HOME/$primary_model_base/Model/config/datasetReferences.tab";
  push @filePaths, "$PROJECT_HOME/$primary_model_base/Model/config/datasetLinks.xml";
  push @filePaths, "$PROJECT_HOME/EbrcModelCommon/Model/lib/xml/datasetPresenters/contacts/allContacts.xml";

  return @filePaths;
}

=head2 getGitTimestamp($filePath, $PROJECT_HOME)

Gets the Unix timestamp of the last git commit that modified a file.
Dies if the file is not tracked in git.

Arguments:
  $filePath - Full path to the file
  $PROJECT_HOME - PROJECT_HOME environment variable

Returns:
  Unix timestamp (integer) of the last commit that modified this file

Dies:
  If the file is not tracked in git

=cut

sub getGitTimestamp {
  my ($filePath, $PROJECT_HOME) = @_;

  # Extract repository root (first directory after PROJECT_HOME)
  my $relativePath = $filePath;
  $relativePath =~ s/^\Q$PROJECT_HOME\E\///;
  my ($repoName) = split('/', $relativePath);
  my $repoPath = "$PROJECT_HOME/$repoName";
  my $fileRelativeToRepo = $relativePath;
  $fileRelativeToRepo =~ s/^\Q$repoName\E\///;

  # Get the last commit timestamp from git (run from repo root)
  my $gitTimestamp = `cd "$repoPath" && git log -1 --format=%ct -- "$fileRelativeToRepo" 2>/dev/null`;
  chomp($gitTimestamp);

  # Error if file is not tracked in git
  unless ($gitTimestamp) {
    die "Error: File is not tracked in git: $filePath\n";
  }

  return $gitTimestamp;
}

1;

__END__

=head1 AUTHOR

VEuPathDB

=head1 SEE ALSO

buildDatasetPresentersTT, needsUpdateDatasetPresentersTT

=cut
