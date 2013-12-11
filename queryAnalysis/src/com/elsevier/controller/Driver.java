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

public class Driver {

	//private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/XQueryXAnalysis/stylesheets/punct-sensitive.xsl";
	//private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/XQueryXAnalysis/stylesheets/scope-query.xsl";
	//private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/XQueryXAnalysis/stylesheets/starts-equals.xsl";
	private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/XQueryXAnalysis/stylesheets/auid-only.xsl";
	//private static String stylesheet = "/Users/mcbeathd/Documents/eclipse/XQueryXAnalysis/stylesheets/proximity.xsl";

	//private static String xml = "/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/SCOPUS_MAIN_FULLTEXTQUERY_10152013.txt";
	//private static String xml = "/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/SCOPUS_AUTHOR_FULLTEXTQUERY_10152013.txt";
	private static String xml = "/Users/mcbeathd/ScopusSearchQueries/dec052013/ScopusMainXQueryX_Dec052013.txt";
	//private static String xml = "/Users/mcbeathd/SDSearchQueries/xmlxqueryx/SD_XQueryX_Logs_Nov2013.txt";
	//private static String xml = "/Users/mcbeathd/SDSearchQueries/alerts/alerts.txt";
	//private static String xml = "/Users/mcbeathd/SDSearchQueries/rss/SD_RSS.txt";
	
	private static String ofile = "/Users/mcbeathd/ScopusSearchQueries/dec052013/results/ScopusMainAuId.txt";
	//private static String ofile = "/Users/mcbeathd/ScopusSearchQueries/xmlxqueryx/results/ScopusAffiliationAfid.txt";
	//private static String ofile = "/Users/mcbeathd/SDSearchQueries/xmlxqueryx/results/punctSensitiveAnalysis.txt";
	//private static String ofile = "/Users/mcbeathd/SDSearchQueries/rss/results/scopeAnalysis.txt";
	//private static String ofile = "/Users/mcbeathd/SDSearchQueries/alerts/results/proximityAnalysis.txt";
	//private static String ofile = "/Users/mcbeathd/SDSearchQueries/rss/results/proximityAnalysis.txt";
	
	private static int errorCnt = 0;
	
	public static void main(String[] args) {
		
		try {
			
			/*
			if (args.length != 2) {
				System.out.println("Two parameters required.");
				System.out.println("First parameter is the stylesheet name/location.");
				System.out.println("Second parameter is the xml file name/location.");
				System.exit(-1);
			}
		  

			String stylesheet = args[0];
			String xml = args[1];
			*/
			
			PrintWriter owriter = new PrintWriter(ofile, "UTF-8");
			
			// Prepare the transformer
			InputStream stylesheetIs = new FileInputStream(stylesheet);	  
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(stylesheetIs));
		  
			// Process the file	
			BufferedReader br = new BufferedReader(new FileReader(xml));
			String line;
			int ctr = 0;
			while ((line = br.readLine()) != null) {
								
				InputStream contentIs = IOUtils.toInputStream(line, "utf-8");
				String result = null;
				try {
					result = transform(transformer,contentIs);
				} catch (Exception ex) {
					// Record has malformed xqueryx structures
					System.out.println(line);
					errorCnt++;
					continue;
				}	


				if (result != null && result.length() > 0) {
					
					System.out.println(line);
					System.out.println(result);
					// Write to a file
					owriter.println(result);
					owriter.flush();

				}

			}
			System.out.println("Invalid XML Count is " + errorCnt);
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
