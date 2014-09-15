package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class strips stop words from a specified term.  A stop word is:
* <ul>
* <li> a high frequency word, such as a preposition.
* <li> a grammer word, which does not contribute the meaning of the sentence 
* too much.
* </ul>
* <p> The defualt stop words include:
* <br>"of", "and", "with", "for", "nos", "to", "in", "by", "on", "the",
* (non mesh)".
* The stop words list is configurable by modifying the configuration file.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/stripStopWords.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToStripStopWords extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   stopWords   Vector<String> - stop words list
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, Vector<String> stopWords,
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate the term: strip stop words
        String term = StripStopWords(in.GetSourceTerm(), stopWords);
        // strip multiple stop words, such as (non mesh)
        Vector<String> multipleStopWords = GetMultipleStopWords(stopWords);
        term = Strip.StripStrings(term, multipleStopWords, false);
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
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_STOP_WORDS, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * Read in stop words from configuration file
    *
    * @param   config   Configuratin object
    *
    * @return   Vector<String> - stop words
    */
    public static Vector<String> GetStopWordsFromFile(Configuration config)
    {
        String fName = config.GetConfiguration(Configuration.LVG_DIR)
            + config.GetConfiguration(Configuration.STOP_WORD_FILE);
        String line = null; 
        Vector<String> stopWords = new Vector<String>();
        try        // load stop words from file
        {
            BufferedReader in = new BufferedReader(new FileReader(fName));
            // read in line by line from a file
            while((line = in.readLine()) != null)
            {
                // skip the line if it is empty or comments (#)
                if((line.length() > 0) && (line.charAt(0) != '#'))
                {
                    stopWords.addElement(line);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception: " + e.toString());
            System.err.println(
                "** Error: problem of opening/reading stop words file: '" + 
                fName + "'.");
        }
        return stopWords;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "On the Top");
        Vector<String> stopWords = GetStopWordsFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToStripStopWords.Mutate(in, stopWords, true, 
            true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static String StripStopWords(String inStr, Vector<String> stopWords)
    {
        Vector<StrTokenObject> list = StripToken.Tokenize(inStr); 
        Vector<StrTokenObject> newList = new Vector<StrTokenObject>(list);
        // Strip stop words
        for(int i = 0; i < list.size(); i++)
        {
            StrTokenObject cur = list.elementAt(i);
            if((cur.GetTokenType() == StrTokenObject.TOKEN)
            && (IsContain(stopWords, cur.GetTokenStr()) == true))
            {
                // change the type of token to be stripped
                StrTokenObject temp = 
                    new StrTokenObject(" ", StrTokenObject.STRIPPED);
                newList.setElementAt(temp, i);
            }
        }
        // compose the string
        Vector<StrTokenObject> cleanList = StripToken.CleanUpToken(newList);
        return (StripToken.ComposeString(cleanList)).trim();
    }
    // check if the input string contain any element of the specified list
    private static Vector<String> GetMultipleStopWords(Vector<String> stopWords)
    {
        Vector<String> out = new Vector<String>();
        if(stopWords == null)
        {
            return stopWords;
        }
        for(int i = 0; i < stopWords.size(); i++)
        {
            String cur = stopWords.elementAt(i);
            if(cur.indexOf(" ") != -1)
            {
                out.addElement(cur);
            }
        }
        return out;
    }
    private static boolean IsContain(Vector<String> list, String inStr)
    {
        if(list == null)
        {
            return false;
        }
        boolean isContain = false;
        for(int i = 0; i < list.size(); i++)
        {
            if(inStr.equalsIgnoreCase(list.elementAt(i)) == true)
            {
                isContain = true;
                break;
            }
        }
        return isContain;
    }
    // data members
    private static final String INFO = "Strip Stop Words";
}
