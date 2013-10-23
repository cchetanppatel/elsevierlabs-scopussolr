package com.elsevier.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NestedObjectHelper {

	private Map<Integer, Map<String, String>> nestedObjects = new HashMap<Integer, Map<String, String>>();
	
	public void addNestedObject(Integer key, Map<String, String> fields) {
		nestedObjects.put(key, fields);
	}
	
	public Set<Integer> getObjectKeys() { return nestedObjects.keySet(); }
	public Map<String, String> getObjectFields(Integer key) { return nestedObjects.get(key); }
	
	public boolean isEmpty() {
		return nestedObjects.isEmpty();
	}
}