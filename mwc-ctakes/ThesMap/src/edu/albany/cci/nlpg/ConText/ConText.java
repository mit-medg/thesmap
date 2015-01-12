
package edu.albany.cci.nlpg.ConText;

// Imports
import java.util.*;
import java.util.regex.*;

/**
 * <h1><em>ConText</em> Algorithm Implemented in Java</h1><p>
 * <b>Usage:</b> Call the getConText function with the below information.
 * <p>
 * <ul>
 * 	<li>Provide a sentence to determine the context of.</li>
 * 	<li>
 * 		Create an ArrayList of trigger terms to locate and annotate
 * 		in the sentence. Each list item should be formatted as
 * 		follows, tab-separated:	
 * 		<p><em>trigger-term</em>&nbsp;&nbsp;&nbsp;&nbsp;<em>[TAG]</em>
 * 		<p><em>Note:</em> Trigger terms may be in the form of regular
 * 		expressions. This is useful for a variety of time related
 * 		trigger terms.
 * 		<p>The following tags are available:
 * 		<ul>
 * 			<li>[PSEU] - Pseudo negation trigger</li>
 * 			<li>[PREN] - Pre-negation trigger</li>
 * 			<li>[PREP] - Possible pre-negation trigger</li>
 * 			<li>[POST] - Post-negation trigger</li>
 * 			<li>[POSP] - Possible post-negation trigger</li>
 * 			<li>[EXPO] - Other experiencer trigger</li>
 * 			<li>[HYPT] - Hypothetical temporality trigger</li>
 * 			<li>[HYPS] - Pseudo hypothetical temporality trigger</li>
 * 			<li>[HIST] - Historical temporality trigger</li>
 * 			<li>[HISP] - Pseudo historical temporality trigger</li>
 * 			<li>[TERM] - Termination trigger (end scope)</li>
 * 		</ul>
 * 	</li>
 * 	<li>
 * 		<em>Optional:</em> Create an ArrayList of phrases to locate
 * 		and annotate in the sentence. Each line should contain a
 * 		single phrase to search the sentence for.
 * 	</li>
 * </ul>
 * <p>
 * ConText/NegEx algorithm from Wendy Chapman
 * <p>
 * Based on NegEx implementation by Imre Solti
 * 
 * @author	Ken Burford
 * @author	Imre Solti (sortTerms function)
 */

public class ConText {
	
	/**
	 * Set to true to stop getConText from sorting the list of
	 * triggers and phrases on every call.
	 * <p>
	 * The trigger and phrase lists must be sorted ahead of time
	 * by calling the sortTerms function first, or incorrect results
	 * will be returned.
	 */
	public boolean DISABLE_SORT = false;
	/**
	 * By default, getConText will attempt to correct incorrect phrase
	 * tags, such as [PHRASE=APR]some disease[PHRASE], and fill the
	 * whitespace with a filler character. Set to false to disable this.
	 * <p>
	 * If the phrases already have the proper filler characters,
	 * leaving this enabled will not hurt input, and it will be
	 * properly stripped before output as expected.
	 */
	public boolean FIX_PHRASE_TAGS = true;
	/**
	 * By default, getConText will apply exceptions to scope for certain
	 * trigger terms. Set this to true to disable these exceptions.
	 * <p>
	 * Example: the trigger "previous" has a hard coded scope of one token.
	 */
	public boolean DISABLE_SCOPE_EXCEPTIONS = false;
	/**
	 * By default, the post-processor will not leave trigger tags
	 * in a sentence. Set this variable to true to switch to verbose
	 * mode, which will leave trigger tags.
	 */
	public boolean VERBOSE_MODE = false;
	
	/* The filler character used in trigger terms during processing */
	static final private String FILLER_CHAR = "_";
	/* The minimum size of the keyword used to encapsulate triggers/phrases */
	static final private int BRACKET_INNER_MIN = 4;
	/* The maximum size of the keyword used to encapsulate triggers/phrases */
	static final private int BRACKET_INNER_MAX = 10;

