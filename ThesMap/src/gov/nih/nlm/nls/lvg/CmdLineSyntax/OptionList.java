package gov.nih.nlm.nls.lvg.CmdLineSyntax;
import java.util.*;
/*****************************************************************************
* This OptionList class includes basic operations for a option list.
* This class provides function to connect (add) flags in an option list.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class OptionList<E> extends LinkedList<E>
{
    // Public Constructors
    /**
    * Create a option list object
    */
    public OptionList()
    {
        super();
        init();
    }
    // public methods
    /**
    * Add an option flag into a given option list
    *
    * @param   obj   an option flag to be added into option list
    */
    public void AddOptionFlag(E obj)
    {
        OptionFlag flag = (OptionFlag) obj;
        int curLevel = flag.GetLevel();
        // if the current level is 0 (top), add it
        if(curLevel == 0)
        {
            add(obj);
            parent_[curLevel+1] = flag;        // assign lower level's parent
        }
        else    // lower level
        {
            // update parent for input flag
            flag.SetParent(parent_[curLevel]);
            // init child list if it has no child 
            if(parent_[curLevel].GetChild() == null)    // no child
            {
                parent_[curLevel].SetChild(new OptionList<OptionFlag>());
            }
            // init & update child list 
            parent_[curLevel].GetChild().add(flag);
            parent_[curLevel+1] = flag;        // assign lower level's parent
        }
    }
    // private method
    private void init()
    {
        // init parent
        for(int i = 0; i < Option.MAX_LEVEL; i++)
        {
            parent_[i] = new OptionFlag(Option.ROOT, -1);
        }
    }
    // data member
    // working parents, only used in this class
    /** @serial */
    private OptionFlag[] parent_ = new OptionFlag[Option.MAX_LEVEL];    
    private final static long serialVersionUID = 5L;
}
