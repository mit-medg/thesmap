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
* This class provides an LVG API for users to setup flows by defining a 
* Lvg command.  Two methods, ProcessLine( ) and Mutate( ) are used for
* flow mutation for the case of using interface prompt or not.  The input of 
* this API is a term (string).  The output is a Vector<String> of Lvg ouputs .
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
* <li>SCR-05, chlu, 06-08-12, fix config file error message
* <li>SCR-15, chlu, 07-23-12, add derivatin type: prefixD, suffixD, zeroD
* <li>SCR-20, chlu, 07-23-12, add derivtion negation options
* </ul>
*
* @author NLM NLS Development Team
*
* @see <a href="../../../../../../../designDoc/LifeCycle/deploy/LvgCmdApi.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public class LvgCmdApi extends SystemOption
{
    // public constructor
    /**
    * Creates an LvgCmdApi object and initiate related data (default).
    * This constructor is consider as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection
    * after using this object
    */
    public LvgCmdApi()
    {
        Init();
    }
    /**
    * Creates an LvgApi object, initiate related data, using a command string. 
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    */
    public LvgCmdApi(String optionStr)
    {
        option_ = new Option(optionStr);
        Init();
    }
    /**
    * Creates an LvgApi object, initiate related data, using a command string
    * and the path of configuration file. 
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param configFile the absolute path of the configuration file
    */
    public LvgCmdApi(String optionStr, String configFile)
    {
        option_ = new Option(optionStr);
        configFile_ = configFile;
        Init();
    }
    /**
    * Creates an LvgCmdApi object and initiate related data with
    * properties to be overwritten in configuration.
    * This constructor is consider as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection
    * after using this object
    *
    * @param properties  properties to be overwrite in configuration
    */
    public LvgCmdApi(Hashtable<String, String> properties)
    {
        properties_ = properties;
        Init();
    }
    /**
    * Creates an LvgApi object, initiate related data, using a command string
    * with properties to be overwritten in configuration.
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param properties  properties to be overwrite in configuration
    */
    public LvgCmdApi(String optionStr, Hashtable<String, String> properties)
    {
        option_ = new Option(optionStr);
        properties_ = properties;
        Init();
    }
    /**
    * Creates an LvgApi object, initiate related data, using a command string
    * and the path of configuration file with properties to be overwritten 
    * in configuration.  
    * This constructor is considered as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param optionStr  the initial lvg option string
    * @param configFile the absolute path of the configuration file
    * @param properties  properties to be overwrite in configuration
    */
    public LvgCmdApi(String optionStr, String configFile, 
        Hashtable<String, String> properties)
    {
        option_ = new Option(optionStr);
        configFile_ = configFile;
        properties_ = properties;
        Init();
    }
    // public methods
    /**
    * Set the prompt string.  This method allows users to set their 
    * customerized prompt string for using Lvg prompt interface.
    *
    * @param   promptStr  the customerized prompt string
    */
    public void SetPromptStr(String promptStr)
    {
        promptStr_ = promptStr;
    }
    /**
    * Set a list for quiting the program while using Lvg prompt interface.
    *
    * @param   quitStrList  Vector<String> quiting program
    */
    public void SetQuitStrList(Vector<String> quitStrList)
    {
        quitStrList_ = new Vector<String>(quitStrList);
    }
    /**
    * Check if the input command is legal.
    *
    * @return  true or false if the input command is legal or illegal
    */
    public boolean IsLegalOption()
    {
        boolean isLegalOption = 
            ((SystemOption.CheckSyntax(option_, GetOption(), false, true))
            && (CheckInflectionByCatInfl()));
        return isLegalOption;
    }
    /**
    * Set the Lvg command for flows.
    *
    * @param   optionStr  Lvg command for flows
    */
    public void SetOption(String optionStr)
    {
        option_ = new Option(optionStr);
        // check input command, and open database connection and tries
        PreProcess();
        // Init Database and Persistant Trie
        InitDbAndTrie();
    }
    /**
    * Get Lvg Output Option.
    *
    * @return   lvg output option object
    */
    public LvgOutputOption GetLvgOutputOption()
    {
        return lvgOutputOption_;
    }
    /**
    * A method to get the configuration object
    *
    * @return  Configuration
    */
    public Configuration GetConfiguration()
    {
        return conf_;
    }
    /**
    * Get the Db connection
    *
    * @return   Connection  lvg database connection
    */
    public Connection GetConnection()
    {
        return conn_;
    }
    /**
    * Close Lvg database connection and persistent tries.  This methods must
    * be called before exiting LvgCmdApi.  It is a method from post process.
    */
    public void CleanUp()
    {
        try
        {
            Close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
    * Performs flow mutation by processing the input line (term).  This method
    * provides Lvg command line interface prompt, read input term from system 
    * input or from a file.  The result of this method include output filter.
    *
    * @return  true or false if the input line is not or is to quit the program
    */
    public boolean ProcessLine() throws SQLException, IOException
    {
        // check RunFlag
        if(runFlag_ == false)
        {
            return false;
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
            inReader_ = new BufferedReader(new InputStreamReader(System.in,
                "UTF-8"));
        }
        line = inReader_.readLine();
        // check if the input is a command for quiting
        if((line == null) || (quitStrList_.contains(line)))
        {
            return false;
        }
        // Process the line
        Process(line, false);
        return true;
    }
    /**
    * Return the boolean run flag 
    *
    * @return return true if valid api or false if otherwise (wrong config)
    */
    public boolean GetRunFlag()
    {
        return runFlag_;
    }
    /**
    * Print out the Lvg help menu.
    */
    public void PrintLvgHelp()
    {
        LvgHelp.LvgHelp(outWriter_, fileOutput_);
    }
    /**
    * Performs flow mutation by processing the input term.
    * The result of this method also include go through all output filter.
    *
    * @param  inTerm  the term to be mutated.
    */
    public void Mutate(String inTerm) throws Exception
    {
        // Process the mutation on the input term
        Process(inTerm, false);
    }
    /**
    * Performs flow mutation by processing the input term and return the result
    * as a string.
    * The result of this method also include go through all output filter.
    *
    * @param  inTerm  the term to be mutated.
    *
    * @return the Lvg output result as a string 
    */
    public String MutateToString(String inTerm) throws Exception
    {
        Out.ResetOutString();
        // Process the mutation on the input term
        Process(inTerm, true);
        return Out.GetOutString();
    }
    /**
    * Get Lvg Flow Specific Options
    *
    * @return  the object of flow specific options
    */
    public LvgFlowSpecificOption GetFlowSpecificOptions()
    {
        return lvgFlowSpecificOption_; 
    }
    /**
    * Set minimum Trie Length
    *
    * @param  minTermLength   minimum trie term length
    */
    public void SetMinTermLength(int minTermLength)
    {
        if(ramTrieI_ != null)
        {
            ramTrieI_.SetMinTermLength(minTermLength);
        }
        if(ramTrieD_ != null)
        {
            ramTrieD_.SetMinTermLength(minTermLength);
        }
    }
    // protected methods
    /**
    * Execute Lvg command for a specified option item in a given system option.
    * This function needs to be modified if adding a new flow option.
    *
    * @param  optionItem  the option to be executed
    * @param  systemOption  the system option that the option item will be 
    * run on
    */
    protected void ExecuteCommand(OptionItem optionItem, Option systemOption)
    {
        OptionItem nameItem =
            OptionUtility.GetItemByName(optionItem, systemOption, false);
        Vector<OptionItem> systemItems = systemOption.GetOptionItems();
        if(CheckOption(nameItem, "-C:INT") == true)
        {
            lvgOutputOption_.SetCaseFlag(
                Integer.parseInt(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-cf:INT") == true)
        {
            catFieldNum_ = Integer.parseInt(nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-if:INT") == true)
        {
            inflFieldNum_ = Integer.parseInt(nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-ccgi") == true)
        {
            lvgOutputOption_.SetMarkEndFlag(true);
        }
        else if(CheckOption(nameItem, "-ci") == true)
        {
            try
            {
                // get config file from environment variable
                boolean useClassPath = false;
                String configFile = configFile_;
                if(configFile == null)
                {
                    useClassPath = true;
                    configFile = "data.config.lvg";
                }
                Configuration conf = 
                    new Configuration(configFile, useClassPath);
                if(properties_ != null)
                {
                    conf.OverwriteProperties(properties_);
                }
                Out.Println(outWriter_, conf.GetInformation(), fileOutput_, 
                    false);
            }
            catch (IOException e) { }
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-CR:o") == true)
        {
            lvgOutputOption_.SetCombineRule(CombineRecords.BY_TERM);
        }
        else if(CheckOption(nameItem, "-CR:oc") == true)
        {
            lvgOutputOption_.SetCombineRule(CombineRecords.BY_CATEGORY);
        }
        else if(CheckOption(nameItem, "-CR:oe") == true)
        {
            lvgOutputOption_.SetCombineRule(CombineRecords.BY_EUI);
        }
        else if(CheckOption(nameItem, "-CR:oi") == true)
        {
            lvgOutputOption_.SetCombineRule(CombineRecords.BY_INFLECTION);
        }
        else if(CheckOption(nameItem, "-DC:LONG") == true)
        {
            lvgOutputOption_.SetOutCategory(
                Long.parseLong(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-DI:LONG") == true)
        {
            lvgOutputOption_.SetOutInflection(
                Long.parseLong(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-EC:LONG") == true)
        {
            lvgOutputOption_.SetExcludeCategory(
                Long.parseLong(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-EI:LONG") == true)
        {
            lvgOutputOption_.SetExcludeInflection(
                Long.parseLong(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-d") == true)
        {
            detailsFlag_ = true;
        }
        else if(CheckOption(nameItem, "-h") == true)
        {
            LvgHelp.LvgHelp(outWriter_, fileOutput_);
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-hs") == true)
        {
            systemOption.PrintOptionHierachy();        //not UTF-8
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-F:INT") == true)
        {
            Integer fieldNum = new Integer(nameItem.GetOptionArgument());
            Vector<Integer> outputFieldList 
                = lvgOutputOption_.GetOutputFieldList();
            outputFieldList.addElement(fieldNum);
            lvgOutputOption_.SetOutputFieldList(outputFieldList);
        }
        else if(CheckOption(nameItem, "-F:h") == true)
        {
            LvgHelp.OutputFieldHelp(outWriter_, fileOutput_);
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-f:0") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_NEC_NOS));
        }
        else if(CheckOption(nameItem, "-f:A") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.ACRONYMS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:An") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.ANTINORM));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:a") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.EXPANSIONS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:B") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.UNINFLECT_WORDS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Bn") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NORM_UNINFLECT_WORDS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:b") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.UNINFLECT_TERM));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:C") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.CANONICALIZE));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Ct") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.CITATION));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:c") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.TOKENIZE));
        }
        else if(CheckOption(nameItem, "-f:ca") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.TOKENIZE_KEEP_ALL));
        }
        else if(CheckOption(nameItem, "-f:ch") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.TOKENIZE_NO_HYPHENS));
        }
        else if(CheckOption(nameItem, "-f:d") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.DERIVATION));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:dc~LONG") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.DERIVATION_BY_CATEGORY));
            dbFlag_ = true;
            derivationCatList_.addElement(nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-f:e") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.BASE_SPELLING_VARIANTS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:f") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FILTER));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:fa") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FILTER_ACRONYM));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:fp") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FILTER_PROPER_NOUN));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:E") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RETRIEVE_EUI));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:G") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FRUITFUL_VARIANTS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Ge") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FRUITFUL_ENHANCED));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Gn") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FRUITFUL_VARIANTS_LEX));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:g") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.REMOVE_GENITIVE));
        }
        else if(CheckOption(nameItem, "-f:h") == true)
        {
            LvgHelp.FlowHelp(outWriter_, fileOutput_);
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-f:i") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.INFLECTION));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:is") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.INFLECTION_SIMPLE));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:ici~STR+STR") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.INFLECTION_BY_CAT_INFL));
            dbFlag_ = true;
            Vector<String> inList = 
                OptionItem.GetArgumentList(nameItem.GetOptionArgument());
            inflectionCatList_.addElement(inList.elementAt(0));
            inflectionInflList_.addElement(inList.elementAt(1));
            curInflectionByCatInflNum_++;
        }
        else if(CheckOption(nameItem, "-f:L") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RETRIEVE_CAT_INFL));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Ln") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RETRIEVE_CAT_INFL_DB));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:Lp") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RETRIEVE_CAT_INFL_BEGIN));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:l") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.LOWER_CASE));
        }
        else if(CheckOption(nameItem, "-f:m") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.METAPHONE));
        }
        else if(CheckOption(nameItem, "-f:N") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NORMALIZE));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:N3") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.LUI_NORMALIZE));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:n") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NO_OPERATION));
        }
        else if(CheckOption(nameItem, "-f:nom") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NOMINALIZATION));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:o") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.REPLACE_PUNCTUATION_WITH_SPACE));
        }
        else if(CheckOption(nameItem, "-f:P") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_PUNCTUATION_ENHANCED));
        }
        else if(CheckOption(nameItem, "-f:p") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_PUNCTUATION));
        }
        else if(CheckOption(nameItem, "-f:q") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_DIACRITICS));
        }
        else if(CheckOption(nameItem, "-f:q0") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.MAP_SYMBOL_TO_ASCII));
        }
        else if(CheckOption(nameItem, "-f:q1") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.MAP_UNICODE_TO_ASCII));
        }
        else if(CheckOption(nameItem, "-f:q2") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.SPLIT_LIGATURES));
        }
        else if(CheckOption(nameItem, "-f:q3") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.GET_UNICODE_NAME));
        }
        else if(CheckOption(nameItem, "-f:q4") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.GET_UNICODE_SYNONYM));
        }
        else if(CheckOption(nameItem, "-f:q5") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NORM_UNICODE));
        }
        else if(CheckOption(nameItem, "-f:q6") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.NORM_UNICODE_WITH_SYNONYM));
        }
        else if(CheckOption(nameItem, "-f:q7") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.UNICODE_CORE_NORM));
        }
        else if(CheckOption(nameItem, "-f:q8") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_MAP_UNICODE));
        }
        else if(CheckOption(nameItem, "-f:R") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RECURSIVE_DERIVATIONS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:r") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.RECURSIVE_SYNONYMS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:rs") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.REMOVE_S));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:S") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.SYNTACTIC_UNINVERT));
        }
        else if(CheckOption(nameItem, "-f:Si") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.SIMPLE_INFLECTIONS));
        }
        else if(CheckOption(nameItem, "-f:s") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(
                Flow.GENERATE_SPELLING_VARIANTS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:T") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_AMBIGUITY_TAGS));
        }
        else if(CheckOption(nameItem, "-f:t") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.STRIP_STOP_WORDS));
        }
        else if(CheckOption(nameItem, "-f:U") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.CONVERT_OUTPUT));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:u") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.UNINVERT));
        }
        else if(CheckOption(nameItem, "-f:v") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.FRUITFUL_VARIANTS_DB));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-f:w") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.SORT_BY_WORD_ORDER));
        }
        else if(CheckOption(nameItem, "-f:ws~INT") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.WORD_SIZE));
            wordSize_ = Integer.parseInt(nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-f:y") == true)
        {
            flowStrs_.addElement(Flow.GetBitName(Flow.SYNONYMS));
            dbFlag_ = true;
        }
        else if(CheckOption(nameItem, "-i:STR") == true)
        {
            String inFile = nameItem.GetOptionArgument();
            if(inFile != null)
            {
                try
                {
                    inReader_ = new BufferedReader(new InputStreamReader(
                        new FileInputStream(inFile), "UTF-8"));
                }
                catch (IOException e)
                {
                    runFlag_ = false;
                    System.err.println(
                        "**Error: problem of opening/reading file " + inFile);
                }
            }
        }
        else if(CheckOption(nameItem, "-kd:INT") == true)
        {
            lvgFlowSpecificOption_.SetDerivationFilter(
                Integer.parseInt(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-kdn:STR") == true)
        {
            lvgFlowSpecificOption_.SetDerivationNegation(
                nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-kdt:STR") == true)
        {
            lvgFlowSpecificOption_.SetDerivationType(
                nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-ki:INT") == true)
        {
            lvgFlowSpecificOption_.SetInflectionFilter( 
                Integer.parseInt(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-m") == true)
        {
            mutateFlag_ = true;
        }
        else if(CheckOption(nameItem, "-n") == true)
        {
            lvgOutputOption_.SetNoOutputFlag(true);
        }
        else if(CheckOption(nameItem, "-o:STR") == true)
        {
            String outFile = nameItem.GetOptionArgument();
            if(outFile != null)
            {
                try
                {
                    outWriter_ = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(outFile), "UTF-8"));
                    fileOutput_ = true;
                }
                catch (IOException e)
                {
                    runFlag_ = false;
                    System.err.println(
                        "**Error: problem of opening/writing file " + outFile);
                }
            }
        }
        else if(CheckOption(nameItem, "-p") == true)
        {
            promptFlag_ = true;
        }
        else if(CheckOption(nameItem, "-R:INT") == true)
        {
            lvgOutputOption_.SetOutRecordNum(
                Integer.parseInt(nameItem.GetOptionArgument()));
        }
        else if(CheckOption(nameItem, "-SC") == true)
        {
            lvgOutputOption_.SetShowCategoryStrFlag(true);
        }
        else if(CheckOption(nameItem, "-SI") == true)
        {
            lvgOutputOption_.SetShowInflectionStrFlag(true);
        }
        else if(CheckOption(nameItem, "-St:o") == true)
        {
            lvgOutputOption_.SetSortFlag(LexItemComparator.TERM);
        }
        else if(CheckOption(nameItem, "-St:oc") == true)
        {
            lvgOutputOption_.SetSortFlag(LexItemComparator.TERM_CAT);
        }
        else if(CheckOption(nameItem, "-St:oci") == true)
        {
            lvgOutputOption_.SetSortFlag(LexItemComparator.TERM_CAT_INFL);
        }
        else if(CheckOption(nameItem, "-s:STR") == true)
        {
            String separator = nameItem.GetOptionArgument();
            // if sub by tab
            if(separator.equals("\\t"))
            {
                separator = new Character((char)(9)).toString();    // tab
            }
            GlobalBehavior.SetFieldSeparator(separator);
        }
        else if(CheckOption(nameItem, "-t:INT") == true)
        {
            termFieldNum_ = Integer.parseInt(nameItem.GetOptionArgument());
        }
        else if(CheckOption(nameItem, "-ti") == true)
        {
            lvgOutputOption_.SetFilterInputFlag(true);
        }
        else if(CheckOption(nameItem, "-v") == true)
        {
            try
            {
                String releaseStr = "lvg." + GlobalBehavior.YEAR;
                Out.Println(outWriter_, releaseStr, fileOutput_, false);
            }
            catch (IOException e) { }
            runFlag_ = false;
        }
        else if(CheckOption(nameItem, "-x:STR") == true)
        {
            configFile_ = nameItem.GetOptionArgument();
        }
    }
    /**
    * Define the Lvg system option by defining a string.
    * This function needs to be modified if adding a new flow option.
    */
    protected void DefineFlag()
    {
        // define all option flags & arguments by giving a option string
        String flagStr = "-cf:INT -C:INT -ccgi -ci -CR:o:oc:oe:oi -DC:LONG -DI:LONG -d -EC:LONG -EI:LONG -F:INT:h -f:0:A:An:a:B:Bn:b:C:Ct:c:ca:ch:d:dc~LONG:e:E:f:fa:fp:G::Ge:Gn:g:h:i:is:ici~STR+STR:L:Ln:Lp:l:m:N:N3:n:nom:o:P:p:q:q0:q1:q2:q3:q4:q5:q6:q7:q8:R:r:rs:S:Si:s:T:t:U:u:v:w:ws~INT:y -h -hs -i:STR -if:INT -kd:INT -kdn:STR -kdt:STR -ki:INT -m -n -o:STR -p -R:INT -SC -SI -St:o:oc:oci -s:STR -t:INT -ti -v -x:STR";
        
        // init the system option
        systemOption_ = new Option(flagStr);
        // Add the full name for flags
        systemOption_.SetFlagFullName("-cf:INT", "Input_Category_Field");
        systemOption_.SetFlagFullName("-C:INT", "Case_Setting");
        systemOption_.SetFlagFullName("-ccgi", "Mark_The_End");
        systemOption_.SetFlagFullName("-ci", "Show_Config_Info");
        systemOption_.SetFlagFullName("-CR:o", "Combine_By_Output_Term");
        systemOption_.SetFlagFullName("-CR:oc", "Combine_By_Category");
        systemOption_.SetFlagFullName("-CR:oe", "Combine_By_Eui");
        systemOption_.SetFlagFullName("-CR:oi", "Combine_By_Inflection");
        systemOption_.SetFlagFullName("-DC:LONG", "Specify_Categories");
        systemOption_.SetFlagFullName("-DI:LONG", "Specify_Inflections");
        systemOption_.SetFlagFullName("-d", "Detail_Operations");
        systemOption_.SetFlagFullName("-EC:LONG", "Exclude_Categories");
        systemOption_.SetFlagFullName("-EI:LONG", "Exclude_Inflections");
        systemOption_.SetFlagFullName("-F:INT", "Output_Field");
        systemOption_.SetFlagFullName("-F:h", "Output_Field_Menu");
        systemOption_.SetFlagFullName("-f", "Flow");
        systemOption_.SetFlagFullName("-f:0", "Strip_NEC_NOS");
        systemOption_.SetFlagFullName("-f:A", "Acronyms");
        systemOption_.SetFlagFullName("-f:An", "AntiNorm");
        systemOption_.SetFlagFullName("-f:a", "Expansions");
        systemOption_.SetFlagFullName("-f:B", "Uninflect_Words");
        systemOption_.SetFlagFullName("-f:Bn", "Normalize_Uninflect_Words");
        systemOption_.SetFlagFullName("-f:b", "Uninflect_Term");
        systemOption_.SetFlagFullName("-f:C", "Canonicalize");
        systemOption_.SetFlagFullName("-f:Ct", "Citation");
        systemOption_.SetFlagFullName("-f:c", "Tokenize");
        systemOption_.SetFlagFullName("-f:ca", "Tokenize_Keep_All");
        systemOption_.SetFlagFullName("-f:ch", "Tokenize_No_Hyphens");
        systemOption_.SetFlagFullName("-f:d", "Derivation");
        systemOption_.SetFlagFullName("-f:dc~LONG", "Derivation_By_Category");
        systemOption_.SetFlagFullName("-f:e", "Base_From_Spelling_Variants");
        systemOption_.SetFlagFullName("-f:E", "Retrieve_Eui");
        systemOption_.SetFlagFullName("-f:f", "Filter_Output");
        systemOption_.SetFlagFullName("-f:fa", "Filter_Out_Acronym");
        systemOption_.SetFlagFullName("-f:fp", "Filter_Out_ProperNouns");
        systemOption_.SetFlagFullName("-f:G", "Fruitful_Variants");
        systemOption_.SetFlagFullName("-f:Ge", "Fruitful_Variants_Enhanced");
        systemOption_.SetFlagFullName("-f:Gn", "Fruitful_Variants_Lex");
        systemOption_.SetFlagFullName("-f:g", "Remove_Genitive");
        systemOption_.SetFlagFullName("-f:h", "Flow_Help_Menu");
        systemOption_.SetFlagFullName("-f:i", "Inflection");
        systemOption_.SetFlagFullName("-f:is", "Inflection_Simple");
        systemOption_.SetFlagFullName("-f:ici~STR+STR", 
            "Inflection_By_Cat_Infl");
        systemOption_.SetFlagFullName("-f:L", "Retrieve_Cat_Infl");
        systemOption_.SetFlagFullName("-f:Ln", "Retrieve_Cat_Infl_Db");
        systemOption_.SetFlagFullName("-f:Lp", "Retrieve_Cat_Infl_Begin");
        systemOption_.SetFlagFullName("-f:l", "LowerCase");
        systemOption_.SetFlagFullName("-f:m", "Metaphone");
        systemOption_.SetFlagFullName("-f:N", "Normalize");
        systemOption_.SetFlagFullName("-f:N3", "LuiNormalize");
        systemOption_.SetFlagFullName("-f:n", "No_Operation");
        systemOption_.SetFlagFullName("-f:nom", "Retrieve_Nominalizations");
        systemOption_.SetFlagFullName("-f:o", "Replace_Punctuation_With_Space");
        systemOption_.SetFlagFullName("-f:P", "Strip_Punctuation_Enhanced");
        systemOption_.SetFlagFullName("-f:p", "Strip_Punctuation");
        systemOption_.SetFlagFullName("-f:q", "Strip_Diacritics");
        systemOption_.SetFlagFullName("-f:q0", "Map_Symbol_To_ASCII");
        systemOption_.SetFlagFullName("-f:q1", "Map_Unicode_To_ASCII");
        systemOption_.SetFlagFullName("-f:q2", "Split_Ligatures");
        systemOption_.SetFlagFullName("-f:q3", "Get_Unicode_Name");
        systemOption_.SetFlagFullName("-f:q4", "Get_Unicode_Synonym");
        systemOption_.SetFlagFullName("-f:q5", "Norma_Unicode");
        systemOption_.SetFlagFullName("-f:q6", "Norm_Unicode_With_Synonym");
        systemOption_.SetFlagFullName("-f:q7", "Unicode_Core_Norm");
        systemOption_.SetFlagFullName("-f:q8", "Strip_Map_Unicode");
        systemOption_.SetFlagFullName("-f:R", "Recursive_Derivations");
        systemOption_.SetFlagFullName("-f:r", "Recursive_Synonyms");
        systemOption_.SetFlagFullName("-f:rs", "Remove_(s)_(es)_(ies)");
        systemOption_.SetFlagFullName("-f:S", "Syntactic_Uninvert");
        systemOption_.SetFlagFullName("-f:Si", "Simple_Inflections");
        systemOption_.SetFlagFullName("-f:s", "Spelling_Variants");
        systemOption_.SetFlagFullName("-f:T", "Strip_Ambiguity_Tags");
        systemOption_.SetFlagFullName("-f:t", "Strip_Stop_Words");
        systemOption_.SetFlagFullName("-f:U", "Convert_Output");
        systemOption_.SetFlagFullName("-f:u", "Uninvert");
        systemOption_.SetFlagFullName("-f:v", "Fruitful_Variants_Db");
        systemOption_.SetFlagFullName("-f:w", "Sort_By_Word_Order");
        systemOption_.SetFlagFullName("-f:ws~INT", "Word_Size_Filter");
        systemOption_.SetFlagFullName("-f:y", "Synonyms");
        systemOption_.SetFlagFullName("-h", "Help");
        systemOption_.SetFlagFullName("-hs", "Hierarchy_Struture");
        systemOption_.SetFlagFullName("-i:STR", "Input_File");
        systemOption_.SetFlagFullName("-if:INT", "Input_Inflection_Field");
        systemOption_.SetFlagFullName("-kd:INT", "Restrict_Derivations");
        systemOption_.SetFlagFullName("-kdn:STR", "Derivation_Negative");
        systemOption_.SetFlagFullName("-kdt:STR", "Derivation_Types");
        systemOption_.SetFlagFullName("-ki:INT", "Restrict_Inflections");
        systemOption_.SetFlagFullName("-m", "Mutation_Information");
        systemOption_.SetFlagFullName("-n", "No_Output");
        systemOption_.SetFlagFullName("-o:STR", "Output_file");
        systemOption_.SetFlagFullName("-p", "Show_Prompt");
        systemOption_.SetFlagFullName("-R:INT", "Restrict_Out_Number");
        systemOption_.SetFlagFullName("-SC", "Show_Category_String");
        systemOption_.SetFlagFullName("-SI", "Show_Inflection_String");
        systemOption_.SetFlagFullName("-St:o", "Sort_By_Term");
        systemOption_.SetFlagFullName("-St:oc", "Sort_By_Term_Cat");
        systemOption_.SetFlagFullName("-St:oci", "Sort_By_Term_Cat_Infl");
        systemOption_.SetFlagFullName("-s:STR", "Field_Separator");
        systemOption_.SetFlagFullName("-t:INT", "Term_Field");
        systemOption_.SetFlagFullName("-ti", "Filter_Input_Term");
        systemOption_.SetFlagFullName("-v", "Version");
        systemOption_.SetFlagFullName("-x:STR", "Load_Configuration_file");
    }
    /**
    * Get the Lvg interface prompt and print it out to system output.
    */
    protected void GetPrompt() throws IOException
    {
        Out.Println(outWriter_, promptStr_, fileOutput_, false);
    }
    /**
    * Execute a specified Lvg flow transformation for a given LexItem.
    *
    * <p>Notes This function needs to be modified if adding a new flow option.
    *
    * @param  in   the LexItem to be transformed
    * @param  flowStr  the specified flow in a string format
    *
    * @return Vector<LexItem> - output
    *
    */
    protected Vector<LexItem> ExecuteFlow(LexItem in, String flowStr)
        throws SQLException
    {
        long flowNum = Flow.Enumerate(flowStr); 
        Vector<LexItem> outs = new Vector<LexItem>();
        // reset vars 
        curDerivationCatNum_ = 0;
        curInflectionByCatInflCount_ = 0;
        curInflectionByCatInflNum_ = 0;
        if(flowNum == Flow.GetBitValue(Flow.LOWER_CASE))    
        {
            outs = ToLowerCase.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_STOP_WORDS))
        {
            outs = ToStripStopWords.Mutate(in, stopWords_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.REMOVE_GENITIVE))
        {
            outs = ToRemoveGenitive.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.REPLACE_PUNCTUATION_WITH_SPACE))
        {
            outs = ToReplacePunctuationWithSpace.Mutate(in, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_PUNCTUATION))
        {
            outs = ToStripPunctuation.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_PUNCTUATION_ENHANCED))
        {
            outs = ToStripPunctuationEnhanced.Mutate(in, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.SORT_BY_WORD_ORDER))
        {
            outs = ToSortWordsByOrder.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_NEC_NOS))
        {
            outs = ToStripNecNos.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NO_OPERATION))
        {
            outs = ToNoOperation.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.TOKENIZE))
        {
            outs = ToTokenize.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.TOKENIZE_NO_HYPHENS))
        {
            outs = ToTokenizeNoHyphens.Mutate(in, detailsFlag_, mutateFlag_);
        }
        // use DB & trie
        else if(flowNum == Flow.GetBitValue(Flow.UNINFLECT_TERM))
        {
            outs = ToUninflectTerm.Mutate(in, conn_, ramTrieI_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.INFLECTION))
        {
            outs = ToInflection.Mutate(in, conn_, ramTrieI_, 
                lvgFlowSpecificOption_.GetInflectionFilter(), 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.UNINFLECT_WORDS))
        {
            outs = ToUninflectWords.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxPermuteTermNum(), 
                conn_, ramTrieI_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NORMALIZE))
        {
            outs = ToNormalize.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxPermuteTermNum(), 
                stopWords_, conn_, ramTrieI_, 
                symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
                nonStripMap_, removeSTree_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.CANONICALIZE))
        {
            outs = ToCanonicalize.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.LUI_NORMALIZE))
        {
            outs = ToLuiNormalize.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxPermuteTermNum(), 
                stopWords_, conn_, ramTrieI_, 
                symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
                nonStripMap_, removeSTree_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.GENERATE_SPELLING_VARIANTS))
        {
            outs = ToSpellingVariants.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.ACRONYMS))
        {
            outs = ToAcronyms.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.EXPANSIONS))
        {
            outs = ToExpansions.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.DERIVATION))
        {
            outs = ToDerivation.Mutate(in, conn_, ramTrieD_, 
                lvgFlowSpecificOption_.GetDerivationFilter(), 
                lvgFlowSpecificOption_.GetDerivationType(),
                lvgFlowSpecificOption_.GetDerivationNegation(), 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.DERIVATION_BY_CATEGORY))
        {
            long category = Long.parseLong(
                derivationCatList_.elementAt(curDerivationCatNum_));
            curDerivationCatNum_++;
            outs = ToDerivationByCategory.Mutate(in, conn_, ramTrieD_, 
                lvgFlowSpecificOption_.GetDerivationFilter(), 
                lvgFlowSpecificOption_.GetDerivationType(),
                lvgFlowSpecificOption_.GetDerivationNegation(), 
                category, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.INFLECTION_BY_CAT_INFL))
        {
            long category = Long.parseLong(
                inflectionCatList_.elementAt(curInflectionByCatInflCount_));
            long inflection = Long.parseLong(
                inflectionInflList_.elementAt(curInflectionByCatInflCount_));
            curInflectionByCatInflCount_++;
            outs = ToInflectionByCatInfl.Mutate(in, conn_, ramTrieI_, 
                lvgFlowSpecificOption_.GetInflectionFilter(), 
                category, inflection, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.BASE_SPELLING_VARIANTS))
        {
            outs = ToBaseSpellingVariants.Mutate(in, conn_, ramTrieI_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RETRIEVE_EUI))
        {
            outs = ToRetrieveEui.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RETRIEVE_CAT_INFL))
        {
            outs = ToRetrieveCatInfl.Mutate(in, conn_, ramTrieI_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RETRIEVE_CAT_INFL_DB))
        {
            outs = ToRetrieveCatInflDb.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RETRIEVE_CAT_INFL_BEGIN))
        {
            outs = ToRetrieveCatInflBegin.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.SYNONYMS))
        {
            outs = ToSynonyms.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FILTER))
        {
            outs = ToFilter.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FILTER_PROPER_NOUN))
        {
            outs = ToFilterProperNoun.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FILTER_ACRONYM))
        {
            outs = ToFilterAcronym.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_AMBIGUITY_TAGS))
        {
            outs = ToStripAmbiguityTags.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.UNINVERT))
        {
            outs = ToUninvert.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.CONVERT_OUTPUT))
        {
            outs = ToConvertOutput.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RECURSIVE_SYNONYMS))
        {
            outs = ToRecursiveSynonyms.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_, false);
        }
        else if(flowNum == Flow.GetBitValue(Flow.RECURSIVE_DERIVATIONS))
        {
            outs = ToRecursiveDerivations.Mutate(in, conn_, ramTrieD_, 
                lvgFlowSpecificOption_.GetDerivationFilter(), 
                lvgFlowSpecificOption_.GetDerivationType(),
                lvgFlowSpecificOption_.GetDerivationNegation(), 
                detailsFlag_, mutateFlag_, false);
        }
        else if(flowNum == Flow.GetBitValue(Flow.CITATION))
        {
            outs = ToCitation.Mutate(in, conn_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NORM_UNINFLECT_WORDS))
        {
            outs = ToNormUninflectWords.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxPermuteTermNum(),
                conn_, ramTrieI_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_DIACRITICS))
        {
            outs = ToStripDiacritics.Mutate(in, diacriticMap_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.METAPHONE))
        {
            outs = ToMetaphone.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxMetaphoneCodeLength(), 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FRUITFUL_VARIANTS))
        {
            outs = ToFruitfulVariants.Mutate(in, conn_, ramTrieI_, ramTrieD_,
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.TOKENIZE_KEEP_ALL))
        {
            outs = ToTokenizeKeepAll.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.SYNTACTIC_UNINVERT))
        {
            outs = ToSyntacticUninvert.Mutate(in, nonInfoWords_, 
                conjunctionWords_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FRUITFUL_VARIANTS_LEX))
        {
            outs = ToFruitfulVariantsLex.Mutate(in, conn_, ramTrieI_, ramTrieD_,
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FRUITFUL_VARIANTS_DB))
        {
            outs = ToFruitfulVariantsDb.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.ANTINORM))
        {
            outs = ToAntiNorm.Mutate(in, 
                lvgFlowSpecificOption_.GetMaxPermuteTermNum(), 
                stopWords_, conn_, ramTrieI_, symbolMap_, unicodeMap_,
                ligatureMap_, diacriticMap_, nonStripMap_,
                removeSTree_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.WORD_SIZE))
        {
            outs = ToWordSize.Mutate(in, wordSize_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.FRUITFUL_ENHANCED))
        {
            outs = ToFruitfulEnhanced.Mutate(in, conn_, ramTrieI_, ramTrieD_,
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.SIMPLE_INFLECTIONS))
        {
            outs = ToSimpleInflections.Mutate(in, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.INFLECTION_SIMPLE))
        {
            outs = ToInflectionSimple.Mutate(in, conn_, ramTrieI_, 
                lvgFlowSpecificOption_.GetInflectionFilter(), 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.SPLIT_LIGATURES))
        {
            outs = ToSplitLigatures.Mutate(in, ligatureMap_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.GET_UNICODE_NAME))
        {
            outs = ToGetUnicodeNames.Mutate(in, startTag_, endTag_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.GET_UNICODE_SYNONYM))
        {
            outs = ToGetUnicodeSynonyms.Mutate(in, unicodeSynonymMap_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NORM_UNICODE))
        {
            outs = ToNormUnicode.Mutate(in, 
                symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
                startTag_, endTag_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NORM_UNICODE_WITH_SYNONYM))
        {
            outs = ToNormUnicodeWithSynonym.Mutate(in, unicodeSynonymMap_, 
                symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
                startTag_, endTag_, detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.NOMINALIZATION))
        {
            outs = ToNominalization.Mutate(in, conn_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.REMOVE_S))
        {
            outs = ToRemoveS.Mutate(in, removeSTree_, detailsFlag_, 
                mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.MAP_SYMBOL_TO_ASCII))
        {
            outs = ToMapSymbolToAscii.Mutate(in, symbolMap_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.MAP_UNICODE_TO_ASCII))
        {
            outs = ToMapUnicodeToAscii.Mutate(in, unicodeMap_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.UNICODE_CORE_NORM))
        {
            outs = ToUnicodeCoreNorm.Mutate(in, 
                symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
                detailsFlag_, mutateFlag_);
        }
        else if(flowNum == Flow.GetBitValue(Flow.STRIP_MAP_UNICODE))
        {
            outs = ToStripMapUnicode.Mutate(in, nonStripMap_, 
                detailsFlag_, mutateFlag_);
        }
        return outs;
    }
    // private methods
    private void Process(String line, boolean toStringFlag) 
        throws SQLException, IOException
    {
        originalTerm_ = line;
        // Input filter: get the term from appropriate field
        String fs = GlobalBehavior.GetFieldSeparator();
        String inTerm = InputFilter.GetInputTerm(line, fs, termFieldNum_);
        long inCat = InputFilter.GetInputCategory(line, fs, catFieldNum_);
        long inInfl = InputFilter.GetInputInflection(line, fs, inflFieldNum_);
        
        // go through all parallel flows 
        for(int flowNum = 0; flowNum < flowStrsList_.size(); flowNum++) 
        {
            LexItem in = new LexItem(inTerm, inCat, inInfl);
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
                for(int j = 0; j < ins.size(); j++)
                {
                    LexItem tempIn = ins.elementAt(j);
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
            // Output Filter Options
            String outStr = OutputFilter.ExecuteOutputFilter(outs, 
                mutateFlag_, detailsFlag_, fs, originalTerm_, inTerm,
                lvgOutputOption_); 
            
            // print results
            Out.Print(outWriter_, outStr, fileOutput_, toStringFlag);
        }
    }
    // check if category and inflection value are legal in -f:ici
    private boolean CheckInflectionByCatInfl()
    {
        boolean legalFlag = true;
        for(int i = 0; i < curInflectionByCatInflNum_; i++)
        {
            // category
            String catStr = inflectionCatList_.elementAt(i);
            try
            {
                Long.parseLong(catStr);
            }
            catch (Exception e)
            {
                if(catStr.equalsIgnoreCase("all") == true)
                {
                    inflectionCatList_.setElementAt(
                        Long.toString(Category.ALL_BIT_VALUE), i);
                }
                else
                {
                    System.err.println("** Error: Illegal category value (" 
                        + catStr + ") for -f:ici.");
                    legalFlag = false;
                }
            }
            // inflection
            String inflStr = inflectionInflList_.elementAt(i);
            try
            {
                Long.parseLong(inflStr);
            }
            catch (Exception e)
            {
                if(inflStr.equalsIgnoreCase("all") == true)
                {
                    inflectionInflList_.setElementAt(
                        Long.toString(Inflection.ALL_BIT_VALUE), i);
                }
                else
                {
                    System.err.println("** Error: Illegal inflection value (" 
                        + inflStr + ") for -f:ici.");
                    legalFlag = false;
                }
            }
        }
        return legalFlag;
    }
    private static Vector<String> GetOptions(String inStr)
    {
        Vector<String> out = new Vector<String>();
        StringTokenizer buf = new StringTokenizer(inStr, " \t");
        while(buf.hasMoreTokens() == true)
        {
            out.addElement(buf.nextToken());
        }
        return out;
    }
    private void Init()
    {
        PreProcess();
        // Init Quit String List
        //quitStrList_.addElement("q");
        //quitStrList_.addElement("quit");
        // Init config vars
        InitConfigVars();
        // Init Database and Persistant Trie
        if(runFlag_ == true)
        {
            InitDbAndTrie();
        }
    }
    // This method must be call after the optionStr is set and before Mutate
    private void PreProcess()
    {
        // go through all options
        int oldFlowNum = 0;
        Vector<String> args = GetOptions(option_.GetOptionStr());
        // Reset the pararell flows
        flowStrsList_.removeAllElements();
        // reset fieldList
        lvgOutputOption_.GetOutputFieldList().removeAllElements(); 
        // go through all options
        for(int i = 0; i < args.size(); i++)
        {
            String temp = args.elementAt(i);
            Option io = new Option(temp);
            // check if it is a new flow
            if(IsNewFlow(temp) == true)
            {
                flowNum_++;
                flowStrs_ = new Vector<String>();
            }
            // Decode input option to form options
            ExecuteCommands(io, GetOption());
            // update flowStrsList_
            if((flowNum_ > 0) && (flowNum_ != oldFlowNum))
            {
                // update flow String list
                flowStrsList_.addElement(flowStrs_);
                oldFlowNum = flowNum_;
            }
        }
    }
    // open database connection and persistent tries
    private void InitDbAndTrie()
    {
        // check config size
        if(conf_.GetSize() > 0)
        {
            int minTermLen = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MIN_TERM_LENGTH));
            String lvgDir = conf_.GetConfiguration(Configuration.LVG_DIR);
            int minTrieStemLength = Integer.parseInt(
                conf_.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
            try
            {
                if((dbFlag_ == true) && (conn_ == null))
                {
                    conn_ = DbBase.OpenConnection(conf_);     // connect to DB
                }
                if(ramTrieI_ == null)
                {
                    ramTrieI_ = new RamTrie(true, minTermLen, lvgDir, 0);
                }
                if(ramTrieD_ == null)
                {
                    ramTrieD_ = 
                        new RamTrie(false, minTermLen, lvgDir, minTrieStemLength);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void InitConfigVars()
    {
        // get config file from environment variable
        boolean useClassPath = false;
        if(configFile_ == null)
        {
            useClassPath = true;
            configFile_ = "data.config.lvg";
        }
        // read in configuration file
        conf_ = new Configuration(configFile_, useClassPath);
        // overwrite properties to configuration
        if(properties_ != null)
        {
            conf_.OverwriteProperties(properties_);
        }

        // set default prompt
        if(Platform.IsWindow() == true)
        {
            promptStr_ = 
            "- Please input a term (type \"Ctl-z\" then \"Enter\" to quit) >";
        }
        else
        {
            promptStr_ = "- Please input a term (type \"Ctl-d\" to quit) >";
        }

        // check config size
        if(conf_.GetSize() <= 0)
        {
            runFlag_ = false;
        }
        else
        {
            // Init varaibles that defines in configuration file
            if(lvgOutputOption_.GetOutRecordNum() == -1)
            {
                lvgOutputOption_.SetOutRecordNum(Integer.parseInt(
                    conf_.GetConfiguration(Configuration.MAX_RESULT)));
            }
            if(lvgOutputOption_.GetNoOutputStr() == null)
            {
                lvgOutputOption_.SetNoOutputStr( 
                    conf_.GetConfiguration(Configuration.NO_OUTPUT));
            }
            if(lvgOutputOption_.GetMarkEndStr() == null)
            {
                lvgOutputOption_.SetMarkEndStr(
                    conf_.GetConfiguration(Configuration.CCGI));
            }
            // read in the prompt from config file
            if(conf_.GetConfiguration(Configuration.LVG_PROMPT).equals("DEFAULT") 
                == false)
            {
                promptStr_ = conf_.GetConfiguration(Configuration.LVG_PROMPT);
            }
            if(lvgFlowSpecificOption_.GetMaxPermuteTermNum() == -1)
            {
                lvgFlowSpecificOption_.SetMaxPermuteTermNum(Integer.parseInt(
                    conf_.GetConfiguration(Configuration.MAX_UNINFLS)));
            }
            if(lvgFlowSpecificOption_.GetMaxMetaphoneCodeLength() == -1)
            {
                lvgFlowSpecificOption_.SetMaxMetaphoneCodeLength(Integer.parseInt(
                    conf_.GetConfiguration(Configuration.MAX_METAPHONE)));
            }
            if(stopWords_ == null)
            {
                stopWords_ = ToStripStopWords.GetStopWordsFromFile(conf_);
            }
            if(nonInfoWords_ == null)
            {
                nonInfoWords_ = ToSyntacticUninvert.GetNonInfoWordsFromFile(conf_);
            }
            if(conjunctionWords_ == null)
            {
                conjunctionWords_ = 
                    ToSyntacticUninvert.GetConjunctionWordsFromFile(conf_);
            }
            if(diacriticMap_ == null)
            {
                diacriticMap_ = ToStripDiacritics.GetDiacriticMapFromFile(conf_);
            }
            if(ligatureMap_ == null)
            {
                ligatureMap_ = ToSplitLigatures.GetLigatureMapFromFile(conf_);
            }
            if(startTag_ == null)
            {
                startTag_ = conf_.GetConfiguration(Configuration.START_TAG);
            }
            if(endTag_ == null)
            {
                endTag_ = conf_.GetConfiguration(Configuration.END_TAG);
            }
            if(unicodeSynonymMap_ == null)
            {
                unicodeSynonymMap_ = 
                    ToGetUnicodeSynonyms.GetUnicodeSynonymMapFromFile(conf_);
            }
            if(removeSTree_ == null)
            {
                removeSTree_ = ToRemoveS.GetRTrieTreeFromFile(conf_);
            }
            if(symbolMap_ == null)
            {
                symbolMap_ = ToMapSymbolToAscii.GetSymbolMapFromFile(conf_);
            }
            if(unicodeMap_ == null)
            {
                unicodeMap_ = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf_);
            }
            if(nonStripMap_ == null)
            {
                nonStripMap_ = ToStripMapUnicode.GetNonStripMapFromFile(conf_);
            }
        }
    }
    private void Close() throws IOException, SQLException
    {
        if((outWriter_ != null) && (fileOutput_ == true))
        {
            outWriter_.close();
        }
        if(inReader_ != null)
        {
            inReader_.close();
        }
        if(conn_ != null) 
        {
            DbBase.CloseConnection(conn_, conf_);     // close db connection
        }
    }
    // check if the option means another new flow (parallel)
    private static boolean IsNewFlow(String option)
    {
        boolean flag = false;
        if((option.length() >= 3)
        && (option.substring(0, 3).equals("-f:") == true))
        {
            flag = true;
        }
        return flag;
    }
    // data member
    protected Vector<String> quitStrList_ = new Vector<String>();  //quiting str
    protected boolean runFlag_ = true;         // flag for running LVG
    protected static BufferedReader inReader_ = null;         // infile buffer
    private static BufferedWriter outWriter_ = null;        // outfile buffer
    private static boolean fileOutput_ = false;        // flag for file output
    protected Vector<String> flowStrs_ = new Vector<String>();
    protected Vector<Vector<String>> flowStrsList_ 
        = new Vector<Vector<String>>();
    private boolean dbFlag_ = false;         // flag for connecting to DB
    private Option option_ = new Option("");    // input option
    private String promptStr_ = null;
    private String originalTerm_ = null;  // orignal input
    // input filter options
    protected int termFieldNum_ = 1;     // field num for input term
    protected int catFieldNum_ = -1;        // field num for input cat
    protected int inflFieldNum_ = -1;    // field num for input infl
    // output options
    private LvgOutputOption lvgOutputOption_ = new LvgOutputOption();
    // flow specific options
    private LvgFlowSpecificOption lvgFlowSpecificOption_ 
        = new LvgFlowSpecificOption();
    // global behavior options
    protected boolean promptFlag_ = false;     // flag for display prompt
    private int wordSize_ = 2;           // word size
    private boolean detailsFlag_ = false;    // flag for details print
    private boolean mutateFlag_ = false;     // flag for mutate print
    // for q3 flow component
    private String startTag_ = null;
    private String endTag_ = null;
    // for di flow compoment
    private Vector<String> derivationCatList_ = new Vector<String>();
    private int curDerivationCatNum_ = 0;
    // for ici flow compoment
    private Vector<String> inflectionCatList_ = new Vector<String>();
    private Vector<String> inflectionInflList_ = new Vector<String>();
    private int curInflectionByCatInflCount_ = 0;
    private int curInflectionByCatInflNum_ = 0;
    private int flowNum_ = 0;            // flow number
    private Connection conn_ = null;        // database connection
    private RamTrie ramTrieI_ = null;    // Ram trie: inflection
    private RamTrie ramTrieD_ = null;    // Ram trie: derivation
    private String configFile_ = null;
    // configuration related vars
    private Configuration conf_ = null;
    private Hashtable<String, String> properties_ = null;  // overwrite properties
    private Vector<String> stopWords_ = null;
    private Vector<String> nonInfoWords_ = null;
    private Vector<String> conjunctionWords_ = null;
    private Hashtable<Character, Character> diacriticMap_ = null;
    private Hashtable<Character, String> ligatureMap_ = null;
    private Hashtable<Character, Character> unicodeSynonymMap_ = null;
    private Hashtable<Character, String> symbolMap_ = null;
    private Hashtable<Character, String> unicodeMap_ = null;
    private Hashtable<Character, String> nonStripMap_ = null;
    private RTrieTree removeSTree_ = null; // ram trie tree: remove S rules
    static
    {
        try
        {
            outWriter_ = new BufferedWriter(new OutputStreamWriter(
                System.out, "UTF-8"));
        }
        catch (IOException e)
        {
            System.err.println("**Error: problem of opening Std-out.");
        }
    }
}
