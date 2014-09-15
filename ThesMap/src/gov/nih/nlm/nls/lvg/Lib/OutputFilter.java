package gov.nih.nlm.nls.lvg.Lib;
import java.io.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Flows.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Api.*;
/*****************************************************************************
* This class provides methods of Lvg output filter functions.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-15, chlu, 07-23-12, add derivtional type options
* <li>SCR-20, chlu, 07-23-12, add derivtion negation options
* <li>SCR-30, chlu, 08-24-12, Add option for derivation type and negation
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OutputFilter
{
    // public constructor
    // public methods
    /**
    * Execute the functions of output filter and print the result to the
    * LVG desinated outputs, screen ot a file.
    *
    * @param  results   Lvg output records before going through output filter
    * @param  mutateFlag   a boolean flag for showing the result with mutate 
    *         information
    * @param  detailsFlag   a boolean flag for showing the result with detail
    *         information of flow operation
    * @param  separator   a replacement separator other than the Lvg default
    *         separator
    * @param  originalTerm  the original input term (the whole line).
    * @param  lvgOutputOption  the lvg output filter option object.
    *
    * @return  result in String
    * 
    */
    public static String ExecuteOutputFilter(Vector<LexItem> results,
        boolean mutateFlag, boolean detailsFlag, String separator,
        String originalTerm, LvgOutputOption lvgOutputOption)
    {
        return ExecuteOutputFilter(results, mutateFlag, detailsFlag,
            separator, originalTerm, originalTerm, lvgOutputOption);
    }
    /**
    * Execute the functions of output filter and print the result to the
    * LVG desinated outputs, screen ot a file.
    *
    * @param  results   Lvg output records before going through output filter
    * @param  mutateFlag   a boolean flag for showing the result with mutate 
    *         information
    * @param  detailsFlag   a boolean flag for showing the result with detail
    *         information of flow operation
    * @param  separator   a replacement separator other than the Lvg default
    *         separator
    * @param  originalTerm  the original input term (the whole line).
    * @param  inTerm  the input term for lvg mutation
    * @param  lvgOutputOption  the lvg output filter option object.
    *
    * @return  result in String
    * 
    */
    public static String ExecuteOutputFilter(Vector<LexItem> results,
        boolean mutateFlag, boolean detailsFlag, String separator,
        String originalTerm, String inTerm, LvgOutputOption lvgOutputOption)
    {
        String outStr = new String();
        StringBuffer buffer = new StringBuffer();
        try
        {
            // combine result first if combine option is chosen
            Vector<LexItem> combinedResults = CombineRecords.Combine(results, 
                lvgOutputOption.GetCombineRule());
            // sort output by output term
            if(lvgOutputOption.GetSortFlag() != LexItemComparator.NONE)
            {
                LexItemComparator<LexItem> lc 
                    = new LexItemComparator<LexItem>();
                lc.SetRule(lvgOutputOption.GetSortFlag());
                Collections.sort(combinedResults, lc);
            }
            // Output filter options: -DC, -DI, -EC, -EI
            int recNum = 0;
            long excCat = Bit.Minus(Category.ALL_BIT_VALUE,
                lvgOutputOption.GetExcludeCategory());
            long excInfl = Bit.Minus(Inflection.ALL_BIT_VALUE,
                lvgOutputOption.GetExcludeInflection());
            // get the final results
            Vector<LexItem> finalResults = new Vector<LexItem>();    
            for(int i = 0; i < combinedResults.size(); i++)
            {
                LexItem temp = combinedResults.elementAt(i);
                // check option -DC, -DI
                if(IsRecContainCategoryInflection(temp, 
                    lvgOutputOption.GetOutCategory(), 
                    lvgOutputOption.GetOutInflection()) == false)
                {
                    continue;
                }
                // check option -EC, -EI
                if(IsCategoryInflectionContainRec(temp, excCat, excInfl) 
                    == false)
                {
                    continue;
                }
                finalResults.add(temp);
            }
            // Check if no output - no output message
            if(finalResults.size() == 0)        // No output options
            {
                if(lvgOutputOption.GetNoOutputFlag() == true)
                {
                    String out = originalTerm;
                    if(lvgOutputOption.GetFilterInputFlag() == true)
                    {
                        out = inTerm;
                    }
                    String resultStr = out + separator 
                        + lvgOutputOption.GetNoOutputStr();
                    // -F: set output according to the specified field number
                    String filteredStr = FieldFilter(resultStr, 
                        lvgOutputOption.GetOutputFieldList());
                    buffer.append(filteredStr);
                    buffer.append(
                        System.getProperty("line.separator").toString());
                }
            }
            // if the size is not 0
            for(int i = 0; i < finalResults.size(); i++)
            {
                LexItem temp = finalResults.elementAt(i);
                // -m,  -C, -SC, -SI, -ti, -s: get the basic outputs
                String resultStr = LexItem.GetResultString(temp,
                    originalTerm, mutateFlag, lvgOutputOption.GetCaseFlag(), 
                    lvgOutputOption.GetShowCategoryStrFlag(),
                    lvgOutputOption.GetShowInflectionStrFlag(),
                    lvgOutputOption.GetFilterInputFlag(), separator);
                // -F: set output according to the specified field number
                String filteredStr = FieldFilter(resultStr, 
                    lvgOutputOption.GetOutputFieldList());
                // printout the results
                buffer.append(filteredStr);
                buffer.append(GlobalBehavior.LS_STR);
                recNum++;
                // -d: print details
                if(detailsFlag == true)
                {
                    buffer.append(temp.GetDetailInformation());
                    buffer.append(GlobalBehavior.LS_STR);
                }
                // -R:INT restrict the number of output records
                if((lvgOutputOption.GetOutRecordNum() > 0) 
                && (recNum >= lvgOutputOption.GetOutRecordNum()))
                {
                    break;
                }
            }
        
            // -ccgi: Mark the end of the set of variants returned
            if(lvgOutputOption.GetMarkEndFlag() == true)
            {
                String endMarkStr = lvgOutputOption.GetMarkEndStr();
                buffer.append(endMarkStr);
                buffer.append(GlobalBehavior.LS_STR);
            }
        }
        catch (Exception e)
        {
            System.err.println("** Error in ExecuteOutoutFilter( ): " 
                + e.toString()); 
        }
        outStr = buffer.toString();
        return outStr;
    }
    /**
    * Simplify lexItems by dropping target inflections with value greater than 
    * 256 (positive)   
    *
    * @param  ins  a vector of LexItems to be simplified
    *
    * @return  Vector<LexItem> LexItems with inflection values less than 256.  
    *          An empty Vector<LexItem> is returned if no such LexItem exist.  
    */
    public static Vector<LexItem> GetSimpleInflection(Vector<LexItem> ins)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem in = ins.elementAt(i);
            LexItem out = GetSimpleInflection(in); 
            if(out != null)
            {
                outs.addElement(out);
            }
        }
        return outs;
    }
    /**
    * Simplify lexItems by dropping target inflections with value greater than 
    * 256 (positive) except for category is modal or auxiliary.
    *
    * @param  ins  a vector of LexItems to be simplified
    *
    * @return  Vector<LexItem> of LexItems with inflection values less than 
    *          256 or it's category is auxiliary or modal.
    *          An empty Vector<LexItem> is returned if no such LexItem exist.  
    */
    public static Vector<LexItem> GetEnhancedSimpleInflection(
        Vector<LexItem> ins)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem in = ins.elementAt(i);
            LexItem out = GetEnhancedSimpleInflection(in); 
            if(out != null)
            {
                outs.addElement(out);
            }
        }
        return outs;
    }
    /**
    * Simplify an lexItem by dropping target inflections with value greater than
    * 256 (positive)   
    *
    * @param  in  the LexItem to be simplified
    *
    * @return  an LexItem with inflection values less than 256.  Null is 
    *          returned if no such value exist.  If the value of inflection 
    *          covers range accross 256, only those inflections greater than 
    *          256 are dropped.
    */
    public static LexItem GetSimpleInflection(LexItem in)
    {
        // simplified inflections 
        Inflection infl = in.GetTargetInflection();
        long inflValue = 
            Bit.Minus(infl.GetValue(), Inflection.UPPER_INFLECTIONS);
        LexItem out = null;
        if(inflValue > 0)
        {
            out = new LexItem(in, true);
            out.SetTargetInflection(inflValue);
        }
        return out;
    }
    /**
    * Simplify an lexItem by dropping target inflections with value greater than
    * 256 (positive) except for it's category is auxiliary or modal. 
    *
    * @param  in  the LexItem to be simplified
    *
    * @return  an LexItem with inflection values less than 256.  Null is 
    *          returned if no such value exist.  If the value of inflection 
    *          covers range accross 256, only those inflections greater than 
    *          256 are dropped except for it's category is modal or auxiliary.
    */
    public static LexItem GetEnhancedSimpleInflection(LexItem in)
    {
        // simplified inflections 
        Inflection infl = in.GetTargetInflection();
        long inflValue = 
            Bit.Minus(infl.GetValue(), Inflection.UPPER_INFLECTIONS);
        LexItem out = null;
        long cat = in.GetTargetCategory().GetValue();
        if((inflValue > 0)
        || (cat == Category.ToValue("aux"))
        || (cat == Category.ToValue("modal")))
        {
            out = new LexItem(in, true);
            out.SetTargetInflection(inflValue);
        }
        return out;
    }
    /**
    * Checks if an LexItem contains a specified category.
    *
    * @param  in  the LexItem to be chekced
    * @param  category  the specified category in a long integer format
    *
    * @return  true or false to represents the in LexItem contains
    *          the specified category or not.
    */
    public static boolean IsRecContainCategory(LexItem in, long category)
    {
        boolean containFlag = 
            Bit.Contain(in.GetTargetCategory().GetValue(), category);
        return containFlag;
    }
    /**
    * Checks if the category of an LexItem is contained in a specified category.
    *
    * @param  in  the LexItem to be chekced
    * @param  category  the specified category in a long integer format
    *
    * @return  true or false to represents the cateogry of an LexItem is 
    *          contained in the specified category or not.
    */
    public static boolean IsCategoryContainRec(LexItem in, long category)
    {
        // return orginal str if no special category
        if(category == Category.ALL_BIT_VALUE)
        {
            return true;
        }
        boolean containFlag = 
            Bit.Contain(category, in.GetTargetCategory().GetValue());
        return containFlag;
    }
    /**
    * Checks if an LexItem contains specified category and inflection.
    *
    * @param  in  the LexItem to be chekced
    * @param  category  the specified category in a long integer format
    * @param  inflection  the specified inflection in long integer format
    *
    * @return  true or false to represents the in LexItem contains
    *          the specified category and inflection or not.
    */
    public static boolean IsRecContainCategoryInflection(LexItem in, 
        long category, long inflection)
    {
        // catFlag is true when no restriction or contains
        boolean catFlag = (category == Category.ALL_BIT_VALUE) ||
            Bit.Contain(in.GetTargetCategory().GetValue(), category);
        // inflFlag is true when no restriction or contains
        boolean inflFlag = (inflection == Inflection.ALL_BIT_VALUE) ||
            Bit.Contain(in.GetTargetInflection().GetValue(), inflection);
        boolean containFlag = catFlag && inflFlag;
        return containFlag;
    }
    /**
    * Checks if the category and inflection of an LexItem are contained in the 
    * specified category and inflection.
    *
    * @param  in  the LexItem to be chekced
    * @param  category  the specified category in a long integer format
    * @param  inflection  the specified inflection in long integer format
    *
    * @return  true or false to represents the specified category and
    *          inflection contain the category and inflection of the 
    *          in LexItem or not.
    */
    public static boolean IsCategoryInflectionContainRec(LexItem in, 
        long category, long inflection)
    {
        // catFlag is true when no restriction or contains
        boolean catFlag = (category == Category.ALL_BIT_VALUE) ||
            Bit.Contain(category, in.GetTargetCategory().GetValue());
        // inflFlag is true when no restriction or contains
        boolean inflFlag = (inflection == Inflection.ALL_BIT_VALUE) ||
            Bit.Contain(inflection, in.GetTargetInflection().GetValue());
        boolean containFlag = catFlag && inflFlag;
        return containFlag;
    }
    /**
    * Restrict option used for inflections and derivations.
    *
    * @param  facts  results come from lexicon facts
    * @param  rules  results come from morphology trie
    * @param  restrictFlag  a flag for the restriction
    *
    * @return  a vector composed of facts and rules based on the restriction
    * flag
    */
    public static Vector<LexItem> RestrictOption(Vector<LexItem> facts, 
        Vector<LexItem> rules, int restrictFlag)
    {
        Vector<LexItem> out = new Vector<LexItem>();
        switch(restrictFlag)
        {
            case OutputFilter.LVG_ONLY:
                out.addAll(facts);
                break;
            case OutputFilter.ALL:
                out.addAll(facts);
                out.addAll(rules);
                break;
            case OutputFilter.LVG_OR_ALL:
                out.addAll(facts);
                if(out.size() == 0)
                {
                    out.addAll(rules);
                }
                break;
        }
        return out;
    }
    // private methods
    // modify case
    private static String ModifyCase(String inStr, int caseFlag)
    {
        String out = inStr;
        switch(caseFlag)
        {
            case OutputFilter.LOWERCASE:
                out = inStr.toLowerCase();
                break;
            case OutputFilter.UPPERCASE:
                out = inStr.toUpperCase();
                break;
        }
        return out;
    }
    // This method filter out fields that is not in the FieldList.
    private static String FieldFilter(String inStr, Vector<Integer> fieldList)
    {
        // use fields API for the field filter function
        String fs = GlobalBehavior.GetFieldSeparator();
        return FieldsApi.Mutate(inStr, fs, fieldList);
    }
    // data members
    /** Enumerated type for not a derivation: for future use */
    public final static int NOT_DERIVATION = 0;      // not a derivation
    /** Enumerated type for restrict to LVG: used in inflection and  
    derivation flow components */
    public final static int LVG_ONLY = 1;      // facts: restrict to LVG
    /** Enumerated type for all if no LVG exist: used in inflection and 
    derivation components */
    public final static int LVG_OR_ALL = 2;      // facts, then rules
    /** Enumerated type for no restriction: all.  It is used in  inflection
    and derivation flow components */
    public final static int ALL = 3;      // no restriction: fact & rules
    /** Enumerated type for output case */
    public final static int PRESERVED_CASE = 1;      // preserved case
    public final static int LOWERCASE = 2;           // lower case
    public final static int UPPERCASE = 3;           // upper case
    /** Enumerated type for derivation type */
    public final static int D_TYPE_ZERO = 0;      // zeroD only
    public final static int D_TYPE_PREFIX = 1;    // prefixD only
    public final static int D_TYPE_SUFFIX = 2;    // suffixD only
    public final static int D_TYPE_ZERO_PREFIX = 3;    // zeroD & prefixD
    public final static int D_TYPE_ZERO_SUFFIX = 4;    // zeroD & suffixD
    public final static int D_TYPE_SUFFIX_PREFIX = 5;    // suffixD & prefixD
    public final static int D_TYPE_ALL = 6;    // all: zeroD, prefixD, suffixD
    /** Enumerated type for derivation negation */
    public final static int D_NEGATION_OTHERWISE = 0; // not negative, otherwise
    public final static int D_NEGATION_NEGATIVE = 1;  // negative derivation
    public final static int D_NEGATION_BOTH = 2;     // both negative and others
    // private data member
    protected final static String HEAD = "<";
    protected final static String TAIL = ">";
}
