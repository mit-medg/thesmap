package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class normalizes Unicode characters to ASCII with synonym option.
* The normalization includes
* <ul>
* <li> get unicode synonym base (-f:q4)
* <li> get core unicode Norm (-f:q7)
* <li> get unicode name (-f:q3)
* </ul>
* In other words, this flow compoment is composed by -f:q4:q7:q3.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/normUnicodeWithSynonym.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/normUnicodeSynonym.html"> 
* Norm Unicode characters to ASCII with Synonym options</a>
*
* @version    V-2013
****************************************************************************/
public class ToNormUnicodeWithSynonym extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   unicodeSynonymMap   a hash table contains Unicode synonym 
    * @param   symbolMap   a hash table contain the Unicode symbols mapping
    * @param   unicodeMap   a hash table contain the Unicode mapping
    * @param   ligatureMap   a hash table contains the mapping of ligatures
    * @param   diacriticMap  a hash table contains the mapping of diacritics
    * @param   startTag   the starting tag for symbol name (default: ![ )
    * @param   endTag     the ending tag for symbol name (default: ]! )
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector)
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, Character> unicodeSynonymMap,
        Hashtable<Character, String> symbolMap,
        Hashtable<Character, String> unicodeMap,
        Hashtable<Character, String> ligatureMap,
        Hashtable<Character, Character> diacriticMap,
        String startTag, String endTag, boolean detailsFlag, boolean mutateFlag)
    {
        // mutate the term: Unicode coreNorm 
        String synonymTerm = ToGetUnicodeSynonyms.GetUnicodeSynonym(
            in.GetSourceTerm(), unicodeSynonymMap);
        // mutate the term: Unicode coreNorm 
        String coreNormTerm = ToUnicodeCoreNorm.GetCoreNormStr(
            synonymTerm, symbolMap, unicodeMap, ligatureMap, diacriticMap);
        // mutate the term: symbol name 
        String term = ToGetUnicodeNames.GetUnicodeName(coreNormTerm, startTag,
            endTag);
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = INFO;
        }
        if(mutateFlag == true)
        {
            mutate = Transformation.NO_MUTATE_INFO;
        }
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.NORM_UNICODE_WITH_SYNONYM, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        String testStr = GetTestStr(args, "\u00A9 and \u00B5");
        Configuration conf = new Configuration("data.config.lvg", true);
        Hashtable<Character, Character> unicodeSynonyms 
            = ToGetUnicodeSynonyms.GetUnicodeSynonymMapFromFile(conf);
        Hashtable<Character, String> symbolMap
            = ToMapSymbolToAscii.GetSymbolMapFromFile(conf);
        Hashtable<Character, String> unicodeMap
            = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf);
        Hashtable<Character, String> ligatureMap 
            = ToSplitLigatures.GetLigatureMapFromFile(conf);
        Hashtable<Character, Character> diacriticMap 
            = ToStripDiacritics.GetDiacriticMapFromFile(conf);
        String startTag = conf.GetConfiguration(Configuration.START_TAG);
        String endTag = conf.GetConfiguration(Configuration.END_TAG);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToNormUnicodeWithSynonym.Mutate(in, 
            unicodeSynonyms, symbolMap, unicodeMap, ligatureMap, diacriticMap,    
            startTag, endTag, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    // data members
    private static final String INFO = "Unicode Norm With Synonym option";
}
