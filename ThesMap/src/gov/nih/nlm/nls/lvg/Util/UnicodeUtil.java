package gov.nih.nlm.nls.lvg.Util;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
/*****************************************************************************
* This class represents Unicode related methods.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class UnicodeUtil
{
    // public methods
    /**
    * Check if a char is an ASCII char ( < 128).
    *
    * @param   inChar  input chararter to check if it is ASCII
    *
    * @return  true or false if the input character is or is not ASCII
    */
    public static boolean IsAsciiChar(char inChar)
    {
        return ((inChar > 127)?false:true);
    }
    /**
    * Check if a char is an ASCII String.
    *
    * @param   inStr  input string to check if it is ASCII
    *
    * @return  true or false if the input string is or is not ASCII
    */
    public static boolean IsAsciiStr(String inStr)
    {
        if(inStr == null)
        {
            return false;
        }
        // go through all characters in the input string
        for(int i = 0; i < inStr.length(); i++)
        {
            if(IsAsciiChar(inStr.charAt(i)) == false)
            {
                return false;
            }
        }
        return true;
    }
    /**
    * Convert a char to a decimal number (int)
    *
    * @param   inChar  input char to be converted to an int
    *
    * @return  unicode value in decimal (int)
    */
    public static int CharToNum(char inChar)
    {
        return (new Character(inChar).hashCode());
    }
    /**
    * Convert a char to a string
    *
    * @param   inChar  input char to be converted to a string
    *
    * @return  unicode in string
    */
    public static String CharToStr(char inChar)
    {
        return (String.valueOf(inChar));
    }
    /**
    * Convert an unicode in decimal to unicdoe char
    *
    * @param   inNum  input unicode value in decimal to be converted to 
    *          a unicode char
    *
    * @return  unicode character
    */
    public static char NumToChar(int inNum)
    {
        return ((char) inNum);
    }
    /**
    * Convert an unicode in decimal to an unicdoe string
    *
    * @param   inNum  input unicode value in decimal to be converted to 
    *          a unicode string
    *
    * @return  unicode string
    */
    public static String NumToStr(int inNum)
    {
        return (CharToStr(NumToChar(inNum)));
    }
    /**
    * Get Unicode name of an Unicode value in decimal
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode name in string
    */
    public static String GetUnicodeName(int inNum)
    {
        return UCharacter.getName(inNum);
    }
    /**
    * Get Unicode name of a character
    *
    * @param   inChar  input unicode character
    *
    * @return  unicode name in string
    */
    public static String GetUnicodeName(char inChar)
    {
        return UCharacter.getName(CharToNum(inChar));
    }
    /**
    * Get Unicode block of an Unicode value in decimal
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode block in string
    */
    public static String GetUnicodeBlock(int inNum)
    {
        return UCharacter.UnicodeBlock.of(inNum).toString();
    }
    /**
    * Get Unicode block of a character
    *
    * @param   inChar  input unicode character 
    *
    * @return  unicode name in string
    */
    public static String GetUnicodeBlock(char inChar)
    {
        return UCharacter.UnicodeBlock.of(CharToNum(inChar)).toString();
    }
    /**
    * Get Unicode category of an Unicode value in decimal
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode category in string
    */
    public static String GetUnicodeCategory(int inNum)
    {
        return UCharacterCategory.toString(UCharacter.getType(inNum));
    }
    /**
    * Get Unicode category of a character
    *
    * @param   inChar  input unicode character 
    *
    * @return  unicode category in string
    */
    public static String GetUnicodeCategory(char inChar)
    {
        return UCharacterCategory.toString(UCharacter.getType(
            CharToNum(inChar)));
    }
    /**
    * Convert a decimal number to Unicode in HTML Hex format
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode in HTML Hex format
    */
    public static String NumToHtmlHex(int inNum)
    {
        String heading = new String();
        if(inNum == 32)
        {
            return "&nbsp;";    // space
        }
        else if(inNum < 16)
        {
            heading = "&#X000";
        }
        else if(inNum < 256)
        {
             heading = "&#X00";
        }
        else if(inNum < 4096)
        {
            heading = "&#X0";
        }
        else
        {
            heading = "&#X";
        }
        StringBuffer htmlNum = new StringBuffer();
        htmlNum.append(heading);
        htmlNum.append(Integer.toHexString(inNum));
        htmlNum.append(";");
        return htmlNum.toString();
    }
    /**
    * Convert a decimal number to Unicode Hex format
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode in Unicode Hex format
    */
    public static String NumToUnicodeHex(int inNum)
    {
        String heading = new String();
        if(inNum < 16)
        {
            heading = "U+000";
        }
        else if(inNum < 256)
        {
            heading = "U+00";
        }
        else if(inNum < 4096)
        {
            heading = "U+0";
        }
        else
        {
            heading = "U+";
        }
        StringBuffer unicodeHex = new StringBuffer();
        unicodeHex.append(heading);
        unicodeHex.append(Integer.toHexString(inNum).toUpperCase());
        return unicodeHex.toString();
    }
    /**
    * Convert a Unicode in Hex format to decimal format
    *
    * @param   inUnicodeHex  input unicode value in Hex format 
    *
    * @return  unicode value in decimal
    */
    public static int UnicodeHexToNum(String inUnicodeHex)
    {
        String valueStr = inUnicodeHex.substring(2);    // remvoe U+
        return (Integer.valueOf(valueStr, 16)).intValue();
    }
    /**
    * Convert a Unicode in Hex format to unicode string
    *
    * @param   inUnicodeHex  input unicode value in Hex format 
    *
    * @return  unicode string
    */
    public static String UnicodeHexToStr(String inUnicodeHex)
    {
        String outStr = new String();
        if(inUnicodeHex.startsWith("U+") == true)
        {
            int inNum = UnicodeHexToNum(inUnicodeHex);
            outStr = NumToStr(inNum);
        }
        return outStr;
    }
    /**
    * Get XNB information of an unicode character.
    * XNB: Unicode Hex|Unicode name|Unicode Block
    *
    * @param   inNum  input unicode value in decimal 
    *
    * @return  unicode in Unicode Hex format
    */
    public static String GetUnicodeInfoXNB(int inNum)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(NumToUnicodeHex(inNum));
        buf.append(", ");
        buf.append(GetUnicodeName(inNum));
        buf.append(", ");
        buf.append(GetUnicodeBlock(inNum));
        return buf.toString();
    }
    /**
    * Get XNB information of an unicode character.
    * XNB: Unicode Hex|Unicode name|Unicode Block
    *
    * @param   inChar  input unicode character 
    *
    * @return  unicode in Unicode Hex format
    */
    public static String GetUnicodeInfoXNB(char inChar)
    {
        return GetUnicodeInfoXNB(CharToNum(inChar));
    }
}
