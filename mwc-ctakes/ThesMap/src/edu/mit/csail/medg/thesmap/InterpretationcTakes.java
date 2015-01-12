package edu.mit.csail.medg.thesmap;

public class InterpretationcTakes extends Interpretation {
	boolean negated = false;
	double score = 0.0;

	public InterpretationcTakes(String cui, String str, String tui, String sty, boolean neg, double score) {
		super();
		type = AnnotatorcTakes.name;
		this.cui = cui;
		this.str = str;
		this.tui = tui;
		this.sty = sty;
		this.negated = neg;
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
