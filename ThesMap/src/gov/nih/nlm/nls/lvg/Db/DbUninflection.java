package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to find uninflected term from
* Inflection and CitationLowerCase tables in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        InflectionRecord
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbUninflection 
{
    /**
    * Get all uninflected terms from inflection records for a specified term
    * from LVG database.
    *
    * @param  inStr  term for finding uninflected term.
    * @param  conn  database connection
    *
    * @return  all uninflected term from inflection records for a specified
    * term
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetUninflections(String inStr, 
        Connection conn) throws SQLException
    {
        // retireve all record by inflected terms
        Vector<InflectionRecord> out 
            = DbInflectionUtil.GetRecordsByIfTerm(inStr, conn, false);
        UninflectionComparator<InflectionRecord> uc 
            = new UninflectionComparator<InflectionRecord>();
        Collections.sort(out, uc);
        return out;
    }
    /**
    * Check if a specified term is an uninflected term in LVG database
    *
    * @param  unTerm  a term to be checked
    * @param  conn  database connection
    *
    * @return  true or false if the specified term does or does not exist in
    * LVG database, Inflection table
    *
    * @exception  SQLException if there is a database error happens
    */
    public static boolean IsExistUninflectedTerm(String unTerm, 
        Connection conn) throws SQLException
    {
        if(unTerm == null)
        {
            return false;
        }
        boolean existInTable = false;
        String query = "SELECT unTerm FROM Inflection WHERE unTermLC = ?"; 
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, unTerm.toLowerCase());
        // get the boolean value of execute in table inflection
        ResultSet rs = ps.executeQuery();
        if(rs.next() == true)
        {
            existInTable = true;
        }
        // Clean up
        rs.close();
        ps.close();
        return existInTable;
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "oth";
        if(args.length == 1)
        {
            testStr = args[0];
        }
        // read in configuration file
        Configuration conf = new Configuration("data.config.lvg", true);
        // obtain a connection
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                // test for methods
                System.out.println("--- " + testStr + ": " 
                    + IsExistUninflectedTerm(testStr, conn));
                    
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
