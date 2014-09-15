package gov.nih.nlm.nls.lvg.Lib;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides the base object of Unicode Core Norm.  
* This object is need in the recursive Unicode Core Norm operation.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class CoreNormObj
{
    // public constructor
    /**
    * Create a default Core Norm object
    */
    public CoreNormObj()
    {
    }
    /**
    * Create a default Core Norm object, using a specified char
    */
    public CoreNormObj(char curChar)
    {
        curStr_ = UnicodeUtil.CharToStr(curChar);
        maxRecursiveNum_ = RECURSIVE_FACTOR*curStr_.length();
        recursiveNo_ = 0;
    }
    /**
    * Create a default Core Norm object, using a specified string
    */
    public CoreNormObj(String curStr)
    {
        curStr_ = curStr;
        maxRecursiveNum_ = RECURSIVE_FACTOR*curStr_.length();
        recursiveNo_ = 0;
    }
    /**
    * Set the current string
    * 
    * @param    curStr  the input current string 
    */
    public void SetCurStr(String curStr)
    {
        curStr_ = curStr;
    }
    /**
    * Update the current string by insert the new string into current
    * string at current position
    * 
    * @param    newStr  the new string to be inserted 
    */
    public void UpdateCurStr(String newStr)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(curStr_.substring(0, curPos_));
        buf.append(newStr);
        buf.append(curStr_.substring(curPos_+1));
        curStr_ = buf.toString();
    }
    /**
    * Set the current position
    * 
    * @param    curPos  the input current position 
    */
    public void SetCurPos(int curPos)
    {
        curPos_ = curPos;
    }
    /**
    * Update the current position by increase 1
    */
    public void UpdateCurPos()
    {
        curPos_++; 
    }
    
    /**
    * Update the current position by increase the specified amount
    * 
    * @param    increment  the input current position 
    */
    public void UpdateCurPos(int increment)
    {
        curPos_ += increment;
    }
    /**
    * Set the recursive no
    * 
    * @param    recursiveNo  the specified recursive no 
    */
    public void SetRecursiveNo(int recursiveNo)
    {
        recursiveNo_ = recursiveNo;
    }
    /**
    * Set the max limit of recursive number
    * 
    * @param    maxRecursiveNum  the specified max limit of recursive number 
    */
    public void SetMaxRecursiveNum(int maxRecursiveNum)
    {
        maxRecursiveNum_ = maxRecursiveNum;
    }
    /**
    * Update the recursive no by increase 1
    */
    public void UpdateRecursiveNo()
    {
        recursiveNo_++;
    }
    /**
    * Set the message of detail recursive operations
    * 
    * @param    normProcess  the specified operation 
    */
    public void SetDetails(int normProcess)
    {
        String plusStr = ((details_.length() > 0)?"+":new String());
        
        switch(normProcess)
        {
            case ASCII:
                details_ += plusStr + "AS";
                break;
            case SYMBOL_MAPPING:
                details_ += plusStr + "SM";
                break;
            case UNICODE_MAPPING:
                details_ += plusStr + "UM";
                break;
            case STRIP_DIACRITIC:
                details_ += plusStr + "SD";
                break;
            case SPLIT_LIGATURE:
                details_ += plusStr + "SL";
                break;
            case NO_MORE_OPERATION:
                details_ += plusStr + "NMO";
                break;
            case ERROR:
                details_ += plusStr + "ERR";
                break;
        }
    }
    /**
    * Get the cuurent string
    *
    * @return the current working string
    */
    public String GetCurStr()
    {
        return curStr_;
    }
    /**
    * Get the details of recursive operation
    *
    * @return the details of recursive operation
    */
    public String GetDetails()
    {
        return details_;
    }
    /**
    * Get current position
    *
    * @return the current position
    */
    public int GetCurPos()
    {
        return curPos_;
    }
    
    /**
    * Get recursive no
    *
    * @return the recursive no
    */
    public int GetRecursiveNo()
    {
        return recursiveNo_;
    }
    /**
    * Get max. limit of recursive number
    *
    * @return max. limit of recursive number
    */
    public int GetMaxRecursiveNum()
    {
        return maxRecursiveNum_;
    }
    /**
    * Check if the operation is within the max. limit of recursive number
    *
    * @return true if within the max. limit of recursive number
    */
    public boolean IsWithinRecursiveLimit()
    {
        return (recursiveNo_ < maxRecursiveNum_);
    }
    
    // data members
    // 20 recursive for each character 
    // Max. (20) happens in arabic ligature U+FDFA|65018
    public final static int RECURSIVE_FACTOR = 25;
    public final static int ASCII = 0;        // AS
    public final static int SYMBOL_MAPPING = 1;    // SM
    public final static int UNICODE_MAPPING = 2;    // UM
    public final static int STRIP_DIACRITIC = 3;    // SD
    public final static int SPLIT_LIGATURE = 4;        // SL
    public final static int NO_MORE_OPERATION = 5;        // NMO
    public final static int ERROR = 6;        // ERR: exceed the max recursive no
    private int maxRecursiveNum_ = 0;    // max number of recursive operation
    private int recursiveNo_ = 0;    // number of recursive operation
    private int curPos_ = 0;        // current working char position of string
    private String curStr_ = new String();    // the current String
    private String details_ = new String();    // the deatils processes
}
