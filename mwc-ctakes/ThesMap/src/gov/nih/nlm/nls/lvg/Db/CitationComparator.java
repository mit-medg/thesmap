package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides methods to compare citation in inflection records.
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
public class CitationComparator<T> implements Comparator<T>
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
        // Use dictionary order
        int length1 = (((InflectionRecord) o1).GetCitationTerm()).length();
        int length2 = (((InflectionRecord) o2).GetCitationTerm()).length();
        if(length1 != length2)        // different length
        {
            return (length1-length2);
        }
        // Same category, same length, use alphabetic order
        String str1 = ((InflectionRecord)o1).GetCitationTerm();
        String str2 = ((InflectionRecord)o2).GetCitationTerm();
        return str1.compareTo(str2);
    }
}
