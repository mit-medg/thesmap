package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides features of generating inflectional variants.  This
* class handles cases in the following two ways:
* <ul>
* <li> case insensitive search while making the SQL query
* <li> the results preserve original case
* </ul>
* This feature is handled by adding another columns (lowercased) for inflection
* terms in the Inflection table of Lvg DB.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/inflection.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToInflection extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   restrictFlag   a numerical flag to filter the output.  It's 
    * values are defined in OutputFilter as:
    * <br>LVG_ONLY, LVG_OR_ALL, ALL. 
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @see DbBase
    * @see OutputFilter
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, int restrictFlag, boolean detailsFlag, boolean mutateFlag) 
    {
        // Mutate: retrieve EUI & uninflected term from Inflected term
        Vector<LexItem> out = InflectWords(in, conn, trie, restrictFlag, INFO,
            detailsFlag, mutateFlag, Flow.INFLECTION);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "sleep");
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
            if(conn == null)
            {
                System.err.println("** Error: Db connection problem!");
            }
            boolean isInflection = true;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
            if(conn != null)
            {
                outs = ToInflection.Mutate(in, conn, trie, 
                    OutputFilter.LVG_OR_ALL, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        // print out results
        PrintResults(in, outs);
    }
    // package methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   restrictFlag   a numerical flag to filter the output.  It's 
    * values are defined in OutputFilter as:
    * <br>LVG_ONLY, LVG_OR_ALL, ALL. 
    * @param   infoStr   the header of detail information, usually is the
    * full name of the current flow
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @see DbBase
    */
    static Vector<LexItem> InflectWords(LexItem in, Connection conn,
        RamTrie trie, int restrictFlag, String infoStr,
        boolean detailsFlag, boolean mutateFlag, int flowName)
    {
        String inStr = in.GetSourceTerm();
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            // Fact: Get unflections from database
            Vector<InflectionRecord> factList = 
                DbInflection.GetInflections(inStr, inCat, inInfl, conn);
            Vector<LexItem> facts = new Vector<LexItem>();
            // update LexItems
            for(int i = 0; i < factList.size(); i++)
            {
                InflectionRecord record = factList.elementAt(i);
                String term = record.GetInflectedTerm();
                // details & mutate
                String details = null;
                String mutate = null;
                if(detailsFlag == true)
                {
                    details = infoStr + " (FACT|" + record.GetUninflectedTerm()
                        + "|" + Category.ToName(record.GetCategory()) 
                        + "|base)";
                }
                if(mutateFlag == true)
                {
                    String fs = GlobalBehavior.GetFieldSeparator();
                    mutate = "FACT" + fs
                        + record.GetUninflectedTerm() + fs
                        + Category.ToName(record.GetCategory()) + fs 
                        + "base" + fs
                        + term + fs
                        + Category.ToName(record.GetCategory()) + fs
                        + Inflection.ToName(record.GetInflection()) + fs
                        + record.GetEui() + fs;
                }
                LexItem temp = UpdateLexItem(in, term, flowName,
                    record.GetCategory(), record.GetInflection(), details, 
                    mutate);
                facts.addElement(temp);
            }
            // Rule generated inflections
            Vector<LexItem> rules = new Vector<LexItem>();
            if((restrictFlag == OutputFilter.ALL)
            || ((restrictFlag == OutputFilter.LVG_OR_ALL) 
             && (facts.size() == 0)))
            {
                // Rule: Use trie to get the result from rule 
                Vector<RuleResult> ruleList = 
                    trie.GetInflectedTermsByRules(inStr, inCat, inInfl, true);
                // update LexItems
                for(int i = 0; i < ruleList.size(); i++)
                {
                    RuleResult record = ruleList.elementAt(i);
                    String term = record.GetOutTerm();
                    // heuristic rules: check if inflection in Lexicon
                    if(DbInflection.IsExistInflectedTerm(term, conn) == false)
                    {
                        // details & mutate
                        String details = null;
                        String mutate = null;
                        if(detailsFlag == true)
                        {
                            details = infoStr + " (RULE|" + record.GetInTerm()
                                + "|" + record.GetRuleString() + ")";
                        }
                        if(mutateFlag == true)
                        {
                            String fs = GlobalBehavior.GetFieldSeparator();
                            mutate = "RULE" + fs 
                                + record.GetInTerm() + fs
                                + record.GetRuleString() + fs;
                        }
                        LexItem temp = UpdateLexItem(in, term, flowName,
                            Category.ToValue(record.GetOutCategory()), 
                            Inflection.ToValue(record.GetOutInflection()), 
                            details, mutate);
                        rules.addElement(temp);
                    }
                }
            }
            // form output by the restrict flag
            outs = OutputFilter.RestrictOption(facts, rules, restrictFlag);
        }
        catch (Exception e) 
        { 
            System.err.println("** Error: Sql Exception in ToInflection Flow.");
        }
        // Sort: category, length, case incentive sort
        LexItemComparator<LexItem> lc = new LexItemComparator<LexItem>();
        lc.SetRule(LexItemComparator.LVG_RULE);
        Collections.sort(outs, lc);
        return outs;
    }
    // private methods
    // data members
    private static final String INFO = "Inflection";
}
