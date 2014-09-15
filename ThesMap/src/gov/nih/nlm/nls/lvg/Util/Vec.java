package gov.nih.nlm.nls.lvg.Util;
import java.util.*;
/*****************************************************************************
* This class represents Vector related methods.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Vec
{
    // public methods
    /*
    * Transfer from  Vector<Long> to an array of longs.
    *
    * @param   inList  Vector<Long>
    *
    * @return an array of longs
    */
    public static long[] ToArray(Vector<Long> inList)
    {
        int size = inList.size();
        long[] outs = new long[size];
        for(int i = 0; i < size; i++)
        {
            outs[i] = (inList.elementAt(i)).longValue();
        }
        return outs;
    }
}