	/**************************************************************************/

	/**
	 * Annotates a sentence based on specified phrases and trigger terms
	 * and runs it through Wendy Chapman's ConText algorithm.
	 * <p>
	 * The resulting sentence has all trigger terms properly annotated
	 * and the specified phrases flagged to indicate their predicted context.
	 * 
	 * @param	sentence	the sentence to annotate
	 * @param	phrase		the phrase to determine the context of
	 * @param	triggers	the list of trigger terms and bracketed identifiers
	 * @return	the sentence will triggers annotated and context flags set
	 */
	public String getConText(String sentence, ArrayList<String> triggers,
							 ArrayList<String> phrases) throws Exception {
		
		ArrayList<String> sortedTriggers = triggers;
		ArrayList<String> sortedPhrases = new ArrayList<String>();
		
		/* Ensure that the XML phrase tags are valid, with proper
		 * attributes. If an attribute is invalid, reset the entire
		 * phrase to the default context. If the tag is valid, 
		 * convert it to an internal tag with the initialized context.
		 * Ex: <phrase neg="possible" exp="other" temp="recent">...</phrase>
		 * will be initialized to [PHRASE=POR]...[PHRASE] */
		if (FIX_PHRASE_TAGS) sentence = fixPhraseTags(sentence);
		
		/* If the calling app won't sort the lists itself.. */
		if (DISABLE_SORT == false) {

			/* Sort trigger terms be length, descending */
			sortedTriggers = sortTerms(triggers);
			/* NOTE: For efficiency, do this operation on the rule list
			 * just once directly from the calling application. */

			/* If we're being passed phrases, sort them by length, descending */
			 if (phrases != null) {
				 sortedPhrases = sortTerms(phrases);
			 }
			 
		}
 
		/* Pre-process sentence and obtain annotated result */
		sentence = prepSentence(sentence, sortedPhrases, sortedTriggers);

		/* Run ConText over the annotated sentence */
		sentence = setContext(sentence);

        /* Perform cleanup by removing underscores from
         * phrases/trigger terms and replacing them with
         * spaces, then trimming the sentence */
        sentence = removeFiller(sentence);
        
        /* If we're not in verbose mode, remove trigger tags */
        if (!VERBOSE_MODE) sentence = removeTriggerTags(sentence);
        
        /* Post-process the sentence and convert our internal tags
         * to XML compliant tags, then return the result */
        sentence = convertToXML(sentence);
		return sentence;
		
	} /* getConText */
	
	/**************************************************************************/
	
	public String getConText(String sentence, ArrayList<String> triggers)
							throws Exception {
		return getConText(sentence, triggers, null);
	} /* getConText */
	
	/**************************************************************************/
	
