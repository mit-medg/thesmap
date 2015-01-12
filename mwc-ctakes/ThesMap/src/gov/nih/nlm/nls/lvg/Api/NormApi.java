package gov.nih.nlm.nls.lvg.Api;
import java.util.*;
import java.sql.*;
import java.io.*;
import gov.nih.nlm.nls.lvg.Lib.*;
import gov.nih.nlm.nls.lvg.Flows.*;
import gov.nih.nlm.nls.lvg.Db.*;
import gov.nih.nlm.nls.lvg.Trie.*;
/*****************************************************************************
* This class provides an API for norm.
*
* <p><b>History:</b>
* <ul>
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class NormApi
{
    // public constructor
    /**
    * Creates a NormApi object and initiate related data (default).
    *
    * <p> CleanUp( ) method must be called to close Db connection
    * after using this object
    */
    public NormApi()
    {
        Init();
    }
    /**
    * Creates a NormApi object and initiate related data using a specified
    * configuration file.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param   configFile   the absolute path of the configuration file
    */
    public NormApi(String configFile)
    {
        configFile_ = configFile;
        Init();
    }
    /**
    * Creates a NormApi object and initiate related data with properties
    * needs to be overwritten
    *
    * <p> CleanUp( ) method must be called to close Db connection
    * after using this object
    *
    * @param   properties   properties to be overwritten in config
    */
    public NormApi(Hashtable<String, String> properties)
    {
        properties_ = properties;
        Init();
    }
    /**
    * Creates a NormApi object and initiate related data using a specified
    * configuration file with properties to be wverwritten.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param   configFile   the absolute path of the configuration file
    * @param   properties   properties to be overwritten in config
    */
    public NormApi(String configFile, Hashtable<String, String> properties)
    {
        configFile_ = configFile;
        properties_ = properties;
        Init();
    }
    /**
    * Creates a NormApi object and initiate related data using specified
    * database connection and persistent trie.
    *
    * <p> CleanUp( ) method must be called to close Db connection 
    * after using this object
    *
    * @param   conn   database connection
    * @param   ramTrieI  persistent trie for inflectional morphology
    */
    public NormApi(Connection conn, RamTrie ramTrieI)
    {
        conn_ = conn;
        ramTrieI_ = ramTrieI;
    }
    // public methods
    /**
    * A method to get the normalized strings of an input LexItem
    *
    * @param   in  an input LexItem to be mutated
    *
    * @return  Vector<LexItem> - normalized results 
    */
    public Vector<LexItem> Mutate(LexItem in) throws Exception
    {
        Vector<LexItem> outs = new Vector<LexItem>();
        // process: Mutate
        outs = ToNormalize.Mutate(in, maxTerm_, stopWords_, conn_, 
            ramTrieI_, symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_, 
            nonStripMap_, removeSTree_, false, false);
        return outs;
    }
    /**
    * A method to get the normalized strings of an input string
    *
    * @param   inTerm  an input term in a string format to be mutated
    *
    * @return  Vector<String> - normalized results
    */
    public Vector<String> Mutate(String inTerm) throws Exception
    {
        // declare a new LexItem for input
        LexItem in = new LexItem(inTerm);
        Vector<LexItem> outs = Mutate(in);
        // just return the normalize String
        Vector<String> outStrs = new Vector<String>();
        for(int i = 0; i < outs.size(); i ++)
        {
            LexItem cur = outs.elementAt(i);
            outStrs.addElement(cur.GetTargetTerm());
        }
        return outStrs;
    }
    /**
    * A method to get the normalized strings of an input string along with
    * details information of norm operations
    *
    * @param   inTerm  an input term in a string format to be mutated
    * @param   showDetails  a boolean flag of showing details
    *
    * @return  Vector<LexItem> - normalized results 
    */
    public Vector<LexItem> Mutate(String inTerm, boolean showDetails) 
        throws Exception
    {
        // declare a new LexItem for input
        LexItem in = new LexItem(inTerm);
        Vector<LexItem> outs = new Vector<LexItem>();
        // process: Mutate
        outs = ToNormalize.Mutate(in, maxTerm_, stopWords_, conn_, 
            ramTrieI_, symbolMap_, unicodeMap_, ligatureMap_, diacriticMap_,
            nonStripMap_, removeSTree_, showDetails, false);
        return outs;
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
    * A method to get the configuration object
    *
    * @return  Configuration
    */
    public Configuration GetConfiguration()
    {
        return conf_;
    }
    /**
    * A method to get the established database connection
    *
    * @return  database connection
    */
    public Connection GetConnection()
    {
        return conn_;
    }
    /**
    * A method to get the persistent trie for inflectional morphology
    *
    * @return  persistent trie for inflectional morphology
    */
    public RamTrie GetTrie()
    {
        return ramTrieI_;
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
        if(properties_ != null)
        {
            conf_.OverwriteProperties(properties_);
        }
        int minTermLen = Integer.parseInt(
            conf_.GetConfiguration(Configuration.MIN_TERM_LENGTH));
        String lvgDir = conf_.GetConfiguration(Configuration.LVG_DIR);
        if(maxTerm_ == -1)
        {
            maxTerm_ = Integer.parseInt(
                conf_.GetConfiguration(Configuration.MAX_UNINFLS));
        }
        if(stopWords_ == null)
        {
            stopWords_ = ToStripStopWords.GetStopWordsFromFile(conf_);
        }
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
        if(removeSTree_ == null)
        {
            removeSTree_ = ToRemoveS.GetRTrieTreeFromFile(conf_);
        }
        try
        {
            // Open DataBase connection
            if(conn_ == null)
            {
                conn_ = DbBase.OpenConnection(conf_);     // connect to DB
            }
            // Open Ram Tries
            if(ramTrieI_ == null)
            {
                ramTrieI_ = new RamTrie(true, minTermLen, lvgDir, 0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    // Close persistent files, files, and database connection 
    private void Close() throws IOException, SQLException
    {
        if(conn_ != null)
        {
            DbBase.CloseConnection(conn_, conf_);      // close db connection
        }
    }
    // data members
    private String configFile_ = null;    // configuration file
    private Configuration conf_ = null;   // configuration object
    private Connection conn_ = null;      // database connection
    private RamTrie ramTrieI_ = null;     // Ram trie: inflection
    private int maxTerm_ = -1;
    private Vector<String> stopWords_ = null;
    private Hashtable<String, String> properties_ = null;  // overwrite properties
    private Hashtable<Character, String> symbolMap_ = null;
    private Hashtable<Character, String> unicodeMap_ = null;
    private Hashtable<Character, String> ligatureMap_ = null;
    private Hashtable<Character, Character> diacriticMap_ = null;
    private Hashtable<Character, String> nonStripMap_ = null;
    private RTrieTree removeSTree_ = null;
}
