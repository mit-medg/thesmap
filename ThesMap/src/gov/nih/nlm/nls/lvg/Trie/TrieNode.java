package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
/*****************************************************************************
* This class creates object of a trie node.
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
public class TrieNode
{
    // public constructors
    /**
    * Create an object of trie node (default). 
    */
    public TrieNode()
    {
    }
    /**
    * Create an object of trie node, using key and level for the node.
    */
    public TrieNode(char key, int level)
    {
        key_ = key;
        level_ = level;
    }
    /**
    * Create an object of trie node, using key, level, rules and child of the 
    * node.
    */
    public TrieNode(char key, int level, Vector<InflectionRule> rule, 
        Vector<TrieNode> child)
    {
        key_ = key;
        level_ = level;
        rules_ = rule;
        child_ = child;
    }
    // public methods
    /**
    * Set the child of the current node.  The child is Vector<TrieNode>.
    *
    * @param  child   the child of the current node
    */
    public void SetChild(Vector<TrieNode> child)
    {
        child_ = child;
    }
    /**
    * Set rules of the current node.  The rules is Vector<InflectionRule>.
    *
    * @param  rules  rules of the current node
    */
    public void SetRules(Vector<InflectionRule> rules)
    {
        rules_ = rules;
    }
    /**
    * Get rules of the current node.  The rules is Vector<InflectionRule>.
    *
    * @return  rules of the current node
    */
    public Vector<InflectionRule> GetRules()
    {
        return rules_;
    }
    /**
    * Get the child of the current node.  The child is Vector<TrieNode>.
    *
    * @return  the child of the current node
    */
    public Vector<TrieNode> GetChild()
    {
        return child_;
    }
    /**
    * Get the key of the current node.  The key is a character form the 
    * matching suffix.  Each node in a trie tree is represented by a character
    * from the suffix.
    *
    * @return  the key character of the current node
    */
    public char GetKey()
    {
        return key_;
    }
    /**
    * Get the level of the current node.  The level of root is 0.
    *
    * @return  the level of the current node
    */
    public int GetLevel()
    {
        return level_;
    }
    /**
    * Print out the detail information of a trie node.  This information 
    * includes key, level, rules, and child.
    */
    public void PrintNode()
    {
        System.out.println("--------- Node -------------");
        System.out.println("key: " + key_);
        System.out.println("level: " + level_);
        System.out.println("rules: " + ((rules_ == null)?0:rules_.size()));
        System.out.println("child: " + ((child_ == null)?0:child_.size()));
    }
    /**
    * A test driver for this class.
    */
    public static void main(String[] args)
    {
        TrieNode trieNode = new TrieNode('L', 1, null, null);
        trieNode.PrintNode();
    }
    
    // data member
    private char key_ = WildCard.END;
    private int level_ = -1;
    private Vector<InflectionRule> rules_ = null;  // rules of this node
    private Vector<TrieNode> child_ = null;        // children of this node
}
