package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class provides features of generating known acronym expansions for a
* specified acronym.  The input acronym is stripped punctuationand
* then lowercase before put into SQL for database query.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/expansion.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToExpansions extends Transformation implements Cloneable
{
    // public method
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
        Vector<AcronymRecord> records = GetExpansions(in.GetSourceTerm(), conn);
        // update target LexItem
        Vector<LexItem> out = new Vector<LexItem>();
        int size = records.size();
        // tag: set true if output is unique, used in frutiful variants
        boolean curTagFlag = ((size==1)?true:false);
        Tag tag = new Tag(in.GetTag());
        tag.SetBitFlag(Tag.UNIQUE_ACR_EXP_BIT, curTagFlag);
        String tagStr = ((curTagFlag)?"Unique":"NotUnique");
        in.SetTag(tag.GetValue());
        for(int i = 0; i < size; i++)
        {
            AcronymRecord record = records.elementAt(i);
            String term = record.GetExpansion();
            // details & mutate
            String details = null;
            String mutate = null;
            if(detailsFlag == true)
            {
                details = INFO + " (" + record.GetType() + ")";
            }
            if(mutateFlag == true)
            {
                mutate = record.GetType() + GlobalBehavior.GetFieldSeparator()
                    + tagStr + GlobalBehavior.GetFieldSeparator();
            }
            LexItem temp = UpdateLexItem(in, term, Flow.EXPANSIONS, 
                Transformation.UPDATE, 
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
        String testStr = GetTestStr(args, "ER");
        // Mutate
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToExpansions.Mutate(in, conn, true, true);
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
    private static Vector<AcronymRecord> GetExpansions(String inStr, 
        Connection conn)
    {
        // strip punctuations & lowercase
        String strippedStr = ToStripPunctuation.StripPunctuation(inStr);
        String lcStrippedStr = strippedStr.toLowerCase();
        Vector<AcronymRecord> out = new Vector<AcronymRecord>();
        try
        {
            out = DbAcronym.GetExpansions(lcStrippedStr, conn);
        }
        catch (SQLException e) { }
        return out;
    }
    // data members
    private static final String INFO = "Generate Acronym Expansions";
}
