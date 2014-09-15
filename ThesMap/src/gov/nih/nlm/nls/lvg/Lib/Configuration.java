package gov.nih.nlm.nls.lvg.Lib;
import java.io.*;
import java.util.*;
import gov.nih.nlm.nls.lvg.Util.*;
/*****************************************************************************
* This class provides a way of storing and retrieving configurations through a
* configuration object. 
*
* <p> Currently, it contains variables of:
* LVG_DIR, LVG_STOP_WORD_FILE, MAX_RULE_UNINFLECTED_TERMS, etc..
*
* <p><b>History:</b>
* <ul>
* <li>SCR-5, chlu, 06-08-12, fix config file error message
* </ul>
*
* @author NLM NLS Development Team
*
* @version    V-2013
****************************************************************************/
public class Configuration
{
    // public constructor
    /**
    * Create a Configuration object.  There are two ways of reading 
    * configuration files.  First, finding xxx.properties from Java class path.
    * Second, finding file by the specified path. 
    *  
    * @param  fName  the path of the configuration file or base name when
    * using class path.
    * @param  useClassPath  a flag of finding configurationfile from class path
    */
    public Configuration(String fName, boolean useClassPath)
    {
        SetConfiguration(fName, useClassPath);
    }
    // public methods
    /**
    * Get the size of key of config hashtable.
    *
    * @return  the size of configuration item (keys)
    */
    public int GetSize()
    {
        int size = 0;
        if(config_ != null)
        {
            size = config_.size();
        }
        return size;
    }

