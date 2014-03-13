package com.elsevier.redshift;

import java.io.*;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.amazonaws.HttpMethod;
import com.elsevier.common.Variables;
import com.elsevier.s3.SimpleStorageService;
import com.elsevier.sqs.MessageConstants;
import com.elsevier.sqs.SimpleQueueService;

public class RedshiftMainfestProcessor {

	public static void main(String[] args) {

		InputStream s3is = null;
		InputStream is = null;
		String filename = null;
		boolean problems = false;
		
		JsonNode rootNode;
		ObjectMapper mapper = new ObjectMapper();
		
		Variables.dumpVariables();
		
		// Build up a authenticated URL to pull down the manifest file.
		// Make the URL good for 1 hour.
		long duration = 1000 * 60 * 60;
		try {
			//URL manifestURL = SimpleStorageService.getPresignedURL(Variables.AWS_REDSHIFT_RESULTS_BUCKET, Variables.AWS_REDSHIFT_RESULTS_MANIFEST, duration); 
			//System.out.println(manifestURL.toString());
			s3is = SimpleStorageService.getObject(Variables.AWS_REDSHIFT_RESULTS_BUCKET, Variables.AWS_REDSHIFT_RESULTS_MANIFEST);
			
			// Create a local file in tmp and copy the S3 object to this file
			filename = FileUtils.getTempDirectoryPath() + File.separatorChar + DigestUtils.md5Hex(Variables.AWS_REDSHIFT_RESULTS_MANIFEST);
			FileUtils.copyInputStreamToFile(s3is, new File(filename));
			s3is.close();
			s3is = null;
			
			// Parse to extract the bits we need
			is = FileUtils.openInputStream(new File(filename));
			rootNode = mapper.readTree(is);
			
			JsonNode entries = rootNode.path("entries");
			Iterator<JsonNode> ite = entries.getElements();
			
			while (ite.hasNext()) {
				JsonNode entry = ite.next();
				String url = entry.path("url").asText();
				// Get to the bucket/objectName portion of URL
				String workUrlStr = url.substring(url.indexOf("//") + 2);
				String partBucket = workUrlStr.substring(0, workUrlStr.indexOf('/'));
				String partKey = workUrlStr.substring( workUrlStr.indexOf('/') +1);
				
				URL partURL = SimpleStorageService.getPresignedURL(partBucket, partKey, duration);
				
				// Put record on SQS.  Containing bucket, objectName and authenticated URL
				System.out.println(partURL.toString());
				
				RedshiftPartMessageEntryJson partEntry = new RedshiftPartMessageEntryJson(partBucket, partKey, partURL.toString());
				
				System.out.println("Part entry: " + partEntry.asString());
				
				SimpleQueueService.addmessage(Variables.AWS_REDSHIFT_MANIFEST_PARTS_QUEUE, partEntry.asString());
				
				
			}
			
		}
		catch (Exception e) {
			System.out.println("Exception processing Redshift Manifest: " + Variables.AWS_REDSHIFT_RESULTS_BUCKET + " " + Variables.AWS_REDSHIFT_RESULTS_MANIFEST);
			e.printStackTrace(System.out);
		}
	}

}
