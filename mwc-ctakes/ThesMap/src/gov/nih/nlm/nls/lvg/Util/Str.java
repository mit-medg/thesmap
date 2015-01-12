package gov.nih.nlm.nls.lvg.Util;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
/*****************************************************************************
* This class provides methods of string related operations.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Str
{
    // public methods
    /**
    * Replace a source subString with a target string in an input string.
    *
    * @param   inStr  the input base string which will be changed.
    * @param   source  the source pattern string which will be changed from.
    * @param   target  the target pattern string which will be changed to.
    *
    * @return  a string with changed pattern string from inStr.
    */
    public static String Replace(String inStr, String source, String target)
    {
        int lastIndex = 0;
        int sourceSize = source.length();
        int targetSize = target.length();
        String out = inStr;
        // do the change
        int curIndex = inStr.indexOf(source);
        while(curIndex > -1)
        {
            String before = inStr.substring(0, curIndex);
            String after = 
                inStr.substring(curIndex+sourceSize, inStr.length());
            out = before + target + after;
            inStr = out;
            lastIndex = curIndex + targetSize;
            curIndex = inStr.indexOf(source, lastIndex);
        }
        return out;
    }
    /**
    * Concatenate the specified character to the end of a string.
    * Return a blank string if the original string is null.
    *
    * @param   orgStr  the original string to be cancatenated to.
    * @param   catChar  the character to be concatened
    *
    * @return  a string that represents the concatenation of the original 
    *          string followed by a cancatenated character.  Return a blank 
    *          string if the original string is null. 
    */
    public static String SmartCat(String orgStr, char catChar)
    {
        String outStr = orgStr;
        // if the original string is null, return a new string
        if(outStr == null)
        {
            outStr = new String();
        }
        // otherwise, cancatenate it
        outStr += catChar;
        return outStr;
    }
    /**
    * Concatenate the specified character to the beginning of a string.
    * Return a blank string if the original string is null.
    *
    * @param   orgStr  the original string to be cancatenated to.
    * @param   catChar  the character to be concatened
    *
    * @return  a string that represents the concatenation of a specified 
    *          character and followed by the original string.  Return a blank 
    *          string if the original string is null. 
    */
    public static String SmartAddToHead(String orgStr, char catChar)
    {
        String outStr = orgStr;
        // if the original string is null, return a new string
        if(outStr == null)
        {
            outStr = new String();
        }
        outStr = (catChar + outStr);
        return outStr;
    }
    /**
    * Concatenate the specified string to the end of a string.
    * Return a blank string if the original string is null.
    *
    * @param   orgStr  the original string to be cancatenated to.
    * @param   catStr  the string to be concatened
    *
    * @return  a string that represents the concatenation of the original 
    *          string followed by a cancatenated string.  Return a blank 
    *          string if the original string is null. 
    */
    public static String SmartCat(String orgStr, String catStr)
    {
        String outStr = orgStr;
        // if the original string is null, return a new string
        if(outStr == null)
        {
            outStr = new String();
        }
        outStr += catStr;
        return outStr;
    }
    /**
    * A test driver for this class
    */
    public static void main(String[] args)
    {
        if(args.length != 3)
        {
            System.out.println("** Usage: java Str <inStr> <source> <target>");
        }
        else
        {
            String inStr = args[0];
            String source = args[1];
            String target = args[2];
            System.out.println("-- inStr: '" + inStr + "'");
            System.out.println("-- source: '" + source + "'");
            System.out.println("-- target: '" + target + "'");
            System.out.println("-- Replace: '" + Replace(inStr, source, target)
                + "'");
        }
    }
}
