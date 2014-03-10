package com.elsevier.controller;


import java.util.HashMap;

import com.amazonaws.services.sqs.model.Message;
import com.elsevier.common.Variables;
import com.elsevier.solr.Document;
import com.elsevier.sqs.MessageEntryJson;
import com.elsevier.sqs.SimpleQueueService;


public class DummyNumCitedByUpdateTest {

	public static void main(String[] args) {
		
		long epoch = System.currentTimeMillis();
		
		HashMap<String,String> metadata = new HashMap<String,String>();
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();

		while (true) {
			Message message = null;
			String contentKey;
			String idFieldName = "id";
			
			metadata.clear();
			fieldValues.clear();
			
			try {
				message = SimpleQueueService.readQueue(Variables.SQS_QUEUE_NAME);
				MessageEntryJson json = new MessageEntryJson(message.getBody());
				
				if (message != null) {
					System.out.println("** Processing " + message.getBody() + " from the queue.");
					
					// Parse the message (Jackson) to get key
					contentKey = json.getKey();
					fieldValues.put("numcitedby", "25");
					Document.update(Variables.SOLR_COLLECTION, fieldValues, idFieldName, contentKey, "epoch-rs", epoch );
					
				}
			}
			catch (Exception e) {
				System.out.println("Exception: " + e.getLocalizedMessage());
				e.printStackTrace(System.out);
			}
			finally {
				try {
					if (message != null) {
						SimpleQueueService.deleteMessage(Variables.SQS_QUEUE_NAME, message);
						System.out.println("** Deleted message " + message.getBody() + " from the queue.");
					}
				}
				catch (Exception e1) {
					System.out.println("Exception: " + e1.getLocalizedMessage());
					e1.printStackTrace(System.out);
				}
			}
		}
	}

}
