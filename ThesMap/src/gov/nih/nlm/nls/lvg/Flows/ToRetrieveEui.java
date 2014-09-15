package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class retrieves the unique EUI's for a specified term.  It returns 
* nothing if the specified term is not in the the Lvg database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/retrieveEui.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToRetrieveEui extends Transformation implements Cloneable
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
        // mutate the term
        Vector<EuiRecord> termList = GetEui(in.GetSourceTerm(), conn);
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        for(int i = 0; i < termList.size(); i++)
        {
            EuiRecord rec = termList.elementAt(i);
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
            LexItem temp = UpdateLexItem(in, rec.GetEui(), Flow.RETRIEVE_EUI, 
                rec.GetCategory(), rec.GetInflection(), details, mutate);
            out.addElement(temp);
        }
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // read in configuration file: for data base info
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "building");    // input String
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToRetrieveEui.Mutate(in, conn, true, true);
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
    private static Vector<EuiRecord> GetEui(String inStr, Connection conn) 
        throws SQLException
    {
        Vector<EuiRecord> out = DbEui.GetEuisByInflectedTerm(inStr, conn);
        return out;
    }
    // data members
    private static final String INFO = "Retrieve EUI";
}
