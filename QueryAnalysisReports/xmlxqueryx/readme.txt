Files in  /Users/mcbeathd/ScopusSearchQueries/xmlxqueryx

SCOPUS_INSTITUTION_FULLTEXTQUERY_10152013.txt
 - 53,590 queries
 - Able to create queries for 53,588 (2 would not fit within the 64kb limit of DynamoDB)

SCOPUS_AUTHOR_FULLTEXTQUERY_10152013.txt
 - 118, 294 queries

SCOPUS_SLIM_AUTHOR_FULLTEXTQUERY_10152013.txt
 - 113,894 queries
 - Dropped affiliation related fields (and a couple others)  as they don't exist in our records
 - This excludes afdispcity, afdispctry, afdispname, affilname, affilsortname, afhistdispname, afnameid, saffil, subjabbr
 - Observed a bug in the current system as there were no 'scopeQuery' requests for saffil (there should have been about 3356)
 - The following queries were not inserted into DynamoDB (likely due to 64kb limit)
   Problems with auth_1_9254 
   Problems with auth_1_30296
   Problems with auth_1_31844
   Problems with auth_1_31866
   Problems with auth_1_41273
   Problems with auth_1_41298

SCOPUS_MAIN_FULLTEXTQUERY_10152013.txt
  - 707,858 queries
  - TODO … need to create a subset excluding fields that will not exist

Estimate for the number of 'core' and 'dummy' searches
 grep 'path="statustype">core' SCOPUS_MAIN_FULLTEXTQUERY_10152013.txt | wc -l … 419043
 grep 'path="statustype">dummy' SCOPUS_MAIN_FULLTEXTQUERY_10152013.txt | wc -l … 624
 Is the default statustype 'core'?

Files in /Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/results

ScopusInstitutionFieldsAnalysis.txt
  - Detailed analysis of fields and query types/attributes for institution queries

ScopusAffiliationAfid.txt
  - Queries (extracted from ScopusInstitutionFieldsAnalysis.txt) that contain only query the field afid
  - 53,183 queries

ScopusAuthorFieldsAnalysis.txt
  - Detailed analysis of fields and query types/attributes for author queries

ScopusAuthorAuid.txt
  - Queries (extracted from ScopusAuthorFieldsAnalysis.txt) that contain only query the field authid
  - 63,841 queries

ScopusAuthorStartsEquals.txt
  - Analysis of queries (extracted from ScopusAuthorFieldsAnalysis.txt) that use starts-with and/or equals functionality

ScopusMainFieldsAnalysis.txt
  - Detailed analysis of fields and query types/attributes for the core queries

ScopusMainPunctSensitive.txt
  - Analysis of queries (extracted from ScopusMainFieldsAnalysis.txt) that use punctuation sensitive functionality

ScopusMainScopeAnalysis.txt
  - Analysis of queries (extracted from ScopusMainFieldsAnalysis.txt) that use scope query functionality

ScopusMainProximityAnalysis.txt
  - Analysis of queries (extracted from ScopusMainFieldsAnalysis.txt) that use proximity query functionality
  - Observed that queries (in xqueryx) do not match the supplied xqueryx schema
  - Appear to be situations of 'nested' proximity





