package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare nominalization records by alphabetical
* order, category, and eui.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        NominalizationRecord
*
* @version    V-2013
****************************************************************************/
public class NominalizationComparator<T> implements Comparator<T>
{
    /**
    * Compare two object o1 and o2.  Both objects o1 and o2 are 
    * InflectionRecord. 
    *
    * The compare algorithm compare alphabetical order, category, eui. 
    * eui
    *
    * @param  o1  first object to be compared
    * @param  o2  second object to be compared
    *
    * @return  a negative integer, 0, or positive integer to represent the
    *          object o1 is less, equals, or greater than object 02.
    */
    public int compare(T o1, T o2)
    {
        // compare alphabetical order
        String s1 = 
            (((NominalizationRecord)o1).GetNominalization2()).toLowerCase();
        String s2 = 
            (((NominalizationRecord)o2).GetNominalization2()).toLowerCase();
        if(s1.equalsIgnoreCase(s2) == false)
        {
            return s1.compareTo(s2);
        }
        // compare category
        int c1 = ((NominalizationRecord) o1).GetCat2();
        int c2 = ((NominalizationRecord) o2).GetCat2();
        if(c1 != c2)        // different category
        {
            return (c1-c2);
        }
        // compare eui
        String e1 = ((NominalizationRecord)o1).GetEui2();
        String e2 = ((NominalizationRecord)o2).GetEui2();
        return e1.compareTo(e2);
    }
}
