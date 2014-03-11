/**
 *  Configuration information for the Scopus/ML node.js app.  Separate settings are provided
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
  
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',
                                
                                'ml-host-port-public' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                            'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                            'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },
                                                           
                                'ml-host-port-private' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                             'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                             'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },
                                                                                                                        
                                'ml-host-port' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                     'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                     'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },                                                  
                                
                                'mode' : 'development'
                                
                             };
                             
    } else if (environment == 'production') {
        
        // Production settings
        exports.INIT_PARMS = {

        
                                'aws-access-key-id' : 'AKIAJC674WSBSYUQYJLQ',
                                'aws-secret-access-key' : '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',
                                'aws-account-id' : '5459-5720-4479',
                                
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',                          
                                                           
                                'ml-host-port-public' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                            'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                            'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },
                                                           
                                'ml-host-port-private' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                             'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                             'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },
                                                                                                                        
                                'ml-host-port' : { 'affil': ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                     'auth' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'],
                                                     'core' : ['ec2-54-83-15-82.compute-1.amazonaws.com:8008'] },      
                                
                                'mode' : 'production'                          
                                
                             };
    }
    
};

