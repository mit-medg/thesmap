package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class strips or maps non-ASCII Unicode characters.
* This is a pure table mapping method with simply algorithm to 
* <ul>
* <li>strip non-ASCII unicode characters if they are not in mapping table
* <li>convert non-ASCII unicode characters to ASCII if they are in mapping table
* </ul>
* <p>This flow is used to perform final tune up in normalizing Unicode to ASCII
* after using other Unicode normalization flows.
*
* <p> Users may define their own mapping in $LVG/data/Unicode/nonStripMap.data.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/stripMapUnicode.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/stripMapUnicode.html"> 
* Strip or Map Unicode</a>
*
* @version    V-2013
****************************************************************************/
public class ToStripMapUnicode extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   nonStripMap   a hash table contains the non-Strip map unicode
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, String> nonStripMap, boolean detailsFlag, 
        boolean mutateFlag)
    {
        Vector<LexItem> out = StripMapUnicodeToAscii(in, nonStripMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Read in non-strip map unicode list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of non-strip map Unicode list
    */
    public static Hashtable<Character, String> GetNonStripMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.NON_STRIP_MAP_UNICODE_FILE);
        String line = null; 
        Hashtable<Character, String> nonStripMap 
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
                    StringTokenizer buf = new StringTokenizer(line, "|", true);
                    // readin fields 1 & 2
                    char inChar = 
                        (char) UnicodeUtil.UnicodeHexToNum(buf.nextToken());
                    Character nonStripUnicode = new Character(inChar);
                    buf.nextToken();    // 1st delimiter
                    // new feature if the next field ASCII str is |
                    int nextTokenIndex = 0;
                    String mapStr = new String();
                    while(buf.hasMoreTokens() == true)
                    {
                        String curToken = buf.nextToken();    // next field
                        if(curToken.equals("|") == false)    // not "|"
                        {
                            if(nextTokenIndex == 0)    // 1st token after 1st field
                            {
                                mapStr = curToken;    // 2nd field is not "|"
                            }
                            else    // 2nd field is "|"
                            {
                                // add all "|"s before not "|" field
                                for(int i = 0; i < nextTokenIndex-1; i++)
                                {
                                     mapStr += "|";    // assign "|" to mapStr
                                }
                            }
                            break;
                        }
                        nextTokenIndex++;
                    }
                    // Check fields 1 & 2
                    if((UnicodeUtil.IsAsciiChar(inChar) == true)
                    || (UnicodeUtil.IsAsciiStr(mapStr) == false))
                    {
                        System.err.println(
                            "** Warning: Illegal format in nonStripMap file: '"
                            + fName + "'.");
                        System.err.println(line);
                    }
                    else
                    {
                        nonStripMap.put(nonStripUnicode, mapStr);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "** Error: problem of opening/reading nonStripMap file: '" 
                + fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
        return nonStripMap;
    }
    /**
    * Strip or map unicode to ASCII
    *
    * @param   inChar   an input character
    * @param   nonStripMap   a hash table contains the unicode
    *
    * @return  the stripped or mapped string in ASCII
    */
    public static String StripMapUnicodeToAscii(char inChar, 
        Hashtable<Character, String> nonStripMap)
    {
        // return original ASCII
        if(UnicodeUtil.IsAsciiChar(inChar) == true)
        {
            return String.valueOf(inChar);
        }
        // non-ASCII
        String outStr = new String();    // strip
        if(nonStripMap.containsKey(inChar) == true)        // map
        {
            outStr = nonStripMap.get(inChar);
        }
        return outStr;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        String testStr = GetTestStr(args, "\u00A9 and \u00B5");
        Configuration conf = new Configuration("data.config.lvg", true);
        Hashtable<Character, String> nonStripMap 
            = GetNonStripMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToStripMapUnicode.Mutate(in, 
            nonStripMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> StripMapUnicodeToAscii(LexItem in,
        Hashtable<Character, String> nonStripMap,
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
            // strip or map unicode to ASCII Str 
            char curChar = inStr.charAt(i);
            String opStr = NO_OPERATION + fs;
            // ASCII: no operation
            if(UnicodeUtil.IsAsciiChar(curChar) == true)
            {
                buffer.append(curChar);
            }
            else    // NON-ASCII: Stripping or Mapping
            {
                Character key = new Character(curChar);
                if(nonStripMap.containsKey(key) == true)    // map
                {
                    buffer.append(nonStripMap.get(key));
                    opStr = MAPPING + fs;
                }
                else    // strip
                {
                    opStr = STRIPPING + fs;
                }
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
        LexItem temp = UpdateLexItem(in, term, Flow.STRIP_MAP_UNICODE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // data members
    private static final String INFO = "Strip or Map Unicode to ASCII";
    final private static String NO_OPERATION = "NO";
    final private static String MAPPING = "MP";
    final private static String STRIPPING = "SP";
}
