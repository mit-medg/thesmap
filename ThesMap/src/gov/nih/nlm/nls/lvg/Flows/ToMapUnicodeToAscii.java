package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class converts Unicode characters to ASCII by table mapping.
* This is a pure table mapping method and is used to normalize unicode to ASCII 
* for characrters can't be nroamlized by other algorithm.
*
* <p> Users may define their own mapping in $LVG/data/Unicode/unicodeMap.data.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/mapUnicodeToAscii.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/mapUnicodeToAscii.html"> 
* Map Unicode characters to ASCII</a>
*
* @version    V-2013
****************************************************************************/
public class ToMapUnicodeToAscii extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   unicodeMap   a hash table contains the unicode mapping
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, String> unicodeMap, boolean detailsFlag, 
        boolean mutateFlag)
    {
        Vector<LexItem> out = MapUnicodeToAscii(in, unicodeMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Read in unicode to ASCII mapping list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of unicode to ASCII mapping list
    */
    public static Hashtable<Character, String> GetUnicodeMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.UNICODE_FILE);
        String line = null; 
        Hashtable<Character, String> unicodeMap 
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
                    Character unicode = new Character(inChar);
                    buf.nextToken();    // 1st delimiter
                    // new feature if the next field ASCII str is |
                    int nextTokenIndex = 0;
                    String mapStr = new String();
                    while(buf.hasMoreTokens() == true)
                    {
                        String curToken = buf.nextToken();  // next field
                        if(curToken.equals("|") == false)   // not "|"
                        {
                            if(nextTokenIndex == 0) // 1st token after 1st field
                            {
                                mapStr = curToken;  // 2nd field is not "|
                            }
                            else    // 2nd field is "|"
                            {
                                // add all "|"s before not "|" field
                                for(int i = 0; i < nextTokenIndex-1; i++)
                                {
                                    mapStr += "|"; // assign "|" to mapStr
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
                            "** Warning: Illegal format in Unicode file: '"
                            + fName + "'.");
                        System.err.println(line);
                    }
                    else
                    {
                        unicodeMap.put(unicode, mapStr);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "** Error: problem of opening/reading Unicode file: '" 
                + fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
        return unicodeMap;
    }
    /**
    * Get unicode synonym
    *
    * @param   inChar   an input character
    * @param   unicodeMap   a hash table contains the unicode
    *
    * @return  the mapped unicode unicode string in ASCII
    */
    public static String MapUnicodeToAscii(char inChar, 
        Hashtable<Character, String> unicodeMap)
    {
        String outStr = UnicodeUtil.CharToStr(inChar);
        if(unicodeMap.containsKey(inChar))
        {
            outStr = unicodeMap.get(inChar);
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
        Hashtable<Character, String> unicodeMap 
            = GetUnicodeMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToMapUnicodeToAscii.Mutate(in, 
            unicodeMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> MapUnicodeToAscii(LexItem in,
        Hashtable<Character, String> unicodeMap,
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
            // map unicode to ASCII Str if in the mapping table
            char curChar = inStr.charAt(i);
            String opStr = NO_OPERATION + fs;
            Character key = new Character(curChar);
            if(unicodeMap.containsKey(key))
            {
                buffer.append(unicodeMap.get(key));
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
        LexItem temp = UpdateLexItem(in, term, Flow.MAP_UNICODE_TO_ASCII, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // data members
    private static final String INFO = "Map Unicode to ASCII";
    final private static String NO_OPERATION = "NO";
    final private static String MAPPING = "MP";
}
