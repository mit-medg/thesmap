package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/****************************************************************************
* This class provides a case insensitive alphabetical comparator.
* It is a comparison function, which imposes a total ordering on some 
* collection of objects.
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class AlphaBeticalComparator<T> implements Comparator<T>
{
    /**
    * Compare two string objects alphabetically
    *
    * @param   o1  the first string object to be compared
    * @param   o2  the second string object fto be compared
    *
    * @return  a negative integer, zero, or a positive integer as the first 
    *          argument is less than, equal to, or greater than the second.
    */
    public int compare(Object o1, Object o2)
    {
        String str1 = ((String) o1).toLowerCase();
        String str2 = ((String) o2).toLowerCase();
        return str1.compareTo(str2);
    }
}