	/**
	 * Takes a sentence and checks that 
	 */
	private String fixPhraseTags(String sentence) {
		
		/* Convert <phrase> tags to internal tag format with the
		 * ConText specified default context: affirmed, patient, recent
		 * [PHRASE=APR]...[PHRASE] */
		sentence = sentence.replace("<phrase>", "[PHRASE=APR]");
		
		/* Replace the closing tags */
		sentence = sentence.replace("</phrase>", "[PHRASE]");
		
		/* Find all opening phrase tags */
		Pattern pat = Pattern.compile("(?si)<phrase([^>]+?)>");
		Matcher mat = pat.matcher(sentence);
		
		/* For each captured phrase, check to see if it's a valid tag
		 * and repair it if not */
		while (mat.find()) {
			
			String phraseTag = mat.group();
			
			/* Compile pattern to check phrase attributes */
			Pattern pat2 = Pattern.compile("(neg|exp|temp)=\\\"([\\w]+)\\\"");
			Matcher mat2 = pat2.matcher(mat.group(1));
			
			/* Set default ConText */
			String negation = "affirmed";
			String experiencer = "patient";
			String temporality = "recent";
			boolean invalid_tag = false;
			
			/* For each attribute... */
			while (mat2.find()) {
				
				String attrib = mat2.group(1);
				String info = mat2.group(2);
				
				/* For the negation attribute.. */
				if (attrib.equals("neg")) {
					/* If the negation attribute is invalid,
					 * replace the entire phrase tag with the default */
					if (!(info.equals("affirmed") || info.equals("negated") ||
						  info.equals("possible"))) {
						invalid_tag = true;
						break;
					}
					negation = info;
				}
				else if (attrib.equals("exp")) {
					/* If the experiencer attribute is invalid,
					 * replace the entire phrase tag with the default */
					if (!(info.equals("patient") || info.equals("other"))) {
						invalid_tag = true;
						break;
					}
					experiencer = info;
				}
				else if (attrib.equals("temp")) {
					/* If the temporality attribute is invalid,
					 * replace the entire phrase tag with the default */
					if (!(info.equals("recent") || info.equals("hypothetical") ||
						  info.equals("historical"))) {
						invalid_tag = true;
						break;
					}
					temporality = info;
				}
				else {
					invalid_tag = true;
					break;
				}
				
			} /* while */
			
			/* If the tag was determined invalid, set default context */
			if (invalid_tag) sentence = sentence.replace(phraseTag, "[PHRASE=APR]");
			/* Otherwise, apply the new context */
			else {
				
				/* Build the new opening phrase tag */
				if (negation.equals("affirmed")) negation = "A";
				else if (negation.equals("possible")) negation = "P";
				else if (negation.equals("negated")) negation = "N";
				if (experiencer.equals("patient")) experiencer = "P";
				else if (experiencer.equals("other")) experiencer = "O";
				if (temporality.equals("recent")) temporality = "R";
				else if (temporality.equals("hypothetical")) temporality = "H";
				else if (temporality.equals("historical")) temporality = "P";
				String openTag = "[PHRASE=" + negation + experiencer + temporality + "]";
				
				/* Replace the opening tag */
				sentence = sentence.replace(phraseTag, openTag);
				
			}
			
		} /* while */
		
		/* Replace whitespace inside phrase tags with filler
		 * Compile the pattern that will capture between phrase tags */
		pat = Pattern.compile(
			"(\\[PHRASE=[ANP]{1}[PO]{1}[RHP]{1}\\])(.+?)(\\[PHRASE\\])",
			Pattern.DOTALL);
		mat = pat.matcher(sentence);
		
		/* For each captured phrase, insert the filler */
		while (mat.find() == true) {
			sentence = sentence.replaceAll(mat.group(2), 
				mat.group(2).replaceAll(" ", FILLER_CHAR));
		}
		
		return sentence;
		
	} /* fixPhraseTags */
	
	/**************************************************************************/
	
	/**
	 * Prepares a sentence to be run through ConText by annotating
	 * detected triggers and the phrase.
	 * 
	 * @param	sentence	the sentence to be annotated
	 * @param	phrase		the phrase to be tagged
	 * @param	triggers	an arraylist of trigger terms
	 * @return	the fully annotated sentence
	 */
	private String prepSentence(String sentence, ArrayList<String> phrases, 
								ArrayList<String> triggers) {
		
        /* Append space to beginning and end of sentence, trimmed later */
        sentence = " " + sentence + " ";
        
        /* Iterate through the phrase list and tag a phrase with its
         * appropriate tag */
        Iterator<String> thePhrase = phrases.iterator();
        while (thePhrase.hasNext()) {
        	
        	/* Tag the given phrase with [PHRASE=APR]...[PHRASE], while also
    		 * replacing spaces in the phrase with a filler (ie, an underscore) */
        	String phrase = (String) thePhrase.next();
        	sentence = annotateTerm(sentence, phrase, "[PHRASE]");
        	
        }

        /* Iterate through the trigger list and tag a trigger with its
         * appropriate tag */
        Iterator<String> theTrigger = triggers.iterator();
        Pattern patSplit = Pattern.compile("[\\t]+");
        while (theTrigger.hasNext()){
        	
        	/* Split the rule entry into the trigger, 
        	 * and the bracketed identifier */
        	String trigger = (String) theTrigger.next();
        	String[] ruleTokens = patSplit.split(trigger.trim());
        	String triggerTerm = ruleTokens[0];
        	String triggerCapsule = ruleTokens[1];
        	
        	/* Tag the given trigger term with its proper annotation,
        	 * ie: for historical context; [HIST]past history[HIST] */
        	sentence = annotateTerm(sentence, triggerTerm, triggerCapsule);

        }
        
        return sentence.trim();
		
	} /* prepSentence */
	
