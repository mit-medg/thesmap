package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This class demonstrates how to customize specific command line syntax by 
* using this package.
* <ol>
* <li>This class must extends SystemOption class
* <li>Define the legal option flags in DefineFlag().
* <li>Define what to do for each option in ExecuteCommand().
* <li>If help menus are needed, add help flags and treat them as above.
* </ol>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionTest extends SystemOption
{
    // public constructor
    // public method
    /**
    * A test driver for using command line system
    */
    public static void main(String[] args)
    {
        // define the system option
        String optionStr = "-a:aa~123+45+1:Absolute~12:ac:ad~adb "
            + "-B_Flag:bb:bc~bca:bd:be~bea_Full_Name~10.0 "
            + "-C_Flag";
        Option io = new Option(optionStr);
        if(args.length > 0)
        {
            optionStr = "";
            for(int i = 0; i < args.length; i++)
            {
                if(i == 0)
                {
                    optionStr = args[i];
                }
                else
                {
                    optionStr += (" " + args[i]);
                }
            }
            io = new Option(optionStr);
        }
        // define the system option flag & argument
        OptionTest optionTest = new OptionTest();
        // execute command according to option & argument
        System.out.println("---------- input: --------------");
        System.out.println(io.GetOptionStr());
        System.out.println("---------- System: --------------");
        System.out.println(optionTest.GetOption().GetOptionStr());
        System.out.println("---------- Result: --------------");
        SystemOption.CheckSyntax(io, optionTest.GetOption(), true, true);
        System.out.println("---------- ConvertName(name): --------------");
        System.out.println(OptionUtility.ConvertName(io.GetOptionStr(), 
            optionTest.GetOption(), false));
        System.out.println("---------- ConvertName(fullName): --------------");
        System.out.println(OptionUtility.ConvertName(io.GetOptionStr(), 
            optionTest.GetOption(), true));
    }
    // protected methods
    /**
    * Execute input command based on the predefined system command option
    */
    protected void ExecuteCommand(OptionItem optionItem, Option systemOption)
    {
        OptionItem nameItem =
            OptionUtility.GetItemByName(optionItem, systemOption, false);
        Vector<OptionItem> systemItems = systemOption.GetOptionItems();
        if(CheckOption(nameItem, "-a:aa~INT+INT+INT") == true)
        {
            Vector<String> inList = 
                OptionItem.GetArgumentList(nameItem.GetOptionArgument());
            int[] arguList = ToIntArray(inList);
            System.out.println("=> Call '-a:aa~INT+INT+INT' flag function ("
                + arguList[0] + ", " + arguList[1] + ", " + arguList[2] + ")");
        }
        else if(CheckOption(nameItem, "-a:ab~aba") == true)
        {
            System.out.println("=> Call '-a:ab~aba' flag function");
        }
        else if(CheckOption(nameItem, "-a:ab~INT") == true)
        {
            int argu = Integer.parseInt(nameItem.GetOptionArgument());
            System.out.println("=> Call '-a:ab~INT' flag function (" + argu 
                + ")"); 
        }
        else if(CheckOption(nameItem, "-a:ac") == true)
        {
            System.out.println("=> Call '-a:ac' flag function");
        }
        else if(CheckOption(nameItem, "-a:ad:ada") == true)
        {
            System.out.println("=> Call '-a:ad:ada' flag function");
        }
        else if(CheckOption(nameItem, "-a:ad~adb") == true)
        {
            System.out.println("=> Call '-a:ad~adb' flag function");
        }
        else if(CheckOption(nameItem, "-a:ad~adc") == true)
        {
            System.out.println("=> Call '-a:ad~adc' flag function");
        }
        else if(CheckOption(nameItem, "-b:bb") == true)
        {
            System.out.println("=> Call '-b:bb' flag function");
        }
        else if(CheckOption(nameItem, "-b:bc~bca") == true)
        {
            System.out.println("=> Call '-b:bc~bca' flag function");
        }
        else if(CheckOption(nameItem, "-b:bc~bcb") == true)
        {
            System.out.println("=> Call '-b:bc~bcb' flag function");
        }
        else if(CheckOption(nameItem, "-b:bc~bcc") == true)
        {
            System.out.println("=> Call '-b:bc~bcc' flag function");
        }
        else if(CheckOption(nameItem, "-b:bd") == true)
        {
            System.out.println("=> Call '-b:bd' flag function");
        }
        else if(CheckOption(nameItem, "-b:be~INT") == true)
        {
            int argu = Integer.parseInt(nameItem.GetOptionArgument());
            System.out.println("=> Call '-b:be~INT' flag function (" + argu 
                + ")"); 
        }
        else if(CheckOption(nameItem, "-b:be~bea") == true)
        {
            System.out.println("=> Call '-b:be~bea' flag function");
        }
        else if(CheckOption(nameItem, "-b:be~BLN") == true)
        {
            boolean argu = ToBoolean(nameItem.GetOptionArgument());
            System.out.println("=> Call '-b:be~BLN' flag function (" + argu 
                + ")");
        }
        else if(CheckOption(nameItem, "-b:be~FLT") == true)
        {
            float argu = Float.parseFloat(nameItem.GetOptionArgument());
            System.out.println("=> Call '-b:be~FLT' flag function (" + argu 
                + ")"); 
        }
        else if(CheckOption(nameItem, "-b:be~STR") == true)
        {
            String argu = nameItem.GetOptionArgument();
            System.out.println("=> Call '-b:be~STR' flag function (" + argu 
                + ")"); 
        }
        else if(CheckOption(nameItem, "-c") == true)
        {
            System.out.println("=> call '-c' flag function");
        }
    }
    /**
    * Define option flag, including structure and full names
    */
    protected void DefineFlag()
    {
        // define all option flags & arguments by giving a option string
        String flagStr = "-a:aa~INT+INT+INT:ab~aba~INT:ac:ad~ada~adb~adc " 
            + "-b:bb:bc~bca~bcb~bcc:bd:be~INT~bea~BLN~FLT~STR " 
            + "-c";
        // init the system option
        systemOption_ = new Option(flagStr);
        // Add the full name for flags
        systemOption_.SetFlagFullName("-a:ab", "Absolute");
        systemOption_.SetFlagFullName("-a:ab~aba", "abaFullName");
        //systemOption_.SetFlagFullName("-a:be~bea", "bea_Full_Name");
        systemOption_.SetFlagFullName("-b", "B_Flag");
        systemOption_.SetFlagFullName("-b:be~bea", "bea_Full_Name");
        systemOption_.SetFlagFullName("-c", "C_Flag");
    }
}
