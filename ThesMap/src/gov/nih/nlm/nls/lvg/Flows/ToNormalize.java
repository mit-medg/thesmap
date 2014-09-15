package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides features of the normalize flow component.  This flow 
* component consists of 10 other flow components in a serial order.
* They are: q7, g, rs, o, t, l, B, Ct, w, q8.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/normalize.html">
* Design Document </a>
* @see ToMapSymbolToAscii
* @see ToRemoveGenitive
* @see ToRemoveS
* @see ToReplacePunctuationWithSpace
* @see ToStripStopWords
* @see ToLowerCase
* @see ToUninflectWords
* @see ToCitation
* @see ToUnicodeCoreNorm
* @see ToStripMapUnicode
* @see ToSortWordsByOrder
*
* @version    V-2013
****************************************************************************/
public class ToNormalize extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.  Normalize flow component
    * is significantly changed in release 2008. 
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   maxTerm   the maxinum number of permutation term (uninflect)
    * @param   stopWords   Vector<String> - stop wrods list
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   symbolMap   a hash table contains the unicode symbols mapping
    * @param   unicodeMap   a hash table contains the unicode mapping
    * @param   ligatureMap   a hash table contains the mapping of ligatures
    * @param   diacriticMap  a hash table contains the mapping of diacritics
    * @param   nonStripMap   a hash table contains the non-Strip map unicode
    * @param   removeSTree   a reverse trie tree of removeS pattern rules
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector)
    * of LexItems
    *
    * @exception SQLException if errors occurr while connect to LVG database.
    *
    * @see DbBase
    * @see PersistentTrie
    */
    public static Vector<LexItem> Mutate(LexItem in, int maxTerm, 
        Vector<String> stopWords, Connection conn, RamTrie trie, 
        Hashtable<Character, String> symbolMap,
        Hashtable<Character, String> unicodeMap,
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap, 
        Hashtable<Character, String> nonStripMap,
        RTrieTree removeSTree, boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        Vector<LexItem> outList = new Vector<LexItem>();
        Vector<LexItem> inList = new Vector<LexItem>();
        inList.addElement(in);
        // go through 10 flow components: q7, g, rs, o, t, l, B, Ct, w, q8
        for(int i = 0; i < NORM_STEPS; i++)
        {
            outList = GetNormBySteps(i, inList, maxTerm, stopWords, conn, trie, 
                symbolMap, unicodeMap, ligatureMap, diacriticMap,
                nonStripMap, removeSTree, detailsFlag, mutateFlag);
            // convert the output of current lexItem to input of next LexItem
            inList.removeAllElements();
            for(int j = 0; j < outList.size(); j++)
            {
                LexItem out = outList.elementAt(j);
                LexItem temp = LexItem.TargetToSource(out);
                inList.addElement(temp);
            }
        }
        // no need to update history since it's done in each flow component
        // reset mutate information
        for(int i = 0; i < outList.size(); i++)
        {
            outList.elementAt(i).SetMutateInformation(
                Transformation.NO_MUTATE_INFO);
        }
        
        return outList;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "Left");
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        int maxTerm = Integer.parseInt(
            conf.GetConfiguration(Configuration.MAX_UNINFLS));
        Vector<String> stopWords = ToStripStopWords.GetStopWordsFromFile(conf);
        Hashtable<Character, String> symbolMap
            = ToMapSymbolToAscii.GetSymbolMapFromFile(conf);
        Hashtable<Character, String> unicodeMap
            = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf);
        Hashtable<Character, String> ligatureMap 
            = ToSplitLigatures.GetLigatureMapFromFile(conf);
        Hashtable<Character, Character> diacriticMap 
            = ToStripDiacritics.GetDiacriticMapFromFile(conf);
        Hashtable<Character, String> nonStripMap
            = ToStripMapUnicode.GetNonStripMapFromFile(conf);
        RTrieTree removeSTree = ToRemoveS.GetRTrieTreeFromFile(conf);
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            boolean isInflection = true;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
            if(conn != null)
            {
                outs = ToNormalize.Mutate(in, maxTerm, stopWords, conn, trie, 
                    symbolMap, unicodeMap, ligatureMap, diacriticMap,
                    nonStripMap, removeSTree, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        // print out results
        PrintResults(in, outs);
    }
    // private method
    private static Vector<LexItem> GetNormBySteps(int step, Vector<LexItem> ins,
        int maxTerm, Vector<String> stopWords, Connection conn, RamTrie trie, 
        Hashtable<Character, String> symbolMap,
        Hashtable<Character, String> unicodeMap,
        Hashtable<Character, String> ligatureMap, 
        Hashtable<Character, Character> diacriticMap, 
        Hashtable<Character, String> nonStripMap,
        RTrieTree removeSTree, boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        int index = 0;
        // go through all elements for the ins
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem in = ins.elementAt(i);
            Vector<LexItem> tempOuts = new Vector<LexItem>();
            switch(step)
            {
                case 0:         // -f:q0
                    tempOuts = ToMapSymbolToAscii.Mutate(in, symbolMap,
                        detailsFlag, mutateFlag);
                    break;
                case 1:         // -f:g
                    tempOuts = ToRemoveGenitive.Mutate(in, detailsFlag, 
                        mutateFlag);
                    break;
                case 2:         // -f:rs
                    tempOuts = ToRemoveS.Mutate(in, removeSTree,
                        detailsFlag, mutateFlag);
                    break;
                case 3:         // -f:o
                    tempOuts = ToReplacePunctuationWithSpace.Mutate(in, 
                        detailsFlag, mutateFlag);
                    break;
                case 4:         // -f:t
                    tempOuts = ToStripStopWords.Mutate(in, stopWords,
                        detailsFlag, mutateFlag);
                    break;
                case 5:         // -f:l
                    tempOuts = ToLowerCase.Mutate(in, detailsFlag, mutateFlag);
                    break;
                case 6:         // -f:B
                    tempOuts = ToUninflectWords.Mutate(in, maxTerm, conn, trie, 
                        detailsFlag, mutateFlag);
                    break;
                case 7:     // -f:Ct, don't use -f:ct, varon has non-ASCII Ct
                    tempOuts = GetCitation(in, conn, diacriticMap, ligatureMap);
                    break;
                case 8:         // -f:q7
                    tempOuts = ToUnicodeCoreNorm.Mutate(in, symbolMap,
                        unicodeMap, ligatureMap, diacriticMap,
                        detailsFlag, mutateFlag);
                    break;
                case 9:         // -f:q8
                    tempOuts = ToStripMapUnicode.Mutate(in, 
                        nonStripMap, detailsFlag, mutateFlag);
                    break;
                case 10:         // -f:w
                    tempOuts = ToSortWordsByOrder.Mutate(in, detailsFlag, 
                        mutateFlag);
                    break;
            }
            outs.addAll(tempOuts);
        }
        return outs;
    }
    // Use this method instead of -f:Ct, lowcases and trim after Ct.
    private static Vector<LexItem> GetCitation(LexItem in, Connection conn,
        Hashtable<Character, Character> diacriticMap, 
        Hashtable<Character, String> ligatureMap) throws SQLException
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        String inStr = in.GetSourceTerm();
        String delim = " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^";
        StringTokenizer buf = new StringTokenizer(inStr, delim);
        String newCit = new String();
        while(buf.hasMoreTokens() == true)
        {
            String inBase = buf.nextToken();
            Vector<InflectionRecord> citations = 
                DbCitation.GetCitationsFromBase(inBase, conn);
            // sort citation to ensure consistency accross different database
            CitationComparator<InflectionRecord> cc 
                = new CitationComparator<InflectionRecord>();
            Collections.sort(citations, cc);
            // set citation to inBase in case no citation found
            String citation = inBase;
            if((citations != null) && (citations.size() > 0))
            {
                InflectionRecord record = citations.elementAt(0);
                citation = record.GetCitationTerm();
            }
            newCit += citation + " ";
        }
        // strip diacritics, lowercase, trim
        //newCit = ToStripDiacritics.StripDiacritics(newCit, diacriticMap);
        newCit = newCit.toLowerCase().trim();
        String details = "Citation";
        String mutate = new String();
        LexItem temp = UpdateLexItem(in, newCit, Flow.CITATION,
            in.GetSourceCategory().GetValue(), 
            in.GetSourceInflection().GetValue(), details, mutate);
        outs.add(temp);
        return outs;
    }
    // data members
    private final static int NORM_STEPS = 11;       // current norm
}
