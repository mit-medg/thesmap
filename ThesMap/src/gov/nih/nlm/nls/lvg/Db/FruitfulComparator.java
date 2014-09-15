package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare fruitful records.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        FruitfulRecord
*
* @version    V-2013
****************************************************************************/
public class FruitfulComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are FruitfulRecord. 
    * The compare algorithm uses distance and then alphabetic order
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
        int dist1 = ((FruitfulRecord) o1).GetDistance();
        int dist2 = ((FruitfulRecord) o2).GetDistance();
        if(dist1 != dist2)
        {
            return (dist1-dist2);
        }
        String str1 = ((FruitfulRecord) o1).GetVariantTerm();
        String str2 = ((FruitfulRecord) o2).GetVariantTerm();
        // use alphabetic order
        return str1.compareTo(str2);
    }
}
