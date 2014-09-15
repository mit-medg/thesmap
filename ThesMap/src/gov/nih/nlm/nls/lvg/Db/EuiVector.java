package gov.nih.nlm.nls.lvg.Db;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides a special Collection Vector<EuiRecord> for eui records.
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
public class EuiVector<E> extends Vector<E> 
{
    // public methods
    /**
    * Add a new EuiRecord into EuiVector.  Combine categories and inflections
    * if they are same Eui
    *
    */
    protected void Add(E obj)
    {
        EuiRecord newRec = (EuiRecord) obj; 
        boolean existFlag = false;
        for(int i = 0; i < size(); i++)
        {
            EuiRecord rec = (EuiRecord) elementAt(i);
            if(rec.GetEui().equals(newRec.GetEui()) == true)
            {
                int cat = Bit.Add(rec.GetCategory(), newRec.GetCategory());
                long infl = 
                    Bit.Add(rec.GetInflection(), newRec.GetInflection());
                rec.SetCategory(cat);
                rec.SetInflection(infl);
                existFlag = true;
                break;
            }
        }
        if(existFlag == false)
        {
            addElement(obj);
        }
    }
    private final static long serialVersionUID = 5L;
}
