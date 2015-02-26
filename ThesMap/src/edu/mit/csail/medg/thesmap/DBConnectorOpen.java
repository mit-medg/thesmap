package edu.mit.csail.medg.thesmap;

/**
 * This implements a connector to save to an MySQL database.
 * @author mwc
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConnectorOpen {

	Connection conn = null;
	Statement stmt = null;
	PreparedStatement query = null;
	String host;
	String db;
	String user;
	String pwd;
	
	public DBConnectorOpen() {
		ThesProps prop = ThesMap.prop;
		host = prop.getProperty(ThesProps.sourceHostName);
		db =  prop.getProperty(ThesProps.sourceDbName);
		user =  prop.getProperty(ThesProps.sourceUserName);
		pwd = prop.getProperty(ThesProps.sourcePasswordName);
		
		String dbUrl = "jdbc:mysql://" + host + "/" + db;
		U.log("Trying to open connection to "+db+" on " +host + " via "+user+"/"+pwd);
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, user, pwd);
		}
		catch (ClassNotFoundException e) {
			System.err.println("Unable to load MySQL driver: " + e);
		}
		catch (SQLException e) {
			System.err.println("Unable to connect to database \"" + db + "\" on " 
								+ host + " as \"" + user + "\": " + e.getMessage());
		}
	}
	
	
	public DBConnectorOpen(String hostName, String dbName, String username, String pwd) {
		host = hostName; 
		db = dbName; 
		user = username;
		this.pwd = pwd;
		
		String dbUrl = "jdbc:mysql://" + host + "/" + db;
		U.log("Trying to open connection to "+db+" on " +host + " via "+user+"/"+pwd);
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, user, pwd);
			
		}
		catch (ClassNotFoundException e) {
			System.err.println("Unable to load MySQL driver: " + e);
		}
		catch (SQLException e) {
			System.err.println("Unable to connect to database \"" + db + "\" on " 
								+ host + " as \"" + user + "\": " + e.getMessage());
		}
	}
	
	
	/**
	 * Run the specified SQL command.
	 * 
	 * @param sqlStatement - String that is in the proper SQL format.
	 * @return ResultSet from the sql query.
	 */
	public ResultSet processSQL(String stmt) {
		try {
			query = conn.prepareStatement(stmt);
			ResultSet rs = query.executeQuery();
			return rs;
		} catch (SQLException e) {
			System.err.println("SQL Error running command: \""
					+ stmt + "\": " + e.getMessage());
		}
		return null;
	}
	
	public void close() {
		if (conn!=null) {
			U.p("Closing MySQL Connection.");
			try{
				if (query!=null) query.close();
			} catch (SQLException e) {}
			try{
				if (conn!=null) conn.close();
			} catch (SQLException e) {}
		}
	}
	
}
