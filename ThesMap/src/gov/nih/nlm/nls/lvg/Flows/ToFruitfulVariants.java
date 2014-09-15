package gov.nih.nlm.nls.lvg.Flows;
import java.util.*;
import java.sql.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class generates all fruitful variants.  The variants are created by 
* generating inflectional variants, spelling variants, acronyms and
* abbreviations, expansion, derivational variants (recursively), synonyms 
* (recursively), and combinations of these
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/UDF/flow/fruitfulVariants.html">
* Design Document </a>
*
* @version    V-2013
****************************************************************************/
public class ToFruitfulVariants extends Transformation implements Cloneable
{
    // public methods
    /**
    * Performs the mutation of this flow component.
    *
    * @param   in   a LexItem as the input for this flow component
    * @param   conn   LVG database connection
    * @param   trieI  LVG Ram trie for inflections
    * @param   trieD  LVG Ram trie for derivations
    * @param   detailsFlag   a boolean flag for processing details information
    * @param   mutateFlag   a boolean flag for processing mutate information
    *
    * @return  Vector<LexItem> - the results from this flow component
    * of LexItems
    *
    * @see DbBase
    */
    public static Vector<LexItem> Mutate(LexItem in, Connection conn, 
        RamTrie trieI, RamTrie trieD, boolean detailsFlag, boolean mutateFlag)
        throws SQLException
    {
        // get no operation with legal category and inflection
        Vector<LexItem> originalSet = 
            GetNoOperationSet(in, conn, trieI, detailsFlag, false);
        // get the variant set
        // n, b, A/a, y, d, A/ad, yd, dy, A/ady, ydy, A/ay, yA/a
        Vector<LexItem> variantSet 
            = GetVariantSet(in, conn, trieI, trieD, detailsFlag);
        
        // get spelling and inflectional variants of above set
        Vector<LexItem> sivSet 
            = GetSpellingAndInflections(variantSet, conn, trieI, 
            detailsFlag, OutputFilter.LVG_OR_ALL);
        // add original & spelling inflectional set to fruitful variants
        originalSet = AddToVariantList(originalSet, sivSet);
        // filter out LexItems with shortest distance, update mutate information
        // and clean up history
        String flowName = Flow.GetBitName(Flow.FRUITFUL_VARIANTS, 1);
        Vector<LexItem> outs = GetFinalSet(originalSet, flowName, mutateFlag); 
        return outs;
    }
    /**
    * A unit test driver for this flow component.
    */
    public static void main(String[] args)
    {
        // load config file
        Configuration conf = new Configuration("data.config.lvg", true);
        String testStr = GetTestStr(args, "neurological");   // get input String
        int minTermLen = Integer.parseInt(
            conf.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf.GetConfiguration(Configuration.LVG_DIR);
        int minTrieStemLength = Integer.parseInt(
            conf.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
        // Mutate: connect to DB
        LexItem in = new LexItem(testStr);
        Vector<LexItem> outs = new Vector<LexItem>();
        try
        {
            Connection conn = DbBase.OpenConnection(conf);
            RamTrie trieI = new RamTrie(true, minTermLen, lvgDir, 0);
            RamTrie trieD = 
                new RamTrie(false, minTermLen, lvgDir, minTrieStemLength);
            if(conn != null)
            {
                outs = ToFruitfulVariants.Mutate(in, conn, trieI, trieD, true, 
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
    // package methods
    // Get spelling variants, and inflectional variants, then update history
    static Vector<LexItem> GetSpellingAndInflections(Vector<LexItem> ins, 
        Connection conn, RamTrie trieI, boolean detailsFlag, int restrictFlag) 
    {
        // List 7: spelling variants
        Vector<LexItem> list7 = GetSpellingVariants(ins, conn, detailsFlag);
        // add ins to list7
        list7 = AddToVariantList(list7, ins);
        // List 8: inflections
        Vector<LexItem> list8 = GetInflections(list7, conn, trieI, detailsFlag, 
            restrictFlag);
        // add list 8 into list 7
        list7 = AddToVariantList(list7, list8);
        return list7;
    }
    // filter out lexItems with same term, category, and longer distance
    // filter out LexItems with inflections greater than 256
    // update mutate information: cat|flow history|distance|tag
    // change flowHistory to flowName
    static Vector<LexItem> GetFinalSet(Vector<LexItem> ins, String flowName, 
        boolean mutateFlag) 
    {
        Hashtable<TermCatCatKey, LexItem> ht 
            = new Hashtable<TermCatCatKey, LexItem>();
        // filter out item with same term, category but longer distance
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem tempRec = ins.elementAt(i);
            TermCatCatKey tempKey = new TermCatCatKey(
                tempRec.GetTargetTerm().toLowerCase(),
                GetFirstCategory(tempRec),
                (int) tempRec.GetTargetCategory().GetValue());
            if(ht.containsKey(tempKey) == true)
            {
                LexItem existRec = ht.get(tempKey);
                int existDist = CalculateDistance(existRec.GetFlowHistory());
                int tempDist = CalculateDistance(tempRec.GetFlowHistory());
                if(existDist > tempDist)
                {
                    ht.remove(tempKey);
                    ht.put(tempKey, tempRec);
                }
            }
            else
            {
                ht.put(tempKey, tempRec);
            }
        }
        Vector<LexItem> uniqueList = new Vector<LexItem>(ht.values());
        // filter out all inflection greater than 256
        Vector<LexItem> legalList 
            = OutputFilter.GetEnhancedSimpleInflection(uniqueList);
        // clean up history and calculate distance
        Vector<LexItem> outs = UpdateVariants(legalList, flowName, mutateFlag);
        return outs;
    }
    // package methods
    static Vector<LexItem> GetVariantSet(LexItem in, Connection conn, 
        RamTrie trieI, RamTrie trieD, boolean detailsFlag) throws SQLException
    {
        // Get the base forms (b/n), and category information
        Vector<LexItem> bn = GetBaseSet(in, conn, trieI, detailsFlag);
        // Get spelling variants set of base
        Vector<LexItem> s = GetSpellingVariants(bn, conn, detailsFlag);
        // go through all different follow components
        // List 1: A/a, y
        Vector<LexItem> A = GetAcronyms(s, conn, detailsFlag);    // A & a
        Vector<LexItem> y = GetRecursiveSynonyms(s, conn, detailsFlag);
        Vector<LexItem> list1 = new Vector<LexItem>();
        list1.addAll(s);
        list1 = AddToVariantList(list1, A);
        list1 = AddToVariantList(list1, y);
        // List 2: d, A/ad, yd
        Vector<LexItem> list2 
            = GetRecursiveDerivations(list1, conn, trieD, detailsFlag);
        // List 3: dy, A/ady, ydy
        Vector<LexItem> list3 = GetRecursiveSynonyms(list2, conn, detailsFlag);
        // List 4: A/ay
        Vector<LexItem> list4 = GetRecursiveSynonyms(A, conn, detailsFlag);
        // List 5: yA/a
        Vector<LexItem> list5 = GetAcronyms(y, conn, detailsFlag);
        // List 6: list 1, 2, 3, 4, 5: 
        // n, b, A/a, y, d, A/ad, yd, dy, A/ady, ydy, A/ay, yA/a
        Vector<LexItem> list6 = new Vector<LexItem>();
        list6 = AddToVariantList(list6, list1);        // A/a, y
        list6 = AddToVariantList(list6, list2);        // d, A/ad, yd
        list6 = AddToVariantList(list6, list3);        // dy, A/ady, ydy
        list6 = AddToVariantList(list6, list4);        // A/ay
        list6 = AddToVariantList(list6, list5);        // yA/a
        return list6;
    }
    static Vector<LexItem> GetNoOperationSet(LexItem in, Connection conn, 
        RamTrie trieI, boolean detailsFlag, boolean lexiconOnly)
        throws SQLException
    {
        // Retrieve category and inflections 
        String term = in.GetSourceTerm();
        String details = null;
        if(detailsFlag == true)
        {
            details = "No Operation (Retrieve Cat/Infl)";
        }
        String mutate = null;
        // Facts:
        Vector<InflectionRecord> facts = new Vector<InflectionRecord>();
        facts = DbInflection.GetCatInfl(in.GetSourceTerm(), conn);
        Hashtable<CatInflKey, LexItem> ht 
            = new Hashtable<CatInflKey, LexItem>();        // unique hash table
        Vector<LexItem> catInfl = new Vector<LexItem>();
        for(int i = 0; i< facts.size(); i++)
        {
            InflectionRecord record = facts.elementAt(i);
            // check inflections: must be simplify or aux or modal
            if((record.GetInflection() > Inflection.LOWER_INFLECTIONS)
            && (record.GetCategory() != Category.ToValue("aux"))
            && (record.GetCategory() != Category.ToValue("modal")))
            {
                continue;
            }
            // assign the first category and inflection
            mutate = Long.toString(record.GetCategory()) 
                + GlobalBehavior.GetFieldSeparator() 
                + Long.toString(record.GetInflection());
 
            LexItem temp = UpdateLexItem(in, term, Flow.NO_OPERATION, 
                record.GetCategory(), record.GetInflection(), details, mutate);
            catInfl.addElement(temp);
            CatInflKey tempKey =
                new CatInflKey((int) temp.GetTargetCategory().GetValue(),
                temp.GetTargetInflection().GetValue());
            ht.put(tempKey, temp);
        }
        // Rules:
        if((lexiconOnly == false) && (catInfl.size() == 0))
        {
            Vector<CatInfl> rules = trieI.GetCatInflsByRules(term, 
                in.GetSourceCategory().GetValue(),
                in.GetSourceInflection().GetValue());
            for(int i = 0; i < rules.size(); i++)
            {
                CatInfl rec = rules.elementAt(i);
                // check inflections 
                if(rec.GetInflection() > Inflection.LOWER_INFLECTIONS)
                {
                    continue;
                }
                mutate = Long.toString(rec.GetCategory());
                LexItem temp = UpdateLexItem(in, term, Flow.NO_OPERATION,
                    rec.GetCategory(), rec.GetInflection(), details, 
                    mutate);
                catInfl.addElement(temp);
                CatInflKey tempKey =
                    new CatInflKey((int) temp.GetTargetCategory().GetValue(),
                    temp.GetTargetInflection().GetValue());
                ht.put(tempKey, temp);
            }
        }
        // convert hash table to a vector
        Vector<LexItem> outs = new Vector<LexItem>(ht.values());
        return outs;
    }
    // add to the variant list if the spelling is different
    // keep the one with shorter distance if the spelling are the same
    static Vector<LexItem> AddToVariantList(Vector<LexItem> orgList, 
        Vector<LexItem> newList)
    {
        Hashtable<TermCatCatKey, LexItem> ht 
            = new Hashtable<TermCatCatKey, LexItem>(
            orgList.size() + newList.size());
        // put all items of orgList into hash table
        for(int i = 0; i < orgList.size(); i++)
        {
            LexItem curOrg = orgList.elementAt(i);
            TermCatCatKey curOrgKey = new TermCatCatKey(curOrg.GetTargetTerm(), 
                GetFirstCategory(curOrg),
                (int)curOrg.GetTargetCategory().GetValue());
            ht.put(curOrgKey, curOrg);
        }
        // put all items of newList into hash table
        for(int i = 0; i < newList.size(); i++)
        {
            LexItem curNew = newList.elementAt(i);
            TermCatCatKey curNewKey = new TermCatCatKey(curNew.GetTargetTerm(), 
                GetFirstCategory(curNew),
                (int) curNew.GetTargetCategory().GetValue());
            if(ht.containsKey(curNewKey) == true)
            {
                LexItem existRec = ht.get(curNewKey);
                int existDist = CalculateDistance(existRec.GetFlowHistory());
                int curNewDist = CalculateDistance(curNew.GetFlowHistory());
    
                if(existDist > curNewDist)
                {
                    ht.remove(curNewKey);
                    ht.put(curNewKey, curNew);
                }
            }
            else
            {
                ht.put(curNewKey, curNew);
            }
        }
        Vector<LexItem> outs = new Vector<LexItem>(ht.values());
        return outs;
    }
    public static int GetFirstCategory(LexItem in)
    {
        int category = 0;
        if(in.GetMutateInformation() != null)
        {
            try
            {
                String delim = "|";
                StringTokenizer buf = 
                    new StringTokenizer(in.GetMutateInformation(), delim);
                category = Integer.parseInt(buf.nextToken());
            }
            catch (Exception e) { }
        }
        return category;
    }
    // private methods
    private static Vector<LexItem> GetBaseSet(LexItem in, Connection conn, 
        RamTrie trieI, boolean detailsFlag) throws SQLException
    {
        String inTerm = in.GetSourceTerm();
        Vector<LexItem> b = new Vector<LexItem>();
        
        // Get base form by using uninflected a term
        b = ToUninflectTerm.Mutate(in, conn, trieI, detailsFlag, false);
        // combine record with same term and category 
        Vector<LexItem> fb 
            = CombineRecords.Combine(b, CombineRecords.BY_CATEGORY);
        // change flow history to "n" if the term are the same as input
        // add category to mutate information
        String noOperationFlowName = Flow.GetBitName(Flow.NO_OPERATION, 1);
        for(int i = 0; i < fb.size(); i++)
        {
            LexItem temp = fb.elementAt(i);
            String curCategory = 
                Long.toString(temp.GetTargetCategory().GetValue());
            String curInflection = 
                Long.toString(temp.GetTargetInflection().GetValue());
            String mutate = curCategory + GlobalBehavior.GetFieldSeparator()
                + curInflection;
            temp.SetMutateInformation(mutate);
            if(inTerm.equals(temp.GetTargetTerm()) == true)
            {
                temp.SetFlowHistory(noOperationFlowName);
            }
        }
        return fb;
    }
    // calculate distance from the history
    private static int CalculateDistance(String history)
    {
        int dist = 0;
        for(int i = 0; i < history.length(); i++)
        {
            switch(history.charAt(i))
            {
                case 'i':
                case 'b':
                    dist += 1;
                    break;
                case 'a':
                case 'A':
                case 'y':
                    dist += 2;
                    break;
                case 'd':
                    dist += 3;
                    break;
                default:
                    break;
            }
        }
        return dist;
    }
    // update history & mutate info
    private static Vector<LexItem> UpdateVariants(Vector<LexItem> outList, 
        String flowName, boolean mutateFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // go through all outputs
        for(int i = 0; i < outList.size(); i++)
        {
            LexItem cur = outList.elementAt(i);
            // mutate information
            if(mutateFlag == true)
            {
                String fs = GlobalBehavior.GetFieldSeparator();
                String flowHis = cur.GetFlowHistory();
                String cat = cur.GetMutateInformation();
                StringBuffer buffer = new StringBuffer();
                buffer.append(cat);
                buffer.append(fs);
                buffer.append(flowHis);
                buffer.append(fs);
                buffer.append(CalculateDistance(flowHis));
                buffer.append(fs);
                // add tag information
                buffer.append(cur.GetTag());
                buffer.append(fs);
                String mutateInfo = buffer.toString();
                // assign new attribute properties
                cur.SetMutateInformation(mutateInfo);
            }
            // flow history: need to be done after setting mutate info
            cur.SetFlowHistory(flowName);
            outs.addElement(cur);
        }
        return outs;
    }
    private static Vector<LexItem> GetRecursiveDerivations(Vector<LexItem> ins,
        Connection conn, RamTrie trieD, boolean detailsFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // transfer from out of ins to in of ins
        Vector<LexItem> tempIns = LexItem.TargetsToSources(ins);
        // get derivations
        for(int i = 0; i < tempIns.size(); i++)
        {
            LexItem curIn = tempIns.elementAt(i);
            Vector<LexItem> derivations = 
                ToRecursiveDerivations.Mutate(curIn, conn, 
                trieD, OutputFilter.LVG_ONLY, detailsFlag, false, true);
            PassCategory(derivations, curIn.GetMutateInformation());
            outs.addAll(derivations);
        }
        return outs;
    }
    private static Vector<LexItem> GetAcronyms(Vector<LexItem> ins, 
        Connection conn, boolean detailsFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // transfer from out of ins to in of ins
        Vector<LexItem> tempIns = LexItem.TargetsToSources(ins);
        // get acronyms
        for(int i = 0; i < tempIns.size(); i++)
        {
            LexItem curIn = tempIns.elementAt(i);
            Vector<LexItem> acronyms = ToAcronyms.Mutate(curIn, conn, 
                detailsFlag, false);
            PassCategory(acronyms, curIn.GetMutateInformation());
            outs.addAll(acronyms);
            Vector<LexItem> expansions = ToExpansions.Mutate(curIn, conn, 
                detailsFlag, false);
            PassCategory(expansions, curIn.GetMutateInformation());
            outs.addAll(expansions);
        }
        return outs;
    }
    // This method is used for passing category
    private static void PassCategory(Vector<LexItem> ins, String mutateInfo)
    {
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem cur = ins.elementAt(i);
            cur.SetMutateInformation(mutateInfo);
        }
    }
    private static Vector<LexItem> GetRecursiveSynonyms(Vector<LexItem> ins, 
        Connection conn, boolean detailsFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // transfer from out of ins to in of ins
        Vector<LexItem> tempIns = LexItem.TargetsToSources(ins);
        // get recursive synonyms
        for(int i = 0; i < tempIns.size(); i++)
        {
            LexItem curIn = tempIns.elementAt(i);
            Vector<LexItem> rSynonyms = ToRecursiveSynonyms.Mutate(curIn, conn, 
                detailsFlag, false, true);
            PassCategory(rSynonyms, curIn.GetMutateInformation());
            outs.addAll(rSynonyms);
        }
        return outs;
    }
    private static Vector<LexItem> GetInflections(Vector<LexItem> ins, 
        Connection conn, RamTrie trieI, boolean detailsFlag, int restrictFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // transfer from out of ins to in of ins
        Vector<LexItem> tempIns = LexItem.TargetsToSources(ins);
        // get inlfections
        for(int i = 0; i < tempIns.size(); i++)
        {
            LexItem curIn = tempIns.elementAt(i);
            // TBD: toxica -> toxicum (singular, not base)
            CatInfl converted = CatInfl.ConvertToBase(new CatInfl(
                curIn.GetSourceCategory().GetValue(),
                curIn.GetSourceInflection().GetValue())); 
            curIn.SetSourceInflection(converted.GetInflection());
            // filter out all non-simple infelctions
            Vector<LexItem> infls = ToInflection.Mutate(curIn, conn, trieI,
                restrictFlag, detailsFlag, false);
            PassCategory(infls, curIn.GetMutateInformation());
            outs.addAll(infls);
        }
        return outs;
    }
    private static Vector<LexItem> GetSpellingVariants(Vector<LexItem> ins, 
        Connection conn, boolean detailsFlag)
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // transfer from out of ins to in of ins
        Vector<LexItem> tempIns = LexItem.TargetsToSources(ins);
        // get inlfections
        for(int i = 0; i < tempIns.size(); i++)
        {
            LexItem curIn = tempIns.elementAt(i);
            Vector<LexItem> sv = ToSpellingVariants.Mutate(curIn, conn, 
                detailsFlag, false);
            PassCategory(sv, curIn.GetMutateInformation());
            outs.addAll(sv);
        }
        // remove "+s" if it is not a real spellign variant
        for(int i = 0; i < outs.size(); i++)
        {
            LexItem temp = outs.elementAt(i);
            if(temp.GetSourceTerm().equalsIgnoreCase(temp.GetTargetTerm()))
            {
                String flowHistory = temp.GetFlowHistory();
                int endIndex = flowHistory.length() - 2;
                temp.SetFlowHistory(flowHistory.substring(0, endIndex));
            }
        }
        return outs;
    }
    // data members
    private static final String INFO = "Generate Fruitful Variants";
}
