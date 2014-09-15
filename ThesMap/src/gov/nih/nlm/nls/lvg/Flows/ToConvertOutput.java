package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class converts the output of the Xerox Parc stochastic tagger to lvg 
* style pipe delimited format.
* <br> The format of Xerox Parc stochastic tagger is: ['term', 'category']
* <br> The format of Lvg style pipe delimite format is: 
* in term|out term|category|inflection|Flow History| 
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/convertOutput.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToConvertOutput extends Transformation implements Cloneable
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
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        boolean detailsFlag, boolean mutateFlag) throws SQLException
    {
        Vector<LexItem> out = new Vector<LexItem>();
        // mutate the term
        XeroxParc xp = new XeroxParc(in.GetSourceTerm());
        String term = xp.GetTerm();
        long cat = Category.ALL_BIT_VALUE;
        long infl = Inflection.ALL_BIT_VALUE;
        if(xp.IsLegal() == true)
        {
            cat = xp.GetCategoryValue();
            infl = DbInflection.GetInflByCat(term, (int)cat, conn);
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
            mutate = Transformation.NO_MUTATE_INFO;
        }
        // updatea target 
        LexItem temp = UpdateLexItem(in, term, Flow.CONVERT_OUTPUT, 
            cat, infl, details, mutate);
        out.addElement(temp);
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file: for data base info
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "['elderly', 'adj']");
        // mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToConvertOutput.Mutate(in, conn, true, true);
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
    // data members
    private static final String INFO = "Convert";
}
