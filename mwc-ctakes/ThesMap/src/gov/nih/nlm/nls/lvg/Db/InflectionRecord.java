package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of an inflection record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/database/inflectionTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class InflectionRecord 
{
    // public constructor
    /**
    *  Default constructor for creating an inflection record.
    */
    public InflectionRecord()
    {
    }
    // public methods
    /**
    * Set the inflected term to the current inflection record.
    *
    * @param  value   string value of an inflected term
    */
    public void SetInflectedTerm(String value)
    {
        ifTerm_ = value;
    }
    /**
    * Set the uninflected term to the current inflection record.
    *
    * @param  value   string value of an uninflected term
    */
    public void SetUnInflectedTerm(String value)
    {
        unTerm_ = value;
    }
    /**
    * Set the citation term to the current inflection record.
    *
    * @param  value   string value of a citation term
    */
    public void SetCitationTerm(String value)
    {
        ctTerm_ = value;
    }
    /**
    * Set the EUI to the current inflection record.
    *
    * @param  value   string value of an EUI (unique ID of inflection record)
    */
    public void SetEui(String value)
    {
        eui_ = value;
    }
    /**
    * Set the category to the current inflection record.
    *
    * @param  value   category in an integer format of a inflection record
    */
    public void SetCategory(int value)
    {
        cat_ = value;
    }
    /**
    * Set the inflection to the current inflection record.
    *
    * @param  value   inflection in a long integer format of a inflection record
    */
    public void SetInflection(long value)
    {
        infl_ = value;
    }
    /**
    * Get the inflected term from the current inflection record.
    *
    * @return  inflected term of the current inflection record
    */
    public String GetInflectedTerm()
    {
        return ifTerm_;
    }
    /**
    * Get the uninflected term from the current inflection record.
    *
    * @return  uninflected term of the current inflection record
    */
    public String GetUninflectedTerm()
    {
        return unTerm_;
    }
    /**
    * Get the citation term from the current inflection record.
    *
    * @return  citation term of the current inflection record
    */
    public String GetCitationTerm()
    {
        return ctTerm_;
    }
    /**
    * Get the EUI from the current inflection record.
    *
    * @return  unique ID (EUI) of the current inflection record
    */
    public String GetEui()
    {
        return eui_;
    }
    /**
    * Get the category from the current inflection record.
    *
    * @return  category of the current inflection record
    */
    public int GetCategory()
    {
        return cat_;
    }
    /**
    * Get the inflection from the current inflection record.
    *
    * @return  inflection of the current inflection record
    */
    public long GetInflection()
    {
        return infl_;
    }
    /**
    * Print out the current inflection record in a string format to system
    * output.
    */
    public void PrintRecord()
    {
        System.out.println(ifTerm_ + "|" + unTerm_ + "|" + ctTerm_ + "|"
            + eui_ + "|" + cat_ + "|" + infl_);
    }
    // data member
    private String ifTerm_ = null;    // inflected term
    private String unTerm_ = null;    // uninflected term
    private String ctTerm_ = null;    // citation term
    private String eui_ = null;       // EUI
    private int cat_ = -1;            // category
    private long infl_ = -1;          // inflection
}
