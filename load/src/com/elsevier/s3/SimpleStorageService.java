package com.elsevier.s3;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import com.elsevier.common.Variables;

/**
 * Class with static method to put object in an S3 bucket.
 * 
 * @author Darin McBeath
 *
 */
public class SimpleStorageService {

	// Create the S3 client
	private static AWSCredentials awsCredentials = new BasicAWSCredentials(Variables.AWS_ACCESS_KEY, Variables.AWS_SECRET_KEY);
	private static ClientConfiguration cCfg = new ClientConfiguration().withConnectionTimeout(Variables.CONNECTION_TIMEOUT).withMaxErrorRetry(Variables.MAX_ERROR_RETRY).withSocketTimeout(Variables.SOCKET_TIMEOUT);
	private static AmazonS3 s3Client = new AmazonS3Client(awsCredentials, cCfg);

	// Set the S3 client endpoint
	static { s3Client.setEndpoint(Variables.S3_ENDPOINT); }	

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");

	public static void main(String[] args)  {

		//getMetadata(Variables.S3_HTML_OUTLINE_BUCKET_NAME, "B9780000002082001464");
		try {
			System.out.println(IOUtils.toString(SimpleStorageService.getObject("affiliation-xml","101940415")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * Get the object from the specified S3 bucket and key value.
	 * 
	 * @param bucketName S3 bucket name
	 * @param key S3 key value
	 * @return S3 object contents
	 */
	public static InputStream getObject(String bucketName, String key) {

			S3Object object = s3Client.getObject(new GetObjectRequest(bucketName, key));
			return object.getObjectContent();

	}


	/**
	 * Get the metadata for the object from the specified S3 bucket and key value.
	 * 
	 * @param bucketName S3 bucket name
	 * @param key S3 key value
	 */
	public static void getMetadata(String bucketName, String key) {

		ObjectMetadata om = s3Client.getObjectMetadata(bucketName, key);

		// Get default metadata
		System.out.println("bucket = '" + bucketName + "'");
		System.out.println("key = '" + key + "'");
		System.out.println("length = '" + om.getContentLength() + "'");
		System.out.println("encoding = '" + om.getContentEncoding() + "'");
		System.out.println("type = '" + om.getContentType() + "'");
		System.out.println("modified = '" + sdf.format(om.getLastModified()) + "'");

		// Get user metadata
    	Map<String,String> userMetadata = om.getUserMetadata();
		String[] userKeys = new String[userMetadata.size()];
		userKeys = (String[])(userMetadata.keySet().toArray(userKeys));
		for (int i=0; i<userKeys.length; i++) {
			System.out.println(userKeys[i] + " = '" + userMetadata.get(userKeys[i]) + "'");
		}

	}

}