package com.elsevier.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class SimpleSendQuery {

	public SimpleSendQuery() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String [] args) throws UnsupportedEncodingException, IOException {

		SimpleSendQuery ssq = new SimpleSendQuery();

		if (args == null || args.length < 2) {
			System.out.println("\n\tusage: java " + SimpleSendQuery.class + " <queryDirectory> <httpEndPointOfSolr>");
			System.out.println("\tjava "+ SimpleSendQuery.class + "piisDir http://localhost:8983/solr/select/q=");
			System.exit(-1);
		}

		String qDirectoryStr = args[0];
		String endpoint      = args[1];

		ssq.processDirectory(qDirectoryStr, endpoint);

	}

	/**
	 * driver to test queries against endpoints
	 * 
	 * @param qDirectoryStr
	 * @param endpoint
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	private void processDirectory(String qDirectoryStr, String endpoint) throws UnsupportedEncodingException, IOException {

		File file = new File(qDirectoryStr);

		// Reading directory contents
		File[] files = file.listFiles();

		if ((endpoint.endsWith("/"))){
			System.out.println("adjusted the endpoint");
			endpoint =endpoint.substring(0,endpoint.length());
		}

		for (int i = 0; i < files.length; i++) {

			if (files[i].isFile()) {
				
				System.out.println(files[i]);
				BufferedReader freader = null;

				freader= new BufferedReader(new InputStreamReader(new FileInputStream(files[i]), "utf-8"));
				
				String fline = "";
				
				int j = 0;
				
				while (fline != null) {
					
					j++;
					if(j > 5) break;
					
					System.out.println("\n\t"+j+"\n");
					
					fline = freader.readLine();
					
					String urlStr = endpoint+fline;
					System.out.println(urlStr);
					//send this file to the endpoint
					URL url = new URL(urlStr);
					BufferedReader reader = null;

					try {
		
						reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

						for (String line; (line = reader.readLine()) != null;) {
							System.out.println(line);
						}
					} finally {
						if (reader != null) try { reader.close(); } catch (IOException ignore) {}
					}
				}
			}
		}
		System.out.println("\n\ndone");
		}
	}
