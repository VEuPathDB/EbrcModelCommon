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


1;
