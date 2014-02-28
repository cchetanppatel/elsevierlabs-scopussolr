package com.elsevier.sqs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.elsevier.common.Variables;

/**
 * 
 * @author Curt Kohler
 *
 */
public class ScopusBulkLoad {

	// Format for the timestamp
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	public static void main(String[] args) {
		
		// Must be one parameter
		if (args.length != 3 && args.length != 4) {
			System.err.println("Three parameters are required.  The fourth parameter is optional.");
			System.err.println("First parameter is the file containing the Scopus S3 keys.");
			System.err.println("Second parameter is record type to add ('a' for adds, 'u' for updates");
			System.err.println("Third parameter is the version to use. A version of 0 will use the current timestamp as the epoch");
			System.err.println("Fourth (optional) parameter is the number of keys to process.");
	        System.exit(1);
		}

    	
    	System.out.println("*** Start " + sdf.format(new Date()));
    	int ctr = 0;
    	try {
    		
    		BufferedReader br = new BufferedReader(new FileReader(args[0]));
    		String line;
    		List<SendMessageBatchRequestEntry> batch = new ArrayList<SendMessageBatchRequestEntry>();

			 String action = args[1];
			 String prefix = "pfx";
			 String version = args[2];
			 String epoch = args[2];
			 int numberRecordsToProcess = 0;
			 if (args.length == 4) {
				 numberRecordsToProcess = Integer.parseInt(args[3],10);
			 }
			 int numProcessed = 0;
			 
			 long messageEpoch = 0;
			 // should we use the current time as the epoch?
			 if (epoch.contentEquals("0")) {
				 // All messages in this run will have the same epoch value
				 messageEpoch = System.currentTimeMillis();
				 version = "" + messageEpoch;
			 } else {
				 messageEpoch = Long.parseLong(epoch,10);
			 }
			 
			 System.out.println("Configuation based on parameters:");
			 System.out.println("Number of paramaters: " +  args.length);
			 System.out.println("File: " + args[0]);
			 System.out.println("Action: " + args[1]);
			 System.out.println("messageEpoch: " + messageEpoch);
			 System.out.println("numberRecordsToProcess: " + numberRecordsToProcess + " (0 means process all keys in file)");
			 System.out.println("");
			 
    		 while((line = br.readLine()) != null && (numberRecordsToProcess == 0 || numProcessed < numberRecordsToProcess)) {
    			 
    			 numProcessed++;
    			 
    			 // Process a record.  The only thing in a line will be the key.
    			 String key = line.trim();
    			 
				 MessageEntryForXMLProcessing entry = new MessageEntryForXMLProcessing(action, messageEpoch, key, prefix, version);
				 String jsonEntry = MessageForXMLProcessing.toJson(Variables.S3_XML_BUCKET_NAME, entry);
				 SendMessageBatchRequestEntry messageEntry = new SendMessageBatchRequestEntry()
				 													.withMessageBody(jsonEntry)
				 													.withId(Integer.toString(batch.size()));
				 batch.add(messageEntry);
				 ctr++;
				 
				 // Output in batches of 10
    			 if (batch.size() == 10) {
    				 SendMessageBatchResult res = SimpleQueueService.addMessageBatch(Variables.SQS_QUEUE_NAME, batch);
    				 List<BatchResultErrorEntry> failed = res.getFailed();
    				 if (failed.isEmpty() == false) {
    					 Iterator<BatchResultErrorEntry> iter = failed.iterator();
    					 while (iter.hasNext()) {
    						 BatchResultErrorEntry error = iter.next();
    						 System.out.println("*** SQS batch add error: ID: " + error.getId() + ", Code: " + error.getCode() + ", SenderFault: " + error.getSenderFault() + ", Msg: " + error.getMessage());
    					 }
    				 }
    				 batch.clear();
    				 System.out.println("Messages = '" + ctr + "'");
    				 System.out.flush();
    			 }
    		 }
    		 
    		 // Output the last batch if necessary
    		 if (batch.size() > 0) {
    			 SimpleQueueService.addMessageBatch(Variables.SQS_QUEUE_NAME, batch);
    		 }

    		 System.out.println("Total Messages = '" + ctr + "'");
    		 System.out.println("*** End " + sdf.format(new Date()));
    		
    	} catch (Exception ex) {
    		System.out.println("Exception Msg: " + ex.getMessage());
    		ex.printStackTrace(System.out);
    		
    	}
    	
    	catch (Throwable t) {
    		System.out.println("Throwable Msg: " + t.getMessage());
    		System.out.flush();
    		throw new RuntimeException(t);
    	}
		
	}
	
}
