#!/bin/bash

# Core type
#keytype="core"
# Affiliation type
#keytype="affil"
# Author Type
keytype="auth"

keyset="auidafid"

#  Count
cnt="52221"

for (( i=1; i <= $cnt; i++ ))
do
 printf "%s_%s_%s\n" $keytype $keyset $i 
done
















