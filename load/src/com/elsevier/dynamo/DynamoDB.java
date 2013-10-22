package com.elsevier.dynamo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TableStatus;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.elsevier.common.Variables;

/**
 * Class with static methods to handle interactions with DynamoDB.  The class contains a combination of
 * administration capabilities (such as createTable and its supporting methods) and feature
 * capabilities (such as insertRecord).  It might make sense to refactor the single
 * class into 2 classes.  One for administration capabilities and one for users that want to insert
 * records into a table.
 * 
 * @author Darin McBeath
 *
 */
public class DynamoDB {
	
	// Create the DynamoDB client
	private static AWSCredentials awsCredentials = new BasicAWSCredentials(Variables.AWS_ACCESS_KEY, Variables.AWS_SECRET_KEY);
	private static ClientConfiguration cCfg = new ClientConfiguration().withConnectionTimeout(Variables.CONNECTION_TIMEOUT).withMaxErrorRetry(Variables.MAX_ERROR_RETRY).withSocketTimeout(Variables.SOCKET_TIMEOUT);
	private static AmazonDynamoDBClient adbClient = new AmazonDynamoDBClient(awsCredentials, cCfg);
	
	// Set the DynamoDB client endpoint
	static { adbClient.setEndpoint(Variables.DYNAMO_ENDPOINT); }
	
