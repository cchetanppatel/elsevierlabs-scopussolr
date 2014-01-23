#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/SCOPUS_AUTHOR_FULLTEXTQUERY_10152013.txt"

printf "%s - %d\n" "Record Count" `grep "<" $filename | wc -l` 

printf "\n\nQuery Types\n\n"

for elem in numericCompare wordList word orQuery andQuery anyQuery rankQuery notQuery proximityQuery childProximity scopeQuery
do
ecount=`grep "<ft:$elem" $filename | wc -l` 
fcount=`grep "<$elem" $filename | wc -l` 
tcount=`expr $ecount + $fcount`
printf "%s - %d\n" "$elem" "$tcount"
done

printf "\nWildcards\n\n"
printf "%s - %d\n" "single char wildcard" `grep "?" $filename | wc -l` 
printf "%s - %d\n" "muli char wildcard" `grep "*" $filename | wc -l` 

printf "\n\nWord Attributes\n\n"
printf "%s - %d\n" "stem=false" `grep "stem=\"false\"" $filename | wc -l`
printf "%s - %d\n" "stem=true" `grep "stem=\"true\"" $filename | wc -l`
printf "%s - %d\n" "phrase=false" `grep "phrase=\"false\"" $filename | wc -l`
printf "%s - %d\n" "phrase=true" `grep "phrase=\"true\"" $filename | wc -l`
printf "%s - %d\n" "case=sensitive" `grep "case=\"sensitive\"" $filename | wc -l`
printf "%s - %d\n" "case=insensitive" `grep "case=\"insensitive\"" $filename | wc -l`
printf "%s - %d\n" "punct=sensitive" `grep "punct=\"sensitive\"" $filename | wc -l`
printf "%s - %d\n" "punct=insensitive" `grep "punct=\"insensitive\"" $filename | wc -l`
printf "%s - %d\n" "diacrit=sensitive" `grep "diacrit=\"sensitive\"" $filename | wc -l`
printf "%s - %d\n" "diacrit=insensitive" `grep "diacrit=\"insensitive\"" $filename | wc -l`
printf "%s - %d\n" "equals=false" `grep "equals=\"false\"" $filename | wc -l`
printf "%s - %d\n" "equals=true" `grep "equals=\"true\"" $filename | wc -l`
printf "%s - %d\n" "startsWith=false" `grep "startsWith=\"false\"" $filename | wc -l`
printf "%s - %d\n" "startsWith=true" `grep "startsWith=\"true\"" $filename | wc -l`
printf "%s - %d\n" "endsWith=false" `grep "endsWith=\"false\"" $filename | wc -l`
printf "%s - %d\n" "endsWith=true" `grep "endsWith=\"true\"" $filename | wc -l`
printf "%s - %d\n" "lang" `grep "lang=\"" $filename | wc -l`
printf "%s - %d\n" "weight" `grep "weight=\"" $filename | wc -l`
printf "%s - %d\n" "exactly" `grep "exactly=\"" $filename | wc -l`
printf "%s - %d\n" "atleast" `grep "atleast=\"" $filename | wc -l`

printf "\n\nField Counts\n\n"

for field in active afdispcity afdispctry afdispname affil affilcity affilctry affilcurr affilhist affilname affilnamevar affilparafid affilprefname affilsortname afhistcity afhistctry afhistdispname afhistid afid afnameid alias aliascurstatus aliasauthorid aliastimestamp aliasstatus authfirst authid  authlast authname certscore count datecompletedtxt eid fastloaddate issn loaddate loadunit namevar namevarfirst namevarini namevarlast parafid preffirst preffirstsort prefini preflast prefname prefparname pubrangefirst pubrangelast quality saffil salias scert:cert scert:cert:orgid orgid scert:cert:score score sortname srctitle status subjabbr subjclus subjmain suppress toplevel transid
 
do
fcount=`grep "path=\"$field\"" $filename | wc -l` 
printf "%s - %d\n" "$field" "$fcount" 
done


















