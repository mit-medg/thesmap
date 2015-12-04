package edu.mit.csail.medg.thesmap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Preferences for ThesMap
 * This is a place to bring together all the preference names.
 */

public class ThesProps {

	// Names of the possible property files for ThesMap
	// These may be in the user directory, home directory, 
	
	protected static final String propFileName = ".ThesMap.properties";
	protected static final String propFileNameAlternate = "ThesMap.properties";
	
	// Property names used by ThesMap
	
	// For connection to UMLS resources
	static final String dbhostName = "dbHost";
	static final String dbuserName = "dbUser";
	static final String dbpasswordName = "dbPassword";
	static final String dbName = "db";
	
	// For connection to UMLS base resources
	static final String dbbhostName = "dbbHost";
	static final String dbbuserName = "dbbUser";
	static final String dbbpasswordName = "dbbPassword";
	static final String dbbName = "dbb";

	// for connection to the ASD database at CH
	static final String chdbhostName = "chdbHost";
	static final String chdbuserName = "chdbUser";
	static final String chdbpasswordName = "chdbPassword";
	static final String chdbName = "chdb";
	
	// For connection to the norm program of the UMLS LVG package
	static final String normYearName = "year";
	static final String normLvgHomeName = "lvgHome";
	static final String normLvgPropsName = "lvgProps"; 
	
	// For connection to MetaMap
	static final String serverProp = "MetaMapServer";
	static final String portProp = "MetaMapPort";
	static final String timeoutProp = "MetaMapTimeout";
	// For MetaMap processing options
	static final String outputMappingModeName = "MetaMapOutputMode";
	static final String mmHighestMappingMostSources = "HighestMappingMostSources";
//	static final String mmHighestMappingLowestCUI = "HighestMappingLowestCUI";
	static final String mmHighestMappingOnly = "HighestMappingOnly";
	static final String mmAllMappings = "AllMappings";
	
	// For connection to WJL's ParseMed
	static final String parseMedUrl = "ParseMedUrl";
	
	// For connection to cTakes
	static final String cTakesUserProp = "ctakes.umlsuser";
	static final String cTakesPWProp = "ctakes.umlspw";
	
	// For recording results in the thesmap database
	static final String resultHostName = "resultHost";
	static final String resultDbName = "resultDb";
	static final String resultUserName = "resultUser";
	static final String resultPasswordName = "resultPassword";
	static final String resultTableName = "resultTableName";
	
	// For getting sources to run batch SQL command
	static final String sourceHostName = "sourceHost";
	static final String sourceDbName = "sourceDb";
	static final String sourceUserName = "sourceUser";
	static final String sourcePasswordName = "sourcePassword";
	
	// Annotator Flag correspondences in SQL database 
	static final String UMLSFlag = "UmlsFlag";
	static final String UMLSBaseFlag = "UmlsBaseFlag";
	static final String MetaMapFlag = "MetaMapFlag";
	static final String cTakesFlag = "cTakesFlag";
	static final String NumericFlag = "NumericFlag";
	
	public Properties thesMapDefaults = null;
	public Properties thesMapProps = null;

	public ThesProps() {
		super();
		
		// First load the default Properties file, which is a resource in the
		// jar file:
		URL inJar = ClassLoader.getSystemClassLoader().getResource(propFileNameAlternate);
		if (inJar != null) {
			thesMapDefaults = new Properties();
			try (InputStream is = inJar.openStream()) {
				thesMapDefaults.load(is); 
			} catch (IOException e) {
				thesMapDefaults = null;	// as failure indicator
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Unable to find and load default TheMap.properties file;\n" +
								"Defaults should at least be in jar; exiting.");
			System.exit(1);
		}
		
		// Now look for the user-defined Properties file and load it
		thesMapProps = new Properties(thesMapDefaults);
		try {
			//load a properties file
			File homeDir = new File(System.getProperty("user.home"), propFileName);
			File homeDir1 = new File(System.getProperty("user.home"), propFileNameAlternate);
			File launchDir = new File(System.getProperty("user.dir"), propFileName);
			File launchDir1 = new File(System.getProperty("user.dir"), propFileNameAlternate);
			if (homeDir.exists()) thesMapProps.load(new FileReader(homeDir));
			else if (homeDir1.exists()) thesMapProps.load(new FileReader(homeDir1));
			else if (launchDir.exists()) thesMapProps.load(new FileReader(launchDir));
			else if (launchDir1.exists()) thesMapProps.load(new FileReader(launchDir1));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	// get/setProperty simply pass on the call to the actual Properties object
	public String getProperty(String key) {
		return thesMapProps.getProperty(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		return thesMapProps.getProperty(key, defaultValue);
	}
	
	public void setProperty(String key, String value) {
		thesMapProps.setProperty(key, value);
	}
	
	public Integer getInteger(String key) {
		return toInteger(getProperty(key));
	}
	
	public Integer getInteger(String key, String defaultValue) {
		return toInteger(getProperty(key, defaultValue));
	}
	
	public Integer getInteger(String key, Integer defaultInt) {
		Integer ans = getInteger(key);
		return (ans == null) ? defaultInt : ans;
	}
	
	private static Integer toInteger(String val) {
		if (val == null) return null;
		Integer ans = null;
		try {ans = new Integer(val);}
		catch (NumberFormatException e) {}
		return ans;
	}
	
	public String toShow() {
		StringBuilder sb = new StringBuilder("ThesMap Props:\n");
//		Set<Map.Entry<String, String>> foo = thesMapProps;
		for (Entry<Object, Object> x: thesMapProps.entrySet()) {
			sb.append(x.getKey());
			sb.append(": ");
			sb.append(x.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

}
