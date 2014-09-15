package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class strips punctuations from a specified term.  Punctuations include:
* <ul>
* <li><b>DASH_PUNCTUATION (20):</b> -
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
* @see <a href="../../../../../../../designDoc/UDF/flow/stripPunctuation.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToStripPunctuation extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    */
    public static Vector<LexItem> Mutate(LexItem in, boolean detailsFlag, 
        boolean mutateFlag)
    {
        // Mutate the term:
        String term = StripPunctuation(in.GetSourceTerm());
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
        // update target
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_PUNCTUATION, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "Left's 12.34.56");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToStripPunctuation.Mutate(in, true, true);
        PrintResults(in, outs);     // print out result
    }
    // package method
    /**
    * Strip punctuations from a specified term.
    *
    * @param   inStr   the specified term to be stripped punctuations from.
    *
    * @return  the term after being stripped punctuations
    */
    static String StripPunctuation(String inStr)
    {
        int length = inStr.length();
        char[] temp = new char[length];
        int index = 0;
        for(int i = 0; i < length; i++)
        {
            char tempChar = inStr.charAt(i);
            if(Char.IsPunctuation(tempChar) == false)
            {
                temp[index] = tempChar;
                index++;
            }
        }
        String out = new String(temp);
        return out.trim();            // must be trimmed 
    }
    // data members
    private static final String INFO = "Strip Punctuation";
}
