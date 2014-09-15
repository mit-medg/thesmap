package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare derivation records.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class DerivationComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are 
    * DerivationRecords
    * The compare algorithm compare the priority (frequency) of category,
    * length, ignores cases, and uses alphabetic order
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(Object o1, Object o2)
    {
        // Sort by Category first
        int c1 = LexItemComparator.GetCategoryPriority(((DerivationRecord) o1).
            GetTargetCat());
        int c2 = LexItemComparator.GetCategoryPriority(((DerivationRecord) o2).
            GetTargetCat());
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // Same category, Use length
        // Use dictionary order
        String str1 = ((DerivationRecord) o1).GetTarget();
        String str2 = ((DerivationRecord) o2).GetTarget();
        int length1 = str1.length();
        int length2 = str2.length();
        if(length1 != length2)        // different length
        {
            return (length1-length2);
        }
        // same length, use alphabetic order
        return str1.toLowerCase().compareTo(str2.toLowerCase());
    }
}
