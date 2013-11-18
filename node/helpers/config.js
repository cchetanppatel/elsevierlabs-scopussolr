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
                           
                                'solr-host-port-public' : ['ec2-54-224-19-230.compute-1.amazonaws.com:8983', 
                                                           'ec2-54-211-189-131.compute-1.amazonaws.com:8983'],
                                                           
                                'solr-host-port-private' : ['10.234.11.254:8983',
                                                            '10.28.212.147:8983'],
                                
                                'solr-host-port' : ['ec2-54-224-19-230.compute-1.amazonaws.com:8983', 
                                                    'ec2-54-211-189-131.compute-1.amazonaws.com:8983'],
                                                    
                                'solr-cluster' : { 'auth'  : 'author',
                                                   'affil' : 'affiliation',
                                                   'main'  : 'main'},
                                
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
                           
                                'solr-host-port-public' : ['ec2-54-224-19-230.compute-1.amazonaws.com:8983', 
                                                           'ec2-54-211-189-131.compute-1.amazonaws.com:8983'],
                                                           
                                'solr-host-port-private' : ['10.234.11.254:8983',
                                                            '10.28.212.147:8983'],
                                
                                'solr-host-port' : ['10.234.11.254:8983', 
                                                    '10.28.212.147:8983'],
 
                                 'solr-cluster' : { 'auth'  : 'author',
                                                   'affil' : 'affiliation',
                                                   'main'  : 'main'},
                                                   
                                'mode' : 'production'                          
                                
                             };
    }
    
};

