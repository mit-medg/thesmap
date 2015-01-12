package edu.mit.csail.medg.thesmap;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnnotatorNumeric extends Annotator{
	
	public static final String name = "Numeric";
	final static int nPhases = 14;
	static ResourceConnectorUnits units;
	
	int phase = 0;
	int oldProgress = 0;
	int progress = 0;
	String text;
	double textl;
	AnnotationSet tempAnnSet;	// Accumulates Numeric Annotations
	
	public AnnotatorNumeric(UmlsWindow w) {
		super(name, w);
		units = ResourceConnectorUnits.get();
		tempAnnSet = new AnnotationSet();
	}
	
	public static AnnotatorNumeric makeAnnotatorInstance(String name, UmlsWindow w) {
		return new AnnotatorNumeric(w);
	}
	
	/**
	 * Checks to see if Annotations of type Numeric can be made because the right
	 * resources are available.  There are no needed resources, so this always 
	 * succeeds.
	 * @return null if OK, an error string if not.
	 */
	public static String errInit() {
		return null;
	}
	/**
	 * This parses the general pattern of data that we have empirically found in clinical text.
	 * 
	 * 1. Optionally a preceding label, e.g., "T=98.9"; the separator can be :, >, <, =, ~, -, or nothing.
	 * 		Spaces are not permitted around the separator, because if there, they would already be
	 * 		interpreted by AnnotatorUmls as a separate token. 
	 * 		the label must start with an alphabetic or %, but can then contain any word class further 
	 * 		characters. However, T97.5 would be interpreted as T=97.5, not T97=.5
	 * 
	 * 2. A number, with optional preceding + or - and optional decimal point.
	 * 
	 * 3. Optionally, a / or -, followed by another number, of the same format.
	 * 
	 * 4. Optionally a unit, optionally preceded by / or -; we exclude * or # from the unit because
	 * 		the EMR producing these sometimes appends them to indicate abnormality or some other feature.
	 * 
	 */
	static final Pattern basicPattern = Pattern.compile(
		       "([A-Za-z%]\\w*)?" 
		       + "(([:<>=~])|(-)\\s+)??"
		       + "([+-]?(\\d*\\.\\d+|\\d+\\.?))"
		       + "(([/+-])(\\d*\\.\\d+|\\d+\\.?))?"
		       + "\\s*(([A-Za-z]+(%|\\d)?)|%)?"
		       + "((\\s*/\\s*(([A-Za-z]+(%|\\d)?)|%)\\b)*)"
		       );
	static final Pattern unitRestPattern = Pattern.compile("\\s*/\\s*(([A-Za-z]+(%|\\d)?)|%)\\b");
	/**
	 * This parses a time pattern of hours:minutes:seconds (seconds is optional), followed by
	 * am or pm (or AM or PM).
	 */
	static final Pattern timePattern =
			Pattern.compile("(?i)\\b(\\d\\d?):(\\d\\d)(:(\\d\\d))?((AM|PM)\\b)?");
	/**
	 * A date pattern of month/day/year. Year may by two or four digits.
	 * As a heuristic, if the date is not valid, we will try interchanging the day and month
	 * to see if that yields a valid date.  This is the case for all the numeric date patterns.
	 */
	static final Pattern datePattern1 = 
			Pattern.compile("\\b(\\d\\d?)/(\\d\\d?)(/(\\d\\d(\\d\\d)?))?\\b");
	/**
	 * A date pattern of year-month-day. Year must be four digits, or else very ambiguous.
	 */
	static final Pattern datePattern2 = 
			Pattern.compile("\\b(\\d\\d\\d\\d)-(\\d\\d?)-(\\d\\d?)\\b");
	/**
	 * A date pattern of month-day-year. Year is four digits or it would match pat2.
	 */
	static final Pattern datePattern3 = 
			Pattern.compile("\\b(\\d\\d?)-(\\d\\d?)-(\\d\\d\\d\\d)\\b");
	/**
	 * A date pattern such as Month day, year. Month must start with one of the three-letter
	 * month name abbreviations but can have additional word characters. Year may be two or four digits.
	 */
	static final Pattern datePattern4 = 
			Pattern.compile("(?i)\\b((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[A-Za-z]*)\\s+(\\d+),\\s+(\\d\\d(\\d\\d)?)\\b");
	/**
	 * A date pattern such as day-month-year or day month year. Month must be one of the three-letter
	 * month name abbreviations. Year may be two or four digits.
	 */
	static final Pattern datePattern5 = 
			Pattern.compile("(?i)\\b(\\d+)(-|\\s+)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(-|\\s+)(\\d\\d(\\d\\d)?)\\b");
	/**
	 * An MGH accession number; this IS rather specialized!
	 */
	static final Pattern mghAccessionPattern = 
			Pattern.compile("(?i)\\b(ml|mn|cg|ms|s|c)-?\\d{2,}-?\\w?-?\\d{3,}\\b");
	/**
	 * A date range of the form 1/2/14-1-9/14, where each date is month/day/year. Year may be 2 or 4 digits.
	 */
	static final Pattern dateRangePattern =
			Pattern.compile("\\b(\\d\\d?)/(\\d\\d?)(/(\\d\\d(\\d\\d)?))?-(\\d\\d?)/(\\d\\d?)/(\\d\\d(\\d\\d)?)\\b");
	/**
	 * A phone number:
	 * Optionally, the type of phone.
	 * Optionally a 1 and optional space or -
	 * Optionally a 3-digit area code, optionally surrounded by (...)
	 * A 3-digit number followed by a 4-digit number, optionally separated by space or -.
	 */
	static final Pattern phonePattern = 
			Pattern.compile("\\b((Phone|Cell|Mobile|Work|Home|Fax):)?(1[ -]?)?"
					+ "((?i)\\b((\\d\\d\\d)[ -]|\\((\\d\\d\\d)\\)\\s*))?(\\d\\d\\d)([ -])?(\\d\\d\\d\\d)"
					+ "(\\s*(X|ext)\\.?(\\d+))");
	/**
	 * A number, written with commas separating groups of three digits. We allow a decimal portion,
	 * with groups of digits also separated by commas.  
	 */
	static final Pattern numCommaPattern = 
			Pattern.compile("\\b[+-]?\\d{1,3}(,\\d\\d\\d)+(\\.(\\d\\d\\d)*\\d{1,3})?\\b");
	/**
	 * A ratio of ranges, e.g., 1-3/0-10. Also allows units, as in the basicPattern.
	 */
	static final Pattern ratioOfRangesPattern =
			Pattern.compile("\\b([+-]?\\d+\\.?|[+-]?\\d*\\.\\d+)-(\\d+\\.?|\\d*\\.\\d+)"
					+ "/([+-]?\\d+\\.?|[+-]?\\d*\\.\\d+)-(\\d+\\.?|\\d*\\.\\d+)([-/])?([a-zA-Z%][^*#]*)?\\b");
	/**
	 * A range of ratios, e.g., 80/60-150/130. Also allows units, as in the basicPattern.
	 */
	static final Pattern rangeOfRatiosPattern =
			Pattern.compile("\\b([+-]?\\d+\\.?|[+-]?\\d*\\.\\d+)/(\\d+\\.?|\\d*\\.\\d+)"
					+ "-([+-]?\\d+\\.?|[+-]?\\d*\\.\\d+)/(\\d+\\.?|\\d*\\.\\d+)([a-zA-Z%][^*#]*?)?\\b");
	/* Matches a token starting with digits, then a ), followed by an
	 * alphabetic and possibly other characters. These are often bullet
	 * points that were missing a space. E.g., "3)tylenol250"
	 */
	static final Pattern enumerationPattern =
			Pattern.compile("\\b(\\d+\\.?)\\)([a-zA-Z].*)\\b");
	
	@Override
	protected Void doInBackground() throws Exception {
//		U.log("Starting to run AnnotatorNumeric.doInBackground()");
		long startTime = System.nanoTime();
		text = myWindow.textArea.getText();
		textl = text.length();
		try {
			Matcher basicMatcher = basicPattern.matcher(text);
			Matcher timeMatcher = timePattern.matcher(text);
			Matcher date1Matcher = datePattern1.matcher(text);
			Matcher date2Matcher = datePattern2.matcher(text);
			Matcher date3Matcher = datePattern3.matcher(text);
			Matcher date4Matcher = datePattern4.matcher(text);
			Matcher date5Matcher = datePattern5.matcher(text);
			Matcher mghMatcher = mghAccessionPattern.matcher(text);
			Matcher dateRangeMatcher = dateRangePattern.matcher(text);
			Matcher phoneMatcher = phonePattern.matcher(text);
			Matcher numCommaMatcher = numCommaPattern.matcher(text);
			Matcher ratioMatcher = ratioOfRangesPattern.matcher(text);
			Matcher rangeMatcher = rangeOfRatiosPattern.matcher(text);
			Matcher enumMatcher = enumerationPattern.matcher(text);
			ArrayList<Annotation> newAnns = new ArrayList<Annotation>();
			int lastGood = 0;	// end position of last valid unit
			// Get my private version of a UmlsAnnotator to look up labels and units.
			while (timeMatcher.find()) {
				/* "(?i)\\b(\\d\\d?):(\\d\\d)(:(\\d\\d))?((AM|PM)\\b)?" */
				reportProgress(timeMatcher);
//				U.log(U.showMatcher(timeMatcher));
				Interpretation theTime = InterpretationTime.makeInterpretationTime(
						timeMatcher.group(1), timeMatcher.group(2), timeMatcher.group(4), timeMatcher.group(6), 
						timeMatcher.group());
				recordInterpretation(timeMatcher.start(), timeMatcher.end(), theTime);
			}
			while (date1Matcher.find()) {
				// "\\b(\\d\\d?)/(\\d\\d?)(/(\\d\\d(\\d\\d)?))?\\b"
				reportProgress(date1Matcher);
//				U.log(U.showMatcher(date1Matcher));
				Interpretation theDate = InterpretationDate.makeInterpretationDate(
						date1Matcher.group(4), date1Matcher.group(1), date1Matcher.group(2), date1Matcher.group());
				recordInterpretation(date1Matcher, theDate);
			}
			while (date2Matcher.find()) {
				// "\\b(\\d\\d\\d\\d)-(\\d\\d?)-(\\d\\d?)\\b"
				reportProgress(date2Matcher);
//				U.log(U.showMatcher(date2Matcher));
				Interpretation theDate = InterpretationDate.makeInterpretationDate(
						date2Matcher.group(1), date2Matcher.group(2), date2Matcher.group(3), date2Matcher.group());
				recordInterpretation(date2Matcher, theDate);
			}
			while (date3Matcher.find()) {
				// "\\b(\\d\\d?)-(\\d\\d?)-(\\d\\d\\d\\d)\\b"
				reportProgress(date3Matcher);
//				U.log(U.showMatcher(date3Matcher));
				Interpretation theDate = InterpretationDate.makeInterpretationDate(
						date3Matcher.group(3), date3Matcher.group(1), date3Matcher.group(2), date3Matcher.group());
				recordInterpretation(date3Matcher, theDate);
			}
			while (date4Matcher.find()) {
				//"(?i)\\b((jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[A-Za-z]*)\\s+(\\d+),\\s+(\\d\\d(\\d\\d)?)\\b"
				reportProgress(date4Matcher);
//				U.log(U.showMatcher(date4Matcher));
				Interpretation theDate = InterpretationDate.makeInterpretationDate(
						date4Matcher.group(4), monthAbbrev(date4Matcher.group(2)), date4Matcher.group(3), 
						date4Matcher.group());
				recordInterpretation(date4Matcher, theDate);
			}
			while (date5Matcher.find()) {
				// "(?i)\\b(\\d+)(-|\\s+)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(-|\\s+)(\\d\\d(\\d\\d)?)\\b"
				reportProgress(date5Matcher);
//				U.log(U.showMatcher(date5Matcher));
				Interpretation theDate = InterpretationDate.makeInterpretationDate(
						date5Matcher.group(5), monthAbbrev(date5Matcher.group(3)), date5Matcher.group(1), 
						date5Matcher.group());
				recordInterpretation(date5Matcher, theDate);
			}			AnnotatorUmls umls = new AnnotatorUmls(myWindow);
			while (basicMatcher.find()) {

//				 "([A-Za-z%]\\w*)?"
//				  1               
//		       + "(([:<>=~])|(-)\\s+)??"
//		          23         4         
//		       + "([+-]?(\\d*\\.\\d+|\\d+\\.?))"
//		          5     6                      
//		       + "(([/+-])(\\d*\\.\\d+|\\d+\\.?))?"
//		          78      9                       
//		       + "\\s*(([A-Za-z]+(%|\\d)?)|%)?"
//		              01         2            
//		       + "((\\s*/\\s*(([A-Za-z]+(%|\\d)?)|%)\\b)*)"
//		          34         56         7
				// Groups:
				// 1. label
				// 3 or 4. relation
				// 5. number
				// 8. separator
				// 9. number2d
				// 10. unit1
				// 13. unitRest
				// All are optional except number.
//				U.log(U.showMatcher(basicMatcher));
				String label = basicMatcher.group(1);
				String relation = basicMatcher.group(3);
				if (relation == null) relation = basicMatcher.group(4);
				String numberString = basicMatcher.group(5);
				Integer numberInt = null;
				Double numberDouble = null;
				if (numberString != null) {
					try {
						numberInt = new Integer(numberString);
					} catch (NumberFormatException e) {}
					try {
						numberDouble = new Double(numberString);
					} catch (NumberFormatException e) {}
				}
				String separator = basicMatcher.group(8);
				String number2String = basicMatcher.group(9);
				Integer number2Int = null;
				Double number2Double = null;
				if (number2String != null) {
					try {
						number2Int = new Integer(number2String);
					} catch (NumberFormatException e) {}
					try {
						number2Double = new Double(number2String);
					} catch (NumberFormatException e) {}
				}
//				String allUnits = basicMatcher.group(10);
				String firstUnit = basicMatcher.group(10);
				String restUnits = basicMatcher.group(13);
				String allNormUnits = null;
				// Remember 
				int endBeforeUnits = basicMatcher.end(7);
				if (endBeforeUnits < 0) endBeforeUnits = basicMatcher.end(5);
//				U.p(basicMatcher.group());
//				if (firstUnit != null && firstUnit.equals("%")) {
//					U.p("Found %!");
//				}
				if (firstUnit != null && okUnit(firstUnit)) {
					lastGood = basicMatcher.end(10);
					StringBuilder sb = new StringBuilder(firstUnit);
					if (restUnits != null) {
						Matcher m = unitRestPattern.matcher(restUnits);
						while (m.find()) {
							String poss = m.group(1);
							if (okUnit(poss)) {
								// Good Unit
								lastGood = basicMatcher.start(13) + m.end();
								sb.append("/").append(poss);
							} else {
								break;
							}
						}
					}
					allNormUnits = sb.toString();
				} else {
					// No units were OK, lastGood is end of last number.
					lastGood = basicMatcher.end(7);
					if (lastGood < 0) lastGood = basicMatcher.end(5);
				}
				reportProgress(basicMatcher);
				int beg = basicMatcher.start();

				// If there is a label, annotate it because it was not tokenized separately
				if (label != null) {
					InterpretationSet is = umls.lookup(label);
					if (is != null && is != InterpretationSet.nullInterpretationSet) {
						myWindow.integrate(
								new Annotation(basicMatcher.start(1), basicMatcher.end(1), label, is));
					}
					beg = (basicMatcher.group(2) != null) ? basicMatcher.start(2) : basicMatcher.start(5);
				} 
				// If there was no label, but there was a relation, we could consider dropping the relation.
				// However, then if the label was tokenized separately, we would also lose the relation.
				
//				U.p("Make " + beg + " to " + lastGood);
				InterpretationNumeric ni = new InterpretationNumeric(text.substring(beg, lastGood));
				ni.label = label;	// Note: we store the label, although it is not in the span!
				ni.int1 = numberInt;
				ni.dbl1 = numberDouble;
				ni.int2 = number2Int;
				ni.dbl2 = number2Double;
				ni.reln = relation;
				ni.sep = separator;
				ni.units = allNormUnits;

				recordInterpretation(beg, lastGood, ni);
			}

		} catch (Exception err) {
			err.printStackTrace(System.err);
		}

		// Integrate the non-redundant annotations.
		for (Annotation a: tempAnnSet) {
			myWindow.integrate(a);
		}
		
		long diff = System.nanoTime() - startTime;
		U.log("AnnotatorNumeric elapsed time (ms): " + diff/1000000);
//		U.debug(myWindow.annSet.toShow());
		return null;
	}
	
	void recordInterpretation(int start, int end, Interpretation interp) {
		if (interp == null || interp == Interpretation.nullInterpretation) return;
		tempAnnSet.integrateNumeric(new Annotation(start, end, interp.str, interp));
	}
	
	void recordInterpretation(Matcher m, Interpretation interp) {
		recordInterpretation(m.start(), m.end(), interp);
	}
	
	void reportProgress(Matcher m) {
		int here = m.start();
		oldProgress = progress;
		// progress fraction is (phase + here/textl) / nPhases 
		progress = (int)Math.round((phase + (new Double(here))/textl) / nPhases * 100.0);
		if (progress == 100) progress = 99;	// Don't allow progress to hit 100% until really done!
		phase++;
		firePropertyChange(name, oldProgress, progress);
	}
	
	static final String[] monthAbbrevs = 
		{"jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
	
	static String monthAbbrev(String matched) {			
		String month = null;
		for (int i = 0; i < monthAbbrevs.length; i++) {
			if (matched.equalsIgnoreCase(monthAbbrevs[i])) {
				month = Integer.toString(i + 1);
				break;
			}
		}
		return month;
	}
	
	static boolean okUnit(String poss) {
		if (poss == null) return false;
		int pl1 = poss.length() - 1;
		return poss.equals("%")
				|| units.lookup(poss)
				|| (poss.endsWith("%") && units.lookup(poss.substring(0, pl1)))
				|| (poss.substring(pl1).matches("[0-9]") 
						&& units.lookup(poss.substring(0, pl1)));
	}
}