	/**************************************************************************/
	
	/**
	 * Applies the ConText algorithm to an annotated sentence
	 * and updates all tagged phrases with their deduced context.
	 * 
	 * @param	sentence	the sentence to determine the context of
	 * @return	the annotated sentence complete with updated context flags
	 */
	private String setContext(String sentence) {
		
		/* Pattern to match [XXXX] */
		String triggerPattern =
			"(?s).*\\[[A-Z]{" + BRACKET_INNER_MIN + "}\\].*";	
		
        /* Tokenize the processed sentence looking for trigger terms */
        String[] tokens = sentence.split("[ ]+");
        List<String> theList = Arrays.asList(tokens);
        ArrayList<String> tokenList = new ArrayList<String>(theList);
        for (int x = 0; x < tokenList.size(); x++) {
        	
        	/* Get the token and check if it's a trigger term */
        	String trigger = tokenList.get(x).trim();
        	if (trigger.matches(triggerPattern) &&
        		 !trigger.contains("[PSEU]") &&
        		 !trigger.contains("[HYPS]") &&
        		 !trigger.contains("[HISP]") &&
        		 !trigger.contains("[PHRASE") &&
        		 !trigger.contains("[TERM]")) {
        		
        		
        		/* If this is a post-negation trigger, enter scope and go left */
        		if (trigger.contains("[POST]") || trigger.contains("[POSP]")) {
        			for (int y = x-1; y >= 0; y--) {
        				
        				String result = whileInScope(sentence, y, tokenList, trigger);
        				if (result == null) break;
        				else sentence = result;
        				
        			}
        		}
        		/* Otherwise, if this is anything other than post-negation,
        		 * enter scope and go right */
        		else {
        			for (int y = x+1; y < tokenList.size(); y++) {
        				
        				String result = whileInScope(sentence, y, tokenList, trigger);
        				if (result == null) break;
        				else sentence = result;
        				
        				/* If scope exceptions are enabled */
        				if (DISABLE_SCOPE_EXCEPTIONS == false) {
        					/* If token is "previous," break scope after one token */
        					if (trigger.matches("(?si)\\[HIST\\]previous\\[HIST\\]")) break;
        				}
        				
        			} /* for */
        		}
        		
        	}
        	
        } /* for */
		
        return sentence;
        
	} /* setContext */
	
	/**************************************************************************/
	
