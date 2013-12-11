#!/bin/bash

# Load Test Driver
# Keys are in set/affilKeys or set/authKeys

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
    if [ "$cnt" -lt 100000 ];
    then
        ENCODEDQUERY="$(urlencode "$LINE")"
        MSG="query="$ENCODEDQUERY
        curl -s 'localhost:81/loadTest/' -d $MSG >> /dev/null
    fi
    cnt=$((cnt+1))
done < set/affilKeys