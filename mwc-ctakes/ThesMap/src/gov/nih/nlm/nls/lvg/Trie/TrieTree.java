package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class creates an object of a trie tree.  A trie trie include a tree
* structure consists of trie nodes.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/trietree.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public class TrieTree
{
    // public constructors
    /**
    * Create an object of trie tree (default).
    */
    public TrieTree()
    {
        root_ = new TrieNode(WildCard.END, 0);
    }
    /**
    * Create an object of trie tree, using a boolean flag of wild card.
    * 
    * @param   wildCard  a boolean flag and is set true for using wildcard
    */
    public TrieTree(boolean wildCard)
    {
        wildCard_ = wildCard;
        root_ = new TrieNode(WildCard.END, 0);
    }
    // public methods
    /**
    * Set the boolean flag of the wild card for a trie tree.
    * 
    * @param   wildCard  a boolean flag and is set true for using wildcard
    */
    public void SetWildCard(boolean wildCard)
    {
        wildCard_ = wildCard;
    }
    /**
    * Read in LVG trie rules from a specified rule.
    *
    * @param   dir   the path name of the directory of the LVG trie rule file  
    * @param   fName   the file name of the LVG trie rule file  
    * @param   reverse   true or false to load the rule in forward or backward
    * @param   duplicateFlag   flag to send warning message when duplicate rule
    * direction from the file.  All rules in LVG trie are bi-directional.
    */
    public void LoadRulesFromFile(String dir, String fName, boolean reverse,
        boolean duplicateFlag)
    {
        // check the file name
        if((fName == null) || (dir == null))
        {
            return;
        }
        String fullName = dir + fName;
        try
        {
            String line = null;
            BufferedReader in = new BufferedReader(new FileReader(fullName));
            // read in line by line from a file
            while((line = in.readLine()) != null)
            {
                if(line.length() > 0)    // check if the line is empty
                {
                    if(line.startsWith(EXCEPTION) == true) // An Exception
                    {
                        StringTokenizer buf = new StringTokenizer(line, " ");
                        buf.nextToken();
                        line = ReverseException(buf.nextToken(), reverse);
                        curRule_.AddException(line);
                        exceptionNum_++;
                    }
                    else if(line.startsWith(RULE) == true)        // a Rule
                    {
                        StringTokenizer buf = new StringTokenizer(line, " ");
                        buf.nextToken();
                        line = ReverseRule(buf.nextToken(), reverse);
                        AddRule(line, duplicateFlag);
                    }
                    else if (line.startsWith(FILE) == true)   // file
                    {
                        StringTokenizer buf = new StringTokenizer(line, " ");
                        buf.nextToken();
                        String newFileName = buf.nextToken();
                        LoadRulesFromFile(dir, newFileName, reverse, 
                            duplicateFlag);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception: " + e.toString());
            System.err.println("**Error: problem of opening/reading file '"
                + fullName + "'.");
        }
    }
    /**
    * Find a collection of TrieNodes which matches the suffix as a specified
    * term.
    *
    * @param  inStr   a string is used to find nodes with matching suffix
    */
    public Vector<TrieNode> FindRule(String inStr)
    {
        // form the input string to a char array
        String curStr = inStr.trim() + '$';
        char[] inCharArray = curStr.toCharArray();
        // init the foundNodeList
        foundNodeList_ = new Vector<TrieNode>();
        foundNodeList_.addElement(root_);
        FindNode(root_, inCharArray);
        return foundNodeList_;
    }
    /**
    * Print the detail informatin of current trie tree.
    *
    * @param   details   true or false to print out details (traverse path) 
    * or not of the current trie tree
    * @param   rulesFlag   true or false to print out all rules of the
    * current trie tree
    * @param   exceptionFlag   true or false to print out all exceptions of
    * the current trie tree
    */
    public void PrintTrie(boolean details, boolean rulesFlag,
        boolean exceptionFlag)
    {
        PrintTrie(root_, details, rulesFlag, exceptionFlag);
    }
    /**
    * Get the root trie node of the current tire tree.
    *
    * @return   the root trie node of the current tire tree
    */
    public TrieNode GetRoot()
    {
        return root_;
    }
    /**
    * A test driver for this class.
    */
    public static void main(String[] args)
    {
        TrieTree rules = new TrieTree();
        rules.SetWildCard(true);
        rules.LoadRulesFromFile("../../data/rules/", "im.rul", false, false);
        System.out.println("----- result ---------");
        rules.PrintTrie(rules.GetRoot(), true, true, true);
        /**
        System.out.println("----- FindRule(inhale)");
        rules.FindRule("inhale").PrintNode();
        System.out.println("----- FindRule(ooth)");
        rules.FindRule("ooth").PrintNode();
        System.out.println("----- FindRule(stooth)");
        rules.FindRule("stooth").PrintNode();
        System.out.println("----- FindRule(sooth)");
        rules.FindRule("sooth").PrintNode();
        **/
    }
    // private method
    private String ReverseException(String inLine, boolean reverse)
    {
        String outLine = inLine;
        if(reverse == true)
        {
            RuleException exception = new RuleException(inLine);
            exception.Reverse();
            outLine = exception.GetExceptionStr();
        }
        return outLine;
    }
    private String ReverseRule(String inLine, boolean reverse)
    {
        String outLine = inLine;
        if(reverse == true)
        {
            InflectionRule rule = new InflectionRule(inLine);
            rule.Reverse();
            outLine = rule.GetRuleStr();
        }
        return outLine;
    }
    private boolean FindNode(TrieNode node, char[] charArray)
    {
        char curKey = node.GetKey();
        int curLevel = node.GetLevel();
        int arraySize = Array.getLength(charArray);
        int index = arraySize-1-curLevel;            // index in the char array
        // check if current key is the same as char in CharArray
        if(WildCard.IsMatchKey(curKey, index, charArray) == true)
        {
            // add match node if there are rules for this node
            if(node.GetRules() != null)    
            {
                foundNode_ = node;
                AddNodeToFoundList();     // Add foundNode_ into foundNodeList_
            }
            if(index == 0)     // reach the beginning of the inStr, stop travers
            {
                return true;
            }
            // travers the tree
            boolean oneChildMatch = false;
            if(node.GetChild() != null)
            {
                for(int i = 0; i < node.GetChild().size(); i++)
                {
                    TrieNode childNode = node.GetChild().elementAt(i);
                    if(FindNode(childNode, charArray) == true)
                    {
                        oneChildMatch = true;
                    }
                }
                if(oneChildMatch == true)  // update list if there is one match
                {
                    AddNodeToFoundList();
                }
            }
            else    // reach end the the tree branch
            {
                return true;
            }
        }
        else    // no matching for current node
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
    // read a rule in, Add the Node, then add rules 
    private void AddRule(String line, boolean duplicateFlag)
    {
        curRule_ = new InflectionRule(line);
        String inSuffix = curRule_.GetInSuffix();
        
        // traverse the Trie tree to check if this is a new node, 
        char curChar = ' ';
        curNode_ = root_;
        for(int i = inSuffix.length()-1-1; i >= 0; i--)    // skip last char '$'
        {
            curChar = inSuffix.charAt(i);
            // Add this TrieNode to TrieTree
            int level = inSuffix.length()-1-i;
            AddTrieNode(curChar, level);
        }
        // deposit the rule to the node
        if(curNode_.GetRules() == null)        // no rules exist in the node
        {
            Vector<InflectionRule> rules = new Vector<InflectionRule>();
            rules.addElement(curRule_);
            curNode_.SetRules(rules);
            ruleNum_++;
        }
        else    // rules exist in the node
        {
            // check if this rule exist: duplicate rules
            boolean duplicateRule = false;
            for(int i = 0; i < curNode_.GetRules().size(); i++)
            {
                InflectionRule tempRule = curNode_.GetRules().elementAt(i);
                if(curRule_.equals(tempRule))
                {
                    if(duplicateFlag == true)
                    {
                        System.out.println("* Warning: duplicate rule ("
                            + curRule_.GetRuleStr() + ")");
                    }
                    duplicateRule = true;
                    break;
                }
            }
            // add rule into rule list if it is not duplicated
            if(duplicateRule == false)
            {
                // insert rule by the order of: noun, verb, adj, adv
                int size = curNode_.GetRules().size(); 
                int insertIndex = size;        // insert index
                long inCategory = curRule_.GetInCategory();
                // find index for all category to insert
                for(int i = 0; i < curNode_.GetRules().size(); i++)
                {
                    InflectionRule tempRule = curNode_.GetRules().elementAt(i);
                    
                    if(GetDistance(inCategory, tempRule.GetInCategory()) > 0)
                    {
                        insertIndex = i;
                        break;
                    }
                }
                curNode_.GetRules().add(insertIndex, curRule_);
                ruleNum_++;
            }
        }
    }
    // use for sorting rule by noun, verb, adj, & adv
    private int GetDistance(long inCategory, long curCategory)
    {
        int in = ToPriority(inCategory);
        int cur = ToPriority(curCategory);
        int dis = cur-in;
        return dis;
    }
    // use for sorting rule by noun, verb, adj, & adv
    private int ToPriority(long category)
    {
        int priority = 0;
        if(category == Category.ToValue("noun"))
        {
            priority = 1;
        }
        else if(category == Category.ToValue("verb"))
        {
            priority = 2;
        }
        else if(category == Category.ToValue("adj"))
        {
            priority = 3;
        }
        else if(category == Category.ToValue("adv"))
        {
            priority = 4;
        }
        return priority;
    }
    private void AddTrieNode(char key, int level)
    {
        // check current node and it's child
        char parentKey = curNode_.GetKey();
        if(curNode_ != null) 
        {
            // init child
            if(curNode_.GetChild() == null)
            {
                curNode_.SetChild(new Vector<TrieNode>());
            }
            // check child list
            boolean found = false;
            Vector<TrieNode> childList = curNode_.GetChild();
            for(int i = 0; i < childList.size(); i++)
            {
                TrieNode curChild = childList.elementAt(i);
                if(key == curChild.GetKey())    // if this key existed
                {
                    curNode_ = curChild;
                    found = true;
                    break;
                }
            }
            if(found == false)             // new node (not exist), create it
            {
                TrieNode newNode = new TrieNode(key, level);
                InsertNode(curNode_.GetChild(), newNode);
                curNode_ = newNode;
                nodeNum_++;
            }
        }
    }
    // insert node to child list in accsent order
    private void InsertNode(Vector<TrieNode> child, TrieNode node)
    {
        // find index and insert node to there
        int index = FindIndex(child, node);
        child.insertElementAt(node, index);
    }
    private int FindIndex(Vector<TrieNode> child, TrieNode node)
    {
        int size = child.size();
        int index = size;
        char key = node.GetKey();
        for(int i = 0; i < size; i++)
        {
            TrieNode curNode = child.elementAt(i);
            if(curNode.GetKey() >= key)
            {
                index = i;
                break;
            }
        }
        return index;
    }
    private void PrintTrie(TrieNode node, boolean details, boolean rulesFlag,
        boolean exceptionFlag)
    {
        if(firstTime_ == true)
        {
            firstTime_ = false;
            System.out.println("======== Trie Tree Information =========");
            System.out.println("Number of nodes: " + nodeNum_);
            System.out.println("Number of rules: " + ruleNum_);
            System.out.println("Number of exceptions: " + exceptionNum_);
            System.out.println("----------------------------------------");
        }
        System.out.print("--");
        for(int i = 0; i < node.GetLevel(); i++)
        {
            System.out.print("---");
        }
        System.out.print(node.GetKey());
        
        // print detail information
        if(details == true)
        {
            int cSize = ((node.GetChild() == null)?0:node.GetChild().size());
            int rSize = ((node.GetRules() == null)?0:node.GetRules().size());
            System.out.print(" (child: " + cSize + "; rule: " + rSize + ")");
        }
        System.out.print(System.getProperty("line.separator").toString());
        // print rules
        if(rulesFlag == true)
        {
            Vector<InflectionRule> rules = node.GetRules();
            if(rules != null)
            {
                for(int i = 0; i < rules.size(); i++)
                {
                    InflectionRule rule = rules.elementAt(i);
                    System.out.print("  ");
                    for(int j = 0; j < node.GetLevel(); j++)
                    {
                        System.out.print("   ");
                    }
                    System.out.print("R-" + i + ": '" + rule.GetRuleStr()
                        + "'"); 
                    // print exception number
                    int eSize = ((rule.GetExceptions() == null)?0:
                        rule.GetExceptions().size());
                    System.out.println(" (Exceptions: " + eSize + ")");
                    // print out exception
                    if(exceptionFlag == true)
                    {
                        Hashtable<String, String> exceptions 
                            = rule.GetExceptions();
                        if(exceptions != null)
                        {
                            Enumeration<String> keyEn = exceptions.keys();
                            Enumeration<String> eleEn = exceptions.elements();
                            int k = 0;
                            while(keyEn.hasMoreElements() == true)
                            {
                                System.out.print("      ");
                                for(int l = 0; l < node.GetLevel(); l++)
                                {
                                    System.out.print("   ");
                                }
                                String key = keyEn.nextElement();
                                String ele = eleEn.nextElement();
                                System.out.println("E-" + k + ": '" + key + 
                                    "' => '" + ele + "'");
                                k++;
                            }
                        }
                    }
                }
            }
        }
        // recursively print all children
        if(node.GetChild() != null)
        {
            for(int i = 0; i < node.GetChild().size(); i++)
            {
                TrieNode childNode = node.GetChild().elementAt(i);
                PrintTrie(childNode, details, rulesFlag, exceptionFlag);
            }
        }
    }
    // data member
    private boolean wildCard_ = false;        // change wildCard letters
    private TrieNode root_ = null;            // the base nod eot the trie tree
    private TrieNode curNode_ = null;         // use for adding rule
    private TrieNode foundNode_ = null;       // use for finding rule
    private Vector<TrieNode> foundNodeList_ = null;     // use for finding rule
    private int nodeNum_ = 1;                // min it has root node
    private int ruleNum_ = 0;
    private int exceptionNum_ = 0;
    private boolean firstTime_ = true;
    private InflectionRule curRule_ = null;   // current rule read fr im.rul
    private static final String RULE = "RULE: ";
    private static final String EXCEPTION = "EXCEPTION: ";
    private static final String FILE = "FILE: ";
}
