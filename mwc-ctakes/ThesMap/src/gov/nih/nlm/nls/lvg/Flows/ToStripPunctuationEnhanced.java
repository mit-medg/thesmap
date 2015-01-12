package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class performs an enhanced function of stripping punctuations from
* a specified term.  This enhanced function does not strip punctuations from
* following cases:
* <ul>
* <li> Floating number: "1.25", "-23", or "-23.38"
*   <br> => use Float.ParseFloat() to find float words. x.xx or -x or -x.xx
* <li> Date: "10/12/97" or "10-12-00"
*    <br> => utilize DateFormat, SimpleDateFormat to find "d/M/YY" and "d-M-YY"
* <li> Telephone: "301-435-3170" or "301.435.3170"
* <li> Catelog: such as "007.12.1234.07" or "007-12-1234-07"
*   <br> => NN-NN-NN if '-' is arounded by number, don't do anything
*   <br> => Hypened words or Chemical: XX-XX-XX if '-' is not around by 
*        number only, replace it with space.
* <li> Genitive: such as "Guy's", "Guys' ", "Guyz' ", "Guyx' " (TBD)
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/stripPunctuationEnhanced.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToStripPunctuationEnhanced extends Transformation implements Cloneable
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
        String term = StripPunctuationEnhanced(in.GetSourceTerm());
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
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_PUNCTUATION_ENHANCED, 
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
        Vector<LexItem> outs 
            = ToStripPunctuationEnhanced.Mutate(in, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private methods
    // strip punctuations (enhanced) from a term
    private static String StripPunctuationEnhanced(String inStr)
    {
        StringTokenizer buf = new StringTokenizer(inStr, " \t");
        String word = null;
        Vector<String> strList = new Vector<String>();
        while(buf.hasMoreTokens() == true)
        {
            word = StripPunctuationFromWord(buf.nextToken());
            strList.addElement(word);
        }
        // construct the String
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < strList.size(); i++)
        {
            buffer.append(strList.elementAt(i));
            buffer.append(" ");
        }
        String out = buffer.toString();
        return out.trim();
    }
    // strip punctuations (enhanced) from a word
    static String StripPunctuationFromWord(String inWord)
    {
        if(Word.HasPunctuation(inWord) == false)    // no punctuations
        {
            return inWord;
        }
        else if(Word.IsCatelogNumber(inWord) == true)    // float, date, Catelog
        {
            return inWord;
        }
        inWord = inWord.replace('-', ' ');       // replace hyphen with a space
        return ToStripPunctuation.StripPunctuation(inWord);
    }
    // data members
    private static final String INFO = "Strip Punctuation Enchanced";
}
