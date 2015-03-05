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
	static final String countStmt = "SELECT COUNT(*) AS rowcount FROM ?"; 
	
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
			U.log("Unable to load MySQL driver: " + e);
		}
		catch (SQLException e) {
			U.log("Unable to connect to database \"" + db + "\" on " 
								+ host + " as \"" + user + "\": " + e.getMessage());
		}
	}
	
	public int countFiles(String table) {
		int count = 0;
		try {
			query = conn.prepareStatement(countStmt);
			query.setString(1, table);

			ResultSet r = query.executeQuery();
			r.next();
			count = r.getInt("rowcount");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return count;
	}
	
	/**
	 * Run the specified SQL command.
	 * 
	 * @param sqlStatement - String that is in the proper SQL format.
	 * @return ResultSet from the sql query.
	 */
	public ResultSet processSQL(String stmt) {
		try {
			// TODO: Should probably validate the SQL command to prevent SQL table drops and other malicious things.
			if (validStatement(stmt)) {
				query = conn.prepareStatement(stmt);
				ResultSet rs = query.executeQuery();
				U.log("Processing SQL Command: " + stmt);
				return rs;
			}
			else { 
				U.log("Not a 'select' SQL command. Please try again.");
			}

		} catch (SQLException e) {
			U.log("SQL Error running command: \""
					+ stmt + "\": " + e.getMessage());
		}
		
		return null;
	}

	/**
	 * Validate the SQL command by checking to see if it is a 'select' statement.
	 * Should enhance to make sure that there aren't other ways of deleting from database.
	 * 
	 * @param stmt - pass in the statement given by user
	 * @return true - if valid; false - this statement could cause harm in database. 
	 */
	protected boolean validStatement(String stmt) {
		int i = stmt.indexOf(" ");
		if (i >= 0) {
			String word = stmt.substring(0, i);
			if (word.equalsIgnoreCase("select")) {
				return true;
			}
		}
		return false;
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
