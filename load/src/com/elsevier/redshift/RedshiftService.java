package com.elsevier.redshift;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import java.sql.*;

import com.elsevier.common.Variables;


/**
 * Class with static methods to handle interactions with SQS.  The main is used for testing/debugging.
 * The remaining methods would be used by other classes.
 * 
 * @author Darin McBeath
 *
 */
public class RedshiftService {

	private static String redshiftId = Variables.AWS_REDSHIFT_ID;
	private static String redshiftPswd = Variables.AWS_REDSHIFT_PSWD;
	private static String redshiftConnectURL = Variables.AWS_REDSHIFT_CONNECT_URL;

	
	// Static block to prime the JDBC driver for Redshift
	static{
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to load JDBC Driver for RedshiftService");
			e.printStackTrace(System.out);
			System.exit(-1);
		}
	}
	   
	   
	/**
	 * Main to test some basic functionality for Redshift.  Used to make sure records  
	 * can be added/deleted.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
        
		
		ArrayList<String> values = new ArrayList<String>();
        values.add("456");
        values.add("10");
		replaceRecord("corestoauthors", "1", values);
		
		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("5");
		replaceRecord("corestoauthors", "1", values2);

		//deleteRecord("corestoauthors", "1");
		
		System.out.println("Done!!");
	}

	
	
	
	/**
	 * Update the underlying Redshift table with a new set of values for the specified key
	 * 
	 * @param tableName tablename in Redshift
	 * @param key key/partial key for the record to replace in Redshift
	 * @param values value(s) to associate with the specified key in the table.
	 * 
	 */
	public static boolean replaceRecord(String tableName, String key, List<String> values) {
		// 
		if (deleteRecord(tableName, key) == false)
			return false;
		else if (addRecord(tableName, key, values))
			return false;
		else
			return true;
	}

	
	/**
	 * Add the provided key and values into specified table. Note, any pre-existing values will NOT be deleted.  
	 * 
	 * @param tableName tablename in Redshift
	 * @param key key/partial key for the record to update in Redshift
	 * @param values value(s) to associate with the specified key in the table.
	 * 
	 */
	public static boolean addRecord(String tableName, String key, List<String> values) {
		try {
			Connection db = DriverManager.getConnection(redshiftConnectURL, redshiftId, redshiftPswd);
			db.setAutoCommit(false);
			Statement st = db.createStatement();	
			for (int x = 0; x < values.size(); x++) {
				String value = values.get(x);
				int rowsInserted = st.executeUpdate("INSERT INTO  " +  tableName + " values(" + key + "," + value +")");
			}
			st.close();
			db.commit();
			db.close();
		}
		catch(SQLException e) {
			System.out.println("Exception adding Redshift record!");
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}
	
	/**
	 * Remove the values associated with the provided key.  
	 * 
	 * @param tableName tablename in Redshift
	 * @param key key/partial key for the record to delete in Redshift
	 * 
	 */
	public static boolean deleteRecord(String tableName, String key) {
		try {
			Connection db = DriverManager.getConnection(redshiftConnectURL, redshiftId, redshiftPswd);
			db.setAutoCommit(false);
			Statement st = db.createStatement();
			int rowsDeleted = st.executeUpdate("DELETE FROM " +  tableName + " WHERE eid = " + key);
			System.out.println("Deleted " + rowsDeleted + " rows.");
			st.close();
			db.commit();
			db.close();
		}
		catch(SQLException e) {
			System.out.println("Exception deleting Redshift record!");
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

}
