package edu.mit.csail.medg.thesmap;

public class InterpretationWjl extends Interpretation {
	
	boolean negated = false;
	String category = null;

	public InterpretationWjl(String cui, String str, String tui, String sty, boolean neg, String category) {
		super();
		type = AnnotatorParseMed.name;
		this.cui = cui;
		this.str = str;
		this.tui = tui;
		this.sty = sty;
		this.negated = neg;
		this.category = category;
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		return super.toShow(indent) + ((negated) ? " NEG " : " ") + category;
	}
	
	public String toString() {
		return super.toString() + ((negated) ? " NEG " : " ") + category;
	}

}
