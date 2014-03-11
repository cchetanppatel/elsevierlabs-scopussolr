/**
 *  Initialize the node.js app with configuration and
 *  routing information.
 * 
 *  Author: Darin McBeath
 */

/**
 * Module dependencies.
 */
var express = require('express')
  , winston = require('winston')
  , sns = require('winston-sns').SNS
  , cfg = require('./helpers/config')
  , loadTest = require('./routes/loadTest')
  , http = require('http')
  , path = require('path');

http.globalAgent.maxSockets = 100;

var app = express();

app.configure(function(){
  app.set('port', process.env.PORT || 81);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'ejs');  
  app.use(express.favicon());
  app.use(express.static(path.join(__dirname, 'public')));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.cookieParser());
  //app.use(express.logger('default'));
  app.use(express.logger('QUERY_STATS :res[rType] :status TS :date RES_TIME :response-time ML_TIME :res[mlRspTim] LEN :res[content-length] ML_LEN :res[mlRspLen] DTYPE :res[dType] DSET :res[dSet] DNAV :res[dNav] DIDX :res[dIdx] HITS :res[dHits] :method'));
  app.use(app.router);
});

var myLogTransports = [];

// Development configuration
app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true })); 
  cfg.init('development');
  // Log to console when level >= info and out.log when level >= warn
  // levels: { silly: 0, verbose: 1, info: 2, warn: 3, debug: 4, error: 5 }
  myLogTransports.push(new (winston.transports.Console)({ level: 'info' }));
});

// Production configuration
app.configure('production', function(){
  app.use(express.errorHandler()); 
  cfg.init('production');
  // Log to out.log when level >= info
  // levels: { silly: 0, verbose: 1, info: 2, warn: 3, debug: 4, error: 5 }
  myLogTransports.push(new (winston.transports.Console)({ level: 'info' }));
  myLogTransports.push(new (winston.transports.File)({ filename: 'errors.log', level: 'error' }));
  myLogTransports.push(new (winston.transports.SNS)({ level: 'error',  
                                                      'aws_key' : cfg.INIT_PARMS['aws-access-key-id'],
                                                      'aws_secret' : cfg.INIT_PARMS['aws-secret-access-key'],
                                                      'subscriber' : cfg.INIT_PARMS['aws-account-id'],
                                                      'topic_arn' : cfg.INIT_PARMS['sns-topic']}));
});


// Create the logger with the defined transport(s)
var logger = new (winston.Logger)({
  transports: myLogTransports,
});


/**
 * Map incoming requests to the appropriate route.  This works similar to how Java servlet
 * mapping happens in the web.xml.
 */

// load
app.get('/', loadTest.form)
app.get('/loadTest', loadTest.form);
app.get('/loadTest/form', loadTest.form);
app.post('/loadTest', loadTest.submit);


/**
 *  Create the server and listen on the specified port.
 */
http.createServer(app).listen(app.get('port'), function(){
  logger.info("Express server listening on port " + app.get('port'));
  logger.info(JSON.stringify(cfg.INIT_PARMS, null, ' '));
});

// Necessary for test cases, getting port, etc.
exports.app = app;

// Necessary for logging by other modules
exports.logger = logger;