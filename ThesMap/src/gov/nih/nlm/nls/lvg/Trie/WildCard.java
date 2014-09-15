package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import java.lang.reflect.*;
/*****************************************************************************
* This class creates an object of wild cards for trie.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/wildcard.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
final public class WildCard
{
    // constructors
    /**
    * private constructor to make sure no one use it (since all public methods
    * in this class are static)
    */
    private WildCard()
    {
    }
    // public methods
    /**
    * Get a string with modified suffix from a specified string by giving
    * the in suffix and out suffix of a rule to be applied.
    * 
    * @param   inSuffix   the matching suffix of input term
    * @param   outSuffix   the generated output suffix
    * @param   inStr   the string will be modified it's suffix
    * 
    * @return   a string with modified suffix  
    */
    public static String GetSuffix(String inSuffix, String outSuffix,
        String inStr)
    {
        int size = inStr.length(); 
        int inSize = inSuffix.length();
        String endStr = inStr.substring(size-inSize);    // suffix of inStr
        StringBuffer suffixStr = new StringBuffer();
        // go through out all character of out suffix
        for(int i = 0; i < outSuffix.length(); i++)
        {
            char curChar = outSuffix.charAt(i);
            if((IsLegalWildCard(curChar) == true)
            && (curChar != WildCard.END))
            {
                if(i >= inSuffix.length()-1)    // outsuffix is longer
                {
                    // append char from the last of original suffix, 'S'
                    suffixStr.append(endStr.charAt(endStr.length()-2));
                }
                else  // append char from original suffix
                {
                    suffixStr.append(endStr.charAt(i));
                }
            }
            else    // append char form out suffix if not a wild card
            {
                suffixStr.append(curChar);
            }
        }
        return suffixStr.toString();
    }
    /**
    * Check if a specific key matches a character of a suffix.  The suffix is
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
        // check illegal key
        if((IsWildCard(key) == true)
        && (IsLegalWildCard(key) == false))
        {
            return false;
        }
        // check if they are matched
        char curChar = inCharArray[index];
        String curStr = String.valueOf(curChar);
        if(key == inCharArray[index])    // not wild card
        {
            matchFlag = true;
        }
        else if((key == VOWEL)
        && (vowelSet_.contains(curStr) == true))
        {
            matchFlag = true;
        }
        else if((key == CONSONANT)
        && (consonantSet_.contains(curStr) == true))
        {
            matchFlag = true;
        }
        else if((key == SAME_AS_PREV)
        && (index < Array.getLength(inCharArray)-1)
        && (curChar == inCharArray[index+1]))
        {
            matchFlag = true;
        }
        else if((key == DIGIT) && (Character.isDigit(curChar)))
        {
            matchFlag = true;
        }
        else if((key == LETTER) && (Character.isLetter(curChar))
        && (Character.isLowerCase(curChar)))
        {
            matchFlag = true;
        }
        else if((key == END) && (index == Array.getLength(inCharArray)-1))
        {
            matchFlag = true;
        }
        else if((key == BEGIN) && (index == -1))    // "^" is not included
        {
            matchFlag = true;
        }
        return matchFlag;
    }
    /**
    * Transform a specific character to a wildCard
    *
    * @param  inChar   the character to be transform to a wild card
    *
    * @return   a wild card, which is transformed from the specific character
    */
    public static char WildCardTransform(char inChar)
    {
        String inStr = String.valueOf(inChar);
        char outChar = inChar;
        // Transform Vowel to V
        if(vowelWildCardSet_.contains(inStr) == true)
        {
            outChar = VOWEL;
        }
        else if(consonantWildCardSet_.contains(inStr) == true)
        {
            outChar = CONSONANT;
        }
        // Transform Consonant to C
        return outChar;
    }
    /**
    * Transform a specific string to a string consist of wildcard.
    * This function does not need to be used in LVG trie rule operations since
    * all rules are transformed into wildcard in the input file.
    *
    * @param  inStr   the string to be transformed to wild cards
    *
    * @return   a string consist of wild cards, transformed from the specific
    * string
    */
    public static String WildCardTransform(String inStr)
    {
        StringBuffer curStr = new StringBuffer(inStr);
        char lastChar = ' ';
        // go through each character of curStr, backward
        for(int i = curStr.length()-1; i >= 0; i--)
        {
            char curChar = curStr.charAt(i);
            if(IsWildCard(curChar) == true)
            {
                if(curChar == lastChar)        // set the 'S' wildcard
                {
                    curStr.setCharAt(i, SAME_AS_PREV);
                }
                else        // set all other wild card
                {
                    curStr.setCharAt(i, WildCardTransform(curChar));
                }
            }
            lastChar = inStr.charAt(i);
        }
        return curStr.toString();
    }
    /**
    * Check if a specific character belong ot a pre-defined wild card.
    *
    * @param  inChar   the character to be test for a wild card
    *
    * @return   true or false to represent the specified character is or is not
    * a wild card
    */
    public static boolean IsWildCard(char inChar)
    {
        // a wildcard must be an upper case letter (not including digit)
        boolean isWildCard = 
            Character.isLetter(inChar) && !Character.isLowerCase(inChar);
        return isWildCard;
    }
    /**
    * test driver for this class.
    */
    public static void main(String[] args)
    {
        String str = "CEXXer$|adj|comparative|CEX$|adj|positive";
        System.out.println("in:  '" + str + "'");
        System.out.println("out: '" + WildCard.WildCardTransform(str) + "'");
        System.out.println("-----------------------");
        //str = "CUAW|CUUW|CUAC|CDDer";
        str = "|CUAB|BDDer|ARRRRT|arrrrt";
        System.out.println("in:  '" + str + "'");
        System.out.println("out: '" + WildCard.WildCardTransform(str) + "'");
        System.out.println("-----------------------");
        System.out.println("IsLegalWildCard('A'): " + 
            WildCard.IsLegalWildCard('A'));
        System.out.println("IsLegalWildCard('S'): " + 
            WildCard.IsLegalWildCard('S'));
    }
    // private method
    private static boolean IsLegalWildCard(char inChar)
    {
        String inStr = String.valueOf(inChar);
        return wildCardSet_.contains(inStr);
    }
    // data member
    /** a character symbol for representing a vowel: [a,e,i,o,u] */
    public final static char VOWEL = 'V';
    /** a character symbol for representing a consonant */
    public final static char CONSONANT = 'C';
    /** a character symbol for representing a same character as the following
    character in the string */
    public final static char SAME_AS_PREV = 'S';    // same char as prievious 
    /** a character symbol for representing a digit [0-9] */
    public final static char DIGIT     = 'D';        // any digit 0-9
    /** a character symbol for representing a letter [a-z] */
    public final static char LETTER    = 'L';        // any letter a-z
    /** a character symbol for represetning the beginning of a string */
    public final static char BEGIN     = '^';        // START
    /** a character symbol for represetning the end of a string */
    public final static char END       = '$';        // END
    /** a character symbol for representing a field separator */
    public final static char FS        = '|';        // Field Separator
    private static HashSet<String> vowelWildCardSet_ = new HashSet<String>();
    private static HashSet<String> consonantWildCardSet_ 
        = new HashSet<String>();
    private static HashSet<String> wildCardSet_ = new HashSet<String>();
    private static HashSet<String> vowelSet_ = new HashSet<String>();
    private static HashSet<String> consonantSet_ = new HashSet<String>();
    private static HashSet<String> digitSet_ = new HashSet<String>();
    private static HashSet<String> letterSet_ = new HashSet<String>();
    static
    {
        vowelWildCardSet_.add("A");
        vowelWildCardSet_.add("E");
        vowelWildCardSet_.add("I");
        vowelWildCardSet_.add("O");
        vowelWildCardSet_.add("U");
        consonantWildCardSet_.add("B");
        consonantWildCardSet_.add("C");
        consonantWildCardSet_.add("F");
        consonantWildCardSet_.add("G");
        consonantWildCardSet_.add("H");
        consonantWildCardSet_.add("J");
        consonantWildCardSet_.add("K");
        consonantWildCardSet_.add("M");
        consonantWildCardSet_.add("N");
        consonantWildCardSet_.add("P");
        consonantWildCardSet_.add("Q");
        consonantWildCardSet_.add("R");
        consonantWildCardSet_.add("T");
        consonantWildCardSet_.add("V");
        consonantWildCardSet_.add("W");
        consonantWildCardSet_.add("X");
        consonantWildCardSet_.add("Y");
        consonantWildCardSet_.add("Z");
        wildCardSet_.add(new Character(VOWEL).toString());
        wildCardSet_.add(new Character(CONSONANT).toString());
        wildCardSet_.add(new Character(SAME_AS_PREV).toString());
        wildCardSet_.add(new Character(DIGIT).toString());
        wildCardSet_.add(new Character(LETTER).toString());
        wildCardSet_.add(new Character(BEGIN).toString());
        wildCardSet_.add(new Character(END).toString());
        vowelSet_.add("a");
        vowelSet_.add("e");
        vowelSet_.add("i");
        vowelSet_.add("o");
        vowelSet_.add("u");
        // add diacritics
        vowelSet_.add("\u00E0");
        vowelSet_.add("\u00E1");
        vowelSet_.add("\u00E2");
        vowelSet_.add("\u00E3");
        vowelSet_.add("\u00E4");
        vowelSet_.add("\u00E5");
        vowelSet_.add("\u00E8");
        vowelSet_.add("\u00E9");
        vowelSet_.add("\u00EA");
        vowelSet_.add("\u00EB");
        vowelSet_.add("\u00EC");
        vowelSet_.add("\u00ED");
        vowelSet_.add("\u00EE");
        vowelSet_.add("\u00EF");
        vowelSet_.add("\u00F0");
        vowelSet_.add("\u00F2");
        vowelSet_.add("\u00F3");
        vowelSet_.add("\u00F4");
        vowelSet_.add("\u00F5");
        vowelSet_.add("\u00F6");
        vowelSet_.add("\u00F8");
        vowelSet_.add("\u00F9");
        vowelSet_.add("\u00FA");
        vowelSet_.add("\u00FB");
        vowelSet_.add("\u00FC");
        consonantSet_.add("b");
        consonantSet_.add("c");
        consonantSet_.add("d");
        consonantSet_.add("f");
        consonantSet_.add("g");
        consonantSet_.add("h");
        consonantSet_.add("j");
        consonantSet_.add("k");
        consonantSet_.add("l");
        consonantSet_.add("m");
        consonantSet_.add("n");
        consonantSet_.add("p");
        consonantSet_.add("q");
        consonantSet_.add("r");
        consonantSet_.add("s");
        consonantSet_.add("t");
        consonantSet_.add("v");
        consonantSet_.add("w");
        consonantSet_.add("x");
        consonantSet_.add("y");
        consonantSet_.add("z");
        // add diacritics
        consonantSet_.add("\u00E7");
        consonantSet_.add("\u00F1");
        consonantSet_.add("\u00FD");
        consonantSet_.add("\u00FE");
        letterSet_ = new HashSet<String>(consonantSet_);
        letterSet_.add("a");
        letterSet_.add("e");
        letterSet_.add("i");
        letterSet_.add("o");
        letterSet_.add("u");
        // add diacritics
        letterSet_.add("\u00E0");
        letterSet_.add("\u00E1");
        letterSet_.add("\u00E2");
        letterSet_.add("\u00E3");
        letterSet_.add("\u00E4");
        letterSet_.add("\u00E5");
        letterSet_.add("\u00E8");
        letterSet_.add("\u00E9");
        letterSet_.add("\u00EA");
        letterSet_.add("\u00EB");
        letterSet_.add("\u00EC");
        letterSet_.add("\u00ED");
        letterSet_.add("\u00EE");
        letterSet_.add("\u00EF");
        letterSet_.add("\u00F0");
        letterSet_.add("\u00F2");
        letterSet_.add("\u00F3");
        letterSet_.add("\u00F4");
        letterSet_.add("\u00F5");
        letterSet_.add("\u00F6");
        letterSet_.add("\u00F8");
        letterSet_.add("\u00F9");
        letterSet_.add("\u00FA");
        letterSet_.add("\u00FB");
        letterSet_.add("\u00FC");
        letterSet_.add("\u00E7");
        letterSet_.add("\u00F1");
        letterSet_.add("\u00FD");
        letterSet_.add("\u00FE");
    }
}
