package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides the base element of LVG, LexItem.  All Lvg operations, 
* such as flow component and filter options use LexItems as units of inputs 
* and outputs. 
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LexItem 
{
    // public constructor
    /**
    * Create a default LexItem object
    */
    public LexItem()
    {
    }
    /**
    * Create a default LexItem object, using a specified term
    */
    public LexItem(String term)    // construct a LexItem based on a given term
    {
        orgTerm_ = term;
        srcTerm_ = term;
    }
    /**
    * Create a default LexItem object, using a specified term and category
    */
    public LexItem(String term, long cat)
    {
        orgTerm_ = term;
        srcTerm_ = term;
        srcCategory_ = new Category(cat);
    }
    /**
    * Create a default LexItem object, using a specified term, category, and
    * inflection
    */
    public LexItem(String term, long cat, long infl)
    {
        orgTerm_ = term;
        srcTerm_ = term;
        srcCategory_ = new Category(cat);
        srcInflection_ = new Inflection(infl);
    }
    // construct a LexItem based on a given LexItem
    /**
    * Create a default LexItem object, using a specified LexItem and a flag of
    * inheriting properties from the specified LexItem.
    */
    public LexItem(LexItem in, boolean allFlag)    
    {
        orgTerm_ = in.GetOriginalTerm();
        srcTerm_ = in.GetSourceTerm();
        srcCategory_ = new Category(in.GetSourceCategory().GetValue());
        srcInflection_ = new Inflection(in.GetSourceInflection().GetValue());
        srcGender_ = new Gender(in.GetSourceGender().GetValue());
        detailInfo_ = in.GetDetailInformation();
        flowHistory_ = in.GetFlowHistory();
        flowNum_ = in.GetFlowNumber();
        flowComponentNum_ = in.GetFlowComponentNumber();
        tag_ = in.GetTag();
        if(allFlag == true)
        {
            tarTerm_ = in.GetTargetTerm();
            tarCategory_ = new Category(in.GetTargetCategory().GetValue());    
            tarInflection_ = 
                new Inflection(in.GetTargetInflection().GetValue());    
            tarGender_ = new Gender(in.GetTargetGender().GetValue());
            mutateInfo_ = in.GetMutateInformation();
        }
    }
    // public method
    /**
    * This override method checks the objects sequentiqlly if hascode are the 
    * same. It is used to remove duplicate LexItems in a set. Two LexItems
    * are considered as the same if the String format are the same:
    * org term|src term|src cat|src infl|tar term|tar cat|tar infl
    * Please note that differnet application might have differnt rules
    * for same LexItem.
    *
    */
    public boolean equals(Object anObject)
    {
        boolean flag = false;
        if((anObject != null) && (anObject instanceof LexItem))
        {
            if(this.ToString().equals(((LexItem)anObject).ToString()))
            {
                flag = true;
            }
        }
        return flag;
    }
    /**
    * This override method is used in hashTable to store data as key. It is
    * used to removed duplicate LexItems in a set. The hasdcode of String
    * format is used.
    *
    * @return  hash code of the detail string of LexItem
    */
    public int hashCode()
    {
        int hashCode = this.ToString().hashCode();
        return hashCode;
    }
    /**
    * This method returns a string of all data members of the current LexItem. 
    * The format is:
    * org term|src term|src cat|src infl|tar term|tar cat|tar infl
    *
    * @return  a string representation of current LexItem object
    */
    public String ToString()
    {
        String fs = GlobalBehavior.GetFieldSeparator();
        String outStr = orgTerm_ + fs + srcTerm_ + fs 
            + srcCategory_.GetName() + fs + srcInflection_.GetName() + fs
            + tarTerm_ + fs
            + tarCategory_.GetName() + fs + tarInflection_.GetName() + fs
            + mutateInfo_ + fs;
        return outStr;
    }
    /**
    * This method returns a detail string representation of all data members 
    * of the current LexItem. 
    *
    * @return  a detail string representation of current LexItem object
    */
    public String ToStringDetail()
    {
        String outStr = new String();
        outStr = "------------------------------------------------";
        outStr += GlobalBehavior.LS_STR;
        outStr += "Original Term: " + orgTerm_ + GlobalBehavior.LS_STR;
        outStr += "Source Term: " + srcTerm_ + GlobalBehavior.LS_STR;
        outStr += "Source Category: " + srcCategory_.GetValue() 
            + GlobalBehavior.LS_STR;
        outStr += "Source Inflection: " + srcInflection_.GetValue() 
            + GlobalBehavior.LS_STR;
        outStr += "Target Term: " + tarTerm_ + GlobalBehavior.LS_STR;
        outStr += "Target Category: " + tarCategory_.GetValue() 
            + GlobalBehavior.LS_STR;
        outStr += "Target Inflection: " + tarInflection_.GetValue() 
            + GlobalBehavior.LS_STR;
        outStr += "Flow History: " + flowHistory_ + GlobalBehavior.LS_STR;
        outStr += "Detail Information: " + detailInfo_ + GlobalBehavior.LS_STR;
        outStr += "Mutate Information: " + mutateInfo_ + GlobalBehavior.LS_STR;
        outStr += "Flow Number: " + flowNum_ + GlobalBehavior.LS_STR;
        return outStr;
    }
    /**
    * This method change a target LexItem (output from a flow component) to
    * a source LexItem (input for a flow component).  Such transfer methods 
    * must be used between a series of flow components operations in a flow.
    *
    * @param  tar  an LexItem that is the output of a flow component and 
    *         to be changed as an input for next flow component.
    *
    * @return  an LexItem for an input of a flow component
    */
    public static LexItem TargetToSource(LexItem tar)
    {
        LexItem src = new LexItem();
        src.SetOriginalTerm(tar.GetOriginalTerm());
        src.SetSourceTerm(tar.GetTargetTerm());
        src.SetSourceCategory(tar.GetTargetCategory().GetValue());
        src.SetSourceInflection(tar.GetTargetInflection().GetValue());
        src.SetDetailInformation(tar.GetDetailInformation());
        src.SetMutateInformation(tar.GetMutateInformation());
        src.SetFlowHistory(tar.GetFlowHistory());
        src.SetFlowNumber(tar.GetFlowNumber());
        src.SetFlowComponentNumber(tar.GetFlowComponentNumber()+1);
        src.SetTag(tar.GetTag());
        return src;
    }
    /**
    * This method change Vector<lexiTem> of target (outputs from a flow 
    * component) to Vector<LexItem> of source (inputs for a flow component).  
    * Such transfer methods must be used between a series of flow components 
    * operations in a flow.
    *
    * @param  tars  Vector<LexItem> of the outputs of a flow component 
    *         and to be changed as the inputs for next flow component.
    *
    * @return  Vector<LexItem> for the inputs of a flow component
    */
    public static Vector<LexItem> TargetsToSources(Vector<LexItem> tars)
    {
        if(tars == null)
        {
            return null;
        }
        Vector<LexItem> outs = new Vector<LexItem>();
        for(int i = 0; i < tars.size(); i++)
        {
            LexItem temp = tars.elementAt(i);
            LexItem tempIn = LexItem.TargetToSource(temp);
            outs.addElement(tempIn);
        }
        return outs;
    }
    /**
    * Set the original term of current LexItem.  The original term is never
    * changed during Lvg flow operations.
    *
    * @param  value  string value of the original term to be set.
    */
    public void SetOriginalTerm(String value)
    {
        orgTerm_ = value;
    }
    /**
    * Set the source term of current LexItem.  The source term is the term 
    * that Lvg flow component uses as input.
    *
    * @param  value  string value of the source term to be set.
    */
    public void SetSourceTerm(String value)
    {
        srcTerm_ = value;
    }
    /**
    * Set the target term of current LexItem.  The target term is the term
    * that come from the output of Lvg flow component.
    *
    * @param  value  string value of the target term to be set.
    */
    public void SetTargetTerm(String value)
    {
        tarTerm_ = value;
    }
    /**
    * Set the source categeroy of current LexItem.  The source category is 
    * the category of the source term of the LexItem.
    *
    * @param  value  a long integer value of the source category
    */
    public void SetSourceCategory(long value)
    {
        srcCategory_.SetValue(value);
    }
    /**
    * Set the target category of current LexItem.  The target category is 
    * the category of the target term of the LexItem.
    *
    * @param  value  a long integer value of the target category
    */
    public void SetTargetCategory(long value)
    {
        tarCategory_.SetValue(value);
    }
    /**
    * Set the source inflection of current LexItem.  The source inflection is 
    * the inflection of the source term of the LexItem.
    *
    * @param  value  a long integer value of the source inflection
    */
    public void SetSourceInflection(long value)
    {
        srcInflection_.SetValue(value);
    }
    /**
    * Set the target inflection of current LexItem.  The target inflection is 
    * the inflection of the target term of the LexItem.
    *
    * @param  value  a long integer value of the target inflection
    */
    public void SetTargetInflection(long value)
    {
        tarInflection_.SetValue(value);
    }
    /**
    * Set the detail information of current LexItem.  The detail information 
    * contains all information of each flow component in a flow operation.
    * This information can be used with option flag -d.
    *
    * @param  value  string value of the detail information to be set.
    */
    public void SetDetailInformation(String value)
    {
        detailInfo_ = value;
    }
    /**
    * Set the mutate information of current LexItem.  The mutate information 
    * provides addition information for user to understand the flow operation.
    * This information can be used with option flag -m.
    *
    * @param  value  string value of the mutate information to be set.
    */
    public void SetMutateInformation(String value)
    {
        mutateInfo_ = value;
    }
    /**
    * Set the tag information of current LexItem.  The tag information 
    * provides addition information by mark a tag on the lexItem
    *
    * @param  value  integer value of the tag information to be set.
    */
    public void SetTag(long value)
    {
        tag_ = value;
    }
    /**
    * Set the flow history of current LexItem.  The flow history utilizes the
    * combination of flow symbols (abbreviation names).
    *
    * @param  value  string value of the flow history to be set.
    */
    public void SetFlowHistory(String value)
    {
        flowHistory_ = value;
    }
    /**
    * Set the flow number in pararell flows
    *
    * @param  flowNum  an integer value of flow number
    */
    public void SetFlowNumber(int flowNum)
    {
        flowNum_ = flowNum;
    }
    /**
    * Set the flow component number in a flow.  This is used in detail
    * information.
    *
    * @param  flowComponentNum  an integer value of flow ocmponent number
    */
    public void SetFlowComponentNumber(int flowComponentNum)
    {
        flowComponentNum_ = flowComponentNum;
    }
    /**
    * Get the original term of current LexItem.
    *
    * @return  string value of the original term from the current LexItem.
    */
    public String GetOriginalTerm()
    {
        return orgTerm_;
    }
    /**
    * Get the source term of current LexItem.
    *
    * @return  string value of the source term from the current LexItem.
    */
    public String GetSourceTerm()
    {
        return srcTerm_;
    }
    /**
    * Get the source gender of current LexItem.
    *
    * @return  string value of the source gender from the current LexItem.
    */
    public Gender GetSourceGender()
    {
        return srcGender_;
    }
    /**
    * Get the target term of current LexItem.
    *
    * @return  string value of the target term from the current LexItem.
    */
    public String GetTargetTerm()
    {
        return tarTerm_;
    }
    /**
    * Get the source category of current LexItem.
    *
    * @return  the source category object from the current LexItem.
    */
    public Category GetSourceCategory()
    {
        return srcCategory_;
    }
    /**
    * Get the target category of current LexItem.
    *
    * @return  the target category object from the current LexItem.
    */
    public Category GetTargetCategory()
    {
        return tarCategory_;
    }
    /**
    * Get the source inflection of current LexItem.
    *
    * @return  the source inflection object from the current LexItem.
    */
    public Inflection GetSourceInflection()
    {
        return srcInflection_;
    }
    /**
    * Get the target inflection of current LexItem.
    *
    * @return  the target inflection object from the current LexItem.
    */
    public Inflection GetTargetInflection()
    {
        return tarInflection_;
    }
    /**
    * Get the target gender of current LexItem.
    *
    * @return  the target gender object from the current LexItem.
    */
    public Gender GetTargetGender()
    {
        return tarGender_;
    }
    /**
    * Get the flow history of current LexItem.
    *
    * @return  string value of the flow history from the current LexItem.
    */
    public String GetFlowHistory()
    {
        return flowHistory_;
    }
    /**
    * Get the detail information of current LexItem.
    *
    * @return  string value of the detail information from the current LexItem.
    */
    public String GetDetailInformation()
    {
        return detailInfo_;
    }
    /**
    * Get the tag information of current LexItem.
    *
    * @return  integer value of the tag information from the current LexItem.
    */
    public long GetTag()
    {
        return tag_;
    }
    /**
    * Get the mutate information of current LexItem.
    *
    * @return  string value of the mutate information from the current LexItem.
    */
    public String GetMutateInformation()
    {
        return mutateInfo_;
    }
    /**
    * Get the flow number of current LexItem.
    *
    * @return  flow number of the current LexItem in this flow
    */
    public int GetFlowNumber()
    {
        return flowNum_;
    }
    /**
    * Get the flow component number of current LexItem.
    *
    * @return  flow component number of the current LexItem in this flow
    */
    public int GetFlowComponentNumber()
    {
        return flowComponentNum_;
    }
    /**
    * Get the string representation of the transformation results.
    *
    * @param   in   an output LexItem of a transformation
    * @param   originalTerm   the original input line (term)
    * @param   mutateFlag   a boolean flag is used to append or not the mutate 
    * information if the value is true or false.
    * @param   caseFlag    a int flag of modifying case on the output
    * @param   showCategoryStrFlag  a boolean flag of showing categories 
    * in name
    * @param   showInflectionStrFlag  a boolean flag of showing inflections 
    * in name
    * @param   filterInputFlag   a boolean flag of filtering out addition
    * fields from the input line.
    * @param   separator   field separator in String
    *
    * @return  the string representation of the transformation results (LexItem)
    * The format is:
    * <br>Input|Output|Categories|Inflections|Flow History|Flow Number|Mutate
    * Information|
    */
    public static String GetResultString(LexItem in, String originalTerm,
        boolean mutateFlag, int caseFlag, boolean showCategoryStrFlag,
        boolean showInflectionStrFlag, boolean filterInputFlag,
        String separator) 
    {
        // original source term
        String sourceTerm = originalTerm;    // all inputs line
        if(filterInputFlag == true)
        {
            sourceTerm = in.GetOriginalTerm();    // only the input term used
        }
        // target term
        String targetTerm = in.GetTargetTerm();
        switch(caseFlag)
        {
            case OutputFilter.LOWERCASE:
                targetTerm = targetTerm.toLowerCase();
                break;
            case OutputFilter.UPPERCASE:
                targetTerm = targetTerm.toUpperCase();
                break;
        }
        // target category
        String catStr = Long.toString(in.GetTargetCategory().GetValue());
        if(showCategoryStrFlag == true)
        {
            catStr = OutputFilter.HEAD + in.GetTargetCategory().GetName() 
                + OutputFilter.TAIL;
        }
        // target inflection
        String inflStr = Long.toString(in.GetTargetInflection().GetValue());
        if(showInflectionStrFlag == true)
        {
            inflStr = OutputFilter.HEAD + in.GetTargetInflection().GetName() 
                + OutputFilter.TAIL;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(sourceTerm);
        buffer.append(separator);
        buffer.append(targetTerm);
        buffer.append(separator);
        buffer.append(catStr);
        buffer.append(separator);
        buffer.append(inflStr);
        buffer.append(separator);
        buffer.append(in.GetFlowHistory());
        buffer.append(separator);
        buffer.append(in.GetFlowNumber());
        buffer.append(separator);
        // add extra information for mutation
        if(mutateFlag == true)
        {
            buffer.append(in.GetMutateInformation());
        }
        String outStr = buffer.toString();
        return outStr;
    }
    /**
    * A test driver for this class
    */
    public static void main(String[] args)
    {
        LexItem a = new LexItem("Test");
        LexItem.PrintLexItem(a);
        LexItem b = LexItem.TargetToSource(a);
        LexItem.PrintLexItem(b);
        Vector<LexItem> ab = new Vector<LexItem>();
        ab.addElement(a);
        ab.addElement(b);
        Vector<LexItem> abs = LexItem.TargetsToSources(ab);
        for(int i = 0; i < abs.size(); i++)
        {
            LexItem l = abs.elementAt(i);
            LexItem.PrintLexItem(l);
        }
        try
        {
            LexItem c = (LexItem) a.clone();
        }
        catch (Exception e) {}
    }
    // private method
    static private void PrintLexItem(LexItem in)
    {
        System.out.println("------------------------------------------------");
        System.out.println("Original Term: " + in.GetOriginalTerm());
        System.out.println("Source Term: " + in.GetSourceTerm());
        System.out.println("Source Category: " + 
            in.GetSourceCategory().GetValue());
        System.out.println("Source Inflection: " + 
            in.GetSourceInflection().GetValue());
        System.out.println("Target Term: " + in.GetTargetTerm());
        System.out.println("Target Category: " + 
            in.GetTargetCategory().GetValue());
        System.out.println("Target Inflection: " + 
            in.GetTargetInflection().GetValue());
        System.out.println("Flow History: " + in.GetFlowHistory());
        System.out.println("Detail Information: " + in.GetDetailInformation());
        System.out.println("Mutate Information: " + in.GetMutateInformation());
        System.out.println("Flow Number: " + in.GetFlowNumber());
    }
    // data members
    private String mutateInfo_ = null;    // mutate info for a flow component 
    private String detailInfo_ = null;    // accumulated detail information 
    private int flowNum_ = 1;                // flow number in pararell flows
    private int flowComponentNum_ = 1;    // flow component number in a flow
    private String flowHistory_ = null;   // accumulated flow history
    private long tag_ = Tag.ALL_BIT_VALUE; // tag information
    private String orgTerm_ = null;       // original term, never changed
    private String srcTerm_ = null;       // source term 
    private String tarTerm_ = null;       // target term, result
    // source Category
    private Category srcCategory_ = new Category(Category.ALL_BIT_VALUE);
    private Category tarCategory_ = new Category();        // target Category
    private Inflection srcInflection_ = 
        new Inflection(Inflection.ALL_BIT_VALUE);  // source inflection
    private Inflection tarInflection_ = new Inflection();  // target inflection
    private Gender srcGender_ = new Gender();
    private Gender tarGender_ = new Gender();
}
