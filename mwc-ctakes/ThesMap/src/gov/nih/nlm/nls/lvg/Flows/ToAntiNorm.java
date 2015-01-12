package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class returns reversed norm of lexicon terms for the input.
* It find normalizations for the input term and then use the normalized form 
* to find the lexicon terms from antiNorm database.
* This process is similar to the reverse process of norm and thus is called
* antiNorm.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/antiNorm.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToAntiNorm extends Transformation
{
    // public methods
    /**
    * Performs the mutation of this flow component.
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
        // Mutate the term: retrieve the normalized form
        Vector<LexItem> norms = ToNormalize.Mutate(in, maxTerm, stopWords, 
            conn, trie, symbolMap, unicodeMap, ligatureMap, diacriticMap, 
            nonStripMap, removeSTree, detailsFlag, mutateFlag);
        // Get lexicon term from AntiNorm Table
        Vector<LexItem> antiNorms = new Vector<LexItem>();
        Vector<LexItem> out = new Vector<LexItem>();
        // go through all normalized term list
        for(int i = 0; i < norms.size(); i++)
        {
            LexItem norm = norms.elementAt(i);
            String normStr = norm.GetTargetTerm();
            // find the lexicon term from antiNorm table
            Vector<AntiNormRecord> normAntiNorms 
                = DbAntiNorm.GetAntiNorms(normStr, conn);
            // details & mutate
            for(int j = 0; j < normAntiNorms.size(); j++)
            {
                AntiNormRecord cur = normAntiNorms.elementAt(j);
                String details = null;
                String mutate = null;
                if(detailsFlag == true)
                {
                    details = INFO;    // detail mutate info
                }
                if(mutateFlag == true)
                {
                    mutate = cur.GetEui() + GlobalBehavior.GetFieldSeparator();
                }
                // update target LexItems  
                String term = cur.GetInflectedTerm();
                int cat = cur.GetCategory();
                long infl = cur.GetInflection();
                LexItem temp = UpdateLexItem(in, term, Flow.ANTINORM, 
                    cat, infl, details, mutate);
                out = AddToAntiNormOutput(out, temp);
            }
        }
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file: for data base info
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "Rendu-Osler disease");
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
                outs = ToAntiNorm.Mutate(in, maxTerm, stopWords, conn, trie, 
                    symbolMap, unicodeMap, ligatureMap, diacriticMap,
                    nonStripMap, removeSTree, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);             // print out results
    }
    // private methods
    private static Vector<LexItem> AddToAntiNormOutput(Vector<LexItem> in, 
        LexItem cur)
    {
        boolean existFlag = false;
        Vector<LexItem> out = new Vector<LexItem>(in);
        for(int i = 0; i < in.size(); i++)
        {
            LexItem temp = in.elementAt(i);
            if((temp.GetTargetTerm().equals(cur.GetTargetTerm()) == true)
            && (temp.GetTargetCategory().GetValue() 
              == cur.GetTargetCategory().GetValue())
            && (temp.GetTargetInflection().GetValue() 
              == cur.GetTargetInflection().GetValue()))
            {
                if((temp.GetMutateInformation() == null)
                 || (cur.GetMutateInformation() == null))
                {
                    existFlag = true;
                    break;
                }
                else if(temp.GetMutateInformation().equals(
                    cur.GetMutateInformation()) == true)
                {
                    existFlag = true;
                    break;
                }
            }
        }
        if(existFlag == false)
        {
            out.addElement(cur);
        }
        return out;
    }
    // data members
    private static final String INFO = "AntiNorm";
}