    /**
    * Get a value from configuration file by specifying the key.
    *
    * @param  key  key (name) of the configuration value to be get
    *
    * @return  the value of the configuration item in a string format
    */
    public String GetConfiguration(String key)
    {
        String out = config_.get(key);
        return out;
    }
    /**
    * Overwrite the value if it is specified in the properties.
    *
    * @param  properties  properties to be overwrite in the configuration
    */
    public void OverwriteProperties(Hashtable<String, String> properties)
    {
        for(Enumeration<String> e = properties.keys(); e.hasMoreElements();)
        {
            String key = e.nextElement();
            String value = properties.get(key);
            config_.put(key, value);
        }
    }
    /**
    * Get system level information from configuration.  This includes
    * LVG_DIR, DB_TYPE, DB_NAME
    *
    * @return  the value of the configuration item in a string format
    */
    public String GetInformation()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("LVG_DIR: [" + GetConfiguration(LVG_DIR) + "]");
        buffer.append(GlobalBehavior.LS_STR);
        buffer.append("DB_TYPE: [" + GetConfiguration(DB_TYPE) + "]");
        buffer.append(GlobalBehavior.LS_STR);
        buffer.append("DB_NAME: [" + GetConfiguration(DB_NAME) + "]");
        return buffer.toString();
    }
    // private methods
    private void SetConfiguration(String fName, boolean useClassPath)
    {
        try
        {
            // get config data from fName.properties in class path
            if(useClassPath == true)
            {
                configSrc_ = 
                    (PropertyResourceBundle) ResourceBundle.getBundle(fName);
            }
            else // get config data from fName (path) file
            {
                // check if fName exist
                FileInputStream file = new FileInputStream(fName);
                configSrc_ = new PropertyResourceBundle(file);
                file.close();
            }
        }
        catch (Exception e)
        {
            System.err.println("** Configuration Error: " + e.getMessage());
            System.err.println(
                "** Error: problem of opening/reading config file: '" +
                fName + "'. Use -x option to specify the config file path.");
        }

        // put properties from configSrc_ into config_
        if(configSrc_ != null)
        {
            for(Enumeration<String> e = configSrc_.getKeys(); 
                e.hasMoreElements();)
            {
                String key = e.nextElement();
                String value = configSrc_.getString(key);
                config_.put(key, value);
            }
        }
        // reset TOP_DIR
        String lvgDir = GetConfiguration(LVG_DIR); 
        if((lvgDir != null)
        && (lvgDir.equals(AUTO_MODE) == true))
        {
            File file = new File(System.getProperty("user.dir"));
            String curDir = file.getAbsolutePath()
                + System.getProperty("file.separator");
            config_.put(LVG_DIR, curDir);    
        }
    }
    // data member
    /** key for the path of LVG directory defined in configuration file */
    public final static String LVG_DIR = "LVG_DIR";
    public final static String AUTO_MODE = "AUTO_MODE";
    /** key for the path of stop word file defined in configuration file */
    public final static String STOP_WORD_FILE = "LVG_STOP_WORD_FILE";
    /** key for the path of non-info word file defined in configuration file */
    public final static String NONINFO_WORD_FILE = "LVG_NONINFO_WORD_FILE";
    /** key for path of conjunction word file defined in configuration file*/
    public final static String CONJ_WORD_FILE = "LVG_CONJ_WORD_FILE";
    /** key for the path of diacritics file defined in configuration file */
    public final static String DIACRITICS_FILE = "LVG_DIACRITICS_FILE";
    /** key for the path of ligatures file defined in configuration file */
    public final static String LIGATURES_FILE = "LVG_LIGATURES_FILE";
    /** key for the path of unicode synonyms file defined in conf file */
    public final static String UNICODE_SYNONYM_FILE = "LVG_UNICODE_SYNONYM_FILE";
    /** key for the path of Unicode symbols mapping file defined in conf file */
    public final static String UNICODE_SYMBOL_FILE = "LVG_UNICODE_SYMBOL_FILE";
    /** key for the path of Unicode mapping file defined in conf file */
    public final static String UNICODE_FILE = "LVG_UNICODE_FILE";
    /** key for the path of non-strip map Unicode file defined in conf file */
    public final static String NON_STRIP_MAP_UNICODE_FILE = "LVG_NON_STRIP_MAP_UNICODE_FILE";
    
    /** key for the path of remove (s) rule file defined in conf file */
    public final static String REMOVE_S_FILE = "LVG_REMOVE_S_FILE";
    /** key for the minimum length of rule generated terms */
    public final static String MIN_TERM_LENGTH = "MIN_TERM_LENGTH";
    /** key for the maximum length of metaphone code */
    public final static String MAX_METAPHONE = "MAX_METAPHONE";
    /** key for the maximum uninflected terms defined in configuration file */
    public final static String MAX_UNINFLS = "MAX_RULE_UNINFLECTED_TERMS";
    /** key for the mark of cgi ending defined in configuration file */
    public final static String CCGI = "CGI_EOP";
    /** key for the mark of no output message defined in configuration file */
    public final static String NO_OUTPUT = "NO_OUTPUT";
    /** key for the number of maximum records shown defined in configuration 
    file */
    public final static String MAX_RESULT = "TRUNCATED_RESULTS";
    /** key for the mark of prompt in configuration file */
    public final static String LVG_PROMPT = "LVG_PROMPT";
    /**  Min legal number of stem legnth in derivation trie */
    public final static String DIR_TRIE_STEM_LENGTH = "DIR_TRIE_STEM_LENGTH";
    /**  starting tag for unicode symbol name */
    public final static String START_TAG = "START_TAG";
    /**  ending tag for unicode symbol name */
    public final static String END_TAG = "END_TAG";
    /** Data base */
    public final static String DB_TYPE = "DB_TYPE";
    /** Java Data base Connectivity Driver */
    public final static String DB_DRIVER = "DB_DRIVER";
    /** Java Data Base Connectivity URL */
    public final static String JDBC_URL = "JDBC_URL";
    /** Database host name */
    public final static String DB_HOST = "DB_HOST";
    /** database name */
    public final static String DB_NAME = "DB_NAME";
    /** database user name */
    public final static String DB_USERNAME = "DB_USERNAME";
    /** database password */
    public final static String DB_PASSWORD = "DB_PASSWORD";
    // private data member
    private PropertyResourceBundle configSrc_ = null;
    private Hashtable<String, String> config_ =
        new Hashtable<String, String>();    // the real config vars
}
