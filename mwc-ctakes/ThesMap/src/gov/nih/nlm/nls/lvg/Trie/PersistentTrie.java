package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides API for using LVG persistent trie rules.  As a matter of 
* fact,  this class is the highest level of API for the LVG persistent trie
* package and is the only class that is used by LVG flow components.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/index.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
final public class PersistentTrie
{
    // public constructors
    /**
    * Create an object of LVG persistent trie, using a flag to indicate
    * using inflection or derivation trie.
    *
    * @param  isInflection   true or false to indicate the persistent trie 
    * type as inflections or derivations.
    * @param  minTermLength   minimum length of rule generated out term
    * @param  dir   the top directory of LVG
    */
    public PersistentTrie(boolean isInflection, int minTermLength, String dir)
    {
        isInflection_ = isInflection;
        minTermLength_ = minTermLength;
        // inflection trie
        String triePath = dir +  "/data/rules/trieI.data";
        String rulePath = dir + "/data/rules/ruleI.data";
        String exceptionPath =  dir + "/data/rules/exceptionI.data";
        // derivation trie
        if(isInflection == false)
        {
            triePath = dir +  "/data/rules/trieD.data";
            rulePath = dir + "/data/rules/ruleD.data";
            exceptionPath =  dir + "/data/rules/exceptionD.data";
        }
        // assign the random access files for the trie
        try
        {
            RandomAccessFile trieRaf = new RandomAccessFile(triePath, "r");
            RandomAccessFile ruleRaf = new RandomAccessFile(rulePath, "r");
            RandomAccessFile exceptionRaf = 
                new RandomAccessFile(exceptionPath, "r");
            trieRaf_ = trieRaf;
            ruleRaf_ = ruleRaf;
            exceptionRaf_ = exceptionRaf;
            root_ = (PersistentTrieNode) 
                PersistentTrieNode.GetNode(trieRaf_, 0);
        }
        catch (IOException e) 
        {
        }
    }
    /**
    * Create an object of LVG persistent trie, using random acccess files
    * of trie, rule, and exception.
    *
    * @param  trieRaf   random access file of Lvg persistent trie
    * @param  ruleRaf   random access file of Lvg persistent rule
    * @param  exceptionRaf   random access file of Lvg persistent exception
    * @param  minTermLength   minimum length of rule generated out term
    * @param  isInflection   true or false to indicate the persistent trie 
    * type as inflections or derivations.
    */
    public PersistentTrie(RandomAccessFile trieRaf, RandomAccessFile ruleRaf,
        RandomAccessFile exceptionRaf, int minTermLength, boolean isInflection)
    {
        isInflection_ = isInflection;
        minTermLength_ = minTermLength;
        trieRaf_ = trieRaf;
        ruleRaf_ = ruleRaf;
        exceptionRaf_ = exceptionRaf;
        try
        {
            root_ = (PersistentTrieNode) 
                PersistentTrieNode.GetNode(trieRaf_, 0);
        }
        catch (IOException e) 
        {
        }
    }
    // public methods
    /**
    * Close all random access files of Lvg persistent trie, rules, exceptions.
    */
    public void Close()
    {
        try
        {
            trieRaf_.close();
            ruleRaf_.close();
            exceptionRaf_.close();
        }
        catch (IOException e)
        {
        }
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
    * @return  Vector<RuleResult> - uninflected term
    */
    public Vector<RuleResult> GetUninflectedTermsByRules(String term, 
        long inCat, long inInfl, boolean showAll)
    {
        // get all result by the out Inflection is BASE
        Vector<RuleResult> resultList = Mutate(term, showAll, inCat, 
            inInfl, LEGAL_CATEGORY, LEGAL_BASE);
        // sort resut list by the order of noun, verb, adj, and adv
        RuleResultComparator<RuleResult> rrc 
            = new RuleResultComparator<RuleResult>();
        Collections.sort(resultList, rrc); 
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
        Vector<RuleResult> resultList = new Vector<RuleResult>();
        long cat = 0;
        long infl = 0;
        // go through trie to find the rule if match nodes
        try
        {
            Vector<PersistentTrieNode> matchNodeList = FindRule(term);
            matchedNodeNum_ = matchNodeList.size();
            // Go through all match nodes
            int curLevel = 0;
            for(int i = 0; i < matchNodeList.size(); i++)
            {
                PersistentTrieNode node = matchNodeList.elementAt(i);
                long ruleAddess = node.GetRuleAddress();
                if((node == null) || (ruleAddess == -1))
                {
                    System.out.println(
                        "** Error: null in TrieNode or it's rules");
                    break;
                }
                long nextAddress = ruleAddess;
                if(nextAddress != -1)
                {
                    nextAddress += PersistentList.HEADER_OFFSET;  // skip header
                    while(nextAddress != -1)    // go through all rules
                    {
                        PersistentRuleNode ruleNode = (PersistentRuleNode) 
                            PersistentRuleNode.GetNode(ruleRaf_, nextAddress);
                        InflectionRule rule = 
                            new InflectionRule(ruleNode.GetRuleString());
                        // check exception
                        if(IsException(term, ruleNode.GetExceptionAddress()) == 
                            false)
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
                        nextAddress = ruleNode.GetNext();
                    }
                }
            }
        }
        catch (Exception e) { }
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
    * @return  Vector<CatInfl> categories and inflections
    */
    public Vector<CatInfl> GetCatInflsByRules(String term, long inCat, 
        long inInfl)
    {
        Vector<RuleResult> resultList = new Vector<RuleResult>();
        long cat = 0;
        long infl = 0;
        Vector<CatInfl> outs = new Vector<CatInfl>();
        // go through trie to find the rule if match nodes
        try
        {
            Vector<PersistentTrieNode> matchNodeList = FindRule(term);
            matchedNodeNum_ = matchNodeList.size();
            // Go through all match nodes
            int curLevel = 0;
            for(int i = 0; i < matchNodeList.size(); i++)
            {
                PersistentTrieNode node = matchNodeList.elementAt(i);
                long ruleAddess = node.GetRuleAddress();
                if((node == null) || (ruleAddess == -1))
                {
                    System.out.println(
                        "** Error: null in TrieNode or it's rules");
                    break;
                }
                long nextAddress = ruleAddess;
                if(nextAddress != -1)
                {
                    nextAddress += PersistentList.HEADER_OFFSET;  // skip header
                    while(nextAddress != -1)    // go through all rules
                    {
                        PersistentRuleNode ruleNode = (PersistentRuleNode) 
                            PersistentRuleNode.GetNode(ruleRaf_, nextAddress);
                        InflectionRule rule = 
                            new InflectionRule(ruleNode.GetRuleString());
                        // check exception
                        if(IsException(term, ruleNode.GetExceptionAddress()) == 
                            false)
                        {
                            long curCat = rule.GetInCategory();
                            long curInfl = rule.GetInInflection();
                            CatInfl catInfl = new CatInfl(curCat, curInfl);
                            outs.addElement(catInfl);
                        }
                        nextAddress = ruleNode.GetNext();
                    }
                }
            }
        }
        catch (Exception e) { }
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
    * @return  Vector<RuleResult> of inflected term in a ruleResult format
    */
    public Vector<RuleResult> GetInflectedTermsByRules(String term, long inCat,
        long inInfl, boolean showAll)
    {
        // get all uninflected terms by the out Inflection is BASE
        Vector<RuleResult> uninflectedList = Mutate(term, showAll, inCat, 
            inInfl, LEGAL_CATEGORY, LEGAL_BASE);
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
                tempResult = Mutate(tempTerm, showAll, cat, LEGAL_INFLECTION,
                    LEGAL_CATEGORY, LEGAL_INFLECTION);
            }
            lastTerm = tempTerm;
            lastCat = cat;
            // Hueristic rule: if the result is an uninflected term with
            // different spelling with the base, it should be dropped
            Vector<RuleResult> newTempResult 
                = RemoveIllegalTerms(tempTerm, tempResult); 
            // Add temp result into resultList if it is not exist
            resultList = AddRusultsToInflectList(resultList, newTempResult);
        }
        // sort resut list by the order of noun, verb, adj, and adv
        RuleResultComparator<RuleResult> rrc 
            = new RuleResultComparator<RuleResult>();
        Collections.sort(resultList, rrc); 
        return resultList;
    }
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
            if((Inflection.Contains(LEGAL_BASE, infl) == false)
            || (base.equals(tempTerm) == true))
            {
                out.addElement(temp);
            }
        }
        return out;
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
    * @return  Vector<RuleResult> - inflected terms
    */
    public Vector<RuleResult> GetDerivationsByRules(String term, long inCat, 
        long inInfl, boolean showAll)
    {
        // get all result by the out Inflection is BASE
        Vector<RuleResult> resultList = Mutate(term, showAll, inCat, 
            inInfl, LEGAL_CATEGORY, LEGAL_BASE);
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
    * A test driver for this class.
    */
    public static void main(String[] args)
    {
        if(args.length != 3)
        {
            System.out.println("Usage: java PersistentTrie <term> <-i/d> <-s>");
            System.out.println(" term: input term for testing");
            System.out.println(" -i/d: Get inflection/Derivation");
            System.out.println("   -s: mutate with all branch rules applied");
        }
        else
        {
            String inStr = args[0];
            // inflection or derivation
            String triePath = "../data/rules/trieI.data";
            String rulePath = "../data/rules/ruleI.data";
            String exceptionPath = "../data/rules/exceptionI.data";
            boolean isInflection = true;
            if(args[1].equals("-d") == true)
            {
                triePath = "../data/rules/trieD.data";
                rulePath = "../data/rules/ruleD.data";
                exceptionPath = "../data/rules/exceptionD.data";
                isInflection = false;
            }
            // show all
            boolean showAll = false;
            if(args[2].equals("-s") == true)
            {
                showAll = true;            // not function in this test driver
            }
            try
            {
                RandomAccessFile trieRaf = new RandomAccessFile(triePath, "r");
                RandomAccessFile ruleRaf = new RandomAccessFile(rulePath, "r");
                RandomAccessFile exceptionRaf = 
                    new RandomAccessFile(exceptionPath, "r");
                PersistentTrie trie = new PersistentTrie(trieRaf, ruleRaf, 
                    exceptionRaf, 3, isInflection);
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
                    Vector<RuleResult> result = trie.GetDerivationsByRules(
                        inStr, Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE,                         true);
                    trie.PrintResults(result);
                }
                // close all Raf
                trieRaf.close();
                ruleRaf.close();
                exceptionRaf.close();
            }
            catch (Exception e) 
            { 
                System.err.println(e.getMessage());
            }
        }
    }
    // private methods
    // return a vector of RuleResult: inTerm, outTerm, rulesStr
    private Vector<RuleResult> Mutate(String term, boolean showAll, 
        long inCategory, long inInflection, long outCategory, 
        long outInflection)
    {
        Vector<RuleResult> resultList = new Vector<RuleResult>();
        // go through trie to find the rule if match nodes
        try
        {
            Vector<PersistentTrieNode> matchNodeList = FindRule(term);
            matchedNodeNum_ = matchNodeList.size();
            // Go through all match nodes
            int curLevel = 0;
            for(int i = 0; i < matchedNodeNum_; i++)
            {
                PersistentTrieNode node = matchNodeList.elementAt(i);
                long ruleAddess = node.GetRuleAddress();
                if((node == null) || (ruleAddess == -1))
                {
                    System.out.println(
                        "** Error: null in TrieNode or it's rules");
                    break;
                }
                long nextAddress = ruleAddess;
                if(nextAddress != -1)
                {
                    nextAddress += PersistentList.HEADER_OFFSET;  // skip header
                    while(nextAddress != -1)    // go through all rules
                    {
                        PersistentRuleNode ruleNode = (PersistentRuleNode) 
                            PersistentRuleNode.GetNode(ruleRaf_, nextAddress);
                        InflectionRule rule = 
                            new InflectionRule(ruleNode.GetRuleString());
                        // check exception
                        if(IsException(term, ruleNode.GetExceptionAddress()) == 
                            false)
                        {
                            RuleResult result = ApplyRules(term, rule, 
                                inCategory, inInflection, outCategory,
                                outInflection);
                            // showAll: add all nodes on the traversal path
                            if(result != null)
                            {
                                if(showAll == true)
                                {
                                    resultList.add(result);
                                }
                                else 
                                // add only the lowest node on traversal path
                                {
                                    // filter out the result
                                    // reset and add result if at higher level
                                    if(node.GetLevel() > curLevel)
                                    {
                                        curLevel = node.GetLevel();
                                        resultList.removeAllElements();
                                        resultList.add(result);
                                    }
                                    // add result if at the same level
                                    else if(node.GetLevel() == curLevel)
                                    {
                                        resultList.add(result);
                                    }
                                }
                            }
                        }
                        nextAddress = ruleNode.GetNext();
                    }
                }
            }
        }
        catch (Exception e) { }
        // no need to do further check if it is a derivation trie
        if(isInflection_ == false)
        {
            return resultList;
        }
        // heuristic rule: check the length
        Vector<RuleResult> out = new Vector<RuleResult>();
        for(int i = 0; i < resultList.size(); i++)
        {
            RuleResult temp = resultList.elementAt(i);
            String tempStr = temp.GetOutTerm();
            if((tempStr.length() > minTermLength_)   // check min. length
            || (tempStr.equals(term)))               // return the input term
            {
                out.addElement(temp);
            }
        }
        return out;
    }
    // add the result into inflection list
    static Vector<RuleResult> AddRusultsToInflectList(
        Vector<RuleResult> oldList, Vector<RuleResult> newList)
    {
        Vector<RuleResult> out = new Vector<RuleResult>(oldList);
        for(int i = 0; i < newList.size(); i++)
        {
            RuleResult temp = newList.elementAt(i);
            if(HasResultWithSameOut(out, temp) == false)
            // min char limit: canceled by lu, 7-19 TBD
            {
                out.addElement(temp);
            }
        }
        return out;
    }
    private static boolean HasResultWithSameOut(Vector<RuleResult> list, 
        RuleResult result)
    {
        boolean hasSameOut = false;
        for(int i = 0; i < list.size(); i++)
        {
            RuleResult temp = list.elementAt(i);
            // return true if list contains result
            if((temp.GetOutTerm().equals(result.GetOutTerm()))
            && (temp.GetOutCategoryAndInflection().equals(
                result.GetOutCategoryAndInflection())))
            {
                hasSameOut = true;
                break;
            }
        }
        return hasSameOut;
    }
    private Vector<PersistentTrieNode> FindRule(String inStr) throws IOException
    {
        // form the input string to a char array
        String curStr = inStr.trim() + '$';
        char[] inCharArray = curStr.toCharArray();
        // init the foundNodeList
        foundNodeList_ = new Vector<PersistentTrieNode>();
        foundNodeList_.addElement(root_);
        FindNode(root_, inCharArray);
        return foundNodeList_;
    }
    private boolean IsException(String inStr, long exceptionAddress)
        throws IOException
    {
        boolean isException = false;
        long nextAddress = exceptionAddress;
        if(nextAddress != -1)
        {
            nextAddress += PersistentList.HEADER_OFFSET;    // skip header
        
            while(nextAddress != -1)    // go through all exceptions
            {
                PersistentExceptionNode exceptionNode = 
                    (PersistentExceptionNode)
                    PersistentExceptionNode.GetNode(exceptionRaf_, nextAddress);
                if(inStr.equals(exceptionNode.GetKey()) == true)
                {
                    isException = true;
                    break;
                }
                nextAddress = exceptionNode.GetNext();
            }
        }
        return isException;
    }
    // Change inTerm to outTerm base on the rules found
    private RuleResult ApplyRules(String inStr, InflectionRule rule, 
        long inCategory, long inInflection, long outCategory,
        long outInflection)
    {
        String tempStr = inStr + '$';
        String inSuffix = rule.GetInSuffix();
        String outSuffix = rule.GetOutSuffix();
        int tempSize = tempStr.length();
        int inSize = inSuffix.length();
        long inCat = Category.ToValue(rule.GetInCategoryStr());
        long outCat = Category.ToValue(rule.GetOutCategoryStr());
        long inInf = Inflection.ToValue(rule.GetInInflectionStr());
        long outInf = Inflection.ToValue(rule.GetOutInflectionStr());
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
    private boolean FindNode(PersistentTrieNode node, char[] charArray)
        throws IOException
    {
        char curKey = node.GetKey();
        int curLevel = node.GetLevel();
        int arraySize = Array.getLength(charArray);
        int index = arraySize-1-curLevel;           // index in the char array
        // check if current key is the same as char in CharArray
        if(WildCard.IsMatchKey(curKey, index, charArray) == true)
        {
            // assign match node if there are rules for this node
            long ruleAddress = node.GetRuleAddress();
            if(ruleAddress != -1)     
            {
                foundNode_ = node;
                AddNodeToFoundList();    // Add foundNode_ into foundNodeList_
            }
            if(index == 0)  // reach the beginning of the inStr, stop travers
            {
                return true;
            }
            // travers the tree
            boolean oneChildMatch = false;
            if(node.GetChild() != -1)
            {
                long nextAddress = node.GetChild();   // assign next to child
                while(nextAddress != -1)
                {
                    PersistentTrieNode nextNode = (PersistentTrieNode) 
                        PersistentTrieNode.GetNode(trieRaf_, nextAddress);
                    if(FindNode(nextNode, charArray) == true)
                    {
                        oneChildMatch = true;
                    }
                    nextAddress = nextNode.GetNext();
                }
                if(oneChildMatch == true)   // update list if there is one match
                {
                    AddNodeToFoundList();
                }
            }
            else    // reach end the the tree branch
            {
                return true;
            }
        }
        else     // no matching for current node
        {
            return false;
        }
        return true;
    }
    private void AddNodeToFoundList()
    {
        boolean duplicateNode = false;
        for(int i = 0; i < foundNodeList_.size(); i++)
        {
            if(foundNode_ == foundNodeList_.elementAt(i))
            {
                duplicateNode = true;
                break;
            }
        }
        if(duplicateNode == false)
        {
            foundNodeList_.addElement(foundNode_);
        }
    }
    // data members
    /** a number get by using bit-or for legal categories: 
    adj, adv, noun, verb */
    public final static long LEGAL_CATEGORY = 1155;    // adj, adv, noun, verb
    
    /** a number get by using bit-or for legal inflections: 
    * base - singular - plural
    *      - positive - comparative - superlative
    *      - infinitive - pres - past - presPart - pastPart
    */
    public final static long LEGAL_INFLECTION = 2099071;
    // base, singular, positive, infinitive
    public final static long LEGAL_BASE = 1793;        
    // private data members
    private Vector<PersistentTrieNode> foundNodeList_ = null;
    private PersistentTrieNode root_ = null;
    private PersistentTrieNode foundNode_ = null;
    private RandomAccessFile trieRaf_ = null;          // use to hold trie node
    private RandomAccessFile ruleRaf_ = null;          // use for rules
    private RandomAccessFile exceptionRaf_ = null;     // use for exceptions
    private int matchedNodeNum_ = 0;
    private int minTermLength_ = 2;
    private boolean isInflection_ = true;
}