	/**
	 * Performs operations over a sentence while in scope during
	 * the ConText search, including applying new context flags.
	 * 
	 * @param	sentence		the sentence to update with new context flags
	 * @param	currentToken	the place in the arraylist of the current token
	 * @param	sentenceTokens	the list of sentence tokens
	 * @param	trigger			the trigger that brought ConText in scope
	 * @return	the resulting sentence from updating flags in scope, or
	 * 			null if a termination term is encountered
	 */
	private String whileInScope(String sentence, int currentToken,
								ArrayList<String> sentenceTokens, String trigger) {
		
		/* Get the next token in the sentence */
		String nextToken = sentenceTokens.get(currentToken);
		
		/* End scope if termination term occurs */
		if (nextToken.trim().contains("[TERM]")) {
			return null;
		}
		/* Set new context if phrase occurs */
		else if (nextToken.trim().contains("[PHRASE")) {
			
			/* Get the position of the tag */
			int tokenTagPos = nextToken.trim().indexOf("[");
			int trigTagPos = trigger.indexOf("[");
			
			/* Extract the tag type */
			String trigType = trigger.substring(trigTagPos, trigTagPos+6);
			nextToken = nextToken.trim().substring(tokenTagPos);
			String newContext = updateFlag(nextToken, trigType);
			
			/* Escape brackets to allow regex literal matching of trigger */
			String cleaned = 
				nextToken.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
			sentence = sentence.replaceAll(cleaned, newContext);
			
			/* Search all tokens and replace all matching phrases */
			for (int z = 0; z < sentenceTokens.size(); z++) {
				if (sentenceTokens.get(z).contains(nextToken)) {
					sentenceTokens.set(z, newContext);
				}
			} /* for */
			
		}
		
		return sentence;
		
	} /* whileInScope */
	
	/**************************************************************************/
	
	/**
	 * Annotates a specified term in a sentence (for pre-processing).
	 * 
	 * @param	sentence	the sentence to apply the annotation to
	 * @param	term		the term to annotate
	 * @param	termType	the type of the term (ie, "[PHRASE]", "[HIST]", etc)
	 * @return	the sentence with the applied annotation
	 */
	private String annotateTerm(String sentence, String term, String termType) {

		/* Initialize open and close strings */
		String open = termType;
		String close = termType;
		
		/* If this is a phrase type, add the flags */
		if (termType.equals("[PHRASE]")) {
			open = "[PHRASE=APR]";
		}
	
		/* Strip additional whitespace from phrase */
		term = term.replaceAll("[\\s]+", " ");
		
		/* Modifies the straight term into a regular expression that
		 * will still match the given phrase if there are non-alphanumeric
		 * characters between words, and also accommodates extra spaces. */
		term = term.replaceAll(" ", "[\\\\W]+");
		
		/* Tag the given term with [XXXX]...[XXXX], while also replacing
		 * spaces in the phrase with a filler (ie, an underscore) */
        Pattern pat = 
        	Pattern.compile("(?i)([\\s,.;:\'\\\"]+)(" + term + ")([\\s,.;:\'\\\"]+)");
        Matcher mat = pat.matcher(sentence);
        while (mat.find() == true) {
        	sentence = 
        		mat.replaceAll(mat.group(1) + open + 
        				mat.group(2).replaceAll(" ", FILLER_CHAR) + 
        				close + mat.group(3));
        }
        
        return sentence;
		
	} /* annotateTerm */
	
	/**************************************************************************/
	
	/**
	 * Strips underscores from annotated triggers and phrases in a sentence.
	 * 
	 * @param	sentence	the sentence to perform cleanup
	 * @return	the cleaned up sentence
	 */
	private String removeFiller(String sentence) {
		
		/* Pattern which matches any trigger term or phrase
		 * and captures the string inside */
		String bracketPattern = 
			"(?s)\\[[A-Z=]{" + BRACKET_INNER_MIN + "," +
			BRACKET_INNER_MAX + "}\\](.+?)\\[[A-Z=]{" +
			BRACKET_INNER_MIN + "," + BRACKET_INNER_MAX + "}\\]";
		
		Pattern pat = Pattern.compile(bracketPattern);
        Matcher mat = pat.matcher(sentence);
        while (mat.find() == true) {
        	String toClean = mat.group(1);
        	sentence = sentence.replaceAll(toClean, 
        			toClean.replaceAll(FILLER_CHAR, " "));
        }
        
		return sentence;
        
	} /* removeFiller */
	
	/**************************************************************************/
	
