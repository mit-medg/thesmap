package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
/*****************************************************************************
* This class provides a class for all Lvg options related variables.  This 
* class is used in the OutputFilter ExecuteOutputFilter method.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class LvgOutputOption 
{
    // public constructor
    /**
    * Create a default LexItem object
    */
    public LvgOutputOption()
    {
    }
    // public methods
    public void SetMarkEndFlag(boolean value)
    {
        markEndFlag_ = value;
    }
    public void SetNoOutputFlag(boolean value)
    {
        noOutputFlag_ = value;
    }
    public void SetShowCategoryStrFlag(boolean value)
    {
        showCategoryStrFlag_ = value;
    }
    public void SetShowInflectionStrFlag(boolean value)
    {
        showInflectionStrFlag_ = value;
    }
    public void SetFilterInputFlag(boolean value)
    {
        filterInputFlag_ = value;
    }
    public void SetSortFlag(int value)
    {
        sortFlag_ = value;
    }
    public void SetCaseFlag(int value)
    {
        caseFlag_ = value;
    }
    public void SetCombineRule(int value)
    {
        combineRule_ = value;
    }
    public void SetOutRecordNum(int value)
    {
        outRecordNum_ = value;
    }
    public void SetOutCategory(long value)
    {
        outCategory_ = value;
    }
    public void SetOutInflection(long value)
    {
        outInflection_ = value;
    }
    public void SetExcludeCategory(long value)
    {
        exCategory_ = value;
    }
    public void SetExcludeInflection(long value)
    {
        exInflection_ = value;
    }
    public void SetNoOutputStr(String value)
    {
        noOutputStr_ = value;
    }
    public void SetMarkEndStr(String value)
    {
        markEndStr_ = value;
    }
    public void SetOutputFieldList(Vector<Integer> value)
    {
        outputFieldList_ = value;
    }
    public boolean GetMarkEndFlag()
    {
        return markEndFlag_;
    }
    public boolean GetNoOutputFlag()
    {
        return noOutputFlag_;
    }
    public boolean GetShowCategoryStrFlag()
    {
        return showCategoryStrFlag_;
    }
    public boolean GetShowInflectionStrFlag()
    {
        return showInflectionStrFlag_;
    }
    public boolean GetFilterInputFlag()
    {
        return filterInputFlag_;
    }
    public int GetSortFlag()
    {
        return sortFlag_;
    }
    public int GetCaseFlag()
    {
        return caseFlag_;
    }
    public int GetCombineRule()
    {
        return combineRule_;
    }
    public int GetOutRecordNum()
    {
        return outRecordNum_;
    }
    public long GetOutCategory()
    {
        return outCategory_;
    }
    public long GetOutInflection()
    {
        return outInflection_;
    }
    public long GetExcludeCategory()
    {
        return exCategory_;
    }
    public long GetExcludeInflection()
    {
        return exInflection_;
    }
    public String GetNoOutputStr()
    {
        return noOutputStr_;
    }
    public String GetMarkEndStr()
    {
        return markEndStr_;
    }
    public Vector<Integer> GetOutputFieldList()
    {
        return outputFieldList_;
    }
    // data members
    // Output filter
    private boolean markEndFlag_ = false;             // ccgi
    private boolean noOutputFlag_ = false;            // n
    private boolean showCategoryStrFlag_ = false;       // SC
    private boolean showInflectionStrFlag_ = false;     // SI
    private boolean filterInputFlag_ = false;         // ti
    private int caseFlag_ = OutputFilter.PRESERVED_CASE; // C:INT
    private int combineRule_ = CombineRecords.BY_NONE;     // CR:o,oc,oe,oi
    private int outRecordNum_ = -1;                         // R:INT
    private int sortFlag_ = LexItemComparator.NONE;        // St:o, oc, oci
    private long outCategory_ = Category.ALL_BIT_VALUE;     // DC:LONG
    private long outInflection_ = Inflection.ALL_BIT_VALUE; // DI:LONG
    private long exCategory_ = Category.NO_BIT_VALUE;       // EC:LONG
    private long exInflection_ = Inflection.NO_BIT_VALUE;   // EI:LONG
    private Vector<Integer> outputFieldList_ = new Vector<Integer>(); // F:INT
    // Configurations
    private String noOutputStr_ = null;               // n
    private String markEndStr_ = null;                // ccgi
}
