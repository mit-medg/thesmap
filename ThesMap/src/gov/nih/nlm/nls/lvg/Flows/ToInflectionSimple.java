package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides features of inflect a term and show the result with 
* simple inflections.  This flow component consists of 2 other flow components 
* in a serial order. They are: i, Si.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/inflectionSimple.html">
* Design Document </a>
* @see ToInflection
* @see ToSimpleInflections
*
* @version    V-2013
****************************************************************************/
public class ToInflectionSimple extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG Ram trie
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, int restrictFlag, boolean detailsFlag, boolean mutateFlag) 
    {
        // Mutate: Get inflections
        Vector<LexItem> infls = ToInflection.Mutate(in, conn, trie, 
            restrictFlag, detailsFlag, mutateFlag);
        // Get simple inflections
        Hashtable<TermCatInflKey, LexItem> ht 
            = new Hashtable<TermCatInflKey, LexItem>(infls.size());
        String flowName = Flow.GetBitName(Flow.INFLECTION_SIMPLE, 1);
        for(int i = 0; i < infls.size(); i++)
        {
            LexItem curIn = infls.elementAt(i);
            ToSimpleInflections.GetSimpleInflectionsOnTarget(curIn);
            String oldHistory = curIn.GetFlowHistory();
            String flowHistory = oldHistory.substring(0, oldHistory.length()-1) 
                + flowName;
            curIn.SetFlowHistory(flowHistory);
            TermCatInflKey curKey = new TermCatInflKey(curIn.GetTargetTerm(),
                (int)curIn.GetTargetCategory().GetValue(),
                curIn.GetTargetInflection().GetValue());
            ht.put(curKey, curIn);
        }
        Vector<LexItem> outs = new Vector<LexItem>(ht.values());
        return outs;
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
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE,
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn == null)
            {
                System.err.println("** Error: Db connection problem!");
            }
             boolean isInflection = true;
             RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
             if(conn != null)
             {
                outs = ToInflectionSimple.Mutate(in, conn, trie,
                    OutputFilter.LVG_OR_ALL, true, true);
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
    // data members
    private static final String INFO = "Inflection Simple";
}
