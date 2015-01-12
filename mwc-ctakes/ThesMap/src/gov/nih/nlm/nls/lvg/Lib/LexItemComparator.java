package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class performs a comparator for LexItems.
* This class is used with CombinedRecord for output filter
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LexItemComparator<T> implements Comparator<T>
{
    
    /**
    * To compare two object o1 and o2.  Both objects o1 and o2 are LexItems.
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the 
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        int out = 0;
        switch(rule_)
        {
            case TERM:
            case ALPHABETIC:
                out = CompareByAlphaBetic(o1, o2);
                break;
            case TERM_CAT:
                out = CompareByTermCat(o1, o2);
                break;
            case TERM_CAT_INFL:
                out = CompareByTermCatInfl(o1, o2);
                break;
            case LVG_RULE:
                out = CompareByLvg(o1, o2);
                break;
            default:
                out = CompareForCombine(o1, o2);
                break;
        }
        return out;
    }
    /**
    * Set the comparing rule for this comparator. 
    *
    * @param  rule  an enumurated value. Such as Lvg.LexItemComparator.LVG_RULE 
    *         means to use lvg principle to compare object.  Otherwise, use
    *         compartor for combining records.
    */
    public void SetRule(int rule)
    {
        rule_ = rule;
    }
    /**
    * Set the .g for case sensitivity 
    *
    * @param  caseFlag  flag for case sensitivity 
    */
    public void SetCase(boolean caseFlag)
    {
        caseFlag_ = caseFlag;
    }
    /**
    * Get the priority of ordering for a category.
    *
    * @param  category  a long integer value of a category  
    *
    * @return the priority of the given category.  The priority is from 1 to 12.
    *         The lower value of priority, such as 1, means it shows up and is
    *         used more often.
    */
    public static int GetCategoryPriority(long category)
    {
        int priority = 12;
        int catBit = Category.GetBitIndex(category);
        if(catBit> 0)
        {
            priority = catPriority_[catBit];
        }
        return priority;
    }
    //private methods
    // compare alphebetic of target term.  
    private int CompareByAlphaBetic(T o1, T o2)
    {
        LexItem item1 = (LexItem) o1;
        LexItem item2 = (LexItem) o2;
        // compare the output term first, if same compare the rest
        lc_.SetLengthFlag(false);
        lc_.SetCase(caseFlag_);
        int out = lc_.compare(item1.GetTargetTerm(), item2.GetTargetTerm());
        return out;
    }
    private int CompareByTermCat(T o1, T o2)
    {
        LexItem item1 = (LexItem) o1;
        LexItem item2 = (LexItem) o2;
        // compare the output term first, if same, compare the rest
        lc_.SetLengthFlag(false);
        lc_.SetCase(caseFlag_);
        int out = lc_.compare(item1.GetTargetTerm(), item2.GetTargetTerm());
        // Compare the category
        if(out == 0)
        {
            out = (int)(item1.GetTargetCategory().GetValue() -
                item2.GetTargetCategory().GetValue());
        }
        return out;
    }
    private int CompareByTermCatInfl(T o1, T o2)
    {
        LexItem item1 = (LexItem) o1;
        LexItem item2 = (LexItem) o2;
        // compare the output term first, if same, compare the rest
        lc_.SetLengthFlag(false);
        lc_.SetCase(caseFlag_);
        int out = lc_.compare(item1.GetTargetTerm(), item2.GetTargetTerm());
        // Compare the category
        if(out == 0)
        {
            out = (int)(item1.GetTargetCategory().GetValue() -
                item2.GetTargetCategory().GetValue());
            if(out == 0)
            {
                long temp = item1.GetTargetInflection().GetValue() -
                    item2.GetTargetInflection().GetValue();
                if(temp > 0)
                {
                    out = 1;
                }
                else if(temp < 0)
                {
                    out = -1;
                }
                else
                {
                    out = 0;
                }
            }
        }
        return out;
    }
    // compare tager category, size, alphebetic, of target term.
    private int CompareByLvg(T o1, T o2)
    {
        LexItem item1 = (LexItem) o1;
        LexItem item2 = (LexItem) o2;
        // Sort by Category first
        int c1 = GetCategoryPriority(item1.GetTargetCategory().GetValue());
        int c2 = GetCategoryPriority(item2.GetTargetCategory().GetValue());
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // compare length, case insentivie compare
        lc_.SetLengthFlag(true);
        lc_.SetCase(false);
        int out = lc_.compare(item1.GetTargetTerm(), item2.GetTargetTerm());
        return out;
    }
    // compare size, alphebetic, of target term.  Then by EUI or category,
    // or inflection
    private int CompareForCombine(T o1, T o2)
    {
        LexItem item1 = (LexItem) o1;
        LexItem item2 = (LexItem) o2;
        // don't compare anything
        int out = -1;    // compare by none
        if(rule_ == CombineRecords.BY_NONE)
        {
            return out;
        }
        // compare the output term first, if same compare the rest
        lc_.SetLengthFlag(true);
        lc_.SetCase(caseFlag_);
        out = lc_.compare(item1.GetTargetTerm(), item2.GetTargetTerm());
        if(out == 0)
        {
            switch(rule_)
            {
                case CombineRecords.BY_EUI:            // compare EUI
                    out = lc_.compare(item1.GetMutateInformation(),
                        item2.GetMutateInformation());
                    break;
                case CombineRecords.BY_CATEGORY:       // compare category
                    int c1 = GetCategoryPriority(item1.GetTargetCategory().
                        GetValue());
                    int c2 = GetCategoryPriority(item2.GetTargetCategory().
                        GetValue());
                    out = c1-c2;
                    break;
                case CombineRecords.BY_INFLECTION:        // compare inflection
                    out = lc_.compare(
                        String.valueOf(item1.GetTargetInflection().GetValue()),
                        String.valueOf(item2.GetTargetInflection().GetValue()));
                    break;
            }
        }
        // default-BY_NONE: return a negative number
        return out;
    }
    // data members
    /** an enumerated type for setting the comparing rule by LVG principle */ 
    public final static int NONE          = 0;
    public final static int TERM          = 1;
    public final static int TERM_CAT      = 2;
    public final static int TERM_CAT_INFL = 3;
    public final static int LVG_RULE       = 10;
    public final static int ALPHABETIC     = 11;
    // private data member
    private int rule_ = CombineRecords.BY_NONE;
    private boolean caseFlag_ = true;               // true for case sensitive
    private LvgComparator<String> lc_ = new LvgComparator<String>();
    private static int[] catPriority_ = new int[Category.TOTAL_BITS];
    static
    {
        catPriority_[Category.NOUN_BIT] = 1;
        catPriority_[Category.ADJ_BIT] = 2;
        catPriority_[Category.VERB_BIT] = 3;
        catPriority_[Category.ADV_BIT] = 4;
        catPriority_[Category.PREP_BIT] = 5;
        catPriority_[Category.PRON_BIT] = 6;
        catPriority_[Category.CONJ_BIT] = 7;
        catPriority_[Category.DET_BIT] = 8;
        catPriority_[Category.MODAL_BIT] = 9;
        catPriority_[Category.AUX_BIT] = 10;
        catPriority_[Category.COMPL_BIT] = 11;
    }
}
