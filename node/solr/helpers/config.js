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
        
                                'aws-access-key-id' : 'XXXXXXXXXXX',
                                'aws-secret-access-key' : 'XXXXXXXXX',
                                'aws-account-id' : '5459-5720-4479',

                                'ddb-endpoint' : 'https://dynamodb.us-east-1.amazonaws.com',
                                'ddb-region' : 'us-east-1',
                                //'ddb-load-query-table' : 'scopus-solr-load-institution-query',
                                //'ddb-load-query-table' : 'scopus-solr-load-author-query',
                                'ddb-load-query-table' : 'scopus-solr-load-core-query',
                                //'ddb-load-query-table' : 'scopus-solr-count-features-query',
                                
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',
                                
                                'solr-host-port-public' : { 'affil': ['54.82.2.85:8080'],
                                                            'auth' : ['54.224.74.192:8080'],
                                                            'core' : ['54.197.171.116:8080','54.221.76.76:8080'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.9.186.16:8080'],
                                                             'auth' : ['10.79.131.225:8080'],
                                                             'core' : ['10.148.196.70:8080','10.148.144.220:8080'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['54.82.2.85:8080'],
                                                     'auth' : ['54.224.74.192:8080'],
                                                     'core' : ['54.197.171.116:8080','54.221.76.76:8080'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},
                                                   
                                'solr-facets' : { 'auth'   : ['active-f','affilcity-f','affilctry-f','afid-f','afnameid-f','srctitle-f','subjclus-f'],
                                                  'affil'  : ['affilcity-f','affilctry-f'],
                                                  //'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactkeyword-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','statustype-f','subjabbr-f','subtype-f']},
                                                  'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','subjabbr-f','subtype-f']},
                                                  
                                'solr-all' : { 'old' : "qf='all itemtitle^12 keywords^8 abs^8 auth^8 srctitle^2 issn^2 coden^2 doi^2 isbn^2 lang^2 pub^2 chemname^2 affil^2 ed^2 corres^2 collab^2 confall^2'",
                                               'new' : "qf='all itemtitle^12 keywords^8 abs^8 auth^8'"},
                                    
                                'solr-medium' : { 'old' : "qf='allmed itemtitle^12 keywords^8 abs^8 auth^8'",
                                                  'new' : "qf='allmed itemtitle^12 abs^8 auth^8'"},

                                'solr-small' : { 'old' : "qf='allsmall itemtitle^12 keywords^8 abs^8'",
                                                 'new' : "qf='allsmall itemtitle^12 abs^8'"},
                                
                                'task-limit' : 5,
                                
                                'facets-disabled' : true,
                                
                                'rewrite-query' : true,
                                
                                'slim-edismax' : false,
                                
                                'mode' : 'development'
                                
                             };
                             
    } else if (environment == 'production') {
        
        // Production settings
        exports.INIT_PARMS = {

        
                                'aws-access-key-id' : 'XXXXXXXXXXXXX',
                                'aws-secret-access-key' : 'XXXXXX',
                                'aws-account-id' : '5459-5720-4479',

                                'ddb-endpoint' : 'https://dynamodb.us-east-1.amazonaws.com',
                                'ddb-region' : 'us-east-1',
                                //'ddb-load-query-table' : 'scopus-solr-load-institution-query',
                                //'ddb-load-query-table' : 'scopus-solr-load-author-query',
                                'ddb-load-query-table' : 'scopus-solr-load-core-query',
                                
                                'sns-topic' : 'arn:aws:sns:us-east-1:545957204479:scopus-search',                          
                                                           
                                'solr-host-port-public' : { 'affil': ['54.82.2.85:8080'],
                                                            'auth' : ['54.224.74.192:8080'],
                                                            'core' : ['54.197.171.116:8080','54.221.76.76:8080'] },
                                                           
                                'solr-host-port-private' : { 'affil': ['10.9.186.16:8080'],
                                                             'auth' : ['10.79.131.225:8080'],
                                                             'core' : ['10.148.196.70:8080','10.148.144.220:8080'] },
                                                                                                                        
                                'solr-host-port' : { 'affil': ['10.9.186.16:8080'],
                                                     'auth' : ['10.79.131.225:8080'],
                                                     'core' : ['10.148.196.70:8080','10.148.144.220:8080'] },
                                                     
                                'solr-cluster' : { 'auth'  : 'auth',
                                                   'affil' : 'affil',
                                                   'core'  : 'core'},

                                'solr-facets' : { 'auth'   : ['active-f','affilcity-f','affilctry-f','afid-f','afnameid-f','srctitle-f','subjclus-f'],
                                                  'affil'  : ['affilcity-f','affilctry-f'],
                                                  //'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactkeyword-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','statustype-f','subjabbr-f','subtype-f']},
                                                  'core'   : ['affilctry-f','afid-f','aucite-f','authgrpid-f','authid-f','exactsrctitle-f','lang-f','prefnameauid-f','pubyr-f','srctype-f','subjabbr-f','subtype-f']},

                                'solr-all' : { 'old' : "qf='all itemtitle^12 keywords^8 abs^8 auth^8 srctitle^2 issn^2 coden^2 doi^2 isbn^2 lang^2 pub^2 chemname^2 affil^2 ed^2 corres^2 collab^2 confall^2'",
                                               'new' : "qf='all itemtitle^12 keywords^8 abs^8 auth^8'"},
                                    
                                'solr-medium' : { 'old' : "qf='allmed itemtitle^12 keywords^8 abs^8 auth^8'",
                                                  'new' : "qf='allmed itemtitle^12 abs^8 auth^8'"},

                                'solr-small' : { 'old' : "qf='allsmall itemtitle^12 keywords^8 abs^8'",
                                                 'new' : "qf='allsmall itemtitle^12 abs^8'"},
                                                  
                                'task-limit' : 5,

                                'facets-disabled' : true,
                                
                                'rewrite-query' : true,
                                
                                'slim-edismax' : false,                                
                                
                                'mode' : 'production'                          
                                
                             };
    }
    
};

