package edu.mit.csail.medg.thesmap;


import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotatorUmlsBase extends Annotator{
	
//	UmlsWindow myWindow = null;
	ResourceConnectorUmlsBase umls = null;
	ResourceConnectorNorm norm = null;
	ResourceConnectorNoise noise = null;
	
	public static final String name = "UMLSBase";

	private static final Pattern spaces = Pattern.compile("[\\W&&[^']]+|$"); //\\s+
	
	public AnnotatorUmlsBase(UmlsWindow w) {
		super(name, w);
		umls = ResourceConnectorUmlsBase.get();
		norm = ResourceConnectorNorm.get();
		noise = ResourceConnectorNoise.get();
	}
	
	public static AnnotatorUmlsBase makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorUmlsBase(w);
	}
	
	/**
	 * Checks to see if Annotations of type umls can be made because the right
	 * resources are available.  As a side-effect, it also caches one of each
	 * such resource.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		ResourceConnectorUmlsBase umls = ResourceConnectorUmlsBase.get();
		ResourceConnectorNorm norm = ResourceConnectorNorm.get();
		ResourceConnectorNoise noise = ResourceConnectorNoise.get();
		boolean badUmls = umls == null || !umls.initialized;
		boolean badNorm = norm == null || !norm.initialized;
		boolean badNoise = noise == null || !noise.initialized;
		if (!badUmls && !badNorm && !badNoise) return null;
		String e = "";
		if (badUmls) e += ((e.length() > 0) ? ", " : "") + "UMLSBase connector";
		if (badNorm)  e += ((e.length() > 0) ? ", " : "") + "LVG/Norm connector";
		if (badNoise)  e += ((e.length() > 0) ? ", " : "") + "NoiseWords connector";
		return "Could not initialize " + e;
	}
	
	/**
	 * Creates annotations on the text of the textArea component of UMLSWindow.
	 * Algorithm:
	 * We more forward by words, as identified when we tokenize by spaces.
	 * At the end of each word, we look back to a maximum of n words to find
	 * phrases that might have interpretations. E.g., for n=3, we consider the
	 * last word, the last two words and the last three words.
	 * We then prune interpretations that are also found from shorter subphrases
	 * of a phrase.  We do this by looking back to phrases that overlap the 
	 * current one. If the current gives the same interpretation
	 * @param phraseLength
	 */

	@Override
	protected Void doInBackground() throws Exception {
		int progress = 0;
		int oldProgress = 0;
		U.log("Starting to run AnnotatorUmls.doInBackground()");
		long startTime = System.nanoTime();
		SpaceRecord sr = new SpaceRecord(myWindow.annSet.phraseLength);
		String text = myWindow.textArea.getText();
		double textl = text.length();
		// Tokenize by spaces
		Matcher m = spaces.matcher(text);
		while (m.find()) {
			int here = m.start();	// next found space beginning
			oldProgress = progress;
			progress = (int)Math.round((new Double(here))/textl*100.0);
			firePropertyChange(name, oldProgress, progress);
//			myWindow.methodChooser.setProgress(name, (int)Math.round((new Double(here))/textl*100.0));
			// end of space = beginning of next word; this doubles analysis of last word (!)
			sr.add(m.end());
			// Consider all phrases starting phraseLength words back from here
			for (int back = 1; back <= myWindow.annSet.phraseLength; back++) {
				int start = sr.getPrevStart(back);
				if (start >= 0) {
					String phrase = text.substring(start, here);
//					U.log("Trying phrase ["+start+","+here+"]: " + phrase);
					InterpretationSet i = lookup(phrase);
					if (i != null && i != InterpretationSet.nullInterpretationSet) {
						// Add these interpretations to the annotations unless they duplicate
						// or cover the same interpretations that are already present.
						// See note in documentation. 
						myWindow.annSet.integrate(new Annotation(start, here, phrase, i));
					}
				}
			}
		}
		long diff = System.nanoTime() - startTime;
		U.log("AnnotatorUmlsBase elapsed time (ms): " + diff/1000000);
//		U.debug(myWindow.annSet.toShow());
		return null;
	}
	
	/**
	 * Look up all the interpretations of the normalized versions
	 * of an unnormalizedPhrase.  We use Umls's lookup to look up
	 * the InterpretationSet of each normalized phrase and combine
	 * them into a single InterpretationSet.  If empty, we return
	 * the designated nullInterpretationSet.
	 * This would be the place to cache this lookup if we chose to do so.
	 * @param unnormalizedPhrase
	 * @return InterpretationSet of all non-redundant interpretations.
	 */
	InterpretationSet lookup(String unnormalizedPhrase) {
		if (unnormalizedPhrase == null) return null;	// Really an error condition
		if (noise.lookup(unnormalizedPhrase)) return null;
		Vector<String> normalizedPhrases = norm.lookup(unnormalizedPhrase);
		if (normalizedPhrases == null) return InterpretationSet.nullInterpretationSet;
		InterpretationSet ans = new InterpretationSet();
//		U.log("Normalized Phrases: " + normalizedPhrases);
		for (int i = 0; i < normalizedPhrases.size(); i++) {
//			U.log(normalizedPhrases.get(i));
			String normalizedPhrase = normalizedPhrases.get(i);
			if (!noise.lookup(normalizedPhrase)) {
				InterpretationSet is = umls.lookupNormalized(normalizedPhrase, name);
//				U.log("IS:" + is.toString());
				ans.add(is);
			}
		}
		if (ans.size() == 0) ans = InterpretationSet.nullInterpretationSet;
		return ans;
	}


}
