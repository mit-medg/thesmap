package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides methods of combining Lvg output records by specifying 
* different rules.  It is utilized under Lvg Output filter options.
*
* 
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Flow extends BitMaskBase
{
    // public constructor
    /**
    * Create a Flow object
    */
    public Flow()
    {
        super(ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Create a Flow object, using an integer value
    */
    public Flow(int intValue)
    {
        super(intValue, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Convert a combined value string to a long gender value.
    *
    * @param   valueStr  combined name in String format
    *
    * @return  a long value of the specified name
    */
    public static long ToValue(String valueStr)
    {
        return ToValue(valueStr, bitStr_);
    }
    /**
    * Convert a long gender value to a combined string (abbreviation).
    *
    * @param   value  number for finding it's combined name
    *
    * @return  name in a String format
    */
    public static String ToName(long value)
    {
        return ToName(value, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Get the name (first in the name list) of a specified bit (single).
    * The first one in LVG is the abbreviation name.
    *
    * @param   bitValue  bit nubmer for finding it's name
    *
    * @return  name of the bit specified
    */
    public static String GetBitName(int bitValue)
    {
        return GetBitName(bitValue, 0);
    }
    /**
    * Get the name at index order of a specified bit (single).
    *
    * @param   bitValue  bit nubmer for finding it's name
    * @param   index   the order index of the name in bitStr_[]
    *
    * @return  name at index order of the bit specified
    */
    public static String GetBitName(int bitValue, int index)
    {
        return GetBitName(bitValue, index, bitStr_);
    }
    /**
    * Get the long value for one single name (no combine names of bits).
    *
    * @param   valueStr  name of a bit for finding it's long value
    *
    * @return  a long value of the specified name
    *
    */
    public static long Enumerate(String valueStr)
    {
        return Enumerate(valueStr, bitStr_);
    }
    // public methods
    /** 
    * Test driver for this Flow class
    */
    public static void main(String[] args)
    {
        // static function
        System.out.println("------------------------------" );
        System.out.println(" - ToValue(l): " + Flow.ToValue("l")); 
        System.out.println(" - ToValue(i): " + Flow.ToValue("i")); 
        System.out.println(" - ToValue(l+i): " + Flow.ToValue("l+i")); 
        Flow f = new Flow();
        System.out.println("------------------------------" );
        System.out.println(" - GetBitName(LOWER_CASE): " 
            + Flow.GetBitName(LOWER_CASE)); 
        System.out.println(" - GetBitName(INFLECTION): " 
            + Flow.GetBitName(INFLECTION)); 
        System.out.println("------------------------------" );
        System.out.println(" - Enumerate(ToLowerCase): " + 
            Flow.Enumerate("ToLowerCase")); 
        System.out.println(" - Enumerate(l): " + Flow.Enumerate("l")); 
        System.out.println(" - Enumerate(L): " + Flow.Enumerate("L")); 
        
        System.out.println("------------------------------" );
        f.SetValue(3);
        System.out.println(" - GetBit(LOWER_CASE): " 
            + f.GetBitFlag(LOWER_CASE)); 
        System.out.println(" - GetBit(INFLECTION): " 
            + f.GetBitFlag(INFLECTION)); 
        System.out.println("------------------------------" );
        System.out.println(" - GetValue(): " + f.GetValue()); 
        f.SetBitFlag(LOWER_CASE, false); 
        System.out.println(" - GetValue(): " + f.GetValue()); 
        f.SetBitFlag(LOWER_CASE, true); 
        System.out.println(" - GetValue(): " + f.GetValue()); 
        System.out.println("------------------------------" );
        f.SetBitFlag(INFLECTION, false); 
        System.out.println(" - GetValue(): " + f.GetValue()); 
        f.SetBitFlag(INFLECTION, true); 
        System.out.println(" - GetValue(): " + f.GetValue()); 
    }
    // data members
    /** None bit for flow */
    public final static int NONE                           = 0;
    /** inflection flow component bit for flow */
    public final static int INFLECTION                     = 1;
    /** strip stop word flow component bit for flow */
    public final static int STRIP_STOP_WORDS               = 2;
    /** remove genitive flow component bit for flow */
    public final static int REMOVE_GENITIVE                = 3;
    /** replace punctuation with space flow component bit for flow */
    public final static int REPLACE_PUNCTUATION_WITH_SPACE = 4;
    /** strip punctuation flow component bit for flow */
    public final static int STRIP_PUNCTUATION              = 5;
    /** strip punctuation, enhanced, flow component bit for flow */
    public final static int STRIP_PUNCTUATION_ENHANCED     = 6;
    /** sort by word order flow component bit for flow */
    public final static int SORT_BY_WORD_ORDER             = 7;
    /** uninflect words flow component bit for flow */
    public final static int UNINFLECT_WORDS                = 8;
    /** normalize flow component bit for flow */
    public final static int NORMALIZE                       = 9;
    /** canonicalize flow component bit for flow */
    public final static int CANONICALIZE                   = 10;
    /** lui nomalize flow component bit for flow */
    public final static int LUI_NORMALIZE                   = 11;
    /** strip NEC and NOS flow component bit for flow */
    public final static int STRIP_NEC_NOS                   = 12;
    /** uninflect term flow component bit for flow */
    public final static int UNINFLECT_TERM                 = 13;
    /** generate spelling variants flow component bit for flow */
    public final static int GENERATE_SPELLING_VARIANTS     = 14;
    /** no operatin flow component bit for flow */
    public final static int NO_OPERATION                   = 15;
    /** generate acronyms flow component bit for flow */
    public final static int ACRONYMS                       = 16;
    /** generate expansions flow component bit for flow */
    public final static int EXPANSIONS                     = 17;
    /** generate derivation flow component bit for flow */
    public final static int DERIVATION                     = 18;
    /** generate derivation by category flow component bit for flow */
    public final static int DERIVATION_BY_CATEGORY         = 19;
    /** derivation by category and inlfection flow component bit for flow */
    public final static int INFLECTION_BY_CAT_INFL         = 20;
    /** tokenize flow component bit for flow */
    public final static int TOKENIZE                       = 21;
    /** tokenize without hyphens flow component bit for flow */
    public final static int TOKENIZE_NO_HYPHENS            = 22;
    /** generate base spelling variants flow component bit for flow */
    public final static int BASE_SPELLING_VARIANTS         = 23;
    /** retrieve EUI flow component bit for flow */
    public final static int RETRIEVE_EUI                   = 24;
    /** retrieve Category and inflection flow component bit for flow */
    public final static int RETRIEVE_CAT_INFL              = 25;
    /** retrieve category and inflectin from LVG flow component bit for flow */
    public final static int RETRIEVE_CAT_INFL_DB           = 26;
    /** retrieve category and inflection for term begin with a given pattern 
    flow component bit for flow */
    public final static int RETRIEVE_CAT_INFL_BEGIN        = 27;
    /** generate synonyms flow component bit for flow */
    public final static int SYNONYMS                       = 28;
    /** filter flow component bit for flow */
    public final static int FILTER                         = 29;
    /** filter out proper nouns flow component bit for flow */
    public final static int FILTER_PROPER_NOUN             = 30;
    /** filter out acronym flow component bit for flow */
    public final static int FILTER_ACRONYM                 = 31;
    /** strip ambiguity tags flow component bit for flow */
    public final static int STRIP_AMBIGUITY_TAGS           = 32;
    /** uninvert flow component bit for flow */
    public final static int UNINVERT                       = 33;
    /** convert output flow component bit for flow */
    public final static int CONVERT_OUTPUT                 = 34;
    /** generate synonyms recursively flow component bit for flow */
    public final static int RECURSIVE_SYNONYMS             = 35;
    /** generate derivations recursively flow component bit for flow */
    public final static int RECURSIVE_DERIVATIONS          = 36;
    /** lower case flow component bit for flow */
    public final static int LOWER_CASE                     = 37;
    /** citation term flow component bit for flow */
    public final static int CITATION                       = 38;
    /** normalize uninflected words flow component bit for flow */
    public final static int NORM_UNINFLECT_WORDS           = 39;
    /** strip diacritics flow component bit for flow */
    public final static int STRIP_DIACRITICS               = 40;
    /** metaphone flow component bit for flow */
    public final static int METAPHONE                      = 41;
    /** fruitful variants flow component bit for flow */
    public final static int FRUITFUL_VARIANTS              = 42;
    /** tokenize, keep all flow component bit for flow */
    public final static int TOKENIZE_KEEP_ALL              = 43;
    /** syntactic uninvert flow component bit for flow */
    public final static int SYNTACTIC_UNINVERT             = 44;
    /** fruitful variants known to Lexicon flow component bit for flow */
    public final static int FRUITFUL_VARIANTS_LEX          = 45;
    /** fruitful variants by database flow component bit for flow */
    public final static int FRUITFUL_VARIANTS_DB           = 46;
    /** antiNorm flow component bit for flow */
    public final static int ANTINORM                       = 47;
    /** word size filter flow component bit for flow */
    public final static int WORD_SIZE                      = 48;
    /** enhanced fruitful variants flow component bit for flow */
    public final static int FRUITFUL_ENHANCED              = 49;
    /** simple inflection flow component bit for flow */
    public final static int SIMPLE_INFLECTIONS             = 50;
    /** inflection simple flow component bit for flow */
    public final static int INFLECTION_SIMPLE              = 51;
    /** splite ligature flow component bit for flow */
    public final static int SPLIT_LIGATURES                = 52;
    /** get symbol name flow component bit for flow */
    public final static int GET_UNICODE_NAME               = 53;
    /** get synonyms of symbol name flow component bit for flow */
    public final static int GET_UNICODE_SYNONYM            = 54;
    /** normalize characters in a string flow component bit for flow */
    public final static int NORM_UNICODE                   = 55;
    /** normalize characters with symbol synonyms flow component bit for flow */
    public final static int NORM_UNICODE_WITH_SYNONYM      = 56;
    /** nominalization flow component bit for flow */
    public final static int NOMINALIZATION                 = 57;
    /** removeS flow component bit for flow */
    public final static int REMOVE_S                       = 58;
    /** punctuation Symbol mapping flow component bit for flow */
    public final static int MAP_SYMBOL_TO_ASCII            = 59;
    /** unicdoe mapping flow component bit for flow */
    public final static int MAP_UNICODE_TO_ASCII           = 60;
    /** Unicode coreNorm flow component bit for flow */
    public final static int UNICODE_CORE_NORM              = 61;
    /** Unicode strip or mapping flow component bit for flow */
    public final static int STRIP_MAP_UNICODE              = 62;
    // private data member
    private final static long ALL_BIT_VALUE                = 0;    // dummy
    private static ArrayList<Vector<String>> bitStr_ 
        = new ArrayList<Vector<String>>(MAX_BIT);  // Must have
    // Init static data member, bitStr_.
    static
    {
        for(int i = 0; i < MAX_BIT; i++)
        {
            bitStr_.add(i, new Vector<String>());
        }
        bitStr_.get(NONE).addElement("");
        bitStr_.get(NONE).addElement("");        // Need to be duplicated
        bitStr_.get(INFLECTION).addElement("Inflection");
        bitStr_.get(INFLECTION).addElement("i");
        bitStr_.get(STRIP_STOP_WORDS).addElement("StripStopWords");
        bitStr_.get(STRIP_STOP_WORDS).addElement("t");
        bitStr_.get(REMOVE_GENITIVE).addElement("RemoveGenitive");
        bitStr_.get(REMOVE_GENITIVE).addElement("g");
        bitStr_.get(REPLACE_PUNCTUATION_WITH_SPACE).addElement(
            "ReplacePunctuationWithSpace");
        bitStr_.get(REPLACE_PUNCTUATION_WITH_SPACE).addElement("o");
        bitStr_.get(STRIP_PUNCTUATION).addElement("StripPunctuation");
        bitStr_.get(STRIP_PUNCTUATION).addElement("p");
        bitStr_.get(STRIP_PUNCTUATION_ENHANCED).addElement(
            "StripPunctuationEnhanced");
        bitStr_.get(STRIP_PUNCTUATION_ENHANCED).addElement("P");
        bitStr_.get(SORT_BY_WORD_ORDER).addElement("SortByWordOrder");
        bitStr_.get(SORT_BY_WORD_ORDER).addElement("w");
        bitStr_.get(UNINFLECT_WORDS).addElement("UninflectWords");
        bitStr_.get(UNINFLECT_WORDS).addElement("B");
        bitStr_.get(NORMALIZE).addElement("Normalize");
        bitStr_.get(NORMALIZE).addElement("N");
        bitStr_.get(CANONICALIZE).addElement("Canonicalize");
        bitStr_.get(CANONICALIZE).addElement("C");
        bitStr_.get(LUI_NORMALIZE).addElement("LuiNormalize");
        bitStr_.get(LUI_NORMALIZE).addElement("N3");
        bitStr_.get(STRIP_NEC_NOS).addElement("StripNecNos");
        bitStr_.get(STRIP_NEC_NOS).addElement("0");
        bitStr_.get(UNINFLECT_TERM).addElement("UninflectTerm");
        bitStr_.get(UNINFLECT_TERM).addElement("b");
        bitStr_.get(GENERATE_SPELLING_VARIANTS).addElement(
            "GenerateSpellingVariants");
        bitStr_.get(GENERATE_SPELLING_VARIANTS).addElement("s");
        bitStr_.get(NO_OPERATION).addElement("NoOperation");
        bitStr_.get(NO_OPERATION).addElement("n");
        bitStr_.get(ACRONYMS).addElement("Acronyms");
        bitStr_.get(ACRONYMS).addElement("A");
        bitStr_.get(EXPANSIONS).addElement("Expansions");
        bitStr_.get(EXPANSIONS).addElement("a");
        bitStr_.get(DERIVATION).addElement("Derivation");
        bitStr_.get(DERIVATION).addElement("d");
        bitStr_.get(DERIVATION_BY_CATEGORY).addElement("DerivationByCategory");
        bitStr_.get(DERIVATION_BY_CATEGORY).addElement("dc");
        bitStr_.get(INFLECTION_BY_CAT_INFL).addElement("InflectionByCatInfl");
        bitStr_.get(INFLECTION_BY_CAT_INFL).addElement("ici");
        bitStr_.get(TOKENIZE).addElement("Tokenize");
        bitStr_.get(TOKENIZE).addElement("c");
        bitStr_.get(TOKENIZE_NO_HYPHENS).addElement("TokenizeNoHyphens");
        bitStr_.get(TOKENIZE_NO_HYPHENS).addElement("ch");
        bitStr_.get(BASE_SPELLING_VARIANTS).addElement("BaseSpellingVariants");
        bitStr_.get(BASE_SPELLING_VARIANTS).addElement("e");
        bitStr_.get(RETRIEVE_EUI).addElement("RetrieveEui");
        bitStr_.get(RETRIEVE_EUI).addElement("E");
        bitStr_.get(RETRIEVE_CAT_INFL).addElement("RetrieveCatInfl");
        bitStr_.get(RETRIEVE_CAT_INFL).addElement("L");
        bitStr_.get(RETRIEVE_CAT_INFL_DB).addElement("RetrieveCatInflDb");
        bitStr_.get(RETRIEVE_CAT_INFL_DB).addElement("Ln");
        bitStr_.get(RETRIEVE_CAT_INFL_BEGIN).addElement("RetrieveCatInflBegin");
        bitStr_.get(RETRIEVE_CAT_INFL_BEGIN).addElement("Lp");
        bitStr_.get(SYNONYMS).addElement("GenerateSynonyms");
        bitStr_.get(SYNONYMS).addElement("y");
        bitStr_.get(FILTER).addElement("Filter");
        bitStr_.get(FILTER).addElement("f");
        bitStr_.get(FILTER_PROPER_NOUN).addElement("FilterProperNoun");
        bitStr_.get(FILTER_PROPER_NOUN).addElement("fp");
        bitStr_.get(FILTER_ACRONYM).addElement("FilterAcronym");
        bitStr_.get(FILTER_ACRONYM).addElement("fa");
        bitStr_.get(STRIP_AMBIGUITY_TAGS).addElement("StripAmbiguityTags");
        bitStr_.get(STRIP_AMBIGUITY_TAGS).addElement("T");
        bitStr_.get(UNINVERT).addElement("Uninvert");
        bitStr_.get(UNINVERT).addElement("u");
        bitStr_.get(CONVERT_OUTPUT).addElement("ConvertOutput");
        bitStr_.get(CONVERT_OUTPUT).addElement("U");
        bitStr_.get(RECURSIVE_SYNONYMS).addElement("RecursiveSynonyms");
        bitStr_.get(RECURSIVE_SYNONYMS).addElement("r");
        bitStr_.get(RECURSIVE_DERIVATIONS).addElement("RecursiveDerivations");
        bitStr_.get(RECURSIVE_DERIVATIONS).addElement("R");
        bitStr_.get(LOWER_CASE).addElement("LowerCase");
        bitStr_.get(LOWER_CASE).addElement("l");
        bitStr_.get(CITATION).addElement("Citation");
        bitStr_.get(CITATION).addElement("Ct");
        bitStr_.get(NORM_UNINFLECT_WORDS).addElement("NormUninflectWords");
        bitStr_.get(NORM_UNINFLECT_WORDS).addElement("Bn");
        bitStr_.get(STRIP_DIACRITICS).addElement("StripDiacritics");
        bitStr_.get(STRIP_DIACRITICS).addElement("q");
        bitStr_.get(METAPHONE).addElement("Metaphone");
        bitStr_.get(METAPHONE).addElement("m");
        bitStr_.get(FRUITFUL_VARIANTS).addElement("FruitfulVariants");
        bitStr_.get(FRUITFUL_VARIANTS).addElement("G");
        bitStr_.get(TOKENIZE_KEEP_ALL).addElement("TokenizeKeepAll");
        bitStr_.get(TOKENIZE_KEEP_ALL).addElement("ca");
        bitStr_.get(SYNTACTIC_UNINVERT).addElement("SyntacticUninvert");
        bitStr_.get(SYNTACTIC_UNINVERT).addElement("S");
        bitStr_.get(FRUITFUL_VARIANTS_LEX).addElement("FruitfulVariantsLex");
        bitStr_.get(FRUITFUL_VARIANTS_LEX).addElement("Gn");
        bitStr_.get(FRUITFUL_VARIANTS_DB).addElement("FruitfulVariantsDb");
        bitStr_.get(FRUITFUL_VARIANTS_DB).addElement("v");
        bitStr_.get(ANTINORM).addElement("AntiNorm");
        bitStr_.get(ANTINORM).addElement("An");
        bitStr_.get(WORD_SIZE).addElement("WordSize");
        bitStr_.get(WORD_SIZE).addElement("ws");
        bitStr_.get(FRUITFUL_ENHANCED).addElement("FruitfulEnhanced");
        bitStr_.get(FRUITFUL_ENHANCED).addElement("Ge");
        bitStr_.get(SIMPLE_INFLECTIONS).addElement("SimpleInflections");
        bitStr_.get(SIMPLE_INFLECTIONS).addElement("Si");
        bitStr_.get(INFLECTION_SIMPLE).addElement("InflectionSimple");
        bitStr_.get(INFLECTION_SIMPLE).addElement("is");
        bitStr_.get(SPLIT_LIGATURES).addElement("SplitLigature");
        bitStr_.get(SPLIT_LIGATURES).addElement("q2");
        bitStr_.get(GET_UNICODE_NAME).addElement("GetUnicodeName");
        bitStr_.get(GET_UNICODE_NAME).addElement("q3");
        bitStr_.get(GET_UNICODE_SYNONYM).addElement("GetUnicodeSynonym");
        bitStr_.get(GET_UNICODE_SYNONYM).addElement("q4");
        bitStr_.get(NORM_UNICODE).addElement("NormUnicode");
        bitStr_.get(NORM_UNICODE).addElement("q5");
        bitStr_.get(NORM_UNICODE_WITH_SYNONYM).addElement("NormUnicodeWithSynonym");
        bitStr_.get(NORM_UNICODE_WITH_SYNONYM).addElement("q6");
        bitStr_.get(NOMINALIZATION).addElement("RetrieveNominalization");
        bitStr_.get(NOMINALIZATION).addElement("nom");
        bitStr_.get(REMOVE_S).addElement("RemoveS");
        bitStr_.get(REMOVE_S).addElement("rs");
        bitStr_.get(MAP_SYMBOL_TO_ASCII).addElement("MapSymbolToAscii");
        bitStr_.get(MAP_SYMBOL_TO_ASCII).addElement("q0");
        bitStr_.get(MAP_UNICODE_TO_ASCII).addElement("MapUnicodeToAscii");
        bitStr_.get(MAP_UNICODE_TO_ASCII).addElement("q1");
        bitStr_.get(UNICODE_CORE_NORM).addElement("UnicodeCoreNorm");
        bitStr_.get(UNICODE_CORE_NORM).addElement("q7");
        bitStr_.get(STRIP_MAP_UNICODE).addElement("StripMapUnicode");
        bitStr_.get(STRIP_MAP_UNICODE).addElement("q8");
    }
}
