package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class retrieves a pre-computed canonical form from a specified input 
* term which is assumed to be an uninflected form.
* All canonical form are pre-computed and put in a database table, Canonical.
* If the input is not an uninflected form, the database will not contain it.
* This class returns the input term itself (with number: -1) if it is not
* found in the database.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/canonicalize.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToCanonicalize extends Transformation
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
        // Mutate the term: retrieve the Canonical Form for an UnInflected term
        CanonInfo canonInfo = 
            CanonicalizeWords(in.GetSourceTerm().toLowerCase(), conn);
        String term = canonInfo.GetCanonTerm(); 
        String numberStr = canonInfo.GetCanonNum();        // Canonical number
        // details & mutate
        String details = null;
        String mutate = null;
        if(detailsFlag == true)
        {
            details = INFO + " (" + numberStr + ")";    // detail mutate info
        }
        if(mutateFlag == true)
        {
            mutate = numberStr + GlobalBehavior.GetFieldSeparator();
        }
        // update target LexItems  
        Vector<LexItem> out = new Vector<LexItem>();
        LexItem temp = UpdateLexItem(in, term, Flow.CANONICALIZE, 
            Transformation.UPDATE, Transformation.UPDATE, details, mutate);
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
        String testStr = GetTestStr(args, "being");
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToCanonicalize.Mutate(in, conn, true, true);
            }
            DbBase.CloseConnection(conn, conf);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
        PrintResults(in, outs);             // print out results
    }
    // private methods
    private static CanonInfo CanonicalizeWords(String inStr, Connection conn) 
        throws SQLException
    {
        StringBuffer out = new StringBuffer();
        StringBuffer canonId = new StringBuffer();
        StringTokenizer buf = new StringTokenizer(inStr, " \t");
        Vector<CanonRecord> canonList = new Vector<CanonRecord>();
        //Canonicalize terms
        // Canonicalize each words in the string
        while(buf.hasMoreTokens() == true)
        {
            String curStr = buf.nextToken();
            CanonRecord temp = CanonicalizeWord(curStr, conn);
            canonList.addElement(temp);
        }
        // form the out String
        for(int i = 0; i < canonList.size(); i++)
        {
            CanonRecord temp = canonList.elementAt(i);
            out.append(temp.GetCanonicalizedTerm());
            out.append(" ");
            canonId.append(temp.GetCanonicalId());
            canonId.append(" ");
        }
        CanonInfo canonInfo = new CanonInfo(out.toString().trim(), 
            canonId.toString().trim()); 
        return canonInfo;
    }
    private static CanonRecord CanonicalizeWord(String inWord, Connection conn)
        throws SQLException
    {
        CanonRecord out = new CanonRecord(inWord);
        // retrieve the Canonical Form for an UnInflected term
        Vector<CanonRecord> outList = DbCanon.GetCanons(inWord, conn);
        if(outList.size() == 1)         // legal result
        {
            out = outList.elementAt(0);
        }
        return out;
    }
    // inner class: only used by this class
    private static class CanonInfo
    {
        private CanonInfo(String canonTerm, String canonNum)
        {
            canonTerm_ = canonTerm;
            canonNum_ = canonNum;
        }
        private String GetCanonTerm()
        {
            return canonTerm_;
        }
        private String GetCanonNum()
        {
            return canonNum_;
        }
        private String canonTerm_ = null;
        private String canonNum_ = null;
    }
    // data members
    private static final String INFO = "Canonicalize";
}
