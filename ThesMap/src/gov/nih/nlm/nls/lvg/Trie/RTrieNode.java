package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
/*****************************************************************************
* This class creates object of a reverse trie node.
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
public class RTrieNode
{
    // public constructors
    /**
    * Create an object of reversed trie node (default).
    */
    public RTrieNode()
    {
    }
    /**
    * Create an object of reversed trie node (default).
    */
    public RTrieNode(char key, int level)
    {
        key_ = key;
        level_ = level;
    }
    /**
    * Create an object of reversed trie node (default).
    */
    public RTrieNode(char key, int level, Vector<RTrieNode> child)
    {
        key_ = key;
        level_ = level;
        child_ = child;
    }
    // public methods
    /**
    * Set the child of the current node.  The child is Vector<RTrieNode> 
    *
    * @param  child   the child of the current node
    */
    public void SetChild(Vector<RTrieNode> child)
    {
        child_ = child;
    }
    /**
    * Get the child of the current node.  The child is Vector<RTrieNode> 
    *
    * @return  the child of the current node
    */
    public Vector<RTrieNode> GetChild()
    {
        return child_;
    }
    /**
    * Get the key of the current node.  The key is a character form the
    * matching pattern.  Each node in a reverse trie tree is represented by a 
    * character from the pattern.
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
    * Print out the detail information of a reverse  trie node.  This 
    * information includes key, level, and child.
    */
    public void PrintNode()
    {
        System.out.println("--------- Node -------------");
        System.out.println("key: " + key_);
        System.out.println("level: " + level_);
        System.out.println("child: " + ((child_ == null)?0:child_.size()));
    }
    // data member
    private char key_ = RWildCard.END;
    private int level_ = -1;
    private Vector<RTrieNode> child_ = null;       // children of this node
}
