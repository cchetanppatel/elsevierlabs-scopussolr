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

        searchParameters.dType = 'user';
        searchParameters.dSet = '1';
        searchParameters.dIdx = '1';
        
    } else {
    
        var keyToks = key.split('_');
        // Get the query directives 
        searchParameters.dType = keyToks[0];
        searchParameters.dSet = keyToks[1];
        searchParameters.dIdx = keyToks[2];
        
    }


    // Get the query from DDB
    ddb.getQuery(key, freeFormQuery, function(err,query) {
        
        //console.log(query);
              
        if (err) {
     
            console.log(err);

            res.status(501)
                    .set('rType', 'loadQuery')
                    .set('dType', searchParameters.dType)
                    .set('dSet',searchParameters.dSet)
                    .set('dIdx',searchParameters.dIdx)
                    .set('dHits', -1)
                    .set('solrRspLen', -1)
                    .set('solrRspTim', -1)
	                .send();
         
        } else {
                       
    
           // Set the HTTP Headers
           var headers = {
                            'Accept-Encoding' : 'gzip'
           };
    
           var solrHostPort = cfg.INIT_PARMS['solr-host-port'][getRandomInt(0, cfg.INIT_PARMS['solr-host-port'].length - 1)];    
           
           var solrHostPortToks = solrHostPort.split(':');
    
           // Prepare the HTTP options
           // TODO this is not really a POST as nothing is sent in the body
	       var options = {
                            host: solrHostPortToks[0],
                            port: solrHostPortToks[1],
                            method: 'POST',
                            path: '/solr/' + cfg.INIT_PARMS['solr-cluster'].affil + '/select?' + encodeURI(query) + '&wt=json&indent=true',
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
                                        .set('dIdx',searchParameters.dIdx)
                                        .set('dHits', results.response.numFound)
                                        .set('solrRspLen', solrRspLen)
                                        .set('solrRspTim', results.responseHeader.QTime)
	                                    .send(results);
                                                                
	                             }
	                             
                               });

                             } else {
                             
                                console.log(buf.toString());
                                var results = JSON.parse(buf);
                                results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;

                                 res.status(200)
                                    .set('Content-Type', 'application/json; charset=UTF-8')
                                    .set('rType', 'loadQuery')
                                    .set('dType', searchParameters.dType)
                                    .set('dSet',searchParameters.dSet)
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
                    .set('dIdx',searchParameters.dIdx)
                    .set('dHits', -1)
                    .set('solrRspLen', -1)
                    .set('solrRspTim', -1)
	                .send();
	           
            });
                 
            // Send the request
            req2.write('');
            req2.end();                              
                                
        }

    });

};