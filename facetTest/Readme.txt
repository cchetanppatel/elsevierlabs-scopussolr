facetTest.sh
Ran the 50 queries identified in facetQuerySet.txt.
Modifed the query set to change facet limit to 3, 10, 20, and 200.
Restarted Solr between each run to clear caches.
Captured results into facet3, facet10, facet20, and facet200 folder.
Compared results using 'node facetCompare.js'.  The javascript needed
to be adjusted based on what file we were comparing.  
All of the files matched perfectly.

facetCount.sh
Ran the 50 queries identified in facetQueryCountSet.txt.  These are the
same queries in the facetQuerySet.txt.
Each query gets the value for the top 10 facets and appends this to the original
query to compare the results (to make sure they are the same).
Used the ScopusSolr node app (facetTest).
Output was routed to counts.
There were no issues.
