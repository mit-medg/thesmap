package gov.nih.nlm.nls.lvg.Db;
/*****************************************************************************
* This class defined the data structure of a canon record.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/database/canonicalTable.html">
*  Desgin Document </a>
*
* @version    V-2013
****************************************************************************/
public class CanonRecord
{
    // public constructor
    /**
    *  Default constructor for creating a canon record.
    */
    public CanonRecord()
    {
    }
    /**
    * Creates a canon record, using a term as uninflected and canonicalized
    * terms.
    */
    public CanonRecord(String term)
    {
        unTerm_ = term;
        canTerm_ = term;
    }
    // public methods
    /**
    * Set the string value of the uninflected term to current canon record.
    *
    * @param  value   string value of an uninflected term
    */
    public void SetUnInflectedTerm(String value)
    {
        unTerm_ = value;
    }
    /**
    * Set the string value of canonicalized term to current canon record.
    *
    * @param  value   string value of a canonicalized term
    */
    public void SetCanonicalizedTerm(String value)
    {
        canTerm_ = value;
    }
    /**
    * Set the ID ofcanonicalezed term to current canon record.
    *
    * @param  value   string value of a type, Acronym or abbreviation
    */
    public void SetCanonicalId(int value)
    {
        canonId_ = value;
    }
    /**
    * Get the string value of uninflected term from current canon record.
    *
    * @return  the string value of uninflected term from current canon record
    */
    public String GetUninflectedTerm()
    {
        return unTerm_;
    }
    /**
    * Get the string value of canonicalized term from current canon record.
    *
    * @return  the string value of canonicalized term from current canon record
    */
    public String GetCanonicalizedTerm()
    {
        return canTerm_;
    }
    /**
    * Get the integer value of canonical number (ID) of current canon record.
    *
    * @return  the integer value of canonical number from current canon record
    */
    public int GetCanonicalId()
    {
        return canonId_;
    }
    // data member
    private String unTerm_ = null;    // uninflected term
    private String canTerm_ = null;   // canonicalized term
    private int canonId_ = -1;         // ID for canonicalized term
}
