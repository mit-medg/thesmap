package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides key for LexItem using term, category, and inflections
* This overrides the hashcode() and equals() for using hashtable and hashSet.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class TermCatInflKey
{
    // public constructor
    /**
    * Create a key object, using a specified term, category, inflection
    */
    public TermCatInflKey(String term, int category, long inflection)
    {
        term_ = term;
        category_ = category;
        inflection_ = inflection;
    }
    /**
    * Get the term of this object
    *
    * @return term in String format
    */
    public String GetTerm()
    {
        return term_;
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
        if((anObject != null) && (anObject instanceof TermCatInflKey))
        {
            TermCatInflKey temp = (TermCatInflKey) anObject;
            return 
                ((term_.equals(temp.GetTerm())) && 
                 (category_ == temp.GetCategory()) &&
                 (inflection_ == temp.GetInflection()));
        }
        else
        {
            return false;
        }
    }
    private String term_ = null;
    private int category_ = (int) Category.NO_BIT_VALUE;
    private long inflection_ = Inflection.NO_BIT_VALUE;
}
