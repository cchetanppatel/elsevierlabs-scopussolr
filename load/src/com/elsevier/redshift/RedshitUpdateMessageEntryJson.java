package com.elsevier.redshift;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.elsevier.sqs.MessageConstants;


public class RedshitUpdateMessageEntryJson {

	private JsonNode rootNode;
	private ObjectMapper mapper = new ObjectMapper();
	
	private String type = "";
	private String id = "";
	private long cnt = 0;
	private List<String> idlist = null;
	private long rs_epoch = 0;

	private static String TYPE = "type";
	private static String ID = "id";
	private static String CNT = "cnt";
	private static String IDLIST = "idlist";
	private static String RS_EPOCH = "rs_epoch";

	
	public static void main(String[] args) {
		try {
			List<String> ids = new ArrayList<String>();
			ids.add("id1");
			ids.add("id2");
			
		RedshitUpdateMessageEntryJson entry = new RedshitUpdateMessageEntryJson("auth", "1234", 25, ids, 123L);
		
		System.out.println("JSON = " + entry.asString());
		}
		catch (Exception e) {
			System.out.println("Exception:  + e");
		}
	}
	
	//
	// Create a message entry from the raw values
	//
	public RedshitUpdateMessageEntryJson(String type, String id, long cnt, List<String> idlist, long rs_epoch) {				
		// Currently, a lot of assumptions below and hard-coding
		this.type = type;
		this.id = id;
		this.cnt = cnt;
		this.idlist = idlist;
		this.rs_epoch = rs_epoch;
		
		ObjectNode root = mapper.createObjectNode();
		root.put(TYPE, type);
		root.put(ID, id);
		root.put(CNT, cnt);
		ArrayNode listNode = root.putArray(IDLIST);
		if (idlist != null && idlist.size() > 0) {
			Iterator<String> iter = idlist.iterator();
			while (iter.hasNext()) {
				listNode.add(iter.next());
			}
		}
		root.put(RS_EPOCH, rs_epoch);
		rootNode = root;
	}
	
	//
	// Parse the Json string into Jackson tree
	//
	public RedshitUpdateMessageEntryJson(String jsonString) throws JsonProcessingException, IOException {				
		rootNode = mapper.readTree(jsonString);
		
		// Currently, a lot of assumptions below and hard-coding
		type = rootNode.path(TYPE).asText();
		id = rootNode.path(ID).asText();
		cnt = rootNode.path(CNT).getLongValue();
		idlist = rootNode.findValuesAsText(IDLIST);
		rs_epoch = rootNode.path(RS_EPOCH).getLongValue();

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
	
	
	public String getType() {
		return type;
	}

	public String getId() {
		return id;
	}
	
	public long getCnt() {
		return cnt;
	}
	
	public List<String> getIdlist() {
		return idlist;
	}

	public long getRs_epoch() {
		return rs_epoch;
	}
	

}