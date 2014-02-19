package com.elsevier.controller;


import java.util.HashMap;

import com.elsevier.common.Variables;
import com.elsevier.solr.Document;


public class AuthCntUpdateTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// Make up a new count
		long epoch = System.currentTimeMillis();
		
		HashMap<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("count", "10");
		String idFieldName = "authid";
		String id = "36135960300";

		try {
			Document.update(Variables.SOLR_COLLECTION, fieldValues, idFieldName, id, "epoch-rs", epoch );
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		
	}

}
