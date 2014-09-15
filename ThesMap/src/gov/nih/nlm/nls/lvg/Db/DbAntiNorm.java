package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to AntiNorm table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        AntiNormRecord
* @see <a href="../../../../../../../designDoc/UDF/database/antiNormTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbAntiNorm 
{
    /**
    * Get all antiNorm records for an nromalized term.
    *
    * @param  normTerm  normalized term
    * @param  conn  database connection
    *
    * @return  all antiNorm records with the normalized term is the same
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<AntiNormRecord> GetAntiNorms(String normTerm, 
        Connection conn) throws SQLException
    {
        String query = "SELECT normTerm, inflTerm, termCat, termInfl, eui "
            + "FROM AntiNorm WHERE normTerm = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, normTerm);
        Vector<AntiNormRecord> antiNorms = new Vector<AntiNormRecord>();
        // get data from itable inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            AntiNormRecord antiNormRecord = new AntiNormRecord();
            antiNormRecord.SetNormalizedTerm(rs.getString(1));   // normTerm
            antiNormRecord.SetInflectedTerm(rs.getString(2));    //inflTerm
            antiNormRecord.SetCategory(rs.getInt(3));            // termCat
            antiNormRecord.SetInflection(rs.getLong(4));         // termInfl
            antiNormRecord.SetEui(rs.getString(5));              // eui
            antiNorms.addElement(antiNormRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        AntiNormComparator<AntiNormRecord> ac 
            = new AntiNormComparator<AntiNormRecord>();
        Collections.sort(antiNorms, ac);
        return antiNorms;
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "disease osler rendu";
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
                Vector<AntiNormRecord> antiNormList 
                    = GetAntiNorms(testStr, conn);
                
                System.out.println("----- Total antiNorm forms found: " +
                    antiNormList.size());
                for(int i = 0; i < antiNormList.size(); i++)
                {
                    AntiNormRecord antiNorms = antiNormList.elementAt(i);
                    System.out.println("=== Found AntiNorm ===");
                    System.out.println(antiNorms.GetNormalizedTerm() + "|"
                        + antiNorms.GetInflectedTerm() + "|" 
                        + antiNorms.GetCategory() + "|" 
                        + antiNorms.GetInflection() + "|" 
                        + antiNorms.GetEui());
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
