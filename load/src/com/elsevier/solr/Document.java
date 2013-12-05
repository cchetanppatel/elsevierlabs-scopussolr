package com.elsevier.solr;


import com.elsevier.common.Variables;
import com.elsevier.transform.NestedObjectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public class Document {

	private static HttpSolrServer solrServer = null;
	
	
	/**
	 * Initialization
	 */
	public static void init() {
		// Set up to talk to the configured Solr instance
		solrServer = new HttpSolrServer(Variables.SOLR_ENDPOINT);
		System.out.println("SolrServer Base URL: " + solrServer.getBaseURL());

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

		HttpSolrServer solrServer = new HttpSolrServer(Variables.SOLR_ENDPOINT);
		
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
					    
			UpdateResponse response = solrServer.add(solrDoc, 30000);
		    
			int status = response.getStatus();
		
			if (status == 0) {
				System.out.println("** ADDED id= '" + id + "'");
			} else {
				System.out.println("Error adding doc. id='"  + id + "'");
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
		}
				
		
	}
	
}
