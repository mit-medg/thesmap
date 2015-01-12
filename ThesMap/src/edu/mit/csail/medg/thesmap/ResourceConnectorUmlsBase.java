/**
 * This implements a resource connector to a MySQL version of the UMLS
 * Metathesaurus.
 * When it is first initialized, we also init on SemanticEntity, to make sure
 * we have cached in Java structures the TUI tree from UMLS. 
 */
package edu.mit.csail.medg.thesmap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author psz
 *
 */
public class ResourceConnectorUmlsBase extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorUmlsBase> pool = 
			new ResourceConnectorPool<ResourceConnectorUmlsBase>();
	
	Connection conn = null;
	Statement stmt = null;
	PreparedStatement query = null;
	PreparedStatement cui2tui = null;
	ResultSet rs = null;
	static final String nstr2cuiStmt = 
			"select y.cui, y.str, s.tui, s.sty from (select c.cui, c.str from (select distinct cui from mrxns_eng where nstr=?) as x join mrconso c on c.cui=x.cui where  c.ts='P' and c.stt='PF' and c.ispref='Y' and c.cvf is not null and c.LAT='ENG') as y join mrsty s on y.cui=s.cui;";
	static final String cui2tuiStmt = 
			"select tui from mrsty where cui=?";
	
	// interpretationCache maps any normalized phrases we have looked up to
	// an InterpretationSet. If nothing was found, we store the nullInterpretationSet,
	// to remember that we have previously seen this phrase.
	// We implement this as a static cache, which means it will be shared
	// by all instances of a ResourceConnectorUmls.
	static ConcurrentHashMap<String, InterpretationSet> interpretationCache =
			new ConcurrentHashMap<String, InterpretationSet>();
	static ConcurrentHashMap<String, String> cui2tuiCache =
			new ConcurrentHashMap<String, String>();
	static boolean broken = false;
	
	public ResourceConnectorUmlsBase() {
		super("UMLSBase");
		ThesProps prop = ThesMap.prop;
		String dbhost = prop.getProperty(ThesProps.dbbhostName);
		String dbuser = prop.getProperty(ThesProps.dbbuserName);
		String dbpassword = prop.getProperty(ThesProps.dbbpasswordName);
		String db = prop.getProperty(ThesProps.dbbName);
		String dbUrl = "jdbc:mysql://" + dbhost + "/" + db;
		U.log("Trying to open connection to "+db+" on " +dbhost + " via "+dbuser+"/"+dbpassword);
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			query = conn.prepareStatement(nstr2cuiStmt);
			cui2tui = conn.prepareStatement(cui2tuiStmt);
//			SemanticEntity.init(this);
			initialized = true;
		}
		catch (ClassNotFoundException e) {
			System.err.println("Unable to load MySQL driver: " + e);
			initialized = false;
		}
		catch (SQLException e) {
			System.err.println("Unable to connect to UMLS database \"" + db + "\" on " 
								+ dbhost + " as \"" + dbuser + "\": " + e.getMessage());
			//e.printStackTrace(System.err);
			initialized = false;
		}
		if (initialized) {
			pool.add(this);
			U.log("Opened connection to UMLSBase data.");
		}
	}
	
	public static ResourceConnectorUmlsBase get() {
		if (broken) return null;
		ResourceConnectorUmlsBase ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorUmlsBase());
			ans = pool.getNext();
			if (!ans.initialized) {
				broken = true;
				ans = null;
			}
		}
		return ans;
	}
	
	public static void assurePoolSize(int n) {
		for (; n < pool.size(); n++) {
			pool.add(new ResourceConnectorUmlsBase());
		}
	}

	/**
	 * Looks up the UMLS interpretation(s) of a normalized phrase.
	 * We keep a cache so we don't have to look it up again.
	 * 
	 * @param normalizedPhrase
	 * @return
	 */
	public synchronized InterpretationSet lookupNormalized(String normalizedPhrase, String annType) {
//		U.log("LookupNorm: " + normalizedPhrase);
		InterpretationSet value = interpretationCache.get(normalizedPhrase);
			if (value != null) {
//				U.log("...found in cache: " + value.toString());
				return value;	// Includes case of nullInterpretationSet
			}
			value = new InterpretationSet();
			try {
				query.setString(1,  normalizedPhrase);
				rs = query.executeQuery();
				while (rs.next()) value.add(Interpretation.makeInterpretation(annType, rs));
			} catch (SQLException e) {
				System.err.println("SQL Error looking up normalized phrase \""
						+ normalizedPhrase + "\": " + e.getMessage());
				value = InterpretationSet.nullInterpretationSet;
			}
			if (value.size() == 0) value = InterpretationSet.nullInterpretationSet;
			interpretationCache.put(normalizedPhrase, value);
//			U.log("lookupNorm returns " + value.toString());
			return value;
	}
	
	public ArrayList<String> cui2tui(String cui) {
		ArrayList<String> ans = new ArrayList<String>();
		try {
			cui2tui.setString(1,  cui);
			ResultSet rs = cui2tui.executeQuery();
			while (rs.next()) ans.add(rs.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ans;
		
	}
	
	public void close() {
		if (conn!=null) {
			try{ 
				U.p("Closing MySQL Connection.");
				if (rs!=null) rs.close();
			} catch (SQLException e) {}
			try{
				if (query!=null) query.close();
			} catch (SQLException e) {}
			try{
				if (conn!=null) conn.close();
			} catch (SQLException e) {}
		}
	}
	
	public void report() {
		int nNulls = 0;
		for (InterpretationSet i: interpretationCache.values()) {
			if (i.isNull()) nNulls++;
		}
		U.p(interpretationCache.size() + " cached normalized phrases, " + nNulls + " null.");
	}

	
}
