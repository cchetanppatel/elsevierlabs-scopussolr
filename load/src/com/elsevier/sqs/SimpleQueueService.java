package com.elsevier.sqs;


import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.elsevier.common.Variables;


/**
 * Class with static methods to handle interactions with SQS.  The main is used for testing/debugging.
 * The remaining methods would be used by other classes.
 * 
 * @author Darin McBeath
 *
 */
public class SimpleQueueService {

	// Create SQS client
	private static AWSCredentials awsCredentials = new BasicAWSCredentials(Variables.AWS_ACCESS_KEY, Variables.AWS_SECRET_KEY);
	private static ClientConfiguration cCfg = new ClientConfiguration().withConnectionTimeout(Variables.CONNECTION_TIMEOUT).withMaxErrorRetry(Variables.MAX_ERROR_RETRY).withSocketTimeout(Variables.SOCKET_TIMEOUT);
	private static AmazonSQS sqsClient = new AmazonSQSClient(awsCredentials, cCfg);
	
	// Set the SQS client endpoint
	static { sqsClient.setEndpoint(Variables.SQS_ENDPOINT); }
	
	// SQS Queue URL hashmap
	private static HashMap<String,String> queueUrlMap = new HashMap<String,String>();
	
	/**
	 * Main to test some basic functionality for SQS.  Used to make sure a queue 
	 * can be created, and items could be added/read/deleted.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        
        try {
         	
        	// Create a SQS queue
        	createQueue(Variables.SQS_QUEUE_NAME);
        	
        	// Create a SQS problem queue
        	//createQueue(Variables.SQS_PROBLEM_QUEUE_NAME);
        	
        	// Process problem queue
        	//processProblemQueue();
        	
        	// Populate queue
        	//populateQueue();
        	

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered " +
                    "a serious internal problem while trying to communicate with SQS, such as not " +
                    "being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
	}

	
	/**
	 * Create the AWS SQS queue using the specified name.
	 * 
	 * @param qName SQS queue name
	 */
	public static void createQueue(String qName) {
		
		System.out.println("** Creating the '" + qName + "' queue.\n");
		
        // Create a SQS queue
        CreateQueueRequest createQueueRequest = new CreateQueueRequest(qName);
        HashMap<String,String> attributes = new HashMap<String,String>();
        
        // Set message retention to 2 weeks
        attributes.put("MessageRetentionPeriod", "1209600");
        
        // Set message processing time to 4 hours
        attributes.put("VisibilityTimeout", "1800");
        
        createQueueRequest.withAttributes(attributes);
        sqsClient.createQueue(createQueueRequest);

	}
	
	
	/**
	 * Read one message from the specified AWS SQS queue.
	 * 
	 * @param qName SQS queue name
	 */
	public static Message readQueue(String qName) {

		// Note:  Running into Java bug that limits the number of Readers that can be generated
		// from a single XMLInputFactory (basically what happens in the AWS client). It becomes a 
		// problem for long running processes with a static client that do a lot of calls. In order 
		// to get around it, we create now the client new for each request even though it is less efficient for now.
		AmazonSQS sqsClient = new AmazonSQSClient(awsCredentials, cCfg);
		
		//System.out.println("** Reading one message from the '" + qName + "' queue.\n");
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(getQueueUrl(qName));
        
        // Make sure only one message is read (even though that is the default)
        receiveMessageRequest.setMaxNumberOfMessages(1);
        
        // Set how long the message is 'invisible' in the queue (in seconds).  In other words, this is how long
        // the application has to process this message before it is automatically re-added back to the queue.
        // This value is currently set to 30 minutes.
        receiveMessageRequest.setVisibilityTimeout(1800);
        
        // Implement long polling (20 seconds)
        receiveMessageRequest.setWaitTimeSeconds(20);
        
        List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).getMessages();
        if (messages.size() == 1) {
        	return messages.get(0);
        }
        return null;    
        
	}

	
	/**
	 * Populate the specified SQS queue with the message.  
	 * 
	 * @param qName SQS queue name
	 * @param msg Message added to the queue
	 */
	public static SendMessageResult addmessage(String qName, String msg) {
		// Note:  Running into Java bug that limits the number of Readers that can be generated
		// from a single XMLInputFactory (basically what happens in the AWS client). It becomes a 
		// problem for long running processes with a static client that do a lot of calls. In order 
		// to get around it, we create now the client new for each request even though it is less efficient for now.
		//AmazonSQS sqsClient = new AmazonSQSClient(awsCredentials, cCfg);
		SendMessageResult res = sqsClient.sendMessage(new SendMessageRequest(getQueueUrl(qName), msg));
		return res;
	}
	
	/**
	 * Populate the specified SQS queue with the batch of messages.  There is currently a
	 * limit of 10 messages per batch.
	 * 
	 * @param qName SQS queue name
	 * @param batch Messages to add to the queue
	 */
	public static SendMessageBatchResult addMessageBatch(String qName, List<SendMessageBatchRequestEntry> batch) {
		// Note:  Running into Java bug that limits the number of Readers that can be generated
		// from a single XMLInputFactory (basically what happens in the AWS client). It becomes a 
		// problem for long running processes with a static client that do a lot of calls. In order 
		// to get around it, we create now the client new for each request even though it is less efficient for now.
		//AmazonSQS sqsClient = new AmazonSQSClient(awsCredentials, cCfg);
        SendMessageBatchResult res = sqsClient.sendMessageBatch(new SendMessageBatchRequest(getQueueUrl(qName), batch));
        return res;
	}
	
	
	/**
	 * Delete the message from the specified AWS SQS queue.
	 * 
	 * @param qName SQS queue name
	 * @param message Message to delete
	 */
	public static void deleteMessage(String qName, Message message) {
        
        sqsClient.deleteMessage(new DeleteMessageRequest(getQueueUrl(qName), message.getReceiptHandle()));
        
	}
	
	
	/**
	 * Output information about the message.  Useful for debugging.
	 * 
	 * @param message Message to output
	 */
	private static void outputMessage(Message message) {
		
        System.out.println("  Message");
        System.out.println("    MessageId:     " + message.getMessageId());
        System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
        System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
        System.out.println("    Body:          " + message.getBody());
        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
            System.out.println("  Attribute");
            System.out.println("    Name:  " + entry.getKey());
            System.out.println("    Value: " + entry.getValue());
        }
        
	}
	
	
	/**
	 * Get the URL associated with the AWS SQS queue. Helper method used by the other methods
	 * in the class.
	 * 
	 * @param qName SQS queue name
	 * @return Queue URL
	 */
	private static String getQueueUrl(String qName) {
		
		// Is this in our cache
		if (queueUrlMap.containsKey(qName)) {
			
			return queueUrlMap.get(qName);
			
		} else {
			
			GetQueueUrlRequest queueURLReq = new GetQueueUrlRequest(); 
	        queueURLReq.setQueueName(qName);
	        GetQueueUrlResult queueURLResult = sqsClient.getQueueUrl(queueURLReq);
	        synchronized(SimpleQueueService.class) {
	        	queueUrlMap.put(qName, queueURLResult.getQueueUrl());
	        }
	        return queueUrlMap.get(qName);
	        
		}
        
	}
	
	
	/**
	 * Copy messages from the problem queue and place them on the
	 * queue to be reprocessed again.  If the copy is successful, then remove
	 * the message from the problem-queue.
	 */
	private static void processProblemQueue() {
		
		Message message = readQueue(Variables.SQS_PROBLEM_QUEUE_NAME);
		
		while (message != null) {
			
			// Get the message body
			String body = message.getBody();
			
			// Add the message to the zip queue
			addmessage(Variables.SQS_QUEUE_NAME, body);
			
			// Delete the message from the problem queue
			deleteMessage(Variables.SQS_PROBLEM_QUEUE_NAME, message);
			
			// Get the next message from the problem queue
			message = readQueue(Variables.SQS_PROBLEM_QUEUE_NAME);
			
		}
		
	}
	
	
	private static void populateQueue() {
		
		// Affiliation records
		/*
		String msg = "{" +
			           "\"bucket\": \"affiliation-xml\"," +
			           "\"entries\": [{" +
			           "\"key\": \"101940415\"," +
			           //"\"key\": \"100242197\"," +
			           "\"prefix\": \"10-s2.0\"," +
			           "\"action\": \"a\"," +
			           "\"epoch\": 14," +
			           "\"version\": \"2008-02-07T02:28:34.797307-05:00\"" +
			           "}]" +
			          "}";
		*/
		String msg = "{" +
        				"\"bucket\": \"author-xml\"," +
        				"\"entries\": [{" +
        				"\"key\": \"10038761700\"," +
        				"\"prefix\": \"9-s2.0\"," +
        				"\"action\": \"a\"," +
        				"\"epoch\": 8," +
        				"\"version\": \"2008-02-07T02:28:34.797307-05:00\"" +
        				"}]" +
        			  "}";
		
		sqsClient.sendMessage(new SendMessageRequest(getQueueUrl(Variables.SQS_QUEUE_NAME), msg));
		
	}

}
