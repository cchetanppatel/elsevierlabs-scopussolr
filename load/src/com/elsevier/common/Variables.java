package com.elsevier.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Constants used by the Solr project.
 * 
 * @author Darin McBeath
 *
 */
public class Variables {

	// Container for watch configuration information
	private static Properties prop = new Properties();
	
	// Process the properties file (if one was specified)
	static { 
		
		try {
			
			// will want to change the directory/file to /watch4Content/watcher.properties
			//prop.load(new FileInputStream("/Users/mcbeathd/props/elasticsearch.properties"));
			prop.load(new FileInputStream("/home/ec2-user/solr.properties"));
			
		} catch (FileNotFoundException fnfex) {
			
			// File not found, use the default settings
			System.out.println("Properties file not found.  Will use default settings.");
			
		} catch (Exception ex) {
			
			// Problems processing the properties file. Abort.
			ex.printStackTrace();
			System.out.println("Problems with properties file.  Aborting.");
			System.exit(1);
			
		}

	}	
	
	// SOLR
	public static final String SOLR_ENDPOINT = prop.getProperty("SOLR_ENDPOINT","http://localhost:8983/solr/core/");
	//public static final String SOLR_COLLECTION = "affil";
	//public static final String SOLR_COLLECTION = "auth";
	public static final String SOLR_COLLECTION = "core";
	
	public static final int SOLR_AUTOCOMMIT_DELAY_MS = Integer.parseInt(prop.getProperty("SOLR_AUTOCOMMIT_DELAY_MS","-1"));   // -1 will use the autocommit delay defined in core's solrconfig.xml
	
	// SQS Endpoint
	public static final String SQS_ENDPOINT = prop.getProperty("SQS_ENDPOINT","https://sqs.us-east-1.amazonaws.com");
	
	// S3 Endpoint
	public static final String S3_ENDPOINT = prop.getProperty("S3_ENDPOINT","https://s3.amazonaws.com");
	
	// Dynamo Endpoint
	public static final String DYNAMO_ENDPOINT = prop.getProperty("DYNAMO_ENDPOINT","https://dynamodb.us-east-1.amazonaws.com/");
	
	// SNS Endpoint
	public static final String SNS_ENDPOINT = prop.getProperty("SNS_ENDPOINT","https://sns.us-east-1.amazonaws.com");
	
	// SQS queue name
	//public static final String SQS_QUEUE_NAME = prop.getProperty("SQS_QUEUE_NAME","solr-affiliation-bulkload");
	//public static final String SQS_PROBLEM_QUEUE_NAME = prop.getProperty("SQS_PROBLEM_QUEUE_NAME","solr_affiliation-bulkload-problems");
	//public static final String SQS_QUEUE_NAME = prop.getProperty("SQS_QUEUE_NAME","solr-author-bulkload");
	//public static final String SQS_PROBLEM_QUEUE_NAME = prop.getProperty("SQS_PROBLEM_QUEUE_NAME","solr-author-bulkload-problems");
	public static final String SQS_QUEUE_NAME = prop.getProperty("SQS_QUEUE_NAME","solr-core-bulkload");
	public static final String SQS_PROBLEM_QUEUE_NAME = prop.getProperty("SQS_PROBLEM_QUEUE_NAME","solr-core-bulkload-problems");
	
	// S3 bucket name
	//public static final String S3_XML_BUCKET_NAME = prop.getProperty("S3_XML_BUCKET_NAME","affiliation-xml");
	//public static final String S3_XML_BUCKET_NAME = prop.getProperty("S3_XML_BUCKET_NAME","author-xml");
	public static final String S3_XML_BUCKET_NAME = prop.getProperty("S3_XML_BUCKET_NAME","abstract-xml");
	
