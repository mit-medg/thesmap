package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class establishes Trie from flat files and put them into Ram.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/index.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
final public class RamTrie
{
    // public constructors
    /**
    * Create an object of LVG trie, using a flag to indicate
    * using inflection or derivation trie.
    *
    * @param  isInflection   true or false to indicate the persistent trie
    * type as inflections or derivations.
    * @param  minTermLength   minimum length of rule generated out term
    * @param  dir   the top directory of LVG
    * @param  minTrieStemLength   min. legal stem length in trie
    */
    public RamTrie(boolean isInflection, int minTermLength, String dir,
        int minTrieStemLength)
    {
        isInflection_ = isInflection;
        minTermLength_ = minTermLength;
        minTrieStemLength_ = minTrieStemLength;
        // inflection trie
        String trieDir = dir + "/data/rules/";
        String ruleFileName = "im.rul";
        // derivation trie
        if(isInflection == false)
        {
            ruleFileName = "dm.rul";
        }
        trie_.LoadRulesFromFile(trieDir, ruleFileName, false, true);  // forward
        trie_.LoadRulesFromFile(trieDir, ruleFileName, true, false);  // reverse
    }
    // public methods
    /**
    * Traverse along the trie tree, find the matching suffix pattern, modify
    * the suffix according to the rules, and print out results.
    *
    * @param  term   a term to be used for finding inflection or derivation
    * @param term  the term to be found for uninflections
    * @param inCategory the input category
    * @param inInflection the input inflection
    * @param outCategory the output category
    * @param outInflection the output inflection
    * @param showAll  set false to get uninflected terms from a node that has
    * matching suffix and ignore all other matching nodes above it in the
    * same tree branch.
    * <br>set true to get all uninflected terms from all nodes that has matching
    * suffix in the entire tree.
    */
    private Vector<RuleResult> Mutate(String term, boolean showAll, 
        long inCategory, long inInflection, long outCategory, 
        long outInflection)
    {
        Vector<RuleResult> resultList = new Vector<RuleResult>();
        // go through trie to find the rule if match nodes
        Vector<TrieNode> matchNodeList = trie_.FindRule(term);
        matchedNodeNum_ = matchNodeList.size();
        // Go through all match nodes
        int curLevel = 0;
        for(int i = 0; i < matchedNodeNum_; i++)
        {
            TrieNode node = matchNodeList.elementAt(i);
            Vector<InflectionRule> rules = node.GetRules();
            if((node == null) || (rules == null))
            {
                System.err.println("** Error: null in TrieNode or it's rules");
                break;
            }
            // go through all matched rules
            for(int j = 0; j < rules.size(); j++)    
            {
                InflectionRule rule = rules.elementAt(j); 
                // suffix rule, check stem length for derivation
                if((isInflection_ == false)
                && (minTrieStemLength_ > 0)
                && (GetStemLength(term, rule) <= minTrieStemLength_))
                {
                    continue;
                }
                // apply rules and add into result
                if(IsException(term, rule) == false)
                {
                    RuleResult result = ApplyRules(term, rule,
                        inCategory, inInflection, outCategory, outInflection);
                    // showAll: add all nodes on the traversal path
                    if(result != null)
                    {
                        if(showAll == true)
                        {
                            resultList.add(result);
                        }
                        else    // add only the lowest node on traversal path
                        {
                            // filter out the result
                            // reset and add result if at higher level
                            if(node.GetLevel() > curLevel)
                            {
                                curLevel = node.GetLevel();
                                resultList.removeAllElements();
                                resultList.add(result);
                            }    // add result if at the same level
                            else if(node.GetLevel() == curLevel)
                            {
                                resultList.add(result);
                            }
                        }
                    }
                }
            }
        }
        // heuristic rule: check the length
        Vector<RuleResult> out = new Vector<RuleResult>();
        for(int i = 0; i < resultList.size(); i++)
        {
            RuleResult temp = resultList.elementAt(i);
            String tempStr = temp.GetOutTerm();
            if((tempStr.length() >= minTermLength_)   // check min. length
            || (tempStr.equals(term)))               // return the input term
            {
                out.addElement(temp);
            }
        }
        return out;
    }
    /**
    * Get uninflected terms for a specific inflected term from LVG trie rules.
    *
    * @param term  the term to be found for uninflections
    * @param inCat the input category
    * @param inInfl the input inflection
    * @param showAll  set false to get uninflected terms from a node that has
    * matching suffix and ignore all other matching nodes above it in the
    * same tree branch.
    * <br>set true to get all uninflected terms from all nodes that has matching
    * suffix in the entire tree.
    *
    * @return  Vector&lt;RuleResult> of uninflected term 
    */
    public Vector<RuleResult> GetUninflectedTermsByRules(String term, 
        long inCat, long inInfl, boolean showAll)
    {
        // get all result by the out Inflection is BASE
        Vector<RuleResult> resultList = Mutate(term, showAll, inCat,
            inInfl, PersistentTrie.LEGAL_CATEGORY, PersistentTrie.LEGAL_BASE);
        // sort resut list by the order of noun, verb, adj, and adv
        RuleResultComparator<RuleResult> rrc 
            = new RuleResultComparator<RuleResult>();
        //TBD: Collections.sort(resultList, rrc);
        return resultList;
    }
    /**
    * Get all possible categories and inflections for a term from trie rules.
    *
    * @param term  the term to be found all possible categories and inflections
    * @param inCat  input categories
    * @param inInfl  input inflections
    *
    * @return  a record of combined categories and inflections
    */
    public CatInfl GetCatInflByRules(String term, long inCat, long inInfl)
    {
        long cat = 0;
        long infl = 0;
        // go through trie to find the rule if match nodes
        Vector<TrieNode> matchNodeList = trie_.FindRule(term);
        matchedNodeNum_ = matchNodeList.size();
        // Go through all match nodes
        int curLevel = 0;
        for(int i = 0; i < matchNodeList.size(); i++)
        {
            TrieNode node = matchNodeList.elementAt(i);
            Vector<InflectionRule> rules = node.GetRules();
            if((node == null) || (rules == null))
            {
                System.err.println("** Error: null in TrieNode or it's rules");
                break;
            }
            // go through all matched rules
            for(int j = 0; j < rules.size(); j++)
            {
                InflectionRule rule = rules.elementAt(j);
                if(IsException(term, rule) == false)
                {
                    long curCat = rule.GetInCategory();
                    long curInfl = rule.GetInInflection();
                    if((Bit.Contain(inCat, curCat) == true)
                    && (Bit.Contain(inInfl, curInfl) == true))
                    {
                        cat = cat | curCat;
                        infl = infl | curInfl;
                    }
                }
            }
        }
        CatInfl catInfl = new CatInfl(cat, infl);
        return catInfl;
    }
    /**
    * Get all possible categories and inflections for a term from trie rules.
    *
    * @param term  the term to be found all possible categories and inflections
    * @param inCat  input categories
    * @param inInfl  input inflections
    *
    * @return  Vector<CatInfl> -  categories and inflections
    */
    public Vector<CatInfl> GetCatInflsByRules(String term, long inCat, 
        long inInfl)
    {
        long cat = 0;
        long infl = 0;
        Vector<CatInfl> outs = new Vector<CatInfl>();
        // go through trie to find the rule if match nodes
        Vector<TrieNode> matchNodeList = trie_.FindRule(term);
        matchedNodeNum_ = matchNodeList.size();
        // Go through all match nodes
        int curLevel = 0;
        for(int i = 0; i < matchNodeList.size(); i++)
        {
            TrieNode node = matchNodeList.elementAt(i);
            Vector<InflectionRule> rules = node.GetRules();
            if((node == null) || (rules == null))
            {
                System.err.println("** Error: null in TrieNode or it's rules");
                break;
            }
            // go through all matched rules
            for(int j = 0; j < rules.size(); j++)
            {
                InflectionRule rule = rules.elementAt(j);
                // check exception
                if(IsException(term, rule) == false)
                {
                    long curCat = rule.GetInCategory();
                    long curInfl = rule.GetInInflection();
                    CatInfl catInfl = new CatInfl(curCat, curInfl);
                    outs.addElement(catInfl);
                }
            }
        }
        return outs;
    }
    /**
    * Get inflected terms for a specific term from LVG trie rules.
    *
    * @param term  the term to be found for inflections
    * @param inCat the input category
    * @param inInfl the input inflection
    * @param showAll  set false to get inflected terms from a node that has
    * matching suffix and ignore all other matching nodes above it in the
    * same tree branch.
    * <br>set true to get all inflected terms from all nodes that has matching
    * suffix in the entire tree.
    *
    * @return  Vector<RuleResult> of inflected term
    */
    public Vector<RuleResult> GetInflectedTermsByRules(String term, long inCat, 
        long inInfl, boolean showAll)
    {
        // get all uninflected terms by the out Inflection is BASE
        Vector<RuleResult> uninflectedList = Mutate(term, showAll, inCat,
            inInfl, PersistentTrie.LEGAL_CATEGORY, PersistentTrie.LEGAL_BASE);
        // get all inflected term by go through all base term
        Vector<RuleResult> resultList = new Vector<RuleResult>();
        String lastTerm = null;
        long lastCat = -1;
        for(int i = 0; i < uninflectedList.size(); i ++)
        {
            RuleResult temp = uninflectedList.elementAt(i);
            String tempTerm = temp.GetOutTerm();
            String ruleStr = temp.GetRuleString();
            long cat = Category.ToValue(temp.GetOutCategory());  // out Cat
            // get Uninflected terms only if the infinitive are different
            Vector<RuleResult> tempResult = new Vector<RuleResult>();
            if((cat != lastCat)
            || (tempTerm.equals(lastTerm) != true))
            {
                tempResult = Mutate(tempTerm, showAll, cat, 
                    PersistentTrie.LEGAL_INFLECTION,
                    PersistentTrie.LEGAL_CATEGORY, 
                    PersistentTrie.LEGAL_INFLECTION);
            }
            lastTerm = tempTerm;
            lastCat = cat;
            // Hueristic rule: if the result is an uninflected term with
            // different spelling with the base, it should be dropped
            Vector<RuleResult> newTempResult 
                = RemoveIllegalTerms(tempTerm, tempResult);
            // Add temp result into resultList if it is not exist
            resultList = PersistentTrie.AddRusultsToInflectList(
                resultList, newTempResult);
        }
        // sort resut list by the order of noun, verb, adj, and adv
        RuleResultComparator<RuleResult> rrc 
            = new RuleResultComparator<RuleResult>();
        Collections.sort(resultList, rrc);
        return resultList;
    }
    /**
    * Get derivation for a specific term from LVG trie rules.
    *
    * @param term  the term to be found for derivations
    * @param inCat the input category
    * @param inInfl the input inflection
    * @param showAll  set false to get derivation from a node that has
    * matching suffix and ignore all other matching nodes above it in the
    * same tree branch.
    * <br>set true to get all derivation from all nodes that has matching
    * suffix in the entire tree.
    *
    * @return  Vector<RuleResult> - of derivation
    */
    public Vector<RuleResult> GetDerivationsByRules(String term, long inCat, 
        long inInfl, boolean showAll)
    {
        // get all result by the out Inflection is BASE
        Vector<RuleResult> resultList = Mutate(term, showAll, inCat,
            inInfl, PersistentTrie.LEGAL_CATEGORY, PersistentTrie.LEGAL_BASE);
        return resultList;
    }
    /**
    * Print out a collection of trie ruleresult.
    *
    * @param resultList  A vector of ruleResult to be print out.
    */
    public void PrintResults(Vector<RuleResult> resultList)
    {
        // print out result
        System.out.println("-- matchNodeList size: " + GetMatchedNodeNum());
        for(int i = 0; i < resultList.size(); i++)
        {
            RuleResult result = resultList.elementAt(i);
            System.out.println(result.GetInTerm() + " --> " +
                result.GetOutTerm() + " ... Rule: " +
                result.GetRuleString());
        }
    }
    /**
    * Get the object of trie tree.
    *
    * @return   the trie tree is using
    */
    public TrieTree GetTrie()
    {
        return trie_;
    }
    /**
    * Get the total number of nodes which match suffix
    *
    * @return   A vector of ruleResult to be print out.
    */
    public int GetMatchedNodeNum()
    {
        return matchedNodeNum_;
    }
    /**
    * Set the minimum term length
    *
    * @param   minTermLength   minimum term length used in Morpology
    */
    public void SetMinTermLength(int minTermLength)
    {
        minTermLength_ = minTermLength;
    }
    /**
    * This is the executable program for using LVG rule trie through RAM.
    * In other words, this program read all information of LVG rules and load 
    * them up into RAM.  The command of running this program is:
    * <br> java2 RamTrie <term> <-i/-d> <-ps>
    * <br> &lt term &gt: input term for testing
    * <br> &lt -i &gt: mutate with all branch rules applied
    * <br> &lt -p &gt: print details, rule, & exceptions
    * <br> &lt -s &gt: mutate with all branch rules applied
    */
    public static void main(String[] args)
    {
        if((args.length != 3))
        {
            System.out.println("Usage: java RamTrie <term> <-i/d> <-ps>");
            System.out.println(" term: input term for testing");
            System.out.println("   -i: mutate with all branch rules applied");
            System.out.println("   -p: print details, rule, & exceptions");
            System.out.println("   -s: mutate with all branch rules applied");
        }
        else
        {
            String inStr = args[0];
            Configuration conf = new Configuration("data.config.lvg", true);
            String dir = conf.GetConfiguration(Configuration.LVG_DIR)
                + "/data/rules/";
            // inflection or derivation
            boolean isInflection = true;
            if(args[1].equals("-d") == true)
            {
                isInflection = false;
            }
            // show all
            boolean showAll = false;
            if(args[2].equals("-s") == true)
            {
                showAll = true;            // not function in this test driver
            }
        
            int minTrieStemLength = Integer.parseInt(
                conf.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
            RamTrie trie = new RamTrie(isInflection, 3, dir, minTrieStemLength);
            if(isInflection == true)
            {
                System.out.println("-------- Uninflected Terms ----------");
                Vector<RuleResult> result =
                    trie.GetUninflectedTermsByRules(inStr,
                    Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE, true);
                trie.PrintResults(result);
                System.out.println("-------- Inflected Terms ------------");
                result = trie.GetInflectedTermsByRules(inStr,
                    Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE, true);
                trie.PrintResults(result);
                System.out.println("------ Category & Inflection -----");
                CatInfl catInfl = trie.GetCatInflByRules(inStr,
                    Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE);
                System.out.println(catInfl.GetCategory() + ", " + 
                    catInfl.GetInflection());
                System.out.println("------ Categories & Inflections -----");
                Vector<CatInfl> result2 = trie.GetCatInflsByRules(inStr,
                    Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE);
                for(int i = 0; i < result2.size(); i++)
                {
                    catInfl = result2.elementAt(i);
                    System.out.println(catInfl.GetCategory() + ", "
                        + catInfl.GetInflection());
                }
            }
            else
            {
                System.out.println("---------- Derivations -------------");
                Vector<RuleResult> result = trie.GetDerivationsByRules(inStr,
                    Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE, true);
                trie.PrintResults(result);
            }
        }
    }
    // private methods
    private Vector<RuleResult> RemoveIllegalTerms(String base, 
        Vector<RuleResult> inflections)
    {
        Vector<RuleResult> out = new Vector<RuleResult>();
        for(int i = 0; i < inflections.size(); i++)
        {
            RuleResult temp = inflections.elementAt(i);
            String tempTerm = temp.GetOutTerm();
            String ruleStr = temp.GetRuleString();
            long infl = Inflection.ToValue(temp.GetOutInflection());
            // drop it if it is an uninflected form && different spelling
            // add it if it is not an uninflected form || same spelling
            if((Inflection.Contains(PersistentTrie.LEGAL_BASE, infl) == false)
            || (base.equals(tempTerm) == true))
            {
                out.addElement(temp);
            }
        }
        return out;
    }
    private boolean IsException(String inStr, InflectionRule rule)
    {
        boolean isException = false;
        Hashtable<String, String> exceptions = rule.GetExceptions();
        if(exceptions != null)
        {
            isException = exceptions.containsKey(inStr);
        }
        return isException;
    }
    private int GetStemLength(String term, InflectionRule rule)
    {
        int stemLength = term.length() + 1 - rule.GetInSuffix().length();
        return stemLength;
    }
    private RuleResult ApplyRules(String inStr, InflectionRule rule,
        long inCategory, long inInflection, long outCategory,
        long outInflection)
    {
        String tempStr = inStr + '$';
        String inSuffix = rule.GetInSuffix();
        String outSuffix = rule.GetOutSuffix();
        int tempSize = tempStr.length();
        int inSize = inSuffix.length();
        long inCat = rule.GetInCategory();
        long outCat = rule.GetOutCategory();
        long inInf = rule.GetInInflection();
        long outInf = rule.GetOutInflection();
        String unchangeStr = tempStr.substring(0, tempSize-inSize); 
        String changeStr = WildCard.GetSuffix(inSuffix, outSuffix, tempStr);
        String outStr = unchangeStr + changeStr;
        outStr = outStr.substring(0, outStr.length()-1);    // remove '$'
        RuleResult out = null;
        // check category and inflection
        if((Category.Contains(inCategory, inCat) == true)
        && (Category.Contains(outCategory, outCat) == true)
        && (Inflection.Contains(inInflection, inInf) == true)
        && (Inflection.Contains(outInflection, outInf) == true))
        {
            // add rule and new result into output
            out = new RuleResult(inStr, outStr, rule.GetRuleStr());
        }
        return out;
    }
    // data members
    private TrieTree trie_ = new TrieTree(true);
    private int minTermLength_ = 3;
    private int matchedNodeNum_ = 0;
    private int minTrieStemLength_ = 0;
    private boolean isInflection_ = true;
}
