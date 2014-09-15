package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of an acronym record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/database/acronymTable.html">
*  Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class AcronymRecord
{
    // public constructor
    /**
    *  Default constructor for creating an acronym record.
    */
    public AcronymRecord()
    {
    }
    // public methods
    /**
    * Set the string value of an acronym to current acronym record.
    *
    * @param  value   string value of an acronym
    */
    public void SetAcronym(String value)
    {
        acronym_ = value;
    }
    /**
    * Set the string value of an expansion to current acronym record.
    *
    * @param  value   string value of an expansion
    */
    public void SetExpansion(String value)
    {
        expansion_ = value;
    }
    /**
    * Set the string value of type to current acronym record.
    *
    * @param  value   string value of a type, acronym or abbreviation
    */
    public void SetType(String value)
    {
        type_ = value;
    }
    /**
    * Get the string value of acronym from current acronym record.
    *
    * @return  the string value of acronym from current acroynym record
    */
    public String GetAcronym()
    {
        return acronym_;
    }
    /**
    * Get the string value of expansion from current acronym record.
    *
    * @return  the string value of expansion from current acroynym record
    */
    public String GetExpansion()
    {
        return expansion_;
    }
    /**
    * Get the string value of type from current acronym record.
    *
    * @return  the string value of type, acronym or abbreviation, from current 
    * acroynym record
    */
    public String GetType()
    {
        return type_;
    }
    // data member
    private String acronym_ = null;     // Acronym
    private String expansion_ = null;   // Acronym Expansion
    private String type_ = null;        // type: Acronym or abbreviation
}
