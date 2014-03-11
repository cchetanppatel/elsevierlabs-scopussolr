/**
 * 
 *  Author: Darin McBeath
 */
 
 
/**
 * Module dependencies.
 */
var cfg = require('../helpers/config');
var http = require('http');
var app = require('../app');
var async = require('async');


/**
 * getTaskLimit
 */
function getTaskLimit() {
    return cfg.INIT_PARMS['task-limit'];
};


/**
 * getRamdonInt
 * 
 * @param min - min value for random number generation
 * @param max - max value for random number generation
 * 
 * Generate a random number betwee the passed 'min' and 'max' values.
 * Used to randomly select from multiple SOLR endpoints.
 */
function getRandomInt (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
};


/**
 * getFacets
 * 
 * @param cluster - identifies which facets to construct (auth, affil, core)
 * @param limit - maximum number of values to return for the facet
 * 
 * Build up facet query string parameters for the specified 'cluster'.
 */
function getFacets(cluster,limit) {

    var facets = cfg.INIT_PARMS['solr-facets'][cluster];
    
    var str = '&facet=true&facet.mincount=1';
    
    for (var i = 0; i < facets.length; i++) {
        str += '&facet.field=' + facets[i];
        str += '&f.' + facets[i] + '.facet.limit=' + limit
    }
 
    return str;  
    
};


/**
 * Get the 'count' for the specified facet.  This is done by appending the facet field (and value)
 * to the original search query.
 * 
 * @param facetParams{ query, facetName, facetValue, facetCount}
 * @param callback
 */
function getFacetCount(facetParams,callback) {

    // Set the HTTP Headers
    var headers = {
        'Content-Type' : 'application/x-www-form-urlencoded'
    };
  
    var solrHostPort = cfg.INIT_PARMS['solr-host-port'][facetParams.cluster][getRandomInt(0, cfg.INIT_PARMS['solr-host-port'][facetParams.cluster].length - 1)];     
    var solrHostPortToks = solrHostPort.split(':');
    
    // Prepare the HTTP options
    var options = {
        host: solrHostPortToks[0],
        port: solrHostPortToks[1],
        method: 'POST',
        path: '/solr/' + cfg.INIT_PARMS['solr-cluster'][facetParams.cluster] + '/select',
        headers: headers
    };
    
    //console.log(options);

    var chunks = [];
    var solrRspLen;

    var req2 = http.request(options, function(res2) {
                                      
        res2.on('data', function (chunk) {
                  
            chunks.push(chunk);
        });
                            
        res2.on('end', function() {
                   
            var buf = Buffer.concat(chunks);
                       
            if (res2.statusCode == 200) {
                         
                var results = JSON.parse(buf);
                var numFound = results.response.numFound;
                
                var facetResult = {
                            facetName : facetParams.facetName,
	                        facetValue : facetParams.facetValue,
	                        facetCount : facetParams.facetCount,
	                        searchCount : numFound };
                
                //console.log(JSON.stringify(facetResult));

                callback(null,facetResult);
                                   
            } else {
                  
                callback('Problems getting the facet counts for ' + JSON.stringify(facetParams));                             
               
            }

        });
        
    });

    req2.on('error', function(e) {
    
        console.log(e);

        callback('Problems getting the facet counts for ' + JSON.stringify(facetParams));
	           
    });
                 
    // Send the request (currently making the facet value as a phrase because it can contain more than one term)
    req2.write('q=(' + facetParams.query + ') AND ' + facetParams.facetName + ':"' + facetParams.facetValue + '"&wt=json&indent=true&rows=0');
    req2.end();        
    
};


/**
 * addTask (closure)
 * 
 * @param facetParams
 */
function addTask(facetParams) { // factory function to create the queries
    return function (cb) { // doQuery
        getFacetCount(facetParams, cb);
    };
}
 
 
 /**
  * verifyFacetCounts
  * 
  * @param query
  * @param cluster
  * @param limit
  * @param results
  * @param fn - callback 
  */
