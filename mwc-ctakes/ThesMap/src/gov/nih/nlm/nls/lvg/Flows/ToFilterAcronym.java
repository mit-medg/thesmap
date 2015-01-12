package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class filters out terms if they are Acronyms and Abbreviations.  This
* class is case sensitive.  Acronyms and abbreviations are pre-computed and
* are put in the Acronym table in Lvg database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/filterAcronym.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFilterAcronym extends Transformation implements Cloneable
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
        boolean isAcronym = IsAcronym(in.GetSourceTerm(), conn);
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        // if the input is an Acronym
        if(isAcronym == false)
        {
            String term = in.GetSourceTerm();
            // details & mutate
            String details = null;
            String mutate = null;
            if(detailsFlag == true)
            {
                details = INFO;
            }
            if(mutateFlag == true)
            {
                mutate = Transformation.NO_MUTATE_INFO;
            }
            LexItem temp = UpdateLexItem(in, term, Flow.FILTER_ACRONYM, 
                Category.ALL_BIT_VALUE, Inflection.ALL_BIT_VALUE, 
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
        String testStr = GetTestStr(args, "AIDS");
        // Mutate
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToFilterAcronym.Mutate(in, conn, true, true);
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
    // check if the term is a acronym
    private static boolean IsAcronym(String inStr, Connection conn)
    {
        // strip punctuations & lowercase
        String strippedStr = ToStripPunctuation.StripPunctuation(inStr);
        String lcStrippedStr = strippedStr.toLowerCase();
        boolean isAcronym = false;
        try
        {
            isAcronym = DbAcronym.IsAcronym(lcStrippedStr, conn);
        }
        catch (SQLException e) { }
        return isAcronym;
    }
    // data members
    private static final String INFO = "Filter Out Acronyms";
}
