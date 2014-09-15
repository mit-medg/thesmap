package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides key using category and inflection for LexItem.  
* This overrides the hashcode() and equals() for using hashtable and hashSet.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class CatInflKey
{
    // public constructor
    /**
    * Create a key object, using a specified category and inflection
    */
    public CatInflKey(int category, long inflection)
    {
        category_ = category;
        inflection_ = inflection;
    }
    /**
    * Get the category value of this object
    *
    * @return category value
    */
    public int GetCategory()
    {
        return category_;
    }
    /**
    * Get the inflection value of this object
    *
    * @return inflection value
    */
    public long GetInflection()
    {
        return inflection_;
    }
    public int hashCode()
    {
        return category_;
    }
    public boolean equals(Object anObject)
    {
        if((anObject != null) && (anObject instanceof CatInflKey))
        {
            CatInflKey temp = (CatInflKey) anObject;
            return 
                ((category_ == temp.GetCategory())
                && (inflection_ == temp.GetInflection()));
        }
        else
        {
            return false;
        }
    }
    private int category_ = (int) Category.NO_BIT_VALUE;
    private long inflection_ = Inflection.NO_BIT_VALUE;
}
