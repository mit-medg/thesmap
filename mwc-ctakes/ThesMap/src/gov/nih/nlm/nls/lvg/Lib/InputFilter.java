package gov.nih.nlm.nls.lvg.Lib;
import java.io.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides methods of Lvg input filter functions.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class InputFilter
{
    // private constructor, so that no one can instance it
    private InputFilter()
    {
    }
    // public methods
    /**
    * Get input term from the input line at a given field
    *
    * @param  inLine  a String value of the input line
    * @param  separator  a String value of field separator
    * @param  fieldNum  the field number of the input term
    *
    * @return  a String of retireved input term
    */
    public static String GetInputTerm(String inLine, String separator,
        int fieldNum)
    {
        String out = In.GetField(inLine, separator, fieldNum);
        return out;
    }
    /**
    * Get input category from the input line at a given field
    *
    * @param  inLine  a String value of the input line
    * @param  separator  a String value of field separator
    * @param  fieldNum  the field number of the input category
    *
    * @return  category in a long value
    */
    public static long GetInputCategory(String inLine, String separator,
        int fieldNum)
    {
        long out = Category.ALL_BIT_VALUE;
        if(fieldNum != -1)
        {
            try
            {
                out = Long.parseLong(In.GetField(inLine, separator, fieldNum));
            }
            catch (Exception e) 
            { 
                System.out.println("** Warning in GetInputCategory( ): " 
                    + e.toString());
            }
        }
        return out;
    }
    /**
    * Get input inflection from the input line at a given field
    *
    * @param  inLine  a String value of the input line
    * @param  separator  a String value of field separator
    * @param  fieldNum  the field number of the input inflection
    *
    * @return  inflection in a long value
    */
    public static long GetInputInflection(String inLine, String separator,
        int fieldNum)
    {
        long out = Inflection.ALL_BIT_VALUE;
        if(fieldNum != -1)
        {
            try
            {
                out = Long.parseLong(In.GetField(inLine, separator, fieldNum));
            }
            catch (Exception e) 
            { 
                System.out.println("** Warning in GetInputInflection( ): " 
                    + e.toString());
            }
        }
        return out;
    }
    /**
    * Check if the current inflection and category are legal and do not
    * need to be filter out
    *
    * @param  legalCat  a long value for legal category
    * @param  legalInfl  a long value for legal inflection
    * @param  actualCat  a long value for actual category
    * @param  actualInfl  a long value for actual inflection
    *
    * @return  a boolean flag of showing legal inflection & category
    */
    public static boolean IsLegal(long legalCat, long legalInfl, 
        long actualCat, long actualInfl)
    {
        if((IsLegal(legalCat, actualCat) == true)
        && (IsLegal(legalInfl, actualInfl) == true))
        {
            return true;
        }
        return false;
    }
    /**
    * Check if the current category or inflection is legal
    *
    * @param  legal  a long value for legal value
    * @param  actual  a long value for actual value
    *
    * @return  a boolean flag of showing legal category or entity
    */
    public static boolean IsLegal(long legal, long actual)
    {
        return (Bit.Contain(legal, actual));
    }
}
