package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class gets the lexical name for a specified term.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/nominalization.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToNominalization extends Transformation
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
            = GetNominalization(in, conn, INFO, detailsFlag, mutateFlag);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "able");
        // Mutate connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToNominalization.Mutate(in, conn, true, true);
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
    * Get nominalization from a specified term from Lexicon.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   infoStr   the header of detail information, usually is the
    * full name of the current flow
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @see DbBase
    */
    private static Vector<LexItem> GetNominalization(LexItem in, 
        Connection conn, String infoStr, boolean detailsFlag, 
        boolean mutateFlag)
    {
        String inStr = in.GetSourceTerm();
        Vector<LexItem> out = new Vector<LexItem>();
        try
        {
            // get nominalizations from database
            Vector<NominalizationRecord> nomList 
                = DbNominalization.GetNominalizations(inStr, conn); 
            // update LexItems
            // go through all records from the results of fact DB
            for(int i = 0; i < nomList.size(); i++)
            {
                NominalizationRecord record = nomList.elementAt(i);
                String inTerm = record.GetNominalization1();
                String inEui = record.GetEui1();
                long inCat = (long) record.GetCat1();
                String nomTerm = record.GetNominalization2();
                String nomEui = record.GetEui2();
                long nomCat = (long) record.GetCat2();
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
                    mutate = inEui + fs + Category.ToName(inCat) + fs 
                        + nomEui + fs;
                }
                // update LexItem's info
                LexItem temp = UpdateLexItem(in, nomTerm, Flow.NOMINALIZATION, 
                    nomCat, Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate);
            
                out.addElement(temp);
            }
        }
        catch (Exception e) { }
        return out;
    }
    // data members
    private static final String INFO = "Nominalization";
}
