package com.elsevier.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import com.amazonaws.services.sqs.model.Message;
import com.elsevier.common.Variables;
import com.elsevier.dynamo.DynamoDB;
import com.elsevier.s3.SimpleStorageService;
import com.elsevier.sns.SimpleNotificationService;
import com.elsevier.redshift.RedShiftService;
import com.elsevier.solr.Document;
import com.elsevier.sqs.MessageEntryJson;
import com.elsevier.sqs.SimpleQueueService;
import com.elsevier.transform.AbstractTransform;



public class SolrCore { 

	// Format for the timestamp 
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	// Wakeup counter
	private static int wakeupCtr = 0;
	
	
	public static void main(String[] args) {
		
		// Must be one parameter
		if (args.length != 1) {
			System.err.println("First Argument must be a directory for the 'pause' file check.");
	        System.exit(1);
		}
		
		// Directory for pause file path is first argument
		String pausePath = args[0]; 
		File pauseFolder = new File(pausePath);
		
		// Specified path must be a directory
		if (!pauseFolder.isDirectory()) {
			System.err.println("First Argument '" + pausePath +"' is not a directory.");
	        System.exit(1);			
		}
		
		// Pause check file 
		File pauseCheckFile = new File(pausePath + File.separatorChar + Variables.PAUSE_FILE_NAME);
		
		Variables.dumpVariables();
		
		HashMap<String,String> metadata = new HashMap<String,String>();
		
		// Infinite loop (keep looking for messages in the SQS queue)
		while (true) {
						
			Message message = null;
			String contentKey = null;
			String contentBucket = null;
			long epoch = 0;
			InputStream s3is = null;
			InputStream is = null;
			String filename = null;
			boolean problems = false;
			
			metadata.clear();
			
			try {
				
				// Check to see if pause file exists
				if (pauseCheckFile.exists()) {
				
					System.out.println("** Processing currently paused.");
			
				} else {
					
					// Read a message from the queue (may want to think about leveraging a 'long' poll)
					message = SimpleQueueService.readQueue(Variables.SQS_QUEUE_NAME);

				}
			
				if (message != null) {
				
					System.out.println("** Processing " + message.getBody() + " from the queue.");
			
					// Parse the message (Jackson) to get key, action, version, epoch
					MessageEntryJson json = new MessageEntryJson(message.getBody());
					
					contentKey = json.getKey();
					contentBucket = json.getBucket();
					epoch = json.getEpoch();
					
					metadata.clear();
					metadata.put(DynamoDB.KEY_COLUMN, json.getKey());	
					metadata.put(DynamoDB.ACTION_COLUMN, json.getAction());
					metadata.put(DynamoDB.EPOCH_COLUMN, Long.toString(json.getEpoch(), 10));
					metadata.put(DynamoDB.VERSION_COLUMN, json.getVersion());

					// Get the current epoch value from DynamoDB and compare with passed in version
					// Skip if the value is less than or equal
					if (epoch <= DynamoDB.getEpoch(contentKey)) {
						System.out.println("** Skipping " + message.getBody());
						// Delete the message
						SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
						continue;
					}
					 
					// Check the action (add,update,delete)
					if (json.getAction().compareTo("d") == 0) {
						
						System.out.println("** Deleting '" + contentKey + "' from the index.");
						// Currently, we catch all exceptions in Document.delete.
						// TODO - think about moving messages to the problem queue
						
						Document.delete(Variables.SOLR_COLLECTION, contentKey, epoch);
											
					} else {

						// Process Add/Update
						System.out.println("** Adding/Update '" + contentKey + "' to the index.");
												
						// Get the record from S3
						s3is = SimpleStorageService.getObject(contentBucket, contentKey);
						
						// Create a local file in tmp and copy the S3 object to this file
						filename = FileUtils.getTempDirectoryPath() + File.separatorChar + DigestUtils.md5Hex(contentKey);
						FileUtils.copyInputStreamToFile(s3is, new File(filename));
						s3is.close();
						s3is = null;
					
						// Parse to extract the bits we need
						is = FileUtils.openInputStream(new File(filename));
						AbstractTransform sdt = new AbstractTransform();
						HashMap<String, Object> fieldValues =  sdt.transform(is);
						
						// Put the requests epoch value into the document at the version of the document
						// so Solr can filter out stale requests.
						fieldValues.put("epoch", Long.toString(epoch, 10) );
						// Making fastload date the same as the epoch per Darin's email.
						fieldValues.put("fastloaddate", Long.toString(epoch, 10) );
						
						//Debug ... output the keys/values to see if we did it right
						for (String key:fieldValues.keySet()) {
							
							Object val = fieldValues.get(key);
							if (val instanceof String) {
								System.out.println("key="+ key +" val=" + (String)val);
							} else if (val instanceof ArrayList<?>){
								System.out.println("key=" + key);
								ArrayList<String> vals = (ArrayList<String>)val;
								Iterator<String> it = vals.iterator();
								System.out.println("  [");
								while (it.hasNext()) {
									System.out.print("    " + it.next());
									if (it.hasNext()) {
										System.out.println(",");
									} else {
										System.out.println();
									}
								}
								System.out.println("  ]");
							}
							
						}  
						// Populate the ElasticSearch index
						Document.add(Variables.SOLR_COLLECTION, fieldValues, contentKey, epoch);							
						
						if (Variables.AWS_REDSHIFT_INTEGRATE_REDSHIFT.equals("true")) {
							// Get core id
							String key = (String)fieldValues.get("intid");
							// Get author ids
							if (fieldValues.containsKey("authid") == true) {
								Object vals = fieldValues.get("authid");
								if (vals instanceof String) {
									ArrayList<String> workVals = new ArrayList<String>();
									workVals.add((String)vals);
									RedShiftService.replaceRecord(Variables.AWS_REDSHIFT_AUTH_CNT_TABLE, key, workVals);
								} else if (vals instanceof ArrayList<?>) {
									RedShiftService.replaceRecord(Variables.AWS_REDSHIFT_AUTH_CNT_TABLE, key, (ArrayList<String>)vals);
								}
							}
							
							// Get affiliation ids
							if (fieldValues.containsKey("afid") == true) {
								Object vals = fieldValues.get("afid");
								if (vals instanceof String) {
									ArrayList<String> workVals = new ArrayList<String>();
									workVals.add((String)vals);
									RedShiftService.replaceRecord(Variables.AWS_REDSHIFT_AFFIL_CNT_TABLE, key, workVals);
								} else if (vals instanceof ArrayList<?>) {
									RedShiftService.replaceRecord(Variables.AWS_REDSHIFT_AFFIL_CNT_TABLE, key, (ArrayList<String>)vals);
								}
							}
							// Get reference ids
							
							// Update the author counts
							
							
							// Update the affilliation counts
							
							
							// Update the reference counts
							
						}
					}
					
					// Update DynamoDB
					DynamoDB.insertRecord(metadata);
					
					// Delete the message
					SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
					
					
				} else {
					
										
					// Sleep and check again
					try {
						Thread.sleep(Variables.SOLR_SEARCH_SLEEP_TIME);
						wakeupCtr++;			
						
						// Only output a message every 5 wakeups
						if (wakeupCtr % 5 == 0) {
							Calendar date = Calendar.getInstance();
							System.out.println("Waking up ... " + sdf.format(date.getTime()));
						}
						
					} catch (InterruptedException ie) {
						
						System.out.println("Oops, we got interrupted.  Don't think this should really happen.");
						
					}
					
				}
			
			} catch (Exception ex) {
				
				// Problems encountered
				problems = true;
				
				ex.printStackTrace();
							
			} finally {

				try {
					
					if (s3is != null) {
						s3is.close();
					}
				
					if (is != null) {
						is.close();
					}
				
					if (filename != null) {
						File f = new File(filename);
						if (f.isFile()) {
							f.delete();
						}
					}
				
				} catch (IOException e) {
					
					e.printStackTrace();
					
				}
				
				if (message != null) {
					
					if (problems) {
						
						// Send SNS message
						String prob = "** Problems processing " + message.getBody() + ".";
						System.out.println(prob);
						//SimpleNotificationService.sendMessage(Variables.SNS_TOPIC_NAME, prob); 
					
						// Put message on the problem queue (for later processing)
						try {
						
							System.out.println("** SQS  Adding " + message.getBody() + " to '" + Variables.SQS_PROBLEM_QUEUE_NAME + "' queue.");
							SimpleQueueService.addmessage(Variables.SQS_PROBLEM_QUEUE_NAME, message.getBody());
						
						} catch (Exception exc) {
						
							String msg = "** SQS Problems with adding " + message.getBody() + " to the the '" + Variables.SQS_PROBLEM_QUEUE_NAME + "' queue.";
							System.out.println(msg);
							exc.printStackTrace();
							SimpleNotificationService.sendMessage(Variables.SNS_TOPIC_NAME, msg); 
						
						}
						
					}
					
					// Delete the message
					try {
						
						System.out.println("** SQS  Removing " + message.getBody() + " from the queue.");
						SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
					
					} catch (Exception ex) {
						
						String msg = "** SQS Problems with removing " + message.getBody() + " from the queue.";
						System.out.println(msg);
						ex.printStackTrace();
						SimpleNotificationService.sendMessage(Variables.SNS_TOPIC_NAME, msg);
						
					}

					
				} 
			
			
			} 
		
		}

	}
	
}