	// Format for the timestamp to store in DynamoDB
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	// Column names
	public static final String KEY_COLUMN = "k";
	public static final String EPOCH_COLUMN = "e";
	public static final String ACTION_COLUMN = "a";
	public static final String TIMESTAMP_COLUMN = "t";
	public static final String PREFIX_COLUMN = "p";
	public static final String VERSION_COLUMN = "v";
	
	
	/**
	 * Main to test some basic functionality for DynamoDB.  Was initially used to make sure a table 
	 * could be created.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Create the xml table to hold information about the xml files.
		createXmlTable(Variables.DYNAMO_SOLR_SEARCH_TABLE_NAME);

	}

	
	/**
	 * Create the AWS DynamoDB table using the specified name.  
	 * This table will store metadata about the various xml files that have been processed.
	 * 
	 * @param tableName DynamoDB table name
	 */
	public static void createXmlTable(String tableName) {
		    
        // Provide the initial provisioned throughput values as Java long data types
        // The Read/Write Capacities will need to be adjusted (use the AWS Console).
        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
            .withReadCapacityUnits(Variables.DYNAMO_SOLR_SEARCH_TABLE_READ_CAPACITY)
            .withWriteCapacityUnits(Variables.DYNAMO_SOLR_SEARCH_TABLE_WRITE_CAPACITY);
        
        CreateTableRequest request = new CreateTableRequest()
            .withTableName(tableName)
            .withKeySchema(new KeySchemaElement().withAttributeName(KEY_COLUMN).withKeyType(KeyType.HASH))
            .withAttributeDefinitions(new AttributeDefinition().withAttributeName(KEY_COLUMN).withAttributeType(ScalarAttributeType.S))
            .withProvisionedThroughput(provisionedThroughput);
        
        adbClient.createTable(request);
        
        waitUntilTableReady(tableName);        

        getTableInformation(tableName);
        
	}

	
	/**
	 * Get information for the specified table.  Useful for debugging.
	 * 
	 * @param tableName DynamoDB table name
	 */
	private static void getTableInformation(String tableName) {
		
        TableDescription tableDescription = adbClient.describeTable(
                new DescribeTableRequest().withTableName(tableName)).getTable();
        		System.out.format("Name: %s:\n" +
                "Status: %s \n" + 
                "Provisioned Throughput (read capacity units/sec): %d \n" +
                "Provisioned Throughput (write capacity units/sec): %d \n",
                tableDescription.getTableName(),
                tableDescription.getTableStatus(),
                tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
                tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
        
    }
	
	
	/**
	 * Wait for the DynamoDB table to become ready.  Used as part of the create table logic.
	 * 
	 * @param tableName  DynamoDB table name
	 */
	private static void waitUntilTableReady(String tableName) {
		
        System.out.println("Waiting for " + tableName + " to become ACTIVE...");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (10L * 60L * 1000L);  // Wait up to 10 minutes
        while (System.currentTimeMillis() < endTime) {
            try {Thread.sleep(1000L * 20L);} catch (Exception e) {}
            try {
                TableDescription tableDescription = adbClient.describeTable(new DescribeTableRequest().withTableName(tableName)).getTable();
                String tableStatus = tableDescription.getTableStatus();
                
                System.out.println("  - current state: " + tableStatus);
                
                if (tableStatus.equals(TableStatus.ACTIVE.toString())) {
                    return;
                }
            } catch (AmazonServiceException ase) {
                // Describe table is eventual consistent.
                if (ase.getErrorCode().equalsIgnoreCase("ResourceNotFoundException") == false) {
                    throw ase;
                }
            }
        }
        throw new RuntimeException("Table " + tableName + " never went active");
        
    }
	

	/**
	 * Insert record into DynamoDB.  These records are inserted into the 'xml' table.
	 * 
	 * @param keyValues - HashMap of values to be inserted into the table (all must be 'S')
	 */
	public static void insertRecord(HashMap<String,String> keyValues) {
	
        // Add an item
        Map<String, AttributeValue> item = newItem(keyValues);
        PutItemRequest putItemRequest = new PutItemRequest(Variables.DYNAMO_SOLR_SEARCH_TABLE_NAME, item);
        adbClient.putItem(putItemRequest);
        
	}
        
	
	/**
	 * Get the epoch from the content record.  If the record doesn't exist, a value of 0 will be returned.
	 * 
	 * @param primary key
	 * @return epoch
	 */
	public static long getEpoch(String key) {
		
		HashMap<String,AttributeValue> map = new HashMap<String,AttributeValue>();
		map.put(KEY_COLUMN, new AttributeValue(key));
		GetItemResult result = adbClient.getItem(new GetItemRequest(Variables.DYNAMO_SOLR_SEARCH_TABLE_NAME, map).withConsistentRead(true));
		Map<String,AttributeValue>resultMap = result.getItem();
		
		if (resultMap == null) {
			
			return 0;
			
		} else {
		
			AttributeValue av = resultMap.get(EPOCH_COLUMN);
		
			if (av == null) {
			
				return 0;
			
			} else {
			
				return Long.parseLong(av.getS(),10);
			
			}
			
		}
			
	}
	
	
	/**
	 * Create a record that can be inserted into DynamoDB 
	 * 
	 * @param keyValues HashMap of values to be inserted into the table (all must be 'S')
	 * @return Map
 */
    private static Map<String, AttributeValue> newItem(HashMap<String,String> keyValues) {
    	
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		Iterator<Map.Entry<String, String>> it = keyValues.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<String,String> pairs = it.next();
	    	item.put(pairs.getKey(), new AttributeValue().withS(pairs.getValue()));
	    }       
		item.put(TIMESTAMP_COLUMN, new AttributeValue().withS(sdf.format(new Date())));
        return item;

    }
	
		
	/**
	 * Print an item.
	 * 
	 * @param attributeList
	 */
    private static void printItem(Map<String, AttributeValue> attributeList) {
    	
        for (Map.Entry<String, AttributeValue> item : attributeList.entrySet()) {
            String attributeName = item.getKey();
            AttributeValue value = item.getValue();
            System.out.println(attributeName
                    + " "
                    + (value.getS() == null ? "" : "S=[" + value.getS() + "]")
                    + (value.getN() == null ? "" : "N=[" + value.getN() + "]")
                    + (value.getSS() == null ? "" : "SS=[" + value.getSS() + "]")
                    + (value.getNS() == null ? "" : "NS=[" + value.getNS() + "] \n"));
        }
        
    }	
    

    /**
     * Helper method that retrieves a record from a DynamoDB table based on the primary key.
     * 
     * @param tableName DynamoDB table name.
     * @param keyName Primary key field name
     * @param keyValue Primary key value.
     */
    public static void getRecord(String tableName, String keyName, String keyValue) {

		HashMap<String,AttributeValue> map = new HashMap<String,AttributeValue>();
		map.put(keyName, new AttributeValue(keyValue));
		GetItemResult result = adbClient.getItem(new GetItemRequest(tableName, map).withConsistentRead(true));
		Map<String,AttributeValue>resultMap = result.getItem();
		
		if (resultMap == null) {
			
			System.out.println("Record not found");
			
		} else {
		
			printItem(resultMap);
			
		}
		
    }
    
}
