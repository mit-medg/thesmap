/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

/**
 * @author mwc
 *
 */
public class ResourceConnectorcTakes extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorcTakes> pool = 
			new ResourceConnectorPool<ResourceConnectorcTakes>();
	
	
	Connection conn = null;
	Statement stmt = null;
	PreparedStatement query = null;
	PreparedStatement cui2tui = null;
	ResultSet rs = null;
	static final String nstr2cuiStmt = 
			"select y.cui, y.str, s.tui, s.sty from (select c.cui, c.str from (select distinct cui from mrxns_eng where nstr=?) as x join mrconso c on c.cui=x.cui where  c.ts='P' and c.stt='PF' and c.ispref='Y' and c.cvf is not null and c.LAT='ENG') as y join mrsty s on y.cui=s.cui;";
	static final String cui2tuiStmt = 
			"select tui from mrsty where cui=?";
	
	public AnalysisEngineDescription cTakesAED = null;
	
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorcTakes() {
		super("cTakes");
		ThesProps prop = ThesMap.prop;
		String dbhost = prop.getProperty(ThesProps.dbhostName);
		String dbuser = prop.getProperty(ThesProps.dbuserName);
		String dbpassword = prop.getProperty(ThesProps.dbpasswordName);
		String db = prop.getProperty(ThesProps.dbName);
		String dbUrl = "jdbc:mysql://" + dbhost + "/" + db;
		U.log("Trying to open connection to "+db+" on " +dbhost + " via "+dbuser+"/"+dbpassword+"for cTakes");

		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			query = conn.prepareStatement(nstr2cuiStmt);
			cui2tui = conn.prepareStatement(cui2tuiStmt);
			//SemanticEntity.init(this);
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
		if (initialized) pool.add(this);

		try {
			cTakesAED = ClinicalPipelineFactory.getDefaultPipeline();
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
	}
	
	public static ResourceConnectorcTakes get() {
		if (broken) return null;
		ResourceConnectorcTakes ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorcTakes());
			ans = pool.getNext();
		}
		if (!ans.initialized) {
			broken = true;
			ans = null;
		}
		return ans;
	}
	
	public static void assurePoolSize(int n) {
		for (; n < pool.size(); n++) {
			pool.add(new ResourceConnectorcTakes());
		}
	}
	
	@Override
	public void close() {
		
	}
	
	public void process(String text) throws UIMAException {
		if (broken || cTakesAED == null) {
			U.pe("Cannot process by cTakes because there is no connection.");
		}

	  JCas jcas = JCasFactory.createJCas();
	  jcas.setDocumentText(text);
	  SimplePipeline.runPipeline(jcas, cTakesAED);
	  for(IdentifiedAnnotation entity : JCasUtil.select(jcas, IdentifiedAnnotation.class)){
	      System.out.println("Entity: " + entity.getCoveredText() + " === Polarity: " + entity.getPolarity() +
	    		  													" === Uncertain? " + (entity.getUncertainty()==CONST.NE_UNCERTAINTY_PRESENT) +
	    		  													" === Subject: " + entity.getSubject() + 
	    		  													" === Generic? "  + (entity.getGeneric() == CONST.NE_GENERIC_TRUE) +
	    		  													" === Conditional? " + (entity.getConditional() == CONST.NE_CONDITIONAL_TRUE) +
	    		  													" === History? " + (entity.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT) +
	    		  													" === Type ID? " + entity.getTypeID()
	    		  );
	    }
	  
	  	// Write results to an XML File.
	  //cTakesAED.toXML(new FileWriter(args[0]));
	}
	
}
