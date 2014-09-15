package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This class provides an example of using CmdLineSyntax package and OptionTest.
* <br> &lt Usage &gt this program uses user's command line arguments.
* Defualt arguments will be used if there is no argument.
* The system command line syntax (flags) are defined in optionTest
*
* <p><b>History:</b>
*
* @see OptionTest
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionMain
{
    // public constructor
    // public method
    /**
    * A test driver of command line system for using classes of Option and
    * option test
    */
    public static void main(String[] args)
    {
        String optionStr = "-a:aa~123+45+1:Absolute~12:ac:ad~adb "
            + "-B_Flag:bb:bc~bca:bd:be~bea_Full_Name~10.0 "
            + "-C_Flag";
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
        }
        Option io = new Option(optionStr);
        // define the system option flag & argument
        OptionTest optionTest = new OptionTest();
        // execute command according to option & argument
        System.out.println("---------- input: --------------");
        System.out.println(io.GetOptionStr());
        // print out help menu if there is a syntax error
        if(!SystemOption.CheckSyntax(io, optionTest.GetOption(), false, true))
        {
            optionTest.GetOption().PrintOptionHierachy();
        }
        else
        {
            // Execute the command
            optionTest.ExecuteCommands(io, optionTest.GetOption());
            System.out.println("------- ConvertName(name): --------------");
            System.out.println(OptionUtility.ConvertName(io.GetOptionStr(), 
                optionTest.GetOption(), false));
            System.out.println("------- ConvertName(fullName): --------------");
            System.out.println(OptionUtility.ConvertName(io.GetOptionStr(), 
                optionTest.GetOption(), true));
        }
    }
}
