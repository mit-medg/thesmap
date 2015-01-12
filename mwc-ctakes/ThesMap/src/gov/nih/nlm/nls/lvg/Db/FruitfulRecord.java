package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of a fruitful variant record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/database/fruitfulTable.html">
*  Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class FruitfulRecord
{
    // public constructor
    /**
    *  Default constructor for creating an acronym record.
    */
    public FruitfulRecord()
    {
    }
    // public methods
    /**
    * Set the string value of lowercased term to current record.
    *
    * @param  value   string value of a lower cased term
    */
    public void SetLowerCasedTerm(String value)
    {
        termLc_ = value;
    }
    /**
    * Set the string value of variants term to current record.
    *
    * @param  value   string value of variants term
    */
    public void SetVariantTerm(String value)
    {
        variantTerm_ = value;
    }
    /**
    * Set the string value of flow history to current record.
    *
    * @param  value   string value of flow history
    */
    public void SetFlowHistory(String value)
    {
        flowHistory_ = value;
    }
    /**
    * Set the integer value of category to current record.
    *
    * @param  value   integer value of category
    */
    public void SetCategory(int value)
    {
        termCat_ = value;
    }
    /**
    * Set the integer value of original category to current record.
    *
    * @param  value   integer value of original category
    */
    public void SetOriginalCategory(int value)
    {
        orgCat_ = value;
    }
    /**
    * Set the long value of inflection to current record.
    *
    * @param  value   long value of inflection
    */
    public void SetInflection(long value)
    {
        termInfl_ = value;
    }
    /**
    * Set the long value of original inflection to current record.
    *
    * @param  value   long value of original inflection
    */
    public void SetOriginalInflection(long value)
    {
        orgInfl_ = value;
    }
    /**
    * Set the integer value of distance to current record.
    *
    * @param  value   integer value of distance
    */
    public void SetDistance(int value)
    {
        distance_ = value;
    }
    /**
    * Set the long value of tag information to current record.
    *
    * @param  value   long value of inflection
    */
    public void SetTagInformation(long value)
    {
        tagInfo_ = value;
    }
    /**
    * Get the string value of lowercased term from current record.
    *
    * @return  the string value of lowercased term from current record
    */
    public String GetLowerCasedTerm()
    {
        return termLc_;
    }
    /**
    * Get the string value of variants term from current record.
    *
    * @return  the string value of variants term from current record
    */
    public String GetVariantTerm()
    {
        return variantTerm_;
    }
    /**
    * Get the string value of flow history from current record.
    *
    * @return  the string value of flow history from current record
    */
    public String GetFlowHistory()
    {
        return flowHistory_;
    }
    /**
    * Get the integer value of category from current record.
    *
    * @return  the integer value of category from current record
    */
    public int GetCategory()
    {
        return termCat_;
    }
    /**
    * Get the integer value of orignal category from current record.
    *
    * @return  the integer value of original category from current record
    */
    public int GetOriginalCategory()
    {
        return orgCat_;
    }
    /**
    * Get the long value of orignal inflection from current record.
    *
    * @return  the long value of original inflection from current record
    */
    public long GetOriginalInflection()
    {
        return orgInfl_;
    }
    /**
    * Get the long value of inflection from current record.
    *
    * @return  the long value of inflection from current record
    */
    public long GetInflection()
    {
        return termInfl_;
    }
    /**
    * Get the integer value of distance from current record.
    *
    * @return  the integer value of distance from current record
    */
    public int GetDistance()
    {
        return distance_;
    }
    /**
    * Get the long value of tag information from current record.
    *
    * @return  the long value of tag information from current record
    */
    public long GetTagInformation()
    {
        return tagInfo_;
    }
    // data member
    private String termLc_ = null;          // term in lower case (key)
    private String variantTerm_ = null;     // variant term
    private String flowHistory_ = null;     // flow history
    private int termCat_ = -1;              // category
    private long termInfl_ = -1;            // inflection
    private int orgCat_ = -1;               // original category (after 1 flow)
    private long orgInfl_ = -1;             // original inflections
    private int distance_ = -1;             // distance
    private long tagInfo_ = -1;             // tag information
}
