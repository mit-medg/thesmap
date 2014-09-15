package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class provides a mechanism of persistent tree.  One file is capable of 
* storing multiple trie trees.  However, we use one only tree in one file 
* in our application for simplicity reason.
*
* <p>The file format is:
* <ul>
* <li>0~3: int: level
* <li>4~11: long: address for next node
* <li>12~19: long: address for parent node
* <li>20~27: long: address for child list
* <li>28~x: specific data (length varies)
* </ul>
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/persistent.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class PersistentTree
{
    // public constructors
    /**
    * Create an object of persistent tree, using a string for the path of
    * the binary file to be strored.
    */
    public PersistentTree(String path)
    {
        try
        {
            raf_ = new RandomAccessFile(path, "rw");
        }
        catch (Exception e)
        {
        }
    }
    // public methods
    /**
    * Get the random access file of the current persistent tree.
    *
    * @return  the random access file of the current persistent tree
    */
    public RandomAccessFile GetRaf()
    {
        return raf_;
    }
    /**
    * Get the address of the root node of the current persistent tree.
    *
    * @return  the address of the root node of the current persistent tree
    */
    public long GetRootAddress()
    {
        return rootAddress_;
    }
    /**
    * Add a persistent node to the current persistent tree at a specific 
    * address.
    *
    * @param   node  the persistent node to be added
    * @param   parentAddress  the address that the persistent node to be added
    * at
    *
    * @exception  IOException  if probelms happens when accessing random access
    * file
    */
    public void Add(PersistentTreeNode node, long parentAddress) 
        throws IOException
    {
        long address = raf_.length();
        // write address to parent node's child
        int parentLevel = -1;
        if(parentAddress != -1)            
        {
            raf_.seek(parentAddress + CHILD_OFFSET);
            long parentChild = raf_.readLong();
            if(parentChild == -1)         // parent has no child, update it
            {
                raf_.seek(parentAddress + CHILD_OFFSET);// update parent's child
                raf_.writeLong(address);
            }
            else    // parent has child, update previous's next 
            {
                long target = parentChild;
                while(true)
                {
                    raf_.seek(target + NEXT_OFFSET);
                    long next = raf_.readLong();
                    if(next == -1)
                    {
                        raf_.seek(target + NEXT_OFFSET);
                        raf_.writeLong(address);    // update previous' next
                        break;
                    }
                    target = next;
                }
            }
            raf_.seek(parentAddress);
            parentLevel = raf_.readInt();
        }
        else    // at the beginning of a new tree
        {
            rootAddress_ = address;
        }
        node.SetAddress(address);
        node.SetLevel(parentLevel +1);
        // write to file
        raf_.seek(address);
        raf_.writeInt(node.GetLevel());
        raf_.writeLong(node.GetNext());
        raf_.writeLong(node.GetParent());
        raf_.writeLong(node.GetChild());
        node.WriteData(raf_); 
    }
    /**
    * Close the random access file of current persistent tree.
    *
    * @exception  IOException  if probelms happens when closing random access
    * file
    */
    public void Close() throws IOException
    {
        raf_.close();
    }
    // data member
    private RandomAccessFile raf_ = null;
    private long rootAddress_ = -1;
    private final static int NEXT_OFFSET   = 4;     // address offset for next
    private final static int CHILD_OFFSET  = 20;    // address offset for child
}
