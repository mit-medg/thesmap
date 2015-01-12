package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
import java.util.*;
/*****************************************************************************
* This class establishes the Persistent files (Random Access File) from a flat 
* file. This class is run before the LVG is distributed and consided as a 
* pre-process class.  LVG end users may ignore this class.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
final public class PersistentTrieTree
{
    /**
    * Creates an object of LVG persistent trie tree, using path names of
    * trie, rules, and exception files.
    */
    public PersistentTrieTree(String triePath, String rulePath,
        String exceptionPath)
    {
        triePath_ = triePath;
        rulePath_ = rulePath;
        exceptionPath_ = exceptionPath;
        persistentTrie_ = new PersistentTree(triePath_);
    }
    // public methods
    /**
    * Establishes a radom access file from a flat file for a persistent trie
    * tree.
    * 
    * @param   isInflection  true or false if the persistent trie tree is
    * inflection or derivation. 
    * 
    * @exception  IOException  if probelms happens when accessing random access
    * file
    */
    public void BuildPersistentTrieTree(boolean isInflection) throws IOException
    {
        // establish trie tree in the RAM
        TrieTree trieTree = new TrieTree(true);
        if(isInflection == true)         // inflection
        {
            trieTree.LoadRulesFromFile("../../data/rules/", "im.rul", false, 
                true);
            trieTree.LoadRulesFromFile("../../data/rules/", "im.rul", true, 
                false);
        }
        else             // derivation
        {
            trieTree.LoadRulesFromFile("../../data/rules/", "dm.rul", false, 
                true);
            trieTree.LoadRulesFromFile("../../data/rules/", "dm.rul", true,
                false);
        }
        // establish persistent file from trie in the RAM
        TrieNode curNode = trieTree.GetRoot();
        long parentAddress = -1;        // root
        Insert(curNode, parentAddress);
    }
    /**
    * Print the detail information of the current persistent trie tree.
    *
    * @exception  IOException  if probelms happens when accessing random access
    * file
    */
    public void PrintPersistentTrieTree() throws IOException
    {
        int nodeNum = 0;
        nodeNum = PersistentTrieNode.PrintNode(persistentTrie_.GetRaf(), 
            persistentTrie_.GetRootAddress(), rulePath_, exceptionPath_);
        System.out.println("------------------------------------");
        System.out.println("-- Number of Node: " + nodeNum);
    }
    /**
    * Close the random access file of persistent trie.
    *
    * @exception  IOException  if probelms happens when accessing random access
    * file
    */
    public void Close() throws IOException
    {
        persistentTrie_.Close();
    }
    /**
    * A executable program for LVG developers to create radom access files
    * for inflections and derivations from flat files.  This program is
    * executed before LVG is distributed to generate RAF.  The commands for
    * running this program are:
    * <ul>
    * <li>java2 PersistentTrieTree -i
    *   <br> generates inflection rafs: trieI.data, ruleI.data, exceptionI.data
    * <li>java2 PersistentTrieTree -d
    *   <br> generates derivation rafs: trieD.data, ruleD.data, exceptionD.data
    * </ul>
    */
    public static void main(String[] args)
    {
        PersistentTrieTree ptt = 
            new PersistentTrieTree("trie.data", "rule.data", "exception.data");
        boolean isInflection = false;
    
        if(args.length != 1)
        {
            System.out.println("Usage: java PersistentTrieTree <-i/d>");
            System.out.println("   -i: build persistent file for inflection");
        }
        else
        {
            if(args[0].equals("-i") == true)
            {
                isInflection = true;
            }
        }
        try
        {
            ptt.BuildPersistentTrieTree(isInflection);
            ptt.PrintPersistentTrieTree();
            ptt.Close();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    // private methods
    // insert node into persistent trie tree: recursive
    private void Insert(TrieNode curNode, long parentAddress) throws IOException
    {
        // insert rules of current node to persistent list file
        long ruleAddress = -1;
        Vector<InflectionRule> rules = curNode.GetRules();
        if(rules != null)
        {
            // create a new Persistent List
            PersistentList persistentRule = new PersistentList(rulePath_);
            for(int i = 0; i < rules.size(); i++)
            {
                InflectionRule rule = rules.elementAt(i);
                // insert exceptions of current rules to persistent file
                Hashtable<String, String> exceptions = rule.GetExceptions();
                long exceptionAddress = -1;
                boolean newExceptionList = true;
                if(exceptions != null)
                {
                    // create a new Persetent List for exceptions
                    PersistentList persistentException = 
                        new PersistentList(exceptionPath_);
                    Enumeration<String> keyEn = exceptions.keys();
                    Enumeration<String> valueEn = exceptions.elements();
                    while(keyEn.hasMoreElements() == true)
                    {
                        String key = keyEn.nextElement();
                        String value = valueEn.nextElement();
                        // add exceptions to persistent file
                        PersistentExceptionNode exceptionNode = 
                            new PersistentExceptionNode(key, value);
                        persistentException.Add(exceptionNode);
                        if(newExceptionList == true)
                        {
                            newExceptionList = false;
                            exceptionAddress = exceptionNode.GetAddress()
                                - PersistentList.HEADER_OFFSET;
                        }
                    }
                    persistentException.Close();
                }
                // add rule to persistent file
                PersistentRuleNode ruleNode = 
                    new PersistentRuleNode(rule.GetRuleStr(), exceptionAddress);
                persistentRule.Add(ruleNode);
                
                if(i == 0)    // first rule: update the rule address
                {
                    // need to minus 12 for header offset
                    ruleAddress = 
                        ruleNode.GetAddress() - PersistentList.HEADER_OFFSET;
                }
            }
            persistentRule.Close();
        }
        // insert current node to persistent tree file
        char key = curNode.GetKey();
        PersistentTrieNode pNode = new PersistentTrieNode(key, ruleAddress);
        persistentTrie_.Add(pNode, parentAddress);
        // insert all child nodes to persistent tree file
        if(curNode.GetChild() != null)
        {
            parentAddress = pNode.GetAddress();
            for(int i = 0; i < curNode.GetChild().size(); i++)
            {
                TrieNode childNode = curNode.GetChild().elementAt(i);
                Insert(childNode, parentAddress);
            }
        }
    }
    // data members
    private String triePath_ = null;            // use to hold trie node
    private String rulePath_ = null;            // use for rules
    private String exceptionPath_ = null;       // use for exceptions
    private PersistentTree persistentTrie_ = null;
}
