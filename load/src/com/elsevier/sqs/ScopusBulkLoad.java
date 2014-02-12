package com.elsevier.sqs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.elsevier.common.Variables;

/**
 * 
 * @author Darin McBeath
 *
 */
public class ScopusBulkLoad {

	// Format for the timestamp
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	public static void main(String[] args) {
		
		// Must be one parameter
		if (args.length != 2 && args.length != 3) {
			System.err.println("Two parameters are required.  The third parameter is optional.");
			System.err.println("First parameter is the file containing the Scopus S3 keys.");
			System.err.println("Second parameter is the version to use. A version of 0 will use the current timestamp as the epoch");
			System.err.println("Third parameter is the number of keys to process.");
	        System.exit(1);
		}

    	
    	System.out.println("*** Start " + sdf.format(new Date()));
    	int ctr = 0;
    	try {
    		
    		BufferedReader br = new BufferedReader(new FileReader(args[0]));
    		String line;
    		List<SendMessageBatchRequestEntry> batch = new ArrayList<SendMessageBatchRequestEntry>();

			 String action = "a";
			 String prefix = "pfx";
			 String version = args[1];
			 String epoch = args[1];
			 int numberRecordsToProcess = 0;
			 if (args.length == 3) {
				 numberRecordsToProcess = Integer.parseInt(args[2],10);
			 }
			 int numProcessed = 0;
			 
    		 while((line = br.readLine()) != null && (numberRecordsToProcess == 0 || numProcessed < numberRecordsToProcess)) {
    			 
    			 numProcessed++;
    			 
    			 // Process a record.  The only thing in a line will be the key.
    			 String key = line.trim();

    			 long messageEpoch = 0;
    			 // should we use the current time as the epoch?
    			 if (epoch.contentEquals("0")) {
    				 messageEpoch = System.currentTimeMillis();
    				 version = "" + messageEpoch;
    			 } else {
    				 messageEpoch = Long.parseLong(epoch,10);
    			 }
    			 
				 MessageEntryForXMLProcessing entry = new MessageEntryForXMLProcessing(action, messageEpoch, key, prefix, version);
				 String jsonEntry = MessageForXMLProcessing.toJson(Variables.S3_XML_BUCKET_NAME, entry);
				 SendMessageBatchRequestEntry messageEntry = new SendMessageBatchRequestEntry()
				 													.withMessageBody(jsonEntry)
				 													.withId(Integer.toString(batch.size()));
				 batch.add(messageEntry);
				 ctr++;
				 
				 // Output in batches of 10
    			 if (batch.size() == 10) {
    				 SimpleQueueService.addMessageBatch(Variables.SQS_QUEUE_NAME, batch);
    				 batch.clear();
    				 System.out.println("Messages = '" + ctr + "'");
    			 }
    		 }
    		 
    		 // Output the last batch if necessary
    		 if (batch.size() > 0) {
    			 SimpleQueueService.addMessageBatch(Variables.SQS_QUEUE_NAME, batch);
    		 }

    		 System.out.println("Messages = '" + ctr + "'");
    		 System.out.println("*** End " + sdf.format(new Date()));
    		
    	} catch (Exception ex) {
    		
    		ex.printStackTrace();
    		
    	}
		
	}
	
}
