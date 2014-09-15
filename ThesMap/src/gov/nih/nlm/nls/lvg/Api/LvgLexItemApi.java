package gov.nih.nlm.nls.lvg.Api;
import java.util.*;
import java.sql.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.CmdLineSyntax.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Util.*;
import gov.nih.nlm.nls.lvg.Flows.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides an LVG API for users to setup flows process with Java
* LexItem class.  The input of this API are focus on LexItem(s). The output is 
* Vector<String> of Lvg ouputs.
*
* <p>All LVG API consists methods for three phases:
* <ul>
* <li><b>PreProcess:</b>
* <br>Taking care of preparation work for using Lvg transformations.  Such as
* initiating configuration data, declaring persistent files for trie, and
* establishing database connection.
* <li><b>Process:</b>
* <br> This is the core program for user's applications.  Users define and run
* their flow(s) in this process.
* <li><b>PostProcess:</b>
* <br>Methods in this phases are used to cleanly close database connection, 
* persistent files, etc..  
* </ul>
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @see  <a href="../../../../../../../designDoc/LifeCycle/deploy/LvgLexItem.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public class LvgLexItemApi extends LvgCmdApi
{
    // public constructor
    /**
    * Creates an LvgLexItemApi object and initiate related data (default).
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    */
    public LvgLexItemApi()
    {
        super();
    }
    /**
    * Creates an LvgLexItemApi object, initiate related data, using a command 
    * string.  This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    */
    public LvgLexItemApi(String optionStr)
    {
        super(optionStr);
    }
    /**
    * Creates an LvgLexItemApi object, initiate related data, using a command 
    * string.  This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param configFile the absolute path of the configuration file
    */
    public LvgLexItemApi(String optionStr, String configFile)
    {
        super(optionStr, configFile);
    }
    /**
    * Creates an LvgLexItemApi object and initiate related data with
    * properties to be overwritten in configuration.
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param properties  properties to be overwrite in configuration
    */
    public LvgLexItemApi(Hashtable<String, String> properties)
    {
        super(properties);
    }
    /**
    * Creates an LvgLexItemApi object, initiate related data, using a command 
    * string with properties to be overwritten in configuration.  
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param properties  properties to be overwrite in configuration
    */
    public LvgLexItemApi(String optionStr, Hashtable<String, String> properties)
    {
        super(optionStr, properties);
    }
    /**
    * Creates an LvgLexItemApi object, initiate related data, using a command 
    * string with properties to be overwritten in configuration.  
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param configFile the absolute path of the configuration file
    * @param properties  properties to be overwrite in configuration
    */
    public LvgLexItemApi(String optionStr, String configFile, 
        Hashtable<String, String> properties)
    {
        super(optionStr, configFile, properties);
    }
    /**
    * Performs flow mutation by processing the input term.
    *
    * @param  inLine  the input to be mutated.
    *
    * @return  Vector<lexItem> Lvg mutation results
    */
    public Vector<LexItem> MutateLexItem(String inLine) throws Exception
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // Process the mutation on the input term
        outs = ProcessLexItem(inLine);
        return outs;
    }
    /**
    * Performs flow mutation by processing the input.  The input is the
    * results from previous flow component.
    * The output LexItems from the result of a flow component needs to be
    * do some target to source conversion before being sent into next flow
    * component for mutation.  This method takes care of such convertion.
    *
    * @param  ins  Vector<LexItem> input (from flow compoment output)
    *
    * @return  Vector<LexItem> mutation result
    */
    public Vector<LexItem> ProcessLexItemsFromFCO(Vector<LexItem> ins) 
        throws SQLException, IOException
    {
        if(ins == null)
        {
            return null;
        }
        Vector<LexItem> results = new Vector<LexItem>();
        // go through all LexItems
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem in = ins.elementAt(i);
            LexItem tempIn = LexItem.TargetToSource(in);    // convert LexItem
            Vector<LexItem> out = ProcessLexItem(in);
            results.addAll(out);
        }
        return results;
    }
    /**
    * Performs flow mutation by processing the input.  
    * The input is Vector<LexItem>.
    *
    * @param  ins  Vector<lexItem>
    *
    * @return  Vector<LexItem> mutation result
    */
    public Vector<LexItem> ProcessLexItems(Vector<LexItem> ins) 
        throws SQLException, IOException
    {
        if(ins == null)
        {
            return null;
        }
        Vector<LexItem> results = new Vector<LexItem>();
        // go through all LexItems in the Vector
        for(int i = 0; i < ins.size(); i++)
        {
            LexItem in = ins.elementAt(i);
            Vector<LexItem> out = ProcessLexItem(in);
            results.addAll(out);
        }
        return results;
    }
    /**
    * Performs flow mutation by processing the input.  The input is an LexItem.
    *
    * @param  in  a LexItem for mutation
    *
    * @return  Vector<LexItem> mutation result
    */
    public Vector<LexItem> ProcessLexItem(LexItem in) 
        throws SQLException, IOException
    {
        Vector<LexItem> results = new Vector<LexItem>();    // result for return
        // go through all parallel flows 
        for(int flowNum = 0; flowNum < flowStrsList_.size(); flowNum++) 
        {
            flowStrs_ = flowStrsList_.elementAt(flowNum); 
            // temp ins & outs for each flow
            Vector<LexItem> ins = new Vector<LexItem>();     // input 
            Vector<LexItem> outs = new Vector<LexItem>();    // output results
            ins.addElement(in);                // first flow component
            // go through all flow compoments in a flow
            for(int flowComp = 0; flowComp < flowStrs_.size(); flowComp++)
            {
                // go through all LexItems generated from flow components 
                Vector<LexItem> cur = new Vector<LexItem>();
                for(int inLex = 0; inLex < ins.size(); inLex++)
                {
                    LexItem tempIn = ins.elementAt(inLex);
                    tempIn.SetFlowNumber(flowNum+1);  // set flow number
                    Vector<LexItem> tempOuts = ExecuteFlow(tempIn, 
                        flowStrs_.elementAt(flowComp));
                    cur.addAll(tempOuts);
                }
                // Update outs
                outs.removeAllElements();
                outs.addAll(cur);
                // convert results from one flow component into ins for next
                ins = LexItem.TargetsToSources(outs);
            }
            // put the outs from one flow component into results
            results.addAll(outs);
        }
        return results;
    }
    /**
    * Performs flow mutation by processing the a input from System in.  
    * The input is a term (string).  This method provide Lvg interface prompt.
    *
    * @return  mutation result (a vector of LexItems)
    */
    public Vector<LexItem> ProcessLineLexItem() throws SQLException, IOException
    {
        // check RunFlag
        if(runFlag_ == false)
        {
            return null;
        }
        // Display a prompt to the user
        if(promptFlag_ == true)
        {
            GetPrompt();    
        }
        // read line from System.in or a file
        String line = null;
        if(inReader_ == null)
        {
            BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));
            line = in.readLine();
        }
        else
        {
            line = inReader_.readLine();
        }
        // check if the input is a command for quiting
        if((line == null) || (quitStrList_.contains(line)))
        {
            return null;
        }
        // Process the line
        Vector<LexItem> outs = ProcessLexItem(line);
        return outs;
    }
    // private methods
    // Use String as input and return a vector of LexItem
    private Vector<LexItem> ProcessLexItem(String line) 
        throws SQLException, IOException
    {
        Vector<LexItem> results = new Vector<LexItem>();    // result for return
        // get the term from appropriate field
        String fs = GlobalBehavior.GetFieldSeparator();
        String inTerm = InputFilter.GetInputTerm(line, fs, termFieldNum_);
        long inCat = InputFilter.GetInputCategory(line, fs, catFieldNum_);
        long inInfl = InputFilter.GetInputInflection(line, fs, inflFieldNum_);
        LexItem in = new LexItem(inTerm, inCat, inInfl);
        // go through all parallel flows 
        for(int flowNum = 0; flowNum < flowStrsList_.size(); flowNum++) 
        {
            flowStrs_ = flowStrsList_.elementAt(flowNum); 
            // temp ins & outs for each flow
            Vector<LexItem> ins = new Vector<LexItem>();     // input 
            Vector<LexItem> outs = new Vector<LexItem>();    // output results
            ins.addElement(in);                // first flow component
            // go through all flow compoments in a flow
            for(int flowComp = 0; flowComp < flowStrs_.size(); flowComp++)
            {
                // go through all LexItems generated from flow components 
                Vector<LexItem> cur = new Vector<LexItem>();
                for(int inLex = 0; inLex < ins.size(); inLex++)
                {
                    LexItem tempIn = ins.elementAt(inLex);
                    tempIn.SetFlowNumber(flowNum+1);  // set flow number
                    Vector<LexItem> tempOuts = ExecuteFlow(tempIn, 
                        flowStrs_.elementAt(flowComp));
                    cur.addAll(tempOuts);
                }
                // Update outs
                outs.removeAllElements();
                outs.addAll(cur);
                // convert results from one flow component into ins for next
                ins = LexItem.TargetsToSources(outs);
            }
            // put the outs from one flow component into results
            results.addAll(outs);
        }
        return results;
    }
}
