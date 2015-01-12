package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides high level interfaces to Inflection and CitationLowerCase
* tables in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see       InflectionRecord
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbInflection 
{
    /**
    * Get inflections for a specific inflected term and category
    * from LVG database.
    *
    * @param  inStr  inflected term
    * @param  cat  category for the term
    * @param  conn  database connection
    *
    * @return  an inflection in a long integer format
    *
    * @exception  SQLException if there is a database error happens
    */
    public static long GetInflByCat(String inStr, int cat, Connection conn)
        throws SQLException
    {
        // retrieve record from Inflected terms
        Vector<InflectionRecord> out 
            = DbInflectionUtil.GetRecordsByIfTermCat(inStr, cat, conn);
        long infl = 0;
        for(int i = 0; i < out.size(); i++)
        {
            InflectionRecord record = out.elementAt(i);
            infl = Bit.Add(infl, record.GetInflection());
        }
        return infl;
    }
    /**
    * Get all inflection records for an inflected term begins with a specific
    * string with specified category and inflection from LVG database.
    *
    * @param  inStr  the beginning string pattern for an inflected term
    * @param  conn  database connection
    *
    * @return  all inflection records for an inflected term begins with a 
    * specific string with specified category and inflection
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetCatInflBegin(String inStr, 
        Connection conn) throws SQLException
    {
        // retrieve record from Inflected terms
        Vector<InflectionRecord> out = 
            DbInflectionUtil.GetRecordsBeginWithIfTerm(inStr, conn, false);
        // sort
        CatInflBeginComparator<InflectionRecord> cibc 
            = new CatInflBeginComparator<InflectionRecord>();
        Collections.sort(out, cibc);
        return out;
    }
    /**
    * Get categories and inflections from all inflection records for an 
    * inflected term from LVG database.
    *
    * @param  inStr  inflected term
    * @param  conn  database connection
    *
    * @return  all inflection records for an inflected term
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetCatInfl(String inStr, 
        Connection conn) throws SQLException
    {
        // retrieve record from Inflected terms
        Vector<InflectionRecord> out 
            = DbInflectionUtil.GetRecordsByIfTerm(inStr, conn, false);
        // sort 
        CatInflComparator<InflectionRecord> cic 
            = new CatInflComparator<InflectionRecord>();
        Collections.sort(out, cic);
        return out;
    }
    /**
    * Get inflection from all inflection records for an inflected term from 
    * LVG database.
    *
    * @param  inStr  input inflected term
    * @param  inCat  input category
    * @param  inInfl input inflection
    * @param  conn  database connection
    *
    * @return  all inflection records for an inflected term
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetInflections(String inStr, 
        long inCat, long inInfl, Connection conn) throws SQLException
    {
        Vector<InflectionRecord> out = new Vector<InflectionRecord>();
        // retrieve record (EUI & uninflected) term from Inflected terms
        Vector<InflectionRecord> list 
            = DbInflectionUtil.GetRecordsByIfTerm(inStr, conn, false);
        // get inflections for each record (eui)
        Vector<String> euiList = new Vector<String>();
        for(int i = 0; i < list.size(); i++)
        {
            InflectionRecord record = list.elementAt(i);
            long curCat = record.GetCategory();
            long curInfl = record.GetInflection();
            // input filter for category and inflection
            if((Bit.Contain(inCat, curCat) == false)
            || (Bit.Contain(inInfl, curInfl) == false))
            {
                continue;
            }
            // retrieve all results if EUI (record) has not retrieve inflection
            if(euiList.contains(record.GetEui()) == false)
            {
                Vector<InflectionRecord> records 
                    = GetInflections(record.GetEui(), 
                    record.GetUninflectedTerm(), -1, -1, conn, false);
                out.addAll(records);
                euiList.add(record.GetEui());
            }
        }
        
        // sort
        InflectionComparator<InflectionRecord> ic 
            = new InflectionComparator<InflectionRecord>();
        Collections.sort(out, ic);
        return out;
    }
    /**
    * Check if a specified term is an inflected term in LVG database
    *
    * @param  ifTerm  an inflectional term to be checked
    * @param  conn  database connection
    *
    * @return  true or false if the specified inflectional term does or does 
    * not exist in LVG database, Inflection table
    *
    * @exception  SQLException if there is a database error happens
    */
    public static boolean IsExistInflectedTerm(String ifTerm,
        Connection conn) throws SQLException
    {
        if(ifTerm == null)
        {
            return false;
        }
        boolean existInTable = false;
        String query = "SELECT ifTermLC FROM Inflection WHERE ifTermLC = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, ifTerm.toLowerCase());
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
    // private methods
    private static Vector<InflectionRecord> GetInflections(String eui, 
        String unTerm, int cat, long infl, Connection conn, 
        boolean isUniqueEui) throws SQLException
    {
        PreparedStatement ps = null;
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE eui= ? AND unTermLC = ?"; 
        if((cat >= 0) && (infl >= 0))
        {
            query += " AND termCat = ? AND termInfl = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, eui);
            ps.setString(2, unTerm.toLowerCase());
            ps.setInt(3, cat);
            ps.setLong(4, infl);
        }
        else if(cat >= 0)
        {
            query += " AND termCat = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, eui);
            ps.setString(2, unTerm.toLowerCase());
            ps.setInt(3, cat);
        }
        else if(infl >= 0)
        {
            query += " AND termInfl = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, eui);
            ps.setString(2, unTerm.toLowerCase());
            ps.setLong(3, infl);
        }
        else
        {
            ps = conn.prepareStatement(query);
            ps.setString(1, eui);
            ps.setString(2, unTerm.toLowerCase());
        }
        return DbInflectionUtil.GetRecordsByPreparedStatement(ps, isUniqueEui);
    }
}
