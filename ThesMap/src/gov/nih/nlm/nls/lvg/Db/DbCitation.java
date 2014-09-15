package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
/*****************************************************************************
* This class provides high level interfaces to find uninflected term from
* Inflection and CitationLowerCase tables in LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        InflectionRecord
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class DbCitation 
{
    /**
    * Get all citation terms from inflection records for a specified term
    * from LVG database.
    *
    * @param  inStr  term for finding citation term.
    * @param  conn  database connection
    *
    * @return  all citation terms from inflection records for a specified
    * term
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetCitations(String inStr, 
        Connection conn) throws SQLException
    {
        // retireve all record by inflected terms
        Vector<InflectionRecord> out 
            = DbInflectionUtil.GetRecordsByIfTerm(inStr, conn, false);
        CitationComparator<InflectionRecord> cc 
            = new CitationComparator<InflectionRecord>();
        Collections.sort(out, cc);
        return out;
    }
    /**
    * Get the citation terms from inflection records for a base form
    * from LVG database.
    *
    * @param  inStr  base form for finding citation term.
    * @param  conn  database connection
    *
    * @return  the citation term from inflection records for a base form
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetCitationsFromBase(String inStr, 
        Connection conn) throws SQLException
    {
        // retireve all record by inflected terms
        Vector<InflectionRecord> out = 
            DbInflectionUtil.GetRecordsByIfTermInfl(inStr, 1, conn);
        CitationComparator<InflectionRecord> cc 
            = new CitationComparator<InflectionRecord>();
        Collections.sort(out, cc);
        return out;
    }
}
