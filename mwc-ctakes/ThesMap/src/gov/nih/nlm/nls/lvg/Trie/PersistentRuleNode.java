package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class creates an object of the rule node for persistent data.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class PersistentRuleNode extends PersistentListNode
{
    // public constractors
    /**
    * Create an object of persistent rule node, using an address in a random
    * access file.
    */
    public PersistentRuleNode(long address)
    {
        super(address);
    }
    /**
    * Create an object of persistent rule node, using an address and next
    * address in a random access file.
    */
    public PersistentRuleNode(long address, long next)
    {
        super(address, next);
    }
    /**
    * Create an object of persistent rule node, using an string representation
    * of a specific rule and the address of a specific exception object.
    */
    public PersistentRuleNode(String ruleStr, long exceptionAddress)
    {
        super();
        ruleStr_ = ruleStr;
        exceptionAddress_ = exceptionAddress;
    }
    // public methods
    /**
    * Get the string representation of the current rule.
    * 
    * @return  the string representation of the current rule
    */
    public String GetRuleString()
    {
        return ruleStr_;
    }
    /**
    * Get the address of the exception for the current rule.
    * 
    * @return  the address of the exception for the current rule
    */
    public long GetExceptionAddress()
    {
        return exceptionAddress_;
    }
    /**
    * Write the string representation data of current rule node to a specified
    * random access file.
    *
    * @param  raf  the random access file that data will be written to
    *
    * @exception   IOException  if problems happen when writing data to the 
    * random access file.
    */
    public void WriteData(RandomAccessFile raf) throws IOException
    {
        raf.writeUTF(ruleStr_);
        raf.writeLong(exceptionAddress_);
    }
    /**
    * Read the string representation data from a specified random access file
    * and assigned this data to the current rule node. 
    *
    * @param  raf  the random access file that data will be read from
    *
    * @exception   IOException  if problems happen when reading data from the 
    * random access file.
    */
    public void ReadData(RandomAccessFile raf) throws IOException
    {
        ruleStr_ = raf.readUTF();
        exceptionAddress_ = raf.readLong();
    }
    /**
    * Get the current persistent rule node from a specified binary file at
    * a specified address.
    *
    * @param  raf  the binary file that the persistent rule node will be 
    * retrieved from
    * @param  address  the address in the binary file that the persistent
    * rule node will be retrieved from
    *
    * @exception   IOException  if problems happen when accessing the random
    * access file.
    */
    public static PersistentListNode GetNode(RandomAccessFile raf, long address)
        throws IOException
    {
        PersistentRuleNode node = new PersistentRuleNode(address);
        raf.seek(address);
        node.ReadData(raf);
        long next = raf.readLong();
        node.SetNext(next);
        return node;
    }
    /**
    * Print all sub tree (list) for a persistent nule node at a specific address
    * of specific binary files of rules and exceptions.
    *
    * @param  rulePath  the path/name of the persistent rules.
    * @param  exceptionPath  the path/name of the persistent exceptions.
    * @param  address  the address in the binary file that the persistent
    * rule node will be printed out
    *
    * @exception   IOException  if problems happen when accessing files of 
    * rules and exceptions.
    */
    public static void PrintList(String rulePath, String exceptionPath, 
        long address) throws IOException
    {
        PersistentList persistentRule = new PersistentList(rulePath);
        RandomAccessFile ruleRaf = persistentRule.GetRaf();
        //ruleRaf.seek(address);
        //int nodeNum = ruleRaf.readInt();
        long curAddress = address + PersistentList.HEADER_OFFSET;
        ruleRaf.seek(curAddress);
        int i = 0;
        while(curAddress != -1)
        {
            PersistentRuleNode curNode = 
                (PersistentRuleNode) GetNode(ruleRaf, curAddress);
            long exceptionAddress = curNode.GetExceptionAddress();
            System.out.println("    R-" + i + ": '" + curNode.GetRuleString() + 
                "': " + exceptionAddress);
            curAddress = curNode.GetNext();
            i++;
            // print exceptions
            if(exceptionAddress != -1)
            {
                PersistentExceptionNode.PrintList(exceptionPath, 
                    exceptionAddress);
            }
        }
        persistentRule.Close();
    }
    // data member
    private String ruleStr_ = null;         // the original string for the rule
    private long exceptionAddress_ = -1;    // the address for exception
}
