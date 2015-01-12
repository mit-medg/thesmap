package edu.mit.csail.medg.thesmap;

public class InterpretationMM extends Interpretation {
	
	boolean negated = false;
	int score = 0;

	public InterpretationMM(String cui, String str, String tui, String sty, int neg, int score) {
		super();
		type = AnnotatorMetaMap.name;
		this.cui = cui;
		this.str = str;
		this.tui = tui;
		this.sty = sty;
		this.negated = (neg != 0);
		this.score = score;
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		return super.toShow(indent) + ((negated) ? " NEG " : " ") + score;
	}
	
	public String toString() {
		return super.toString() + ((negated) ? " NEG " : " ") + score;
	}

}
