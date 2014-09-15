package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import com.ibm.icu.lang.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class convert Unicode characters to the base of its synonym.
* This is a pure table mapping method and is usually used at the end of the 
* unicode to ASCII normalization.
*
* <p> Users may define their own mapping in $LVG/data/Unicode/synonymMap.data.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/getUnicodeSynonym.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/getUnicodeSynonym.html"> 
* Get Unicode Synonyms</a>
*
* @version    V-2013
****************************************************************************/
public class ToGetUnicodeSynonyms extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   synonymMap   a hash table contain the unicode synonyms mapping
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, Character> synonymMap, boolean detailsFlag, 
        boolean mutateFlag)
    {
        Vector<LexItem> out = GetUnicodeSynonyms(in, synonymMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Read in unicode synonyms mapping list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of unicode synonyms
    */
    public static Hashtable<Character, Character> GetUnicodeSynonymMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.UNICODE_SYNONYM_FILE);
        String line = null; 
        Hashtable<Character, Character> unicodeSynonymMap 
            = new Hashtable<Character, Character>();
        try        // load ligature from file
        {
            // read in line by line from a file
            BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(fName), "UTF-8"));
            while((line = in.readLine()) != null)
            {
                // skip the line if it is empty or comments (#)
                if((line.length() > 0) && (line.charAt(0) != '#'))
                {
                    // use ' ' and '\t' as delimiter to parse token
                    StringTokenizer buf = new StringTokenizer(line, "|");
                    // readin fields 1 & 2
                    char inChar = 
                        (char) UnicodeUtil.UnicodeHexToNum(buf.nextToken());
                    char mapChar = 
                        (char) UnicodeUtil.UnicodeHexToNum(buf.nextToken());
                    Character srcChar = new Character(inChar);
                    Character tarChar = new Character(mapChar);
                    unicodeSynonymMap.put(srcChar, tarChar);
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "** Error: problem of opening/reading symbol synonym file: '" + 
                fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
        return unicodeSynonymMap;
    }
    /**
    * Get unicode synonym
    *
    * @param   inChar   an input character
    * @param   unicodeSynonymMap   a hash table contains the unicode synonyms
    *
    * @return  the base of unicode synonym
    */
    public static char GetUnicodeSynonym(char inChar, 
        Hashtable<Character, Character> unicodeSynonymMap)
    {
        char outChar = inChar;
        if(unicodeSynonymMap.containsKey(inChar))
        {
            outChar = unicodeSynonymMap.get(inChar).charValue();
        }
        return outChar;
    }
    /**
    * Get unicode synonym
    *
    * @param   inStr   an input string
    * @param   unicodeSynonymMap   a hash table contains the unicode synonyms
    *
    * @return  the base of unicode synonym
    */
    public static String GetUnicodeSynonym(String inStr, 
        Hashtable<Character, Character> unicodeSynonymMap)
    {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            // get unicode synonym if in the mapping table
            char curChar = inStr.charAt(i);
            Character key = new Character(curChar);
            if(unicodeSynonymMap.containsKey(key))
            {
                buffer.append(unicodeSynonymMap.get(key).charValue());
            }
            else
            {
                buffer.append(curChar);
            }
        }
        return buffer.toString();
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        String testStr = GetTestStr(args, "\u00A9 and \u00B5");
        Configuration conf = new Configuration("data.config.lvg", true);
        Hashtable<Character, Character> unicodeSynonymMap 
            = GetUnicodeSynonymMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToGetUnicodeSynonyms.Mutate(in, 
            unicodeSynonymMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> GetUnicodeSynonyms(LexItem in,
        Hashtable<Character, Character> unicodeSynonymMap,
        String infoStr, boolean detailsFlag, boolean mutateFlag)
    {
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = infoStr;
        }
        if(mutateFlag == true)
        {
            mutate = new String();
        }
        // mutate the term: get unicode name
        String inStr = in.GetSourceTerm();
        String fs = GlobalBehavior.GetFieldSeparator();
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            // get unicode synonym if in the mapping table
            char curChar = inStr.charAt(i);
            String opStr = NO_OPERATION + fs;
            Character key = new Character(curChar);
            if(unicodeSynonymMap.containsKey(key))
            {
                buffer.append(unicodeSynonymMap.get(key).charValue());
                opStr = MAPPING + fs;
            }
            else
            {
                buffer.append(curChar);
            }
            // update mutate information
            if(mutateFlag == true)
            {
                mutate += opStr;
            }
        }
        String term = buffer.toString();
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.GET_UNICODE_SYNONYM, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // data members
    private static final String INFO = "Get Unicode Synonmy";
    final private static String NO_OPERATION = "NO";
    final private static String MAPPING = "MP";
}
