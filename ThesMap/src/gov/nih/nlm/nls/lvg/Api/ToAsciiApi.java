package gov.nih.nlm.nls.lvg.Api;
import java.util.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Flows.*;
/*****************************************************************************
* This class provides an API for ToAscii, convert UTF-8 to pure ASCII.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class ToAsciiApi
{
    // public constructor
    /**
    * Creates a ToAsciiApi object and initiate related data (default).
    */
    public ToAsciiApi()
    {
        Init();
    }
    /**
    * Creates a ToAsciiApi object and initiate related data using a specified
    * configuration file.
    *
    * @param   configFile   the absolute path of the configuration file
    */
    public ToAsciiApi(String configFile)
    {
        configFile_ = configFile;
        Init();
    }
    /**
    * Creates a ToAsciiApi object and initiate related data using a specified
    * configuration file.
    *
    * @param   conf   lvg configuration object
    */
    public ToAsciiApi(Configuration conf)
    {
        conf_ = conf;
        Init();
    }
    /**
    * Creates a ToAsciiApi object and initiate related data with properties
    * needs to be overwritten
    *
    * @param   properties   properties to be overwritten in config
    */
    public ToAsciiApi(Hashtable<String, String> properties)
    {
        properties_ = properties;
        Init();
    }
    /**
    * Creates a ToAsciiApi object and initiate related data using a specified
    * configuration file with properties to be wverwritten.
    *
    * @param   configFile   the absolute path of the configuration file
    * @param   properties   properties to be overwritten in config
    */
    public ToAsciiApi(String configFile, Hashtable<String, String> properties)
    {
        configFile_ = configFile;
        properties_ = properties;
        Init();
    }
    // public methods
    /**
    * A method to get the ASCII strings of an input string
    *
    * @param   inTerm  an input term in a string format to be mutated
    *
    * @return  String - ASCII string from toAscii result
    */
    public String Mutate(String inTerm)
    {
        // declare a new LexItem for input
        LexItem in = new LexItem(inTerm);
        LexItem out = Mutate(in);
        String outStr = out.GetTargetTerm();
        return outStr;
    }
    /**
    * A method to get the ASCII strings of an input LexItem
    *
    * @param   in  an input LexItem to be mutated
    *
    * @return  LexItem - ASCII results
    */
    public LexItem Mutate(LexItem in)
    {
        boolean showDetails = false;
        LexItem out = Mutate(in, showDetails);
        return out;
    }
    /**
    * A method to get the normalized strings of an input string along with
    * details information of norm operations
    *
    * @param   in  an input LexItem to be mutated
    * @param   showDetails  a boolean flag of showing details
    *
    * @return  Vector<LexItem> - normalized results 
    */
    public LexItem Mutate(LexItem in, boolean showDetails) 
    {
        // declare a new LexItem for input
        Vector<LexItem> outs = new Vector<LexItem>();
        // process: Mutate
        // -f:q7, Unicode Core Norm
        Vector<LexItem> outs1 = ToUnicodeCoreNorm.Mutate(in,
            symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
            showDetails, false);
        // -f:q8, Strip and Map
        LexItem out1 = outs1.elementAt(0);
        LexItem in1 = LexItem.TargetToSource(out1);
        Vector<LexItem> outs2 = ToStripMapUnicode.Mutate(in1,
            nonStripMap_, showDetails, false);
        // should have only 1 optput
        LexItem out = new LexItem();
        if(outs2.size() > 0)
        {
            out = outs2.elementAt(0);
        }
        return out;
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
    // private methods
    // init data: read in data from configuration file, instantiate trie,
    // and establishes a connection to Lvg Db
    private void Init()
    {
        // get config file from environment variable
        boolean useClassPath = false;
        if(configFile_ == null)
        {
            useClassPath = true;
            configFile_ = "data.config.lvg";
        }
        //read in configuration file
        if(conf_ == null)
        {
            conf_ = new Configuration(configFile_, useClassPath);
        }
        if(properties_ != null)
        {
            conf_.OverwriteProperties(properties_);
        }
        String lvgDir = conf_.GetConfiguration(Configuration.LVG_DIR);
        if(symbolMap_ == null)
        {
            symbolMap_ = ToMapSymbolToAscii.GetSymbolMapFromFile(conf_);
        }
        if(unicodeMap_ == null)
        {
            unicodeMap_ = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf_);
        }
        if(ligatureMap_ == null)
        {
            ligatureMap_ = ToSplitLigatures.GetLigatureMapFromFile(conf_);
        }
        if(diacriticMap_ == null)
        {
            diacriticMap_ = ToStripDiacritics.GetDiacriticMapFromFile(conf_);
        }
        if(nonStripMap_ == null)
        {
            nonStripMap_ = ToStripMapUnicode.GetNonStripMapFromFile(conf_);
        }
    }
    // data members
    private String configFile_ = null;    // configuration file
    private Configuration conf_ = null;   // configuration object
    private Hashtable<String, String> properties_ = null;  // overwrite properties
    private Hashtable<Character, String> symbolMap_ = null;
    private Hashtable<Character, String> unicodeMap_ = null;
    private Hashtable<Character, String> ligatureMap_ = null;
    private Hashtable<Character, Character> diacriticMap_ = null;
    private Hashtable<Character, String> nonStripMap_ = null;
}
