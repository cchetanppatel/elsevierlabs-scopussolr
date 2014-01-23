#!/bin/bash

# Create slim version of institution records
# Remove queries contains 'scope' feature.  This is not supported in load testing.
# May want to consider removing 'proximity' queries.
# NOTE:  'scope' and 'proximity' are not an issue within institution queries as
#        none of the institution queries contain these features.

# Dummy values will be provided for the following fields:
#  count (1)
#  fastloaddate (date when document loaded into SOLR)
#  loadunit (ABCDEFGHIJ0123456789)
#  transid (ABCDEFGHIJ0123456789)

# The scert field will not be populated with any values (field is never used in queries).
 
# Remove queries containing a field where we will not have a value.  
# scert*     search=0 sort=0 

# Input File
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusInstitutionXQueryX_Dec052013.txt"

grep -v 'path="scert' $filename > slimInstitution.txt