	/**
	 * Take a sentence that's been pre-processed and manipulated by ConText
	 * and convert the internal tagging scheme back into valid XML.
	 * 
	 * @param	sentence	the sentence to post-process
	 * @return	the post-processed sentence
	 */
	private String convertToXML(String sentence) {
		
		String triggerName = "";
		String xmlError = "Error: XML tag construct failed: ";
		
		/* Replace opening phrase tags with XML form */
		Pattern pat = Pattern.compile("\\[PHRASE=(A|P|N)(P|O)(R|H|P)\\]");
		Matcher mat = pat.matcher(sentence);
		while (mat.find() == true) {
			
			/* Get the symbols for the phrase context */
			String negation = mat.group(1);
			String experiencer = mat.group(2);
			String temporality = mat.group(3);
			String oldTag = mat.group();
			
			/* Convert the context symbol to a proper term */
			if (negation.equals("A")) negation = "affirmed";
			else if (negation.equals("N")) negation = "negated";
			else if (negation.equals("P")) negation = "possible";
			else {
				System.out.println(xmlError);
				System.exit(-1);
			}
			if (experiencer.equals("P")) experiencer = "patient";
			else if (experiencer.equals("O")) experiencer = "other";
			else {
				System.out.println(xmlError);
				System.exit(-1);
			}
			if (temporality.equals("R")) temporality = "recent";
			else if (temporality.equals("H")) temporality = "hypothetical";
			else if (temporality.equals("P")) temporality = "historical";
			else {
				System.out.println(xmlError);
				System.exit(-1);
			}
			
			/* Build the XML tag */
			String xmlTag = "<phrase neg=\"" + negation + 
				"\" exp=\"" + experiencer + "\" temp=\"" +
				temporality + "\">";
			
			/* Swap in the new XML tag */
			sentence = sentence.replace(oldTag, xmlTag);
			
		} /* while */
		
		/* Replace all closing phrase tags */
		sentence = sentence.replaceAll("\\[PHRASE\\]", "</phrase>");
		
		/* If we're in verbose mode, trigger tags are still in place,
		 * so convert them to XML as well */
		if (VERBOSE_MODE) {
			
			pat = Pattern.compile("(?s)\\[([A-Z]{4})\\](.+?)\\[[A-Z]{4}\\]");
			mat = pat.matcher(sentence);
			while (mat.find()) {
				
				/* Captures */
				String fullTagSet = mat.group();
				String triggerTag = mat.group(1);
				String insideTag = mat.group(2);
				
				/* Take the shorthand for the trigger type and convert
				 * it to the proper XML string name */
				if (triggerTag.equals("PSEU")) triggerName = "pseudo-negation";
				else if (triggerTag.equals("PREN")) triggerName = "pre-negation";
				else if (triggerTag.equals("PREP")) triggerName = "possible-pre-negation";
				else if (triggerTag.equals("POST")) triggerName = "post-negation";
				else if (triggerTag.equals("POSP")) triggerName = "possible-post-negation";
				else if (triggerTag.equals("EXPO")) triggerName = "other";
				else if (triggerTag.equals("HYPT")) triggerName = "hypothetical";
				else if (triggerTag.equals("HYPS")) triggerName = "pseudo-hypothetical";
				else if (triggerTag.equals("HIST")) triggerName = "historical";
				else if (triggerTag.equals("HISP")) triggerName = "pseudo-historical";
				else if (triggerTag.equals("TERM")) triggerName = "conjunction";
				/* Exception: EXOH -- Immediately construct double tag set */
				else if (triggerTag.equals("EXOH")) {
					sentence = sentence.replace(fullTagSet, 
						"<historical><other>" + insideTag + 
						"</other></historical>");
					continue;
				}
				else {
					System.out.println(xmlError + triggerTag);
					System.exit(-1);
				}
				
				/* Perform the XML conversion */
				sentence = sentence.replace(fullTagSet, 
					"<" + triggerName + ">" + insideTag + "</" + triggerName + ">");
				
			} /* while */
			
		}
		
		return sentence;
		
	} /* convertToXML */
	
	/**************************************************************************/
	
