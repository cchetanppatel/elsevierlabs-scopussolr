package com.elsevier.controller;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import com.elsevier.dynamo.DynamoDB;

import org.apache.commons.codec.binary.Base64;



public class Scopout {

	private static String xqueryx = "<ns2:search xmlns:ns2=\"http://webservices.elsevier.com/schemas/search/fast/types/v4\" " +
	    "xmlns:ns3=\"http://webservices.elsevier.com/schemas/easi/headers/types/v1\">" +
	    "<ns2:searchReqPayload>" +
	        "<ns2:xQueryX>" +
	            "<ft:fullTextQuery xmlns:ft=\"http://www.elsevier.com/2003/01/xqueryxFT-schema\" " +
	                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                "xsi:schemaLocation=\"http://www.elsevier.com/2003/01/xqueryxFT-schema eql.xsd\">" +
	                "<ft:query>" +
	                    "<ft:word path=\"refeid\">REFEIDVALUE</ft:word>" +
	                "</ft:query>" +
	            "</ft:fullTextQuery>" +
	        "</ns2:xQueryX>" +
	        "<ns2:orderByList>" +
	            "<ns2:orderByAttributes path=\"datesort\" sortOrder=\"descending\"/>" +
	        "</ns2:orderByList>" +
	        "<ns2:returnAttributes maxResults=\"3\" start=\"0\"/>" +
	        "<ns2:reqFields>srctitle</ns2:reqFields>" +
	        "<ns2:reqFields>itemtitle</ns2:reqFields>" +
	        "<ns2:reqFields>vol</ns2:reqFields>" +
	        "<ns2:reqFields>issue</ns2:reqFields>" +
	        "<ns2:reqFields>pg</ns2:reqFields>" +
	        "<ns2:reqFields>pubyr</ns2:reqFields>" +
	        "<ns2:reqFields>pubdatetxt</ns2:reqFields>" +
	        "<ns2:reqFields>auth</ns2:reqFields>" +
	        "<ns2:reqFields>eid</ns2:reqFields>" +
	        "<ns2:reqFields>doi</ns2:reqFields>" +
	        "<ns2:reqFields>pii</ns2:reqFields>" +
	        "<ns2:cluster>SCOPUS</ns2:cluster>" +
	        "<ns2:viaParamsList>" +
	            "<ns2:paramName>scomode</ns2:paramName>" +
	            "<ns2:paramName>scimode</ns2:paramName>" +
	            "<ns2:paramName>mixer</ns2:paramName>" +
	            "<ns2:paramValue>on</ns2:paramValue>" +
	            "<ns2:paramValue>off</ns2:paramValue>" +
	            "<ns2:paramValue>relevancy</ns2:paramValue>" +
	        "</ns2:viaParamsList>" +
	        "<ns2:elsfilter>" +
	            "<ft:fullTextQuery xmlns:ft=\"http://www.elsevier.com/2003/01/xqueryxFT-schema\" " +
	                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                "xsi:schemaLocation=\"http://www.elsevier.com/2003/01/xqueryxFT-schema eql.xsd\">" +
	                "<ft:query>" +
	                    "<ft:andQuery>" +
	                        "<ft:word path=\"collec\">SCOPUS</ft:word>" +
	                        "<ft:word path=\"db\">scopusbase</ft:word>" +
	                    "</ft:andQuery>" +
	                "</ft:query>" +
	            "</ft:fullTextQuery>" +
	        "</ns2:elsfilter>" +
	    "</ns2:searchReqPayload>" +
	"</ns2:search>";

	public static void main(String[] args) {
		
		try {
			
			String idFile = "/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusMainScopoutIds.txt";
			String outFile = "/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusMainScopout.txt";

			
			PrintWriter owriter = new PrintWriter(outFile, "UTF-8");
					  
			// Process the file	
			BufferedReader br = new BufferedReader(new FileReader(idFile));
			String refeid;
			String rec;
			
			while ((refeid = br.readLine()) != null) {

				rec = xqueryx.replaceAll("REFEIDVALUE",refeid);
				System.out.println(refeid);
				System.out.println(rec);
				owriter.println(rec);
				owriter.flush();


			}
			
		} catch (Exception ex) {
			
			ex.printStackTrace();
			
		}
		  
	}


}
