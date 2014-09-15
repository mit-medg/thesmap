package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class makes no operation on the input term.  In other words, it returns
* the input LexItem.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/noOperation.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToNoOperation extends Transformation implements Cloneable
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
        String term = in.GetSourceTerm();
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
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
        LexItem temp = UpdateLexItem(in, term, Flow.NO_OPERATION, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "force");        // get input String
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToNoOperation.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    // data members
    private static final String INFO = "No Operation";
}
