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
import com.elsevier.solr.Document;
import com.elsevier.sqs.MessageEntryJson;
import com.elsevier.sqs.SimpleQueueService;
import com.elsevier.transform.AuthorTransform;



public class SolrAuthor {

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
						System.out.flush();
						
						// Get the record from S3
						s3is = SimpleStorageService.getObject(contentBucket, contentKey);
						
						// Create a local file in tmp and copy the S3 object to this file
						filename = FileUtils.getTempDirectoryPath() + File.separatorChar + DigestUtils.md5Hex(contentKey);
						FileUtils.copyInputStreamToFile(s3is, new File(filename));
						s3is.close();
						s3is = null;
					
						// Parse to extract the bits we need
						is = FileUtils.openInputStream(new File(filename));
						AuthorTransform sdt = new AuthorTransform();
						HashMap<String, Object> fieldValues =  sdt.transform(is);
						
						// Put the requests epoch value into the document at the version of the document
						// so Solr can filter out stale requests.
						fieldValues.put("epoch", Long.toString(epoch, 10) );
						fieldValues.put("epoch-rs", Long.toString(epoch, 10) );
						
						//Debug ... output the keys/values to see if we did it right
						/*
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
						*/
						
						// Is this an add?
						if (json.getAction().compareTo("a") == 0) {
							fieldValues.put("id", contentKey); 
							fieldValues.put("epoch", Long.toString(epoch, 10) );   // Put the XFab epoch in the index
							fieldValues.put("epoch-rs", Long.toString(epoch, 10) ); // actual version control value is the epoch for adds
							//fieldValues.put("count", "-1");   // Dummy value for count until Redshift job values comes back with one.  Value of -1 will make non-updated records easy to find 
							fieldValues.put("count", "5");
							Document.add(Variables.SOLR_COLLECTION, fieldValues, contentKey, epoch);
						} else {  // Must be an update...
							fieldValues.put("epoch", Long.toString(epoch, 10) );
							Document.update(Variables.SOLR_COLLECTION, fieldValues, "id", contentKey, "epoch-rs", epoch);
						}					
						
					}
					
					// Update DynamoDB
					DynamoDB.insertRecord(metadata);

					// Delete the message
					//System.out.println("Deleting SQS for key: " + contentKey);
					//SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
					//System.out.println("Deleted SQS for key: " + contentKey);
					
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
				System.out.println("*** Exception in mainline..  key: " + contentKey);
				ex.printStackTrace(System.out);
				System.out.flush();
							
			} catch (Throwable e) {
				System.out.println("*** Error in mainline..   key: " + contentKey);
				// Try to make sure there is some memory available before getting stack trace.
				System.gc();
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException ie) {
					System.out.println("Interrupted in Throwable catch block.");
				}
				
		        e.printStackTrace(System.out);
		        System.out.flush();
		        throw new RuntimeException(e);
		    }
			finally {

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
					System.out.println("IOException in main finally block");
					e.printStackTrace(System.out);
					System.out.flush();
					
				}
				
				if (message != null) {
					
					if (problems) {
						
						// Send SNS message
						String prob = "** Problems processing " + message.getBody() + ".";
						System.out.println(prob);
						System.out.flush();
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
						catch (Throwable t) {
							System.out.println("*** Error in writing SQS error message..   key: " + contentKey);
							// Try to make sure there is some memory available before getting stack trace.
							System.gc();
							try {
								Thread.sleep(5000);
							}
							catch (InterruptedException ie) {
								System.out.println("Interrupted in finally's write SQS error record catch block.");
							}
							
					        t.printStackTrace(System.out);
					        System.out.flush();
					        throw new RuntimeException(t);
					    }
						
					}
					
					// Delete the message
					try {
						
						System.out.println("** SQS  Removing " + message.getBody() + " from the queue.");
						System.out.flush();
						SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
					
					} catch (Exception ex) {
						
						String msg = "** SQS Problems with removing " + message.getBody() + " from the queue.";
						System.out.println(msg);
						ex.printStackTrace(System.out);
						System.out.flush();
						SimpleNotificationService.sendMessage(Variables.SNS_TOPIC_NAME, msg);
						
					}
					catch (Throwable t) {
						System.out.println("*** Error in finally..   key: " + contentKey);
						// Try to make sure there is some memory available before getting stack trace.
						System.gc();
						try {
							Thread.sleep(5000);
						}
						catch (InterruptedException ie) {
							System.out.println("Interrupted in Finally Throwable catch block.");
						}
						
				        t.printStackTrace(System.out);
				        System.out.flush();
				        throw new RuntimeException(t);
				    }

					
				} 
			
			
			} 
		
		}

	}
	
}
