#!/bin/bash

# Create slim version of author records
# Remove queries contains 'scope' feature.  This is not supported in load testing.
# May want to consider removing 'proximity' queries.
# NOTE:  'scope' and 'proximity' are not an issue within author queries as
#        none of the author queries contain these features.
#        Affiliation should have been a 'scope' query but there is a bug in Scopus.

# Dummy values will be provided for the following fields:
#  active (1)
#  count (10)
#  loadunit (ABCDEFGHIJ0123456789) 
#  transid (ABCDEFGHIJ0123456789)

# Remove queries containing a field where we will not have a value.  This includes the following fields:
#  afdispcity          search=0 sort=21
#  afdispctry          search=0 sort=68
#  afdispname          search=348 sort=0
#  affil               search=0 sort=0
#  affilcity           search=0 sort=0
#  affilctry           search=0 sort=0
#  affilcurr           search=0 sort=0
#  affilhist           search=0 sort=0
#  affilname           search=303 sort=0
#  affilnamevar        search=0 sort=0
#  affilparafid        search=0 sort=0
#  affilprefname       search=0 sort=0
#  affilsortname       search=0 sort=37
#  afhistcity          search=0 sort=0
#  afhistctry          search=0 sort=0
#  afhistdispname      search=259 sort=0
#  afnameid            search=473 sort=0
#  saffil*             search=7710 sort=0
#  salias*             search=0 sort=0
#  subjclus            search=0 sort=0
 
# Input File
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusAuthorXQueryX_Dec052013.txt"

grep -v 'path="afdispcity"\|path="afdispctry"\|path="afdispname"\|path="affil"\|path="affilcity"\|path="affilctry"\|path="affilcurr"\|path="affilhist"\|path="affilname"\|path="affilnamevar"\|path="affilparafid"\|path="affilprefname"\|path="affilsortname"\|path="afhistcity"\|path="afhistctry"\|path="afhistdispname"\|path="afnameid"\|path="saffil\|path="salias\|path="subjclus"' $filename > slimAuthor.txt

