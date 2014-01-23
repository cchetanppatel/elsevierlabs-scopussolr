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
                                'ddb-load-query-table' : 'scopus-solr-load-query',
   
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',
                                
                                'solr-host-port-public' : { 'affil': ['54.224.52.75:8983'],
                                                            'auth' : ['54.224.52.75:8983'],
                                                            'core' : ['54.224.52.75:8983'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.236.151.246:8983'],
                                                             'auth' : ['10.236.151.246:8983'],
                                                             'core' : ['10.236.151.246:8983'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['54.224.52.75:8983'],
                                                     'auth' : ['54.224.52.75:8983'],
                                                     'core' : ['54.224.52.75:8983'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},
                                
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
                                'ddb-load-query-table' : 'scopus-solr-load-query',
                                
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',                          
                                                           
                                'solr-host-port-public' : { 'affil': ['54.224.52.75:8983'],
                                                            'auth' : ['54.224.52.75:8983'],
                                                            'core' : ['54.224.52.75:8983'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.236.151.246:8983'],
                                                             'auth' : ['10.236.151.246:8983'],
                                                             'core' : ['10.236.151.246:8983'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['10.236.151.246:8983'],
                                                     'auth' : ['10.236.151.246:8983'],
                                                     'core' : ['10.236.151.246:8983'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},
                                                   
                                'mode' : 'production'                          
                                
                             };
    }
    
};

