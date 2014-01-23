Queries were extracted from Nov 19 through Dec 3 from one instance over a 24hour period each day.

ScopusAuthorXQueryX_Dec052013.txt
 - 299,084 queries
 - 176,547 only query on authid

ScopusInstitutionXQueryX_Dec052013.txt
 - 91,548 queries
 - 90,508 queries only contain afid

ScopusMainXQueryX_Dec052013.txt
 - 1,711,315 queries
 - 143,194 only contain doi
 - 246,847 only contain eid
 - 97,661 only contain sdeid
 - 169,473 only contain refeid
 - 381,434 only contain authid
 Total 1,038,608


 - 32 invalid records in main (will not parse)


Potential Navigators in Scopus

 aucite 
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:surname 
	OR
	/xoe:/enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/ce:initials

 affilctry
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/country

 afid
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/@afid

 authgrpid
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/@auid    
	AND   
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/affiliation/@afid

 authid
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/@auid

 exactkeyword (created by FAST)

 exactsrctitle
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/source/sourcetitle

 lang
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/citation-title/titletext/@xml:lang

 prefnameauid
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/preferred-name/ce:surname 
	AND 
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/author-group/author/preferred-name/ce:initials 
	AND 
	/xoe:enhanced-document//xocs:doc/xocs:item/item/bibrecord/head/author-group/author/@auid

 pubyr
	/xoe:enhanced-document/xocs:doc/xocs:item/item/ait:process-info/ait:date-sort/@year

 srctype
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/source/@type

 statustype
	/xoe:enhanced-document/xocs:doc/xocs:item/item/ait:process-info/ait:status/@type

 subjabbr
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/enhancement/classifictiongroup/classifications[@type='SUBJABBR']/classification

 subtype
	/xoe:enhanced-document/xocs:doc/xocs:item/item/bibrecord/head/citation-info/citation-type/@code


