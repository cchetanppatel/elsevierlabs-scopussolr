#!/bin/bash
filename="/Users/mcbeathd/ScopusSearchQueries/dec052013/results/ScopusMainReturnFields.txt"
printf "%s - %d\n" "Record Count" `wc -l $filename`

for field in abs absavail abslang affil affilcity affilctry affilorg afid all allmed allsmall artnum aucite auth authemail authfirstini authgrpid authid authidxname authkeywords authlast authsuff casregistrynum chem cheminfo chemname citeid coden collab collec collecid confall confcode confloc confname confsponsor copyright corres datecompletedtxt dateloaded daterevised datesort db dbdocid doi dptid dummycode dummylink ed edaddress edfirstini edidxname edition edlast eid eissn exactkeyword exactsrctitle fastloaddate firstauth fundacr fundall fundno fundsponsor groupid idxterms idxtype intid isbn issn issnp issue itemtitle keywords lang langitemtitle langreftitle loadnum loadunit manuf numcitedby numpatcites numwebcites oeid pagecount part pateid patinfo pg pgfirst pginfo pglast pii pmid prefnameauid pub pubdatetxt pubyr ref refartnum refauid refauth refauthfirstini refauthlast refcount refeid refeidnss refpg refpgfirst refpubyr refpubyrtxt refscp refsrctitle reftitle relauthid restrictedaccess saffil:affil sauth:auth sdeid sdfullavail sed:ed seqbank seqnumber sfundall:fundall relevancy srcid srctitle srctitleabbr srctype sref:ref sref:ref:refauth sref:ref:refauth:refauthfirstini sref:ref:refauth:refauthlast statusstage statusstate statustype subjabbr subjmain subjterms subjtype subtype supplement tradenames transid type vol webeid website 
 
do
fcount=`grep " $field " $filename | wc -l` 
printf "%s - %d\n" "$field" "$fcount" 
done


















