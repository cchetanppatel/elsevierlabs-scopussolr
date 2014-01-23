#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/results/ScopusAuthorNavigators.txt"
printf "%s - %d\n" "Record Count" `grep "aut"  $filename | wc -l`
printf "\n\nNavigator Counts\n\n"

for nav in autactivenav autaffilcitynav autaffilctrynav autafidnav autafnameidnav autsrctitle autsubjclus

do
fcount=`grep "$nav" $filename | wc -l`
printf "%s - %d\n" "$nav" "$fcount" 
done


















