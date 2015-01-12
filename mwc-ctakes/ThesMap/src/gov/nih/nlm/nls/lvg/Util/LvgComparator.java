package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/*****************************************************************************
* This class provides a LVG comparator, which compares length, then case 
* insensitive ASCII.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LvgComparator<T> implements Comparator<T>
{
    // public methods
    /**
    * Compare two objects by LVG order (length and insentitive ASCII).
    *
    * @param   o1  the first object to be compared
    * @param   o2  the second object to be compared
    *
    * @return  a negative integer, zero, or a positive integer as the first 
    *          argument is less than, equal to, or greater than the second.
    */
    public int compare(T o1, T o2)
    {
        // length sensitive: compare length first
        if(lengthFlag_ == true)
        {
            int length1 = ((String) o1).length();
            int length2 = ((String) o2).length();
            if(length1 != length2)
            {
                return (length1-length2);
            }
        }
        // case non-sensitive: compare letter, ignore case
        if(caseFlag_ == false)
        {
            String str1 = ((String) o1).toLowerCase();
            String str2 = ((String) o2).toLowerCase();
            return str1.compareTo(str2);
        }
        // case sensititve: compare by ASCII
        String str1 = (String) o1;
        String str2 = (String) o2;
        return str1.compareTo(str2);
    }
    /**
    * Set the flag of comparing string length in this comparator.
    *
    * @param   value  true or false if compare length or not in the comparator
    */
    public void SetLengthFlag(boolean value)
    {
        lengthFlag_ = value;
    }
    /**
    * Set the flag of comparing cases in this comparator.
    *
    * @param   value  true or false if compare is or is not case sensitive
    */
    public void SetCase(boolean value)
    {
        caseFlag_ = value;
    }
    // data members
    private boolean lengthFlag_ = false;            // length sort
    private boolean caseFlag_ = true;               // case sensitive sort
}
