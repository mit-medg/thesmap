package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare spelling variant (inflection records).
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        InflectionRecord
*
* @version    V-2013
****************************************************************************/
public class SpellingVarComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are 
    * InflectionRecord. 
    *
    * The compare algorithm compare by alphabetic order, and 
    * priority (frequency) of category, inflection, eui
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // Same category, same length, use alphabetic order
        String str1 = ((InflectionRecord)o1).GetUninflectedTerm();
        String str2 = ((InflectionRecord)o2).GetUninflectedTerm();
        if(str1.equals(str2) == false)
        {
            return str1.compareTo(str2);
        }
        // Sort by Category first
        int c1 = LexItemComparator.GetCategoryPriority(((InflectionRecord) o1).
            GetCategory()); 
        int c2 = LexItemComparator.GetCategoryPriority(((InflectionRecord) o2).
            GetCategory()); 
        if(c1 != c2)
        {
            return (c1-c2);
        }
        long infl1 = ((InflectionRecord)o1).GetInflection();
        long infl2 = ((InflectionRecord)o2).GetInflection();
        if(infl1 > infl2)
        {
            return 1;
        }
        else if(infl1 < infl2)
        {
            return -1;
        }
        String e1 = ((InflectionRecord)o1).GetEui();
        String e2 = ((InflectionRecord)o2).GetEui();
        return e1.compareTo(e2);
    }
}
