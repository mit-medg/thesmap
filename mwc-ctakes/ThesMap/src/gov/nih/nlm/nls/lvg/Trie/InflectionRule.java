package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class creates an object of LVG inflection rule, using a string format
* of a rule.  The string format of a rule is described in  the
* <a href="../../../../../../../designDoc/UDF/trie/file.html">design document</a>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/trie/index.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class InflectionRule
{
    // public constructor
    
    /**
    * Create an object of inflection rule, using a rule String
    *
    * @see <a href="../../../../../../../designDoc/UDF/trie/index.html">
    * Design Document </a>
    */
    public InflectionRule(String ruleStr)
    {
        ruleStr_ = ruleStr;
        DecomposeRuleStr();
    }
    // public methods
    /**
    * Reverse entities for inputs and outputs for a inflection rule.
    * All nflection rules are bi-directional and stored on both cases while it
    * was read in (once).
    */
    public void Reverse()        // reverse in and out for bi-direction rule
    {
        String tempinSuffix = inSuffix_;
        long tempCategory = inCategory_;
        long tempInflection = inInflection_;
        inSuffix_ = outSuffix_;
        inCategory_ = outCategory_;
        inInflection_ = outInflection_;
        outSuffix_ = tempinSuffix;
        outCategory_ = tempCategory;
        outInflection_ = tempInflection;
        String fs = new Character(WildCard.FS).toString();
        ruleStr_ = inSuffix_ + fs + Category.ToName(inCategory_) + fs 
            + Inflection.ToName(inInflection_) + fs
            + outSuffix_ + fs + Category.ToName(outCategory_) 
            + fs + Inflection.ToName(outInflection_);
    }
    /**
    * Check if the current inflection rule object is the same as a specified 
    * inflection rule object by comparing rule strings.
    * 
    * @param   rule  a specified inflection rule object to be comapred
    *
    * @return  true or false if the specified inflection rule object is or is 
    * not the same as the current rule object 
    */
    public boolean equals(InflectionRule rule)    // compare rule by it's string
    {
        return ruleStr_.equals(rule.GetRuleStr());
    }
    /**
    * Get the input suffix of the current inflection rule object.
    * 
    * @return  the String format of input suffix of the current inflection 
    * rule object
    */
    public String GetInSuffix()
    {
        return inSuffix_;
    }
    /**
    * Get the output suffix of the current inflection rule object.
    * 
    * @return  the String format of output suffix of the current inflection 
    * rule object
    */
    public String GetOutSuffix()
    {
        return outSuffix_;
    }
    /**
    * Get exceptions (in a hashtable) of the current inflection rule object.
    * 
    * @return  a hashtable to representing all exceptions of the current 
    * inflection rule object
    */
    public Hashtable<String, String> GetExceptions()
    {
        return exceptions_;
    }
    /**
    * Get the string representation of the current inflection rule object.
    * 
    * @return  a string representing the current inflection rule object
    */
    public String GetRuleStr()
    {
        return ruleStr_;
    }
    /**
    * Get the input category of the current inflection rule object.
    * 
    * @return  a long integer representing the input category of the current 
    * inflection rule object
    */
    public long GetInCategory()
    {
        return inCategory_;
    }
    /**
    * Get the string format of the input category of the current inflection 
    * rule object.
    * 
    * @return  a string representing the input category of the current 
    * inflection rule object
    */
    public String GetInCategoryStr()
    {
        return Category.ToName(inCategory_);
    }
    /**
    * Get the input inflection of the current inflection rule object.
    * 
    * @return  a long integer the input inflection of the current 
    * inflection rule object
    */
    public long GetInInflection()
    {
        return inInflection_;
    }
    /**
    * Get the string format of the input inflection of the current inflection 
    * rule object.
    * 
    * @return  a string representing the input inflection of the current 
    * inflection rule object
    */
    public String GetInInflectionStr()
    {
        return Inflection.ToName(inInflection_);
    }
    /**
    * Get the output category of the current inflection rule object.
    * 
    * @return  a long integer the output category of the current 
    * inflection rule object
    */
    public long GetOutCategory()
    {
        return outCategory_;
    }
    /**
    * Get the string format of the output category of the current inflection 
    * rule object.
    * 
    * @return  a string representing the output category of the current 
    * inflection rule object
    */
    public String GetOutCategoryStr()
    {
        return Category.ToName(outCategory_);
    }
    /**
    * Get the output inflection of the current inflection rule object.
    * 
    * @return  a long integer the output inflection of the current 
    * inflection rule object
    */
    public long GetOutInflection()
    {
        return outInflection_;
    }
    /**
    * Get the string format of the output inflection of the current inflection 
    * rule object.
    * 
    * @return  a string representing the output inflection of the current 
    * inflection rule object
    */
    public String GetOutInflectionStr()
    {
        return Inflection.ToName(outInflection_);
    }
    /**
    * Add a specific exception to the current inflection rule object.
    * 
    * @param   line  the string representation of the specific exception
    * to be added
    */
    public void AddException(String line)
    {
        RuleException exception = new RuleException(line);
        //check if the input is a legal format
        if(exception.IsLegalFormat() == false)
        {
            return;
        }
        // init the exception
        if(exceptions_ == null)
        {
            exceptions_ = new Hashtable<String, String>();
        }
        String key = exception.GetKey();
        String value = exception.GetValue();
        Enumeration<String> keyEn = exceptions_.keys();
        Enumeration<String> valueEn = exceptions_.elements();
        boolean duplicate = false;
        while(keyEn.hasMoreElements() == true)
        {
            if((key.equals(keyEn.nextElement()))
            && (value.equals(valueEn.nextElement())))
            {
                duplicate = true;
                break;
            }
        }
        if(duplicate == false)
        {
            exceptions_.put(key, value);
        }
    }
    /**
    * Test driver for this class
    */
    public static void main(String[] args)
    {
        InflectionRule rule = 
            new InflectionRule("Cy$|adv|positive|Cier$|adv|comparative");
        rule.AddException("|er;");
        rule.AddException("inhal|inhaler;");
        rule.PrintRule();
        rule.Reverse();
        rule.PrintRule();
    }
    // decode rule string into 6 fields
    private void DecomposeRuleStr()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        inSuffix_ = buf.nextToken();
        inCategory_ = Category.ToValue(buf.nextToken());
        inInflection_ = Inflection.ToValue(buf.nextToken());
        outSuffix_ = buf.nextToken();
        outCategory_ = Category.ToValue(buf.nextToken());
        outInflection_ = Inflection.ToValue(buf.nextToken());
    }
    private void PrintRule()
    {
        System.out.println("------ Inflection Rule ------");
        System.out.println("ruleStr_: " + ruleStr_);
        System.out.println("inSuffix_: " + inSuffix_);
        System.out.println("inCategory_: " + inCategory_);
        System.out.println("inInflection_: " + inInflection_);
        System.out.println("outSuffix_: " + outSuffix_);
        System.out.println("outCategory_: " + outCategory_);
        System.out.println("outInflection_: " + outInflection_);
        System.out.println("exceptions_: " + 
            ((exceptions_== null)?0:exceptions_.size()));
    }
    
    // data members
    private String ruleStr_ = null;        // the original string for the rule
    private String inSuffix_ = null;
    private String outSuffix_ = null;
    private long inCategory_ = 0;
    private long outCategory_ = 0;
    private long inInflection_ = 0;
    private long outInflection_ = 0;
    private Hashtable<String, String> exceptions_ = null;    // for name value pair operation
}
