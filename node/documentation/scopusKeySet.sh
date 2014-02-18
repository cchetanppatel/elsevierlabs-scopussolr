#!/bin/bash

# Core type
keytype="core"
# Affiliation type
#keytype="affil"
# Author Type
#keytype="auth"

keyset="1"

# Core Count
cnt="1622399"
# Affiliation Count
#cnt="91533"
# Author Count
#cnt="289656"

for (( i=1; i <= $cnt; i++ ))
do
 printf "%s_%s_%s\n" $keytype $keyset $i 
done
















