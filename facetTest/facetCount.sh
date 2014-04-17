#!/bin/bash

urlencode() {
    local l=${#1}
    for (( i = 0 ; i < l ; i++ )); do
        local c=${1:i:1}
        case "$c" in
            [a-zA-Z0-9.~_-]) printf "$c" ;;
            *) printf '%%%X' "'$c"
        esac
    done
}

cnt=0
while read LINE
do
	ENCODEDQUERY="$(urlencode "$LINE")"
	MSG="cluster=core&limit=10&query=$ENCODEDQUERY"
	curl -o counts/facet$cnt.html 'localhost:81/facetTest/' -d $MSG 
	cnt=$((cnt+1))
done < /Users/mcbeathd/github/scopusSolr/facetTest/facetQueryCountSet.txt
