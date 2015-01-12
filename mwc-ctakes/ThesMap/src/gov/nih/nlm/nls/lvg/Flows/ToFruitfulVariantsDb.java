package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class provides features of generating all fruitful variants known to
* lexicon from precomputed data.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/fruitfulVariantsDb.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFruitfulVariantsDb extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - the results from this flow component
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate: Get acronyms record from Lvg database 
        Vector<FruitfulRecord> records 
            = GetFruitfulVariants(in.GetSourceTerm(), conn);
        // update target LexItem by going through all acronym records
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < records.size(); i++)
        {
            FruitfulRecord record = records.elementAt(i);
            String term = record.GetVariantTerm();
            // details & mutate
            String details = null;
            String mutate = null;
            if(detailsFlag == true)
            {
                details = INFO; 
            }
            if(mutateFlag == true)
            {
                String fs = GlobalBehavior.GetFieldSeparator();
                mutate = record.GetOriginalCategory() + fs
                    + record.GetOriginalInflection() + fs
                    + record.GetFlowHistory() + fs
                    + record.GetDistance() + fs
                    + record.GetTagInformation() + fs;
            }
            // update output LexItem
            LexItem temp = UpdateLexItem(in, term, Flow.FRUITFUL_VARIANTS_DB, 
                record.GetCategory(), record.GetInflection(),
                details, mutate);
            out.addElement(temp);
        }
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "neurological");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToFruitfulVariantsDb.Mutate(in, conn, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // private method
    private static Vector<FruitfulRecord> GetFruitfulVariants(String inStr, 
        Connection conn)
    {
        // strip punctuations & lowercase
        String strLc = inStr.toLowerCase();
        Vector<FruitfulRecord> out = new Vector<FruitfulRecord>();
        try
        {
            out = DbFruitful.GetFruitfulVariants(strLc, conn);
        }
        catch (SQLException e) { }
        return out;
    }
    // data members
    private static final String INFO = "Fruitful Variants Db";
}
