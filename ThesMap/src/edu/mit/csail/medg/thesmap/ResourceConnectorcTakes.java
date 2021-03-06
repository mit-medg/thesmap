/**
 * 
 */
package edu.mit.csail.medg.thesmap;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.ctakes.clinicalpipeline.ClinicalPipelineFactory;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.OntologyConcept;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.xml.sax.SAXException;

/**
 * @author mwc
 *
 */
public class ResourceConnectorcTakes extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorcTakes> pool = 
			new ResourceConnectorPool<ResourceConnectorcTakes>();
	public AnalysisEngineDescription cTakesAED = null;
	
	/*
	 * If this resource is not available, we declare it broken and do not
	 * try to create additional instances.
	 */
	static boolean broken = false;
		
	public ResourceConnectorcTakes() {
		super("cTakes");

		try {
			cTakesAED = ClinicalPipelineFactory.getFastPipeline();
			initialized = true;
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
			initialized = false;
		}
		//if (initialized) pool.add(this);
	}
	
	public static ResourceConnectorcTakes get() {
		if (broken) return null;
		ResourceConnectorcTakes ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorcTakes());
			U.log("new ctakes created");
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
			pool.add(new ResourceConnectorcTakes());
		}
	}

	public ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> process(String text) throws UIMAException, SAXException, IOException {
		if (broken || cTakesAED == null) {
			U.pe("Cannot process by cTakes because there is no connection.");
		}

	  ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet> ctakesInterpretations =
				new ConcurrentHashMap<IdentifiedAnnotation, InterpretationSet>();
	  JCas jcas = JCasFactory.createJCas();
	  jcas.setDocumentText(text);
	  SimplePipeline.runPipeline(jcas, cTakesAED);
	  for(IdentifiedAnnotation entity : JCasUtil.select(jcas, IdentifiedAnnotation.class)){
	      InterpretationSet is = makeInterpretationSet(entity);
	      if (is != null && is != InterpretationSet.nullInterpretationSet) {
	    	  ctakesInterpretations.put(entity, is);
	      }
	   }
	  System.out.println("Finished cTakes processing");
	  
	  return ctakesInterpretations;
	  //return 
	  
	  	// Write results to an XML File.
	  //cTakesAED.toXML(new FileWriter("sampleText.txt"));
	}
	
	// Taken from ctakes-clinical-pipeline-BagOfCUIsGenerator
	protected String extractInformation(IdentifiedAnnotation t) {
		StringBuilder buff = new StringBuilder();
		
		FSArray mentions = t.getOntologyConceptArr();
		
		HashSet<String> uniqueCuis = new HashSet<String>();
		if(mentions == null) return null;
		for(int i = 0; i < mentions.size(); i++){
			if(mentions.get(i) instanceof UmlsConcept){
				UmlsConcept concept = (UmlsConcept) mentions.get(i);
				uniqueCuis.add(concept.getCui());
			}
		}
		
		for(String cui : uniqueCuis){
			if(t.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT) buff.append("-");
			buff.append(cui);
			buff.append("\n");
		}
		
		if(buff.length() == 0) return null;
		return buff.substring(0,buff.length()-1);
	}
	
	// Create the Interpretation Set
	protected InterpretationSet makeInterpretationSet(IdentifiedAnnotation t) {
    	InterpretationSet ans = new InterpretationSet();
		FSArray mentions = t.getOntologyConceptArr();
		
		if (mentions == null) return null;
		for(int i = 0; i < mentions.size(); i++){
			if(mentions.get(i) instanceof UmlsConcept){
				UmlsConcept concept = (UmlsConcept) mentions.get(i);
				boolean neg = false;
				if (t.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT) {
					neg = true;
				}
				String tui = concept.getTui(); 
				String tuiName = SemanticEntity.sems.get(tui).name;
				
				ans.add(new InterpretationcTakes(concept.getCui(), concept.getPreferredText(), tui, tuiName, neg, concept.getScore()));
			}
		}

		if (ans.size() == 0) ans = InterpretationSet.nullInterpretationSet;
		return ans;
	}

	@Override
	public void close() {
		System.out.println("closing the ResourceConnector thread");
	}
}

