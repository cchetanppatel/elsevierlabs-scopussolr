package com.elsevier.redshift;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import java.sql.*;

import com.elsevier.common.Variables;


/**
 * Class with static methods to handle JDBC interactions with RedShift.  The main is used for testing/debugging.
 * The remaining methods would be used by other classes.
 * 
 * @author Curt Kohler
 *
 */
public class RedshiftService {

	private static String redshiftId = Variables.AWS_REDSHIFT_ID;
	private static String redshiftPswd = Variables.AWS_REDSHIFT_PSWD;
	private static String redshiftConnectURL = Variables.AWS_REDSHIFT_CONNECT_URL;

	private static String corestoaffiliationsInsertStmt = "INSERT INTO corestoaffiliations (eid, afid, epoch) values(? , ?, ?)";
	private static String corestoaffiliationsDeleteStmt = "DELETE FROM corestoaffiliations WHERE eid = ?";	
	private static String corestoauthorsInsertStmt = "INSERT INTO corestoauthors (eid, auid, epoch) values(? , ?, ?)";
	private static String corestoauthorsDeleteStmt = "DELETE FROM corestoauthors WHERE eid = ?";
	private static String corestoreferencesInsertStmt = "INSERT INTO corestoreferences (eid, refid, epoch) values(? , ?, ?)";
	private static String corestoreferencesDeleteStmt = "DELETE FROM corestoreferences WHERE eid = ?";
	
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
        
		String test = "2-1s-1.1234";
		
		System.out.println(test.substring(test.lastIndexOf('.') + 1));
		
		ArrayList<String> values = new ArrayList<String>();
        values.add("0077");
        values.add("0071");
		replaceRecord("corestoreferences", "0001", 1L, values);
		
		ArrayList<String> values2 = new ArrayList<String>();
		values2.add("005");
		values2.add("006");
		replaceRecord("corestoreferences", "0001", 2L, values2);

		//deleteRecord("corestoreferences", "1");
		
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
	public static boolean replaceRecord(String tableName, String key, long epoch, List<String> values) {
		// 
		if (deleteRecord(tableName, key) == false)
			return false;
		else if (addRecord(tableName, key, epoch, values))
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
	public static boolean addRecord(String tableName, String key, long epoch, List<String> values) {
		try {
			Connection db = DriverManager.getConnection(redshiftConnectURL, redshiftId, redshiftPswd);
			db.setAutoCommit(false);
			PreparedStatement pst = null;
			if (tableName.equalsIgnoreCase("corestoaffiliations")) {
				pst = db.prepareStatement(corestoaffiliationsInsertStmt);	
			} else if (tableName.equalsIgnoreCase("corestoauthors")) {
				pst = db.prepareStatement(corestoauthorsInsertStmt);
			} else if (tableName.equalsIgnoreCase("corestoreferences")) {
				pst = db.prepareStatement(corestoreferencesInsertStmt);
			} else {
				System.out.println("Invalid RedShift insert table value specified!!:  Value: " + tableName);
				return false;
			}
			for (int x = 0; x < values.size(); x++) {
				String value = values.get(x);
				pst.setString(1, key);
				pst.setString(2,  value);
				pst.setLong(3, epoch);
				int rowsInserted = pst.executeUpdate();
				//int rowsInserted = pst.executeUpdate("INSERT INTO  " +  tableName + " values(" + key + "," + value + "," + epoch + ")");
			}
			pst.close();
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
			PreparedStatement pst = null;
			if (tableName.equalsIgnoreCase("corestoaffiliations")) {
				pst = db.prepareStatement(corestoaffiliationsDeleteStmt);	
			} else if (tableName.equalsIgnoreCase("corestoauthors")) {
				pst = db.prepareStatement(corestoauthorsDeleteStmt);
			} else if (tableName.equalsIgnoreCase("corestoreferences")) {
				pst = db.prepareStatement(corestoreferencesDeleteStmt);
			} else {
				System.out.println("Invalid Redshift delete table value specified!!:  Value: " + tableName);
				return false;
			}
			pst.setString(1, key);
			int rowsDeleted = pst.executeUpdate();
			//int rowsDeleted = st.executeUpdate("DELETE FROM " +  tableName + " WHERE eid = " + key);
			System.out.println("Deleted " + rowsDeleted + " rows.");
			pst.close();
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
