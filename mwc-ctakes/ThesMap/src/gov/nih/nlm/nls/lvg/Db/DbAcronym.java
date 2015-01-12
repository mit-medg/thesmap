package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to Acronym table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        AcronymRecord
* @see <a href="../../../../../../../designDoc/UDF/database/acronymTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbAcronym 
{
    /**
    * Check if the input string is an acronym in LVG database
    *
    * @param  inStr  string to be checked (no punctuation lower case)
    * @param  conn  database connection
    *
    * @return  true or false to represent the inStr is or is not an acronym 
    *          in LVG database
    *
    * @exception  SQLException if there is a database error happens
    */
    public static boolean IsAcronym(String inStr, Connection conn)
        throws SQLException
    {
        boolean isAcronym = false;
        String query = "SELECT acr FROM Acronym WHERE acrNpLc= ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        // get the boolean value of execute in table Acronym
        ResultSet rs = ps.executeQuery();
        if(rs.next() == true)
        {
            isAcronym = true;
        }
        // Clean up
        rs.close();
        ps.close();
        return isAcronym;
    }
    /**
    * Get all acronym records for an expansion (no punctuation lower case)
    * from LVG database.  The result is sorted by ignoring cases and using 
    * alphabetic order.
    *
    * @param  inStr  expansion of an acronym (no punctuation lower case)
    * @param  conn  database connection
    *
    * @return  Vector<AcronymRecord> acronym records with expansion
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<AcronymRecord> GetAcronyms(String inStr, 
        Connection conn) throws SQLException
    {
        String query = "SELECT exp, aType, acr FROM Acronym WHERE expNpLc= ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        Vector<AcronymRecord> acronyms = new Vector<AcronymRecord>();
        // get data from table Acronym
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            AcronymRecord acronymRecord = new AcronymRecord();
            acronymRecord.SetExpansion(rs.getString(1));    // exp
            acronymRecord.SetType(rs.getString(2));         // aType
            acronymRecord.SetAcronym(rs.getString(3));      // acr
            acronyms.addElement(acronymRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        AcronymComparator<AcronymRecord> ac 
            = new AcronymComparator<AcronymRecord>();
        Collections.sort(acronyms, ac);
        return acronyms;
    }
    /**
    * Get all acronym records for an acronym (no punctuation lower case)
    * from LVG database.  The result is sorted by ignoring cases and using 
    * alphabetic order.
    *
    * @param  inStr  acronym of an expension (no punctuation lower case)
    * @param  conn  database connection
    *
    * @return  Vector<AcronymRecord> acronym records with expansion
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<AcronymRecord> GetExpansions(String inStr, 
        Connection conn) 
        throws SQLException
    {
        String query = "SELECT exp, aType, acr FROM Acronym WHERE acrNpLc= ?";
        
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, inStr);
        Vector<AcronymRecord> expansions = new Vector<AcronymRecord>();
        // get data from itable inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            AcronymRecord acronymRecord = new AcronymRecord();
            acronymRecord.SetExpansion(rs.getString(1));    // exp
            acronymRecord.SetType(rs.getString(2));         // aType
            acronymRecord.SetAcronym(rs.getString(3));      // acr
            expansions.addElement(acronymRecord);
        }
        // Clean up
        rs.close();
        ps.close();
        // sort
        ExpansionComparator<AcronymRecord> ec 
            = new ExpansionComparator<AcronymRecord>();
        Collections.sort(expansions, ec);
        return expansions;
    }
    /**
    * Test driver for this class.
    */
    public static void main (String[] args)
    {
        String testStr = "air conditioning";
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
                // Get Expansions
                Vector<AcronymRecord> expansionList = 
                    GetExpansions(testStr.toLowerCase(), conn);
                
                System.out.println("----- IsAcronym (" + testStr.toLowerCase()
                    + "): " + 
                    IsAcronym(testStr.toLowerCase(), conn));
                System.out.println("----- IsAcronym (AC): " + 
                    IsAcronym("ac", conn));
                System.out.println("----- IsAcronym (A.C.): " + 
                    IsAcronym("a.c.", conn));
                System.out.println("----- Total Expansions found: " +
                    expansionList.size());
                for(int i = 0; i < expansionList.size(); i++)
                {
                    AcronymRecord record = expansionList.elementAt(i);
                    System.out.println("=== Found Expansions ===");
                    System.out.println(record.GetAcronym() + "|" +
                        record.GetType() + "|" + record.GetExpansion());
                }
                // Get Acronyms
                Vector<AcronymRecord> acronymList = GetAcronyms(testStr, conn);
                System.out.println("----- Total Acronyms found: " +
                        acronymList.size());
                for(int j = 0; j < acronymList.size(); j++)
                {
                    AcronymRecord rec = acronymList.elementAt(j);
                    System.out.println("=== Found Acronyms ===");
                    System.out.println(rec.GetAcronym() + "|" +
                        rec.GetType() + "|" + rec.GetExpansion());
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
