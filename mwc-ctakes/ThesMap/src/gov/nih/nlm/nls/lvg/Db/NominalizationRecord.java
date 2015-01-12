package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of a nominalization record.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/database/nominalizationTable.html">
*  Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class NominalizationRecord
{
    // public constructor
    /**
    *  Default constructor for creating a synonym record.
    */
    public NominalizationRecord()
    {
    }
    // public methods
    /**
    * Set the string value of input term, nominalization 1.
    *
    * @param  value   string value of input nominalization base form
    */
    public void SetNominalization1(String value)
    {
        nomTerm1_ = value;
    }
    /**
    * Set the string value of eui to nominalization 1.
    *
    * @param  value   string value of eui 1
    */
    public void SetEui1(String value)
    {
        eui1_ = value;
    }
    /**
    * Set the integer value of category for nominalization to current record.
    *
    * @param  value   string value of category for key form
    */
    public void SetCat1(int value)
    {
        cat1_ = value;
    }
    /**
    * Set the string value of input term, nominalization 2.
    *
    * @param  value   string value of input nominalization base form
    */
    public void SetNominalization2(String value)
    {
        nomTerm2_ = value;
    }
    /**
    * Set the string value of eui to nominalization 2.
    *
    * @param  value   string value of eui 2
    */
    public void SetEui2(String value)
    {
        eui2_ = value;
    }
    /**
    * Set the integer value of category for nominalization to current record.
    *
    * @param  value   string value of category for key form
    */
    public void SetCat2(int value)
    {
        cat2_ = value;
    }
    /**
    * Get the string value of nominalization 1 (input term) from current record.
    *
    * @return  the string value of nominalization 1
    */
    public String GetNominalization1()
    {
        return nomTerm1_;
    }
    /**
    * Get the string value of eui from current record.
    *
    * @return  the string value of eui from current record
    */
    public String GetEui1()
    {
        return eui1_;
    }
    /**
    * Get the category of nominalization 1 from current record.
    *
    * @return  category of nominalization 1 in a integer format from current record
    */
    public int GetCat1()
    {
        return cat1_;
    }
    /**
    * Get the string value of nominalization 2 (input term) from current record.
    *
    * @return  the string value of nominalization 2
    */
    public String GetNominalization2()
    {
        return nomTerm2_;
    }
    /**
    * Get the string value of eui from current record.
    *
    * @return  the string value of eui from current record
    */
    public String GetEui2()
    {
        return eui2_;
    }
    /**
    * Get the category of nominalization 2 from current record.
    *
    * @return  category of nominalization 2 in a integer format from current record
    */
    public int GetCat2()
    {
        return cat2_;
    }
    // data member
    private String nomTerm1_ = null;      // nominalization, base, input term
    private String eui1_ = null;          // eui
    private int cat1_ = 0;                // Category of nominlaization 1
    private String nomTerm2_ = null;      // nominalization, base
    private String eui2_ = null;          // eui
    private int cat2_ = 0;                // Categoryof nominalization 2
}
