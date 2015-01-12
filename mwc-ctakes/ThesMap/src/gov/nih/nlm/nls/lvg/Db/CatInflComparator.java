package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare inflection records by category
* and inflection.
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
public class CatInflComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are 
    * InflectionRecord. 
    *
    * The compare algorithm compare the category, inflection, eui
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
        int c1 = ((InflectionRecord) o1).GetCategory();
        int c2 = ((InflectionRecord) o2).GetCategory();
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // Same category, compare infelction
        long i1 = ((InflectionRecord) o1).GetInflection();
        long i2 = ((InflectionRecord) o2).GetInflection();
        if(i1 > i2)
        {
            return 1;
        }
        else if(i1 < i2)
        {
            return -1;
        }
        // Same category, same inflection, use eui
        String e1 = ((InflectionRecord)o1).GetEui();
        String e2 = ((InflectionRecord)o2).GetEui();
        return e1.compareTo(e2);
    }
}
