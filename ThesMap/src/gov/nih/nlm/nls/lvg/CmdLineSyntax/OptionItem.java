package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This class represents the basic elements of an option list.
* An OptionItem = option flag + option argument.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionItem
{
    // Public Constructors
    /**
    * Create an OptionItem object, using an option item in a String format
    */
    public OptionItem(String optionItem)
    {
        optionItem_ = optionItem;
        optionFlag_ = GetOptionFlag(optionItem);
        optionArgu_ = GetOptionArgument(optionItem);
    }
    /**
    * Create an OptionItem object, using an option item, option flag, and
    * option arguments in String format.
    * This constrcutor is currently not used.
    */
    public OptionItem(String optionItem, String optionFlag, String optionArgu)
    {
        optionItem_ = optionItem;
        optionFlag_ = optionFlag;
        optionArgu_ = optionArgu;
    }
    // public method
    /**
    * Get the option flag of an option item
    *
    * @param  optionItem  an option item in a string format
    *
    * @return  option flag of a given option item
    */
    public static String GetOptionFlag(String optionItem)
    {
        return GetPartialOption(optionItem, true);
    }
    /**
    * Get the option argument of an option item
    *
    * @param  optionItem  an option item in a string format
    *
    * @return  option argument of a given option item
    */
    public static String GetOptionArgument(String optionItem)
    {
        return GetPartialOption(optionItem, false);
    }
    /**
    * Get the highest separator of a given item in a string format
    *
    * @param  itemStr  option item in a string format
    * @param  distance  level offset, use 1 for this system
    *
    * @return  separator for a given level
    */
    public static String GetSeparator(String itemStr, int distance)
    {
        String levelSeparator = null;
        for(int i = 0; i < Option.MAX_LEVEL; i++)
        {
            int index = itemStr.indexOf(Option.SEPARATOR[i]);
            if(index == -1)
            {
                int sepIndex = i-1+distance;
                if((sepIndex < 0) || (sepIndex >= Option.MAX_LEVEL))
                {
                    levelSeparator = "-Error-";
                }
                else
                {
                    levelSeparator = "" + Option.SEPARATOR[sepIndex];
                }
                break;
            }
        }
        return levelSeparator;
    }
    /**
    * Get arguments from a given option
    *
    * @param  str  input option for retreiving arguments
    *
    * @return  a vector of option arguments
    */
    public static Vector<String> GetArgumentList(String str)
    {
        Vector<String> arguList = new Vector<String>(2);
        String delimiter = "" + Option.ARGUMENT_SEP;
        StringTokenizer buf = new StringTokenizer(str, delimiter);
        while(buf.hasMoreTokens())
        {
            arguList.addElement(buf.nextToken());
        }
        return arguList;
    }
    /**
    * Get the option item of current option item in string format
    *
    * @return  option item in string format
    */
    public String GetOptionItem()
    {
        return optionItem_;
    }
    /**
    * Get Option flag of current option item in string format
    *
    * @return  option flag in string format
    */
    public String GetOptionFlag()
    {
        return optionFlag_;
    }
    /**
    * Get Option argument of current option item in string format
    *
    * @return  option argument in string format
    */
    public String GetOptionArgument()
    {
        return optionArgu_;
    }
    // private method
    private static String GetPartialOption(String optionItem, boolean isFlag)
    {
        String partialStr = null;
        int index = -1;
        int lastIndex = -1;
        for(int i = 0; i < Option.MAX_LEVEL; i++)
        {
            char separator = Option.SEPARATOR[i];
            index = optionItem.indexOf(separator);
            if(index == -1)
            {
                if(isFlag == true)      // assign flag value
                {
                    partialStr = optionItem.substring(0, lastIndex);
                    // Top flag 
                    if(partialStr.length() == 0)
                    {
                        partialStr = Option.ROOT;
                    }
                }
                else // assign argument value
                {
                    partialStr = optionItem.substring(lastIndex+1);
                }
                break;
            }
            lastIndex = index;
        }
        return partialStr;
    }
    // data member
    private String optionItem_ = null;
    private String optionFlag_ = null;
    private String optionArgu_ = null;
}