	/**
	 * Remove all trigger tags from a given sentence.
	 * 
	 * @param	sentence	the sentence to strip
	 * @return	the cleaned sentence
	 */
	private String removeTriggerTags(String sentence) {
		
		/* Capture from inside a set of trigger tags */
		Pattern pat = Pattern.compile("(?s)\\[[A-Z]{4}\\](.+?)\\[[A-Z]{4}\\]");
		Matcher mat = pat.matcher(sentence);
		while (mat.find()) {
			
			/* Replace the entire match region (tags) with the inside
			 * that's been captured */
			sentence = sentence.replace(mat.group(), mat.group(1));
			
		}
		
		return sentence;
		
	} /* removeTriggerTags */
	
	/**************************************************************************/
	
	/**
	 * Updates the context flag of a phrase, given a specific trigger term.
	 * 
	 * @param	phrase	the isolated phrase to update the flags of
	 * @param	trigger	the trigger term to set flag based on
	 * @return	the phrase with updated context flags
	 */
	private String updateFlag(String phrase, String trigger) {
		
		/* If negation trigger, set negated flag */
		if (trigger.contains("[PREN]") || trigger.contains("[POST]")) {
			/* Sets phrase from [PHRASE=AXX] to [PHRASE=NXX] */
			return phrase.substring(0, 8) + "N" + phrase.substring(9);
		}
		/* If possible negation trigger, set possible neg flag */
		else if (trigger.contains("[PREP]") || trigger.contains("[POSP]")) {
			/* Check to see if the negation flag is already set */
			if (!phrase.substring(8, 9).equals("N")) {
				/* Sets phrase from [PHRASE=AXX] to [PHRASE=PXX] */
				return phrase.substring(0, 8) + "P" + phrase.substring(9);
			}
			/* If negation is already confirmed, don't touch the flags */
			else return phrase;
		}
		/* If experiencer is not the patient, set other flag */
		else if (trigger.contains("[EXPO]")) {
			/* Sets phrase from [PHRASE=XPX] to [PHRASE=XOX] */
			return phrase.substring(0, 9) + "O" + phrase.substring(10);
		}
		/* If the temporality is hypothetical, set hypothetical flag */
		else if (trigger.contains("[HYPT]")) {
			/* Sets phrase from [PHRASE=XXR] to [PHRASE=XXH] */
			return phrase.substring(0, 10) + "H" + phrase.substring(11);
		}
		/* If the temporality is historical, set the flag */
		else if (trigger.contains("[HIST]")) {
			return phrase.substring(0, 10) + "P" + phrase.substring(11);
		}
		else if (trigger.contains("[EXOH]")) {
			/* Sets phrase from [PHRASE=XPR] to [PHRASE=XOP] */
			return phrase.substring(0, 9) + "OP" + phrase.substring(11);
		}

		System.out.println("Error: Invalid trigger detected: " + trigger);
		System.exit(-1);
		return null;

	} /* updateFlag */
	
	/**************************************************************************/
	
	/**
	 * Takes a list of unsorted terms and organizes them by length
	 * in descending order, so that longer terms will be matched first.
	 * 
	 * @param	unsortedTerms	an unsorted list of terms to reorder
	 * @return	the original list of terms organized by length, descending
	 */
	public ArrayList<String> sortTerms(ArrayList<String> unsortedTerms) {
		
		try {
			/* Sort the negation rules by length to make sure 
			 * that longest rules match first. */
			for (int i = 0; i<unsortedTerms.size()-1; i++) {
				for (int j = i+1; j<unsortedTerms.size(); j++) {
					String a = (String) unsortedTerms.get(i);
					String b = (String) unsortedTerms.get(j);
					if (a.trim().length()<b.trim().length()) {
						/* Sorting into descending order by length of string. */
						unsortedTerms.set(i, b);
						unsortedTerms.set(j, a);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println(e);
		}
		
		return unsortedTerms;
		
	} /* sortTerms */
	
} /* ConText */


