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
	public static final String SOLR_ENDPOINT = prop.getProperty("SOLR_ENDPOINT","http://localhost:8983/solr/affiliation/");
	public static final String SOLR_COLLECTION = "affiliation";
	//public static final String SOLR_COLLECTION = "author";
	
	// SQS Endpoint
	public static final String SQS_ENDPOINT = prop.getProperty("SQS_ENDPOINT","https://sqs.us-east-1.amazonaws.com");
	
	// S3 Endpoint
	public static final String S3_ENDPOINT = prop.getProperty("S3_ENDPOINT","https://s3.amazonaws.com");
	
	// Dynamo Endpoint
	public static final String DYNAMO_ENDPOINT = prop.getProperty("DYNAMO_ENDPOINT","https://dynamodb.us-east-1.amazonaws.com/");
	
	// SNS Endpoint
	public static final String SNS_ENDPOINT = prop.getProperty("SNS_ENDPOINT","https://sns.us-east-1.amazonaws.com");
	
	// SQS queue name
	public static final String SQS_QUEUE_NAME = prop.getProperty("SQS_QUEUE_NAME","solr-affiliation-bulkload");
	public static final String SQS_PROBLEM_QUEUE_NAME = prop.getProperty("SQS_PROBLEM_QUEUE_NAME","solr_affiliation-bulkload-problems");
	//public static final String SQS_QUEUE_NAME = prop.getProperty("SQS_QUEUE_NAME","solr-author-bulkload");
	//public static final String SQS_PROBLEM_QUEUE_NAME = prop.getProperty("SQS_PROBLEM_QUEUE_NAME","solr_author-bulkload-problems");
	
	// S3 bucket name
	public static final String S3_XML_BUCKET_NAME = prop.getProperty("S3_XML_BUCKET_NAME","affiliation-xml");
	//public static final String S3_XML_BUCKET_NAME = prop.getProperty("S3_XML_BUCKET_NAME","author-xml");
	
	// DynamoDB table name	
	public static final String DYNAMO_SOLR_SEARCH_TABLE_NAME = prop.getProperty("DYNAMO_SOLR_SEARCH_TABLE_NAME","solr-affiliation");
	//public static final String DYNAMO_SOLR_SEARCH_TABLE_NAME = prop.getProperty("DYNAMO_SOLR_SEARCH_TABLE_NAME","solr-author");
	public static final Long  DYNAMO_SOLR_SEARCH_TABLE_READ_CAPACITY = 5L;
	public static final Long  DYNAMO_SOLR_SEARCH_TABLE_WRITE_CAPACITY = 5L;
	
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
