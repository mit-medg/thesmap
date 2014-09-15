package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class gets the lexical name (citation form) for a specified term.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/citation.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToCitation extends Transformation
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
    * @exception SQLException if errors occurr while connect to LVG database.
    *
    * @see DbBase
    * @see PersistentTrie
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        // mutate the term
        Vector<LexItem> out 
            = GetCitation(in, conn, INFO, detailsFlag, mutateFlag);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "Colour");
        // Mutate connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToCitation.Mutate(in, conn, true, true);
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
    /**
    * Get lexical name from a specified term from Lvg facts and rules.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   infoStr   the header of detail information, usually is the
    * full name of the current flow
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  the results from this flow component - a collection (Vector) 
    * of LexItems
    *
    * @see DbBase
    */
    private static Vector<LexItem> GetCitation(LexItem in, Connection conn,
        String infoStr, boolean detailsFlag, boolean mutateFlag)
    {
        String inStr = in.GetSourceTerm();
        long inCat = in.GetSourceCategory().GetValue();
        long inInfl = in.GetSourceInflection().GetValue();
        Vector<LexItem> out = new Vector<LexItem>();
        try
        {
            // fact get citation from database
            Vector<InflectionRecord> factList 
                = DbCitation.GetCitations(inStr, conn); 
            // update LexItems
            // go through all records from the results of fact DB
            for(int i = 0; i < factList.size(); i++)
            {
                InflectionRecord record = factList.elementAt(i);
                String citation = record.GetCitationTerm();
                String unTerm = record.GetUninflectedTerm();
                long curCat = record.GetCategory();
                long curInfl = record.GetInflection();
                // input filter of category & inflection
                if(InputFilter.IsLegal(inCat, curCat) == false)
                {
                    continue;
                }
                // details & mutate
                String details = null;
                String mutate = null;
                if(detailsFlag == true)
                {
                    details = infoStr;
                }
                if(mutateFlag == true)
                {
                    String fs = GlobalBehavior.GetFieldSeparator();
                    mutate = inStr + fs + unTerm + fs
                        + Category.ToName(curCat) + fs
                        + Inflection.ToName(curInfl) + fs
                        + citation + fs + record.GetEui() + fs;
                }
                // update LexItem's info
                LexItem temp = UpdateLexItem(in, citation, Flow.CITATION, 
                    curCat, Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate);
            
                out.addElement(temp);
            }
        }
        catch (Exception e) { }
        return out;
    }
    // data members
    private static final String INFO = "Citation";
}
