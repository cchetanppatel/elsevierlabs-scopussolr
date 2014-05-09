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
var ddb = require('../helpers/ddb');
var async = require('async');


/**
 * getTaskLimit
 */
function getTaskLimit() {
    return cfg.INIT_PARMS['task-limit'];
};


// Round robin
function getRandomInt (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


/**
 * Get the cto counts (pubyr navigator) for the specified query
 * 
 * @param ctoParams{ query, cluster}
 * @param callback
 */
function getCtoCount(ctoParams,callback) {

    // Set the HTTP Headers
    var headers = {
        'Content-Type' : 'application/x-www-form-urlencoded'
    };
  
    var solrHostPort = cfg.INIT_PARMS['solr-host-port'][ctoParams.cluster][getRandomInt(0, cfg.INIT_PARMS['solr-host-port'][ctoParams.cluster].length - 1)];     
    var solrHostPortToks = solrHostPort.split(':');
    
    // Prepare the HTTP options
    var options = {
        host: solrHostPortToks[0],
        port: solrHostPortToks[1],
        method: 'POST',
        path: '/solr/' + cfg.INIT_PARMS['solr-cluster'][ctoParams.cluster] + '/select',
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
                //console.log(JSON.stringify(results));
                
                var ctoResult = {
                            id : ctoParams.id,
                            pubyr : results.facet_counts.facet_fields['pubyr-f']};
                
                //console.log(JSON.stringify(ctoResult));

                callback(null,ctoResult);
                                   
            } else {
                console.log(buf.toString()); 
                callback('Problems getting the facet counts for ' + JSON.stringify(ctoParams));                             
               
            }

        });
        
    });

    req2.on('error', function(e) {
    
        console.log(e);

        callback('Problems getting the facet counts for ' + JSON.stringify(ctoParams));
	           
    });
                 
    // Send the request
    //console.log(ctoParams.query + '&facet=true&facet.mincount=1&facet.field=pubyr-f&f.pubyr-f.facet.limit=200&facet.sort=index&wt=json&indent=true&rows=0');
    req2.write(ctoParams.query + '&facet=true&facet.mincount=1&facet.field=pubyr-f&f.pubyr-f.facet.limit=200&facet.sort=index&wt=json&indent=true&rows=0');
    req2.end();        
    
};


/**
 * addTask (closure)
 * 
 * @param ctoParams
 */
function addTask(ctoParams) { // factory function to create the queries
    return function (cb) { // doQuery
        getCtoCount(ctoParams, cb);
    };
}


 /**
  * getCtoCounts
  * 
  * @param cluster
  * @param limit
  * @param results
  * @param fn - callback 
  */
function getCtoCounts(cluster, limit, results, fn) {

    // Get the logger
    var logger = app.logger;
    
    var totQuery = 'q=(';
    var indLimit = 20;
    var totLimit = 2000;
    if (results.response.numFound < indLimit) {
        indLimit = results.response.numFound;
    }
    if (results.response.numFound < totLimit) {
        totLimit = results.response.numFound;
    }

    // Create an array of functions to execute in parallel to get pubyr nav counts for each eid
    var tasks = [];
        
    for (var i = 0; i < totLimit; i++) {
    
        if (i < indLimit) {
        
            var query = 'q=refeid:(' + results.response.docs[i].eid + ')';
            
            var ctoParams =  {
                             id : results.response.docs[i].eid,
	                         cluster : cluster,
	                         query : query
            }; 
            
            //console.log(ctoParams);
            tasks.push(addTask(ctoParams));
 
        }
               
        if (i > 0) {
            totQuery += ' OR '
        }
        totQuery += 'refeid:(' + results.response.docs[i].eid + ')';
        
    }
    
    totQuery += ')';

    var ctoParams =  {
            id : 'total',
            cluster : cluster,
            query : totQuery
    }; 
    
    //console.log(totQuery);
    tasks.push(addTask(ctoParams));
    
    async.parallelLimit(tasks,limit,function(err,results) {
        if (err) {
	           
            logger.info('Problems getting cto counts '+ err);
            return fn('Problems getting cto counts');
	               
        } else {
	        
            return fn(null, results);
	               
        }
    });

};


/**
 * Render the cto search form
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
	   .render('ctoTest');
	                      	                        	            
};


/**
 * Submit the search form
 *
 * @param req - http request
 * @param res - http response
 */ 
