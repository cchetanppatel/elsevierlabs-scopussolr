#!/bin/bash

# Create slim version of main records
# Remove queries contains 'scope' feature (46,190).  This is not supported in load testing.
# May want to consider removing 'proximity' queries (3,252).

# Dummy values will be provided for the following fields:
#  collec
#  loadunit
#  transid

# Remove queries containing a field where we will not have a value.  This includes the following fields:
#  auth.name      search=0 sort=0
#  cheminfo       search=0 sort=0          
#  citeid         search=31,746 sort=0          
#  ed.name        search=0 sort=0
#  pateid         search=0 sort=0
#  refeidnss      search=459 sort=0
#  saffil*        search=5,764 sort=0
#  sauth*         search=41,117 sort=0
#  sed*           search=0 sort=0
#  sfundall*      search=11 sort=0
#  sref*          sref=1,166 sort=0

# We still need to investigate the following fields
#  fastloaddate
#  refauid
#  refeid (can we use cto:refeid)
#  sdeid

# Input File
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusMainXQueryX_Dec052013

grep -v 'path="auth.name"\|path="cheminfo"\|path="citeid"\|path="ed.name"\|path="pateid"\|path="refeidnss"\|path="saffil\|path="sauth\|path="sed\|path="sfundall\|path="sref' $filename > slimMain.txt

