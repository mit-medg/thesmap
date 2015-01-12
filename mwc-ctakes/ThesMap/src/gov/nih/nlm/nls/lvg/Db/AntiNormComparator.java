package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare AntiNorm records.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        AntiNormRecord
*
* @version    V-2013
****************************************************************************/
public class AntiNormComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are AntiNormRecord. 
    * The compare algorithm uses alphabetic order, eui, category, inflection
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // ure inflected term by alphabetic order
        String str1 = ((AntiNormRecord) o1).GetInflectedTerm();
        String str2 = ((AntiNormRecord) o2).GetInflectedTerm();
        if(str1.equals(str2) == false)
        {
            return str1.compareTo(str2);
        }
        // compare eui
        String eui1 = ((AntiNormRecord) o1).GetEui();
        String eui2 = ((AntiNormRecord) o2).GetEui();
        if(eui1.equals(eui2) == false)
        {
            return eui1.compareTo(eui2);
        }
        // compare cat
        int cat1 = ((AntiNormRecord) o1).GetCategory();
        int cat2 = ((AntiNormRecord) o2).GetCategory();
        if(cat1 != cat2)
        {
            return (cat1-cat2);
        }
        // compare inflection
        long infl1 = ((AntiNormRecord) o1).GetInflection();
        long infl2 = ((AntiNormRecord) o2).GetInflection();
        long comp =  infl1-infl2;
        if(comp > 0)
        {
            return 1;
        }
        else if(comp < 0)
        {
            return -1;
        }
        return 0;
    }
}
