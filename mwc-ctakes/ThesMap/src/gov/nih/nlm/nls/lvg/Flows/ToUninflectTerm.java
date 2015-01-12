package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class gets the uninflected variants for a specified term.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/uninflectTerm.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToUninflectTerm extends Transformation
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ramtrie
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @exception SQLException if errors occurr while connect to LVG database.
    *
    * @see DbBase
    * @see PersistentTrie
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        // mutate the term
        Vector<LexItem> outs1 = UninflectTerm(in, conn, trie, INFO, 
            detailsFlag, mutateFlag);
        // sort: use -CR:oc as the default output for this flow component
        Vector<LexItem> outs = CombineRecords.Combine(outs1, 
            CombineRecords.BY_CATEGORY);
        return outs;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "Left Data");
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        // Mutate connect to DB
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
                outs = ToUninflectTerm.Mutate(in, conn, trie, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // protected method
    /**
    * Get uninflected variants from a specified term from Lvg facts and rules.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   infoStr   the header of detail information, usually is the
    * full name of the current flow
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @see DbBase
    */
    protected static Vector<LexItem> UninflectTerm(LexItem in, Connection conn,
        RamTrie trie, String infoStr, boolean detailsFlag, boolean mutateFlag)
    {
        String inStr = in.GetSourceTerm();
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        Vector<LexItem> out = new Vector<LexItem>();
        try
        {
            // fact get uninflections from database
            Vector<InflectionRecord> factList = 
                DbUninflection.GetUninflections(inStr, conn); 
            // update LexItems
            // go through all records from the results of fact DB
            for(int i = 0; i < factList.size(); i++)
            {
                InflectionRecord record = factList.elementAt(i);
                String uninflectedTerm = record.GetUninflectedTerm();
                long curCat = record.GetCategory();
                long curInfl = record.GetInflection();
                // input filter for category and inflection
                if(InputFilter.IsLegal(inCat, inInfl, curCat, curInfl) == false)
                {
                    continue;
                }
                // details & mutate
                String details = null;
                String mutate = null;
                if(detailsFlag == true)
                {
                    details = infoStr + " (FACT)";
                }
                if(mutateFlag == true)
                {
                    String fs = GlobalBehavior.GetFieldSeparator();
                    mutate = "FACT" + fs
                        + record.GetInflectedTerm() + fs 
                        + Category.ToName(record.GetCategory()) + fs
                        + Inflection.ToName(record.GetInflection()) + fs
                        + uninflectedTerm + fs
                        + Category.ToName(record.GetCategory()) + fs
                        + "base" + fs
                        + record.GetEui() + fs;
                }
                // update LexItem's info
                LexItem temp = UpdateLexItem(in, uninflectedTerm, 
                    Flow.UNINFLECT_TERM, curCat, 
                    Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate);
                
                out.addElement(temp);
            }
            // Rule: apply Trie rules if no results from fact
            if(out.size() == 0)
            {
                Vector<RuleResult> ruleList = 
                    trie.GetUninflectedTermsByRules(inStr, inCat, inInfl, true);
                // update LexItems
                for(int i = 0; i < ruleList.size(); i++)
                {
                    RuleResult result = ruleList.elementAt(i);
                    String uninflectedTerm = result.GetOutTerm();
                    long outCat = Category.ToValue(result.GetOutCategory());
                    long outInfl = 
                        Inflection.ToValue(result.GetOutInflection());
                    // details & mutate
                    String details = null;
                    String mutate = null;
                    if(detailsFlag == true)
                    {
                        details = infoStr + " (RULE)";
                    }
                    if(mutateFlag == true)
                    {
                        mutate = "RULE" + GlobalBehavior.GetFieldSeparator()
                            + result.GetRuleString();
                    }
                    // update LexItem's Info
                    LexItem temp = UpdateLexItem(in, uninflectedTerm, 
                        Flow.UNINFLECT_TERM, outCat, outInfl, details, mutate);
                    // Heuristic: check if uninflected term is in Lexicon
                    if((out.contains(temp) == false)
                    && (DbInflection.IsExistInflectedTerm(
                        uninflectedTerm, conn) == false))
                    {
                        out.addElement(temp);
                    }
                }
            }
        }
        catch (Exception e) { }
        return out;
    }
    // data members
    private static final String INFO = "Uninflect Term";
}
