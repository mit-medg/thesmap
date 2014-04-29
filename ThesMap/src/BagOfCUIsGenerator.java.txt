package org.apache.ctakes.clinicalpipeline.runtime;

import java.io.IOException;
import java.util.HashSet;

import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.cas.FSArray;

public class BagOfCUIsGenerator extends BagOfAnnotationsGenerator<IdentifiedAnnotation, String> {

	public BagOfCUIsGenerator(String inputDir, String outputDir)
			throws UIMAException, IOException {
		super(inputDir, outputDir);
	}

	@Override
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
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UIMAException 
	 */
	public static void main(String[] args) throws UIMAException, IOException {
		(new BagOfCUIsGenerator("data/input", "data/output")).process();
	}

}
