package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to Fruitful table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        FruitfulRecord
* @see <a href="../../../../../../../designDoc/UDF/database/fruitfulTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbFruitful 
{
    /**
    * Get all fruitful records for a specified term from LVG database.
    *
    * @param  inStr  key form (lower case)
    * @param  conn  database connection
    *
    * @return  all fruitful variants records for a speficied term, inStr
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<FruitfulRecord> GetFruitfulVariants(String inStr, 
        Connection conn) throws SQLException
    {
        String query = "SELECT termLc, variantTerm, termCat, termInfl, "
            + "orgCat, orgInfl, flowHistory, dist, tagInfo FROM Fruitful "
            + "WHERE termLc = ?";
            
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        Vector<FruitfulRecord> fruitfuls = new Vector<FruitfulRecord>();
        // get data from table fruitful
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            FruitfulRecord fruitfulRecord = new FruitfulRecord();
            fruitfulRecord.SetLowerCasedTerm(rs.getString(1));    // termLc
            fruitfulRecord.SetVariantTerm(rs.getString(2));       // variantTerm
            fruitfulRecord.SetCategory(rs.getInt(3));             // termCat
            fruitfulRecord.SetInflection(rs.getLong(4));          // termInfl
            fruitfulRecord.SetOriginalCategory(rs.getInt(5));     // orgCat
            fruitfulRecord.SetOriginalInflection(rs.getLong(6));  // orgInfl
            fruitfulRecord.SetFlowHistory(rs.getString(7));       // flowHistory
            fruitfulRecord.SetDistance(rs.getInt(8));             // dist
            fruitfulRecord.SetTagInformation(rs.getLong(9));      // tagInfo
            fruitfuls.addElement(fruitfulRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        FruitfulComparator<FruitfulRecord> fc 
            = new FruitfulComparator<FruitfulRecord>();
        Collections.sort(fruitfuls, fc);
        return fruitfuls;
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "neurological";
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
                // Get Fruitful Variants
                Vector<FruitfulRecord> fruitfulList 
                    = GetFruitfulVariants(testStr, conn);
                System.out.println("----- Total Fruitful Variants found: " +
                        fruitfulList.size());
                for(int j = 0; j < fruitfulList.size(); j++)
                {
                    FruitfulRecord rec = fruitfulList.elementAt(j);
                    System.out.println("=== Found Fruitfuls ===");
                    System.out.println(rec.GetVariantTerm() + "|"
                        + rec.GetCategory() + "|" + rec.GetInflection() + "|" 
                        + rec.GetOriginalCategory() + "|"
                        + rec.GetOriginalInflection() + "|"
                        + rec.GetFlowHistory() + "|" + rec.GetDistance() + "|" 
                        + rec.GetTagInformation());
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
