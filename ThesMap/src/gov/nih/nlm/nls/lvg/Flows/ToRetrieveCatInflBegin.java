package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class retrieves categories and inflections for all terms that begin
* with a specified word.  This class utilize Lvg database.  However, the
* performance depends on the ability of database handles SQL clause of "like".
* The performance for this function in Instant DB is slow and need to be 
* improved.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/flow/retrieveCatInflBegin.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToRetrieveCatInflBegin extends Transformation implements Cloneable
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
        // mutate the term
        Vector<InflectionRecord> catInflRec 
            = GetCatInfl(in.GetSourceTerm(), conn);
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < catInflRec.size(); i++)
        {
            InflectionRecord record = catInflRec.elementAt(i);
            long curCat = record.GetCategory();
            long curInfl = record.GetInflection();
            // input filter for category and inflection
            if(InputFilter.IsLegal(inCat, inInfl, curCat, curInfl) == false)
            {
                continue;
            }
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
                String fs = GlobalBehavior.GetFieldSeparator();
                mutate = record.GetEui() + fs
                    + term + fs
                    + Category.ToName(record.GetCategory()) + fs
                    + Inflection.ToName(record.GetInflection()) + fs
                    + record.GetCitationTerm() + fs
                    + record.GetUninflectedTerm() + fs;
            }
            LexItem temp = UpdateLexItem(in, term, Flow.RETRIEVE_CAT_INFL_BEGIN,
                curCat, curInfl, details, mutate);
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
        String testStr = GetTestStr(args, "nurse");    // input String
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToRetrieveCatInflBegin.Mutate(in, conn, true, true);
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
    private static Vector<InflectionRecord> GetCatInfl(String inStr, 
        Connection conn) throws SQLException
    {
        Vector<InflectionRecord> out 
            = DbInflection.GetCatInflBegin(inStr, conn);
        return out;
    }
    // data members
    private static final String INFO = "Retrieve Cat Infl, Begin";
}
