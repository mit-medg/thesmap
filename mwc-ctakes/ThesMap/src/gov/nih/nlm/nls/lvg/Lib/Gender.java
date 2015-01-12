package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This Gender class extends BitMaskBase class and performs as LVG gender.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
*
* @see <a href="../../../../../../../designDoc/UDF/flow/design.html#GENDER">
*      Design document </a>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Gender extends BitMaskBase
{
    // public constructors
    /**
    *   Creates a default gender object
    */
    public Gender()
    {
        super(ALL_BIT_VALUE, bitStr_);
    }
    /**
    *   Creates a default gender object, using a integer value
    */
    public Gender(long value)
    {
        super(value, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Convert a combined value string to a long gender value.
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
    * Convert a long gender value to a combined string (abbreviation).
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
        System.out.println(" - ToValue(female+F+male+girl): " + 
            Gender.ToValue("female+F+male+girl")); 
        System.out.println(" - ToValue(female+neuter): " +
            Gender.ToValue("female+neuter")); 
        System.out.println(" - ToValue(Man+girl): " + 
            Gender.ToValue("Man+girl")); 
        System.out.println("---------");
        System.out.println(" - ToName(4): " + Gender.ToName(4)); 
        System.out.println(" - ToName(6): " + Gender.ToName(6)); 
        System.out.println(" - ToName(32): " + Gender.ToName(32));
        System.out.println("---------");
        System.out.println(" - GetBitValue(Gender.MALE_BIT): " 
            + Gender.GetBitValue(Gender.MALE_BIT)); 
        System.out.println(" - GetBitValue(Gender.FEMALE_BIT): " 
            + Gender.GetBitValue(Gender.FEMALE_BIT)); 
        System.out.println(" - GetBitValue(Gender.NEUTER_BIT): " 
            + Gender.GetBitValue(Gender.NEUTER_BIT)); 
        System.out.println(" - GetBitValue(Gender.NOT_KNOWABLE_BIT): " 
            + Gender.GetBitValue(Gender.NOT_KNOWABLE_BIT)); 
        System.out.println("---------");
        System.out.println(" - GetBitIndex(4): " + Gender.GetBitIndex(4));
        System.out.println(" - GetBitIndex(6): " + Gender.GetBitIndex(6));
        System.out.println("---------");
        System.out.println(" - Contains(6, 4): " + Gender.Contains(6, 4));
        System.out.println(" - Contains(6, 1): " + Gender.Contains(6, 1));
        System.out.println("---------");
        System.out.println(" - Enumerate(m): " + Gender.Enumerate("m")); 
        System.out.println(" - Enumerate(female): " 
            + Gender.Enumerate("female")); 
        System.out.println(" - Enumerate(f): " + Gender.Enumerate("f")); 
        System.out.println(" - Enumerate(F): " + Gender.Enumerate("F")); 
        System.out.println("---------");
        System.out.println(" - GetBitName(1): " + Gender.GetBitName(1)); 
        System.out.println(" - GetBitName(1, 0): " + Gender.GetBitName(1,0)); 
        System.out.println(" - GetBitName(1, 1): " + Gender.GetBitName(1,1)); 
        System.out.println(" - GetBitName(1, 2): " + Gender.GetBitName(1,2)); 
        System.out.println(" - GetBitName(4): " + Gender.GetBitName(4)); 
        System.out.println(" - GetBitName(MAX_BIT): " + Gender.GetBitName(63)); 
        System.out.println(" - GetBitName(100): " + Gender.GetBitName(100)); 
        System.out.println(GlobalBehavior.LS_STR +
            "------ object methods ----------");
        Gender g = new Gender();
        System.out.println("---------");
        g.SetValue(6);
        System.out.println(" - g.SetValue(6)"); 
        System.out.println(" - g.GetBitFlag(MALE_BIT): " 
            + g.GetBitFlag(MALE_BIT)); 
        System.out.println(" - g.GetBitFlag(FEMALE_BIT): " 
            + g.GetBitFlag(FEMALE_BIT)); 
        System.out.println(" - g.GetBitFlag(NEUTER_BIT): " 
            + g.GetBitFlag(NEUTER_BIT)); 
        System.out.println(" - g.GetBitFlag(NOT_KNOWABLE_BIT): " 
            + g.GetBitFlag(NOT_KNOWABLE_BIT)); 
        System.out.println("---------");
        System.out.println(" - g.GetValue(): " + g.GetValue()); 
        g.SetBitFlag(MALE_BIT, true); 
        System.out.println(" - g.SetBitFlag(" + Gender.MALE_BIT + ", true);"); 
        System.out.println(" - g.GetValue(): " + g.GetValue()); 
        g.SetBitFlag(MALE_BIT, false); 
        System.out.println(" - g.SetBitFlag(" + Gender.MALE_BIT + ", false);");
        System.out.println(" - g.GetValue(): " + g.GetValue()); 
        System.out.println("---------");
        System.out.println(" - g.SetBitFlag(" + Gender.FEMALE_BIT + ", false);");
        g.SetBitFlag(FEMALE_BIT, false); 
        System.out.println(" - g.GetValue(): " + g.GetValue()); 
        System.out.println(" - g.GetName(): " + g.GetName()); 
        System.out.println(" - g.SetBitFlag(" + Gender.FEMALE_BIT + ", true);");
        g.SetBitFlag(FEMALE_BIT, true); 
        System.out.println(" - g.GetValue(): " + g.GetValue()); 
        System.out.println(" - g.GetName(): " + g.GetName()); 
        System.out.println("---------");
        System.out.println(" - g.Contains(2): " + g.Contains(2)); 
        System.out.println(" - g.Contains(1): " + g.Contains(1)); 
    }
    // data members
    /** male bit for gender */
    public final static int MALE_BIT         = 0;
    /** female bit for gender */
    public final static int FEMALE_BIT       = 1;
    /** neuter bit for gender */
    public final static int NEUTER_BIT       = 2;
    /** unknown gender bit for gender */
    public final static int NOT_KNOWABLE_BIT = 3;
    /** all bits for gender */
    public final static long ALL_BIT_VALUE = 15;   // value for all bits
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
        bitStr_.get(MALE_BIT).addElement("male");
        bitStr_.get(MALE_BIT).addElement("m");
        bitStr_.get(FEMALE_BIT).addElement("female");
        bitStr_.get(FEMALE_BIT).addElement("f");
        bitStr_.get(NEUTER_BIT).addElement("neuter");
        bitStr_.get(NOT_KNOWABLE_BIT).addElement("notKnowable");
        bitStr_.get(NOT_KNOWABLE_BIT).addElement("_");
    }
}
