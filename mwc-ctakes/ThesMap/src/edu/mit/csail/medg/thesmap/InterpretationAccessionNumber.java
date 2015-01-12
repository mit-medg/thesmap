package edu.mit.csail.medg.thesmap;

public class InterpretationAccessionNumber extends InterpretationNumeric {
	
	static final String cuiforAccession = "C2348188";
	static final String tuiforAccession = "T170";
	static final String styforAccession = "Intellectual Product";
	
	
	public InterpretationAccessionNumber(String text) {
		super(cuiforAccession, text, tuiforAccession, styforAccession);
		type = AnnotatorNumeric.name;
	}
	
	public static Interpretation makeInterpretationTime(String text) {
		// Assumes only sc can be null.
		return new InterpretationAccessionNumber(text);
	}
}
