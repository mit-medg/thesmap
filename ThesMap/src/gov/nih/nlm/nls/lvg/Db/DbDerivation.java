package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to derivation table in LVG database.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-15, chlu, 07-23-12, add derivation type options.
* <li>SCR-20, chlu, 07-23-12, add derivation negation options.
* </ul>
*
* @author NLM NLS Development Team
*
* @see        DerivationRecord
* @see 
* <a href="../../../../../../../designDoc/UDF/database/derivationTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbDerivation 
{
    /**
    * Get derivation records for a specific term (lower case) from LVG database.
    * Two SQL queries are performed to get bi-directional
    * search: one uses termLc1 as key, one uses termLc2 as key.
    *
    * @param  inStr  term for derivations (lower case)
    * @param  conn  database connection
    * @param  typeFlag  type flag: 0 (Z), 1 (P), 2(S), 3(ZP), 4 (ZS), 5 (PS), 
    * 6 (ZPS)
    * @param  negationFlag  negation tag: N|O
    *
    * @return  all derivation records for the specified term, inStr, with option
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<DerivationRecord> GetDerivations(String inStr, 
        Connection conn, int typeFlag, int negationFlag) throws SQLException
    {
        Vector<DerivationRecord> derivations = new Vector<DerivationRecord>();
        // Forward: exclude negation
        String query = "SELECT term1, cat1, eui1, term2, cat2, eui2, type, negation, prefix FROM Derivation WHERE termLc1 = ?";
        String tagQuery = AddTagToQuery(query, typeFlag, negationFlag);    
            
        PreparedStatement ps = conn.prepareStatement(tagQuery);
        ps.setString(1, inStr.toLowerCase());
        // get data from table derivation
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            DerivationRecord derivationRecord = new DerivationRecord(
                rs.getString(1),    // term1
                rs.getInt(2),       // cat1
                rs.getString(3),    // eui1
                rs.getString(4),    // term2
                rs.getInt(5),       // cat2
                rs.getString(6),    // eui2
                rs.getString(7),    // type
                rs.getString(8),    // negation
                rs.getString(9));    // prefix
            derivations.addElement(derivationRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // Backward, reversed way: exclude negation
        String query2 = "SELECT term2, cat2, eui2, term1, cat1, eui1, type, negation, prefix FROM Derivation WHERE termLc2 = ?";
        String tagQuery2 = AddTagToQuery(query2, typeFlag, negationFlag);
        PreparedStatement ps2 = conn.prepareStatement(tagQuery2);
        ps2.setString(1, inStr.toLowerCase());
        // get data from table derivation
        ResultSet rs2 = ps2.executeQuery();
        while(rs2.next())
        {
            DerivationRecord derivationRecord = new DerivationRecord(
                rs2.getString(1),    // term2
                rs2.getInt(2),       // cat2
                rs2.getString(3),    // eui2
                rs2.getString(4),    // term1
                rs2.getInt(5),       // cat1
                rs2.getString(6),    // eui1
                rs2.getString(7),    // type
                rs2.getString(8),    // negation
                rs2.getString(9));    // prefix
            derivations.addElement(derivationRecord);
        }
        // Clean up
        rs.close();
        ps2.close();
        return derivations;
    }

    /**
    * Get derivation records for a specific term (lower case) from LVG database.
    * Two SQL queries are performed to get bi-directional
    * search: one uses termLc1 as key, one uses termLc2 as key.
    *
    * @param  inStr  term for derivations (lower case)
    * @param  conn  database connection
    *
    * @return  all non-negative derivation records for the specified term, inStr
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<DerivationRecord> GetDerivations(String inStr, 
        Connection conn) throws SQLException
    {
        // use all types and otherwise negation as default
        Vector<DerivationRecord> derivations  
            = GetDerivations(inStr, conn, OutputFilter.D_TYPE_ALL,
                OutputFilter.D_NEGATION_OTHERWISE);
        return derivations;
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "multiple";
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
                // Get Derivations
                Vector<DerivationRecord> derivationList 
                    = GetDerivations(testStr, conn);
                
                System.out.println("----- Total Derivations found: " +
                    derivationList.size());
                for(int i = 0; i < derivationList.size(); i++)
                {
                    if(i == 0)
                    {
                        System.out.println("=== Found Derivations ===");
                    }
                    DerivationRecord record = derivationList.elementAt(i);
                    System.out.println(record.GetSource() + "|" +
                        record.GetSourceCat() + "|" + record.GetTarget() + "|" +
                        record.GetTargetCat());
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

    // private methods
    private static String AddTagToQuery(String query, int typeFlag, 
        int negationFlag)
    {
        String tagQuery = query;

        // negation
        switch(negationFlag)
        {
            case OutputFilter.D_NEGATION_NEGATIVE:
                tagQuery += " and negation = 'N'";
                break;
            case OutputFilter.D_NEGATION_BOTH:
                // do nothing because no restrict on the derivation negationn
                break;
            case OutputFilter.D_NEGATION_OTHERWISE:
            default:
                tagQuery += " and negation = 'O'";
                break;
        }
    
        // type
        // PSZ: no restriction
        switch(typeFlag)
        {
            case OutputFilter.D_TYPE_ZERO:
                tagQuery += " and type = 'Z'";
                break;
            case OutputFilter.D_TYPE_PREFIX:
                tagQuery += " and type = 'P'";
                break;
            case OutputFilter.D_TYPE_SUFFIX:
                tagQuery += " and type = 'S'";
                break;
            case OutputFilter.D_TYPE_ZERO_PREFIX:
                tagQuery += " and type <> 'S'";
                break;
            case OutputFilter.D_TYPE_ZERO_SUFFIX:
                tagQuery += " and type <> 'P'";
                break;
            case OutputFilter.D_TYPE_SUFFIX_PREFIX:
                tagQuery += " and type <> 'Z'";
                break;
            case OutputFilter.D_TYPE_ALL:
            default:
                // do nothing because no restrict on the derivation type
                break;
        }

        return tagQuery;
    }
}
