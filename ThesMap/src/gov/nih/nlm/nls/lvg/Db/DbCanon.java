package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to Canon table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        CanonRecord
* @see <a href="../../../../../../../designDoc/UDF/database/canonicalTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbCanon 
{
    /**
    * Get all canon records for an uninflected term.
    *
    * @param  unTerm  uninflected term
    * @param  conn  database connection
    *
    * @return  all canon records with the uninflected term is the same as unTerm
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<CanonRecord> GetCanons(String unTerm, Connection conn) 
        throws SQLException
    {
        String query = 
            "SELECT unTerm, canTerm, canonId FROM Canonical WHERE unTerm = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, unTerm);
        Vector<CanonRecord> canons = new Vector<CanonRecord>();
        // get data from itable inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            CanonRecord canonRecord = new CanonRecord();
            canonRecord.SetUnInflectedTerm(rs.getString(1));
            canonRecord.SetCanonicalizedTerm(rs.getString(2));
            canonRecord.SetCanonicalId(rs.getInt(3));
            canons.addElement(canonRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        return (canons);
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "left";
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
                // test for inflection terms
                Vector<CanonRecord> canonList = GetCanons(testStr, conn);
                
                System.out.println("----- Total canonicalized forms found: " +
                    canonList.size());
                for(int i = 0; i < canonList.size(); i++)
                {
                    CanonRecord canon = canonList.elementAt(i);
                    System.out.println("=== Found Canonicalized Term ===");
                    System.out.println(canon.GetUninflectedTerm() + "|" +
                        canon.GetCanonicalizedTerm() + "|" +
                        canon.GetCanonicalId());
                }
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
