#!/bin/bash

filename="/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/SCOPUS_AUTHOR_FULLTEXTQUERY_10152013.txt"

# Queries that reference these fields must be removed
# afdispcity
# afdispctry
# afdispname
# affilname
# affilsortname
# afhistdispname
# afnameid
# saffil
# subjabbr

grep -v 'path="afdispcity"\|path="afdispctry"\|path="afdispname"\|path="affilname"\|path="affilsortname"\|path="afhistdispname"\|path="afnameid"\|path="saffil"\|path="subjabbr"' $filename > newAuthor.txt

