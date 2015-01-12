package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class converts characters from a term into Unicode names. 
* The default format of the unicode name is ![unicode name]!  
*
* <p> Users may have their own format by defining starting tag and ending tag.
* These tags file is configurable by modifying the configuration file.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/getUnicodeNames.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/getUnicodeName.html"> 
* Get Unicode Names</a>
*
* @version    V-2013
****************************************************************************/
public class ToGetUnicodeNames extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   startTag   the starting tag for symbol name (default: ![ ) 
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    */
    public static Vector<LexItem> Mutate(LexItem in, String startTag, 
        String endTag, boolean detailsFlag, boolean mutateFlag)
    {
        Vector<LexItem> out = GetUnicodeNames(in, startTag, endTag, 
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Convert a symbol to it's ![unicode name]!
    *
    * @param   inChar   input character for getting symbol 
    * @param   startTag   the starting tag for symbol name (default: ![ ) 
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * 
    * @return  unicode name of a character in ![unicode name]! format
    */
    public static String GetUnicodeName(char inChar, String startTag, 
        String endTag) 
    {
        String outStr = startTag + UCharacter.getName(inChar) + endTag;
        return outStr;
    }
    /**
    * Convert a symbol to it's ![unicode name]!
    *
    * @param   inStr   input string for getting symbol 
    * @param   startTag   the starting tag for symbol name (default: ![ ) 
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * 
    * @return  unicode name of a character in ![unicode name]! format
    */
    public static String GetUnicodeName(String inStr, String startTag, 
        String endTag) 
    {
        StringBuffer buf = new StringBuffer();
        // go through all chracters in the inStr
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            // if ASCII, no change
            if(UnicodeUtil.IsAsciiChar(curChar) == true)
            {
                buf.append(curChar);
            }
            else    // if no-ASCII, use ![unicode name]!
            {
                buf.append(GetUnicodeName(curChar, startTag, endTag));
            }
        }
        return buf.toString();
    }
    /**
    * Convert a ![unicode name]! to a character
    *
    * @param   unicodeName    character in ![unicode name]! format
    * @param   startTag   the starting tag for symbol name (default: ![ ) 
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * 
    * @return  the character to the unicode name
    */
    public static char GetCharFromUnicodeName(String unicodeName, 
        String startTag, String endTag)
    {
        if(unicodeName == null)
        {
            return (char) -1;
        }
        // remove start and end tags: ![ and ]!
        if(IsLegalUnicodeNameFormat(unicodeName, startTag, endTag) == true)
        {
            unicodeName 
                = GetUnicodeNameWithoutTag(unicodeName, startTag, endTag);
            char out = (char) UCharacter.getCharFromExtendedName(unicodeName);
            return out;
        }
        return (char) -1;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        String testStr = "© and µ";
        Configuration conf = new Configuration("data.config.lvg", true);
        String startTag = conf.GetConfiguration(Configuration.START_TAG);
        String endTag = conf.GetConfiguration(Configuration.END_TAG);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToGetUnicodeNames.Mutate(in, startTag, endTag, 
            true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    /**
    * Convert non-ASCII char to ![unicode name]!
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   startTag   the starting tag for symbol name (default: ![ ) 
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    */
    private static Vector<LexItem> GetUnicodeNames(LexItem in, String startTag,
            String endTag, boolean detailsFlag, boolean mutateFlag)
    {
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = INFO;
        }
        if(mutateFlag == true)
        {
            mutate = new String();
        }
        // mutate the term: ![unicode name]!
        String inStr = in.GetSourceTerm();
        String fs = GlobalBehavior.GetFieldSeparator();
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            // if ASCII, no change
            if(UnicodeUtil.IsAsciiChar(curChar) == true)
            {
                buf.append(curChar);
            }
            else    // if no-ASCII, use ![unicode name]!
            {
                buf.append(GetUnicodeName(curChar, startTag, endTag));
            }
            // update mutate information
            if(mutateFlag == true)
            {
                mutate += UnicodeUtil.GetUnicodeInfoXNB(curChar) + fs;
            }
        }
        String term = buf.toString();
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.GET_UNICODE_NAME, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    private static boolean IsLegalUnicodeNameFormat(String inStr,
        String startTag, String endTag)
    {
        if((inStr.length() < (startTag.length() + endTag.length() + 1))
        || (inStr.startsWith(startTag) == false)
        || (inStr.endsWith(endTag) == false))
        {
            return false;
        }
        return true;
    }
    private static String GetUnicodeNameWithoutTag(String inStr,
        String startTag, String endTag)
    {
        String outStr = inStr.substring(startTag.length(),
            inStr.length()-endTag.length());
        return outStr;
    }
    
    // data members
    private static final String INFO = "Get Unicode Names";
}
