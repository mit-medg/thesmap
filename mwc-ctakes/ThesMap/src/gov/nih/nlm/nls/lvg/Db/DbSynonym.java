package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to Synonym table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        SynonymRecord
* @see <a href="../../../../../../../designDoc/UDF/database/synonymTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbSynonym 
{
    /**
    * Get all synonym records for a specified term from LVG database.
    *
    * @param  inStr  key form (no punctuation lower case)
    * @param  conn  database connection
    *
    * @return  all synonym records for a speficied term, inStr
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<SynonymRecord> GetSynonyms(String inStr, 
        Connection conn) throws SQLException
    {
        String query = "SELECT keyFormNpLc, keyForm, aSynonym, cat1, cat2 "
            + "FROM LexSynonym WHERE keyFormNpLc = ?";
            
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        Vector<SynonymRecord> synonyms = new Vector<SynonymRecord>();
        // get data from itable inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            SynonymRecord synonymRecord = new SynonymRecord();
            synonymRecord.SetKeyFormNpLc(rs.getString(1));  // keyFormNpLc
            synonymRecord.SetKeyForm(rs.getString(2));      // keyForm
            synonymRecord.SetSynonym(rs.getString(3));      // aSynonym
            synonymRecord.SetCat1(rs.getInt(4));            // cat1
            synonymRecord.SetCat2(rs.getInt(5));            // cat2
            synonyms.addElement(synonymRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        SynonymComparator<SynonymRecord> sc 
            = new SynonymComparator<SynonymRecord>();
        Collections.sort(synonyms, sc);
        return (synonyms);
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "aminophylline";
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
                // Get Synonyms
                Vector<SynonymRecord> synonymList = GetSynonyms(testStr, conn);
                System.out.println("----- Total Synonyms found: " +
                        synonymList.size());
                for(int j = 0; j < synonymList.size(); j++)
                {
                    SynonymRecord rec = synonymList.elementAt(j);
                    System.out.println("=== Found Synonyms ===");
                    System.out.println(rec.GetKeyFormNpLc() + "|"
                        + rec.GetKeyForm() + "|" + rec.GetCat1() 
                        + "|" + rec.GetSynonym() + "|" + rec.GetCat2());
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
