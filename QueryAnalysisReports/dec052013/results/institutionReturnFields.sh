#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/results/ScopusInstitutionReturnFields.txt"

printf "%s - %d\n" "Record Count" `wc -l $filename` 

printf "\n\nField Counts\n\n"

for field in active afdispcity afdispctry afdispname affil affilcity affilctry affilcurr affilhist affilname affilnamevar affilparafid affilprefname affilsortname afhistcity afhistctry afhistdispname afhistid afid afnameid alias aliascurstatus aliasauthorid aliastimestamp aliasstatus authfirst authid  authlast authname certscore count datecompletedtxt eid fastloaddate issn loaddate loadunit namevar namevarfirst namevarini namevarlast parafid preffirst preffirstsort prefini preflast prefname prefparname pubrangefirst pubrangelast quality relevancy saffil salias scert:cert scert:cert:orgid orgid scert:cert:score score sortname srctitle status subjabbr subjclus subjmain suppress toplevel transid
 
do
fcount=`grep " $field " $filename | wc -l` 
printf "%s - %d\n" "$field" "$fcount" 
done


















