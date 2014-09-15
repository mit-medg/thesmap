package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
/*****************************************************************************
* This class provides methods to compare rule results.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        RuleResult
*
* @version    V-2013
****************************************************************************/
public class RuleResultComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are RuleResult.
    * The compare algorithm uses category, result length, ignores cases,
    * and uses alphabetic order
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // Sort by Category first
        int c1 = GetCategoryPreority(((RuleResult) o1).GetOutCategory()); 
        int c2 = GetCategoryPreority(((RuleResult) o2).GetOutCategory()); 
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // Same category, Use dictionary order
        int length1 = (((RuleResult) o1).GetOutTerm()).length();
        int length2 = (((RuleResult) o2).GetOutTerm()).length();
        if(length1 != length2)        // different length
        {
            return (length1-length2);
        }
        // Same category, same length, use alphabetic order
        String str1 = (((RuleResult) o1).GetOutTerm()).toLowerCase();
        String str2 = (((RuleResult) o2).GetOutTerm()).toLowerCase();
        return str1.compareTo(str2);
    }
    // Private method
    private int GetCategoryPreority(String categoryStr)
    {
        int priority = 5;
        if(categoryStr.equals("noun"))
        {
            priority = 1;
        }
        else if(categoryStr.equals("adj"))
        {
            priority = 2;
        }
        else if(categoryStr.equals("verb"))
        {
            priority = 3;
        }
        else if(categoryStr.equals("adv"))
        {
            priority = 4;
        }
        return priority;
    }
}
