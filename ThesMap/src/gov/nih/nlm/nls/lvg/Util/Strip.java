package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/*****************************************************************************
* This class strip strings and words from a given string.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
***************************************************************************/
public class Strip
{
    // public methods
    
    /**
    * Strip substrings from a given string.  A list of string is given for 
    * stripping.
    *
    * @param   inStr  string to be stripped
    * @param   strList   a string list.  All substrings in inStr that match any 
    *          element in this list will be stripped.
    * @param   caseSensitive   true or false for case snesitive or insensitive
    *          match for stripping
    * @return  a string after being stripped
    */
    public static String StripStrings(String inStr, Vector<String> strList,
        boolean caseSensitive)
    {
        String out = inStr;
        // go through the string list (stop words)
        for(int i = 0; i < strList.size(); i++)
        {
            String str = strList.elementAt(i);
            out = StripString(out, str, caseSensitive);
        }
        return out;
    }
    /**
    * Strip substrings from a given string.  A string is given for stripping.
    *
    * @param   inStr  string to be stripped
    * @param   str    a string.  All substrings in inStr that match str 
    *          will be stripped.
    * @param   caseSensitive   true or false for case snesitive or insensitive
    *          match for stripping
    * @return  a string after being stripped
    */
    public static String StripString(String inStr, String str, 
        boolean caseSensitive)
    {
        String out = inStr;
        int strSize = str.length();
        int index = -1;
        
        // find if inStr contians str
        if(caseSensitive == true)
        {
            index = out.indexOf(str, 0);
        }
        else    // ignore case
        {
            index = out.toLowerCase().indexOf(str.toLowerCase(), 0);   
        }
        // check if the given string contain the str
        while(index != -1)
        {
            int inSize = out.length();
            int beginIndex = index-1;            // consider the space before
            int endIndex = index+strSize+1;        // consider the space after
            // refine the index
            if(index == 0)        // if the str at the begining
            {
                beginIndex = 0;
            }
            if (index == inSize-strSize)    // if the str at the end
            {
                endIndex = inSize;
            }
            // check if the found str is exact the same as str
            String temp = out.substring(beginIndex, endIndex).trim();
            // special fix if ',' is at the beginning of the temp
            if(temp.charAt(0) == ',')
            {
                temp = temp.substring(1, temp.length());
            }
            // strip the string
            if(caseSensitive == true) 
            {
                if(temp.equals(str) == true)
                {
                    String before = out.substring(0, beginIndex);
                    String after = out.substring(endIndex, inSize);
                    out = before + " " + after;        // add a space between
                    index = out.indexOf(str, beginIndex);
                }
                else
                {
                    index = out.indexOf(str, beginIndex+2);
                }
            }
            else     // caseSensitive == false 
            {
                if(temp.equalsIgnoreCase(str))
                {
                    String before = out.substring(0, beginIndex);
                    String after = out.substring(endIndex, inSize);
                    out = before + " " + after;        // add a space between
                    index = out.toLowerCase().indexOf(str.toLowerCase(), 
                        beginIndex);
                }
                else    // non match case
                {
                    index = out.toLowerCase().indexOf(str.toLowerCase(), 
                        beginIndex+2);
                }
            }
        }
        return out.trim();
    }
    /**
    * Strip words from a given string.  A list of word is given for stripping.
    *
    * @param   inStr  string to be stripped
    * @param   wordList   a word list.  All words in inStr that match any 
    *          element in this list will be stripped.
    * @param   caseSensitive   true or false for case snesitive or insensitive
    *          match for stripping
    * @return  a string after being stripped
    */
    public static String StripWords(String inStr, Vector<String> wordList, 
        boolean caseSensitive)
    {
        StringTokenizer buf = new StringTokenizer(inStr, " \t");
        Vector<String> inStrList = new Vector<String>();
        String temp = null;
        String out = "";
        while(buf.hasMoreTokens() == true)
        {
            // use space and tab to tokenize words from a string
            temp = buf.nextToken();
            boolean foundWord = false;
            for(int i = 0; i < wordList.size(); i++)
            {
                String word = wordList.elementAt(i);
                
                if((caseSensitive == true)
                && (temp.equals(word) == true))
                {
                    foundWord = true;
                    break;
                }
                else if((caseSensitive == false)
                && (temp.toLowerCase().equals(word) == true))
                // all elements in wordList must be lowercase
                {
                    foundWord = true;
                    break;
                }
            }
            if(foundWord == false)
            {
                inStrList.addElement(temp);
            }
        }
        // reconstruct the string from unstripped words
        for(int i = 0; i < inStrList.size(); i++)
        {
            out += inStrList.elementAt(i) + " ";
        }
        return out.trim();
    }
    /**
    * Test driver for using this class
    */
    public static void main(String[] args)
    {
        if(args.length != 1)
        {
            System.out.println("** Usage: java StripWord <inStr>");
        }
        else
        {
            String inStr = args[0];
            Vector<String> strList = GetStringList();
            String outStr = Strip.StripStrings(inStr, strList, false);
            System.out.println("=> outStr: '" + outStr + "'");
        }
    }
    // private methods
    private static Vector<String> GetStringList()
    {
        Vector<String> strList = new Vector<String>(5);
        strList.addElement("of");
        strList.addElement("and");
        strList.addElement("with");
        strList.addElement("for");
        strList.addElement("nos");
        strList.addElement("to");
        strList.addElement("in");
        strList.addElement("by");
        strList.addElement("on");
        strList.addElement("the");
        strList.addElement("(non mesh)");
        return strList;
    }
}
