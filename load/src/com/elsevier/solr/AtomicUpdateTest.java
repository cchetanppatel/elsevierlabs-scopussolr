package com.elsevier.solr;

import java.util.ArrayList;
import java.util.HashMap;

import com.elsevier.common.Variables;

public class AtomicUpdateTest {

	public static void main(String[] args) {

		addDoc("id", "1", "epoch");
		
		updateDoc("id", "1", "epoch");
		
	}
	
	public static void addDoc(String idFieldName, String id, String docVersionFieldName) {
		// TODO Auto-generated method stub

		// Make up a new timestamp
		long epoch = System.currentTimeMillis();
		
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(idFieldName, id);
		fieldValues.put(docVersionFieldName, new String() + epoch);
		fieldValues.put("field", "this value should stay the same");
		ArrayList<String> vals = new ArrayList<String>();
		vals.add("Kim");
		vals.add("Bryan");
		fieldValues.put("multi", vals);
		
		try {
			Document.add(Variables.SOLR_COLLECTION, fieldValues, id, epoch );
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}
	
	public static void updateDoc(String idFieldName, String id, String docVersionFieldName) {

		// Make up a new timestamp
		long epoch = System.currentTimeMillis();
		
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		
		// Update a missing/previously populated single field
		fieldValues.put("count", "10");

		// Update a multivalue field with new values
		ArrayList<String> vals = new ArrayList<String>();
		vals.add("Curt");
		vals.add("Darin");
		fieldValues.put("multi", vals);

		try {
			Document.update(Variables.SOLR_COLLECTION, fieldValues, idFieldName, id, docVersionFieldName, epoch);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}

}
