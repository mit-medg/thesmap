package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This is an abstract class for all systems to use and setup system command 
* line syntax.  A nice feature of it is to check the syntax of the input command
* line.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* <p><b>Notes:</b>
* It is illegal to have multiple arguments with one of them is NONE
* <br>For example: -a:aa~aaa~aab~NONE
* <br>Instead, it should change to -a:aa~aaa~aab~none 
* <br>where none is a pre-defined argument for flag -a
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public abstract class SystemOption
{
    // Public Constructors
    /**
    * Create the system option object
    */
    public SystemOption()
    {
        DefineFlag();
    }
    // public method
    /**
    * Get the defined system option of the command line syntax system
    *
    * @return  system option
    */
    public Option GetOption()
    {
        return systemOption_;
    }
    /**
    * Check if the input option is legal according to the given system option
    * This method can be used as syntax error checker
    *
    * @param  option  input option to be checked
    * @param  systemOption  system option that the checking is based on 
    * @param  printRun  a boolean flag to print algorithm details
    * @param  printError  a boolean flag to print error
    *
    * @return  legal option or not
    */
    public static boolean CheckSyntax(Option option, Option systemOption,
        boolean printRun, boolean printError)
    {
        boolean legalFlag = true;
        Vector<OptionItem> inputItems = option.GetOptionItems();
        // check for every single input option items
        for(int i = 0; i < inputItems.size(); i++)
        {
            OptionItem inputItem = inputItems.elementAt(i);
            legalFlag &= CheckOptionItem(inputItem, systemOption, printRun, 
                printError);
        }
        return legalFlag;
    }
    /**
    * Execute a given option in a command line system (with given system option)
    *
    * @param  inOption  option to be executed
    * @param  systemOption  system option for the command line
    */
    public void ExecuteCommands(Option inOption, Option systemOption)
    {
        Vector<OptionItem> optionItems = inOption.GetOptionItems();
        for(int i = 0; i < optionItems.size(); i++)
        {
            OptionItem optionItem = optionItems.elementAt(i);
            ExecuteCommand(optionItem, systemOption);
        }
    }
    // protected abstract function 
    protected abstract void ExecuteCommand(OptionItem optionItem, 
        Option systemOption);
    protected abstract void DefineFlag();
    // protected methods
    protected boolean ToBoolean(String in)
    {
        boolean argu = false;
        if((in.equalsIgnoreCase("t")) || (in.equalsIgnoreCase("true")))
        {
            argu = true;
        }
        return argu;
    }
    protected int[] ToIntArray(Vector<String> inList)
    {
        int[] array = new int[inList.size()];
        int index = 0;
        for(Iterator<String> it = inList.iterator(); it.hasNext();)
        {
            array[index++] = Integer.parseInt(it.next());
        }
        return array;
    }
    protected float[] ToFloatArray(Vector<String> inList)
    {
        float[] array = new float[inList.size()];
        int index = 0;
        for(Iterator<String> it = inList.iterator(); it.hasNext();)
        {
            array[index++] = Float.parseFloat(it.next());
        }
        return array;
    }
    protected String[] ToStringArray(Vector<String> inList)
    {
        String[] array = (String[]) inList.toArray();
        return array;
    }
    protected static boolean CheckOption(OptionItem inItem, 
        String systemItemStr)
    {
        OptionItem systemItem = new OptionItem(systemItemStr);
        boolean legalFlag = CheckOption(inItem, systemItem);
        return legalFlag;
    }
    // private methods
    private static boolean CheckOptionItem(OptionItem inputItem, 
        Option systemOption, boolean PrintRun, boolean PrintError)
    {
        //Change all names or fullNames to names 
        OptionItem nameItem = 
            OptionUtility.GetItemByName(inputItem, systemOption, false);
        Vector<OptionItem> systemItems = systemOption.GetOptionItems();
        boolean legalOption = false;
        for(int i = 0; i < systemItems.size(); i++)
        {
            OptionItem systemItem = systemItems.elementAt(i);
            // check flag & arguments
            if(CheckOption(nameItem, systemItem) == true)
            {
                if(PrintRun == true)
                {
                    System.out.println("Run: " + inputItem.GetOptionItem() +
                        " => " + systemItem.GetOptionFlag() + ", '" +
                        systemItem.GetOptionArgument() + "'.");
                }
                legalOption = true;
                break;
            }
        }
        // print out error message
        if(legalOption == false)
        {
            if(PrintError == true)
            {
                System.out.println("*** Syntax Error: " 
                    + inputItem.GetOptionItem() + "(" 
                    + GetLegalSyntax(inputItem, systemOption) + ").");
            }
        }
        return legalOption;
    }
    // The input item must be an illegal option
    private static String GetLegalSyntax(OptionItem item, Option systemOption)
    {
        Vector<String> msgList = new Vector<String>(1);
        String msg = null;
        String msgFlag = "";
        String msgArgu = "";
        Vector<OptionItem> systemItems = systemOption.GetOptionItems();
        String itemStr = item.GetOptionItem();
        String flag = item.GetOptionFlag();
        String argu = item.GetOptionArgument();
        String lastStr = null;        // option Str from last loop
        boolean errorFlag = false;
        for(int i = 0; i < systemItems.size(); i++)
        {
            OptionItem systemItem = systemItems.elementAt(i);
            // check if input option item is part of system option item
            String systemItemStr = systemItem.GetOptionItem();
            int index = systemItemStr.indexOf(itemStr);
            if(index != -1)
            {
                // combine option Items if the argument is different
                String newStr = CombineArguments(lastStr, systemItemStr);
                // update the msgList
                if(newStr.equals(lastStr)  == false)
                {
                    int ind = msgList.size()-1;
                    if(ind == -1)        // nothing in msgList
                    {
                        msgList.addElement(newStr);
                    }
                    else // update the last element in msgList
                    {
                        msgList.setElementAt(newStr, ind);
                    }
                    lastStr = newStr;
                }
            }
            // Check option with same flag and different argument
            if(((systemItem.GetOptionFlag()).equals(flag) == true)
            && (CheckArguments(argu, systemItem.GetOptionArgument()) == false))
            {
                msgFlag = systemItem.GetOptionFlag();
                // set the flag = "" if it is "Root"
                if(msgFlag.equals(Option.ROOT) == true)
                {
                    msgFlag = "";
                }
                msgArgu += (OptionItem.GetSeparator(msgFlag, 1) 
                    + systemItem.GetOptionArgument());
                errorFlag = true;
            }
        }
        // update msgList if an option with same flag and different argument
        if(errorFlag == true)
        {
            msgList.addElement(msgFlag + msgArgu);
        }
        // compose the msg
        if(msgList.size() == 0)
        {
            msg = "Illegal option arguments; Too many levels!";
        }
        else
        {
            msg = msgList.elementAt(0);
            for(int i = 1; i < msgList.size(); i++)
            {
                msg += ("; " + msgList.elementAt(i));
            }
        }
        return msg;
    }
    // add a new arguments from systemStr and combine the same flags 
    private static String CombineArguments(String oldStr, String systemStr)
    {
        // check if the oldStr is null: first time
        if(oldStr == null)
        {
            return systemStr;
        }
        // combine 
        Option option = new Option(oldStr);
        Vector<OptionItem> optionItems = option.GetOptionItems();
        OptionItem optionItem = new OptionItem(systemStr);
        optionItems.addElement(optionItem);
        String newStr = OptionUtility.GetOptionStr(optionItems);
        return newStr;
    }
    private static boolean CheckOption(OptionItem inItem, 
        OptionItem systemItem)
    {
        String flag = inItem.GetOptionFlag();
        String argu = inItem.GetOptionArgument();
        boolean legalFlag = 
            (((systemItem.GetOptionFlag().equals(flag)) == true)
            && (CheckArguments(argu, systemItem.GetOptionArgument()) == true));
        return legalFlag;
    }
    private static boolean CheckArguments(String in, String system)
    {
        boolean legalFlag = false;
        // check if the argument contain multiple arguments
        int index = system.indexOf(Option.ARGUMENT_SEP);
        if(index == -1)
        {
            legalFlag = IsLegalArgument(in, system);
        }
        else // multiple arguments
        {
            legalFlag = IsLegalArguments(in, system);
        }
        return legalFlag;
    }
    // check if the input arguments are all legal arguments
    private static boolean IsLegalArguments(String in, String system)
    {
        boolean flag = false;
        Vector<String> systemList = OptionItem.GetArgumentList(system);
        Vector<String> inList = OptionItem.GetArgumentList(in);
        // check the total number of the arguments
        if(systemList.size() == inList.size())
        {
            // loop through all legal argument
            for(int i = 0; i < inList.size(); i++)
            {
                flag = IsLegalArgument(inList.elementAt(i), 
                    systemList.elementAt(i));
                if(flag == false)
                {
                    break;
                }
            }
        }
        return flag;
    }
    // check if the input argument is a legal argument
    private static boolean IsLegalArgument(String in, String system)
    {
        boolean flag = false;
        if(system.equals(Option.STRING_ARGUMENT))    // String argument
        {
            flag = true;
        }
        else if(system.equals(in))                    // preDefined flag
        {
            flag = true;
        }
        else if(system.equals(Option.INT_ARGUMENT))        // int number
        {
            try
            {
                Integer.parseInt(in);
                flag = true;
            }
            catch (NumberFormatException e) { }
        }
        else if(system.equals(Option.LONG_ARGUMENT))    // long number
        {
            try
            {
                Long.parseLong(in);
                flag = true;
            }
            catch (NumberFormatException e) { }
        }
        else if(system.equals(Option.FLOAT_ARGUMENT))    // float number
        {
            try
            {
                Float.parseFloat(in);
                flag = true;
            }
            catch (NumberFormatException e) { }
        }
        else if(system.equals(Option.BOOLEAN_ARGUMENT))        // boolean
        {
            if((in.equalsIgnoreCase("t")) || (in.equalsIgnoreCase("f"))
            || (in.equalsIgnoreCase("true")) || (in.equalsIgnoreCase("false")))
            {
                flag = true;
            }
        }
        return flag;
    }
    // data member
    protected Option systemOption_ = null;
}
