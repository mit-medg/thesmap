package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class converts Unicode punctuation and symbols to ASCII by table mapping.
* This is a pure table mapping method and is used to preserve the original 
* document in NLP.
*
* <p> Users may define their own mapping in $LVG/data/Unicode/symbolMap.data.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/mapSymbolToAscii.html">
* Design Document </a>
* @see <a href="../../../../../../../designDoc/UDF/unicode/NormOperations/mapSymbolToAscii.html"> 
* Map Symbols and punctuation to ASCII</a>
*
* @version    V-2013
****************************************************************************/
public class ToMapSymbolToAscii extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   symbolMap   a hash table contains the unicode symbols mapping
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    */
    public static Vector<LexItem> Mutate(LexItem in, 
        Hashtable<Character, String> symbolMap, boolean detailsFlag, 
        boolean mutateFlag)
    {
        Vector<LexItem> out = MapSymbolToAscii(in, symbolMap, INFO,
            detailsFlag, mutateFlag);
        return out;
    }
    /**
    * Read in unicode symbol to ASCII mapping list from configuration file
    *
    * @param   config   Configuratin object
    * 
    * @return  a hash table of unicode symbol mapping list
    */
    public static Hashtable<Character, String> GetSymbolMapFromFile(
        Configuration config)
    {
        String fName = 
            config.GetConfiguration(Configuration.LVG_DIR) + 
            config.GetConfiguration(Configuration.UNICODE_SYMBOL_FILE);
        String line = null; 
        Hashtable<Character, String> unicodeSymbolMap 
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
                    // use '|' as delimiter to parse token
                    StringTokenizer buf = new StringTokenizer(line, "|", true);
                    // readin fields 1 & 2
                    char inChar = 
                        (char) UnicodeUtil.UnicodeHexToNum(buf.nextToken());
                    Character symbol = new Character(inChar);
                    buf.nextToken();    // delimiter    
                    // when the next field ASCII str is |
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
                                // add all "|"s before non "|" to mapStr
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
                            "** Warning: Illegal format in symbol file: '"
                            + fName + "'.");
                        System.err.println(line);
                    }
                    else
                    {
                        unicodeSymbolMap.put(symbol, mapStr);
                    }
                }
            }
            in.close();
        }
        catch (Exception e)
        {
            System.err.println(
                "** Error: problem of opening/reading Unicode symbol file: '" + 
                fName + "'.");
            System.err.println("Exception: " + e.toString());
        }
        return unicodeSymbolMap;
    }
    /**
    * Get unicode synonym
    *
    * @param   inChar   an input character
    * @param   unicodeSymbolMap   a hash table contains the unicode symbols
    *
    * @return  the mapped unicode symbol string in ASCII
    */
    public static String MapUnicodeSymbolToAscii(char inChar, 
        Hashtable<Character, String> unicodeSymbolMap)
    {
        String outStr = UnicodeUtil.CharToStr(inChar);
        if(unicodeSymbolMap.containsKey(inChar))
        {
            outStr = unicodeSymbolMap.get(inChar);
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
        Hashtable<Character, String> symbolMap 
            = GetSymbolMapFromFile(conf);
        // mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = ToMapSymbolToAscii.Mutate(in, 
            symbolMap, true, true);
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<LexItem> MapSymbolToAscii(LexItem in,
        Hashtable<Character, String> unicodeSymbolMap,
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
            // map unicode symbol to ASCII Str if in the mapping table
            char curChar = inStr.charAt(i);
            String opStr = NO_OPERATION + fs;
            Character key = new Character(curChar);
            if(unicodeSymbolMap.containsKey(key))
            {
                buffer.append(unicodeSymbolMap.get(key));
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
        LexItem temp = UpdateLexItem(in, term, Flow.MAP_SYMBOL_TO_ASCII, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    // data members
    private static final String INFO = "Map Symbol to ASCII";
    final private static String NO_OPERATION = "NO";
    final private static String MAPPING = "MP";
}
