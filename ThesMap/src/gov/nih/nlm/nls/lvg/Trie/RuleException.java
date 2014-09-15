package gov.nih.nlm.nls.lvg.Trie;
import java.util.*;
/*****************************************************************************
* This class creates an object of exception to a rule.  The string format of
* an exception is key|excpetion;
*
* <p><b>History:</b>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class RuleException
{
    // public constructors
    /**
    * Create an object of rule exception, using a string to define the 
    * exception.  The string format of the exception is:
    * <br> key|value;
    * <br> where:
    * <br> key: the term for matching
    * <br> value: the term of exception
    */
    public RuleException(String exceptionStr)
    {
        exceptionStr_ = exceptionStr.trim();    // remove extra space
        DecomposeExceptionStr();
    }
    // public methods
    /**
    * Reverse the exception string.  All exceptions in LVG are bi-directional. 
    */
    public void Reverse()        // reverse in and out for bi-direction rule
    {
        String tempKey = key_;
        key_ = value_;
        value_ = tempKey;
        exceptionStr_ = key_ + "|" + value_ + ";";
    }
    /**
    * Check if the current exception is the same as a specific exception.
    * The checking is based on comparing the string format of exceptions.
    *
    * @param exception rule exception
    *
    * @return  true or false to represent the current exception is or is not 
    * the same as a specified exception
    */
    public boolean equals(RuleException exception)
    {
        return exceptionStr_.equals(exception.GetExceptionStr());
    }
    /**
    * Get the key (matching string pattern) of current exception.
    *
    * @return  key (matching string pattern) of current exception
    */
    public String GetKey()
    {
        return key_;
    }
    /**
    * Get the key (matching string pattern) of current exception.
    *
    * @return  key (matching string pattern) of current exception
    */
    public String GetValue()
    {
        return value_;
    }
    /**
    * Get the exception string of current exception.
    *
    * @return  exception string of current exception
    */
    public String GetExceptionStr()
    {
        return exceptionStr_;
    }
    /**
    * Check if the exception string is legal.
    *
    * @return  true or false if the exception string is or is not legal
    */
    public boolean IsLegalFormat()
    {
        return legalFormat_;
    }
    /**
    * A test driver of this class.
    */
    public static void main(String[] args)
    {
        String exStr = "  key|value";
        RuleException ex = new RuleException(exStr);
        ex.PrintException();
        ex.Reverse();
        ex.PrintException();
        exStr = "|value;";
        ex = new RuleException(exStr);
        ex.PrintException();
        ex.Reverse();
        ex.PrintException();
        exStr = "key|;";
        ex = new RuleException(exStr);
        ex.PrintException();
        ex.Reverse();
        ex.PrintException();
        exStr = "|;";
        ex = new RuleException(exStr);
        ex.PrintException();
        ex.Reverse();
        ex.PrintException();
    }
    // private methods
    private void DecomposeExceptionStr()
    {
        //check if the exceptionStr_ is a legal format "key|value;"
        if((exceptionStr_ == null)
        || (exceptionStr_.indexOf(WildCard.FS) == -1)      // must have '|'
        || (exceptionStr_.endsWith(";") == false)    // must end with ';'
        || (exceptionStr_.indexOf(' ') != -1))       // must not have ' '
        {
            System.err.println("** Error: Wrong format in RuleException("
                + exceptionStr_ + ")");
            legalFormat_ = false;
            return;
        }
        StringTokenizer buf = new StringTokenizer(exceptionStr_, "|;");
        if(exceptionStr_.equals("|;"))
        {
            key_ = "";
            value_ = "";
        }
        else if(exceptionStr_.startsWith("|") == true)
        {
            key_ = "";
            value_ = buf.nextToken();
        }
        else if(exceptionStr_.endsWith("|;") == true)
        {
            key_ = buf.nextToken();
            value_ = "";
        }
        else
        {
            key_ = buf.nextToken();
            value_ = buf.nextToken();
        }
    }
    private void PrintException()
    {
        System.out.println("------ Rule Exception ------");
        System.out.println("exceptionStr_: " + exceptionStr_);
        System.out.println("key_: " + key_);
        System.out.println("value_: " + value_);
    }
    
    // data member
    private String exceptionStr_ = null;    // the original string of exception
    private String key_ = null;                // front part
    private String value_ = null;            // rear part
    private boolean legalFormat_ = true;    // flag for checking format
}
