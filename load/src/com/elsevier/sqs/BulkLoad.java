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
 * Assumes DDBS3Compare has been run with output format of 'full'
 * Split the file using linux split command to get parallel execution
 * Modify output files
 * Run a script
 * 
 * @author Darin McBeath
 *
 */
public class BulkLoad {

	// Format for the timestamp
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	public static void main(String[] args) {
		
		// Must be one parameter
		if (args.length != 1) {
			System.err.println("One parameter is required.");
			System.err.println("First parameter is the file containing output from DynamoDB 'full'.");
	        System.exit(1);
		}

    	
    	System.out.println("*** Start " + sdf.format(new Date()));
    	int ctr = 0;
    	try {
    		
    		BufferedReader br = new BufferedReader(new FileReader(args[0]));
    		String line;
    		List<SendMessageBatchRequestEntry> batch = new ArrayList<SendMessageBatchRequestEntry>();
    		
    		 while((line = br.readLine()) != null) {
    			 
    			 // Process a record ... it MUST be in formation action,prefix,key,version,epoch
    			 String toks[] = line.split(",");
    			 String action = toks[0];
    			 String prefix = toks[1];
    			 String key = toks[2];
    			 String version = toks[3];
    			 String epoch = toks[4];
    			 
				 MessageEntryForXMLProcessing entry = new MessageEntryForXMLProcessing(action, Long.parseLong(epoch,10), key, prefix, version);
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
