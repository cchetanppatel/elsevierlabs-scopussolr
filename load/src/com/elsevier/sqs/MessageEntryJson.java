package com.elsevier.sqs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;


public class MessageEntryJson {

	private JsonNode rootNode;
	private ObjectMapper mapper = new ObjectMapper();
	
	private String bucket;
	private String key;
	private String prefix;
	private String action;
	private String version;
	private long epoch;

	
	//
	// Parse the Json string into Jackson tree
	//
	public MessageEntryJson(String jsonString) throws JsonProcessingException, IOException {				
		rootNode = mapper.readTree(jsonString);
		
		// Currently, a lot of assumptions below and hard-coding
		bucket = rootNode.path(MessageConstants.BUCKET).asText();
		JsonNode entries = rootNode.path(MessageConstants.ENTRIES);
		Iterator<JsonNode> ite = entries.getElements();
		while (ite.hasNext()) {
			JsonNode entry = ite.next();
			key = entry.path(MessageConstants.KEY).asText();
			prefix = entry.path(MessageConstants.PREFIX).asText();
			action = entry.path(MessageConstants.ACTION).asText();
			version = entry.path(MessageConstants.VERSION).asText();
			epoch = entry.path(MessageConstants.EPOCH).asLong();
		}
		
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
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getVersion() {
		return version;
	}
	
	public long getEpoch() {
		return epoch;
	}
	

}