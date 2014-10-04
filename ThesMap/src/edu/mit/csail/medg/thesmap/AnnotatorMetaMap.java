package edu.mit.csail.medg.thesmap;

import gov.nih.nlm.nls.metamap.AcronymsAbbrevs;
import gov.nih.nlm.nls.metamap.ConceptPair;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Annotator using MetaMap. 
 * This code is inspired by 
 * 1. MetaMap's MetaMapApiTest.java
 * 2. http://gate.ac.uk/gate/plugins/Tagger_MetaMap/src/gate/metamap/MetaMapPR.java
 * 
 */
public class AnnotatorMetaMap extends Annotator{

	UmlsWindow myWindow = null;
	int phraseLength;
	ResourceConnectorMetaMap mm = null;
	ResourceConnectorUmls umls = null;

	public static final String name = "MetaMap";

	public AnnotatorMetaMap(UmlsWindow w) {
		super(name, w);
		myWindow = w;
		phraseLength = ThesMap.getInteger("phraseLength");
		mm = ResourceConnectorMetaMap.get();
		umls = ResourceConnectorUmls.get();
	}

	public static AnnotatorMetaMap makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorMetaMap(w);
	}

	/**
	 * Checks to see if Annotations of type umls can be made because the right
	 * resources are available.  As a side-effect, it also caches one of each
	 * such resource.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		ResourceConnectorMetaMap mm = ResourceConnectorMetaMap.get();
		if (mm == null || !mm.initialized)
			return "Could not initialize MetaMap connector.";
		else return null;
	}

	/**
	 * Processes the text via MetaMap and creates Interpretations based on the results.
	 * 
	 * We adopt the method of splitting the input text into "paragraphs" (separated by
	 * blank lines) from the GATE plugin, in order to support updating the progress
	 * bar.  This may be a silly idea.  Otherwise, we could just process the entire
	 * text as one input.  However, even though --blanklines 1000000 does the latter,
	 * there is a bug in the specification of the API so that one cannot recover the
	 * exact offsets of annotations found by MetaMap.  So we do use the GATE method.
	 */
	@Override
	protected Void doInBackground() throws Exception {
//		U.log("Starting to run AnnotatorMetaMap.doInBackground()");
		long startTime = System.nanoTime();
		firePropertyChange(name, 0, -1);
		process(myWindow.textArea.getText());
		long diff = System.nanoTime() - startTime;
		U.log("AnnotatorMetaMap elapsed time (ms): " + diff/1000000);
		return null;
	}

	/**
	 * Process text using MetaMap API and create Annotations.
	 *
	 * @param text the input text
	 */
	void process(String text) throws Exception	{

		// These are for building debugging output for the (optional) log that is based on MetaMap's 
		// MetaMapApiTest code.
		StringBuilder sb;
		String sep = "";

		// This patterns is supposedly equivalent to what MetaMap uses to break up a long input
		// text into processing chunks, namely paragraphs.  We pre-run this in order to be able
		// to process those paragraphs independently, so we can translate MetaMap's offsets as 
		// actual character positions in the text.
		// Group 0: whole pattern
		// 1: leading spaces and control characters (?)
		// 3: content, needs end trimmed
		// We move ahead by size of whole pattern.
		Pattern pattern = Pattern.compile("(?s)((\\p{Space}|\\p{Cntrl})*)(.+?)(\n([\\s]*\n)+|$)");
		int textl = text.length();
		Matcher m = pattern.matcher(text);
		int currentMatchStart = 0;
		int st;	// Start and End of current chunk, counting from beginning of text.
		// To report percentage progress
		int percent = 0;
		int oldPercent = 0;

		while(m.find(currentMatchStart)) {
			currentMatchStart = m.end();
			st = m.start(3);
			String chunk = m.group(3);
			// Report progress
			oldPercent = percent;
			percent = 100 * st / textl;
			firePropertyChange(name, oldPercent, percent);
			
			// Hand this chunk off to MetaMap
			List<Result> resultList = (chunk.trim().length() == 0) ? new ArrayList<Result>() : mm.process(chunk);
			if (resultList.size() > 1) U.log(resultList.size() + " results for " + chunk);
			for (Result result: resultList) {
				if (result != null) {
					log("input text: ");
					log(" " + result.getInputText());
					List<AcronymsAbbrevs> aaList = result.getAcronymsAbbrevsList();
					if (aaList.size() > 0) {
						log("Acronyms and Abbreviations:");
						for (AcronymsAbbrevs e: aaList) {
							log("Acronym: " + e.getAcronym());
							log("Expansion: " + e.getExpansion());
							log("Count list: " + e.getCountList());
							log("CUI list: " + e.getCUIList());
						}
					}
					List<Negation> negList = result.getNegationList();
					if (negList.size() > 0) {
						log("Negations:");
						for (Negation e: negList) {
							log("type: " + e.getType());
							sb = new StringBuilder("Trigger: ");
							sep = "";
							sb.append(e.getTrigger()).append(": [");
							for (Position pos: e.getTriggerPositionList()) {
								sb.append(sep).append(pos);
								sep = ",";
							}
							sb.append("]");
							log(sb.toString());
							sb = new StringBuilder("ConceptPairs: [");
							sep = "";
							for (ConceptPair pair: e.getConceptPairList()) {
								sb.append(sep).append(pair);
							}
							sb.append("]");
							log(sb.toString());
							sb = new StringBuilder("ConceptPositionList: [");
							sep = "";
							for (Position pos: e.getConceptPositionList()) {
								sb.append(sep).append(pos);
								sep = ",";
							}
							sb.append("]");
							log(sb.toString());
						}
					}
					for (Utterance utterance: result.getUtteranceList()) {
						log("Utterance:");
						log(" Id: " + utterance.getId());
						log(" Utterance text: " + utterance.getString());
						log(" Position: " + utterance.getPosition());

						for (PCM pcm: utterance.getPCMList()) {
							log("Phrase:");
							log(" text: " + pcm.getPhrase().getPhraseText());
							log(" Minimal Commitment Parse: " + pcm.getPhrase().getMincoManAsString());
							log("Candidates:");

							for (Ev ev: pcm.getCandidatesInstance().getEvList()) {
								log(" Candidate:");
								log("  Score: " + ev.getScore());
								log("  Concept Id: " + ev.getConceptId());
								log("  Concept Name: " + ev.getConceptName());
								log("  Preferred Name: " + ev.getPreferredName());
								log("  Matched Words: " + ev.getMatchedWords());
								log("  Semantic Types: " + ev.getSemanticTypes());
								log("  MatchMap: " + ev.getMatchMap());
								log("  MatchMap alt. repr.: " + ev.getMatchMapList());
								log("  is Head?: " + ev.isHead());
								log("  is Overmatch?: " + ev.isOvermatch());
								log("  Sources: " + ev.getSources());
								log("  Positional Info: " + ev.getPositionalInfo());
								log("  Pruning Status: " + ev.getPruningStatus());
								log("  Negation Status: " + ev.getNegationStatus());
							}

							log("Mappings:");
							for (Mapping map: pcm.getMappingList()) {
								log(" Map Score: " + map.getScore());
								for (Ev mapEv: map.getEvList()) {
									log("   Score: " + mapEv.getScore());
									log("   Concept Id: " + mapEv.getConceptId());
									log("   Concept Name: " + mapEv.getConceptName());
									log("   Preferred Name: " + mapEv.getPreferredName());
									log("   Matched Words: " + mapEv.getMatchedWords());
									log("   Semantic Types: " + mapEv.getSemanticTypes());
									log("   MatchMap: " + mapEv.getMatchMap());
									log("   MatchMap alt. repr.: " + mapEv.getMatchMapList());
									log("   is Head?: " + mapEv.isHead());
									log("   is Overmatch?: " + mapEv.isOvermatch());
									log("   Sources: " + mapEv.getSources());
									log("   Positional Info: " + mapEv.getPositionalInfo());
									log("   Pruning Status: " + mapEv.getPruningStatus());
									log("   Negation Status: " + mapEv.getNegationStatus());
									// Now create the Annotation(s)
									// Create an Annotation for each Mapping (Event) 
									// The InterpretationSet may have multiple Interpretations because CUI may map to
									// multiple TUIs
									InterpretationSet is = new InterpretationSet();
									String cui = mapEv.getConceptId();
									for (String tui: umls.cui2tui(cui)) {
										is.add(new InterpretationMM(cui, mapEv.getPreferredName(), tui,
												SemanticEntity.sems.get(tui).name, 
												mapEv.getNegationStatus(), mapEv.getScore()));
									}
									if (is.size() > 0) {
										// These positions are relative to the start of the chunk
										// We need to add in the position of the chunk start
										List<Position> posn = mapEv.getPositionalInfo();
										Position pos = posn.get(0);
										int startPos = st + pos.getX();
										int endPos = startPos + pos.getY();
										Annotation ann = new Annotation(startPos, endPos, 
												text.substring(startPos, endPos), is);
										log("Adding Annotation " + ann.toShow());
										myWindow.annSet.integrate(ann);
									}

								}
							}
						}
					}
				} else {
					log("NULL result instance! ");
				}
			}
		}
	}
	
	static void log(String s) {
		if (doLog) U.log(s);
	}
	
	static final boolean doLog = false; 
}
