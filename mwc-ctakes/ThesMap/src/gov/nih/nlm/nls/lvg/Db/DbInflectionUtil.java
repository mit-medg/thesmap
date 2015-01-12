package gov.nih.nlm.nls.lvg.Db;
import java.sql.*;
import java.util.*;
/*****************************************************************************
* This class provides lower level interfaces by generating SQL query to 
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
public class DbInflectionUtil 
{
    // public methods
    /**
    * Get all inflection records for an inflected term.
    *
    * @param  ifTerm  inflected term
    * @param  cat  specified categories for output
    * @param  infl  specified inflections for output
    * @param  conn  database connection
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for a specified inflected term, 
    * categories, and inflections.
    *
    * @exception  SQLException if there is a database error happens
    */
    public static Vector<InflectionRecord> GetRecordsByIfTermCatInfl(
        String ifTerm, int cat, long infl, Connection conn, 
        boolean isUniqueEui) throws SQLException
    {
        PreparedStatement ps = null;
        // join table query
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE ifTermLC = ? "; 
        if((cat >= 0) && (infl >= 0))
        {
            query += " AND termCat = ? AND termInfl = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
            ps.setInt(2, cat);
            ps.setLong(3, infl);
        }
        else if(cat >= 0)
        {
            query += " AND termCat = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
            ps.setInt(2, cat);
        }
        else if(infl >= 0)
        {
            query += " AND termInfl = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
            ps.setLong(2, infl);
        }
        else
        {
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
        }
        
        return GetRecordsByPreparedStatement(ps, isUniqueEui); 
    }
    // package methods
    /**
    * Get all inflection records for an inflected term and category.
    *
    * @param  ifTerm  inflected term
    * @param  cat  specified categories for output
    * @param  conn  database connection
    *
    * @return  all inflection records for a specified inflected term and
    * categories.
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsByIfTermCat(String ifTerm, 
        int cat, Connection conn) throws SQLException
    {
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm " 
            + "FROM Inflection WHERE ifTermLC=? AND termCat=?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, ifTerm.toLowerCase());
        ps.setInt(2, cat);
        return GetRecordsByPreparedStatement(ps, false);
    }
    /**
    * Get all inflection records for an inflected term and inflection.
    *
    * @param  ifTerm  inflected term
    * @param  infl  specified inflections for output
    * @param  conn  database connection
    *
    * @return  all inflection records for a specified inflected term and
    * inflections.
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsByIfTermInfl(String ifTerm, 
        long infl, Connection conn) throws SQLException
    {
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm " 
            + "FROM Inflection WHERE ifTermLC = ? AND termInfl = ?"; 
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, ifTerm.toLowerCase());
        ps.setLong(2, infl);
        return GetRecordsByPreparedStatement(ps, false);
    }
    /**
    * Get all inflection records for an inflected term and inflection.
    *
    * @param  ifTerm  inflected term
    * @param  infl  specified inflections for output
    * @param  conn  database connection
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for a specified inflected term
    * and inflections.
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsByIfTermInfl(String ifTerm, 
        long infl, Connection conn, boolean isUniqueEui) throws SQLException
    {
        PreparedStatement ps = null;
        // join table query
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE ifTermLC = ?"; 
        if(infl >= 0)
        {
            query += " AND termInfl = ?";
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
            ps.setLong(2, infl);
        }
        else
        {
            ps = conn.prepareStatement(query);
            ps.setString(1, ifTerm.toLowerCase());
        }
        
        return GetRecordsByPreparedStatement(ps, isUniqueEui);
    }
    /**
    * Get all inflection records for an inflected term.
    *
    * @param  ifTerm  inflected term
    * @param  conn  database connection
    *
    * @return  all inflection records for a specified inflected term
    *
    * @exception  SQLException if there is a database error happens
    */
    static InflectionRecord GetRecordByIfTerm(String ifTerm, 
        Connection conn) throws SQLException
    {
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE ifTermLC = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, ifTerm.toLowerCase());
        return GetRecordByPreparedStatement(ps);
    }
    /**
    * Get all inflection records for an inflected term.
    *
    * @param  ifTerm  inflected term
    * @param  conn  database connection
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for a specified inflected term
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsByIfTerm(String ifTerm, 
        Connection conn, boolean isUniqueEui) throws SQLException
    {
        // join table query
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE ifTermLC = '" 
            + DbBase.FormatSqlStr(ifTerm.toLowerCase()) + "'";
        return GetRecords(query, conn, isUniqueEui);
    }
    /**
    * Get all inflection records for an inflected term begin with a specified
    * string pattern.
    *
    * @param  ifTermBegin  beginning string pattern of the inflected term
    * @param  conn  database connection
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for an inflected term begins with a
    * specified string pattern
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsBeginWithIfTerm(
        String ifTermBegin, Connection conn, boolean isUniqueEui) 
        throws SQLException
    {
        // join table query
        String query = "SELECT ifTerm, termCat, termInfl, eui, unTerm, ctTerm "
            + "FROM Inflection WHERE ifTermLC LIKE ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, (ifTermBegin.toLowerCase()+"%"));
        return GetRecordsByPreparedStatement(ps, isUniqueEui);
    }
    /**
    * Get the last inflection records from Inflection 
    * tables from LVG database for an specific SQL query.
    *
    * @param  query  SQL query
    * @param  conn  database connection
    *
    * @return  the last inflection records for a specified SQL query 
    *
    * @exception  SQLException if there is a database error happens
    static InflectionRecord GetRecord(String query, Connection conn) 
        throws SQLException
    {
        InflectionRecord inflection = new InflectionRecord();
        // get data from table inflection
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while(rs.next())
        {
            inflection.SetInflectedTerm(rs.getString("ifTerm"));
            inflection.SetUnInflectedTerm(rs.getString("unTerm"));
            inflection.SetEui(rs.getString("eui"));
            inflection.SetCategory(rs.getInt("termCat"));
            inflection.SetInflection(rs.getLong("termInfl"));
            inflection.SetCitationTerm(rs.getString("ctTerm"));
        }
        // Clean up
        rs.close();
        statement.close();
        return inflection;
    }
    */
    /**
    * Get the last inflection records from Inflection 
    * tables from LVG database for an specific SQL query.
    *
    * @param  ps  prepared statement of SQL query
    *
    * @return  the last inflection records for a specified SQL query 
    *
    * @exception  SQLException if there is a database error happens
    */
    static InflectionRecord GetRecordByPreparedStatement(PreparedStatement ps) 
        throws SQLException
    {
        InflectionRecord inflection = null;
        // get data from table inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            inflection = GetOneInflectionRecord(rs);
        }
        // Clean up
        rs.close();
        ps.close();
        return inflection;
    }
    /**
    * Get all inflection records from Inflection table
    * from LVG database for an specific SQL query.
    *
    * @param  query  SQL query
    * @param  conn  database connection
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for a specified SQL query 
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecords(String query, Connection conn, 
        boolean isUniqueEui) throws SQLException
    {
        InflectionVector<InflectionRecord> inflections 
            = new InflectionVector<InflectionRecord>();
        // get data from table inflection
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while(rs.next())
        {
            InflectionRecord inflection = GetOneInflectionRecord(rs);
            if((isUniqueEui == false)
            || (inflections.ContainEui(inflection.GetEui()) == false))
            {
                inflections.addElement(inflection);
            }
        }
        // Clean up
        rs.close();
        statement.close();
        return ((Vector<InflectionRecord>) inflections);
    }
    /**
    * Get all inflection records from Inflection table
    * from LVG database for an specific SQL query, using prepare statement.
    *
    * @param  ps  prepared statement of SQL query
    * @param  isUniqueEui  a boolean flag to specified the output to an unique
    * EUI.  If true, only the first record of records with same EUI is added 
    * to output. 
    *
    * @return  all inflection records for a specified SQL query 
    *
    * @exception  SQLException if there is a database error happens
    */
    static Vector<InflectionRecord> GetRecordsByPreparedStatement(
        PreparedStatement ps, boolean isUniqueEui) throws SQLException
    {
        InflectionVector<InflectionRecord> inflections 
            = new InflectionVector<InflectionRecord>();
        // get data from table inflection
        ResultSet rs = ps.executeQuery();
        while(rs.next())
        {
            InflectionRecord inflection = GetOneInflectionRecord(rs);
            if((isUniqueEui == false)
            || (inflections.ContainEui(inflection.GetEui()) == false))
            {
                inflections.addElement(inflection);
            }
        }
        // Clean up
        rs.close();
        ps.close();
        return ((Vector<InflectionRecord>) inflections);
    }
    /**
    * Get one inflectional record from Lexicon inflectional table
    *
    * @param rs ResultSet
    *
    * @return InflectionRecord inflection record
    *
    * @exception  SQLException if there is a database error happens
    */
    static InflectionRecord GetOneInflectionRecord(ResultSet rs)
        throws SQLException
    {
        InflectionRecord inflection = new InflectionRecord();
        // much faster operation, make sure the order of query is right
        inflection.SetInflectedTerm(rs.getString(1));    // ifTerm
        inflection.SetCategory(rs.getInt(2));            // termCat
        inflection.SetInflection(rs.getLong(3));         // termInfl
        inflection.SetEui(rs.getString(4));              // eui
        inflection.SetUnInflectedTerm(rs.getString(5));  // unTerm
        inflection.SetCitationTerm(rs.getString(6));     // ctTerm
        return inflection;
    }
}
