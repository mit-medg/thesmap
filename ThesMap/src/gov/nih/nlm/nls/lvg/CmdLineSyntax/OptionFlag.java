package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This class OptionFlag represents an object of an option flag.  An option flag 
* is an item in a liked list.  It includes name, parent, and child.
* An option flag may has none, one, or multiple argument(s).  
* It's arguments are its children in the linked list format.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionFlag
{
    // Public Constructors
    /**
    * Default constructor to create an option flag
    */
    public OptionFlag()
    {
    }
    /**
    * Create an option flag, using the option item in string format and the 
    * level in the option hierarchy structure
    */
    public OptionFlag(String name, int level)
    {
        level_ = level;
        name_ = name;
    }
    // public methods
    /**
    * Set the parent of current optionFlag
    *
    * @param  parent  a parent OptionFlag 
    */
    public void SetParent(OptionFlag parent)
    {
        parent_ = parent;
    }
    /**
    * Set the child of current optionFlag
    *
    * @param  child  child optionList
    *
    * @see OptionList
    */
    public void SetChild(OptionList<OptionFlag> child)
    {
        child_ = child;
    }
    /**
    * Set the fullname of optionFlag
    *
    * @param  fullName  full name in a string format 
    */
    public void SetFullName(String fullName)
    {
        fullName_ = fullName;
    }
    /**
    * Get the level of current option flag form option hierarchical structure
    *
    * @return  level of current option flag in the option hierarchy structure
    */
    public int GetLevel()
    {
        return level_;
    }
    /**
    * Get the name of current option flag
    *
    * @return  name of current option flag
    */
    public String GetName()
    {
        return name_;
    }
    /**
    * Get the full name of current optin flag
    *
    * @return  full name of current option flag
    */
    public String GetFullName()
    {
        return fullName_;
    }
    /**
    * Get the parent of current OptionFlag
    *
    * @return  parent OptionFlag of current option flag
    */
    public OptionFlag GetParent()
    {
        return parent_;
    }
    /**
    * Get the child OptionList of current OptinoFlag
    *
    * @return  OptionList<OptionFlag> child OptionList of current option flag
    */
    public OptionList<OptionFlag> GetChild()
    {
        return child_;
    }
    /**
    * Print detail information for the current OptionFlag
    */
    public void PrintInfo()
    {
        System.out.println("--------------- Flag -------------------");
        System.out.println("Level:    " + level_); 
        System.out.println("name:  " + name_); 
        System.out.println("fullName: " + fullName_); 
        System.out.println("parent:   " + parent_); 
        System.out.println("Child:    " + child_); 
    }
    // private data members
    private int level_ = 0;                 // hierachy level in the list
    private String name_ = null;            // short name of flag
    private String fullName_ = null;        // full name of flag
    private OptionFlag parent_ = null;      // parent flag of current flag
    private OptionList<OptionFlag> child_ = null;       // child list
}
