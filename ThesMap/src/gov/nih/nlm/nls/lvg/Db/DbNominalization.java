package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to Nominalization table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        SynonymRecord
* @see <a href="../../../../../../../designDoc/UDF/database/nominalizationTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbNominalization 
{
    /**
    * Get all nominalization records for a specified term from LVG database.
    *
    * @param  inStr  input term of nominalization, base
    * @param  conn  database connection
    *
    * @return  all nominalization records for a speficied term, inStr
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<NominalizationRecord> GetNominalizations(String inStr, 
        Connection conn) throws SQLException
    {
        String query = "SELECT nomTerm1, eui1, cat1, nomTerm2, eui2, cat2 "
            + "FROM Nominalization WHERE nomTerm1 = ?";
            
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        Vector<NominalizationRecord> nominalizations 
            = new Vector<NominalizationRecord>();
        // get data from itable inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            NominalizationRecord nominalizationRecord = new NominalizationRecord();
            nominalizationRecord.SetNominalization1(rs.getString(1));  // nomTerm1
            nominalizationRecord.SetEui1(rs.getString(2));            // eui1
            nominalizationRecord.SetCat1(rs.getInt(3));               // cat1
            nominalizationRecord.SetNominalization2(rs.getString(4));  // nomTerm2
            nominalizationRecord.SetEui2(rs.getString(5));            // eui2
            nominalizationRecord.SetCat2(rs.getInt(6));               // cat2
            nominalizations.addElement(nominalizationRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        query = "SELECT nomTerm2, eui2, cat2, nomTerm1, eui1, cat1 "
            + "FROM Nominalization WHERE nomTerm2 = ?";
            
        ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        // get data from itable inflection
        rs = ps.executeQuery();
        while(rs.next())
        {
            NominalizationRecord nominalizationRecord = new NominalizationRecord();
            nominalizationRecord.SetNominalization1(rs.getString(1));  // nomTerm1
            nominalizationRecord.SetEui1(rs.getString(2));            // eui1
            nominalizationRecord.SetCat1(rs.getInt(3));               // cat1
            nominalizationRecord.SetNominalization2(rs.getString(4));  // nomTerm2
            nominalizationRecord.SetEui2(rs.getString(5));            // eui2
            nominalizationRecord.SetCat2(rs.getInt(6));               // cat2
            nominalizations.addElement(nominalizationRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        NominalizationComparator<NominalizationRecord> nc 
            = new NominalizationComparator<NominalizationRecord>();
        Collections.sort(nominalizations, nc);
        return (nominalizations);
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "active";
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
                // Get Nominalizations
                Vector<NominalizationRecord> nominalizationList 
                    = GetNominalizations(testStr, conn);
                System.out.println("----- Total Nominalization found: " +
                        nominalizationList.size());
                for(int j = 0; j < nominalizationList.size(); j++)
                {
                    NominalizationRecord rec = nominalizationList.elementAt(j);
                    System.out.println("=== Found Nominalizations ===");
                    System.out.println(rec.GetNominalization1() + "|"
                        + rec.GetEui1() + "|" + rec.GetCat1() 
                        + "|" + rec.GetNominalization2() + "|" 
                        + rec.GetEui2() + "|" + rec.GetCat2());
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
