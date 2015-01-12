package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides base interfaces to LVG database.  This class utilize
* Java DataBase Connectivity (JDBC) to connection to LVG database 
* (Instant DB).
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class DbBase 
{
    // public methods
    /**
    * Open a connection using URL specified in configuration bundle
    *
    * @param  config  configuration bundle
    *
    * @return  a connection (session) with the specific database
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Connection OpenConnection(Configuration config) 
        throws SQLException
    {
        String driverName = GetDbDriverFromConfig(config);
        String url = GetDbUrlFromConfig(config);
        Connection conn = null;        // Connect to DB
        LoadDbDriver(driverName);      // Load Driver
        String userName = GetDbUserNameFromConfig(config);
        String password = GetDbPasswordFromConfig(config);
        conn = DriverManager.getConnection(url, userName, password);
        return conn;
    }
    /**
    * Open a connection to a specified URL of the database and specifying
    * a JDBC driver
    *
    * @param  driverName  JDBC connection driver
    * @param  url  JDBC connection URL
    * @param  userName  user name for the DB
    * @param  password  password for the DB
    *
    * @return  a connection (session) with the specific database
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Connection OpenConnection(String driverName, String url,
        String userName, String password) throws SQLException
    {
        Connection conn = null;        // Connect to DB
        LoadDbDriver(driverName);      // Load Driver
        conn = DriverManager.getConnection(url, userName, password);
        return conn;
    }
    /**
    * Close a specified connection (session) with a database
    *
    * @param  conn  database connection
    *
    * @exception  SQLException if there is a database error happens
    */
    public static void CloseConnection(Connection conn) 
        throws SQLException
    {
        conn.close();
    }
    /**
    * Close a specified connection (session) with a database
    *
    * @param  conn  database connection
    * @param  config  configuration bundle
    *
    * @exception  SQLException if there is a database error happens
    */
    public static void CloseConnection(Connection conn, Configuration config) 
        throws SQLException
    {
        String dbStr = config.GetConfiguration(Configuration.DB_TYPE);
        conn.close();
    }
    /**
    * Close a specified connection (session) with a database
    *
    * @param  conn  database connection
    * @param  config  configuration bundle
    *
    * @exception  SQLException if there is a database error happens
    */
    public static void ShutdownDb(Connection conn, Configuration config) 
        throws SQLException
    {
        String dbStr = config.GetConfiguration(Configuration.DB_TYPE);
        // HSqlDb requires SHUTDOWN command before closing connection
        if(dbStr.equals("HSQLDB") == true)
        {
            ExecuteDdl(conn, "SHUTDOWN");
        }
        conn.close();
    }
    /**
    * Format a regular string to a legal SQL string for "'".
    *
    * @param  in  a regualr string used as SQL
    *
    * @return  a legal SQL string
    */
    public static String FormatSqlStr(String in)
    {
        String out = Str.Replace(in, "'", "''");
        return out;
    }
    // package method
    /**
    * Submit a query to a specific database.
    *
    * @param query  a SQL query to be submitted
    */
    static void SubmitDMLs(String query, Configuration config)
    {
        // Connect to DB
        Connection conn = null;
        // Load Driver
        String driverName = GetDbDriverFromConfig(config);
        LoadDbDriver(driverName);
        // obtain a connection
        try
        {
            String url = GetDbUrlFromConfig(config);
            String userName = GetDbUserNameFromConfig(config);
            String password = GetDbPasswordFromConfig(config);
            conn = DriverManager.getConnection(url, userName, password);
            Statement statement = conn.createStatement();
            statement.executeQuery(query);
            // Clean up
            statement.close();
            conn.close();
        }
        catch (SQLException sqle)
        {
            System.err.println(sqle.getMessage());
            if(conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (SQLException e)
                {
                    System.err.println("SQLException: " + e.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    // private method
    // Load the Lvg database (Instant DB) driver by specifying a driverName
    private static void LoadDbDriver(String driverName)
    {
        // Load Driver
        try
        {
            Class.forName(driverName).newInstance();
        }
        catch (Exception e)
        {
            System.err.println("** Error: Unable to load driver (" 
                + driverName + ").");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
    // Get JDBC driver from configuration file
    private static String GetDbUserNameFromConfig(Configuration config)
    {
        String userName = config.GetConfiguration(Configuration.DB_USERNAME);
        return userName;
    }
    private static String GetDbPasswordFromConfig(Configuration config)
    {
        String password = config.GetConfiguration(Configuration.DB_PASSWORD);
        return password;
    }
    private static String GetDbDriverFromConfig(Configuration config)
    {
        String driverName = config.GetConfiguration(Configuration.DB_DRIVER);
        return driverName;
    }
    // Get database URL from configuration file
    private static String GetDbUrlFromConfig(Configuration config)
    {
        String dbStr = config.GetConfiguration(Configuration.DB_TYPE);
        String url = null;
        if(dbStr.equals("HSQLDB") == true)
        {
            //url = "jdbc:hsqldb:file:"
            url = "jdbc:hsqldb:"
                + config.GetConfiguration(Configuration.LVG_DIR)
                + "data/HSqlDb/"
                + config.GetConfiguration(Configuration.DB_NAME);
        }
        else if(dbStr.equals("MYSQL") == true)
        {
            url = "jdbc:mysql://" 
                + config.GetConfiguration(Configuration.DB_HOST) + "/" 
                + config.GetConfiguration(Configuration.DB_NAME);
        }
        else if(dbStr.equals("OTHER") == true)
        {
            url = config.GetConfiguration(Configuration.JDBC_URL);
        }
        return url;
    }
    // DDL: data Definition Language
    private static void ExecuteDdl(Connection conn, String query)
    {
        try
        {
            Statement stmt = conn.createStatement();
            stmt.execute(query);
            stmt.close();
        }
        catch (SQLException e)
        {
            System.out.println("** Error: SQLException: " + e.getMessage());
            System.out.println("** Error: SQLState:     " + e.getSQLState());
            System.out.println("** Error: VendorError:  " + e.getErrorCode());
        }
    }
}
