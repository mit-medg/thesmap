package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class creates an object of persistent list node.  It is used as the base 
* class for persistent list node.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class PersistentListNode
{
    // public constructors
    /**
    * Create an object of persistent list node (defualt).
    */
    public PersistentListNode()
    {
    }
    /**
    * Create an object of persistent list node, using an address in a random
    * access file.
    */
    public PersistentListNode(long address)
    {
        address_ = address;
    }
    /**
    * Create an object of persistent list node, using an address and next 
    * address in a random access file.
    */
    public PersistentListNode(long address, long next)
    {
        next_ = next;
        address_ = address;
    }
    // public methods
    /**
    * Set the address of the next node for the current persistent node.
    * 
    * @param   next  the address of the next node for the current persistent 
    * node
    */
    public void SetNext(long next)
    {
        next_ = next;
    }
    /**
    * Get the address of the next node for the current persistent node.
    * 
    * @return   the address of the next node for the current persistent node
    */
    public long GetNext()
    {
        return next_;
    }
    /**
    * Set the address of the current persistent node.
    * 
    * @param   address  the address of the current persistent node
    */
    public void SetAddress(long address)
    {
        address_ = address;
    }
    /**
    * Get the address of the current persistent node.
    * 
    * @return   address  the address of the current persistent node
    */
    public long GetAddress()
    {
        return address_;
    }
    /**
    * Write data to the random access file.  This methods is needed for building
    * the software.  This method is used in it's extended class.
    * 
    * @param  raf  the random access file that data will be written to
    * 
    * @exception   IOException  if problems happen when accessing the random 
    * access file.
    */
    public void WriteData(RandomAccessFile raf) throws IOException
    {
    }
    /**
    * Read data from the random access file.  This methods is needed for 
    * building the software.  This method is used in it's extended class.
    * 
    * @param  raf  the random access file that data will be read from
    * 
    * @exception   IOException  if problems happen when accessing the random 
    * access file.
    */
    public void ReadData(RandomAccessFile raf) throws IOException
    {
    }
    /**
    * Get the current persistent list node from a specified binary file at
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
    public static PersistentListNode GetNode(RandomAccessFile raf, long address)
        throws IOException
    {
        raf.seek(address);
        PersistentListNode node = new PersistentListNode(address);
        node.ReadData(raf);
        long next = raf.readLong();
        node.SetNext(next);
        return node;
    }
    /**
    * Print all sub tree (list) for a persistent node at a specific address
    * of a specific binary file.
    * 
    * @param  raf  the binary file that the persistent node will be printed 
    * out
    * @param  address  the address in the binary file that the persistent
    * node will be printed out
    * 
    * @exception   IOException  if problems happen when accessing the random 
    * access file.
    */
    public static void PrintList(RandomAccessFile raf, long address) 
        throws IOException
    {
        raf.seek(address);
        int nodeNum = raf.readInt();
        long curAddress = address + PersistentList.HEADER_OFFSET;
        System.out.println("--- List contains " + nodeNum + " nodes, start@"
            + address);
        int i = 0;
        while(curAddress != -1)
        {
            PersistentListNode curNode = GetNode(raf, curAddress);
            System.out.println(i + ": '" + curNode.GetAddress() + "')");
            curAddress = curNode.GetNext();
            i++;
        }
    }
    // data member
    private char key_ = ' ';            // key: for matching pattern
    private long address_ = -1;            
    private long next_ = -1;
}
