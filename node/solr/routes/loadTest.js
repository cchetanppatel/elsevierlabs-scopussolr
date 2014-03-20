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
var zlib = require('zlib');
var ddb = require('../helpers/ddb');


// Round robin
function getRandomInt (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


/**
 * Render the search load form
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
	   .render('loadTest');
	                      	                        	            
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
        searchParameters.dNav = 'unknown';
        
    } else {
    
        var keyToks = key.split('_');
        // Get the query directives 
        searchParameters.dType = keyToks[0];
        searchParameters.dSet = keyToks[1];
        searchParameters.dIdx = keyToks[2];
        searchParameters.dNav = 'unknown';
        
    }
    //console.log(searchParameters);

    // Get the query from DDB
    ddb.getQuery(key, freeFormQuery, function(err,query) {

        //console.log(searchParameters);
        //console.log(query);      
        
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
	                
        } else if (cfg.INIT_PARMS['facets-disabled'] && query.indexOf('facet=') > 0) {
        
            /* Begin Disable Facets */        
            res.status(500)
                    .set('rType', 'loadQuery')
                    .set('dType', searchParameters.dType)
                    .set('dSet',searchParameters.dSet)
                    .set('dNav',searchParameters.dNav)
                    .set('dIdx',searchParameters.dIdx)                   
                    .set('dHits', -1)
                    .set('solrRspLen', -1)
                    .set('solrRspTim', -1)
	                .send();
            /* End Disable Facets */

        } else {
            
            if (cfg.INIT_PARMS['rewrite-query']) {

                /* Begin Rewrite Query */
          
                // search for an 'AND pubyr: range clause in the query (greater than, greater than equal)
                var pubyrbegoffset = query.indexOf('AND pubyr:[');
                if (pubyrbegoffset == -1) {
                    pubyrbegoffset = query.indexOf('AND pubyr:{');
                } 
                var pubyrendoffset = 0;
                var fq = query.indexOf('&fq=');
                
                // Check for clause but make sure it's not part of the filter query
                while (pubyrbegoffset > -1 && (fq < 0 || pubyrbegoffset < fq)) {
                    // Check if the end is less than or less than equal
                    var pubyrendoffset1 = query.substring(pubyrbegoffset).indexOf(']');
                    var pubyrendoffset2 = query.substring(pubyrbegoffset).indexOf('}');
                    if (pubyrendoffset1 > -1 && pubyrendoffset2 > -1) {
                        if (pubyrendoffset1 < pubyrendoffset2) {
                            pubyrendoffset = pubyrendoffset1;
                        } else {
                            pubyrendoffset = pubyrendoffset2;
                        }
                    } else if (pubyrendoffset1 > -1) {
                        pubyrendoffset = pubyrendoffset1;
                    }  else if (pubyrendoffset2 > -1){
                        pubyrendoffset = pubyrendoffset2;
                    }
                    
                    // Adjust the end to point to beyond the enclosing bracket (we need to keep copy along the bracket)
                    pubyrendoffset = pubyrbegoffset + pubyrendoffset + 1
                    // Get the pubyr clause without the AND
                    var pubyrclause = query.substring(pubyrbegoffset + 4,pubyrendoffset);
                    //Remove the AND pubyr clause from the original query and add as a filter instead
                    query = query.replace(query.substring(pubyrbegoffset,pubyrendoffset),'');
                    if (query.indexOf('&fq=') > -1) {
                        query = query.replace('&fq=','&fq=' + pubyrclause + ' AND ')
                    } else {
                        query = query + '&fq=' + pubyrclause;
                    }              
                    // search for next 'AND pubyr: range clause in the query (greater than or greater than equal)
                    pubyrbegoffset = query.indexOf('AND pubyr:[');
                    if (pubyrbegoffset == -1) {
                        pubyrbegoffset = query.indexOf('AND pubyr:{');
                    }
                    pubyrendoffset = 0;
                    fq = query.indexOf('&fq=');
                }
      
                // Now check for pubyr equal clause
                pubyrbegoffset = query.indexOf('AND pubyr:');         
                pubyrendoffset = 0;
                fq = query.indexOf('&fq=');
                
                // Check for clause but make sure it's not part of the filter query
                while (pubyrbegoffset > -1 && (fq < 0 || pubyrbegoffset < fq)) {
                    // Check if the end is less than or less than equal
                    var pubyrendoffset1 = query.substring(pubyrbegoffset + 4).indexOf(' ');
                    var pubyrendoffset2 = query.substring(pubyrbegoffset + 4).indexOf(')');
                    if (pubyrendoffset1 > -1 && pubyrendoffset2 > -1) {
                        if (pubyrendoffset1 < pubyrendoffset2) {
                            pubyrendoffset = pubyrendoffset1;
                        } else {
                            pubyrendoffset = pubyrendoffset2;
                        }
                    } else if (pubyrendoffset1 > -1) {
                        pubyrendoffset = pubyrendoffset1;
                    }  else if (pubyrendoffset2 > -1){
                        pubyrendoffset = pubyrendoffset2;
                    }
                    
                    // adjust end by 4 because we stepped over the 'AND ' when looking for it
                    pubyrendoffset = pubyrbegoffset + pubyrendoffset + 4;
                    // Get the pubyr clause without the AND
                    var pubyrclause = query.substring(pubyrbegoffset + 4,pubyrendoffset);
                    //Remove the AND pubyr clause from the original query and add as a filter instead
                    query = query.replace(query.substring(pubyrbegoffset,pubyrendoffset),'');
                    if (query.indexOf('&fq=') > -1) {
                        query = query.replace('&fq=','&fq=' + pubyrclause + ' AND ')
                    } else {
                        query = query + '&fq=' + pubyrclause;
                    }              
                    // search for next 'AND pubyr: range clause in the query (greater than or greater than equal)
                    pubyrbegoffset = query.indexOf('AND pubyr:');
                    pubyrendoffset = 0;
                    fq = query.indexOf('&fq=');
                }  
           
                // search for an 'AND fastloaddate: range clause in the query (greater than, greater than equal)
                var fastloaddatebegoffset = query.indexOf('AND fastloaddate:[');
                if (fastloaddatebegoffset == -1) {
                    fastloaddatebegoffset = query.indexOf('AND fastloaddate:{');
                } 
                var fastloaddateendoffset = 0;
                fq = query.indexOf('&fq=');
                
                // Check for clause but make sure it's not part of the filter query
                while (fastloaddatebegoffset > -1 && (fq < 0 || fastloaddatebegoffset < fq)) {
                    // Check if the end is less than or less than equal
                    var fastloaddateendoffset1 = query.substring(fastloaddatebegoffset).indexOf(']');
                    var fastloaddateendoffset2 = query.substring(fastloaddatebegoffset).indexOf('}');
                    if (fastloaddateendoffset1 > -1 && fastloaddateendoffset2 > -1) {
                        if (fastloaddateendoffset1 < fastloaddateendoffset2) {
                            fastloaddateendoffset = fastloaddateendoffset1;
                        } else {
                            fastloaddateendoffset = fastloaddateendoffset2;
                        }
                    } else if (fastloaddateendoffset1 > -1) {
                        fastloaddateendoffset = fastloaddateendoffset1;
                    }  else if (fastloaddateendoffset2 > -1){
                        fastloaddateendoffset = fastloaddateendoffset2;
                    }
                
                    // Adjust the end to point to beyond the enclosing bracket (we need to keep copy along the bracket)
                    fastloaddateendoffset = fastloaddatebegoffset + fastloaddateendoffset + 1
                    // Get the fastloaddate clause without the AND
                    var fastloaddateclause = query.substring(fastloaddatebegoffset + 4,fastloaddateendoffset);
                    //Remove the AND fastloaddate clause from the original query and add as a filter instead
                    query = query.replace(query.substring(fastloaddatebegoffset,fastloaddateendoffset),'');
                    if (query.indexOf('&fq=') > -1) {
                        query = query.replace('&fq=','&fq=' + fastloaddateclause + ' AND ')
                    } else {
                        query = query + '&fq=' + fastloaddateclause;
                    }
               
                    // search for next 'AND fastloaddate: range clause in the query (greater than or greater than equal)
                    fastloaddatebegoffset = query.indexOf('AND fastloaddate:[');
                    if (fastloaddatebegoffset == -1) {
                        fastloaddatebegoffset = query.indexOf('AND fastloaddate:{');
                    }
                    fastloaddateendoffset = 0;
                    fq = query.indexOf('&fq=');

                    /* End Rewrite Query */
                    
                } 
                
           
           
                if (cfg.INIT_PARMS['slim-edismax']) {
                
                    /* Begin slim edismax */
                    
                    query = query.replace(cfg.INIT_PARMS['solr-small']['old'],cfg.INIT_PARMS['solr-small']['new']);
                    query = query.replace(cfg.INIT_PARMS['solr-medium']['old'],cfg.INIT_PARMS['solr-medium']['new']);
                    query = query.replace(cfg.INIT_PARMS['solr-all']['old'],cfg.INIT_PARMS['solr-all']['new']);
                    
                    /* End slim edismax */      
                    
                }
           
           } 
           
           //console.log(query);

           
           
           // Indicate if query contains navigators
           if (query.indexOf('facet=true') !== -1) {
               searchParameters.dNav = 'nav';
           } else {
               searchParameters.dNav = 'nonav';
           }
           
           // Set the HTTP Headers
           var headers = {
                            'Content-Type' : 'application/x-www-form-urlencoded', 
                            'Accept-Encoding' : 'gzip'
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
                            
                             if (encoding == 'gzip') {                           

                               zlib.gunzip(buf, function(err, decoded) {
                               
                                 if (err) {
                                 
                                     console.log('Problems with gzip');
                                     console.log(err);
                                     
                                     res.status(500)
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
                                 
                                    var results = JSON.parse(decoded);
                                    results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;

                                     res.status(200)
                                        .set('Content-Type', 'application/json; charset=UTF-8')
                                        .set('rType', 'loadQuery')
                                        .set('dType', searchParameters.dType)
                                        .set('dSet',searchParameters.dSet)
                                        .set('dNav',searchParameters.dNav)
                                        .set('dIdx',searchParameters.dIdx)                                        
                                        .set('dHits', results.response.numFound)
                                        .set('solrRspLen', solrRspLen)
                                        .set('solrRspTim', results.responseHeader.QTime)
	                                    .send(results);
                                                                
	                             }
	                             
                               });

                             } else {
                             
                                //console.log(buf.toString());
                                var results = JSON.parse(buf);
                                results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;

                                 res.status(200)
                                    .set('Content-Type', 'application/json; charset=UTF-8')
                                    .set('rType', 'loadQuery')
                                    .set('dType', searchParameters.dType)
                                    .set('dSet',searchParameters.dSet)
                                    .set('dNav',searchParameters.dNav)
                                    .set('dIdx',searchParameters.dIdx)                                 
                                    .set('dHits', results.response.numFound)
                                    .set('solrRspLen', solrRspLen)
                                    .set('solrRspTim', results.responseHeader.QTime)
	                                .send(results);
	                                                             
	                        }
                                      
	                   } else {

                            var encoding = res2.headers['content-encoding'];
                            
                             if (encoding == 'gzip') {

                               zlib.gunzip(buf, function(err, decoded) {
                               
                                 if (err) {
                                 
                                     console.log('Problems with gzip');
                                     console.log(err);
                                     
                                     res.status(500)
                                        .set('rType', 'loadQuery')
                                        .set('dType', searchParameters.dType)
                                        .set('dSet',searchParameters.dSet)
                                        .set('dNav',searchParameters.dNav)
                                        .set('dIdx',searchParameters.dIdx)                                       
                                        .set('dHits',-1)
                                        .set('solrRspLen', -1)
                                        .set('solrRspTim', -1)
	                                    .send(' ');
	                                   
                                 } else {
                                     
                                     res.status(res2.statusCode)
                                        .set('rType', 'loadQuery')
                                        .set('dType', searchParameters.dType)
                                        .set('dSet',searchParameters.dSet)
                                        .set('dNav',searchParameters.dNav)
                                        .set('dIdx',searchParameters.dIdx)
                                        .set('dHits',-1)
                                        .set('solrRspLen', -1)                                       
                                        .set('solrRspTim', -1)
                                        .send(decoded);
                                        
                                 }
                                });

                             } else {
                                 
                                 //console.log(buf.toString());
                                 var results = JSON.parse(buf);
                                 results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;
                                 
                                 res.status(res2.statusCode)
                                    .set('Content-Type', 'application/json; charset=UTF-8')
                                    .set('rType', 'loadQuery')
                                    .set('dType', searchParameters.dType)
                                    .set('dSet',searchParameters.dSet)
                                    .set('dNav',searchParameters.dNav)
                                    .set('dIdx',searchParameters.dIdx)
                                    .set('dHits', -1)
                                    .set('solrRspLen', -1)
                                    .set('solrRspTim', -1)
                                    .send(results);
	                                                             
	                        }                                
    
	                   }

                   });
            });

            req2.on('error', function(e) {
    
                console.log(e);

                res.status(500)
                    .set('rType', 'loadQuery')
                    .set('dType', searchParameters.dType)
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