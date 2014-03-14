package com.elsevier.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import com.amazonaws.services.sqs.model.Message;
import com.elsevier.common.Variables;
import com.elsevier.redshift.RedshitUpdateMessageEntryJson;
import com.elsevier.sns.SimpleNotificationService;
import com.elsevier.solr.Document;
import com.elsevier.sqs.MessageEntryJson;
import com.elsevier.sqs.SimpleQueueService;

public class SolrAuthorCntUpdate {

	// Format for the timestamp
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss a");

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
			System.err.println("First Argument '" + pausePath + "' is not a directory.");
			System.exit(1);
		}

		// Pause check file
		File pauseCheckFile = new File(pausePath + File.separatorChar + Variables.PAUSE_FILE_NAME);

		Variables.dumpVariables();

		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		
		// Infinite loop (keep looking for messages in the SQS queue)
		while (true) {
			Message message = null;
			boolean problems = false;
			
			try {
				// Check to see if pause file exists
				if (pauseCheckFile.exists()) {
				
					System.out.println("** Processing currently paused.");
			
				} else {
					
					// Read a message from the queue (may want to think about leveraging a 'long' poll)
					message = SimpleQueueService.readQueue(Variables.AWS_REDSHIFT_UPDATE_QUEUE);
					
					RedshitUpdateMessageEntryJson updateEntry = new RedshitUpdateMessageEntryJson(message.getBody());

				}
				
				if (message != null) {
					
					System.out.println("** Processing " + message.getBody() + " from the queue.");
					
					// Parse the message (Jackson) to get id, cnt, and Redshift epoch
					RedshitUpdateMessageEntryJson updateEntry = new RedshitUpdateMessageEntryJson(message.getBody());
				
					fieldValues.clear();
					
					String id = updateEntry.getId();
					long epochRS = updateEntry.getRs_epoch();
					String cnt = "" + updateEntry.getCnt();
					fieldValues.put("count", cnt);
					//fieldValues.put("authid", contentKey);
					Document.update(Variables.SOLR_COLLECTION, fieldValues, "id", id, "epoch-rs", epochRS );
					
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
			}
			catch (Exception e) {
				System.out.println("Exception: " + e.getLocalizedMessage());
				e.printStackTrace(System.out);
			}
			finally {
				if (message != null) {
					
					try {
						System.out.println("** SQS  Removing " + message.getBody() + " from the queue.");
						SimpleQueueService.deleteMessage(Variables.AWS_REDSHIFT_UPDATE_QUEUE, message);
					} 
					catch (Exception e) {
						String msg = "** SQS Problems with removing " + message.getBody() + " from the queue.";
						System.out.println(msg);
						e.printStackTrace(System.out);
						System.out.flush();
						SimpleNotificationService.sendMessage(Variables.SNS_TOPIC_NAME, msg);
					}
				}
			}
		}
		
	}

	public static void main1(String[] args) {
		// TODO Auto-generated method stub

		// Make up a new count
		long epoch = System.currentTimeMillis();

		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("count", "10");
		String idFieldName = "authid";
		String id = "36135960300";

		try {
			Document.update(Variables.SOLR_COLLECTION, fieldValues,
					idFieldName, id, "epoch-rs", epoch);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}

}
