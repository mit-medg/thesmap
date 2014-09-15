package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class generates known spelling variants.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/generateSpellingVariants.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToSpellingVariants extends Transformation implements Cloneable
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
    * @return  Vecotr<LexItem> - results from this flow component
    * of LexItems
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn,
        boolean detailsFlag, boolean mutateFlag)
    {
        // Mutate: 
        Vector<InflectionRecord> records 
            = SpellingVariant(in.GetSourceTerm(), conn);
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        // update target
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < records.size(); i++)
        {
            InflectionRecord record = records.elementAt(i);
            String term = record.GetInflectedTerm();
            long curCat = record.GetCategory();
            long curInfl = record.GetInflection();
            // input filter: category * inflection
            if(InputFilter.IsLegal(inCat, inInfl, curCat, curInfl) == false)
            {
                continue;
            }
            // details & mutate
            String details = null;
            String mutate = null;
            if(detailsFlag == true)
            {
                details = INFO;
            }
            if(mutateFlag == true)
            {
                mutate = record.GetEui() + GlobalBehavior.GetFieldSeparator();
            }
            LexItem temp = UpdateLexItem(in, term, 
                Flow.GENERATE_SPELLING_VARIANTS, curCat, curInfl, details, 
                mutate);
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
        String testStr = GetTestStr(args, "left");      // get input String
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToSpellingVariants.Mutate(in, conn, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // protected methods
    /**
    * Get spelling variants from Lvg DB
    *
    * @param   inStr  the input term which will be found for it's spelling
    * variants
    * @param   conn   LVG database connection
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of InflectionRecord
    *
    * @see DbBase
    * @see InflectionRecord
    */
    protected static Vector<InflectionRecord> SpellingVariant(String inStr, 
        Connection conn)
    {
        Vector<InflectionRecord> out = new Vector<InflectionRecord>();
        try
        {
            out = DbSpellingVariants.GetSpellingVariants(inStr, conn);
        }
        catch (SQLException e) { }
        return out;
    }
    // data members
    private static final String INFO = "Spelling Variants";
}
