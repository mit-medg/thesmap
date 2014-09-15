package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to ProperNoun table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/database/properNounTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbProperNoun 
{
    /**
    * Check if a specified term is a proper noun in LVG database.
    *
    * @param  inStr  term to be checked if a proper noun
    * @param  conn  database connection
    *
    * @return  true or false for the specified term is ro is not a proper noun
    *
    * @exception  SQLException if there is a database error happens
    */
    public static boolean IsProperNoun(String inStr, Connection conn) 
        throws SQLException
    {
        boolean isProperNoun = false;
        String query = "SELECT properNoun FROM ProperNoun WHERE properNoun = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        // get data from table inflection
        ResultSet rs = ps.executeQuery();
        if(rs.next() == true)
        {
            isProperNoun = true;
        }
        // Clean up
        rs.close();
        ps.close();
        return (isProperNoun);
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "Adam";
        if(args.length == 1)
        {
            testStr = args[0];
        }
        System.out.println("--- TestStr:  " + testStr);
        // read in configuration file
        Configuration conf = new Configuration("data.config.lvg", true);
        // obtain a connection
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                // Get ProperNoun
                boolean isProperNoun = IsProperNoun(testStr, conn);
                System.out.println("----- Is ProperNoun: " + isProperNoun);
                System.out.println("----- Is ProperNoun (candy): " 
                    + IsProperNoun("candy", conn));
                DbBase.CloseConnection(conn, conf);
            }
        }
        catch (SQLException sqle)
        {
            System.err.println(sqle.getMessage());
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
}
