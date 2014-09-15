package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This Tag class extends BitMaskBase class and performs as a tagger in LexItem.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Tag extends BitMaskBase
{
    // public constructors
    /**
    *   Creates a default tag object
    */
    public Tag()
    {
        super(ALL_BIT_VALUE, bitStr_);
    }
    /**
    *   Creates a default tag object, using a long value
    */
    public Tag(long value)
    {
        super(value, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Convert a combined value string to a long tag value.
    *
    * @param   valueStr  combined name in String format
    *
    * @return  a long value of the specified name
    */
    public static long ToValue(String valueStr)
    {
        return ToValue(valueStr, bitStr_);
    }
    /**
    * Convert a long tag value to a combined string (abbreviation).
    *
    * @param   value  number for finding it's combined name
    *
    * @return  name in a String format
    */
    public static String ToName(long value)
    {
        return ToName(value, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Get the name (first in the name list) of a specified bit (single).
    * The first one in LVG is the abbreviation name.
    *
    * @param   bitValue  bit nubmer for finding it's name
    *
    * @return  name of the bit specified
    */
    public static String GetBitName(int bitValue)
    {
        return GetBitName(bitValue, 0);
    }
    /**
    * Get the name at index order of a specified bit (single).
    *
    * @param   bitValue  bit nubmer for finding it's name
    * @param   index   the order index of the name in bitStr_[]
    *
    * @return  name at index order of the bit specified
    */
    public static String GetBitName(int bitValue, int index)
    {
        return GetBitName(bitValue, index, bitStr_);
    }
    /**
    * Get the long value for one single name (no combine names of bits).
    *
    * @param   valueStr  name of a bit for finding it's long value
    *
    * @return  a long value of the specified name
    *
    */
    public static long Enumerate(String valueStr)
    {
        return Enumerate(valueStr, bitStr_);
    }
    // public methods
    /** 
    * Test driver for this class 
    */
    public static void main(String[] args)
    {
        // static method
        System.out.println("------ static methods ----------");
        System.out.println(" - ToValue(derNounAdj): " + 
            Tag.ToValue("derNounAdj")); 
        System.out.println(" - ToValue(uniqueAcrExp): " +
            Tag.ToValue("uniqueAcrExp")); 
        System.out.println(" - ToValue(uniqueAcrExp+derNounAdj): " + 
            Tag.ToValue("uniqueAcrExp+derNounAdj")); 
        System.out.println("---------");
        System.out.println(" - ToName(0): " + Tag.ToName(0)); 
        System.out.println(" - ToName(1): " + Tag.ToName(1)); 
        System.out.println(" - ToName(2): " + Tag.ToName(2)); 
        System.out.println(" - ToName(3): " + Tag.ToName(3)); 
        System.out.println(" - ToName(4): " + Tag.ToName(4)); 
    }
    // data members
    /** bit for pure noun/adj operationin recursive derivation */
    public final static int DERV_NOUN_ADJ_BIT         = 0;
    /** bit for unique acronyms and expansions */
    public final static int UNIQUE_ACR_EXP_BIT       = 1;
    public final static long NO_BIT_VALUE = 0;   // value for no bits
    /** all bits for tag */
    public final static long ALL_BIT_VALUE = 3;   // value for all bits
    private static ArrayList<Vector<String>> bitStr_ 
        = new ArrayList<Vector<String>>(MAX_BIT);  // Must have
    /** Initiate static data member, bitStr_ **/
    static
    {
        for(int i = 0; i < MAX_BIT; i++)
        {
            bitStr_.add(i, new Vector<String>());
        }
        // define all bit string
        bitStr_.get(DERV_NOUN_ADJ_BIT).addElement("derNounAdj");
        bitStr_.get(UNIQUE_ACR_EXP_BIT).addElement("uniqueAcrExp");
    }
}
