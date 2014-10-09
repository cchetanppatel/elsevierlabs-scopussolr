package com.elsevier.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Constants used by the ElasticSearch project.
 * 
 * @author Darin McBeath
 *
 */
public class Variables {
	
	// Dynamo Endpoint
	public static final String DYNAMO_ENDPOINT = "https://dynamodb.us-east-1.amazonaws.com/";
		
	// DynamoDB table name
	//public static final String  DYNAMO_SCOPUS_QUERY_TABLE_NAME = "scopus-solr-load-core-query";
	//public static final String  DYNAMO_SCOPUS_QUERY_TABLE_NAME = "scopus-solr-count-features-query";
	//public static final String  DYNAMO_SCOPUS_QUERY_TABLE_NAME = "scopus-solr-load-author-query";
	public static final String  DYNAMO_SCOPUS_QUERY_TABLE_NAME = "scopus-solr-load-core-query-extfile";
	//public static final String  DYNAMO_SCOPUS_QUERY_TABLE_NAME = "scopus-solr-load-institution-query";
	
	// AWS access key
	public static final String AWS_ACCESS_KEY = "";
	
	// AWS secret key
	public static final String AWS_SECRET_KEY = "";
		
	// AWS Client Connection Timeout (default 50000 or 50 seconds)
	public static final int CONNECTION_TIMEOUT = Integer.parseInt("300000"); // 5 minutes
	
	// AWS Client Socket Timeout (default 50000 or 50 seconds)
	public static final int SOCKET_TIMEOUT = Integer.parseInt("300000"); // 5 minutes
	
	// AWS Client Max Error Retry (default 3)
	public static final int MAX_ERROR_RETRY = Integer.parseInt("10");
	
	/**
	 * Dump the variables and their values.
	 */
	public static void dumpVariables() {
		
		System.out.println("DYNAMO_ENDPOINT = '" + DYNAMO_ENDPOINT + "'");
		System.out.println("DYNAMO_XML_TABLE_NAME = '" + DYNAMO_SCOPUS_QUERY_TABLE_NAME + "'");
		System.out.println("AWS_ACCESS_KEY = '" + AWS_ACCESS_KEY + "'");
		System.out.println("AWS_SECRET_KEY = '" + AWS_SECRET_KEY + "'");
		System.out.println("CONNECTION_TIMEOUT = '" + CONNECTION_TIMEOUT + "'");
		System.out.println("SOCKET_TIMEOUT = '" + SOCKET_TIMEOUT + "'");
		System.out.println("MAX_ERROR_RETRY = '" + MAX_ERROR_RETRY + "'");
		
	}	
}
