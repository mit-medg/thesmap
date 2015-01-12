package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class generates synonyms from the specified term.  The synonyms are 
* pre-computed and put in the Synonym table of the LVG database.  The input
* term need to be stripped punctuation and then lowercased in SQL query before 
* being sent to Lvg database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/synonym.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToSynonyms extends Transformation implements Cloneable
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
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        boolean detailsFlag, boolean mutateFlag)
    {
        // mutate: 
        Vector<SynonymRecord> records = GetSynonyms(in.GetSourceTerm(), conn);
        long inCat = in.GetSourceCategory().GetValue();
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < records.size(); i++)
        {
            SynonymRecord record = records.elementAt(i);
            String term = record.GetSynonym();
            long curCat = record.GetCat1();
            // input filter: category
            if(InputFilter.IsLegal(inCat, curCat) == false)
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
                String fs = GlobalBehavior.GetFieldSeparator();
                mutate = "FACT" + fs
                    + record.GetKeyFormNpLc() + fs
                    + record.GetKeyForm() + fs
                    + Category.ToName(record.GetCat1()) + fs
                    + record.GetSynonym() + fs
                    + Category.ToName(record.GetCat2()) + fs;
            }
            LexItem temp = UpdateLexItem(in, term, Flow.SYNONYMS, 
                record.GetCat2(), 
                Inflection.GetBitValue(Inflection.BASE_BIT), 
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
        String testStr = GetTestStr(args, "aminophylline");
        // Mutate
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToSynonyms.Mutate(in, conn, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);     // print out results
    }
    // private methods
    private static Vector<SynonymRecord> GetSynonyms(String inStr, 
        Connection conn)
    {
        // strip punctuations & lowercase
        String strippedStr = ToStripPunctuation.StripPunctuation(inStr);
        String lcStrippedStr = strippedStr.toLowerCase();
        Vector<SynonymRecord> out = new Vector<SynonymRecord>();
        // get synonyms from LvgDB
        try
        {
            out = DbSynonym.GetSynonyms(lcStrippedStr, conn);
        }
        catch (SQLException e) { }
        return out;
    }
    // data members
    private static final String INFO = "Generate Synonyms";
}
