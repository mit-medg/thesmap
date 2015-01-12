package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class remove (s), (es), and (ies) from the input term.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/removeS.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToRemoveS extends Transformation implements Cloneable
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
    public static Vector<LexItem> Mutate(LexItem in, RTrieTree trie, 
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate the term
        String term = RemoveSFromTerm(in.GetSourceTerm(), trie);
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
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.REMOVE_S, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * Read in removeS file name from configuration file and retrun trie tree 
    *
    * @param   config   Configuratin object
    *
    * @return  reversed trie tree for remove s rules
    */
    public static RTrieTree GetRTrieTreeFromFile(Configuration config)
    {
        String fName = config.GetConfiguration(Configuration.LVG_DIR)
            + config.GetConfiguration(Configuration.REMOVE_S_FILE);
        RTrieTree tree = new RTrieTree(fName);
        return tree;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "Ap(s)pCHClpp(s)A");
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        String fName = lvgDir + "/data/rules/removeS.data";
        try
        {
            RTrieTree tree = new RTrieTree(fName);
            // Mutate
            LexItem in = new LexItem(testStr);
            Vector<LexItem> outs = ToRemoveS.Mutate(in, tree, true, true);
            PrintResults(in, outs);     // print out results
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
    // private method
    private static String RemoveSFromTerm(String inStr, RTrieTree tree)
    {
        String outStr = new String();
        // remove (es), (ES), (IES), and (ies)
        outStr = RemovePattern(inStr, ES);
        outStr = RemovePattern(outStr, IES);
        // remove (s) and (S)
        int patternSize = S.length();
        String lowerStr = outStr.toLowerCase();
        int index = lowerStr.indexOf(S);
        int outSize = outStr.length();
        while(index >= 0)
        {
            String str1 = outStr.substring(0, index);
            String str2 = outStr.substring(index+patternSize, outSize);
            // check if pattern match exceptions
            if(tree.FindPattern(str1) == false)
            {
                outStr = str1 + str2;       // remove (s)
                if(str2.length() > 0)       // check to repalce (s) with space
                {
                    char nextChar = str2.charAt(0);
                    if(Character.isLetter(nextChar) == true)
                    {
                        outStr = str1 + " " + str2;
                    }
                }
                lowerStr = outStr.toLowerCase();
                index = lowerStr.indexOf(S);
            }
            else
            {
                lowerStr = outStr.toLowerCase();
                index = lowerStr.indexOf(S, index+1);
            }
            outSize = outStr.length();
        }
        return outStr;
    }
    // ignore case on pattern
    private static String RemovePattern(String term, String pattern)
    {
        String outStr = term;
        String lowerStr = outStr.toLowerCase();
        int patternSize = pattern.length();
        int index = lowerStr.indexOf(pattern);
        int outSize = outStr.length();
        // go through the entire string
        while(index >= 0)
        {
            String str1 = TrimEnd(outStr.substring(0, index));
            String str2 = outStr.substring(index+patternSize, outSize);
            outStr = str1 + str2;
            lowerStr = outStr.toLowerCase();
            index = lowerStr.indexOf(pattern);
            outSize = outStr.length();
        }
        return outStr;
    }
    private static String TrimEnd(String term)
    {
        String outStr = term;
        while(outStr.endsWith(" ") == true)
        {
            outStr = outStr.substring(0, outStr.length()-1);
        }
        return outStr;
    }
    // data members
    private final static String INFO = "Remove (s), (es), (ies)";
    private final static String S = "(s)";
    private final static String ES = "(es)";
    private final static String IES = "(ies)";
}
