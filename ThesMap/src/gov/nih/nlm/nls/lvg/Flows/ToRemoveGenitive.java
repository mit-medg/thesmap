package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class removes possessives from the input term.  It handles following 
* cases:
* <ul>
* <li> xxx's -> xxx
* <li> xxxs' -> xxxs
* <li> yyyx' -> yyyx
* <li> xxxz' -> xxxz
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/removeGenitive.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToRemoveGenitive extends Transformation implements Cloneable
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
        // Mutate
        String term = RemoveGenitiveFromString(in.GetSourceTerm());
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
        LexItem temp = UpdateLexItem(in, term, Flow.REMOVE_GENITIVE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        String testStr = GetTestStr(args, "Downs' Syndrome");// get input String
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToRemoveGenitive.Mutate(in, true, true);
        PrintResults(in, outs);        // print out results
    }
    // private methods
    // remove genetive from a string (term)
    private static String RemoveGenitiveFromString(String inStr)
    {
        StringTokenizer buf = new StringTokenizer(inStr, " \t,", true);
        String word = null;
        Vector<String> strList = new Vector<String>();
        while(buf.hasMoreTokens() == true)
        {
            word = RemoveGenitiveFromWord(buf.nextToken());
            strList.addElement(word);
        }
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < strList.size(); i++)
        {
            buffer.append(strList.elementAt(i));
        }
        String out = buffer.toString();
        return out.trim();
    }
    // remove genetives from a word
    private static String RemoveGenitiveFromWord(String inWord)
    {
        String out = inWord; 
        int size = out.length();
        // check if the inWord is shorter than "'s"
        if(size < 3)
        {
            return out;
        }
        // remove s' or x' or z'
        out = RemoveLastChars(out, "s'", 1);
        out = RemoveLastChars(out, "x'", 1);
        out = RemoveLastChars(out, "z'", 1);
        // remove 's or 'S
        out = RemoveLastChars(out, "'s", 2);
        //boolean remove = IsMoved(size, out.length());
        return out;
    }
    // remove the last chars in a string
    private static String RemoveLastChars(String inStr, String chars, int s)
    {
        int size = inStr.length();        //size of input String
        int cSize = chars.length();        // size of string to be remove
        int index = inStr.toLowerCase().lastIndexOf(chars);
        int i = 0;
        while((index == size-cSize)        // if the chars is at the very last
        && (index > 0))
        {
            inStr = inStr.substring(0, index+cSize-s);
            size = inStr.length();
            index = inStr.toLowerCase().lastIndexOf(chars);
            i++;
        }
        return inStr;
    }
    // check if the character is moved by comparing old size and new size
    private static boolean IsMoved(int oldSize, int newSize)
    {
        return (!(oldSize == newSize));
    }
    // data members
    private static final String INFO = "Remove Genitive";
}
