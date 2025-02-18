#!/usr/bin/perl

use strict;

use lib "$ENV{GUS_HOME}/lib/perl";

use XML::Simple;

use File::Basename;

use DBI;

use Getopt::Long qw(GetOptions);

use Data::Dumper;

use Spreadsheet::ParseExcel;

use EbrcModelCommon::Model::tmUtils;

my ($help, $propfile, $instance, $schema, $suffix, $prefix, $filterValue, $debug);

GetOptions("propfile=s" => \$propfile,
           "instance=s" => \$instance,
           "schema=s" => \$schema,
           "suffix=s" => \$suffix,
           "prefix=s" => \$prefix,
           "filterValue=s" => \$filterValue,
           "debug!" => \$debug,
           "help|h" => \$help,
          );

die "required parameter missing" unless ($propfile && $instance && $suffix);

my $dbh = EbrcModelCommon::Model::tmUtils::getDbHandle($instance, $schema, $propfile);
my $tabFileLocation = "$ENV{PROJECT_HOME}/EbrcModelCommon/Model/data/EDAGeneGraphs.xlsx";

&run();

sub run{

  unless (-e $tabFileLocation)  {
    die "Tab file :$tabFileLocation does not exist";
  }

  if($help) {
    &usage();
  }

  my $failures = 0;

  # parse Tab file

  my $insertStatement = "INSERT INTO EdaGeneGraph$suffix(dataset_name, plot_name, plot_type, x_axix_entity_id, y_axis_entity_id, x_axis_variable_id, y_axis_variable_id, x_min, x_max, y_min, y_max) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
  my $insertRow = $dbh->prepare($insertStatement);
  createEmptyTable($dbh,$suffix);
  if ($tabFileLocation =~ /\.xls$/ || $tabFileLocation =~ /\.xlsx$/) {
    eval {
      require Spreadsheet::ParseExcel;
    };
    if($@) {
      die "Spreadsheet::ParseExcel is a required package when loading from a .xls file";
    }
    else {
      my $oExcel = new Spreadsheet::ParseExcel;
      my $oBook = $oExcel->Parse($tabFileLocation);

      for(my $iSheet=0; $iSheet < $oBook->{SheetCount} ; $iSheet++) {
        my $oSheet = $oBook->{Worksheet}[$iSheet];
        my $minRow = $oSheet->{MinRow};
        my $maxRow = $oSheet->{MaxRow};
        my $minCol = $oSheet->{MinCol};
        my $maxCol = $oSheet->{MaxCol};
        if($maxRow) {
          for(my $i = $minRow; $i <= $maxRow; $i++) {
            my @row;
            for(my $j = $minCol; $j <= $maxCol; $j++) {
              my $oCell = $oSheet->{Cells}[$i][$j];
              my $cellValue = $oCell ? $oCell->Value : undef;
              $cellValue =~ s/["\']//g;
              push(@row, $cellValue);
            }

            # skip comments / header
	    next if($ext_db_name =~ /^#/);

	    if ($ext_db_name) {
	      $insertRow->execute(@row);
	    }
          }
        }
      }
    }
  }

  $dbh->commit();
  $dbh->disconnect();
}

sub createEmptyTable {
     my ($dbh, $suffix) = @_;

    $dbh->do(<<SQL) or die "creating table";
     create table EdaGeneGraph$suffix (
       dataset_name varchar(255),
       plot_name    varchar(255),
       plot_type    varchar(100),
       x_axis_entity_id       varchar(255),
       y_axis_entity_id       varchar(255),
       x_axis_variable_id       varchar(255),
       y_axis_variable_id       varchar(255),
       x_min                    number(5),
       x_max                    number(5),
       y_min                    number(5),
       y_max                    number(5)
  ) nologging
SQL
$dbh->{PrintError} = 0;

    $dbh->do(<<SQL) or die "creating index";
     create index egg_dsn_ix$suffix
       on EdaGeneGraph$suffix (dataset_name)
       tablespace indx
SQL
$dbh->{PrintError} = 0;
}

sub usage {
  my $e = shift;
  if($e) {
    print STDERR $e . "\n";
  }
  print STDERR "usage:  buildEdaGeneGraphTT.pl -instance <instance> -propfile <file> -suffix <NNNN> [ -schema <login> ] [ -debug ] [ -help ] \n";
  exit;
}

1;
