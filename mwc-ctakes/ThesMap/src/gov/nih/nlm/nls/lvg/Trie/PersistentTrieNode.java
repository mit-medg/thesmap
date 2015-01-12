package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
import java.util.*;
/*****************************************************************************
* This class creates an object of persistent trie node.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/trietree.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class PersistentTrieNode extends PersistentTreeNode
{
    // public constructors
    /**
    * Create an object of persistent trie node, using an address in a random
    * access file.
    */
    public PersistentTrieNode(long address)
    {
        super(address);
    }
    /**
    * Create an object of persistent tree node, using key (a character from 
    * suffix) and the address of a rule.  In a LVG trie, each node is a 
    * character from the suffix.  This character is called key.
    */
    public PersistentTrieNode(char key, long ruleAddress)
    {
        super();
        key_ = key;
        ruleAddress_ = ruleAddress;
    }
    /**
    * Create an object of persistent trie node, using an address, next address,
    * parent address, child address in a random access file and level at the
    * persistent tree.
    */
    public PersistentTrieNode(int level, long parent, long next, long child,
        long address)
    {
        super(level, parent, next, child, address);
    }
    // public methods
    /**
    * Get the key of current trie node.
    *
    * @return   key of current trie node
    */
    public char GetKey()
    {
        return key_;
    }
    /**
    * Get the rule address of current trie node.
    *
    * @return   rule address of current trie node
    */
    public long GetRuleAddress()
    {
        return ruleAddress_;
    }
    /**
    * Write data to the random access file.
    *
    * @param  raf  the random access file that data will be written to
    *
    * @exception   IOException  if problems happen when accessing the random
    * access file.
    */
    public void WriteData(RandomAccessFile raf) throws IOException
    {
        raf.writeChar(key_);
        raf.writeLong(ruleAddress_);
    }
    /**
    * Read data from the random access file.
    *
    * @param  raf  the random access file that data will be read from
    *
    * @exception   IOException  if problems happen when accessing the random
    * access file.
    */
    public void ReadData(RandomAccessFile raf) throws IOException
    {
        key_ = raf.readChar();
        ruleAddress_ = raf.readLong();
    }
    /**
    * Get the current persistent trie node from a specified binary file at
    * a specified address.
    *
    * @param  raf  the binary file that the persistent node will be retrieved
    * from
    * @param  address  the address in the binary file that the persistent
    * node will be retrieved from
    *
    * @exception   IOException  if problems happen when accessing the random
    * access file.
    */
    public static PersistentTreeNode GetNode(RandomAccessFile raf, long address)
        throws IOException
    {
        PersistentTrieNode node = new PersistentTrieNode(address);
        raf.seek(address);
        int level = raf.readInt();
        long next = raf.readLong();
        long parent = raf.readLong();
        long child = raf.readLong();
        node.ReadData(raf);
        node.SetLevel(level);
        node.SetParent(parent);
        node.SetNext(next);
        node.SetChild(child);
        return node;
    }
    /**
    * Print all sub tree (list) for a persistent trie node at a specific address
    * of a specific binary file.  The print out information includes the key,
    * rules, and exceptions.
    *
    * @param  trieRaf  the binary file that the persistent trie node will be 
    * printed out
    * @param  address  the address in the binary file that the persistent
    * trie node will be printed out
    * @param  rulePath  the path/name of the LVG persistent rule
    * @param  exceptionPath  the path/name of the LVG persistent exception
    *
    * @exception   IOException  if problems happen when accessing the random
    * access file.
    */
    public static int PrintNode(RandomAccessFile trieRaf, long address,
        String rulePath, String exceptionPath) throws IOException
    {
        int nodeNum = 0;
        PersistentTrieNode trieNode = 
            (PersistentTrieNode) GetNode(trieRaf, address);
        // print the indents
        System.out.print("--");
        for(int i = 0; i < trieNode.GetLevel(); i++)
        {
            System.out.print("---");
        }
        long ruleAddress = trieNode.GetRuleAddress();
        System.out.print(" (" + trieNode.GetKey() + "@ " + address + 
            ", Rule @: " + ruleAddress + ")");
        System.out.print(System.getProperty("line.separator").toString());
        nodeNum++;
        // print rules
        if(ruleAddress != -1)
        {
            PersistentRuleNode.PrintList(rulePath, exceptionPath, ruleAddress);
        }
        // print child
        long childAddress = trieNode.GetChild();
        while(childAddress != -1)
        {
            PersistentTreeNode childNode = GetNode(trieRaf, childAddress);
            nodeNum += 
                PrintNode(trieRaf, childAddress, rulePath, exceptionPath);
            childAddress = childNode.GetNext();
        }
        return nodeNum;
    }
    // data member
    private char key_ = ' ';         // contents (a character) of a trie node
    private long ruleAddress_ = -1;  // address of the rule 
}
