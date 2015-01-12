package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class generates inflectional variants with specifying categories and
* inflections.  It utilizes classes of ToInflection and OutputFilter.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/flow/inflectionByCatInfl.html">
* Design Document </a>
* @see ToInflection
* @see OutputFilter
*
* @version    V-2013
****************************************************************************/
public class ToInflectionByCatInfl extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG persistent trie
    * @param   restrictFlag   a numerical flag to filter the output.  It's 
    * values are defined in OutputFilter as:
    * <br>LVG_ONLY, LVG_OR_ALL, ALL. 
    * @param   outCategory  the specified categories filter. 
    * Inflectional variants contain all these categories will be in the results.
    * @param   outInflection  the specified categories filter.
    * all these categories will be in the output results.
    * Inflectional variants contain all these inflections will be in the 
    * results.
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    * @see OutputFilter
    * @see Category
    * @see Inflection
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, int restrictFlag, long outCategory,
        long outInflection, boolean detailsFlag, boolean mutateFlag) 
    {
        // Mutate: retrieve EUI & uninflected term from Inflected term
        // calling package method from Toinflection is much slower than private
        StringBuffer buffer = new StringBuffer();
        buffer.append(INFO);
        buffer.append(" <");
        buffer.append(outCategory);
        buffer.append(", ");
        buffer.append(outInflection);
        buffer.append(">");
        String infoStr = buffer.toString();
        Vector<LexItem> ori = ToInflection.InflectWords(in, conn, trie, 
            restrictFlag, infoStr, detailsFlag, mutateFlag, 
            Flow.INFLECTION_BY_CAT_INFL);
        // restrict output by category & inflection
        Vector<LexItem> out 
            = RestrictOutputByCatInfl(ori, outCategory, outInflection);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "sleep");
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            boolean isInflection = true;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
            if(conn != null)
            {
                outs = ToInflectionByCatInfl.Mutate(in, conn, trie, 
                    OutputFilter.LVG_OR_ALL, 128, 1, true, true);
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
    // private methods
    private static Vector<LexItem> RestrictOutputByCatInfl(Vector<LexItem> in, 
        long category, long inflection)
    {
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < in.size(); i++)
        {
            LexItem temp = in.elementAt(i);
            // filter out items by category and inflections
            if(OutputFilter.IsRecContainCategoryInflection(temp, category, 
                inflection))
            {
                out.addElement(temp);
            }
        }
        return out;
    }
    // data members
    private static final String INFO = "InflectionByCatInfl";
}
