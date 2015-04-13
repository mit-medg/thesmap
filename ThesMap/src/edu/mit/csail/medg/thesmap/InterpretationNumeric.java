package edu.mit.csail.medg.thesmap;

public class InterpretationNumeric extends Interpretation {
	
	public String label;
	public Integer int1 = null, int2 = null;
	public Double dbl1 = null, dbl2 = null;
	public String reln = null;
	public String sep = null;
	public String units = null;
	
	static final String cuiForNumber = "C0449788";
	static final String tuiForNumber = "T081";
	static final String styForNumber = "Quantitative Concept";
	
	static String type = AnnotatorNumeric.name;

	/*
	 * A general number can be a complex entity, defined by the patterns in AnnotatorNumeric.
	 * A basic number is represented by the above default CUI, TUI and STY.
	 * If it includes units and/or other information, we also look it up in UMLS to
	 * see if there is a more specific match for it; this is done in AnnotatorNumeric.
	 */
	

	public InterpretationNumeric(String str) {
		this(cuiForNumber, str, tuiForNumber, styForNumber);
	}

	public InterpretationNumeric(String cui, String text, String tui, String sty) {
		super(type);
		this.cui = cui;
		this.str = text;
		this.tui = tui;
		this.sty = sty;
		this.updateTypeBits(type);
	}
	
	public String toShow() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toShow());
		sb.append(": ");
		if (label != null) sb.append("{").append(label).append("}");
		sb.append((reln == null) ? " " : reln);
		if (int1 != null) sb.append(int1);
		else sb.append(dbl1);
		sb.append((sep == null) ? " " : sep);
		if (int2 != null) sb.append(int2);
		else if (dbl2 != null) sb.append(dbl2);
		if (units != null) sb.append(units);
		return sb.toString();
	}
	
	public String toString() {
		return toShow();
	}

}
