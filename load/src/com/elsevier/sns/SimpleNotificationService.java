package com.elsevier.sns;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

import com.elsevier.common.Variables;

/**
 * Class with static methods to handle interactions with SNS.  
 * 
 * @author Darin McBeath
 *
 */
public class SimpleNotificationService {

	// Create the SNS client
	private static AWSCredentials awsCredentials = new BasicAWSCredentials(Variables.AWS_ACCESS_KEY, Variables.AWS_SECRET_KEY);
	private static ClientConfiguration cCfg = new ClientConfiguration().withConnectionTimeout(Variables.CONNECTION_TIMEOUT).withMaxErrorRetry(Variables.MAX_ERROR_RETRY).withSocketTimeout(Variables.SOCKET_TIMEOUT);
	private static AmazonSNSClient snsClient = new AmazonSNSClient(awsCredentials, cCfg);
	
	// Set the SNS client endpoint
	static { snsClient.setEndpoint(Variables.SNS_ENDPOINT); }

	
	/**
	 * Main to test some basic functionality for SNS.  
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		sendMessage(Variables.SNS_TOPIC_NAME,"Yoo hoo, did you get this message?");

	}
	
	
	/**
	 * Send a message to subscribers.  This is mainly used for notifications
	 * that there is a problem with the system.
	 * 
	 * @param topic SNS topic name
	 * @param msg Message to send
	 */
	public static void sendMessage(String topic, String msg) {
		
		try {
		
			snsClient.publish(new PublishRequest(topic, msg));
			
		} catch (Exception snsex) {
			
			System.out.println("** SNS problem sending message '" + msg + "'");
			snsex.printStackTrace();
			
		}
		
	}
	
}
