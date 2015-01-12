package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class creates an object for the result after applying a rule. It 
* includes input term, output result, and rule.
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class RuleResult
{
    // public constructors
    /**
    * Create an object for representing the result after applying a rule,
    * using input term, output term, and the string format of a rule.
    */
    public RuleResult(String inTerm, String outTerm, String ruleStr)
    {
        inTerm_ = inTerm;
        outTerm_ = outTerm;
        ruleStr_ = ruleStr;
    }
    // public methods
    /**
    * Get the input term of the current rule result.
    *
    * @return  input term of the current rule result
    */
    public String GetInTerm()
    {
        return inTerm_;
    }
    /**
    * Get the output term (result) of the current rule result.
    *
    * @return  output term (result) of the current rule result
    */
    public String GetOutTerm()
    {
        return outTerm_;
    }
    /**
    * Get the string format of rule used in the current rule result.
    *
    * @return  string format of rule used in the current rule result
    */
    public String GetRuleString()
    {
        return ruleStr_;
    }
    /**
    * Get the input category from the current rule result.
    *
    * @return  input category from the current rule result
    */
    public String GetInCategory()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        buf.nextToken();    // in Suffix
        String inCat = buf.nextToken();
        return inCat;
    }
    /**
    * Get the output category from the current rule result.
    *
    * @return  output category from the current rule result
    */
    public String GetOutCategory()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        buf.nextToken();    // in Suffix
        buf.nextToken();    // in Category
        buf.nextToken();    // in Inflection
        buf.nextToken();    // out Suffix
        String outCat = buf.nextToken();
        return outCat;
    }
    /**
    * Get the input inflection from the current rule result.
    *
    * @return  input inflection from the current rule result
    */
    public String GetInInflection()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        buf.nextToken();    // in Suffix
        buf.nextToken();    // in Category
        String inInfl = buf.nextToken();
        return inInfl;
    }
    /**
    * Get the output inflection from the current rule result.
    *
    * @return  output inflection from the current rule result
    */
    public String GetOutInflection()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        buf.nextToken();    // in Suffix
        buf.nextToken();    // in Category
        buf.nextToken();    // in Inflection
        buf.nextToken();    // out Suffix
        buf.nextToken();    // out Category
        String outInfl = buf.nextToken();
        return outInfl;
    }
    /**
    * Get the output category and inflection from the current rule result.
    *
    * @return  output category and inflection from the current rule result
    */
    public String GetOutCategoryAndInflection()
    {
        StringTokenizer buf = new StringTokenizer(ruleStr_, "|");
        buf.nextToken();    // in Suffix
        buf.nextToken();    // in Category
        buf.nextToken();    // in Inflection
        buf.nextToken();    // out Suffix
        String outCatInfl = buf.nextToken() + "|" + buf.nextToken();
        return outCatInfl;
    }
    // data member
    private String ruleStr_ = null;        // the original string for the rule
    private String inTerm_ = null;
    private String outTerm_ = null;
}
