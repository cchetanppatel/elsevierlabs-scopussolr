#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/results/ScopusInstitutionNavigators.txt"
printf "%s - %d\n" "Record Count" `grep "inst"  $filename | wc -l`
printf "\n\nNavigator Counts\n\n"

for nav in instaffilcitynav instaffilctrynav instcertscorenav insttoplevelnav 

do
fcount=`grep "$nav" $filename | wc -l`
printf "%s - %d\n" "$nav" "$fcount" 
done


















