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

    var keyToks = key.split('_');
    // Get the query directives 
    searchParameters.dType = keyToks[0];
    searchParameters.dSet = keyToks[1];
    searchParameters.dIdx = keyToks[2];
    searchParameters.dNav = 'unknown';
        
    //console.log(searchParameters);

           
    // Set the HTTP Headers
    var headers = {
        'Content-Type' : 'application/x-www-form-urlencoded', 
        'Accept-Encoding' : 'gzip'
    };
  
    var mlHostPort = cfg.INIT_PARMS['ml-host-port'][searchParameters.dType][getRandomInt(0, cfg.INIT_PARMS['ml-host-port'][searchParameters.dType].length - 1)];    
           
    var mlHostPortToks = mlHostPort.split(':');
    
    // Prepare the HTTP options
    var options = {
        host: mlHostPortToks[0],
        port: mlHostPortToks[1],
        method: 'GET',
        path: '/search.xqy?id=' + key,
        headers: headers
    };
            
    //console.log(options);

    var chunks = [];
    var mlRspLen;

    var req2 = http.get(options, function(res2) {
                                      
        res2.on('data', function (chunk) {
                  
            chunks.push(chunk);

        });
                            
        res2.on('end', function() {
                   
            var buf = Buffer.concat(chunks);
            //console.log(buf.toString());
            mlRspLen = buf.length;
                       
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
                                .set('mlRspLen', -1)
                                .set('mlRspTim', -1)
                                .send();
	                                   
                        } else {
                                 
                            // Not even sure we need to worr about gzip ???     
                            var results = decoded.toString();
                            results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;

                            res.status(200)
                                .set('Content-Type', 'application/json; charset=UTF-8')
                                .set('rType', 'loadQuery')
                                .set('dType', searchParameters.dType)
                                .set('dSet',searchParameters.dSet)
                                .set('dNav',searchParameters.dNav)
                                .set('dIdx',searchParameters.dIdx)                                        
                                .set('dHits', '') // TODO extract numFound from json response
                                .set('mlRspLen', mlRspLen)
                                .set('mlRspTim', '') // TODO extract time from json response
                                .send(results);
                                                                
                        }
	                             
                    });

                } else {
                             
                    var results = buf.toString();
                    //console.log(results);
                    
                    // Get numFound
                    var nfs = results.indexOf(' total="');
                    var nfe = results.indexOf('"', nfs + 8);
                    var numFound = results.substring(nfs + 8, nfe);
                    //console.log('numFound is '  + numFound);
                    
                    // Get ML response time (and convert to ms).  
                    var ts = results.indexOf('<search:total-time>PT');
                    var te = results.indexOf('S</search:total-time>', ts);
                    var eTime = results.substring(ts + 21, te);
                    //console.log('eTime is ' + eTime);
                    // PT1.470129S
                    // PT1M37.797717S
                    var mins = 0;
                    var secs = 0;
                    if (eTime.indexOf('M') !== -1) {
                        mins = eTime.substring(0, eTime.indexOf('M'));
                        mins = mins * 60 * 1000;
                        //console.log('minutes ' + mins);
                        secs = eTime.substring(eTime.indexOf('M') + 1)
                        secs =  Math.round(secs * 1000);
                        //console.log('seconds ' + secs);
                    } else {
                        secs = Math.round(eTime * 1000);
                        //console.log('seconds ' + secs);
                    }
                    var msTime = mins + secs;
                    //console.log('msTime is ' + msTime);
                    
                    // Indicate if query contains navigators
                    if (results.indexOf('<search:facet ') !== -1) {
                        searchParameters.dNav = 'nav';
                    } else {
                        searchParameters.dNav = 'nonav';
                    }
                    
                    // Include the key in the response (may not really be necessary)
                    results = results.replace('</search:metrics>','<search:key>' + key + '</search:key></search:metrics>');

                    res.status(200)
                        .set('Content-Type', 'application/xml; charset=UTF-8')
                        .set('rType', 'loadQuery')
                        .set('dType', searchParameters.dType)
                        .set('dSet',searchParameters.dSet)
                        .set('dNav',searchParameters.dNav)
                        .set('dIdx',searchParameters.dIdx)                                 
                        .set('dHits', numFound) 
                        .set('mlRspLen', mlRspLen)
                        .set('mlRspTim', msTime)
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
                                .set('mlRspLen', -1)
                                .set('mlRspTim', -1)
                                .send(' ');
	                                   
                        } else {
                                     
                            res.status(res2.statusCode)
                                .set('rType', 'loadQuery')
                                .set('dType', searchParameters.dType)
                                .set('dSet',searchParameters.dSet)
                                .set('dNav',searchParameters.dNav)
                                .set('dIdx',searchParameters.dIdx)
                                .set('dHits',-1)
                                .set('mlRspLen', -1)                                       
                                .set('mlRspTim', -1)
                                .send(decoded);
                                        
                        }
                    });

                } else {
                                 
                    //console.log(buf.toString());
                    //var results = JSON.parse(buf);
                    //results.query = searchParameters.dType + '_' + searchParameters.dSet + '_' + searchParameters.dIdx;
                                 
                    res.status(res2.statusCode)
                        .set('Content-Type', 'application/xml; charset=UTF-8')
                        .set('rType', 'loadQuery')
                        .set('dType', searchParameters.dType)
                        .set('dSet',searchParameters.dSet)
                        .set('dNav',searchParameters.dNav)
                        .set('dIdx',searchParameters.dIdx)
                        .set('dHits', -1)
                        .set('mlRspLen', -1)
                        .set('mlRspTim', -1)
                        .send(buf.toString());
	                                                             
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
            .set('mlRspLen', -1)
            .set('mlRspTim', -1)
            .send();
	           
    });                         
                                
};