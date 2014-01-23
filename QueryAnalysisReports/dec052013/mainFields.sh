#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusMainXQueryX_Dec052013.txt"
printf "%s - %d\n" "Record Count" `grep "<"  $filename | wc -l`

printf "\n\nQuery Types\n\n"

for elem in numericCompare wordList word orQuery andQuery anyQuery rankQuery notQuery proximityQuery childProximity scopeQuery
do
ecount=`grep "<ft:$elem"  $filename | wc -l`
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

for field in abs absavail abslang affil affilcity affilctry affilorg afid all allmed allsmall artnum aucite auth auth.name authemail authfirstini authgrpid authid authidxname authkeywords authlast authsuff casregistrynum chem cheminfo chemname citeid coden collab collec collecid confall confcode confloc confname confsponsor copyright corres datecompletedtxt dateloaded daterevised datesort db dbdocid doi dptid dummycode dummylink ed ed.name edaddress edfirstini edidxname edition edlast eid eissn exactkeyword exactsrctitle fastloaddate firstauth fundacr fundall fundno fundsponsor groupid idxterms idxtype intid isbn issn issnp issue itemtitle keywords lang langitemtitle langreftitle loadnum loadunit manuf numcitedby numpatcites numwebcites oeid pagecount part pateid patinfo pg pgfirst pginfo pglast pii pmid prefnameauid pub pubdatetxt pubyr ref refartnum refauid refauth refauthfirstini refauthlast refcount refeid refeidnss refpg refpgfirst refpubyr refpubyrtxt refscp refsrctitle reftitle relauthid restrictedaccess saffil:affil sauth:auth sdeid sdfullavail sed:ed seqbank seqnumber sfundall:fundall srcid srctitle srctitleabbr srctype sref:ref sref:ref:refauth sref:ref:refauth:refauthfirstini sref:ref:refauth:refauthlast statusstage statusstate statustype subjabbr subjmain subjterms subjtype subtype supplement tradenames transid type vol webeid website 
 
do
fcount=`grep "path=\"$field\"" $filename | wc -l` 
printf "%s - %d\n" "$field" "$fcount" 
done


















