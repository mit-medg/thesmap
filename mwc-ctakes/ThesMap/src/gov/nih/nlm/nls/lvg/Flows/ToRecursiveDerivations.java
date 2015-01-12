package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class generates derivations from the input term, recursively, until there
* are no more, or until a cycle is detected.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-15, chlu, 07-23-12, add derivation type options.
* <li>SCR-20, chlu, 07-23-12, add derivation negation options.
* <li>SCR-28, chlu, 08-01-12, add derivation negation and type options.
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/rDerivation.html">
* Design Document </a>
* @see ToDerivation
*
* @version    V-2013
****************************************************************************/
public class ToRecursiveDerivations extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   restrictFlag   a numberical flag to restrict out into LVG_ONLY
    * LVG_OR_ALL, or ALL (defined in OutputFilter).
    * @param   derivationType   a numberical flag to restrict derivation type
    * D_TYPE_ZERO, D_TYPE_PREFIX, D_TYPE_SUFFIX, D_TYPE_ZERO_PREFIX,
    * D_TYPE_ZERO_SUFFIX, D_TYPE_PREFIX_SUFFIX, D_TYPE_ALL (defined in 
    * OutputFilter);
    * @param   derivationNegation   a numberical flag to restrict derivation 
    * negation D_NEGATION_OTHERWISE, D_NEGATION_NEGATIVE, D_NEGATION_BOTH
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    * @param   detailFlowFlag   a boolean flag for showing the flow history in
    * details.  For instance use ddd instead of R.
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    * @see OutputFilter
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, int restrictFlag, int derivationType,
        int derivationNegation, boolean detailsFlag, boolean mutateFlag, 
        boolean detailFlowFlag)
    {
        // Mutate: 
        Init(in.GetSourceTerm());
        GetRecursiveDerivations(in, conn, trie, restrictFlag, derivationType,
            derivationNegation, INFO, true, detailsFlag, mutateFlag, null);
        UpdateFlowHistory(detailFlowFlag);   // remove the original term
        return GetDerivationVector();
    }
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   restrictFlag   a numberical flag to restrict out into LVG_ONLY
    * LVG_OR_ALL, or ALL (defined in OutputFilter).
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    * @param   detailFlowFlag   a boolean flag for showing the flow history in
    * details.  For instance use ddd instead of R.
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    * @see OutputFilter
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, int restrictFlag, boolean detailsFlag, 
        boolean mutateFlag, boolean detailFlowFlag)
    {
        // Mutate: 
        Init(in.GetSourceTerm());
        GetRecursiveDerivations(in, conn, trie, restrictFlag, 
            OutputFilter.D_TYPE_ALL, OutputFilter.D_NEGATION_OTHERWISE,
            INFO, true, detailsFlag, mutateFlag, null);
        UpdateFlowHistory(detailFlowFlag);   // remove the original term
        return GetDerivationVector();
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "medicine");      // get input String
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        int minTrieStemLength = Integer.parseInt(
            conf.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            boolean isInflection = false;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 
                minTrieStemLength);
            if(conn != null)
            {
                outs = ToRecursiveDerivations.Mutate(in, conn, trie, 
                    OutputFilter.LVG_ONLY, true, true, false);
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
    /**
    * Get derivational variants for a specified term by Lvg facts or rules.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   restrictFlag   a numberical flag to restrict out into LVG_ONLY
    * LVG_OR_ALL, or ALL (defined in OutputFilter).
    * @param   derivationType   a numberical flag to restrict derivation type
    * D_TYPE_ZERO, D_TYPE_PREFIX, D_TYPE_SUFFIX, D_TYPE_ZERO_PREFIX,
    * D_TYPE_ZERO_SUFFIX, D_TYPE_PREFIX_SUFFIX, D_TYPE_ALL (defined in 
    * OutputFilter);
    * @param   derivationNegation   a numberical flag to restrict derivation 
    * negation D_NEGATION_OTHERWISE, D_NEGATION_NEGATIVE, D_NEGATION_BOTH
    * @param   infoStr   the header of detail information, usually is the
    * full name of the current flo
    * @param   appendFlowHistory   A boolean flag used to append flow history
    * when it's value is true.  This flag should be set to ture only for the 
    * very first time and then set to be false to avoid duplicate flow symbols
    * in the flow history.
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    * @param   rFlowHistory   ccumulate flow history for recursive derivation
    *
    * @return  the results from this flow component - a collection (Vector)
    * of LexItems
    *
    * @see DbBase
    * @see OutputFilter
    */
    private static Vector<LexItem> GetDerivations(LexItem in, Connection conn,
        RamTrie trie, int restrictFlag, int derivationType,
        int derivationNegation, String infoStr, boolean appendFlowHistory, 
        boolean detailsFlag, boolean mutateFlag, String rFlowHistory)
    {
        String inStr = in.GetSourceTerm();
        Vector<LexItem> outs = new Vector<LexItem>();
        // check if input is nothing
        if(in.GetSourceTerm().length() == 0)
        {
            return outs;
        }
        // update flow hsitory for details information
        String flowName = rFlowHistory;
        try
        {
            long inCat = in.GetSourceCategory().GetValue();
            long inInfl = in.GetSourceInflection().GetValue();
            // Fact: get derivation from database
            Vector<DerivationRecord> factList = DbDerivation.GetDerivations(
                inStr, conn, derivationType, derivationNegation);
            Vector<LexItem> facts = new Vector<LexItem>();    
            // update LexItems
            for(int i = 0; i < factList.size(); i++)
            {
                DerivationRecord record = factList.elementAt(i);
                String term = record.GetTarget();
                long curCat = record.GetSourceCat();
                // input category filter:
                // inflection information is not in database, can't be check
                if(InputFilter.IsLegal(inCat, curCat) == false)
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
                        + record.GetPureString(fs)
                        + rFlowHistory + fs;
                }
                LexItem temp = UpdateLexItem(in, term, flowName, 
                    record.GetTargetCat(), 
                    Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate, false);
                facts.addElement(temp);
            }
            // Rule: Use trie to get the result from rule
            Vector<LexItem> rules = new Vector<LexItem>();    

            if((restrictFlag == OutputFilter.ALL)
            || ((restrictFlag == OutputFilter.LVG_OR_ALL)
             && (facts.size() == 0)))
            {
                // Rule: Use trie to get the result from rule
                Vector<RuleResult> ruleList = 
                    trie.GetDerivationsByRules(inStr, inCat, inInfl, true);
                // update LexItems
                for(int i = 0; i < ruleList.size(); i++)
                {
                    RuleResult record = ruleList.elementAt(i);
                    String term = record.GetOutTerm();
                    // heuristic rules: check if derivation in Lexicon
                    // the rule generated derivatin must not in Lexicon
                    if(DbUninflection.IsExistUninflectedTerm(term, conn)
                        == false)
                    {
                        // details & mutate
                        String details = null;
                        String mutate = null;
                        if(detailsFlag == true)
                        {
                            details = infoStr + " (RULE" 
                                + GlobalBehavior.GetFieldSeparator()
                                + record.GetRuleString() + ")";
                        }
                        if(mutateFlag == true)
                        {
                            String fs = GlobalBehavior.GetFieldSeparator();
                            mutate = "RULE" + fs
                                + record.GetInTerm() + fs
                                + record.GetOutTerm() + fs
                                + record.GetRuleString() + fs
                                + rFlowHistory + fs;
                        }
                        LexItem temp = UpdateLexItem(in, term, flowName,
                            Category.ToValue(record.GetOutCategory()),
                            Inflection.ToValue(record.GetOutInflection()), 
                            details, mutate, false);
                        rules.addElement(temp);
                    }
                }
            }
            // form output by the restrict flag
            outs = OutputFilter.RestrictOption(facts, rules, restrictFlag);
        }
        catch (SQLException e)
        { 
            System.err.println(
                "** Error: Sql Exception in ToRecursiveDerivation Flow.");
            System.err.println(e.toString());
        }
        // mark tag for noun/adj and others
        long adjValue = Category.GetBitValue(Category.ADJ_BIT);
        long nounValue = Category.GetBitValue(Category.NOUN_BIT);
        for(int i = 0; i < outs.size(); i++)
        {
            LexItem temp = outs.elementAt(i);
            //check noun/adj
            boolean curTagFlag =
                ((temp.GetSourceCategory().Contains(adjValue)
                  && (temp.GetTargetCategory().Contains(nounValue)))
                || (temp.GetSourceCategory().Contains(nounValue)
                  && (temp.GetTargetCategory().Contains(adjValue))));
            Tag tag = new Tag(temp.GetTag());
            boolean tagFlag = 
                curTagFlag && tag.GetBitFlag(Tag.DERV_NOUN_ADJ_BIT);
            String tagStr = ((tagFlag)?"NounAdj":"NotNounAdj");
            tag.SetBitFlag(Tag.DERV_NOUN_ADJ_BIT, tagFlag);
            temp.SetTag(tag.GetValue());
            String mutateInfo = temp.GetMutateInformation() + tagStr +
                GlobalBehavior.GetFieldSeparator();
            temp.SetMutateInformation(mutateInfo);
        }
        return outs;
    }
    private static void GetRecursiveDerivations(LexItem in, Connection conn,
        RamTrie trie, int restrictFlag, int derivationType, 
        int derivationNegation, String infoStr, boolean topLevel,
        boolean detailsFlag, boolean mutateFlag, String rFlowHistory)
    {
        CalRecursiveDerivations(in, conn, trie, restrictFlag, derivationType,
            derivationNegation, infoStr, topLevel, detailsFlag, mutateFlag, 
            rFlowHistory);
        // put result from hash table to a Vector
        derivations_ = new Vector<LexItem>(derivationHt_.values());
    }
    private static void CalRecursiveDerivations(LexItem in, Connection conn,
        RamTrie trie, int restrictFlag, int derivationType, 
        int derivationNegation, String infoStr, boolean topLevel, 
        boolean detailsFlag, boolean mutateFlag, String rFlowHistory)
    {
        // calculate detail recursive history
        StringBuffer buffer = new StringBuffer();
        if(rFlowHistory == null)
        {
            String prevHistory = in.GetFlowHistory();
            rFlowHistory = new String();
            if(prevHistory == null)
            {
                prevHistory = new String();
            }
            else
            {
                buffer.append(prevHistory);
                buffer.append("+");
            }
        }
        buffer.append(Flow.GetBitName(Flow.DERIVATION, 1));
        rFlowHistory += buffer.toString();
        Vector<LexItem> temp = GetDerivations(in, conn, trie, restrictFlag, 
            derivationType, derivationNegation, infoStr, topLevel, 
            detailsFlag, mutateFlag, rFlowHistory);
        // Add into derivation List
        for(int i = 0; i < temp.size(); i++)
        {
            LexItem tempRec = temp.elementAt(i);
            TermCatCatKey tempKey = new TermCatCatKey(tempRec.GetTargetTerm(),
                ToFruitfulVariants.GetFirstCategory(tempRec),
                (int) tempRec.GetTargetCategory().GetValue());
            if(derivationHt_.containsKey(tempKey) == true)
            {
                LexItem existRec = derivationHt_.get(tempKey);
                String existFh = existRec.GetFlowHistory();
                String tempFh = tempRec.GetFlowHistory();
                // do the recursive when the new one with same key and less hist
                if(existFh.length() > tempFh.length())
                {
                    derivationHt_.remove(tempKey);
                    derivationHt_.put(tempKey, tempRec);
                    LexItem newLexItem = LexItem.TargetToSource(tempRec);
                    CalRecursiveDerivations(newLexItem, conn, trie, 
                        restrictFlag, derivationType, derivationNegation,
                        infoStr, false, detailsFlag, mutateFlag, rFlowHistory);
                }
            }
            else if(tempRec.GetTargetTerm().equals(orgInputTerm_) == false)
            {
                derivationHt_.put(tempKey, tempRec);
                LexItem newLexItem = LexItem.TargetToSource(tempRec);
                CalRecursiveDerivations(newLexItem, conn, trie, restrictFlag, 
                    derivationType, derivationNegation, infoStr, false, 
                    detailsFlag, mutateFlag, rFlowHistory);
            }
        }
    }
    private static void Init(String inputTerm)
    {
        orgInputTerm_ = inputTerm;
        derivationHt_.clear();
        derivations_.removeAllElements();
    }
    // remove the very first elements since it is the original term
    private static void UpdateFlowHistory(boolean detailFlowFlag)
    {
        // change the detail flow flag to "R"
        if(detailFlowFlag == false)
        {
            String flowName = Flow.GetBitName(Flow.RECURSIVE_DERIVATIONS, 1);
            for(int i = 0; i < derivations_.size(); i++)
            {
                LexItem cur = derivations_.elementAt(i);
                cur.SetFlowHistory(flowName);
            }
        }
    }
    private static Vector<LexItem> GetDerivationVector()
    {
        return derivations_;
    }
    // data members
    private static final String INFO = "Recursive Derivation";
    private static Vector<LexItem> derivations_ = new Vector<LexItem>();
    private static Hashtable<TermCatCatKey, LexItem> derivationHt_ 
        = new Hashtable<TermCatCatKey, LexItem>();
    private static String orgInputTerm_ = new String();
}
