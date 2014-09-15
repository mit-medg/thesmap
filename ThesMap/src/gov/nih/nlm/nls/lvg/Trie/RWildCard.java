package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
/*****************************************************************************
* This class creates an object of wild cards for reverse trie.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/removeS/index.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
final public class RWildCard
{
    // constructors
    /**
    * private constructor to make sure no one use it (since all public methods
    * in this class are static)
    */
    public RWildCard()
    {
    }
    // public methods
    /**
    * Check if a specific key matches a character of a pattern.  The pattern is
    * represented as an array of character and is specified by index.
    *
    * @param  key   the key character to be checked for matching
    * @param  index   the index of character in the input array for matching
    * @param  inCharArray   a character array to represent a suffix
    *
    * @return   true or false to represent the specified key does or does not
    * match a specified character in the specified suffix
    */
    public static boolean IsMatchKey(char key, int index, char[] inCharArray)
    {
        boolean matchFlag = false;
        // check if key legal
        if((Character.isLowerCase(key) == false)
        && (IsWildCardKey(key) == false))
        {
            return false;
        }
        // check if it is the beginning of a word
        if(index < 0)
        {
            if(key == BEGIN)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        // check if inChar match key
        char inChar = Character.toLowerCase(inCharArray[index]);
        String inStr = String.valueOf(inChar);
        if(key == inChar)        // not wild card match
        {
            matchFlag = true;
        }
        else if((key == CHARACTER)    // any character
        && (inStr != null))
        {
            matchFlag = true;
        }
        else if((key == DIGIT)
        && (Character.isDigit(inChar)))
        {
            matchFlag = true;
        }
        else if((key == LETTER)
        && (Character.isLetter(inChar)))
        {
            matchFlag = true;
        }
        else if((key == SPACE)
        && (inChar == ' '))
        {
            matchFlag = true;
        }
        else if((key == PUNCTUATION)
        && (punctuationSet_.contains(inStr) == true))
        {
            matchFlag = true;
        }
        return matchFlag;
    }
    private static boolean IsWildCardKey(char inChar)
    {
        String inStr = String.valueOf(inChar);
        return wildCardSet_.contains(inStr);
    }
    // data member
    /** a character symbol for represetning the beginning of a string */
    public final static char BEGIN = '^';          // START
    /** a character symbol for represetning the end of a string */
    public final static char END   = '$';          // END
    /** a character symbol for representing any character */
    public final static char CHARACTER = 'C';      // any character
    /** a character symbol for representing a digit [0-9] */
    public final static char DIGIT = 'D';          // any digit 0-9
    /** a character symbol for representing a letter [a-z], [A-Z] */
    public final static char LETTER = 'L';         // any letter a-z, A-Z
    /** a character symbol for representing a character " " */
    public final static char SPACE = 'S';          // any space
    /** a character symbol for representing punctuations: - (, , */
    public final static char PUNCTUATION = 'P';    // any punctuation: -, (, ,
    private static HashSet<String> wildCardSet_ = new HashSet<String>();
    private static HashSet<String> punctuationSet_ = new HashSet<String>();
    static
    {
        wildCardSet_.add(new Character(BEGIN).toString());
        wildCardSet_.add(new Character(END).toString());
        wildCardSet_.add(new Character(CHARACTER).toString());
        wildCardSet_.add(new Character(DIGIT).toString());
        wildCardSet_.add(new Character(LETTER).toString());
        wildCardSet_.add(new Character(SPACE).toString());
        wildCardSet_.add(new Character(PUNCTUATION).toString());
        punctuationSet_.add("-");
        punctuationSet_.add("(");
        punctuationSet_.add(",");
    }
}
