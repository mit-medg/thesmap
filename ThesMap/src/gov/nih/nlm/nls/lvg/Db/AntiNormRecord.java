package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of an AntiNorm record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/database/antiNormTable.html">
* Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class AntiNormRecord 
{
    // public constructor
    /**
    *  Default constructor for creating an inflection record.
    */
    public AntiNormRecord()
    {
    }
    // public methods
    /**
    * Set the normalized term to the current antiNorm record.
    *
    * @param  value   string value of an normalized term
    */
    public void SetNormalizedTerm(String value)
    {
        normTerm_ = value;
    }
    /**
    * Set the inflected term to the current antiNorm record.
    *
    * @param  value   string value of an inflected term
    */
    public void SetInflectedTerm(String value)
    {
        inflTerm_ = value;
    }
    /**
    * Set the EUI to the current antiNorm record.
    *
    * @param  value   string value of an EUI (unique ID of antiNorm record)
    */
    public void SetEui(String value)
    {
        eui_ = value;
    }
    /**
    * Set the category to the current antiNorm record.
    *
    * @param  value   category in an integer format of an antiNorm record
    */
    public void SetCategory(int value)
    {
        cat_ = value;
    }
    /**
    * Set the inflection to the current antiNorm record.
    *
    * @param  value   inflection in a long integer format of an antiNorm record
    */
    public void SetInflection(long value)
    {
        infl_ = value;
    }
    /**
    * Get the normalized term from the current antiNorm record.
    *
    * @return  normalized term of the current antiNorm record
    */
    public String GetNormalizedTerm()
    {
        return normTerm_;
    }
    /**
    * Get the inflected term from the current antiNorm record.
    *
    * @return  inflected term of the current antiNorm record
    */
    public String GetInflectedTerm()
    {
        return inflTerm_;
    }
    /**
    * Get the EUI from the current antiNorm record.
    *
    * @return  unique ID (EUI) of the current antiNorm record
    */
    public String GetEui()
    {
        return eui_;
    }
    /**
    * Get the category from the current antiNorm record.
    *
    * @return  category of the current antiNorm record
    */
    public int GetCategory()
    {
        return cat_;
    }
    /**
    * Get the inflection from the current antiNorm record.
    *
    * @return  inflection of the current antiNorm record
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
        System.out.println(normTerm_ + "|" + inflTerm_ + "|"
            + cat_ + "|" + infl_ + "|" + eui_);
    }
    // data member
    private String normTerm_ = null;  // normalized term
    private String inflTerm_ = null;  // inflected term (lexicon term)
    private String eui_ = null;       // EUI
    private int cat_ = -1;            // category
    private long infl_ = -1;          // inflection
}
