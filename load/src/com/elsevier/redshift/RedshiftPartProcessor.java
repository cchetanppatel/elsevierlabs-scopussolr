package com.elsevier.redshift;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.elsevier.common.Variables;
import com.elsevier.s3.SimpleStorageService;
import com.elsevier.sqs.MessageConstants;
import com.elsevier.sqs.SimpleQueueService;

public class RedshiftPartProcessor {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
	
	public static void main(String[] args) {

		Message message = null;
		String partKey = null;
		String partBucket = null;
		long epoch = 0;
		
		
		InputStream s3is = null;
		InputStream gzis = null;
		String filename = null;
		boolean problems = false;
		
		JsonNode rootNode;
		ObjectMapper mapper = new ObjectMapper();
		
		Variables.dumpVariables();
		
		System.out.println("Starting part processing " + sdf.format(new Date()));
		int ctr = 0;
		
		try {
			
			// Read a message from the queue (may want to think about leveraging a 'long' poll)
			message = SimpleQueueService.readQueue(Variables.AWS_REDSHIFT_MANIFEST_PARTS_QUEUE);
			
			if (message != null) {
				RedshiftPartMessageEntryJson partEntry = new RedshiftPartMessageEntryJson(message.getBody());
			
				System.out.println("partEntry: " + partEntry.getBucket() + " " + partEntry.getKey());
			
				partKey = partEntry.getKey();
				partBucket =  partEntry.getBucket();
			
				// Get the record from S3
				s3is = SimpleStorageService.getObject(partBucket, partKey);
			
				// Create a local file in tmp and copy the S3 object to this file
				filename = FileUtils.getTempDirectoryPath() + File.separatorChar + DigestUtils.md5Hex(partKey);
				FileUtils.copyInputStreamToFile(s3is, new File(filename));
				s3is.close();
				s3is = null;
				
				
				gzis = new GZIPInputStream(FileUtils.openInputStream(new File(filename)));
				BufferedReader gzreader = new BufferedReader(new InputStreamReader(gzis));
				
				String updateEntry = null;
				
				String type = Variables.AWS_REDSHIFT_UPDATE_TYPE;
				String id = null;
				long cnt = 0;
				// Note:
				//
				// We really want the epoch in the actual update entry, but providing it here for now.
				//
				long rs_epoch = System.currentTimeMillis();
				
				String separator = "|";
				
				List<SendMessageBatchRequestEntry> batch = new ArrayList<SendMessageBatchRequestEntry>();
				
				while ((updateEntry = gzreader.readLine()) != null) {
					
					StringTokenizer tokenizer = new StringTokenizer(updateEntry, separator);
					id = tokenizer.nextToken();
					cnt = Long.parseLong(tokenizer.nextToken());
					
					RedshitUpdateMessageEntryJson queueEntry = new RedshitUpdateMessageEntryJson(type, id, cnt, null, rs_epoch);
					
					SendMessageBatchRequestEntry messageEntry = new SendMessageBatchRequestEntry()
						.withMessageBody(queueEntry.asString())
						.withId(Integer.toString(batch.size()));
					
					batch.add(messageEntry);
					ctr += 1;
					
					//SimpleQueueService.addmessage(Variables.AWS_REDSHIFT_UPDATE_QUEUE, queueEntry.asString());
					 if (batch.size() == 10) {
	    				 SendMessageBatchResult res = SimpleQueueService.addMessageBatch(Variables.AWS_REDSHIFT_UPDATE_QUEUE, batch);
	    				 List<BatchResultErrorEntry> failed = res.getFailed();
	    				 if (failed.isEmpty() == false) {
	    					 Iterator<BatchResultErrorEntry> iter = failed.iterator();
	    					 while (iter.hasNext()) {
	    						 BatchResultErrorEntry error = iter.next();
	    						 System.out.println("*** SQS batch add error: ID: " + error.getId() + ", Code: " + error.getCode() + ", SenderFault: " + error.getSenderFault() + ", Msg: " + error.getMessage());
	    					 }
	    				 }
	    				 batch.clear();
	    			 }
				}
				// Output the last batch if necessary
	    		 if (batch.size() > 0) {
	    			 SimpleQueueService.addMessageBatch(Variables.AWS_REDSHIFT_UPDATE_QUEUE, batch);
	    		 }
			
	    		 System.out.println("Total Messages = '" + ctr + "'");
	    		 System.out.println("*** End " + sdf.format(new Date()));
			}
		}
		catch (Exception e) {
			problems = true;
			System.out.println("Exception processing Redshift Part: " + partBucket + " " + partKey);
			e.printStackTrace(System.out);
		}
		finally {
			try {
				
				if (s3is != null) {
					s3is.close();
				}
			
				if (gzis != null) {
					gzis.close();
				}
			
				if (filename != null) {
					File f = new File(filename);
					if (f.isFile()) {
						f.delete();
					}
				}
	
				if (problems == false) {
					SimpleQueueService.deleteMessage(Variables.AWS_REDSHIFT_MANIFEST_PARTS_QUEUE, message);
				}
			
			} catch (IOException e) {
				
				e.printStackTrace(System.out);
				
			}
		}

	}

}
