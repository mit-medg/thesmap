package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class splits ligature characters from a term. 
* Ligatures are defined in different unicode blocks. 
* This flow is also used to normalize fullwidth chracters.
*
* <p> In addition to ligatures defined in uniCode, users may define their own 
* ligatures and mapping characters. The ligatures mapping list is configurable 
* by modifying the configuration file.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/splitLigatures.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/splitLigatures.html"> 
* Split Ligatures</a>
*
* @version    V-2013
****************************************************************************/
public class ToSplitLigatures extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   ligatureMap a hash table contains the mapping of ligatures
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, String> ligatureMap, 
        boolean detailsFlag, boolean mutateFlag)
    {
        Vector<LexItem> out = SplitLigatures(in, ligatureMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
        
    /**
    * Read in ligatures mapping list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of ligatures
    */
    public static Hashtable<Character, String> GetLigatureMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.LIGATURES_FILE);
        String line = null; 
        Hashtable<Character, String> ligatureMap 
            = new Hashtable<Character, String>();
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
                    String splitStr = buf.nextToken();
                    Character ligature = new Character(inChar);
                    // Check fields 1 & 2
                    if(UnicodeUtil.IsAsciiChar(inChar) == true)
                    {
                        System.err.println(
                            "** Warning: Illegal format in ligatures file: '"
                            + fName + "'.");
                    }
                    else
                    {
                        ligatureMap.put(ligature, splitStr);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception: " + e.toString());
            System.err.println(
                "** Error: problem of opening/reading ligature file: '" 
                + fName + "'.");
        }
        return ligatureMap;
    }
    /**
    * Split ligatures for an input character
    *
    * @param   inChar   input character for spliting ligature
    * @param   ligatureMap   user defined ligatures mapping
    *
    * @return  split ligature string
    */
    public static String SplitLigature(char inChar,
        Hashtable<Character, String> ligatureMap)
    {
        // use local mapping if inChar in the mapping table
        Character key = new Character(inChar);
        String outStr = new String();
        if(ligatureMap.containsKey(key) == true)
        {
            outStr = ligatureMap.get(key);
        }
        else // use unicode normalization NFKC algorithm
        {
            outStr = Normalizer.normalize(inChar, Normalizer.NFKC);
        }
        // remove space character from both ends
        if(outStr.length() > 1)
        {
            outStr = outStr.trim();
        }
        return outStr;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "sp\u00E6lsau");
        Hashtable<Character, String> ligatureMap = GetLigatureMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = 
            ToSplitLigatures.Mutate(in, ligatureMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> SplitLigatures(LexItem in,
        Hashtable<Character, String> ligatureMap, String infoStr,
        boolean detailsFlag, boolean mutateFlag)
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
        // mutate the term: split ligatures
        String inStr = in.GetSourceTerm();
        String fs = GlobalBehavior.GetFieldSeparator();
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            // strip each character in the string
            String opStr = NO_OPERATION + fs;
            Character key = new Character(curChar);
            String outStr = UnicodeUtil.CharToStr(curChar);
            if(ligatureMap.containsKey(key) == true)
            {
                outStr = ligatureMap.get(key);
                opStr = MAPPING + fs;
            }
            else    // use unicode normalization NFKC algorithm
            {
                // get the UniCode normalized String
                String normStr = Normalizer.normalize(curChar, Normalizer.NFKC);
                // remove space character from both ends
                if(normStr.length() > 1)
                {
                    outStr = normStr.trim();
                }
                else
                {
                    outStr = normStr;
                }
                // check if normalized
                if(UnicodeUtil.CharToStr(curChar).equals(outStr) == false)
                {
                    opStr = NORM_NFKC + fs;
                }
            }
            buffer.append(outStr);
            // update mutate information
            if(mutateFlag == true)
            {
                mutate += opStr;
            }
        }
        String term = buffer.toString();
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.SPLIT_LIGATURES, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // data members
    private static final String INFO = "Split Ligatures";
    final private static String NO_OPERATION = "NO";
    final private static String MAPPING = "MP";
    final private static String NORM_NFKC = "NFKC";
}
