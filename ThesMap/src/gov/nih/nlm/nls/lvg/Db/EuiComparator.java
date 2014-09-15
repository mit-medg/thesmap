package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare Eui records.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        EuiRecord
*
* @version    V-2013
****************************************************************************/
public class EuiComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are EuiRecord. 
    * The compare algorithm uses alphabetic order
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // alphabetical order
        String str1 = ((EuiRecord) o1).GetEui();
        String str2 = ((EuiRecord) o2).GetEui();
        return str1.compareTo(str2);
    }
}
