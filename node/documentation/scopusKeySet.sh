#!/bin/bash

keytype="affil"
keyset="1"
cnt="53590"

for (( i=1; i <= $cnt; i++ ))
do
 printf "%s_%s_%s\n" $keytype $keyset $i 
done
















