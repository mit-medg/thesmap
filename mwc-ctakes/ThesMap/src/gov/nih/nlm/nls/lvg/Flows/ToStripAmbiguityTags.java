package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class strips ambiguity tags from a specified input.  Ambiguity tags are 
* found on Metathesaurus concept names, and are an indication that multiple 
* meanings apply for the same lexical string. These tags look like "<1>" or 
* "<2>".
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/stripAmbiguityTags.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToStripAmbiguityTags extends Transformation implements Cloneable
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
        // mutate the term
        String term = StripAmbiguityTag(in.GetSourceTerm()); 
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
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_AMBIGUITY_TAGS, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "Cold <1>");
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToStripAmbiguityTags.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static String StripAmbiguityTag(String inStr)
    {
        int beginIndex = inStr.indexOf('<');
        int endIndex = inStr.indexOf('>', beginIndex);
        // check if '<' & '>' are legally locate in the string
        if(beginIndex < 0)
        {
            return inStr;
        }
        else if ((endIndex < 0) || (endIndex <= beginIndex))
        {
            return inStr;
        }
        // check if the text inside < .. > is a integer number
        String token = inStr.substring(beginIndex+1, endIndex);
        if(IsInteger(token) == false)
        {
            return inStr;
        }
        // strip the ambiguity tag
        StringBuffer buffer = new StringBuffer();
        buffer.append(inStr.substring(0, beginIndex));
        buffer.append(inStr.substring(endIndex, inStr.length()-1));
        String returnStr = buffer.toString();
        return returnStr.trim();
    }
    private static boolean IsInteger(String inStr)
    {
        boolean isInteger = false;
        try
        {
            Integer.parseInt(inStr);
            isInteger = true;
        }
        catch (NumberFormatException e)
        {
            isInteger = false;
        }
        return isInteger;
    }
    // data members
    private static final String INFO = "Strip Ambiguity Tags";
}
