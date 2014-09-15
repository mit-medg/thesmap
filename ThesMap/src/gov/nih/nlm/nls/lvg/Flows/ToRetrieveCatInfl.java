package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class retrieves categories and inflections for a specified term.  This
* class gets categories and inflections from Lvg DB and concatenates values
* of them to the output.  If the term is not in Lexicon, return all possible
* values (from trie).  The possible values from trie is the difference from 
* flow component, filter ouput to conatin only forms from the lexicon.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/retrieveCatInfl.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToRetrieveCatInfl extends Transformation implements Cloneable
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
        RamTrie trie, boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        // mutate the term: Get Cat & Inflection from DB
        Vector<InflectionRecord> catInflRec = GetCatInfl(in.GetSourceTerm(), conn);
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        // update target LexItem
        Vector<InflectionRecord> combined = new Vector<InflectionRecord>();
        for(int i = 0; i < catInflRec.size(); i++)
        {
            InflectionRecord record = catInflRec.elementAt(i);
            AddRecordToCombined(record, combined, inCat, inInfl);
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
                String fs = GlobalBehavior.GetFieldSeparator();
                mutate = "FACT" + fs 
                    + Category.ToName(record.GetCategory()) + fs
                    + Inflection.ToName(record.GetInflection()) + fs;
            }
            LexItem temp = UpdateLexItem(in, term, Flow.RETRIEVE_CAT_INFL, 
                record.GetCategory(), record.GetInflection(), details, mutate);
            out.addElement(temp);
        }
        // Rules: get all rule based categories & inflections if not in Lexicon
        if(out.size() == 0)
        {
            CatInfl catInfl = trie.GetCatInflByRules(in.GetSourceTerm(),
                inCat, inInfl);
            String fs = GlobalBehavior.GetFieldSeparator();
            String mutate = "RULE" + fs 
                + catInfl.GetCategoryStr() + fs
                + catInfl.GetInflectionStr() + fs;
            LexItem temp = UpdateLexItem(in, in.GetSourceTerm(),
                Flow.RETRIEVE_CAT_INFL, catInfl.GetCategory(),
                catInfl.GetInflection(), INFO, mutate);
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
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            boolean isInflection = true;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
            if(conn != null)
            {
                outs = ToRetrieveCatInfl.Mutate(in, conn, trie, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static void AddRecordToCombined(InflectionRecord record,
        Vector<InflectionRecord> combined, long inCat, long inInfl)
    {
        // check if pass the input filter category and inflection
        long curCat = record.GetCategory();
        long curInfl = record.GetInflection();
        if(InputFilter.IsLegal(inCat, inInfl, curCat, curInfl) == false)
        {
            return;
        }
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
                    cur.SetCategory(Bit.Add(cur.GetCategory(), (int)(curCat)));
                    cur.SetInflection(Bit.Add(cur.GetInflection(), curInfl));
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
    private static Vector<InflectionRecord> GetCatInfl(String inStr, 
        Connection conn) throws SQLException
    {
        Vector<InflectionRecord> out = DbInflection.GetCatInfl(inStr, conn);
        return out;
    }
    // data members
    private static final String INFO = "Retrieve Cat Infl";
}