exports.submit = function(req, res){

    // Get the logger
    var logger = app.logger;
    
    // Get ready to set the new parameters
    var searchParameters = {};
   
    searchParameters.query = '';
    var key = req.param('query'); 
    var freeFormQuery = req.param('freeFormQuery');
    
    if (typeof freeFormQuery !== 'undefined') {

        searchParameters.dType = req.param('cluster');
        searchParameters.dSet = 'z';
        searchParameters.dIdx = '1';
        searchParameters.dNav = 'cto';
        
    } else {
    
        var keyToks = key.split('_');
        // Get the query directives 
        searchParameters.dType = keyToks[0];
        searchParameters.dSet = keyToks[1];
        searchParameters.dIdx = keyToks[2];
        searchParameters.dNav = 'cto';
        
    }
    
    //console.log(searchParameters);

    // Get the query from DDB
    ddb.getQuery(key, freeFormQuery, function(err,query) {     
        
        if (err) {
     
            console.log(err);

            res.status(501)
                    .set('rType', 'loadQuery')
                    .set('dType', searchParameters.dType)
                    .set('dSet',searchParameters.dSet)
                    .set('dNav',searchParameters.dNav)
                    .set('dIdx',searchParameters.dIdx)                   
                    .set('dHits', -1)
                    .set('solrRspLen', -1)
                    .set('solrRspTim', -1)
	                .send();
	                
           
        } else { 
           
           //console.log(searchParameters);
           //console.log(query);       
           
           // Clean up the query 
           // Only keep the actual query
           // Only request the eid field to be returned
           // Only request the first 20 documents
           
           var flOffset = query.indexOf('&fl=');
           query = query.substring(0,flOffset);
           query += '&fl=eid&start=0&rows=2000&sort=score desc';
           //console.log(query);
                      
           // Set the HTTP Headers
           var headers = {
                            'Content-Type' : 'application/x-www-form-urlencoded'
           };
  
           var solrHostPort = cfg.INIT_PARMS['solr-host-port'][searchParameters.dType][getRandomInt(0, cfg.INIT_PARMS['solr-host-port'][searchParameters.dType].length - 1)];    
           
           var solrHostPortToks = solrHostPort.split(':');
    
           // Prepare the HTTP options
	       var options = {
                            host: solrHostPortToks[0],
                            port: solrHostPortToks[1],
                            method: 'POST',
                            path: '/solr/' + cfg.INIT_PARMS['solr-cluster'][searchParameters.dType] + '/select',
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
                       solrRspLen = buf.length;
                       
                       if (res2.statusCode == 200) {
                         
                            var encoding = res2.headers['content-encoding'];
                                                     
                            //console.log(buf.toString());
                            var results = JSON.parse(buf);
                            
                            var numFound = results.response.numFound;
                            var docsArr = results.response.docs;
                            
                            if (numFound < 10) {
                            
                                res.status(503)
                                    .set('rType', 'loadQuery')
                                    .set('dType', searchParameters.dType + '_cto')
                                    .set('dSet',searchParameters.dSet)
                                    .set('dNav',searchParameters.dNav)
                                    .set('dIdx',searchParameters.dIdx)
                                    .set('dHits', numFound)
                                    .set('solrRspLen', solrRspLen)
                                    .set('solrRspTim', results.responseHeader.QTime)
	                                .send('Less than 10 results, so we will not run CTO');
                                
                            } else {
                            
                                // Go run the CTO queries
                                getCtoCounts(searchParameters.dType, getTaskLimit(), results, function(err,  data) {
                            
                                    if (err) {
                                
                                        console.log(err);

                                        res.status(500)
                                            .set('rType', 'loadQuery')
                                            .set('dType', searchParameters.dType + '_cto')
                                            .set('dSet',searchParameters.dSet)
                                            .set('dNav',searchParameters.dNav)
                                            .set('dIdx',searchParameters.dIdx)
                                            .set('dHits', -1)
                                            .set('solrRspLen', -1)
                                            .set('solrRspTim', -1)
	                                       .send();
	                                    
                                    } else {
                                                      
                                        //console.log(JSON.stringify(data));
                                        var cto = { numFound : numFound,
                                                    query : query,
                                                    id : searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx,
                                                    navs : data };
                                        //data.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;
                                        //data.numFound = numFound;
                                        //console.log(JSON.stringify(data));

                                        res.status(200)
                                            .set('Content-Type', 'application/json; charset=UTF-8')
                                            .set('rType', 'loadQuery')
                                            .set('dType', searchParameters.dType + '_cto')
                                            .set('dSet',searchParameters.dSet)
                                            .set('dNav',searchParameters.dNav)
                                            .set('dIdx',searchParameters.dIdx)                                 
                                            .set('dHits', numFound)
                                            .set('solrRspLen', solrRspLen)
                                            .set('solrRspTim', results.responseHeader.QTime)
	                                        .send(cto);  
	                                    
	                               }
	                               
	                        });
	                        
	                      }
                                      
	                   } else {

                            var encoding = res2.headers['content-encoding'];
                                                       
                            //console.log(buf.toString());
                            //var results = JSON.parse(buf);
                            //results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;
                                 
                            res.status(res2.statusCode)
                               .set('Content-Type', 'application/json; charset=UTF-8')
                               .set('rType', 'loadQuery')
                               .set('dType', searchParameters.dType + '_cto')
                               .set('dSet',searchParameters.dSet)
                               .set('dNav',searchParameters.dNav)
                               .set('dIdx',searchParameters.dIdx)
                               .set('dHits', -1)
                               .set('solrRspLen', -1)
                               .set('solrRspTim', -1)
                               .send(results);                                                                                        
    
	                   }

                   });
            });

            req2.on('error', function(e) {
    
                console.log(e);

                res.status(500)
                    .set('rType', 'loadQuery')
                    .set('dType', searchParameters.dType + '_cto')
                    .set('dSet',searchParameters.dSet)
                    .set('dNav',searchParameters.dNav)
                    .set('dIdx',searchParameters.dIdx)
                    .set('dHits', -1)
                    .set('solrRspLen', -1)
                    .set('solrRspTim', -1)
	                .send();
	           
            });
                 
            // Send the request
            //console.log(query);
            req2.write(query + '&wt=json&indent=true');
            req2.end();                              
                                
        }

    });

};