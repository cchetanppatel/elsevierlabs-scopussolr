package com.elsevier.dynamo;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
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
	
	// Column names
	private static final String KEY_COLUMN = "k";
	private static final String QUERY_COLUMN = "q";
	private static final String DOCID_COLUMN = "d";

	
	/**
	 * Main to test some basic functionality for DynamoDB.  Was initially used to make sure a table 
	 * could be created.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Create the table to hold records
		createTable(Variables.DYNAMO_SCOPUS_QUERY_TABLE_NAME);
		
		// Get a record
		//getRecord("auth_1_106364");
		
		// Dump the comparison file
		// R throughput for sdxcr-xml should be temporarily increased to 1600 (really 3200 inconsistent)
		// Actual throughput will be number of shards for DynamoDB (which is unknown).  The specified R
		// will actually be divided by the number of shards to calculate the true throughput.  With this
		// configuration (and limit of 7500) the DynamoDB table was dumped in 5 minutes on a cc.x4large.
		// This was approximately 11.8M records.
		//dumpComparisonFile(new File("/html/dbcomparison"), 7500);

		//spawnCompleteReload(1000);
	}
	
	/**
	 * Create the AWS DynamoDB table using the specified name.  Currently, the primary key
	 * name is hard-coded to "filename" with a type of "String".
	 * 
	 * @param tableName DynamoDB table name
	 */
	public static void createTable(String tableName) {
		

        ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
        .withReadCapacityUnits(5L)
        .withWriteCapacityUnits(100L);
    
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
	 * Insert record into DynamoDB.  
	 * 
	 * @param key (primary key)
	 * @param query  query
	 */
	public static void insertRecord(String key, String query) {
	
        // Add an item
		HashMap<String,String> keyValues = new HashMap<String,String>();
		keyValues.put(KEY_COLUMN, key);
		keyValues.put(QUERY_COLUMN, query);
        Map<String, AttributeValue> item = newItem(keyValues);
        PutItemRequest putItemRequest = new PutItemRequest(Variables.DYNAMO_SCOPUS_QUERY_TABLE_NAME, item);
        adbClient.putItem(putItemRequest);
        
	}
        
	

	
	
	/**
	 * Create a record that can be inserted into DynamoDB.
	 * 
	 * @return Map
	 */
    private static Map<String, AttributeValue> newItem(HashMap<String,String> keyValues) {
    	
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
		Iterator<Map.Entry<String, String>> it = keyValues.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<String,String> pairs = it.next();
	    	item.put(pairs.getKey(), new AttributeValue().withS(pairs.getValue()));
	    }    

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
	 * Get an item (as a serialized string)
	 * 
	 * @param attributeList
	 */
    private static String getItem(Map<String, AttributeValue> attributeList) {
    	
    	StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, AttributeValue> item : attributeList.entrySet()) {
            String attributeName = item.getKey();
            AttributeValue value = item.getValue();
            sb.append(attributeName
                    + " "
                    + (value.getS() == null ? "" : "S=[" + value.getS() + "]")
                    + (value.getN() == null ? "" : "N=[" + value.getN() + "]")
                    + (value.getSS() == null ? "" : "SS=[" + value.getSS() + "]")
                    + (value.getNS() == null ? "" : "NS=[" + value.getNS() + "] \n"));
        }
        return sb.toString();
        
    }
    
    
    /**
     * Helper method that retrieves a record from a DynamoDB table based on the primary key.
     * 
     * @param tableName DynamoDB table name.
     * @param key Primary key value.
     */
    
    public static String getRecord(String key) {
    	
    	HashMap<String, AttributeValue> hmkey = new HashMap<String, AttributeValue>();
    	hmkey.put("k", new AttributeValue().withS(key));
    	GetItemRequest getItemRequest = new GetItemRequest()
        											.withTableName(Variables.DYNAMO_SCOPUS_QUERY_TABLE_NAME)
        											.withKey(hmkey)
        											.withConsistentRead(true);
    	
    	
    	GetItemResult result = adbClient.getItem(getItemRequest);

		Map<String,AttributeValue>resultMap = result.getItem();
		
		if (resultMap == null) {
			
			return key + " not found";
			
		} else {
		
			return getItem(resultMap);
			
		}
		
    }

    
}

	

