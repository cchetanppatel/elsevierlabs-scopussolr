package com.elsevier.redshift;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.elsevier.sqs.MessageConstants;


public class RedshiftPartMessageEntryJson {

	private JsonNode rootNode;
	private ObjectMapper mapper = new ObjectMapper();
	
	private String bucket = "";
	private String key = "";
	private String url = "";

	private static String BUCKET = "bucket";
	private static String KEY = "key";
	private static String URL = "url";


	//
	// Create a message entry from the raw values
	//
	public RedshiftPartMessageEntryJson(String bucket, String key, String url) {				
		// Currently, a lot of assumptions below and hard-coding
		this.bucket = bucket;
		this.key = key;
		this.url = url;
		
		ObjectNode root = mapper.createObjectNode();
		root.put(BUCKET, bucket);
		root.put(KEY, key);
		root.put(URL, url);
		rootNode = root;
	}
	
	//
	// Parse the Json string into Jackson tree
	//
	public RedshiftPartMessageEntryJson(String jsonString) throws JsonProcessingException, IOException {				
		rootNode = mapper.readTree(jsonString);
		
		// Currently, a lot of assumptions below and hard-coding
		bucket = rootNode.path(BUCKET).asText();
		key = rootNode.path(KEY).asText();
		url = rootNode.path(URL).asText();

	}
	
	
	//
	// Get the internal root Jackson Json serialized as a string
	//
	public String asString() throws JsonGenerationException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mapper.writeValue(baos, rootNode);
		return baos.toString();
	}
	
	
	//
	// Get the specified Jackson Json serialized as a string
	//
	public String asString(ObjectNode node) throws JsonGenerationException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mapper.writeValue(baos, node);
		return baos.toString();
	}
	
	
	public String getBucket() {
		return bucket;
	}

	public String getKey() {
		return key;
	}
	
	public String getUrl() {
		return url;
	}
	

}