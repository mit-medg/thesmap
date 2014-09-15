package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides key for derivational LexItem used in recursively 
* derivational flow.  This overrides the hashcode() and equals() for using 
* hashtable and hashSet.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class TermCatKey
{
    // public constructor
    /**
    * Create a key object, using a specified term and category
    */
    public TermCatKey(String term, int category)
    {
        term_ = term;
        category_ = category;
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
    public int hashCode()
    {
        return category_;
    }
    public boolean equals(Object anObject)
    {
        if((anObject != null) && (anObject instanceof TermCatKey))
        {
            TermCatKey temp = (TermCatKey) anObject;
            return 
                ((term_.equals(temp.GetTerm())) && 
                 (category_ == temp.GetCategory()));
        }
        else
        {
            return false;
        }
    }
    private String term_ = null;
    private int category_ = (int) Category.NO_BIT_VALUE;
}