function verifyFacetCounts(query, cluster, limit, results, fn) {

    // Get the logger
    var logger = app.logger;
    
    var numFound = results.response.numFound;

    // Create an array of functions to execute in parallel to get counts for each facet value
    var tasks = [];
        
    for (var fname in results.facet_counts.facet_fields) {
    
        //console.log(fname);

        for (var i = 0; i < results.facet_counts.facet_fields[fname].length; i=i+2) {
        
            var facetParams =  {
	                         query : query,
	                         cluster : cluster,
	                         facetName : fname,
	                         facetValue : results.facet_counts.facet_fields[fname][i],
	                         facetCount : results.facet_counts.facet_fields[fname][i+1]
            }; 

            tasks.push(addTask(facetParams));
            
       }
    }
  
    async.parallelLimit(tasks,limit,function(err,results) {
        if (err) {
	           
            logger.info('Problems getting facet counts '+ err);
            return fn('Problems getting facet counts');
	               
        } else {
	        
            return fn(null,numFound,results);
	               
        }
    });
     
};


/**
 * Render the facet form
 *
 * @param req - http request
 * @param res - http response
 */ 
exports.form = function(req, res){

    // Get the logger
    var logger = app.logger;
    
    // Go render the search page
    res.status(200)
	   .set('Content-Type', 'text/html; charset=UTF-8')
	   .render('facetTest');
	                      	                        	            
};


/**
 * Submit the facet form
 *
 * @param req - http request
 * @param res - http response
 */ 
exports.submit = function(req, res){

    // Get the logger
    var logger = app.logger;
    
    var query = req.param('query'); 
    var cluster = req.param('cluster');
    var limit = req.param('limit');
      
    // Set the HTTP Headers
    var headers = {
        'Content-Type' : 'application/x-www-form-urlencoded'
    };
  
    var solrHostPort = cfg.INIT_PARMS['solr-host-port'][cluster][getRandomInt(0, cfg.INIT_PARMS['solr-host-port'][cluster].length - 1)];     
    var solrHostPortToks = solrHostPort.split(':');
    
    // Prepare the HTTP options
    var options = {
        host: solrHostPortToks[0],
        port: solrHostPortToks[1],
        method: 'POST',
        path: '/solr/' + cfg.INIT_PARMS['solr-cluster'][cluster] + '/select',
        headers: headers
    };
            
    //console.log(options);

    var chunks = [];
    var solrRspLen;

    var req2 = http.request(options, function(res2) {
                                      
        res2.on('data', function (chunk) {
                  
            chunks.push(chunk);
        });
                            
        res2.on('end', function() {
                   
            var buf = Buffer.concat(chunks);
                       
            if (res2.statusCode == 200) {
                         
                var results = JSON.parse(buf);                  

/*
                res.status(200)
                    .set('Content-Type', 'application/json; charset=UTF-8')
                    .send(results);
*/
                 // Go check the results  
                 verifyFacetCounts(query, cluster, getTaskLimit(), results, function(err, numFound, data) {
                
                    //console.log(data);
                    //console.log(data[0].facetName);
                    //console.log(data.length);
                    if (err) {
                    
                        res.status(500)
                            .set('Content-Type', 'text/html')
                            .send('Problems');  
                            
                    } else {
                    
                        res.status(200)
                            .set('Content-Type', 'text/html; charset=UTF-8')
	                        .render('facetTestResults', { query : query, 
	                                                      cluster : cluster,
	                                                      numFound : numFound,
	                                                      facets : data});
                         
                    }
                    
                });
                                   
            } else {
                  
                var results = JSON.parse(buf);
                
                res.status(res2.statusCode)
                    .set('Content-Type', 'application/json; charset=UTF-8')
                    .send(results);              
               
            }

        });
        
    });

    req2.on('error', function(e) {
    
        console.log(e);

        res.status(500)
            .send();
	           
    });
                 
    // Tack on the facets and send the request
    req2.write('q=' + query + '&wt=json&indent=true&rows=0' + getFacets(cluster,limit));
    req2.end();    

};