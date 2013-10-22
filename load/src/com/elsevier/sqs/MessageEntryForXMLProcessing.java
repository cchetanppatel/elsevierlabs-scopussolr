package com.elsevier.sqs;

/**
 * Individual entry in a message.  
 * 
 * @author Darin McBeath
 *
 */
public class MessageEntryForXMLProcessing {

	private String action;
	private long epoch;
	private String key;
	private String prefix;
	private String version;
	
	/*
	 * Create a message entry that will later be added to a queue.
	 * 
	 * @param theAction Indicator whether this is an update, add, or delete
	 * @param theEpoch Epoch value associated with the object (higher value, more recent)
	 * @param theKey key for the object stored in S3
	 * @param thePrefix Value before the PII in the EID (allows one to resurrect the full EID).
	 * @param theVersion Version value associated with the object (ew transaction id)
	 * 
	 */
	public  MessageEntryForXMLProcessing(String theAction, long theEpoch, String theKey, String thePrefix, String theVersion) {
		
		action = theAction;
		epoch = theEpoch;
		key = theKey;
		prefix = thePrefix;
		version = theVersion;
		
	}
	
	
	/**
	 * Construct a serialized (json) form of the object.
	 * 
	 * @return json string
	 */
	public String toJson() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"" + MessageConstants.KEY + "\" : ");
		sb.append("\"" + key + "\", ");
		sb.append("\"" + MessageConstants.PREFIX + "\" : ");
		sb.append("\"" + prefix + "\", ");
		sb.append("\"" + MessageConstants.ACTION + "\" : ");
		sb.append("\"" + action + "\", ");
		sb.append("\"" + MessageConstants.EPOCH + "\" : ");
		sb.append(epoch + ", ");
		sb.append("\""+ MessageConstants.VERSION + "\" : ");
		sb.append("\"" + version + "\"");
		sb.append(" }");
		return sb.toString();
		
	}
}
