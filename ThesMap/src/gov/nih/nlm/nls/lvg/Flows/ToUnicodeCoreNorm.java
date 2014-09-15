package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class normalize Unicode characters by recursively performing
* <ul>
* <li>Map Unicode symbols and punctuation to ASCII
* <li>Map Unicode characters to ASCII
* <li>Split ligatures
* <li>Strip diacritics
* </ul>
* until the character is ASCII or no further normalized result are obtained.
* This is the core normalization of covnert Unicode to ASCII.
* Please note that the results might inlcude non-ASCII characters.
* StripMapUnicode or getUnicodeName flows are followed to complete the ASCII
* Normalization.
*
* <p> Four mapping tables are used in this flow:
* <ul>
* <li>$LVG/data/Unicode/symbolMap.data.
* <li>$LVG/data/Unicode/unicodeMap.data.
* <li>$LVG/data/Unicode/ligaturemMap.data.
* <li>$LVG/data/Unicode/diacriticMap.data.
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/unicodeCoreNorm.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/unicodeCoreNorm.html"> 
* Unicode Core Norm</a>
*
* @version    V-2013
****************************************************************************/
public class ToUnicodeCoreNorm extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   symbolMap   a hash table contain the unicode symbols mapping
    * @param   unicodeMap   a hash table contain the unicode mapping
    * @param   ligatureMap   a hash table contain the ligatures mapping
    * @param   diacriticMap   a hash table contain the diacritics mapping
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, String> symbolMap, 
        Hashtable<Character, String> unicodeMap, 
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap, 
        boolean detailsFlag, boolean mutateFlag)
    {
        Vector<LexItem> out = GetUnicodeCoreNorm(in, symbolMap, 
            unicodeMap, ligatureMap, diacriticMap,
            INFO, detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Get unicode synonym
    *
    * @param   inStr   an input string
    * @param   symbolMap   a hash table contain the unicode symbols mapping
    * @param   unicodeMap   a hash table contain the unicode mapping
    * @param   ligatureMap   a hash table contain the ligatures mapping
    * @param   diacriticMap   a hash table contain the diacritics mapping
    *
    * @return  the base of unicode synonym
    */
    public static String GetCoreNormStr(String inStr,
        Hashtable<Character, String> symbolMap, 
        Hashtable<Character, String> unicodeMap, 
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap)
    {
        return GetCoreNormObj(inStr, symbolMap, unicodeMap, ligatureMap,
            diacriticMap).GetCurStr();
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        String testStr = GetTestStr(args, "\u00A9 and \u00B5");
        Configuration conf = new Configuration("data.config.lvg", true);
        Hashtable<Character, String> symbolMap 
            = ToMapSymbolToAscii.GetSymbolMapFromFile(conf);
        Hashtable<Character, String> unicodeMap 
            = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf);
        Hashtable<Character, String> ligatureMap 
            = ToSplitLigatures.GetLigatureMapFromFile(conf);
        Hashtable<Character, Character> diacriticMap 
            = ToStripDiacritics.GetDiacriticMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToUnicodeCoreNorm.Mutate(in, 
            symbolMap, unicodeMap, ligatureMap, diacriticMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> GetUnicodeCoreNorm(LexItem in,
        Hashtable<Character, String> symbolMap,
        Hashtable<Character, String> unicodeMap,
        Hashtable<Character, String> ligatureMap,
        Hashtable<Character, Character> diacriticMap,
        String infoStr, boolean detailsFlag, boolean mutateFlag)
    {
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = infoStr;
        }
        if(mutateFlag == true)
        {
            mutate = new String();
        }
        // mutate the term: get unicode name
        String inStr = in.GetSourceTerm();
        String fs = GlobalBehavior.GetFieldSeparator();
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            // core norm on each char
            char curChar = inStr.charAt(i);
            String curStr = UnicodeUtil.CharToStr(curChar);
            CoreNormObj curCoreNormObj = GetCoreNormObj(curStr,
                symbolMap, unicodeMap, ligatureMap, diacriticMap);
            buffer.append(curCoreNormObj.GetCurStr());
            // update mutate information
            if(mutateFlag == true)
            {
                mutate += curCoreNormObj.GetDetails() + fs;
            }
        }
        String term = buffer.toString();
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.UNICODE_CORE_NORM, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // Core Norm: by punctuation & symbols mapping, Unicode mapping,
    // stripping diacritics, split ligautre
    private static CoreNormObj GetCoreNormObj(String inStr,
        Hashtable<Character, String> symbolMap, 
        Hashtable<Character, String> unicodeMap, 
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap)
    {
        CoreNormObj curObj = new CoreNormObj(inStr);
        // recursive norm by Map, Split, Strip
        // Exit when the inStr == outStr or outStr is ASCII
        CoreNorm(curObj, symbolMap, unicodeMap, ligatureMap, diacriticMap);
        return curObj;
    }
    // normalize a unicode character until it's ascii string or no more result
    private static void CoreNorm(CoreNormObj inObj, 
        Hashtable<Character, String> symbolMap, 
        Hashtable<Character, String> unicodeMap, 
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap)
    {
        String curStr = inObj.GetCurStr();
        int curPos = inObj.GetCurPos();
        inObj.UpdateRecursiveNo();
        // check if exceed max limit of recursive number
        if(inObj.IsWithinRecursiveLimit() == false)
        {
            inObj.SetDetails(CoreNormObj.ERROR);
            return;
        }
        // go through every character in the string
        if(curPos < curStr.length())
        {
            char curChar = curStr.charAt(curPos);
            String slStr = ToSplitLigatures.SplitLigature(curChar, ligatureMap);
            char sdChar 
                = ToStripDiacritics.StripDiacritic(curChar, diacriticMap);
            // 0. if ascii, no operation, move to next character
            if(UnicodeUtil.IsAsciiChar(curChar) == true)
            {
                inObj.UpdateCurPos();
                inObj.SetDetails(CoreNormObj.ASCII);
            }
            // 1. if in punctuation mapping table, map it
            else if(symbolMap.containsKey(curChar) == true)
            {
                String mapStr = ToMapSymbolToAscii.MapUnicodeSymbolToAscii(
                    curChar, symbolMap);
                inObj.UpdateCurStr(mapStr);
                inObj.UpdateCurPos(mapStr.length());
                inObj.SetDetails(CoreNormObj.SYMBOL_MAPPING);
            }
            // 2. if in unicode mapping table, map it
            else if(unicodeMap.containsKey(curChar) == true)
            {
                String mapStr = ToMapUnicodeToAscii.MapUnicodeToAscii(curChar,
                    unicodeMap);
                inObj.UpdateCurStr(mapStr);
                inObj.UpdateCurPos(mapStr.length());
                inObj.SetDetails(CoreNormObj.UNICODE_MAPPING);
            }
            // 3. if a splitable ligature, set the splited string
            else if(slStr.equals(UnicodeUtil.CharToStr(curChar)) == false)
            {
                inObj.UpdateCurStr(slStr);
                inObj.SetDetails(CoreNormObj.SPLIT_LIGATURE);
            }
            // 4. if stripable diacritic, set it, must done after split
            else if(sdChar != curChar)
            {
                inObj.UpdateCurStr(String.valueOf(sdChar));
                inObj.SetDetails(CoreNormObj.STRIP_DIACRITIC);
            }
            // no above operation, it can't be Core normed, move to next
            else
            {
                inObj.UpdateCurPos();
                inObj.SetDetails(CoreNormObj.NO_MORE_OPERATION);
            }
            // recursively call itself
            CoreNorm(inObj, symbolMap, unicodeMap, ligatureMap, diacriticMap);
        }
    }
    // data members
    private static final String INFO = "Unicode Core Norm";
}
