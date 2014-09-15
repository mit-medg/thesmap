package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class provides a mechanism for persistent linked list.  One persistent 
* file is capable of storing multiple linked lists.  However, the store 
* procedure (adding nodes) need to be done at one time consequencially for 
* one linked list.
*
* <p>The file format is:
* <br> header + node1 + node2 + ...
* <br> where header is:
*   <ul>
*   <li>0~3 : int : numbers of nodes
*   <li>4~11: long: next address (address will be used for next node).
*   </ul>
* <br> node:
*   <ul>
*   <li>0~x: specific data (length varies)
*   <li>x+1~x+8: long: next
*   </ul>
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
public class PersistentList
{
    // public constructors
    /**
    * Create an object of persistent list, using a string for the path of
    * the binary file to be strored.
    */
    public PersistentList(String path)
    {
        try
        {
            raf_ = new RandomAccessFile(path, "rw");
        }
        catch (Exception e)
        {
        }
    }
    /**
    * Add a persistent node to the current persistent list.
    * 
    * @param   node  the persistent node to be added
    *
    * @exception  IOException  if probelms happens when accessing random access
    * file
    */
    public void Add(PersistentListNode node) throws IOException
    {
        // beginning of the list
        if(beginAddress_ == -1)
        {
            WriteListHeader(raf_.length());
        }
        // write cur node to file
        raf_.seek(lastAddress_);
        node.WriteData(raf_);
        raf_.writeLong(node.GetNext());
        node.SetAddress(lastAddress_);
        // update previous's next 
        if(nodeNum_ > 0)
        {
            raf_.seek(lastAddress_ - NEXT_OFFSET);
            raf_.writeLong(lastAddress_);
        }
        // update header
        nodeNum_++;
        lastAddress_ = raf_.length();
        UpdateListHeader();
    }
    /**
    * Get the beginning address of current persistent list.
    * 
    * @return the beginning address of current persistent list
    */
    public long GetAddress()
    {
        return beginAddress_;
    }
    /**
    * Get the random access file of the current persistent list.
    * 
    * @return  the random access file of the current persistent list
    */
    public RandomAccessFile GetRaf()
    {
        return raf_;
    }
    /**
    * Close the random access file of current persistent list.
    * 
    * @exception  IOException  if probelms happens when closing random access
    * file
    */
    public void Close() throws IOException
    {
        raf_.close();
    }
    /**
    * A test driver for this class.
    */
    public static void main(String[] args)
    {
        try
        {
            PersistentRuleNode.PrintList("rule.data", "exception.data", 6089);
        }
        catch (Exception e)
        {
        }
    }    
    // private methods
    private void UpdateListHeader() throws IOException
    {
        raf_.seek(beginAddress_);
        raf_.writeInt(nodeNum_);
        raf_.writeLong(lastAddress_);
    }
    private void WriteListHeader(long address) throws IOException
    {
        // update data member;
        nodeNum_ = 0;
        beginAddress_ = address;
        lastAddress_ = beginAddress_ + HEADER_OFFSET;
        // format: numberOfElement (int), adrress of last node (long)
        raf_.seek(address);
        raf_.writeInt(nodeNum_);                // 4: 0 ~ 3
        raf_.writeLong(lastAddress_);            // 12: 4 ~ 11
    }
    // data member
    /** the size of header used in binary file */
    public static final int HEADER_OFFSET = 12;        // 4 + 8
    private static final int NEXT_OFFSET = 8;        //  8
    private RandomAccessFile raf_ = null;
    private int nodeNum_ = 0;
    private long beginAddress_ = -1;    // address for list (header)
    private long lastAddress_ = 0;        // last addess
}
