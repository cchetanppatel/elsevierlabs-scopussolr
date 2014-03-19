/**
 *  Configuration information for the node.js app.  Separate settings are provided
 *  for development and production.
 * 
 *  Author: Darin McBeath
 */
 
exports.init = function(environment) {

    
    if (environment == 'development') {
    
        // Development settings
        exports.INIT_PARMS = {
        
                                'aws-access-key-id' : 'AKIAJC674WSBSYUQYJLQ',
                                'aws-secret-access-key' : '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',
                                'aws-account-id' : '5459-5720-4479',

                                'ddb-endpoint' : 'https://dynamodb.us-east-1.amazonaws.com',
                                'ddb-region' : 'us-east-1',
                                //'ddb-load-query-table' : 'scopus-solr-load-institution-query',
                                //'ddb-load-query-table' : 'scopus-solr-load-author-query',
                                'ddb-load-query-table' : 'scopus-solr-load-core-query',
   
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',
                                
                                'solr-host-port-public' : { 'affil': ['23.22.65.28:8080'],
                                                            'auth' : ['54.224.74.192:8080'],
                                                            'core' : ['54.197.171.116:8080'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.207.53.181:8080'],
                                                             'auth' : ['10.79.131.225:8080'],
                                                             'core' : ['10.148.196.70:8080'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['23.22.65.28:8080'],
                                                     'auth' : ['54.224.74.192:8080'],
                                                     'core' : ['54.197.171.116:8080'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},
                                                   
                                'solr-facets' : { 'auth'   : ['active-f','affilcity-f','affilctry-f','afid-f','afnameid-f','srctitle-f','subjclus-f'],
                                                  'affil'  : ['affilcity-f','affilctry-f'],
                                                  //'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactkeyword-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','statustype-f','subjabbr-f','subtype-f']},
                                                  'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','subjabbr-f','subtype-f']},
                                                  
                                                  
                                'task-limit' : 5,
                                
                                'facets-disabled' : true,
                                
                                'rewrite-query' : true,
                                
                                'mode' : 'development'
                                
                             };
                             
    } else if (environment == 'production') {
        
        // Production settings
        exports.INIT_PARMS = {

        
                                'aws-access-key-id' : 'AKIAJC674WSBSYUQYJLQ',
                                'aws-secret-access-key' : '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',
                                'aws-account-id' : '5459-5720-4479',

                                'ddb-endpoint' : 'https://dynamodb.us-east-1.amazonaws.com',
                                'ddb-region' : 'us-east-1',
                                //'ddb-load-query-table' : 'scopus-solr-load-institution-query',
                                //'ddb-load-query-table' : 'scopus-solr-load-author-query',
                                'ddb-load-query-table' : 'scopus-solr-load-core-query',
                                
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',                          
                                                           
                                'solr-host-port-public' : { 'affil': ['23.22.65.28:8080'],
                                                            'auth' : ['54.224.74.192:8080'],
                                                            'core' : ['54.197.171.116:8080'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.207.53.181:8080'],
                                                             'auth' : ['10.79.131.225:8080'],
                                                             'core' : ['10.148.196.70:8080'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['10.207.53.181:8080'],
                                                     'auth' : ['10.79.131.225:8080'],
                                                     'core' : ['10.148.196.70:8080'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},

                                'solr-facets' : { 'auth'   : ['active-f','affilcity-f','affilctry-f','afid-f','afnameid-f','srctitle-f','subjclus-f'],
                                                  'affil'  : ['affilcity-f','affilctry-f'],
                                                  //'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactkeyword-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','statustype-f','subjabbr-f','subtype-f']},
                                                  'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','subjabbr-f','subtype-f']},
                                                  
                                                  
                                'task-limit' : 5,

                                'facets-disabled' : true,
                                
                                'rewrite-query' : true,
                                
                                'mode' : 'production'                          
                                
                             };
    }
    
};

