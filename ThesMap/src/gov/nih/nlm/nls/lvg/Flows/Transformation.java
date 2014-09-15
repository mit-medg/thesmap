package gov.nih.nlm.nls.lvg.Flows;
import java.util.Vector;
import gov.nih.nlm.nls.lvg.Lib.*;
/*****************************************************************************
* This class provides the base class for lvg flow components.  Classes of all
* lvg flow components are extended from this Transformation class.  It
* contains some common public methods, such as UpdateFlowHistory( ),
* GetResults( ), and Mutate( ).
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see  <a href="../../../../../../../designDoc/UDF/flow/design.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public abstract class Transformation
{
    // public methods
    /**
    * Get the string to be tested (transformed).
    *
    * @param   args   an input string from the command line
    * @param   defaultStr   a program default string
    *
    * @return  the string to be tested (transformed)
    */
    public static String GetTestStr(String[] args, String defaultStr)
    {
        // set the test string to default string first
        String testStr = defaultStr;
        StringBuffer buffer = new StringBuffer();
        // set the test string to the input string, if input string size > 0
        if(args.length > 0)
        {
            buffer.append(args[0]);
            for(int i = 1; i < args.length; i++)
            {
                buffer.append(" ");
                buffer.append(args[i]);
            }
            testStr = buffer.toString();
        }
        return testStr;
    }
    /**
    * Print the string representation of the transformation results.
    *
    * @param   out   an output LexItem of a transformation
    * @param   flowNum   flow number, a number indicates which flow produced 
    * the output
    * @param   mutateFlag   a boolean flag is used to append or not the mutate 
    * information if the value is true or false.
    */
    protected static void PrintResult(LexItem out, int flowNum, 
        boolean mutateFlag)
    {
        System.out.println(LexItem.GetResultString(out, "", mutateFlag, 
            OutputFilter.PRESERVED_CASE, false, false, false, "|"));
    }
    // protected methods
    /**
    * Update data for a LexItem.  This method must be called after the 
    * transformation of a Lvg flow component.
    *
    * @param   in   the LexItem to be updated
    * @param   target   target term for updating
    * @param   flowName   the abbreviation name of the flow component of the 
    * tranformation
    * @param   category   category to be assigned.  if the value is UPDATE, the 
    * category will assigned to all bits or source if the source is or is not 0
    * @param   inflection   inflection to be assigned.  if the value is UPDATE,
    * the category will assigned to all bits or source if the source is or is 
    * not 0
    * @param   details   the detail transform information for all flow 
    * components in a flow
    * @param   mutateInfo   the addition mutate information for a flow 
    * component.  The infomation on the last flow component will be used if 
    * there are more than one flow component in the flow.
    * @param   appendFlowFlag   a flag for appending flow history 
    *
    * @return  an LexItem with updated data.
    */
    protected static LexItem UpdateLexItem(LexItem in, String target, 
        String flowName, long category, long inflection, String details, 
        String mutateInfo, boolean appendFlowFlag)
    {
        // instantiate a LexItem
        LexItem out = new LexItem(in, false);
        // update output term
        out.SetTargetTerm(target);
        // update Category to all bits or source when source is or is not 0 
        if(category == UPDATE)
        {
            if(out.GetSourceCategory().GetValue() == 0)
            {
                out.SetTargetCategory(Category.ALL_BIT_VALUE);
            }
            else
            {
                out.SetTargetCategory(out.GetSourceCategory().GetValue());
            }
        }
        else    // assigned category to the specified category
        {
            out.SetTargetCategory(category);
        }
    
        // update inflection to all bits or source when source is or is not 0 
        if(inflection == UPDATE)
        {
            if(out.GetSourceInflection().GetValue() == 0)
            {
                out.SetTargetInflection(Inflection.ALL_BIT_VALUE);
            }
            else
            {
                out.SetTargetInflection(out.GetSourceInflection().GetValue());
            }
        }
        else
        {
            out.SetTargetInflection(inflection);
        }
        // update Detail Information: accumuated
        if(details != null)
        {
            String detailInformation = out.GetDetailInformation();
            StringBuffer buffer = new StringBuffer();
            if(detailInformation == null)
            {
                detailInformation = "---------------------------------------"
                    + GlobalBehavior.LS_STR;
            }
            int index = flowName.lastIndexOf("+") + 1;
            String curFlowName = flowName.substring(index);
            buffer.append(detailInformation);
            buffer.append(out.GetFlowComponentNumber());
            buffer.append(". (");
            buffer.append(curFlowName);
            buffer.append("): ");
            buffer.append(out.GetSourceTerm());
            buffer.append(" (");
            buffer.append(out.GetSourceCategory().GetValue());
            buffer.append(", ");
            buffer.append(out.GetSourceInflection().GetValue());
            buffer.append(") --> ");
            buffer.append(out.GetTargetTerm());
            buffer.append(" (");
            buffer.append(out.GetTargetCategory().GetValue());
            buffer.append(", ");
            buffer.append(out.GetTargetInflection().GetValue());
            buffer.append("): ");
            buffer.append(details);
            buffer.append(GlobalBehavior.LS_STR);
            out.SetDetailInformation(buffer.toString());
        }
        // update flow mutate information
        if(mutateInfo != null)
        {
            out.SetMutateInformation(mutateInfo);
        }
        // update flow history: accumulated
        String flowHistory = out.GetFlowHistory();
        if(appendFlowFlag == false)
        {
            flowHistory = flowName;
        }
        else    // append the flow 
        {
            if(flowHistory == null)
            {
                flowHistory = flowName;
            }
            else 
            {
                // add "+" between flow symbols
                StringBuffer buffer = new StringBuffer();
                buffer.append(flowHistory);
                buffer.append("+");
                buffer.append(flowName);
                flowHistory = buffer.toString();
            }
        }
        out.SetFlowHistory(flowHistory); // put flow history into out LexItem
        return out;
    }
    /**
    * Update data for a LexItem.  This method must be called after the 
    * transformation of a Lvg flow component.
    *
    * @param   in   the LexItem to be updated
    * @param   target   target term for updating
    * @param   flowBit   the bit number of the flow component of the 
    * tranformation
    * @param   category   category to be assigned.  if the value is UPDATE, the 
    * category will assigned to all bits or source if the source is or is not 0
    * @param   inflection   inflection to be assigned.  if the value is UPDATE,
    * the category will assigned to all bits or source if the source is or is 
    * not 0
    * @param   details   the detail transform information for all flow 
    * components in a flow
    * @param   mutateInfo   the addition mutate information for a flow 
    * component.  The infomation on the last flow component will be used if 
    * there are more than one flow component in the flow.
    *
    * @return  an LexItem with updated data.
    */
    protected static LexItem UpdateLexItem(LexItem in, String target, 
        int flowBit, long category, long inflection, String details, 
        String mutateInfo)
    {
        int index = 1;      // TBD: 1 for short name (option flag)
        String flowName = Flow.GetBitName(flowBit, index);
        LexItem out = UpdateLexItem(in, target, flowName, category, inflection,
            details, mutateInfo, true);
    
        return out;
    }
    /**
    * Update data for a LexItem.  This method sets category, inflection,
    * detail information, and mutate information for a given LexItem.
    *
    * @param   in   the LexItem to be updated
    * @param   flowBit   the bit number of the flow component 
    * @param   category   category to be assigned  
    * @param   inflection   inflection to be assigned
    * @param   details   the detail transform information for all flow 
    * components in a flow
    * @param   mutateInfo   the addition mutate information for a flow 
    * component.  The infomation on the last flow component will be used if 
    * there are more than one flow component in the flow.
    *
    * @return  an LexItem with updated data.
    */
    protected static LexItem UpdateLexItem(LexItem in,
        int flowBit, long category, long inflection, String details, 
        String mutateInfo)
    {
        // instantiate a new LexItem
        LexItem out = new LexItem(in, true);
        // update Category
        out.SetTargetCategory(category);
    
        // update inflection
        out.SetTargetInflection(inflection);
        int index = 1;      // 1 for short name (option flag)
        String flowName = Flow.GetBitName(flowBit, index);
        // update Detail Information: accumuated
        String detailInformation = "----------------------------------------"
            + GlobalBehavior.GetFieldSeparator();
        StringBuffer buffer = new StringBuffer();
        buffer.append(detailInformation);
        buffer.append(out.GetFlowComponentNumber());
        buffer.append(". (");
        buffer.append(flowName);
        buffer.append("): ");
        buffer.append(out.GetSourceTerm());
        buffer.append(" (");
        buffer.append(out.GetSourceCategory().GetValue());
        buffer.append(", ");
        buffer.append(out.GetSourceInflection().GetValue());
        buffer.append(") --> ");
        buffer.append(out.GetTargetTerm());
        buffer.append(" (");
        buffer.append(out.GetTargetCategory().GetValue());
        buffer.append(", ");
        buffer.append(out.GetTargetInflection().GetValue());
        buffer.append("): ");
        buffer.append(details);
        buffer.append(GlobalBehavior.GetFieldSeparator());
        out.SetDetailInformation(buffer.toString());
        // update flow mutate information
        out.SetMutateInformation(mutateInfo);
        // update flow history: accumulated
        String flowHistory = flowName;
        out.SetFlowHistory(flowHistory); // put flow history into out LexItem
        return out;
    }
    /**
    * Print the input and outputs for the current flow component.
    *
    * @param   in   input of current flow component
    * @param   outs   outputs of current flow component
    */
    protected static void PrintResults(LexItem in, Vector<LexItem> outs)
    {
        // print input
        System.out.println("-- in term: " + in.GetSourceTerm());
        // print outputs
        System.out.println("-- outs.size: " + outs.size());
        System.out.println("======================================");
        
        // go through all outputs
        String fs = GlobalBehavior.GetFieldSeparator();
        for(int i = 0; i < outs.size(); i++)
        {
            LexItem out = outs.elementAt(i);
            System.out.println(out.GetOriginalTerm() + fs 
                + out.GetTargetTerm() + fs 
                + out.GetTargetCategory().GetValue() + fs 
                + out.GetTargetInflection().GetValue() + fs
                + out.GetFlowHistory() + fs);
            System.out.println(out.GetDetailInformation());
        }
    }
    // private methods
    // data members
    /** a numberical flag used in UpdateLexItem( ) method to set the categories
    * or inflections to all bits or source bit if the source bit is or is not 0
    */
    public static final int UPDATE = -1;    // flag used for update Cat & Infl
    /** A global definition for no mutation information */
    public static final String NO_MUTATE_INFO = 
        "none" + GlobalBehavior.GetFieldSeparator();
}
