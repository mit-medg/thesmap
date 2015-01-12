package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This class provides utility functions for Option operations. 
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionUtility
{
    // Public Constructors
    public OptionUtility()
    {
    }
    // public method
    /**
    * Convert an Option object from string format to option list format
    *
    * @param  optionStr  option in string format
    *
    * @return  option in optionList format
    *
    * @see OptionList
    */
    public static OptionList<OptionFlag> ToOptionList(String optionStr)
    {
        Option option = new Option(optionStr);
        return option.GetOptionList();
    }
    /**
    * Convert an option from OptionList format to string format
    *
    * @param  optionList  option in optionList format
    *
    * @return  option in string format;
    *
    * @see OptionList
    */
    public static String ToOptionString(OptionList<OptionFlag> optionList)
    {
        String optionStr = "";
        // go through all child
        for(ListIterator<OptionFlag> lit = optionList.listIterator(); 
            lit.hasNext();)
        {
            OptionFlag flag = lit.next();
            // add " " at first level
            if((flag.GetLevel() == 0) && (optionStr.length() > 0))
            {
                optionStr += 
                    (" " + Option.SEPARATOR[flag.GetLevel()] + flag.GetName());
            }
            else
            {
                optionStr += 
                    (Option.SEPARATOR[flag.GetLevel()] + flag.GetName());
            }
            // When flag has child, call itself recursively
            if(flag.GetChild() != null)
            {
                optionStr += ToOptionString(flag.GetChild());
            }
        }
        return optionStr;
    }
    /**
    * Convert between full name and short name for a given option string
    *
    * @param  optionStr  option to be convert in a string format
    * @param  systemOption  system option which the convertion is based on 
    * @param  fullNameFlag  boolean flag for full name
    *         if true, convert to full name for option string
    *         if false, convert to name for option string
    *
    * @return option in string format
    */
    public static String ConvertName(String optionStr, Option systemOption, 
        boolean fullNameFlag)
    {
        String name = null;
        Option option = new Option(optionStr);
        Vector<OptionItem> optionItems = option.GetOptionItems();
        Vector<OptionItem> newItems = new Vector<OptionItem>(1);
        // Change to Name
        for(int i = 0; i < optionItems.size(); i++)
        {
            OptionItem optionItem = optionItems.elementAt(i);
            OptionItem newItem = null;
            newItem = GetItemByName(optionItem, systemOption, fullNameFlag);
            newItems.addElement(newItem);
        }
        // compose to optionStr 
        name = GetOptionStr(newItems);
        return name;
    }
    /**
    * Get the optionItem from an given inputItem by specifying the known name
    *
    * @param  inputItem  the input option item for retrieving 
    * @param  systemOption  system option which the operation is based on
    * @param  fullNameFlag  boolean flag for full name
    *         if true, convert to full name for option string
    *         if false, convert to name for option string
    *
    * @return  option item
    */
    public static OptionItem GetItemByName(OptionItem inputItem, 
        Option systemOption, boolean fullNameFlag)
    {
        String itemStr = inputItem.GetOptionItem();
        OptionList<OptionFlag> systemOptionList = systemOption.GetOptionList();
        String itemName = 
            UpdateItemStrToName(itemStr, 0, systemOptionList, fullNameFlag);
        OptionItem itemByName = new OptionItem(itemName);
        return itemByName;
    }
    /**
    * Get Option in a String format by giving a option in Vector<OptionItem>
    *
    * @param   optionItems  Vector<OptionItem> - option items
    *
    * @return  an option in a string format
    */
    public static String GetOptionStr(Vector<OptionItem> optionItems)
    {
        String optionStr = "";
        OptionItem lastOptionItem = null;
        for(int i = 0; i < optionItems.size(); i++)
        {
            OptionItem optionItem = optionItems.elementAt(i);
            // first item in the vector
            if(lastOptionItem == null)
            {
                optionStr = optionItem.GetOptionItem();
            }
            else
            {
                optionStr += GetNewArgument(lastOptionItem.GetOptionItem(),
                    optionItem.GetOptionItem());
            }
            lastOptionItem = optionItem;
        }
        return optionStr;
    }
    /**
    * Get the level of a given option in an option hierarchical structure
    *
    * @param  item  option in a string format
    *
    * @return  level of the given option in the hierarchical structure 
    */
    public static int GetLevelNum(String item)
    {
        int level = -1;
        // if the input item is null, return -1 means illegal item
        if(item == null)
        {
            return level;
        }
        // go through all separators to find the level
        for(int i = 0; i < Option.MAX_LEVEL; i++)
        {
            int index = item.indexOf(Option.SEPARATOR[i]);
            if(index == -1)
            {
                level = i-1;
                break;
            }
        }
        return level;
    }
    /**
    * Get an option flag for a specified level from a given option
    *
    * @param  item  option in a string format
    * @param  curLevel  level, which option string will be return
    *
    * @return  the option flag for a specified level from a given option
    */
    public static String GetLevelFlag(String item, int curLevel)
    {
        String flag = null;
        int curIndex = item.indexOf(Option.SEPARATOR[curLevel]);
        int nextIndex = item.indexOf(Option.SEPARATOR[curLevel+1]);
        // if no nextIndex existes, the last item is it
        if((curIndex != -1) && (nextIndex == -1))
        {
            flag = item.substring(curIndex+1);
        }
        //  if both index are legal
        else if((curIndex != -1) && (nextIndex != -1) && (nextIndex > curIndex))
        {
            flag = item.substring(curIndex+1, nextIndex);
        }
        else
        {
            flag = "-- Error --";
        }
        return flag;
    }
    // private methods
    private static String GetNewArgument(String orgItem, String newItem)
    {
        String newArgument = "";
        // TBD: check if two identical options happen sequentially
        if(orgItem.equals(newItem))
        {
            newArgument = " " + newItem;
        }
        else
        {
            for(int i = 0; i < GetLevelNum(newItem)+1; i++)
            {
                String newFlag = GetFlag(newItem, i);
                if(GetFlag(orgItem, i).equals(newFlag) == false)
                {
                    if(i == 0)
                    {
                        newArgument = " " + GetArgument(newItem, i);
                    }
                    else
                    {
                        newArgument = GetArgument(newItem, i);
                    }
                    break;
                }
            }
        }
        return newArgument;
    }
    private static String GetFlag(String item, int curLevel)
    {
        String flag = null;
        int index = item.indexOf(Option.SEPARATOR[curLevel+1]);
        if(index == -1)
        {
            flag = item;
        }
        else
        {
            flag = item.substring(0, index);
        }
        return flag;
    }
    private static String GetArgument(String item, int curLevel)
    {
        String argument = null;
        int index = item.indexOf(Option.SEPARATOR[curLevel]);
        argument = item.substring(index);
        return argument;
    }
    private static String UpdateItemStrToName(String itemStr, int curLevel,
        OptionList<OptionFlag> systemOptionList, boolean fullNameFlag)
    {
        String nameStr = itemStr;
        // No such child list
        if(systemOptionList == null)
        {
            return nameStr;
        }
        // go through all system option list
        for(ListIterator<OptionFlag> lit = systemOptionList.listIterator(); 
            lit.hasNext();)
        {
            OptionFlag flag = lit.next();
            // found matched name
            if(flag.GetName().equals(GetLevelFlag(itemStr, curLevel)))
            {
                // change itemstr to full name
                if(fullNameFlag == true)
                {
                    String name = flag.GetFullName();
                    if(name == null)
                    {
                        name = flag.GetName();
                    }
                    nameStr = UpdateItemStr(itemStr, curLevel, name);
                }
                systemOptionList = flag.GetChild();
                curLevel++;
                if(curLevel <= GetLevelNum(itemStr))
                {
                    nameStr = UpdateItemStrToName(nameStr, curLevel, 
                        systemOptionList, fullNameFlag);
                }
                break;
            }
            // found matched full name
            else if(GetLevelFlag(itemStr, curLevel).equals(flag.GetFullName()))
            {
                // change itemstr to name
                if(fullNameFlag == false)
                {
                    nameStr = UpdateItemStr(itemStr, curLevel, flag.GetName());
                }
                systemOptionList = flag.GetChild();
                curLevel++;
                if(curLevel <= GetLevelNum(itemStr))
                {
                    nameStr = UpdateItemStrToName(nameStr, curLevel, 
                        systemOptionList, fullNameFlag);
                }
                break;
            }
        }
        return nameStr;
    }
    private static String UpdateItemStr(String itemStr, int curLevel,
        String name)
    {
        String nameStr = itemStr;
        int size = itemStr.length();
        int stIndex = itemStr.indexOf(Option.SEPARATOR[curLevel]);
        int endIndex = -1;
        
        if(curLevel < Option.MAX_LEVEL)
        {
            endIndex = itemStr.indexOf(Option.SEPARATOR[curLevel+1]);
        }
        if(endIndex == -1)
        {
            endIndex = size;
        }
        nameStr = itemStr.substring(0, stIndex+1) + name + 
            itemStr.substring(endIndex);
        return nameStr;
    }
}
