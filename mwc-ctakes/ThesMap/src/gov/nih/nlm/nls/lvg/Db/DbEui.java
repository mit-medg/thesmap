package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to get EUI from inflection and
* CitationLowerCase tables table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbEui 
{
    /**
    * Get all EUIs (string) for an inflected term from LVG database.
    *
    * @param  ifTerm  an inflected term for finding EUIs
    * @param  conn  database connection
    *
    * @return  all EUIs in String for the inflected term, ifTerm.
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<EuiRecord> GetEuisByInflectedTerm(String ifTerm, 
        Connection conn) throws SQLException
    {
        String query = "SELECT eui, termCat, termInfl FROM Inflection WHERE "
            + "ifTermLC = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, ifTerm.toLowerCase());
        Vector<EuiRecord> out = GetEuisByPreparedStatement(ps);
        return out;
    }
    /**
    * Get all EUIs (string) for an uninflected term from LVG database.
    *
    * @param  unTerm  an uninflected term for finding EUIs
    * @param  conn  database connection
    *
    * @return  all EUIs in String for the uninflected term, unTerm.
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<EuiRecord> GetEuisByUnflectedTerm(String unTerm, 
        Connection conn) throws SQLException
    {
        // Problem: No Lowercase field for uninflected term. 
        // Thus case is sensitive
        String query = "SELECT eui, termCat, termInfl FROM Inflection WHERE " 
            + "unTermLC = ?";
            
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, unTerm.toLowerCase());
        Vector<EuiRecord> out = GetEuisByPreparedStatement(ps);
        return out;
    }
    /**
    * Get all EUIs (string) for an uninflected term with specific category
    * from LVG database.
    *
    * @param  unTerm  an uninflected term for finding EUIs
    * @param  category  a specified category in a integer format
    * @param  conn  database connection
    *
    * @return  an EUI in a String format for the specified uninflected term
    * and category.
    *
    * @exception  SQLException if there is a database error happens
    */
    public static String GetEuisByUnflectedTermCat(String unTerm, int category,
        Connection conn) throws SQLException
    {
        // Problem: No Lowercase field for uninflected term. 
        // Thus case is sensitive
        String query = "SELECT eui, termCat, termInfl FROM Inflection WHERE " 
            + "unTermLC = '" + DbBase.FormatSqlStr(unTerm.toLowerCase()) 
            + "' AND termCat = " + category;
        Vector<EuiRecord> out = GetEuis(query, conn);
        String outString = new String();
        if(out.size() == 0)
        {
            outString = "No EUI found";
        }
        else
        {
            for(int i = 0; i < out.size(); i++)
            {
                outString += out.elementAt(i).GetEui() + ",";
            }
        }
        return outString.substring(0, outString.length()-1);
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "color";
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
                // test for Eui
                Vector<EuiRecord> euiList 
                    = GetEuisByInflectedTerm(testStr, conn);
                
                System.out.println("----- Input term: " + testStr);
                System.out.println("-- Number of Inflected Eui: " + 
                    euiList.size());
                for(int i = 0; i < euiList.size(); i++)
                {
                    System.out.println(i + ". EUI: " 
                        + euiList.elementAt(i).GetEui()); 
                }
                // test for uninflect term Eui
                Vector<EuiRecord> euiList1 
                    = GetEuisByUnflectedTerm(testStr, conn);
                
                System.out.println("-- Number of UnInflected Eui: " + 
                    euiList1.size());
                for(int i = 0; i < euiList1.size(); i++)
                {
                    System.out.println(i + ". EUI: " 
                        + euiList1.elementAt(i).GetEui()); 
                }
                // test for uninflect term Eui with Category
                String euiList2 = 
                    GetEuisByUnflectedTermCat(testStr, 128, conn);
                System.out.println("EUI: " + euiList2); 
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
    // private methods
    private static Vector<EuiRecord> GetEuis(String query, Connection conn) 
        throws SQLException
    {
        EuiVector<EuiRecord> euis = new EuiVector<EuiRecord>();
        // get data from table inflection
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while(rs.next())
        {
            EuiRecord eui = new EuiRecord();
            eui.SetEui(rs.getString(1));        // eui
            eui.SetCategory(rs.getInt(2));      // termCat
            eui.SetInflection(rs.getLong(3));   //termInfl
            euis.Add(eui);
        }
        // Clean up
        rs.close();
        statement.close();
        // sort
        EuiComparator<EuiRecord> ec = new EuiComparator<EuiRecord>();
        Collections.sort(euis, ec);
        return euis;
    }
    private static Vector<EuiRecord> GetEuisByPreparedStatement(
        PreparedStatement ps) throws SQLException
    {
        EuiVector<EuiRecord> euis = new EuiVector<EuiRecord>();
        // get data from table inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            EuiRecord eui = new EuiRecord();
            eui.SetEui(rs.getString(1));        // eui
            eui.SetCategory(rs.getInt(2));      // termCat
            eui.SetInflection(rs.getLong(3));   //termInfl
            euis.Add(eui);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        EuiComparator<EuiRecord> ec = new EuiComparator<EuiRecord>();
        Collections.sort(euis, ec);
        return euis;
    }
}