	// DynamoDB table name	
	//public static final String DYNAMO_SOLR_SEARCH_TABLE_NAME = prop.getProperty("DYNAMO_SOLR_SEARCH_TABLE_NAME","solr-affiliation");
	//public static final String DYNAMO_SOLR_SEARCH_TABLE_NAME = prop.getProperty("DYNAMO_SOLR_SEARCH_TABLE_NAME","solr-author");
	public static final String DYNAMO_SOLR_SEARCH_TABLE_NAME = prop.getProperty("DYNAMO_SOLR_SEARCH_TABLE_NAME","solr-core");
	public static final Long  DYNAMO_SOLR_SEARCH_TABLE_READ_CAPACITY = 100L;
	public static final Long  DYNAMO_SOLR_SEARCH_TABLE_WRITE_CAPACITY = 100L;
	
	
	// Redshift variables
	public static final String AWS_REDSHIFT_INTEGRATE_REDSHIFT = prop.getProperty("AWS_REDSHIFT_INTEGRATE_REDSHIFT","false");
	public static final String AWS_REDSHIFT_ID = prop.getProperty("AWS_REDSHIFT_ID","es");
	public static final String AWS_REDSHIFT_PSWD = prop.getProperty("AWS_REDSHIFT_PSWD","Els3vier");
	public static final String AWS_REDSHIFT_CONNECT_URL = prop.getProperty("AWS_REDSHIFT_CONNECT_URL", "jdbc:postgresql://scopussolr.cx3c0gtqjf0o.us-east-1.redshift.amazonaws.com:5439/counts?tcpKeepAlive=true");
	public static final String AWS_REDSHIFT_AFFIL_CNT_TABLE = prop.getProperty("AWS_REDSHIFT_AFFIL_CNT_TABLE","corestoaffiliations");
	public static final String AWS_REDSHIFT_AUTH_CNT_TABLE = prop.getProperty("AWS_REDSHIFT_AUTH_CNT_TABLE","corestoauthors");
	public static final String AWS_REDSHIFT_REF_TABLE = prop.getProperty("AWS_REDSHIFT_REF_TABLE","corestoreferences");
	
	public static final String AWS_REDSHIFT_RESULTS_BUCKET = prop.getProperty("AWS_REDSHIFT_RESULTS_BUCKET","els-ats");
	//public static final String AWS_REDSHIFT_RESULTS_MANIFEST = prop.getProperty("AWS_REDSHIFT_RESULTS_MANIFEST","els-ats");
	public static final String AWS_REDSHIFT_RESULTS_MANIFEST = prop.getProperty("AWS_REDSHIFT_RESULTS_MANIFEST","scopuscnts/curt/author/results/diff/auth-manifest");
	//public static final String AWS_REDSHIFT_RESULTS_MANIFEST = prop.getProperty("AWS_REDSHIFT_RESULTS_MANIFEST","els-ats");
	
	//public static final String AWS_REDSHIFT_PARTS_QUEUE = prop.getProperty("AWS_REDSHIFT_PARTS_QUEUE","solr-affil-rs-parts");
	public static final String AWS_REDSHIFT_MANIFEST_PARTS_QUEUE = prop.getProperty("AWS_REDSHIFT_MANIFEST_PARTS_QUEUE","solr-author-rs-parts");
	//public static final String AWS_REDSHIFT_PARTS_QUEUE = prop.getProperty("AWS_REDSHIFT_PARTS_QUEUE","solr-core-rs-parts");
	
	//public static final String AWS_REDSHIFT_UPDATE_TYPE = prop.getProperty("AWS_REDSHIFT_UPDATE_TYPE","affil");
	public static final String AWS_REDSHIFT_UPDATE_TYPE = prop.getProperty("AWS_REDSHIFT_UPDATE_TYPE","author");
	//public static final String AWS_REDSHIFT_UPDATE_TYPE = prop.getProperty("AWS_REDSHIFT_UPDATE_TYPE","core");

