package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* The class Option represents an object of a command line system.
* The options of a command line system can be represented in 3 formats:
* <ol>
* <li> A linked list (OptionList)
* <li> An option syntaxed String
* <li> A vector (Array) of OptionItems
* </ol>
*
* <p><b>History:</b>
* <ul>
* <li>SCR-11, chlu, 06-04-12, Fixed compile error due to JDK upgrade
* </ul>
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Option
{
    // Public Constructors
    /**
    * Create a new command line option, using a string format as the input
    */
    public Option(String optionStr)
    {
        optionStr_ = optionStr;
        FormOptionList(optionStr);    // init optionList_
        Init();                       // init optionItems_
    }
    /**
    * Create a new command line option, using an OptionList as the input
    */
    public Option(OptionList<OptionFlag> optionList)
    {
        optionStr_ = OptionUtility.ToOptionString(optionList);
        optionList_ = optionList;
        Init();                       // init optionItems_
    }
    // public methods
    /**
    * Set the full name of a flag option for a option item
    *
    * @param  item   a String specifying the option item (option symbol)  
    * @param  fullName   a String specifying the full name of an option flag of 
    *                    item.
    */
    public void SetFlagFullName(String item, String fullName)
    {
        // go through all child
        OptionFlag flag = GetOptionFlag(optionList_, item, 0);
        if(flag != null)
        {
            flag.SetFullName(fullName);
        }
    }
    /**
    * Get the option in a String format
    *
    * @return  the option in a String format;
    */
    public String GetOptionStr()
    {
        return optionStr_;
    }
    /**
    * Get the option in an OptionList format
    *
    * @return  a option in OptionList format (a linked list with option flags 
    *          as it's elements;
    *
    * @see OptionList
    */
    public OptionList<OptionFlag> GetOptionList()
    {
        return optionList_;
    }
    /**
    * Get all option items of the option object
    *
    * @return  Vector<OptionItem> all option itmes (OptionItem) of this option
    *
    * @see OptionItem
    */
    public Vector<OptionItem> GetOptionItems()
    {
        return optionItems_;
    }
    /**
    * Get Option Items in String format
    *
    * @return  Vector<String> all option items in the String format
    */
    public Vector<String> GetOptionItemStrs()
    {
         return GetOptionItem(optionList_);
    }
    /**
    * Print out the hierarchical structure for all options
    */
    public void PrintOptionHierachy()
    {
        PrintOptionList(optionList_);
    }
    /**
    * Print out the hierarchical structure for all options, including
    * detail option items 
    */
    public void PrintOptionLeaf()
    {
        PrintOptionItems(optionList_);
    }
    /**
    * A test drivier for testing methods in this class
    */
    public static void main(String[] args)
    {
        Option o = new 
        Option("-a:aa~123+45+78:ab~aba~12:ac:ad~ada -b:bb:bc~bca+bcb:bd:be -c");
        if(args.length > 0)
        {
            String optionStr = "";
            for(int i = 0; i < args.length; i++)
            {
                optionStr += args[i];
            }
            o = new Option(optionStr);
        }
        System.out.println("------------ Option String --------------");
        System.out.println("Option String: " + o.GetOptionStr());
        System.out.println("------------ Hierachy -------------------");
        o.PrintOptionHierachy();
        System.out.println("------------ Option Leaf -------------------");
        o.PrintOptionLeaf();
        System.out.println("------------ Option String -------------------");
        System.out.println(OptionUtility.ToOptionString(o.GetOptionList()));
        Option o1 = new Option(o.GetOptionList());
        System.out.println("------------ Option1 String -------------------");
        System.out.println(o1.GetOptionStr());
        System.out.println("------------ GetOptionStr() -------------------");
        System.out.println(OptionUtility.GetOptionStr(o1.GetOptionItems()));
    }
    // private method
    private void Init()
    {
        // init optionItems_
        Vector<String> optionItemStrs = GetOptionItemStrs();
        for(int i = 0; i < optionItemStrs.size(); i++)
        {
            String optionItemStr = optionItemStrs.elementAt(i);
            OptionItem optionItem = new OptionItem(optionItemStr);
            optionItems_.addElement(optionItem);
        }
    }
    // return Vector<String> of String which represents optionItems
    private Vector<String> GetOptionItem(OptionList<OptionFlag> optionList)
    {
        Vector<String> optionItems = new Vector<String>(1);
        // go through all children
        for(ListIterator<OptionFlag> lit = optionList.listIterator(); 
            lit.hasNext();)
        {
            OptionFlag flag = lit.next();
            // if it has child
            if(flag.GetChild() != null)
            {
                parentStr_[flag.GetLevel()] = flag.GetName();
                // recursively call itself
                Vector<String> childItems = GetOptionItem(flag.GetChild());
                for(int i = 0; i < childItems.size(); i++)
                {
                    optionItems.addElement(childItems.elementAt(i)); 
                }
            }
            else    // if no child
            {
                String flagStr = "";
                for(int i = 0; i < flag.GetLevel(); i++ )
                {
                    flagStr += (SEPARATOR[i] + parentStr_[i]);
                }
                flagStr += SEPARATOR[flag.GetLevel()] + flag.GetName();
                optionItems.addElement(flagStr);
            }
        }
        return optionItems;
    }
    // recursive print: Print option Items in String Vecotr format
    private void PrintOptionItems(OptionList<OptionFlag> optionList)
    {
        Vector<String> optionItems = GetOptionItem(optionList);
        for(int i = 0; i < optionItems.size(); i++)
        {
            String optionItemStr = optionItems.elementAt(i);
            System.out.println(optionItemStr + "=> " 
                + OptionItem.GetOptionFlag(optionItemStr) + ", " 
                + OptionItem.GetOptionArgument(optionItemStr));
        }
    }
    // recursive print: Print option List in a Hierachical format
    private void PrintOptionList(OptionList<OptionFlag> optionList)
    {
        // go through all child
        for(ListIterator<OptionFlag> lit = optionList.listIterator(); 
            lit.hasNext();)
        {
            OptionFlag flag = lit.next();
            PrintLevelIndent(flag.GetLevel());
            System.out.print(" " +  flag.GetName());
            if(flag.GetFullName() == null)
            {
                System.out.println("");
            }
            else
            {
                System.out.println(" (" + flag.GetFullName() + ")");
            }
            // print it's child
            if(flag.GetChild() != null)
            {
                PrintOptionList(flag.GetChild());
            }
        }
    }
    private void PrintLevelIndent(int level)
    {
        int numOfDash = (level)*2+1;
        for(int i = 0; i < numOfDash; i++)
        {
            System.out.print("-");
        }
    }
    private void FormOptionList(String optionStr)
    {
        Compose(optionStr, 0);
    }
    // recursive: compose option linked list form option string
    private void Compose(String optionStr, int curLevel)
    {
        // check max level
        if(curLevel < MAX_LEVEL)
        {
            // setup delimiter & tokenlizer
            String delimiter = " " + SEPARATOR[curLevel];
            StringTokenizer buf = new StringTokenizer(optionStr, delimiter);
            // recursive loop: go through every token
            while(buf.hasMoreTokens())
            {
                String token = buf.nextToken(); // token is the separated item
                AddCurFlag(token, curLevel);    // Add cur flag to Optionlist
                String nextLevelItemStr = GetNextLevelItemStr(token, curLevel);
                // branch has child
                if(nextLevelItemStr != null)
                {
                    Compose(nextLevelItemStr, curLevel+1);
                }
                // return to upper level if no child and last in the branch
                else if(!buf.hasMoreTokens())
                {
                    return;
                }
            }
        }
        else
        {
            System.err.println("** Error: curLevel is too large (" + curLevel
                + ").");
        }
    }
    
    private void AddCurFlag(String str, int curLevel)
    {
        String delimiter = "" + SEPARATOR[curLevel+1];
        
        StringTokenizer buf = new StringTokenizer(str, delimiter);
        String curFlag = buf.nextToken();
        // compose linked list
        optionList_.AddOptionFlag(new OptionFlag(curFlag, curLevel));
    }
    private String GetNextLevelItemStr(String str, int curLevel)
    {
        String nextLevelStr = null;
        int index = str.indexOf(SEPARATOR[curLevel+1]);
        if(index != -1)
        {
            nextLevelStr = str.substring(index);
        }
        return nextLevelStr;
    }
    private OptionFlag GetOptionFlag(OptionList<OptionFlag> optionList, 
        String item, int curLevel)
    {
        OptionFlag flag = null;
        // go throuogh the list
        for(ListIterator<OptionFlag> lit = optionList.listIterator(); 
            lit.hasNext();)
        {
            flag = lit.next();
            if(flag.GetName().equals(
                OptionUtility.GetLevelFlag(item, curLevel)))
            {
                optionList = flag.GetChild();
                curLevel++;
                if(curLevel <= OptionUtility.GetLevelNum(item))
                {
                    flag = GetOptionFlag(optionList, item, curLevel);
                }
                return flag;
            }
        }
        // print out error message
        System.err.println(
            "** Error: Can't find a matched OptionFlag for '" + item + "'");
        flag = null;
        return flag;
    }
    // public data members
    /** The maximum level of the command line system */
    public static final int MAX_LEVEL = 5;
    /** The separator of an argument */
    public static final char ARGUMENT_SEP = '+';
    /** The separator of option levels */
    public static final char[] SEPARATOR = {'-', ':', '~', '^', '#'};
    /** The name of the root in an option structure tree */
    public static final String ROOT = "Root";
    /** an abbreviation of a boolean argument in an option */
    public static final String BOOLEAN_ARGUMENT = "BLN";
    /** an abbreviation of a String argument in an option */
    public static final String STRING_ARGUMENT = "STR";
    /** an abbreviation of an int argument in an option */
    public static final String INT_ARGUMENT = "INT";
    /** an abbreviation of a long argument in an option */
    public static final String LONG_ARGUMENT = "LONG";
    /** an abbreviation of a float argument in an option */
    public static final String FLOAT_ARGUMENT = "FLT";
    // private data 
    private static String[] parentStr_ = {"", "", "", "", ""};  // private use
    private String optionStr_ = null;        // the syntaxed String of Option
    private OptionList<OptionFlag> optionList_ = new OptionList<OptionFlag>();  
        // option's linked list 
    private Vector<OptionItem> optionItems_ = new Vector<OptionItem>(1);    
        // expanded optionItems
}
