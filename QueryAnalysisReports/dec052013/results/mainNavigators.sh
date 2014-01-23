#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/results/ScopusMainNavigators.txt"
printf "%s - %d\n" "Record Count" `grep "sco"  $filename | wc -l`
printf "\n\nNavigator Counts\n\n"

for nav in scoaucitenav scoaffilctrynav scoafidnav scoauthgrpidnav scoauthidnav scoexactkeywordsnav scoexactsrctitlenav scolangnav scoprefnameauidnav scopubyrnav scosrctypenav scostatustypenav scosubjabbrnav scosubtypenav scoundefinednav

do
fcount=`grep "$nav" $filename | wc -l`
printf "%s - %d\n" "$nav" "$fcount" 
done


















