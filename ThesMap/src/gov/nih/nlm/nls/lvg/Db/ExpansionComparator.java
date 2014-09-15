package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare expansion records.
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
public class ExpansionComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are AcronymRecord. 
    * In LVG database, acronym and expansion are keep together in a table.
    * The data structure of this record is called AcronymRecord and can be 
    * used for both expansion and acronym.
    *
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
        // lower case
        String str1 = ((AcronymRecord) o1).GetExpansion().toLowerCase();
        String str2 = ((AcronymRecord) o2).GetExpansion().toLowerCase();
        // use alphabetic order
        return str1.compareTo(str2);
    }
}
