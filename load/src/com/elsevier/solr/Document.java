package com.elsevier.solr;


import com.elsevier.common.Variables;
import com.elsevier.transform.NestedObjectHelper;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public class Document {

	// Note: We'll cast the appropriate subclass to this once we decide if we are SolrCloud or not and build the correct class
	private static SolrServer solrServer = null;
	
	
	/**
	 * Initialization
	 */
	//public static void init() {
	static {	
		// Are we running a single Solr instance without a ZooKeeper (Ususal for local development 
		if (Variables.ZOOKEEPER_ENDPOINT.equalsIgnoreCase("")) {
			// Set up to talk to the configured Solr instance
			solrServer = new HttpSolrServer(Variables.SOLR_ENDPOINT);
			System.out.println("SolrHttpServer Base URL: " + ((HttpSolrServer)solrServer).getBaseURL());
		} else {
			// We are running a Zookeeper which will have all the available nodes to talk to for a particular collection 
			// as specified by Variables.SOLR_COLLECTION.
			try {
				solrServer =  new CloudSolrServer(Variables.ZOOKEEPER_ENDPOINT);
				((CloudSolrServer)solrServer).setDefaultCollection(Variables.SOLR_COLLECTION);
				System.out.println("CloudSolrServer Using ZooKeeper: '" + Variables.ZOOKEEPER_ENDPOINT + "'");
			} catch (MalformedURLException e) {
				System.out.println("Invalid ZOOKEEPER_ENDPOINT configuration specified. Value:'" + Variables.ZOOKEEPER_ENDPOINT +"'");
				e.printStackTrace();
				System.exit(-1);
			}
		} 
	}
	
	
	
	/**
	 * Add the document to the ElasticSearch index. 
	 * 
	 * @param index ElasticSearch index name
	 * @param type ElasticSearch index type
	 * @param map Field/Values associated with the document
	 * @param id unique identifier for the document
	 * @param epoch version for the document
	 */
	public static void add(String index, HashMap<String,Object> map, String id, long epoch) throws Exception {
		
		SolrInputDocument solrDoc = new SolrInputDocument();
		
			
			// Get all of the keys in the Map
			Set<String> keySet = map.keySet();
			Iterator<String> it = keySet.iterator();
			
			// Process the keys in the Map
			while (it.hasNext()) {
				
				String key = it.next();
				
				// Get the object
				Object obj = map.get(key);
							
				if (obj instanceof String) {
					
					// If a string, just construct the field/value
					solrDoc.addField(key, obj);
					
				} else if (obj instanceof ArrayList){
					Iterator<String> it2 = ((ArrayList) obj).iterator();
					while(it2.hasNext()) {
						solrDoc.addField(key, it2.next());
					}
				
				}  else {
					
					// Shouldn't happen ... the class should either be String or ArrayList
					System.out.println("--> Unknown class is " + obj.getClass().getName());
					
				}
				
			}	        
					    
			UpdateResponse response = null;
			
			if (Variables.SOLR_AUTOCOMMIT_DELAY_MS == -1) {
				response = solrServer.add(solrDoc);
			} else {
				response = solrServer.add(solrDoc, Variables.SOLR_AUTOCOMMIT_DELAY_MS);
			}
		    
			int status = response.getStatus();
		
			if (status == 0) {
				System.out.println("** ADDED id= '" + id + "'");
			} else {
				System.out.println("Error adding doc. id='"  + id + "'");
				throw new Exception("Error adding doc to index. doc. id='"  + id + "'");
			}
		
	}
	
	
	/**
	 * Remove the document from the ElasticSearch index. 
	 * 
	 * @param index ElasticSearch index name
	 * @param type ElasticSearch index type
	 * @param id unique identifier for the document
	 * @param epoch version for the document
	 */	
	public static void delete(String index, String id, long epoch) throws Exception {
		
		UpdateResponse response = solrServer.deleteById(id);
		
		int status = response.getStatus();
		
		if (status == 0) {
			solrServer.commit();
			System.out.println("** DELETED id= '" + id + "'");
		} else {
			System.out.println("Error deleting doc. id='"  + id + "'");
			throw new Exception("Error deleting doc from index. doc. id='"  + id + "'");
		}
				
		
	}
	
	/**
	 * Update fields in the document to the ElasticSearch index. 
	 * 
	 * @param index ElasticSearch index name
	 * @param type ElasticSearch index type
	 * @param map Field/Values to update in the document
	 * @param idFieldName Solr schema field defined as the unique id
	 * @param id unique identifier for the document
	 * @param epoch version for the document
	 */
	public static void update(String index, HashMap<String,Object> map, String idFieldName, String id, String versionFieldName, long epoch) throws Exception {
				
		SolrInputDocument solrDoc = new SolrInputDocument();
		
		//
		// Updates are different in that you need to do a standard addField for the id and version control 
		// field without any of the special update triggering payloads settings. This means that we 
		// can't just pass in the key/value pairs for those with the fields to be updated.  We'll pass them
		// in separately here and just add them to the doc, and then process the Hashmap of values to be 
		// update fields.
		//
		solrDoc.addField(idFieldName, id);
		solrDoc.addField(versionFieldName, epoch);
		
		// Get all of the keys in the field value Map coming into the function
		Set<String> keySet = map.keySet();
		Iterator<String> it = keySet.iterator();
		
		// While there are still fields we want to update for the document
		while (it.hasNext()) {
			
			// Solr Atomic Update uses a payload of and Map<String, String> to manage getting
			// new updated values into the indexed document (leveraging the saved fields from the 
			// previous add/update). By leveraging the defined value of "set" for the HashMap key,
			// it instructs Solr to replace the existing values with the current value for the 
			// key value specified in the addField call.
			Map<String,Object> updateVals = new HashMap<String, Object>();
			
			String key = it.next();
			
			// Get the object representing the values and set it to replace any existing values in the index
			Object obj = map.get(key);
			updateVals.put("set", obj);
			
			solrDoc.addField(key, updateVals);
		}
		
		UpdateResponse response = null;
		
		if (Variables.SOLR_AUTOCOMMIT_DELAY_MS == -1) {
			response = solrServer.add(solrDoc);
		} else {
			response = solrServer.add(solrDoc, Variables.SOLR_AUTOCOMMIT_DELAY_MS);
		}
	    
		int status = response.getStatus();
	
		if (status == 0) {
			System.out.println("** Updated id= '" + id + "'");
		} else {
			System.out.println("Error atomic updating doc. id='"  + id + "'");
			throw new Exception("Error atomic updating doc. id='"  + id + "'");
		}
		
	}
	
}
