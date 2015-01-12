package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare inflection records.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see        InflectionRecord
*
* @version    V-2013
****************************************************************************/
public class UninflectionComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are 
    * InflectionRecord. 
    *
    * The compare algorithm compare the priority (frequency) of category,
    * length, ignores cases, and uses alphabetic order
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
        int c1 = GetCategoryPriority(((InflectionRecord) o1).GetCategory()); 
        int c2 = GetCategoryPriority(((InflectionRecord) o2).GetCategory()); 
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // Same category, Use dictionary order
        int length1 = (((InflectionRecord) o1).GetUninflectedTerm()).length();
        int length2 = (((InflectionRecord) o2).GetUninflectedTerm()).length();
        if(length1 != length2)        // different length
        {
            return (length1-length2);
        }
        // Same category, same length, use alphabetic order
        String str1 = 
            (((InflectionRecord)o1).GetUninflectedTerm()).toLowerCase();
        String str2 = 
            (((InflectionRecord)o2).GetUninflectedTerm()).toLowerCase();
        return str1.compareTo(str2);
    }
    // Private method
    private int GetCategoryPriority(int category)
    {
        int priority = 5;
        if(category == Category.ToValue("noun"))        // noun
        {
            priority = 1;
        }
        else if(category == Category.ToValue("adj"))        // adj
        {
            priority = 2;
        }
        else if(category == Category.ToValue("verb"))        // verb
        {
            priority = 3;
        }
        else if(category == Category.ToValue("adv"))        // adv
        {
            priority = 4;
        }
        return priority;
    }
}
