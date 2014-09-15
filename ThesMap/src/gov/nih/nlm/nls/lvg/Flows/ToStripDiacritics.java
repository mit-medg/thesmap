package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import com.ibm.icu.text.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class strips diacritics characters from a term. Diacritics includes: 
* <ul>
* <li> grave accent 
* <li> acute accent 
* <li> circumflex accent 
* <li> tilde 
* <li> umlaut 
* <li> ring 
* <li> cedilla 
* <li> slash 
* <li> etc. 
* </ul>
* <p> Diacritic chractrers are in ISO Latin I character set.
* In other words, it is Unicode Latin-1 supplement block (U+0080 ~ U+00FF).
* It also in other unicode blocks, such as Latin Extend-A and Latin Extend-B.
* The diacritics mapping list is configurable by modifying the configuration
* file (${LVG}/data/Unicode/diacriticMap.data).
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/stripDiacritics.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/stripDiacritics.html"> 
* Strip Diacritics</a>
*
* @version    V-2013
****************************************************************************/
public class ToStripDiacritics extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   diacriticMap a hash table contain the mapping of diacritics
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, Character> diacriticMap,
        boolean detailsFlag, boolean mutateFlag)
    {
        Vector<LexItem> out = StripDiacritics(in, diacriticMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * read in diacritics mapping list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of diacritics
    */
    public static Hashtable<Character, Character> GetDiacriticMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.DIACRITICS_FILE);
        String line = null; 
        Hashtable<Character, Character> diacriticMap 
            = new Hashtable<Character, Character>();
        try        // load diacritics from file
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
                    char mapChar = buf.nextToken().charAt(0);
                    Character diacritic = new Character(inChar);
                    Character nDiacritic = new Character(mapChar);
                    // Check fields 1 & 2
                    if((UnicodeUtil.IsAsciiChar(inChar) == true)
                    || (UnicodeUtil.IsAsciiChar(mapChar) == false))
                    {
                        System.err.println(
                            "** Warning: Illegal format in diacritics file: '"
                            + fName + "'.");
                        System.err.println(line);
                    }
                    else
                    {
                        diacriticMap.put(diacritic, nDiacritic);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "** ERR: problem of opening/reading diacritics file: '" + 
                fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
        return diacriticMap;
    }
    /**
    * Strip diacritic for an input character
    *
    * @param   inChar   input character for stripping diacritic
    * @param   diacriticMap   user defined diacritics mapping
    * 
    * @return  a character of stripped diacritic
    */
    public static char StripDiacritic(char inChar, 
        Hashtable<Character, Character> diacriticMap)
    {
        // use local mapping if inChar in the mapping table
        Character key = new Character(inChar);
        char outChar = inChar;
        if(diacriticMap.containsKey(key) == true)
        {
            outChar = (diacriticMap.get(key)).charValue();
        }
        else    // use unicode normalization NFD algorithm
        {
            // get the UniCode normalized String 
            String normStr = Normalizer.normalize(inChar, Normalizer.NFD);
            // remove diacritics in Combinging diacritics Marks
            // For Unicode U+0000 ~ U+FFFF:
            // there are 12660 unicode has different results after Norm NFD
            // 11812 are CJK, Hebrew, etc other languages (no diacritics)
            // 4 are diacritics themselves, U+0340, U+0341, U+0343, U+0344
            // 844 can be strip diacritics by this algorithm
            // NFKD does more than strip diacritics, it also split ligature .. 
            // Thus, we still use NFD
            if((normStr.length() > 1)    // themselves
            && (ContainDiacritics(normStr) == true))    // not other language
            {
                outChar = normStr.charAt(0);
            }
        }
        return outChar;
    }
    /**
    * Strip diacritic for an input string
    *
    * @param   inStr   input string for stripping diacritic
    * @param   diacriticMap   user defined diacritics mapping
    * 
    * @return  a string of stripped diacritic
    */
    public static String StripDiacritics(String inStr, 
        Hashtable<Character, Character> diacriticMap)
    {
        StringBuffer buffer = new StringBuffer();
        // strip each character in the string
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            if(UnicodeUtil.IsAsciiChar(curChar) == true)
            {
                buffer.append(curChar);
            }
            else
            {
                buffer.append(StripDiacritic(curChar, diacriticMap));
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
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "resum\u00E9");
        Hashtable<Character, Character> diacriticMap 
            = GetDiacriticMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs 
            = ToStripDiacritics.Mutate(in, diacriticMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> StripDiacritics(LexItem in,
        Hashtable<Character, Character> diacriticMap, String infoStr,
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
        // mutate the term: strip diacritics
        String inStr = in.GetSourceTerm();
        String fs = GlobalBehavior.GetFieldSeparator();
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            // strip each character in the string
            // use local mapping if inChar in the mapping table
            String opStr = NO_OPERATION + fs;
            Character key = new Character(curChar);
            char outChar = curChar;
            if(diacriticMap.containsKey(key) == true)
            {
                outChar = (diacriticMap.get(key)).charValue();
                opStr = MAPPING + fs;
            }
            else    // use unicode normalization NFD algorithm
            {
                // get the UniCode normalized String 
                String normStr = Normalizer.normalize(curChar, Normalizer.NFD);
                // remove diacritics in Combinging diacritics Marks
                if((normStr.length() > 1)
                && (ContainDiacritics(normStr) == true))
                {
                    // non-diacritic character always on the first
                    outChar = normStr.charAt(0);
                    opStr = NORM_NFD + fs;
                }
            }
            buffer.append(outChar);
            // update mutate information
            if(mutateFlag == true)
            {
                mutate += opStr;
            }
        }
        String term = buffer.toString();
        // updatea target 
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_DIACRITICS, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    private static boolean ContainDiacritics(String inStr)
    {
        boolean flag = false;
        for(int i = 0; i < inStr.length(); i++)
        {
            char curChar = inStr.charAt(i);
            int curInt = UnicodeUtil.CharToNum(curChar);
            // Combining Diacritics Marks: U+0300 ~ U+036F
            if((curInt > 767) && (curInt < 880))
            {
                flag = true;
            }
        }
        return flag;
    }
    // data members
    private static final String INFO = "Strip Diacritics";
    final private static String NO_OPERATION = "NO"; 
    final private static String MAPPING = "MP"; 
    final private static String NORM_NFD = "NFD"; 
}
