package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class replaces punctuations with spaces for the input term.  
* Puunctuations include:
* <ul>
* <li> <b>DASH_PUNCTUATION (20):</b> -
* <li><b>START_PUNCTUATION (21):</b> ( { [
* <li><b>END_PUNCTUATION (22):</b> ) } ]
* <li><b>CONNECTOR_PUNCTUATION (23):</b> _
* <li><b>OTHER_PUNCTUATION (24):</b> ! @ # % & * \ : ; " ' ,  . ? /
* <li><b>MATH_SYMBOL (25):</b> ~ + = | < >
* <li><b>CURRENCY_SYMBOL (26):</b> $
* <li><b>MODIFIER_SYMBOL (27):</b> ` ^
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href= "../../../../../../../designDoc/UDF/flow/replacePunctuationWithSpace.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToReplacePunctuationWithSpace extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, boolean detailsFlag, 
        boolean mutateFlag)
    {
        // mutate the term
        String term = ReplacePunctuationWithSpace(in.GetSourceTerm());
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = INFO;
        }
        if(mutateFlag == true)
        {
            mutate = Transformation.NO_MUTATE_INFO;
        }
        // updatea target
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, 
            Flow.REPLACE_PUNCTUATION_WITH_SPACE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "2-aryl-1,3-dithiolane");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs 
            = ToReplacePunctuationWithSpace.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    // replace punctuation with space in a string
    static String ReplacePunctuationWithSpace(String inStr)
    {
        char[] temp = inStr.toCharArray();
        for(int i = 0; i < temp.length; i++)
        {
            if(Char.IsPunctuation(temp[i]) == true)
            {
                temp[i] = ' ';
            }
        }
        String out = new String(temp);
        return out;
    }
    // data members
    private static final String INFO = "Replace Punctuation With Space";
}
