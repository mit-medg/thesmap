package edu.mit.csail.medg.thesmap;

/**
 * This implements a connector to save to an MySQL database.
 * @author mwc
 *
 */

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class ResourceConnectorDB {

	Connection conn = null;
	Statement stmt = null;
	PreparedStatement query = null;
	static final String insertStmt = "LOAD DATA LOCAL INFILE ? INTO TABLE thesmap.annotations FIELDS TERMINATED by ',' enclosed by '\"' lines terminated by '\n';";
	
	public ResourceConnectorDB() {
		ThesProps prop = ThesMap.prop;
		String resultHost =  prop.getProperty(ThesProps.resultHostName);
		String resultDb =  prop.getProperty(ThesProps.resultDbName);
		String resultUser =  prop.getProperty(ThesProps.resultUserName);
		String resultPassword = prop.getProperty(ThesProps.resultPasswordName);
		String dbUrl = "jdbc:mysql://" + resultHost + "/" + resultDb;
		U.log("Trying to open connection to "+resultDb+" on " +resultHost + " via "+resultUser+"/"+resultPassword);
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, resultUser, resultPassword);
			query = conn.prepareStatement(insertStmt);
			SemanticEntity.init(this);
		}
		catch (ClassNotFoundException e) {
			System.err.println("Unable to load MySQL driver: " + e);
		}
		catch (SQLException e) {
			System.err.println("Unable to connect to database \"" + resultDb + "\" on " 
								+ resultHost + " as \"" + resultUser + "\": " + e.getMessage());
		}
	}
	
	
	/**
	 * Save the csv file to the MySQL database.
	 * 
	 * @param normalizedPhrase
	 * @return
	 */
	public void saveCSVToDB(File csvFile) {
		try {
			query.setString(1, csvFile.getAbsolutePath());
			query.executeQuery();
		} catch (SQLException e) {
			System.err.println("SQL Error loading file: \""
					+ csvFile.getAbsolutePath() + "\": " + e.getMessage());
		}
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
