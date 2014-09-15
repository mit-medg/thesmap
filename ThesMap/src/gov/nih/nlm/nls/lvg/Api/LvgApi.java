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
* This class provides an LVG API for users to setup flows by calling 
* Mutate( ) method in flow component classes.   The input of this API can be 
* either an LexItem or Vector<LexItem>.  The output is Vector<LexItem>.
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
* </ul>
*
* @author NLM NLS Development Team
*
* @see  <a href="../../../../../../../designDoc/LifeCycle/deploy/LvgApi.html">
* Design Document</a>
*
* @version    V-2013
****************************************************************************/
public class LvgApi
{
    // public constructor
    /**
    * Creates an LvgApi object and initiate related data (default).
    * This constructor is consider as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using a LvgApi object
    */
    public LvgApi()
    {
        Init();
    }
    /**
    * Creates an LvgApi object and initiate related data (default), using
    * a specified configuration file.
    * <p> This constructor is consider as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using a LvgApi object
    *
    * @param   configFile   the absolute path of the configuration file
    */
    public LvgApi(String configFile)
    {
        configFile_ = configFile;
        Init();
    }
    /**
    * Creates an LvgApi object and initiate related data with
    * properties to be overwritten in configuration.
    * This constructor is consider as a preprocess method.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using a LvgApi object
    *
    * @param properties  properties to be overwrite in configuration
    */
    public LvgApi(Hashtable<String, String> properties)
    {
        properties_ = properties;
        Init();
    }
    /**
    * Creates an LvgApi object and initiate related data, using
    * a specified configuration file, with properties to be overwritten 
    * in configuration
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using a LvgApi object
    * 
    * @param   configFile   the absolute path of the configuration file
    * @param   properties  properties to be overwrite in configuration
    */
    public LvgApi(String configFile, Hashtable<String, String> properties)
    {
        configFile_ = configFile;
        properties_ = properties;
        Init();
    }
    // public methods
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
    * Get the Lvg database connection.
    * This method is a preprocess method.
    *
    * @return  the JDBC connection to Lvg database
    */
    public Connection GetConnection()
    {
        return conn_;
    }
    /**
    * Close the Lvg database connection.
    *
    * @param  conn the JDBC connection to Lvg database
    */
    public void CloseConnection(Connection conn)
    {
        if(conn != null)
        {
            try
            {
                DbBase.CloseConnection(conn, conf_);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    /**
    * Get the ram trie object for Lvg inflection rules.
    * This method is a preprocess method.
    *
    * @return  Lvg inflection ram trie
    */
    public RamTrie GetInflectionTrie()
    {
        return ramTrieI_;
    }
    /**
    * Get the ram trie object for Lvg derivation rules.
    * This method is a preprocess method.
    *
    * @return  Lvg derivation ram trie
    */
    public RamTrie GetDerivationTrie()
    {
        return ramTrieD_;
    }
    /**
    * Get the ram trie object for Lvg removeS tree.
    * This method is a preprocess method.
    *
    * @return  Lvg inflection ram trie
    */
    public RTrieTree GetRemoveSTree()
    {
        return removeSTree_;
    }
    /**
    * Close Lvg database connection and persistent tries.  This methods must
    * be called before exiting LvgApi.  It is a method from postprocess.
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
    * Get the maximum terms for permuting uninflection wrods.
    *
    * @return  the maximum terms for permuting uninflection wrods
    */
    public int GetMaxTerm()
    {
        return maxTerm_;
    }
    /**
    * Get the maximum length for metaphone code.
    *
    * @return  the maximum length for metaphone code
    */
    public int GetMaxCodeLength()
    {
        return maxCodeLength_;
    }
    /**
    * Get the list of stop words
    *
    * @return  Vector<String> - stop words list
    */
    public Vector<String> GetStopWords()
    {
        return stopWords_;
    }
    /**
    * Get the list of non-info words
    *
    * @return  Vector<String> - non-info words list
    */
    public Vector<String> GetNonInfoWords()
    {
        return nonInfoWords_;
    }
    /**
    * Get the list of conjunction words
    *
    * @return  Vector<String> - conjunction words list
    */
    public Vector<String> GetConjunctionWords()
    {
        return conjunctionWords_;
    }
    /**
    * Get the list of diacritics mapping list
    *
    * @return  a Hash table of diacritics mapping list
    */
    public Hashtable<Character, Character> GetDiacriticMap()
    {
        return diacriticMap_;
    }
    /**
    * Get the list of ligatures mapping list
    *
    * @return  a Hash table of ligatures mapping list
    */
    public Hashtable<Character, String> GetLigatureMap()
    {
        return ligatureMap_;
    }
    /**
    * Get the list of Unicode synonym mapping list
    *
    * @return  a Hash table of Unicode synonym mapping list
    */
    public Hashtable<Character, Character> GetUnicodeSynonymMap()
    {
        return unicodeSynonymMap_;
    }
    /**
    * Get the list of symbols mapping list
    *
    * @return  a Hash table of symbols mapping list
    */
    public Hashtable<Character, String> GetSymbolMap()
    {
        return symbolMap_;
    }
    /**
    * Get the list of Unicode mapping list
    *
    * @return  a Hash table of Unicode mapping list
    */
    public Hashtable<Character, String> GetUnicodeMap()
    {
        return unicodeMap_;
    }
    /**
    * Get the list of non-Strip mapping list
    *
    * @return  a Hash table of nono-strip mapping list
    */
    public Hashtable<Character, String> GetNonStripMap()
    {
        return nonStripMap_;
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
        conf_ = new Configuration(configFile_, useClassPath);
        // overwrite properties to configuration
        if(properties_ != null)
        {
            conf_.OverwriteProperties(properties_);
        }
        
        // check config size
        if(conf_.GetSize() > 0)
        {
            int minTermLen = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MIN_TERM_LENGTH));
            String lvgDir = conf_.GetConfiguration(Configuration.LVG_DIR);
            // Init varaibles that defines in configuration file
            outRecordNum_ = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MAX_RESULT));
            maxTerm_ = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MAX_UNINFLS));
            maxCodeLength_ = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MAX_METAPHONE));
            stopWords_ = ToStripStopWords.GetStopWordsFromFile(conf_);
            nonInfoWords_ = ToSyntacticUninvert.GetNonInfoWordsFromFile(conf_);
            conjunctionWords_ =
                ToSyntacticUninvert.GetConjunctionWordsFromFile(conf_);
            diacriticMap_ = ToStripDiacritics.GetDiacriticMapFromFile(conf_);
            ligatureMap_ = ToSplitLigatures.GetLigatureMapFromFile(conf_);
            unicodeSynonymMap_ =
                ToGetUnicodeSynonyms.GetUnicodeSynonymMapFromFile(conf_);
            symbolMap_ = ToMapSymbolToAscii.GetSymbolMapFromFile(conf_);
            unicodeMap_ = ToMapUnicodeToAscii.GetUnicodeMapFromFile(conf_);
            nonStripMap_ = ToStripMapUnicode.GetNonStripMapFromFile(conf_);
            int minTrieStemLength = Integer.parseInt(
                conf_.GetConfiguration(Configuration.DIR_TRIE_STEM_LENGTH));
            removeSTree_ = ToRemoveS.GetRTrieTreeFromFile(conf_);
            try
            {
                // Open DataBase connection
                conn_ = DbBase.OpenConnection(conf_);     // connect to DB
                // Instantiate Ram Tries
                ramTrieI_ = new RamTrie(true, minTermLen, lvgDir, 0);
                ramTrieD_ = 
                    new RamTrie(false, minTermLen, lvgDir, minTrieStemLength);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    // CLose persistent files, files, and database connection 
    private void Close() throws IOException, SQLException
    {
        if(conn_ != null)
        {
            DbBase.CloseConnection(conn_, conf_);    // close db connection
        }
    }
    // data members
    private int outRecordNum_ = -1;      // output record num for a flow
    private Connection conn_ = null;     // database connection
    private RamTrie ramTrieI_ = null;    // Ram trie: inflection
    private RamTrie ramTrieD_ = null;    // Ram trie: derivation
    private String configFile_ = null;   // configuration file
    private Configuration conf_ = null;  // configuratin object
    private RTrieTree removeSTree_ = null;    // Reverse Trie Tree: remove s
    private Hashtable<String, String> properties_ = null;  // overwrite properties
    // configuration related vars
    private int maxTerm_ = -1;
    private int maxCodeLength_ = -1;
    private Vector<String> stopWords_ = null;
    private Vector<String> nonInfoWords_ = null;
    private Vector<String> conjunctionWords_ = null;
    private Hashtable<Character, Character> diacriticMap_ = null;
    private Hashtable<Character, String> ligatureMap_ = null;
    private Hashtable<Character, Character> unicodeSynonymMap_ = null;
    private Hashtable<Character, String> symbolMap_ = null;
    private Hashtable<Character, String> unicodeMap_ = null;
    private Hashtable<Character, String> nonStripMap_ = null;
}
