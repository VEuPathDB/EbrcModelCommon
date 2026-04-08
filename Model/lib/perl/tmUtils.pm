package EbrcModelCommon::Model::tmUtils;

use strict;
use DBI;
use XML::Simple;

sub parsePropfile {
  my ($propFile) = @_;
  my $props;

  my $simple = XML::Simple->new();
  $props = $simple->XMLin($propFile);

  return $props;
}

sub getDbLoginInfo {
  my ($instance, $schema, $propfile) = @_;

  my $props = parsePropfile($propfile);
  die "no password supplied in propfile $propfile" unless $props->{password};

  $schema = $props->{schema}
    if (! $schema);

  $schema = "ApidbTuning"
    if (! $schema);

  return ($instance, $schema, $props->{username}, $props->{password});
}


sub getDbHandle {
  my ($instance, $schema, $propfile) = @_;

  my ($instance, $schema, $username, $password) = getDbLoginInfo($instance, $schema, $propfile);

  my $dsn = "dbi:Pg:" . $instance;
  my $dbh = DBI->connect(
                $dsn,
                $username,
                $password,
                { PrintError => 1, RaiseError => 0}
                ) or die "Can't connect to the database: $DBI::errstr\n";
  $dbh->{LongReadLen} = 1000000;
  $dbh->{LongTruncOk} = 1;
  $dbh->{RaiseError} = 1;
  $dbh->{AutoCommit} = 0;

  $dbh->do("SET search_path TO $schema") or die ("This doesn't quite work");
  $dbh->commit();

  return $dbh;
}

sub getGitTimestamp {
  my ($filePath, $PROJECT_HOME) = @_;

  $PROJECT_HOME =~ s|/+$||;  # strip trailing /

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
