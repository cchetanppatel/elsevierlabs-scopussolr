/**
 *  This module provides batch and real-time functionality.  The batch functionality
 *  administers (add, updates, deletes) the reference information table and the 
 *  reference lookup table.  The real-time functionality provides reference 
 *  resolution.
 * 
 *  Author: Darin McBeath
 */
 

/**
 * Module dependencies.
 */
var http = require('http');
var cfg = require('../helpers/config')
var app = require('../app');
var AWS = require('aws-sdk');
AWS.config.update({region: 'us-east-1'});


exports.getQuery = function(key, freeFormQuery, fn) {
  
    // Get the logger
    var logger = app.logger;
    
    // User entered query so no need to look up in DDB
    if (typeof freeFormQuery !== 'undefined') {
        return fn(null,freeFormQuery);
    }
    
    // Create DynamoDB object
    var dynamodb = new AWS.DynamoDB({ endpoint : cfg.INIT_PARMS['ddb-endpoint'],
                                      accessKeyId : cfg.INIT_PARMS['aws-access-key-id'],
                                      secretAccessKey : cfg.INIT_PARMS['aws-secret-access-key'],
                                      region : cfg.INIT_PARMS['ddb-region'],
                                      apiVersion: '2012-08-10'
                                     });

//console.log(key);

    var theKey = { 'S' : key};

//console.log(theKey);

    var getParams = {
        TableName : cfg.INIT_PARMS['ddb-load-query-table'],
        Key : {
	            'k' : theKey
         },
        AttributesToGet : ['q']
    };

//console.log(getParams);

    // Execute the query
    dynamodb.client.getItem(getParams, function(err,data) {
 
        if (err) {
        
            logger.info('getQuery error: ' + err);
            return fn('Problems retrieving the query for ' + theKey.S + '.');
            
        }

//console.log(data.Item.q.S);

        return fn(null, data.Item.q.S);

    });  
    
};