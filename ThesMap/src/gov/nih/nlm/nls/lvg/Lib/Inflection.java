package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This Inflection class extends BitMaskBase class and performs as LVG 
* Inflection.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/design.html#INFLECTION">
*      Design document </a>
*
* @version    V-2013
****************************************************************************/
public class Inflection extends BitMaskBase
{
    // public constructors
    /**
    *   Creates a default inflection object
    */
    public Inflection()
    {
        super(ALL_BIT_VALUE, bitStr_);
    }
    /**
    *   Creates a default inflection object, using a long value
    */
    public Inflection(long value)
    {
        super(value, ALL_BIT_VALUE, bitStr_);
    }
    /**
    * Convert a combined value string to a long inflection value.
    *
    * @param   valueStr  combined name in String format
    *
    * @return  a long value of the specified name
    */
    public static long ToValue(String valueStr)
    {
        long value = ToValue(valueStr, bitStr_);
        return value;
    }
    /**
    * Convert a long inflection value to a combined string (abbreviation).
    *
    * @param   value  number for finding it's combined name
    *
    * @return  name in a String format
    */
    public static String ToName(long value)
    {
        String valueStr = ToName(value, ALL_BIT_VALUE, bitStr_);
        return valueStr;
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
    /**
    * Get a Vector<Long> include all values from the combined value.  
    * For examples, a value of 129 will return a vector contains two elements 
    * (Long) with value of 1 and 128.
    *
    * @param   value  combined value
    *
    * @return  Vector<Long> include all values from the combined value
    */
    public static Vector<Long> ToValues(long value)
    {
        return ToValues(value, TOTAL_BITS);
    }
    /**
    * Get an array includes all values from the combined value.  For example,
    * a value of 129 will return an array contains two elements (long) with
    * value of 1 and 128.
    *
    * @param   value  combined value
    *
    * @return  an array includes all values from the combined value
    */
    public static long[] ToValuesArray(long value)
    {
        Vector<Long> out = ToValues(value, TOTAL_BITS);
        return Vec.ToArray(out);
    }
    /** 
    * Test driver  for this class
    */
    public static void main(String[] args)
    {
        // static methods
        String foo = "base+singular+plural+infinitive+pres+past+presPart" +
            "+pastPart+positive+comparative+superlative";
        System.out.println(" -  ToValue(" + foo + "): " 
            + Inflection.ToValue(foo));
        System.out.println(" -  ToName(2099071): " 
            + Inflection.ToName(2099071));
        foo = "base+singular+infinitive+positive";
        System.out.println(" -  ToValue(" + foo + "): " 
            + Inflection.ToValue(foo));
        System.out.println(" -  ToName(1793): " + Inflection.ToName(1793));
        
        Vector<Long> values = Inflection.ToValues(1793);
        for(int i = 0; i < values.size(); i++)
        {
            System.out.println(" - Inflection.ToValues(1793): "
                + values.elementAt(i));
        }
        long[] valuesArray = Inflection.ToValuesArray(1793);
        for(int i = 0; i < valuesArray.length; i++)
        {
            System.out.println(" - Inflection.ToValues(1793): " 
                + valuesArray[i]);
        }
        System.out.println(" -  Category.ToName(2047): "
            + Category.ToName(2047));
    }
    // data members
    /** base bit for inflection: xx (noun, adjv, verb) */
    public final static int BASE_BIT              = 0;    // xx
    /** comparative bit for inflection: xx (adjv) + er */
    public final static int COMPARATIVE_BIT       = 1;    // xx (adjv) + er
    /** superlative bit for inflection: xx (adjv) + est */
    public final static int SUPERLATIVE_BIT       = 2;    // xx (adjv) + est
    /** plural bit for inflection: xx (noun) + s */
    public final static int PLURAL_BIT            = 3;    // xx (noun) + s
    /** present participle bit for inflection: xx (verb) + ing */
    public final static int PRES_PART_BIT         = 4;    // xx (verb) + ing
    /** past bit for inflection: xx (verb) + ed */
    public final static int PAST_BIT              = 5;    // xx (verb) + ed
    /** past [articiple bit for inflection: xx (verb) + en */
    public final static int PAST_PART_BIT         = 6;    // xx (verb) + en
    /** present 3ps bit for inflection: xx (verb) + s */
    public final static int PRES_3S_BIT           = 7;    // xx (verb) + s
    /** positive bit for inflection: xx (adjv) */
    public final static int POSITIVE_BIT          = 8;    // xx (adjv) 
    /** sigular bit for inflection: xx (noun) */
    public final static int SINGULAR_BIT          = 9;    // xx (noun)
    /** infinitive bit for inflection: xx (verb) */
    public final static int INFINITIVE_BIT        = 10;     // xx (verb)
    /** present (1_2_3p) bit for inflection: xx (verb) */
    public final static int PRES_1_2_3P_BIT       = 11;  // xx (verb)
    /** past negative bit for inflection: xx (couldn't, didn't) */
    public final static int PAST_NEG_BIT          = 12;  // couldn't, didn't
    /** present negative (1_2_3p) bit for inflection: xx (don't) */
    public final static int PRES_1_2_3P_NEG_BIT   = 13;  // don't
    /** present (1s) bit for inflection: xx (verb, am) */
    public final static int PRES_1S_BIT           = 14;  // xx (verb), am
    /** past negative (1p_2_3p) bit for inflection: xx (weren't) */
    public final static int PAST_1P_2_3P_NEG_BIT  = 15;  // weren't
    /** past (1p_2_3p) bit for inflection: xx (were) */
    public final static int PAST_1P_2_3P_BIT      = 16;  // were
    /** past negative (1s_3s) bit for inflection: xx (wasn't) */
    public final static int PAST_1S_3S_NEG_BIT    = 17;  // wasn't
    /** present (1p_2_3p) bit for inflection: xx (are) */
    public final static int PRES_1P_2_3P_BIT      = 18;  // are
    /** present negative (1p_2_3p) bit for inflection: xx (aren't) */
    public final static int PRES_1P_2_3P_NEG_BIT  = 19;  // aren't
    /** past (1s_3s) bit for inflection: xx (was) */
    public final static int PAST_1S_3S_BIT        = 20;  // was
    /** present bit for inflection: xx (can) */
    public final static int PRES_BIT              = 21;  // can
    /** present negative (3s) bit for inflection: xx (isn't, hasn't) */
    public final static int PRES_3S_NEG_BIT       = 22;     // isn't, hasn't
    /** present negative bit for inflection: xx (can't, cannot) */
    public final static int PRES_NEG_BIT          = 23;  // can't, cannot
    /** All bits for inflection */
    public final static long LOWER_INFLECTIONS = 255;  // upper infls
    public final static long UPPER_INFLECTIONS = 16776704;  // upper infls
    public final static int SIMPLE_BITS = 7;      // last bits of simple infl
    public final static int TOTAL_BITS = 24;      // total bits used
    public final static long ALL_BIT_VALUE = 16777215;  // all bits are possible
    public final static long NO_BIT_VALUE = 0;          // no bit number
    private static ArrayList<Vector<String>> bitStr_ 
        = new ArrayList<Vector<String>>(MAX_BIT);  // Must have
    /** Mapping simple inflections */
    public static long[] MapSimpleInfl_ = {
        mask_[BASE_BIT], mask_[COMPARATIVE_BIT], mask_[SUPERLATIVE_BIT],
        mask_[PLURAL_BIT], mask_[PRES_PART_BIT], mask_[PAST_BIT],
        mask_[PAST_PART_BIT], mask_[PRES_3S_BIT], 
        mask_[BASE_BIT],         // POSITIVE_BIT
        mask_[BASE_BIT],         // SINGULAR_BIT
        mask_[BASE_BIT],         // INFINITIVE_BIT
        mask_[BASE_BIT],         // PRES_1_2_3P_BIT
        mask_[PAST_BIT],         // PAST_NEG_BIT
        mask_[PRES_BIT],         // PRES_1_2_3P_NEG_BIT
        mask_[PRES_BIT],         // PRES_1S_BIT
        mask_[PAST_BIT],         // PAST_1P_2_3P_NEG_BIT
        mask_[PAST_BIT],         // PAST_1P_2_3P_BIT
        mask_[PAST_BIT],         // PAST_1S_3S_NEG_BIT
        mask_[PRES_BIT],         // PRES_1P_2_3P_BIT
        mask_[PRES_BIT],         // PRES_1P_2_3P_NEG_BIT
        mask_[PAST_BIT],         // PAST_1S_3S_BIT
        mask_[PRES_BIT],         // PRES_BIT
        mask_[PRES_BIT],         // PRES_3S_NEG_BIT
        mask_[PRES_BIT],         // PRES_NEG_BIT
        };
    // Init static vars, bitStr_
    static
    {
        for(int i = 0; i < MAX_BIT; i++)
        {
            bitStr_.add(i, new Vector<String>());
        }
        // define all bit string
        bitStr_.get(BASE_BIT).addElement("base");
        bitStr_.get(COMPARATIVE_BIT).addElement("comparative");
        bitStr_.get(SUPERLATIVE_BIT).addElement("superlative");
        bitStr_.get(PLURAL_BIT).addElement("plural");
        bitStr_.get(PLURAL_BIT).addElement("p");
        bitStr_.get(PRES_PART_BIT).addElement("presPart");
        bitStr_.get(PRES_PART_BIT).addElement("ing");
        bitStr_.get(PAST_BIT).addElement("past");
        bitStr_.get(PAST_PART_BIT).addElement("pastPart");
        bitStr_.get(PRES_3S_BIT).addElement("pres3s");
        bitStr_.get(POSITIVE_BIT).addElement("positive");
        bitStr_.get(SINGULAR_BIT).addElement("singular");
        bitStr_.get(SINGULAR_BIT).addElement("s");
        bitStr_.get(INFINITIVE_BIT).addElement("infinitive");
        bitStr_.get(INFINITIVE_BIT).addElement("inf");
        bitStr_.get(PRES_1_2_3P_BIT).addElement("pres123p");
        // --- belows are not defined in original Java code
        bitStr_.get(PAST_NEG_BIT).addElement("pastNeg");
        bitStr_.get(PRES_1_2_3P_NEG_BIT).addElement("pres123pNeg");
        bitStr_.get(PRES_1S_BIT).addElement("pres1s");
        bitStr_.get(PAST_1P_2_3P_NEG_BIT).addElement("past1p23pNeg");
        bitStr_.get(PAST_1P_2_3P_BIT).addElement("past1p23p");
        bitStr_.get(PAST_1S_3S_NEG_BIT).addElement("past1s3sNeg");
        bitStr_.get(PRES_1P_2_3P_BIT).addElement("pres1p23p");
        bitStr_.get(PRES_1P_2_3P_NEG_BIT).addElement("pres1p23pNeg");
        bitStr_.get(PAST_1S_3S_BIT).addElement("past1s3s");
        bitStr_.get(PRES_BIT).addElement("pres");
        bitStr_.get(PRES_3S_NEG_BIT).addElement("pres3sNeg");
        bitStr_.get(PRES_NEG_BIT).addElement("presNeg");
    }
}
