package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class sorts words of a specified term in an ascending ASCII order. 
* This class strip punctuations from the specified term before the sorting.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/sortWordsByOrder.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToSortWordsByOrder extends Transformation implements Cloneable
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
        // mutate the term: need to replace punctuation with space, first
        String tempStr = ToReplacePunctuationWithSpace.
            ReplacePunctuationWithSpace(in.GetSourceTerm());
        String term = SortWordsByOrder(tempStr);
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
        LexItem temp = UpdateLexItem(in, term, Flow.SORT_BY_WORD_ORDER, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "Left Right Middle");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToSortWordsByOrder.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static String SortWordsByOrder(String inStr)
    {
        String delim = " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        StringBuffer buffer = new StringBuffer();
        Vector<String> strList = new Vector<String>();
        // put all words into a Vector, then sort all elements (words)
        while(buf.hasMoreTokens() == true)
        {
            strList.addElement(buf.nextToken());
        }
        Collections.sort(strList);
        // form the String
        for(int i = 0; i < strList.size(); i++)
        {
            buffer.append(strList.elementAt(i));
            buffer.append(" ");
        }
        String out = buffer.toString();
        return out.trim();
    }
    // data members
    private static final String INFO = "Sort Words By ASCII Order";
}
