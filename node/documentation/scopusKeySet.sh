#!/bin/bash

#keytype="affil"
keytype="auth"
keyset="1"
#cnt="53590"
cnt="113894"

for (( i=1; i <= $cnt; i++ ))
do
 printf "%s_%s_%s\n" $keytype $keyset $i 
done
















