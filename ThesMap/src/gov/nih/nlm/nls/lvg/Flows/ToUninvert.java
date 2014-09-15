package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class uninverts phrases of a specified term around commas.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/uninvert.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToUninvert extends Transformation implements Cloneable
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
        String term = Uninvert(in.GetSourceTerm()); 
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
        LexItem temp = UpdateLexItem(in, term, Flow.UNINVERT, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = 
        GetTestStr(args, "Angioplasty, Transluminal, Percutaneous Coronary");
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToUninvert.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private methods
    // uninvert the input phrase around commas.
    private static String Uninvert(String inStr)
    {
        Vector<String> tokenList = new Vector<String>();
        // Use token class to put tokens into a Vector
        String delim = ",";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        while(buf.hasMoreTokens() == true)
        {
            tokenList.addElement(buf.nextToken());
        }
        // Combine token together if they start with " "
        String lastStr = new String();
        Vector<String> list = new Vector<String>();
        for(int i = 0; i < tokenList.size(); i++)
        {
            String tempStr = tokenList.elementAt(i);
            if(tempStr.startsWith(" ") == true)
            {
                list.addElement(lastStr);
                lastStr = new String(tempStr.trim());
            }
            else
            {
                if(lastStr.length() == 0)
                {
                    lastStr = tempStr.trim();
                }
                else
                {
                    lastStr += "," + tempStr.trim();
                }
            }
        }
        list.addElement(lastStr);
        // reform the out from the Vector
        StringBuffer buffer = new StringBuffer();
        for(int i = list.size()-1; i >= 0; i--)
        {
            String tempStr = list.elementAt(i);
            buffer.append(tempStr.trim());
            buffer.append(" ");
        }
        String outStr = buffer.toString();
        return outStr.trim();
    }
    // data members
    private static final String INFO = "Uninvert";
}
