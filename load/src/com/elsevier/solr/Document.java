package com.elsevier.solr;


import com.elsevier.common.Variables;
import com.elsevier.transform.NestedObjectHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Document {

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

		try {

			// Use the jsonBuilder helper instead of the automatic Map to Json
			XContentBuilder builder = jsonBuilder();
			
			// Start the outer object (the root)
			builder.startObject();
			
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
					builder.field(key, (String)obj);
					
				} else if (obj instanceof ArrayList){
					
					// If an ArrayList, construct an Array of values
					builder.startArray(key);
					Iterator<String> it2 = ((ArrayList) obj).iterator();
					while(it2.hasNext()) {
						builder.value(it2.next());
					}
					builder.endArray();
				
				} else if (obj instanceof NestedObjectHelper){
					NestedObjectHelper nestedObjs = (NestedObjectHelper) obj;
					Iterator<Integer> nestedObjIter = nestedObjs.getObjectKeys().iterator();
					
					Iterator<String> fieldIter = null;
					Map<String, String> workFields = null;
					// If an Map, then create a nested document based on the key value pairs
					builder.startArray(key);
					while (nestedObjIter.hasNext()) {
						Integer workKey = nestedObjIter.next();
						builder.startObject();
						workFields = nestedObjs.getObjectFields(workKey);
						fieldIter = workFields.keySet().iterator();
						while(fieldIter.hasNext()) {
							String fieldKey = fieldIter.next();
							builder.field(fieldKey, workFields.get(fieldKey));
						}
						builder.endObject();
					}
					builder.endArray();
					
				} else {
					
					// Shouldn't happen ... the class should either be String or ArrayList
					System.out.println("--> Unknown class is " + obj.getClass().getName());
					
				}
				
			}	        
			
			// Close the outer object (the root)
		    builder.endObject();
		    
			IndexResponse response = client.prepareIndex(index, type, id)
    		.setSource(builder)
    		.setVersion(epoch)
    		.setVersionType(VersionType.EXTERNAL)
    		.execute()
    		.actionGet();
			
/* 
 * Prior approach used the automatic Map to Json conversion
 * 
			IndexResponse response = client.prepareIndex(index, type, id)
        		.setSource(map)
        		.setVersion(epoch)
        		.setVersionType(VersionType.EXTERNAL)
        		.execute()
        		.actionGet();
*/		
			
			// Index name

			String _index = response.getIndex();
			// Type name
			String _type = response.getType();
			// Document ID (generated or not)
			String _id = response.getId();
			// Version (if it's the first time you index this document, you will get: 1)
			long _version = response.getVersion();
		
			System.out.println("** ADDED index= '" + _index + "' type= '" + _type + "' id= '" + _id + "' version= '" + _version + "'");
		
		} catch (Exception ex) {
			
			System.out.println("** Version Conflict for ADD index= '" + index + "' type= '" + type + "' id= '" + id + "' version= '" + epoch + "'");
			ex.printStackTrace();
			throw(ex);
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
		
		try {
			
			DeleteResponse response = client.prepareDelete(index, type, id)
		    	.setVersion(epoch)
		    	.setVersionType(VersionType.EXTERNAL)
		    	.execute()
		    	.actionGet();
		
			// Index name
			String _index = response.getIndex();
			// Type name
			String _type = response.getType();
			// Document ID (generated or not)
			String _id = response.getId();
			// Version (if it's the first time you index this document, you will get: 1)
			long _version = response.getVersion();
		
			System.out.println("** DELETED index= '" + _index + "' type= '" + _type + "' id= '" + _id + "' version= '" + _version + "'");
		
		} catch (Exception ex) {
			
			System.out.println("** Version Conflict for DELETE index= '" + index + "' type= '" + type + "' id= '" + id + "' version= '" + epoch + "'");
			ex.printStackTrace();
			throw(ex);
			
		}
		
	}
	
}
