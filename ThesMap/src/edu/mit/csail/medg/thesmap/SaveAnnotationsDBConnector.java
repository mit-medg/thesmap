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


public class SaveAnnotationsDBConnector {

	Connection conn = null;
	Statement stmt = null;
	PreparedStatement insertStmt = null;
	static final String loadDBStmt = "LOAD DATA LOCAL INFILE ? INTO TABLE thesmap.annotations FIELDS TERMINATED by ',' enclosed by '\"' lines terminated by '\n';";
	static final String insertDBStmt = "INSERT INTO ANNOTATIONS (start, end, cui, tui, preferredText, annotatorFlag, fileName) values (?, ?, ?, ?, ?, ?, ?);";
	
	public SaveAnnotationsDBConnector() {
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
			insertStmt = conn.prepareStatement(insertDBStmt);
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
	 * Insert into the database for each annotation.
	 * osw.write(ann.begin + "," + ann.end + "," + i.cui + "," + i.tui 
						+ ",\"" + fixq(preferredText) + "\"," + i.annotatorValue + "," + docID + "\n");
	 */
	public void insertEntry(int start, int end, String cui, String tui, String preferredText, int annotatorValue, String docID) {
		try {
			insertStmt.setInt(1, start);
			insertStmt.setInt(2, end);
			insertStmt.setString(3, cui);
			insertStmt.setString(4, tui);
			insertStmt.setString(5, preferredText);
			insertStmt.setInt(6, annotatorValue);
			insertStmt.setString(7, docID);
			insertStmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("SQL Error saving for doc: \"" + docID + "\": " + e.getMessage());
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
			insertStmt = conn.prepareStatement(loadDBStmt);
			insertStmt.setString(1, csvFile.getAbsolutePath());
			insertStmt.executeQuery();
		} catch (SQLException e) {
			System.err.println("SQL Error loading file: \""
					+ csvFile.getAbsolutePath() + "\": " + e.getMessage());
		}
	}
	
	public void close() {
		if (conn!=null) {
			U.p("Closing MySQL Connection.");
			try{
				if (insertStmt!=null) insertStmt.close();
			} catch (SQLException e) {}
			try{
				if (conn!=null) conn.close();
			} catch (SQLException e) {}
		}
	}
	
}
