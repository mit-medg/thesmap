package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides high level interfaces to check spelling variants from
* Inflection table in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">Desgin Document</a>
*
* @version    V-2013
****************************************************************************/
public class DbSpellingVariants 
{
    /**
    * Get all spelling variant from inflection records for a specified term
    * from LVG database.
    *
    * @param  inStr  term for finding spellng variants
    * @param  conn  database connection
    *
    * @return  all acronym records with key form is same as inStr
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetSpellingVariants(String inStr, 
        Connection conn) throws SQLException
    {
        Vector<InflectionRecord> records = 
            DbInflectionUtil.GetRecordsByIfTerm(inStr, conn, false);
        // eliminate duplicate recrods with same EUI, Cat, Infl
        Vector<InflectionRecord> pureRecords 
            = EliminateDuplicateRecords(records);
        Vector<InflectionRecord> svRecords 
            = GetInflectionsByEuiCatInf(pureRecords, conn);
        // Sort
        SpellingVarComparator<InflectionRecord> sc 
            = new SpellingVarComparator<InflectionRecord>();
        Collections.sort(svRecords, sc);
        return svRecords;
    }
    // private methods
    // Eliminate records with same EUI, Category, and Inflection
    private static Vector<InflectionRecord> EliminateDuplicateRecords(
        Vector<InflectionRecord> ins)
    {
        Vector<InflectionRecord> outs = new Vector<InflectionRecord>();
        for(int i = 0; i < ins.size(); i++)
        {
            InflectionRecord record = ins.elementAt(i);
            if(IsContainRecord(outs, record) == false)
            {
                outs.addElement(record);
            }
        }
        return outs;
    }
    // check if it contains an element
    private static boolean IsContainRecord(Vector<InflectionRecord> records, 
        InflectionRecord record)
    {
        // check if the records is empty
        if(record == null)
        {
            return false;
        }
        for(int i = 0; i < records.size(); i++)
        {
            InflectionRecord cur = records.elementAt(i);
            if((record.GetEui().equals(cur.GetEui()))
            && (record.GetCategory() == cur.GetCategory())
            && (record.GetInflection() == cur.GetInflection()))
            {
                return true;
            }
        }
        return false;
    }
    // multiple input inflection records
    private static Vector<InflectionRecord> GetInflectionsByEuiCatInf(
        Vector<InflectionRecord> ins, Connection conn) throws SQLException
    {
        Vector<InflectionRecord> out = new Vector<InflectionRecord>();
        for(int i = 0; i < ins.size(); i++)
        {
            InflectionRecord record = ins.elementAt(i);
            Vector<InflectionRecord> temp 
                = GetInflectionsByEuiCatInf(record, conn);
            out.addAll(temp);
        }
        return out;
    }
    // one input inflection record
    private static Vector<InflectionRecord> GetInflectionsByEuiCatInf(
        InflectionRecord in, Connection conn) throws SQLException
    {
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE eui = ? AND termCat = ? AND termInfl = ?"; 
    
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, in.GetEui());
        ps.setInt(2, in.GetCategory());
        ps.setLong(3, in.GetInflection());
        return DbInflectionUtil.GetRecordsByPreparedStatement(ps, false);
    }
    /**
    * Test driver for this class
    */
    public static void main (String[] args)
    {
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = "color";
        if(args.length == 1)
        {
            testStr = args[0];
        }
        // obtain a connection
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                // test for inflection terms
                Vector<InflectionRecord> records = 
                    GetSpellingVariants(testStr, conn);
                
                System.out.println("----- Total records found: " +
                    records.size());
                for(int i = 0; i < records.size(); i++)
                {
                    InflectionRecord record = records.elementAt(i);
                    System.out.println(record.GetInflectedTerm() + "|"
                        + record.GetUninflectedTerm() + "|"
                        + record.GetCitationTerm() + "|"
                        + record.GetEui() + "|"
                        + record.GetCategory() + "|"
                        + record.GetInflection());
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
