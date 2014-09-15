package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class filters out terms which are not in lexicon (Lvg DB).  It also 
* concatenates values of categories and inflections for terms in lexicon.
* It returns nothing if the term is not in the Lexicon.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/filter.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFilter extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @exception SQLException if errors occurr while connect to LVG database.
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn,
        boolean detailsFlag, boolean mutateFlag) throws SQLException
    {
        // mutate the term: Get Cat & Inflection from DB.
        // The input term will be removed if it is not in the Lvg DB.
        Vector<InflectionRecord> catInflRec 
            = GetCatInfl(in.GetSourceTerm(), conn);
        // update target LexItem
        Vector<InflectionRecord> combined = new Vector<InflectionRecord>();
        for(int i = 0; i < catInflRec.size(); i++)
        {
            InflectionRecord record = catInflRec.elementAt(i);
            AddRecordToCombined(record, combined);
        }
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < combined.size(); i++)
        {
            InflectionRecord record = combined.elementAt(i);
            // retrieve all results
            String term = record.GetInflectedTerm();
            // details & mutate
            String details = null;
            String mutate = null;
            if(detailsFlag == true)
            {
                details = INFO;
            }
            if(mutateFlag == true)
            {
                mutate = Category.ToName(record.GetCategory())
                    + GlobalBehavior.GetFieldSeparator()
                    + Inflection.ToName(record.GetInflection())
                    + GlobalBehavior.GetFieldSeparator();
            }
            LexItem temp = UpdateLexItem(in, term, Flow.FILTER, 
                record.GetCategory(), record.GetInflection(), details, mutate);
            out.addElement(temp);
        }
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file: for data base info
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "bloom");    // input String
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToFilter.Mutate(in, conn, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // private methods
    // Add an inflection recrod to a combined inflection record
    private static void AddRecordToCombined(InflectionRecord record,
        Vector<InflectionRecord> combined)
    {
        // Add the record if nothing in the combined
        if(combined.size() == 0)
        {
            combined.addElement(record);
        }
        else
        {
            boolean recordExist = false;
            for(int i = 0; i < combined.size(); i++)
            {
                InflectionRecord cur = combined.elementAt(i);
                // update category if record exists
                if(cur.GetInflectedTerm().equalsIgnoreCase(
                    record.GetInflectedTerm()))
                {
                    recordExist = true;
                    cur.SetCategory(Bit.Add(cur.GetCategory(), 
                        record.GetCategory()));
                    cur.SetInflection(Bit.Add(cur.GetInflection(), 
                        record.GetInflection()));
                    break;
                }
            }
            // Add the record if it is not exist
            if(recordExist == false)
            {
                combined.addElement(record);
            }
        }
    }
    // get categories and inflections for an inflected term
    private static Vector<InflectionRecord> GetCatInfl(String inStr, 
        Connection conn) throws SQLException
    {
        Vector<InflectionRecord> out = DbInflection.GetCatInfl(inStr, conn);
        return out;
    }
    // data members
    private static final String INFO = "Filter Output";
}