	//public static final String AWS_REDSHIFT_UPDATE_QUEUE = prop.getProperty("AWS_REDSHIFT_UPDATE_QUEUE","solr-affil-rs-updates");
	public static final String AWS_REDSHIFT_UPDATE_QUEUE = prop.getProperty("AWS_REDSHIFT_UPDATE_QUEUE","solr-author-rs-updates");
	//public static final String AWS_REDSHIFT_UPDATE_QUEUE = prop.getProperty("AWS_REDSHIFT_UPDATE_QUEUE","solr-core-rs-updates");
	
	
	// SNS topic name
	public static final String SNS_TOPIC_NAME = prop.getProperty("SNS_TOPIC_NAME","arn:aws:sns:us-east-1:545957204479:solr-scopus");
	
	// SQS Delay (30 seconds)
	public static final Integer SQS_DELAY =  Integer.valueOf(prop.getProperty("SQS_DELAY","30"));
	
	// AWS access key
	public static final String AWS_ACCESS_KEY = prop.getProperty("AWS_ACCESS_KEY","AKIAJC674WSBSYUQYJLQ");
	
	// AWS secret key
	public static final String AWS_SECRET_KEY = prop.getProperty("AWS_SECRET_KEY","AuFLTmVjlu11TkikRa87ZoA4zOF75Q0hYG1T/wIe");
		
	// AWS Client Connection Timeout (default 50000 or 50 seconds)
	public static final int CONNECTION_TIMEOUT = Integer.parseInt(prop.getProperty("CONNECTION_TIMEOUT","300000")); // 5 minutes
	
	// AWS Client Socket Timeout (default 50000 or 50 seconds)
	public static final int SOCKET_TIMEOUT = Integer.parseInt(prop.getProperty("SOCKET_TIMEOUT","300000")); // 5 minutes
	
	// AWS Client Max Error Retry (default 3)
	public static final int MAX_ERROR_RETRY = Integer.parseInt(prop.getProperty("MAX_ERROR_RETRY","10"));
	
	// Pause file name
	public static final String PAUSE_FILE_NAME = prop.getProperty("PAUSE_FILE_NAME","pause");
	
	// Sleep period (60 seconds)
	public static final int SOLR_SEARCH_SLEEP_TIME = Integer.parseInt(prop.getProperty("SOR_SEARCH_SLEEP_TIME","60000"));
		
	/**
	 * Dump the variables and their values.
	 */
	public static void dumpVariables() {
		
		System.out.println("SQS_ENDPOINT = '" + SQS_ENDPOINT + "'");
		System.out.println("S3_ENDPOINT = '" + S3_ENDPOINT + "'");
		System.out.println("DYNAMO_ENDPOINT = '" + DYNAMO_ENDPOINT + "'");
		System.out.println("SNS_ENDPOINT = '" + SNS_ENDPOINT + "'");
		System.out.println("SQS_QUEUE_NAME = '" + SQS_QUEUE_NAME + "'");
		System.out.println("SQS_PROBLEM_QUEUE_NAME = '" + SQS_PROBLEM_QUEUE_NAME + "'");
		System.out.println("S3_XML_BUCKET_NAME = '" + S3_XML_BUCKET_NAME + "'");
		System.out.println("DYNAMO_SOLR_SEARCH_TABLE_NAME = '" + DYNAMO_SOLR_SEARCH_TABLE_NAME + "'");
		System.out.println("SNS_TOPIC_NAME = '" + SNS_TOPIC_NAME + "'");
		System.out.println("SQS_DELAY = '" + SQS_DELAY + "'");
		System.out.println("AWS_ACCESS_KEY = '" + AWS_ACCESS_KEY + "'");
		System.out.println("AWS_SECRET_KEY = '" + AWS_SECRET_KEY + "'");
		System.out.println("CONNECTION_TIMEOUT = '" + CONNECTION_TIMEOUT + "'");
		System.out.println("SOCKET_TIMEOUT = '" + SOCKET_TIMEOUT + "'");
		System.out.println("MAX_ERROR_RETRY = '" + MAX_ERROR_RETRY + "'");
		System.out.println("SOLR_SEARCH_SLEEP_TIME = '" + SOLR_SEARCH_SLEEP_TIME + "'");
		System.out.println("PAUSE_FILE_NAME = '" + PAUSE_FILE_NAME + "'");
		
	}	
}
