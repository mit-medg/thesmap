package gov.nih.nlm.nls.lvg.Trie;
import java.io.*;
/*****************************************************************************
* This class creates an object of rule's exception for persistent data.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class PersistentExceptionNode extends PersistentListNode
{
    // public constructors
    /**
    * Create an object of rule's exception, using an address in the binary file.
    */
    public PersistentExceptionNode(long address)
    {
        super(address);
    }
    /**
    * Create an object of rule's exception, using an address in the binary file
    * and the address of the next exception.
    */
    public PersistentExceptionNode(long address, long next)
    {
        super(address, next);
    }
    /**
    * Create an object of rule's exception, using a key value pair. 
    * 
    * @param  key   the string for matching pattern
    * @param  value   the string for changing pattern
    */
    public PersistentExceptionNode(String key, String value)
    {
        super();
        key_ = key;
        value_ = value;
    }
    // public methods
    /**
    * Get the string of matching pattern of the rule's exception.
    * 
    * @return  the string of matching pattern of the rule's exception
    */
    public String GetKey()
    {
        return key_;
    }
    /**
    * Get the string of changing pattern of the rule's exception.
    * 
    * @return  the string of changing pattern of the rule's exception
    */
    public String GetValue()
    {
        return value_;
    }
    /**
    * Write the matching and changing string partterns to a specific binary
    * file.
    * 
    * @param  raf the object of random access file 
    * 
    * @exception  IOException if problems of writing data to random access file
    * happen.
    */
    public void WriteData(RandomAccessFile raf) throws IOException
    {
        raf.writeUTF(key_);
        raf.writeUTF(value_);
    }
    /**
    * Read the matching and changing string partterns from a specific binary
    * file.
    * 
    * @param  raf the object of random access file 
    * 
    * @exception  IOException if problems of reading data to random access file
    * happen.
    */
    public void ReadData(RandomAccessFile raf) throws IOException
    {
        key_ = raf.readUTF();
        value_ = raf.readUTF();
    }
    /**
    * Get data of a persistent list node at a specific addrss of a specific 
    * binary file.
    * 
    * @param  raf  the object of random access file 
    * @param  address  the address of the persistent list node to be retrieved
    * 
    * @exception  IOException if problems of accessing random access file
    * happen.
    */
    public static PersistentListNode GetNode(RandomAccessFile raf, long address)
        throws IOException
    {
        PersistentExceptionNode node = new PersistentExceptionNode(address);
        raf.seek(address);
        node.ReadData(raf);
        long next = raf.readLong();
        node.SetNext(next);
        return node;
    }
    /**
    * Print the detail information of rule's exceptions for a specific
    * file at a specific address.
    * 
    * @param  exceptionPath  the path/file name of the rule's exceptions to be
    * printed
    * @param  address  the address of the rule's exceptions in a random access 
    * file 
    * 
    * @exception  IOException if problems of accessing random access file
    * happen.
    */
    public static void PrintList(String exceptionPath, long address)
        throws IOException
    {
        PersistentList persistentException = new PersistentList(exceptionPath);
        RandomAccessFile exceptionRaf = persistentException.GetRaf();
        long curAddress = address + PersistentList.HEADER_OFFSET;
        exceptionRaf.seek(curAddress);
        int i = 0;
        while(curAddress != -1)
        {
            PersistentExceptionNode curNode = 
                (PersistentExceptionNode) GetNode(exceptionRaf, curAddress);
            System.out.println("        E-" + i + ": '" + curNode.GetKey() 
                + "|" + curNode.GetValue() + ";'");
            curAddress = curNode.GetNext();
            i++;
        }
        persistentException.Close();
    }
    // data members
    private String key_ = null;         // the string for match pattern
    private String value_ = null;         // the string for change pattern
}
