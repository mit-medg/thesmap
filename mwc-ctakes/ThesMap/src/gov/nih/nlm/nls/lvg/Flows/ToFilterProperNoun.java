package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class filters out terms if they are proper nouns.  This class is case 
* sensitive.  Proper nouns are pre-computed and put in the ProperNoun table
* in the LVG database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/filterProperNoun.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFilterProperNoun extends Transformation implements Cloneable
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
        boolean isProperNoun = IsProperNoun(in.GetSourceTerm(), conn);
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        // if the input is a proper noun
        if(isProperNoun == false)
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
            LexItem temp = UpdateLexItem(in, term, Flow.FILTER_PROPER_NOUN, 
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
        String testStr = GetTestStr(args, "Adam");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToFilterProperNoun.Mutate(in, conn, true, true);
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
    // check if a specified term is a proper noun
    private static boolean IsProperNoun(String inStr, Connection conn)
    {
        boolean isProperNoun = false;
        try
        {
            isProperNoun = DbProperNoun.IsProperNoun(inStr, conn);
        }
        catch (SQLException e) { }
        return isProperNoun;
    }
    // data members
    private static final String INFO = "Filter Proper Noun";
}
