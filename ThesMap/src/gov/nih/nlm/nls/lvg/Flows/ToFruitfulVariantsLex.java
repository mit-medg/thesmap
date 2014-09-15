package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class generates fruitful variants known to lexicon.  This flow is
* identical with generate all fruitful variants except for filtering variant
* not in the lexicon.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/fruitfulVariantsLex.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFruitfulVariantsLex extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trieI  LVG ram trie for inflections
    * @param   trieD  LVG ram trie for derivations
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trieI, RamTrie trieD, boolean detailsFlag, boolean mutateFlag)
        throws SQLException
    {
        // get no operation with legal category and inflection
        Vector<LexItem> originalSet = ToFruitfulVariants.GetNoOperationSet(in, 
            conn, trieI, detailsFlag, true);
        // get the variant set
        Vector<LexItem> variantSet = ToFruitfulVariants.GetVariantSet(in, 
            conn, trieI, trieD, detailsFlag);
        // get Lexical known spelling and inflectional variants of above set
        Vector<LexItem> sivSet = ToFruitfulVariants.GetSpellingAndInflections(
            variantSet, conn, trieI, detailsFlag, OutputFilter.LVG_ONLY);
        // add original to fruitful variants
        originalSet = ToFruitfulVariants.AddToVariantList(originalSet, sivSet);
        // filter out LexItems with shortest distance,
        // update mutate information, and clean up history
        String flowName = Flow.GetBitName(Flow.FRUITFUL_VARIANTS_LEX, 1);
        Vector<LexItem> outs = 
            ToFruitfulVariants.GetFinalSet(originalSet, flowName, mutateFlag);
        return outs;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "neurological");   // get input String
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        int minTrieStemLength = Integer.parseInt(
            conf.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            RamTrie trieI = new RamTrie(true, minTermLen, lvgDir, 0);
            RamTrie trieD = 
                new RamTrie(false, minTermLen, lvgDir, minTrieStemLength);
            if(conn != null)
            {
                outs = ToFruitfulVariantsLex.Mutate(in, conn, trieI, trieD, 
                    true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // data members
    private static final String INFO = "Generate Known Fruitful Variants";
}
