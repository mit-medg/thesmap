package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of an Eui record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class EuiRecord 
{
    // public constructor
    /**
    *  Default constructor for creating an inflection record.
    */
    public EuiRecord()
    {
    }
    // public methods
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
        System.out.println(eui_ + "|" + cat_ + "|" + infl_);
    }
    // data member
    private String eui_ = null;       // EUI
    private int cat_ = 0;           // category
    private long infl_ = 0;          // inflection
}
