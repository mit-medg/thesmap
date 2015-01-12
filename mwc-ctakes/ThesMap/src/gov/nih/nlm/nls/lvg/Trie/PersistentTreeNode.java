package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class creates an object of persistent tree node.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class PersistentTreeNode
{
    // public constructors
    /**
    * Create an object of persistent tree node (defualt).
    */
    public PersistentTreeNode()
    {
    }
    /**
    * Create an object of persistent tree node, using an address in a random
    * access file.
    */
    public PersistentTreeNode(long address)
    {
        address_ = address;
    }
    /**
    * Create an object of persistent tree node, using an address, next address,
    * parent address, child address in a random access file and level at the 
    * persistent tree.
    */
    public PersistentTreeNode(int level, long parent, long next, long child, 
        long address)
    {
        level_ = level;
        parent_ = parent;
        next_ = next;
        child_ = child;
        address_ = address;
    }
    // public methods
    /**
    * Get the level of current persistent tree node at a tree.
    *
    * @return   level of current persistent tree node at a tree
    */
    public int GetLevel()
    {
        return level_;
    }
    /**
    * Set the level of current persistent tree node at a tree.
    *
    * @param   level of current persistent tree node at a tree
    */
    public void SetLevel(int level)
    {
        level_ = level;
    }
    /**
    * Get the parent's address of current persistent tree node at a tree.
    *
    * @return   parent's address of current persistent tree node at a tree
    */
    public long GetParent()
    {
        return parent_;
    }
    /**
    * Set the parent's address of current persistent tree node at a tree.
    *
    * @param   parent parent's address of current persistent tree node at a tree
    */
    public void SetParent(long parent)
    {
        parent_ = parent;
    }
    /**
    * Get the next address of current persistent tree node at a tree.
    *
    * @return   next address of current persistent tree node at a tree
    */
    public long GetNext()
    {
        return next_;
    }
    /**
    * Set the next address of current persistent tree node at a tree.
    *
    * @param   next address of current persistent tree node at a tree
    */
    public void SetNext(long next)
    {
        next_ = next;
    }
    /**
    * Get the child's address of current persistent tree node at a tree.
    *
    * @return   child's address of current persistent tree node at a tree
    */
    public long GetChild()
    {
        return child_;
    }
    /**
    * Set the child's address of current persistent tree node at a tree.
    *
    * @param   child child's address of current persistent tree node at a tree
    */
    public void SetChild(long child)
    {
        child_ = child;
    }
    /**
    * Set the address of current persistent tree node at a tree.
    *
    * @param  address of current persistent tree node at a tree
    */
    public void SetAddress(long address)
    {
        address_ = address;
    }
    /**
    * Get the address of current persistent tree node at a tree.
    *
    * @return   address of current persistent tree node at a tree
    */
    public long GetAddress()
    {
        return address_;
    }
    /**
    * Write data to the random access file.  This methods is needed for building
    * the software.
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
    * building the software.
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
    * Get the current persistent tree node from a specified binary file at
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
        PersistentTreeNode node = new PersistentTreeNode(address);
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
    * Print all sub tree (list) for a persistent tree node at a specific address
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
    public static void PrintNode(RandomAccessFile raf, long address) 
        throws IOException
    {
        PersistentTreeNode curNode = GetNode(raf, address);
        System.out.print("--");
        for(int i = 0; i < curNode.GetLevel(); i++)
        {
            System.out.print("---");
        }
        System.out.print(" (" + curNode.GetAddress() + ")");
        System.out.print(System.getProperty("line.separator").toString());
        // print child
        long childAddress = curNode.GetChild();
        while(childAddress != -1)
        {
            //while(childAddress != -1)
            {
                PersistentTreeNode childNode = GetNode(raf, childAddress);
                PrintNode(raf, childAddress);
                childAddress = childNode.GetNext();
            }
        }
    }
    // data members
    private int level_ = -1;         // level for current node (root is 0) 
    private long address_ = -1;      // address for current node
    private long parent_ = -1;       // address for node's parents
    private long child_ = -1;        // address for node's child
    private long next_ = -1;         // address for next node 
}
