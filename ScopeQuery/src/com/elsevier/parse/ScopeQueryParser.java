package com.elsevier.parse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.apache.lucene.search.spans.SpanQuery;

/*
 * 
	fn:(connector(values))
	
	Stack Entry needs
		field name
		connector
		tokens
	
	values can be tokens to search
	
 */
public class ScopeQueryParser {
	
	public static void main(String[] args) {
		
		ScopeQueryParser sqp = new ScopeQueryParser();
		sqp.parse("AND(OR(AND(fname:darin,fname:william)|AND(fname:darby,fname:lynn))|lname:mcbeath|author:ohio)");
		
	}
	
	
	public ScopeQueryParser() {	
		
	}
	
	public void parse(String query) {
		
		/*
		setConnectors(query);
		
		Iterator<Connector> iter = connectors.iterator();		
		while (iter.hasNext()) {
			Connector currConnector = iter.next();
			System.out.println(currConnector.getOffset() + " " + currConnector.getType());
		}
		*/
		//setFields(query);
		walkTree(query);
		

	}
	
	
	private void  walkTree(String str)
	{
	    if (str.isEmpty())
	        return;
	    
	    StringBuffer currentToken = new StringBuffer();

	    
	    for (int i = 0; i < str.length(); i++) {
	    	
	        char current = str.charAt(i);
	        currentToken.append(current);
	        
	        if (current == '(' ) {
	        	
	        	if (currentToken.substring(0).compareTo("AND(") == 0) {
	        		
	        		System.out.println("AND");
	        		String tmp = matchParenthesis(str.substring(i));
	        		System.out.println(tmp.substring(1));
	        		walkTree(tmp.substring(1));
	        		
	        	} else if (currentToken.substring(0).compareTo("OR(") == 0) {
	        		
	        		System.out.println("OR");
	        		String tmp = matchParenthesis(str.substring(i));
	        		System.out.println(tmp.substring(1));
	        		walkTree(tmp.substring(1));
	        		
	        	} else if (currentToken.substring(0).compareTo("NOT(") == 0) {
	        		
	        		System.out.println("NOT");
	        		String tmp = matchParenthesis(str.substring(i));
	        		System.out.println(tmp.substring(1));
	        		walkTree(tmp.substring(1));
	        	
	        	}
	        	
	        	
	        }

	    }

	    return;
	}
	
	
	
	private String  matchParenthesis(String str) {

	    if (str.isEmpty())
	        return "";

	    int lps = 0;
	    
	    for (int i = 0; i < str.length(); i++) {
	    	
	        char current = str.charAt(i);
	        if (current == '(' ) {
	            lps++;
	        }


	        if (current == ')' ) {
	        	
	        	lps--;
	        	if (lps == 0) {
	        		return str.substring(0, i);
	        	}
	            
	        }

	    }

	    return "";
	}
	

}
