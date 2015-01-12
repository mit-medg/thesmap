package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
/*****************************************************************************
* This class provides a special Collection Vector<InflectionRecord> 
* for inflection records.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see        InflectionRecord
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class InflectionVector<E> extends Vector<E> 
{
    // public methods
    /**
    * Check if this collection Vector<InflectionRecord> contains a specified EUI
    *
    * @param  eui  an EUI in a String format
    *
    * @return  true or false if this collection vector does or does not
    * contain the specified EUI
    */
    public boolean ContainEui(String eui)
    {
        boolean hasEui = false;
        for(int i = 0; i < size(); i++)
        {
            InflectionRecord inflectionRecord = (InflectionRecord) elementAt(i);
            if(inflectionRecord.GetEui().equals(eui) == true)
            {
                hasEui = true;
                break;
            }
        }
        return hasEui;
    }
    private final static long serialVersionUID = 5L;
}
