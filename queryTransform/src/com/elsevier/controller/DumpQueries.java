package com.elsevier.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

import com.elsevier.dynamo.DynamoDB;

public class DumpQueries {


		
		public static void main(String[] args) {
			
			try {
				

				if (args.length != 2) {
					System.out.println("Two parameters required.");
					System.out.println("First parameter is the key file location.");
					System.out.println("Second parameter is the output file.");
					System.exit(-1);
				}
				
				// Open Input File
				BufferedReader br = new BufferedReader(new FileReader(args[0]));
				
				// Open Output File
				PrintWriter owriter = new PrintWriter(args[1], "UTF-8");
		
				// QUERY_STATS loadQuery 400 TS Sun, 26 Jan 2014 18:58:04 GMT RES_TIME 27 SOLR_TIME -1 LEN 467 SOLR_LEN -1 DTYPE auth DSET 1 DIDX 109424 HITS -1 POST
				
				String line;
				while ((line = br.readLine()) != null) {
					String type = line.substring(line.indexOf("DTYPE") + "DTYPE".length(),line.indexOf("DSET")).trim();
					String set = line.substring(line.indexOf("DSET") + "DSET".length(),line.indexOf("DIDX")).trim();
					String idx = line.substring(line.indexOf("DIDX") + "DIDX".length(),line.indexOf("HITS")).trim();
					String key = type + "_" + set + "_" + idx;
					owriter.println(DynamoDB.getRecord(key));
					owriter.flush();
				}
				
			} catch (Exception ex) {
				
				ex.printStackTrace();
				
			}
			  
		}
		
}
