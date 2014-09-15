package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Db.*;
/*****************************************************************************
* This class generates synonyms from the input term, recursively, until there 
* are no more, or until a cycle is detected.  The input term needs to be 
* stripped punctuationand then lowercased in SQL query.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/rSynonym.html">
* Design Document </a>
* @see ToSynonyms
*
* @version    V-2013
****************************************************************************/
public class ToRecursiveSynonyms extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    * @param   detailFlowFlag   a boolean flag for showing the flow history in
    * details
    *
    * @return  Vector<LexItem> - results from this flow component
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        boolean detailsFlag, boolean mutateFlag, boolean detailFlowFlag)
    {
        // mutate: 
        SetSynonymVector(in);        // set the input to the 1st term 
        GetRecursiveSynonyms(in, conn, INFO, true, detailsFlag, mutateFlag, 
            null);
        CleanSynonymVector(detailFlowFlag);    // remove the original term
        Vector<LexItem> out = GetSynonymVector();
        return out;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "chest");
        // Mutate
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            if(conn != null)
            {
                outs = ToRecursiveSynonyms.Mutate(in, conn, true, true, false);
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
    // Get synonyms, recursively
    private static void GetRecursiveSynonyms(LexItem in, Connection conn,
        String infoStr, boolean topLevel, boolean detailsFlag, 
        boolean mutateFlag, String rFlowHistory)
    {
        // calculate detail recursive history
        // if first time, set flow history to original history
        StringBuffer buffer = new StringBuffer();
        if(rFlowHistory == null)
        {
            String prevHistory = in.GetFlowHistory();
            rFlowHistory = new String();
            if(prevHistory == null)
            {
                rFlowHistory = new String();
            }
            else
            {
                buffer.append(prevHistory);
                buffer.append("+");
            }
        }
        buffer.append(Flow.GetBitName(Flow.SYNONYMS, 1));
        rFlowHistory += buffer.toString();
        Vector<LexItem> temp = GetSynonyms(in, conn, infoStr, topLevel, 
            detailsFlag, mutateFlag, rFlowHistory);
        // go through all elements in new Vector
        for(int i = 0; i < temp.size(); i++)
        {
            LexItem tempRec = temp.elementAt(i);
            // go through all elements in an accumulate synonyms Vector
            boolean existed = false;
            for(int j = 0; j < synonyms_.size(); j++)
            {
                // Update category and inflection if the term exists
                LexItem orgRec = synonyms_.elementAt(j);
                // check only the term for the first one (original term)
                if(j == 0)
                {
                    if(orgRec.GetTargetTerm().equals(tempRec.GetTargetTerm()))
                    {
                        existed = true;
                        break;
                    }
                }
                else
                {
                    // lexItems are same if they has same target term, cat, infl
                    // inflection are all set to base (1), no need to check
                    if((orgRec.GetTargetTerm().equals(tempRec.GetTargetTerm()))
                    && (orgRec.GetTargetCategory().GetValue() ==
                        tempRec.GetTargetCategory().GetValue()))
                    {
                        existed = true;
                        break;
                    }
                }
            }
            // if the synonym does not exist, add into Vector
            if(existed == false)
            {
                synonyms_.addElement(tempRec);
                LexItem newLexItem = LexItem.TargetToSource(tempRec);
                GetRecursiveSynonyms(newLexItem, conn, infoStr, false, 
                    detailsFlag, mutateFlag, rFlowHistory);
            }
        }
    }
    // reset the synonym vector by adding a new synonym record with a string
    private static void SetSynonymVector(LexItem in)
    {
        synonyms_.removeAllElements();
        LexItem lexItem = new LexItem(in, true);
        lexItem.SetTargetTerm(in.GetSourceTerm());
        synonyms_.addElement(lexItem);
    }
    // remove the very first elements since it is the original term
    private static void CleanSynonymVector(boolean detailFlowFlag)
    {
        // revove the first LexItem, which is the original input
        synonyms_.removeElementAt(0);
        // change the detail flow flag to "r"
        if(detailFlowFlag == false)
        {
            String flowName = Flow.GetBitName(Flow.RECURSIVE_SYNONYMS, 1);
            for(int i = 0; i < synonyms_.size(); i++)
            {
                LexItem temp = synonyms_.elementAt(i);
                temp.SetFlowHistory(flowName);
            }
        }
    }
    // Get the synonym vector, all recursive synonym records
    private static Vector<LexItem> GetSynonymVector()
    {
        return synonyms_;
    }
    // get synonym from Lvg Db
    private static Vector<LexItem> GetSynonyms(LexItem in, Connection conn,
        String infoStr, boolean appendFlowHistory, boolean detailsFlag, 
        boolean mutateFlag, String rFlowHistory)
    {
        String inStr = in.GetSourceTerm();
        Vector<LexItem> out = new Vector<LexItem>();
        // strip punctuations & lowercase
        String strippedStr = ToStripPunctuation.StripPunctuation(inStr);
        String lcStrippedStr = strippedStr.toLowerCase();
        // set flow history for detail information
        String flowName = rFlowHistory;
        try
        {
            // get synonyms from database
            Vector<SynonymRecord> records 
                = DbSynonym.GetSynonyms(lcStrippedStr, conn);
            long inCat = in.GetSourceCategory().GetValue();
            // update LexItems
            for(int i = 0; i < records.size(); i++)
            {
                SynonymRecord record = records.elementAt(i);
                String term = record.GetSynonym();
                long curCat = record.GetCat1();
                // input filter: category
                // No inflection infomation in the database table
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
                    mutate = "FACT" + fs
                        + record.GetKeyForm() + fs
                        + Category.ToName(record.GetCat1()) + fs
                        + record.GetSynonym() + fs
                        + Category.ToName(record.GetCat2()) + fs
                        + rFlowHistory + fs;
                }
                LexItem temp = UpdateLexItem(in, term, flowName, 
                    record.GetCat2(), 
                    Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate, false);
                out.addElement(temp);
            }
        }
        catch (SQLException e) { }
        return out;
    }
    // data members
    private static final String INFO = "Recursive Synonyms";
    private static Vector<LexItem> synonyms_ = new Vector<LexItem>();
}
