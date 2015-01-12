package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
/*****************************************************************************
* This class creates an object of a reverse trie tree.  A reverse trie trie 
* includes a tree structure consists of trie nodes.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/removeS/index.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public class RTrieTree
{
    // public constructors
    /**
    * Create an object of trie tree (default).
    */
    public RTrieTree()
    {
    }
    /**
    * Create an object of trie tree by specifying pattern rule file anme.
    */
    public RTrieTree(String fName)
    {
        this.LoadRTrieTreeFromFile(fName);
    }
    /**
    * Read in removeS trie rules from a specified file.
    *
    * @param   fName   the file name of the removeS pattern trie rule file
    *
    */
    public void LoadRTrieTreeFromFile(String fName) 
    {
        // check the file name
        if(fName == null)
        {
            return;
        }
        try
        {
            String line = null;
            BufferedReader in = new BufferedReader(new FileReader(fName));
            // read in line by line from a file
            while((line = in.readLine()) != null)
            {
                // skip the line if it is empty or comments (#)
                if((line.length() > 0) && (line.charAt(0) != '#'))
                {
                    AddNode(line);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("**Error: problem of opening/reading file '"
                + fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
    }
    /**
    * Find if there is a matched pattern in the term to the reverse trie tree.
    *
    * @param  term   a string is used to find nodes with matching pattern
    *
    * @return  true if match pattern, false if no matching pattern
    */
    public boolean FindPattern(String term)
    {
        String curStr = term + '$';
        char[] inCharArray = curStr.toCharArray();
        return FindNode(root_, inCharArray);
    }
    /**
    * Print the detail informatin of reverse trie tree
    *
    * @param  node  The top node fo the trie tree to be printed 
    */
    public void PrintRTrieTree(RTrieNode node)
    {
        if(firstTime_ == true)
        {
            firstTime_ = false;
            System.out.println("======== RTrie Tree Information =========");
            System.out.println("Number of nodes: " + nodeNum_);
            System.out.println("----------------------------------------");
        }
        System.out.print("--");
        for(int i = 0; i < node.GetLevel(); i++)
        {
            System.out.print("---");
        }
        System.out.print("level: " + node.GetLevel());
        System.out.println(", key: " + node.GetKey());
        // recursively print all children
        if(node.GetChild() != null)
        {
            for(int i = 0; i < node.GetChild().size(); i++)
            {
                RTrieNode childNode = node.GetChild().elementAt(i);
                PrintRTrieTree(childNode);
            }
        }
    }
    /**
    * Get the detail information (rule) of found pattern
    *
    * @return  the detail infromation, rule of found pattern
    */
    public String GetFoundRule()
    {
        return foundRule_ + RWildCard.END;
    }
    private boolean FindNode(RTrieNode node, char[] charArray)
    {
        char curKey = node.GetKey();
        int curLevel =  node.GetLevel();
        int arraySize = Array.getLength(charArray);
        int index = arraySize-1-curLevel;           // index in the char array
        // check if current key is the same as char in CharArray
        if(RWildCard.IsMatchKey(curKey, index, charArray) == true)
        {
            // if it reach the begining of the rule
            if(curKey == RWildCard.BEGIN)
            {
                return true;
            }
            // traverse the tree
            if(node.GetChild() != null)
            {
                for(int i = 0; i < node.GetChild().size(); i++)
                {
                    RTrieNode childNode = node.GetChild().elementAt(i);
                    if(FindNode(childNode, charArray) == true)
                    {
                        foundRule_ += childNode.GetKey() + "-";
                        return true;
                    }
                }
            }
            else    // reach the end of the tree branch
            {
                return true;
            }
        }
        //no matching for current node
        return false;
    }
    private void AddNode(String line)
    {
        // traverse the Trie tree to check if this is a new node
        char curChar = ' ';
        curNode_ = root_;
        // traverse the rule
        for(int i = line.length()-1-1; i >= 0; i--)        // skip last char '$'
        {
            curChar = line.charAt(i);
            // Add this node to tree
            int level = line.length()-1-i;
            // check current node and it's child
            char parentKey = curNode_.GetKey();
            if(curNode_ != null)
            {
                // init child Vector
                if(curNode_.GetChild() == null)
                {
                    curNode_.SetChild(new Vector<RTrieNode>());
                }
                // check child list
                boolean found = false;
                Vector<RTrieNode> childList = curNode_.GetChild();
                char key = curChar;
                for(int j = 0; j < childList.size(); j++)
                {
                    RTrieNode curChild = childList.elementAt(j);
                    if(key == curChild.GetKey())
                    {
                        curNode_ = curChild;
                        found = true;
                        break;
                    }
                }
                if(found == false)      // new node (not exist), create it
                {
                    RTrieNode newNode = new RTrieNode(key, level);
                    InsertNode(curNode_.GetChild(), newNode);
                    curNode_ = newNode;
                    nodeNum_++;
                }
            }
        }
    }
    private void InsertNode(Vector<RTrieNode> child, RTrieNode node)
    {
        // find index and insert node to there
        int index = FindIndex(child, node);
        child.insertElementAt(node, index);
    }
    private int FindIndex(Vector<RTrieNode> child, RTrieNode node)
    {
        int size = child.size();
        int index = size;
        char key = node.GetKey();
        for(int i = 0; i < size; i++)
        {
            RTrieNode curNode = child.elementAt(i);
            if(curNode.GetKey() >= key)
            {
                index = i;
                break;
            }
        }
        return index;
    }
    private RTrieNode GetRoot()
    {
        return root_;
    }
    /**
    * A test driver for this class.
    */
    public static void main(String[] args)
    {
        RTrieTree rules = new RTrieTree();
        rules.LoadRTrieTreeFromFile("../../data/rules/removeS.data");
        System.out.println("----- result ---------");
        rules.PrintRTrieTree(rules.GetRoot());
        /**
        System.out.println(rules.FindPattern(args[0]));
        System.out.println("Rules: [" + rules.GetFoundRule() + "]");
        **/
    }
    // data member
    private RTrieNode root_ = new RTrieNode(RWildCard.END, 0);
    private int nodeNum_ = 1;               // min it has root node
    private RTrieNode curNode_ = null;      // use for adding node to tree
    private RTrieNode foundNode_ = null;    // use for traverse tree
    private String foundRule_ = new String();
    private boolean firstTime_ = true;
}
