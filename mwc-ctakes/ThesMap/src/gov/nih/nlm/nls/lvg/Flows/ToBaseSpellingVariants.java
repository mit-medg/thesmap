package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides features of generating the uninflected spelling variants.
* When the -m option is turned on, the EUI from the lexicon is displayed.
* The implmentation logic is described as follows:
* <ul>
* <li> Generate spelling variants
* <li> Get the uninflected form for all spelling variants
* <li> Retrieve Eui for each uninflected form of all spelling variants
* <li> Remove duplicated output LexItems
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see 
* <a href="../../../../../../../designDoc/UDF/flow/baseSpellingVariants.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToBaseSpellingVariants extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trie   LVG ram trie
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - results from this flow component 
    *
    * @exception SQLException if errors occurr while connect to LVG database.
    *
    * @see DbBase
    */
    // This function can be better coded (directly)
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trie, boolean detailsFlag, boolean mutateFlag) 
        throws SQLException
    {
        Vector<LexItem> out = new Vector<LexItem>();
        long inCat = in.GetSourceCategory().GetValue();
        // Get the know spelling variants first
        Vector<InflectionRecord> records = 
            ToSpellingVariants.SpellingVariant(in.GetSourceTerm(), conn);
        // get uninflected form for all spelling variants
        for(int i = 0; i < records.size(); i++)
        {
            InflectionRecord record = records.elementAt(i);
            String term = record.GetInflectedTerm();
            long curCat = record.GetCategory();
            long curInfl = record.GetInflection();
            // input filter: category & inflection
            if(InputFilter.IsLegal(inCat, curCat) == false)
            {
                continue;
            }
            // setup the LexItem for inputing to uninflection flow
            LexItem tempIn = new LexItem(in, false);
            tempIn.SetSourceTerm(term);
            tempIn.SetSourceCategory(curCat);
            tempIn.SetSourceInflection(curInfl);
            // Get uninflected form for all spelling variants
            Vector<InflectionRecord> bases = UninflectTerm(tempIn, conn, trie);
            // Update LexItem
            for(int j = 0; j < bases.size(); j++)
            {
                InflectionRecord rec = bases.elementAt(j);
                String uninflectedTerm = rec.GetUninflectedTerm();
                // Reset the source for in: the first is used in detailsInfo
                in.SetSourceCategory(rec.GetCategory());
                in.SetSourceInflection(rec.GetInflection());
                // details & mutate
                String details = null;
                String mutate = null;
                if(detailsFlag == true)
                {
                    details = INFO;
                }
                if(mutateFlag == true)
                {
                    // Get EUI with specified category
                    String eui = DbEui.GetEuisByUnflectedTermCat(
                        uninflectedTerm, rec.GetCategory(), conn);
                    mutate = eui + GlobalBehavior.GetFieldSeparator();
                }
                LexItem temp = UpdateLexItem(in, uninflectedTerm,
                    Flow.BASE_SPELLING_VARIANTS, rec.GetCategory(),
                    Inflection.GetBitValue(Inflection.BASE_BIT), 
                    details, mutate);
                // Remove existed term by adding cur to out if cur is a new term
                AddCurToOut(out, temp);
            }
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
        String testStr = GetTestStr(args, "color");    // input String
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr, Category.ALL_BIT_VALUE, 
            Inflection.ALL_BIT_VALUE);
        Vector<LexItem> outs = new Vector<LexItem>();
        
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            boolean isInflection = true;
            RamTrie trie = new RamTrie(isInflection, minTermLen, lvgDir, 0);
            if(conn != null)
            {
                outs = ToBaseSpellingVariants.Mutate(in, conn, trie, true, 
                    true);
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
    private static Vector<InflectionRecord> UninflectTerm(LexItem in, 
        Connection conn, RamTrie trie)
    {
        String inStr = in.GetSourceTerm();
        Vector<InflectionRecord> out = new Vector<InflectionRecord>();
        try
        {
            // fact: get uninflections from database
            out = DbUninflection.GetUninflections(inStr, conn);
            // Rules should not used since all spelling variants are known
        }
        catch (Exception e) { }
        return out;
    }
    // Add the cur LexItem into out if it (term) is not exist
    private static void AddCurToOut(Vector<LexItem> out, LexItem cur)
    {
        if(out.size() == 0)
        {
            out.addElement(cur);
        }
        else
        {
            boolean existTerm = false;
            for(int i = 0; i < out.size(); i++)
            {
                LexItem temp = out.elementAt(i);
                if((temp.GetTargetTerm().equals(cur.GetTargetTerm()))
                && (temp.GetTargetCategory().GetValue() == 
                    cur.GetTargetCategory().GetValue()))
                {
                    existTerm = true;
                    break;
                }
            }
            if(existTerm == false)
            {
                out.addElement(cur);
            }
        }
    }
    // data members
    private static final String INFO = "Base Spelling Variants";
}
