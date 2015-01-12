package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class drops words with less size than the specified word size from the 
* input term.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/wordSizeFilter.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToWordSize extends Transformation implements Cloneable
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
    public static Vector<LexItem> Mutate(LexItem in, int wordSize, 
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate the term
        String term = WordSizeFilter(in.GetSourceTerm(), wordSize);
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
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.WORD_SIZE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "This is a test."); 
        // Mutate
        int wordsize = 3;
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToWordSize.Mutate(in, wordsize, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static String WordSizeFilter(String inStr, int wordSize)
    {
        // init all delimiters
        String delim = " \t";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        StringBuffer buffer = new StringBuffer();
        while(buf.hasMoreTokens() == true)
        {
            String cur = buf.nextToken();
            if(cur.length() >= wordSize)
            {
                buffer.append(cur);
                buffer.append(" ");
            }
        }
        String out = buffer.toString();
        
        return out.trim();
    }
    // data members
    private static final String INFO = "Word Size Filter";
}
