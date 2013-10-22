package com.elsevier.query;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

public class HeadSimpleSendQuery {

	public HeadSimpleSendQuery() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String [] args) throws UnsupportedEncodingException, IOException {

		HeadSimpleSendQuery ssq = new HeadSimpleSendQuery();

		if (args == null || args.length < 2) {
			System.out.println("\n\tusage: java " + HeadSimpleSendQuery.class + " <queryDirectory> <httpEndPointOfSolr>");
			System.out.println("\tjava "+ HeadSimpleSendQuery.class + "piisDir http://localhost:8983/solr/select/q=");
			System.exit(-1);
		}

		String qDirectoryStr = args[0];
		String endpoint      = args[1];
		File f= new File(qDirectoryStr);

		if (f.isDirectory())
			ssq.processDirectory(qDirectoryStr, endpoint);
		else ssq.processFile(qDirectoryStr, endpoint);
	}

	/**
	 * process File
	 * @param fileStr
	 * @param endpoint
	 * @throws IOException 
	 */
	private void processFile(String fileStr, String endpoint) throws IOException {

		System.out.println("processing a file: "+ fileStr);

		if ((endpoint.endsWith("/"))){
			System.out.println("adjusted the endpoint");
			endpoint =endpoint.substring(0,endpoint.length());
		}
		System.out.println("\nprocessing " +fileStr+"\n");


		BufferedReader freader= new BufferedReader(new InputStreamReader(new FileInputStream(fileStr), "utf-8"));
		BufferedWriter fwriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileStr+".out"), "utf-8"));
		int j=0;
		
		try {
			
		String fline = "";
		
		while (fline != null) {

			j++;
			//System.out.println("\nj="+j);

			fline = freader.readLine();

			String urlStr = endpoint+fline;
			System.out.println(urlStr+"\n");
			//send this file to the endpoint
			URL url = new URL(urlStr);
			BufferedReader reader = null;

			try {

				reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

				for (String line; (line = reader.readLine()) != null;) {
					if (line.contains("numFound")) {
						int sublineNdx = line.indexOf("numFound");
						if (sublineNdx >-1) {

							String res = line.substring(sublineNdx+10, sublineNdx+30);
							System.out.println(res);
							int ndx = res.indexOf(',');
							if (ndx > 0) {
								String subRes = res.substring(0,ndx);
								System.out.println(subRes);
								fwriter.write(j+","+subRes+"\n");
							}
						}
					}

				}
			} finally {
			
				if (reader != null) try { reader.close(); } catch (IOException ignore) {}

			}
		}
	}
	catch(IOException e) {
		if (fwriter != null) try { fwriter.flush(); fwriter.close(); 	System.out.println("we crapped out on line "+j); } catch (IOException ignore) {}
	}
	
		if (fwriter != null) try { fwriter.flush(); fwriter.close(); } catch (IOException ignore) {}
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

			//only process query files
			if (files[i].isFile() && files[i].toString().endsWith(".qry")) {

				System.out.println("\nprocessing " +files[i] +"\n");
				BufferedReader freader = null;


				freader= new BufferedReader(new InputStreamReader(new FileInputStream(files[i]), "utf-8"));

				String fline = "";
				int j=0;
				while (fline != null && j < 2) {

					j++;
					System.out.println("\nj="+j);
					fline = freader.readLine();

					String urlStr = endpoint+fline;
					System.out.println(urlStr+"\n");
					//send this file to the endpoint
					URL url = new URL(urlStr);
					BufferedReader reader = null;

					try {

						reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

						for (String line; (line = reader.readLine()) != null;) {
							System.out.print(".");
						}
					} finally {
						if (reader != null) try { reader.close(); } catch (IOException ignore) {}
					}
				}
			}
		}
	}
}
