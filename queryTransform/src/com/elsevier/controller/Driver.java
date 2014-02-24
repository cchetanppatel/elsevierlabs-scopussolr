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



public class Driver {

	//private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/ESjson/stylesheets/xml2esjson.xsl";
	//private static String xml = "/Users/mcbeathd/SDSearchQueries/count/HotHouseCountQueries.xml";
	//private static String xml = "/Users/mcbeathd/SDSearchQueries/file1/eid.xml";

	public static void main(String[] args) {
		
		try {
			
			if (args.length != 5) {
				System.out.println("Five parameters required.");
				System.out.println("First parameter is the stylesheet name/location.");
				System.out.println("Second parameter is the xml file name/location.");
				System.out.println("Third parameter is type (auth, affil, main).");
				System.out.println("Fourth parameter is set.");
				System.out.println("Fifth parameter is output file.");
				System.exit(-1);
			}
		  

			String stylesheet = args[0];
			String xml = args[1];
			String type = args[2];
			String set = args[3];
			
			PrintWriter owriter = new PrintWriter(args[4], "UTF-8");
			
			// Prepare the transformer
			InputStream stylesheetIs = new FileInputStream(stylesheet);	  
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(stylesheetIs));
		  
			// Process the file	
			BufferedReader br = new BufferedReader(new FileReader(xml));
			String line;
			int idx = 0;
			String result = null;
			
			while ((line = br.readLine()) != null) {
				InputStream contentIs = IOUtils.toInputStream(line, "utf-8");
				
				try {
					result = transform(transformer,contentIs);
				} catch (Exception ex) {
					// Catch badly formed xml (and ignore)
					System.out.println(line);
					System.out.println("Problems");
					ex.printStackTrace();
					//System.exit(1);
					continue;
				}
				
				idx++;
				String key = type + "_" + set + "_" + idx;
				
				//System.out.println(key);
				System.out.println(line);
				System.out.println(result);
				//owriter.println(line);
				//owriter.println("XQueryX record = " + idx + "\n" + result);
				//owriter.println(result);
				//owriter.flush();

				try {
					DynamoDB.insertRecord(key, result);
				} catch (Exception ddbex) {
					System.out.println("Problems with " + key);
				}

			}
			
		} catch (Exception ex) {
			
			ex.printStackTrace();
			
		}
		  
	}

	public static String transform(Transformer transformer, InputStream contentIs) throws TransformerException {

	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  transformer.transform(new StreamSource(contentIs), new StreamResult(baos));
	  
	  // Return the friendlier xml
	  return baos.toString();
	  
	}
}
