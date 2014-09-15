package gov.nih.nlm.nls.lvg.Api;
import java.util.*;
import java.sql.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Flows.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides an API for WordInd.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class WordIndApi
{
    // private constructor
    /**
    * Creates a WordIndApi object and initiate related data (default).
    */
    public WordIndApi()
    {
    }
    // public methods
    /**
    * A method to get the tokenized strings of an input LexItem
    *
    * @param   in  an input LexItem to be mutated
    *
    * @return  Vector<LexItem> - target strings are tokenized Strings 
    */
    public Vector<LexItem> Mutate(LexItem in) throws Exception
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // process: Mutate
        outs = ToTokenize.Mutate(in, false, false);
        return outs;
    }
    /**
    * A method to get the tokenized strings of an input string
    *
    * @param   inTerm  an input term in a string format to be mutated
    *
    * @return  Vector<String> of tokenized Strings
    */
    public Vector<String> Mutate(String inTerm) throws Exception
    {
        // declare a new LexItem for input
        String inStr = inTerm;
        if(reserveCaseFlag_ == false)
        {
            inStr = inStr.toLowerCase();
        }
        LexItem in = new LexItem(inStr);
        Vector<LexItem> outs = Mutate(in);
        // just return the tokenized String
        Vector<String> outStrs = new Vector<String>();
        for(int i = 0; i < outs.size(); i ++)
        {
            LexItem cur = outs.elementAt(i);
            outStrs.addElement(cur.GetTargetTerm());
        }
        return outStrs;
    }
    /**
    * A method to set the flag of reserve case after tokenization
    *
    * @param   reserveCaseFlag  an boolean flag of reserving case of tokens
    *
    */
    public void SetReserveCase(boolean reserveCaseFlag)
    {
        reserveCaseFlag_ = reserveCaseFlag;
    }
    // private methods
    // data members
    private boolean reserveCaseFlag_ = false;
}
