package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare acronym records.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        AcronymRecord
*
* @version    V-2013
****************************************************************************/
public class AcronymComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are AcronymRecord. 
    * The compare algorithm ignores cases and uses alphabetic order
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // ingore case
        String str1 = ((AcronymRecord) o1).GetAcronym().toLowerCase();
        String str2 = ((AcronymRecord) o2).GetAcronym().toLowerCase();
        // use alphabetic order
        return str1.compareTo(str2);
    }
}
