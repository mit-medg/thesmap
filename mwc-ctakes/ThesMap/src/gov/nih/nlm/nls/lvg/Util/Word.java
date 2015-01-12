package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
import java.text.*;
/*****************************************************************************
* This class contains methods for words related operations.  A word does not
* contain spaces or tabs.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
***************************************************************************/
public class Word
{
    // public methods
    /**
    * Check if the given word is a catelog number.  Three formats are considered
    * as a catelog number: 
    * <ul>
    * <li> nnn.nnn.nnn
    * <li> nnn/nnn/nnn
    * <li> nnn-nnn-nnn
    * </ul>
    *
    * @param   inWord  The word to be checked
    *
    * @return  true or false if the input word is or is not a catelog number.
    */
    public static boolean IsCatelogNumber(String inWord)
    {
        boolean isCatelog = true;
        // tokenlize /.-
        StringTokenizer buf = new StringTokenizer(inWord, catelogSeparators_);
        while(buf.hasMoreTokens() == true)
        {
            String temp = buf.nextToken();
            if(IsDigitWord(temp) == false)
            {
                isCatelog = false;
                break;
            }
        }
        return isCatelog;
    }
    /**
    * Check if the given word is a date.  Three formats are considered
    * as a date: 
    * <ul>
    * <li> day.month.year
    * <li> day/month/year
    * <li> day-month-year
    * </ul>
    *
    * @param   inWord  The word to be checked
    *
    * @return  true or false if the input word is or is not a date.
    */
    public static boolean IsDate(String inWord)
    {
        boolean isDate = IsDate(inWord, "d/M/yy") 
            || IsDate(inWord, "d.M.yy") || IsDate(inWord, "d-M-yy");
        return isDate;
    }
    /**
    * Check if the given word is a float.
    *
    * @param   inWord  The word to be checked
    *
    * @return  true or false if the input word is or is not a float.
    */
    public static boolean IsFloat(String inWord)
    {
        boolean isFloat = false;
        try
        {
            Float.parseFloat(inWord);
            isFloat = true;
        }
        catch (NumberFormatException e)
        {
            isFloat = false;
        }
        return isFloat;
    }
    /**
    * Check if the given word contains punctuations.
    *
    * @param   inWord  The word to be checked
    *
    * @return  true or false if the input word has or has not a punctuation(s).
    */
    public static boolean HasPunctuation(String inWord)
    {
        boolean hasPunctuation = false;
        for(int i = 0; i < inWord.length(); i++)
        {
            if(Char.IsPunctuation(inWord.charAt(i)) == true)
            {
                hasPunctuation = true;
                break;
            }
        }
        return hasPunctuation;
    }
    /** 
    * Test driver for this class
    */
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("** Usage: java Word <inStr>");
        }
        else
        {
            String inStr = args[0];
            System.out.println("-- inStr: " + inStr);
            System.out.println("-- HasPunctuation: " + HasPunctuation(inStr));
            System.out.println("-- IsCatelog: " + IsCatelogNumber(inStr));
            System.out.println("-- IsFloat: " + IsFloat(inStr));
            System.out.println("-- IsDate: " + IsDate(inStr));
        }
    }
    // private methods
    private static boolean IsDate(String inWord, String pattern)
    {
        boolean isDate = false;
        try
        {
            SimpleDateFormat foo = new SimpleDateFormat(pattern);
            foo.parse(inWord);
            isDate = true;
        }
        catch (ParseException e)
        {
            isDate = false;
        }
        return isDate;
    }
    // check if all characters in this word are digits
    private static boolean IsDigitWord(String inWord)
    {
        boolean isDigitWord = true;
        for(int i = 0; i < inWord.length(); i++)
        {
            char temp = inWord.charAt(i);
            if(Character.isDigit(temp) == false)
            {
                isDigitWord = false;
                break;
            }
        }
        return isDigitWord;
    }
    // data member
    final private static String catelogSeparators_ = ".-/";
}
