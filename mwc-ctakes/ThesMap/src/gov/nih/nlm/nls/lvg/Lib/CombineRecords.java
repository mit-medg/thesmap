package gov.nih.nlm.nls.lvg.Lib;
import java.io.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Flows.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides methods of combining Lvg output records by specifying 
* different rules.  It is utilized under Lvg Output filter options.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class CombineRecords
{
    // public constructor
    // public methods
    /**
    * Combine Lvg output records (LexItem) by specifing the combining rule 
    *
    * @param  ins  Lvg output records (LexItem) to be combined 
    * @param  rule  combining rule, by same output, EUI, category, or inflection
    *
    * @return   Vector<LexItem> - the input Vector
    *
    * @see LexItem
    */
    public static Vector<LexItem> Combine(Vector<LexItem> ins, int rule)
    {
        Vector<LexItem> out = new Vector<LexItem>();
        // return if no combine rule is used
        if(rule == BY_NONE)
        {
            out = new Vector<LexItem>(ins);
            return out;
        }
        // sort first
        Vector<LexItem> records = new Vector<LexItem>(ins);
        LexItemComparator<LexItem> lc = new LexItemComparator<LexItem>();
        lc.SetRule(rule);            // assign different comparator by rule
        Collections.sort(records, lc);
        // Combine records
        for(int i = 0; i < records.size(); i++)
        {
            if(i == 0)            // add the first element
            {
                LexItem cur = records.elementAt(i);
                out.addElement(cur);
            }
            else
            {
                CombineRecords(out, records, i, rule);
            }
        }
        return out;
    }
    // private methods
    // add element at index from ins to out by rule
    private static void CombineRecords(Vector<LexItem> out, Vector<LexItem> ins,
        int index, int rule) 
    {
        // do nothing if index is illegal
        if((index == 0) || (index >= ins.size()))
        {
            return;
        }
        // ins is sorted, thus, last is the only element need to be checked
        LexItem o1 = out.lastElement();
        LexItem o2 = ins.elementAt(index);
        // add the LexItem or combine them
        if(IsCombinable(o1, o2, rule) == false)
        {
            out.addElement(o2);
        }
        else
        {
            LexItem o3 = CombineByRule(o1, o2, rule);
            out.setElementAt(o3, out.size()-1);
        }
    }
    // combine o1 and o2 by a given rule
    private static LexItem CombineByRule(LexItem o1, LexItem o2, int rule) 
    {
        LexItem out = new LexItem(o1, true);
        long cat = 0;
        long infl = 0;
        switch(rule)
        {
            case BY_TERM:
                cat = Bit.Add(o1.GetTargetCategory().GetValue(), 
                    o2.GetTargetCategory().GetValue());
                infl = Bit.Add(o1.GetTargetInflection().GetValue(), 
                    o2.GetTargetInflection().GetValue());
                break;
            case BY_EUI:
                cat = Bit.Add(o1.GetTargetCategory().GetValue(), 
                    o2.GetTargetCategory().GetValue());
                infl = Bit.Add(o1.GetTargetInflection().GetValue(), 
                    o2.GetTargetInflection().GetValue());
                break;
            case BY_CATEGORY:
                cat = o1.GetTargetCategory().GetValue();
                infl = Bit.Add(o1.GetTargetInflection().GetValue(), 
                    o2.GetTargetInflection().GetValue());
                break;
            case BY_INFLECTION:
                cat = Bit.Add(o1.GetTargetCategory().GetValue(), 
                    o2.GetTargetCategory().GetValue());
                infl = o1.GetTargetInflection().GetValue();
                break;
        }
        // update taget category and inflection
        out.SetTargetCategory(cat);
        out.SetTargetInflection(infl);
        return out;
    }
    //check if o1 and o2 are combinable: same term and other fields
    private static boolean IsCombinable(LexItem o1, LexItem o2, int rule) 
    {
        boolean flag = true;
        
        // check original term and flow History
        // flow number is always the same since OutputFilter called in same loop
        flag = flag && (o1.GetOriginalTerm().equals(o2.GetOriginalTerm()))
            && (o1.GetFlowHistory().equals(o2.GetFlowHistory()));
        switch(rule)
        {
            case BY_TERM:
                flag = flag && (o1.GetTargetTerm().equals(o2.GetTargetTerm()));
                break;
            case BY_EUI:
                flag = flag && (o1.GetTargetTerm().equals(o2.GetTargetTerm()))
                    && (o1.GetMutateInformation().equals(
                        o2.GetMutateInformation()));
                break;
            case BY_CATEGORY:
                flag = flag && (o1.GetTargetTerm().equals(o2.GetTargetTerm()))
                    && (o1.GetTargetCategory().GetValue() ==
                        o2.GetTargetCategory().GetValue());
                break;
            case BY_INFLECTION:
                flag = flag && (o1.GetTargetTerm().equals(o2.GetTargetTerm()))
                    && (o1.GetTargetInflection().GetValue() ==
                        o2.GetTargetInflection().GetValue());
                break;
        }
        return flag;
    }
    // data members
    /** No combine rule specified */
    public final static int BY_NONE       = 0;  // no rule
    /** Combine record by output: input term, output term, flow history, and 
    flow number must be the same */
    public final static int BY_TERM       = 1;  // same in, flow num & hist
    /** Combine record by category: input term, output term, flow history and
    flow number must be the same */
    public final static int BY_CATEGORY   = 2;  // same in, out, flow num & hist
    /** Combine record by inflection: input term, output term, flow history,
    and flow number must be the same */
    public final static int BY_INFLECTION = 3;  // same in, out, flow num & hist
    /** Combine record by EUI: input term, output term, flow history, and 
    flow number must be the same */
    public final static int BY_EUI        = 4;  // same in, out, flow num & hist
}
