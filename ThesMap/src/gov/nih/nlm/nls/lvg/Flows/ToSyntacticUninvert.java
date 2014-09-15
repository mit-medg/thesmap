package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class syntactic uninverts phrases.
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
public class ToSyntacticUninvert extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   nonInfoWords  non-information words to be stripped
    * @param   conjunctionWords   conjuction words
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - the results from this flow component
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Vector<String> nonInfoWords, Vector<String> conjunctionWords, 
        boolean detailsFlag, boolean mutateFlag)
    {
        String inStr = in.GetSourceTerm();
        // Strip non-fino words
        String stripedStr = Strip.StripStrings(inStr, nonInfoWords, false);
        // mutate the term
        String term = Uninvert(stripedStr, conjunctionWords); 
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
        LexItem temp = UpdateLexItem(in, term, Flow.SYNTACTIC_UNINVERT, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * read in non-information words from configuration file
    *
    * @param   conf   Configuratin object
    *
    * @return  Vector<String> -  non information words
    */
    public static Vector<String> GetNonInfoWordsFromFile(Configuration conf)
    {
        String fName = conf.GetConfiguration(Configuration.LVG_DIR) + 
            conf.GetConfiguration(Configuration.NONINFO_WORD_FILE);
        String line = null; 
        Vector<String> nonInfoWords = new Vector<String>();
        try        // load non-info words from file
        {
            BufferedReader in = new BufferedReader(new FileReader(fName));
            // read in line by line from a file
            while((line = in.readLine()) != null)
            {
                // skip the line if it is empty or comments (#)
                if((line.length() > 0) && (line.charAt(0) != '#'))
                {
                    nonInfoWords.addElement(line);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception: " + e.toString());
            System.err.println(
                "** Error: problem of opening/reading non-Info words file: '" + 
                fName + "'.");
        }
        return nonInfoWords;
    }
    /**
    * Read in conjunction words from configuration file
    *
    * @param   conf   Configuratin object
    *
    * @return  Vector<String> - of conjunction words
    */
    public static Vector<String> GetConjunctionWordsFromFile(Configuration conf)
    {
        String fName = conf.GetConfiguration(Configuration.LVG_DIR) + 
            conf.GetConfiguration(Configuration.CONJ_WORD_FILE);
        Vector<String> conjunctionWords = new Vector<String>();
        String line = null; 
        try        // load conjunction words from file
        {
            BufferedReader in = new BufferedReader(new FileReader(fName));
            // read in line by line from a file
            while((line = in.readLine()) != null)
            {
                // skip the line if it is empty or comments (#)
                if((line.length() > 0) && (line.charAt(0) != '#'))
                {
                    conjunctionWords.addElement(line);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception: " + e.toString());
            System.err.println(
                "** Error: problem of opening/reading conjunction words file: '"
                + fName + "'.");
        }
        return conjunctionWords;    
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, 
            "Angioplasty, Transluminal, Percutaneous Coronary");
        Vector<String> nonInfoWords = GetNonInfoWordsFromFile(conf);
        Vector<String> conjunctionWords = GetConjunctionWordsFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToSyntacticUninvert.Mutate(in, nonInfoWords, 
            conjunctionWords, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private methods
    // uninvert the input phrase around commas.
    private static String Uninvert(String inStr, Vector<String> conjunctionWords)
    {
        Vector<String> tokenList = new Vector<String>();
        // Use token class to put tokens into a Vector
        String delim = ",";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        boolean conjunctionFlag = false;
        while(buf.hasMoreTokens() == true)
        {
            String tempStr = buf.nextToken();
            tokenList.addElement(tempStr);
            String firstWord = GetFirstWord(tempStr);
            if(conjunctionWords.contains(firstWord))
            {
                conjunctionFlag = true;
            }
        }
        // Combine token together if they start with " "
        String lastStr = new String();
        Vector<String> list = new Vector<String>();
        for(int i = 0; i < tokenList.size(); i++)
        {
            String tempStr = tokenList.elementAt(i);
            // rearrange the token list
            if(tempStr.startsWith(" ") == true)        // case of "xxx, xxx"
            {
                // if the word after comma is conjunction word
                if(conjunctionFlag == true)
                {
                    lastStr += ", " + tempStr.trim();
                }
                else
                {
                    list.addElement(lastStr);
                    lastStr = new String(tempStr.trim());
                }
            }
            else        // case of beginning or xxx,xxx
            {
                if(lastStr.length() == 0)     // beginning
                {
                    lastStr = tempStr.trim();
                }
                else    // put "," back 
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
            buffer.append(tempStr);
            buffer.append(" ");
        }
        String outStr = buffer.toString();
        return outStr.trim();
    }
    private static String GetFirstWord(String inStr)
    {
        String delim = " \t";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        return buf.nextToken();
    }
    // data members
    private static final String INFO = "Syntactic Uninvert";
}
