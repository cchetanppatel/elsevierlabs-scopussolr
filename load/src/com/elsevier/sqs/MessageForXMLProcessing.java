package com.elsevier.sqs;

import java.util.HashMap;

/**
 * Class with static method for constructing a SQS message.  
 * 
 * @author Darin McBeath
 *
 */
public class MessageForXMLProcessing {

	/**
	 * Static method for constructing an xml message for processing.  This will ultimately be added to a SQS queue.
	 * 
	 * @param bucketName S3 bucket name
	 * @param entries List to pick from when creating the message
	 * @param offset Starting offset within the entries
	 * @param max Maximum number of entries to return
	 * 
	 * @return Serialized message (as a json string)
	 */
	public static String toJson(String bucketName, HashMap<String,MessageEntryForXMLProcessing> entries, int offset, int max) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"" + MessageConstants.BUCKET + "\" : ");
		sb.append("\"" + bucketName + "\", ");
		sb.append("\"" + MessageConstants.ENTRIES + "\" : [");
		
		String[] items = new String[entries.size()];
		items = (String[])(entries.keySet().toArray(items));
		
		for (int i = offset; i < offset + max && i < items.length; i++) {
			if (i != offset) {
				sb.append(", ");
			}
			sb.append(entries.get(items[i]).toJson());
		}
		
		sb.append(" ]");
		sb.append(" }");
		return sb.toString();
		
	}
	
	/**
	 * Static method for constructing an xml message for processing.  This will ultimately be added to a SQS queue.
	 * 
	 * @param bucketName S3 bucket name
	 * @param entry
	 * 
	 * @return Serialized message (as a json string)
	 */
	public static String toJson(String bucketName, MessageEntryForXMLProcessing entry) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"" + MessageConstants.BUCKET + "\" : ");
		sb.append("\"" + bucketName + "\", ");
		sb.append("\"" + MessageConstants.ENTRIES + "\" : [");
		sb.append(entry.toJson());		
		sb.append(" ]");
		sb.append(" }");
		return sb.toString();
		
	}
}
