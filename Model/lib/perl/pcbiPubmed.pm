#! /usr/bin/perl

package EbrcModelCommon::Model::pcbiPubmed;
require Exporter;
@ISA = qw (Exporter);
@EXPORT = qw (
            setPubmedID,
            fetchAuthorList,
            fetchPublication,
            fetchPubmedUrl
            );

use strict;
use LWP::Simple;
use EbrcModelCommon::Model::XMLUtils;
use Encode;
use HTML::Entities ();

my $ncbiEutilsUrl = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"
                    . "api_key=f2006d7a9fa4e92b2931d964bb75ada85a08&db=pubmed&retmode=xml&rettype=abstract&id=";

my $publicationPubmedUrl = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?"
                            . "cmd=Retrieve&amp;db=PubMed&amp;list_uids=";

my ($id, $content);

sub setPubmedID {
  my ($pubmed_id) = @_;
  return unless $pubmed_id;
  $id = $pubmed_id;
  my $raw_content = LWP::Simple::get ($ncbiEutilsUrl . $pubmed_id);
  return -1 unless $raw_content;    # if HTTP failed

  # Some versions of LWP::Simple (5.827) use $resp->decoded_content , others (1.41)
  # use $resp->content . Encode accordingly.
  $content = (utf8::is_utf8($raw_content)) ? encode('UTF-8', $raw_content) : $raw_content;
}

sub fetchPubmedUrl {
    return $publicationPubmedUrl . $id;
}

sub extractAndDecodeTagContent {
	my ($source, $tag) = @_;

	my $tagContent = EbrcModelCommon::Model::XMLUtils::extractTagContent($source, $tag);

	# If the extracted tag content does not contain any '<' characters, then there
	# are no child XML nodes, and the text can be safely run through an HTML
	# entities decoder to unescape any special characters (such as `&nbsp;`).
	#
	# Regex uses a negative lookahead `(?!<)` to verify the text contains no '<'
	# characters.
	if ($tagContent =~ /^(?!<).+$/) {
		return HTML::Entities::decode($tagContent);
	}

	return $tagContent;
}

sub fetchAuthorList {
	my @authors;
	my $aContent = EbrcModelCommon::Model::XMLUtils::extractTagContent ($content, "AuthorList");

	foreach my $author (EbrcModelCommon::Model::XMLUtils::extractAllTags ($aContent, "Author")) {
		my $attrValue = EbrcModelCommon::Model::XMLUtils::getAttrValue ($author, "Author", "ValidYN");
		#Some of them don't have this attribute.
	    if (!$attrValue || $attrValue eq "Y") {
			my $lastname = extractAndDecodeTagContent($author, "LastName");
			return $lastname? "$lastname et al." : "";
	    }
	}
        return "";
}

sub fetchAuthorListLong {
	my @authors;
	my $aContent = EbrcModelCommon::Model::XMLUtils::extractTagContent ($content, "AuthorList");

	foreach my $author (EbrcModelCommon::Model::XMLUtils::extractAllTags ($aContent, "Author")) {
		my $attrValue = EbrcModelCommon::Model::XMLUtils::getAttrValue ($author, "Author", "ValidYN");
		#Some of them don't have this attribute.
	    if (!$attrValue || $attrValue eq "Y") {
			my $lastname = EbrcModelCommon::Model::XMLUtils::extractTagContent ($author, "LastName");
	        my $initials = EbrcModelCommon::Model::XMLUtils::extractTagContent ($author, "Initials");
	        push @authors, "$lastname $initials";
	    }
	}

	return join (", ", @authors);
}

sub fetchTitle {
    my $title = extractAndDecodeTagContent($content, "ArticleTitle");
	return $title;
}

sub fetchPublication {
	my $publication = EbrcModelCommon::Model::XMLUtils::extractTag ($content, "Journal");
	my ($pubName, $pubVolume, $pubIssue, $pubDate, $pubPages);

	# The name of the journal can come from one of the three sources
	#	1. ISOAbbreviation
	#	2. Title
	#	3. MedlineTA
	# Use the same order of preference in obtaining a name.
	# Don't know what's the difference between Title and MedlineTA, but
	# one of them didn't have Title, but had MedlineTA.

	if (!($pubName = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "ISOAbbreviation"))) {
		if (!($pubName = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Title"))) {
			$pubName = EbrcModelCommon::Model::XMLUtils::extractTagContent ($content, "MedlineTA");
		}
	}

	# Publication Date can have three forms:
	#   1. Year and Month, and optionally Day.
	#   2. Year and Season
	#   3. MedlineDate

	my ($pubYear, $pubSeason, $pubMonth, $pubDay, $pubMedlineDate);
	if ($pubYear = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Year")) {
	    if ($pubMonth = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Month")) {
			if ($pubDay = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Day")) {
		        $pubDate = "$pubYear $pubMonth $pubDay";
			} else {
				$pubDate = "$pubYear $pubMonth";
			}
	    } else {
	        $pubDate = "$pubYear "
	                    . EbrcModelCommon::Model::XMLUtils::extractTagContent($publication, "Season");
	    }
	} else {
	    $pubDate = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "MedlineDate");
	}

	$pubVolume = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Volume");
	$pubIssue = EbrcModelCommon::Model::XMLUtils::extractTagContent ($publication, "Issue");

	# Pagination can have two forms:
	#   1. MedlinePgn - indicates both start and end
	#   2. StartPage, and optionally EndPage
	my $pages = EbrcModelCommon::Model::XMLUtils::extractTagContent ($content, "Pagination");
	$pubPages = EbrcModelCommon::Model::XMLUtils::extractTagContent ($pages, "MedlinePgn")
	    or $pubPages = EbrcModelCommon::Model::XMLUtils::extractTagContent ($pages, "StartPage")
	                . "-"
	                . EbrcModelCommon::Model::XMLUtils::extractTagContent ($pages, "EndPage");

	return $pubName? "$pubName $pubDate;$pubVolume($pubIssue):$pubPages" : "";
}

1;

