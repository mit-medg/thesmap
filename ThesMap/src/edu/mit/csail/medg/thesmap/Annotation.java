package edu.mit.csail.medg.thesmap;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * An Annotation is an InterpretationSet with the character
 * offsets of the start and end of the phrase to which it applies.
 * @author psz
 *
 */
public class Annotation {

	public Integer begin, end;
	public String text; 
	public InterpretationSet interpSet;

	public Annotation(int begin, int end, String text, InterpretationSet i) {
		this.begin = new Integer(begin);
		this.end = new Integer(end);
		this.text = text;
		this.interpSet = i;
	}
	
	public Annotation(int begin, int end, String text, Interpretation i) {
		this(begin, end, text, new InterpretationSet());
		interpSet.add(i);
	}
	
	public InterpretationSet getInterpretationSet() {
		return interpSet;
	}
	
	public boolean isNull() {
		return interpSet.isNull();
	}
	
	/**
	 * Adds the Interpretations in its argument to those in the Annotation,
	 * checking that the start and end positions are compatible.
	 * @param ann
	 */
	public void add(Annotation ann) {
		if (begin.equals(ann.begin) && end.equals(ann.end)) interpSet.add(ann.interpSet);
		else System.err.println("Error trying to add Annotation "+ann+" to "+this+"; non-matching spans!");
	}
	
	public int length() {
		return end - begin;
	}
	
	public boolean matchesTui(String tui) {
		return interpSet.matchesTui(tui);
	}

	public boolean matchesTui(ArrayList<String> tuis) {
		return interpSet.matchesTui(tuis);
	}
	
	public boolean matchesType(ArrayList<String> types) {
		return interpSet.matchesType(types);
	}
	
	public boolean matchesTuiAndType(ArrayList<String> tuis, ArrayList<String> types) {
		return interpSet.matchesTui(tuis) && interpSet.matchesType(types);
	}
	
	public Annotation matchingTui(String tui) {
		if (tui == null) return this;
		return new Annotation(begin, end, text, interpSet.matchingTui(tui));
	}
	
	public Annotation matchingTui(ArrayList<String> tuis) {
		if (tuis == null) return this;
		return new Annotation(begin, end, text, interpSet.matchingTui(tuis));
	}
	
	public Annotation matchingTuiAndType(ArrayList<String> tuis, ArrayList<String> types) {
		if (tuis == null && types == null) return this;
		Annotation ans = new Annotation(begin, end, text, interpSet.matchingTuiAndType(tuis, types));
		// Error checks.  None of the answers should match any other types.
		// This actually destroys annotationTypes!!!
//		Set<String> otherTypes = Annotator.annotationTypes.keySet();
//		for (String type: types) otherTypes.remove(type);
//		ArrayList<String> ot = new ArrayList<String>();
//		for (String s: otherTypes) ot.add(s);
//		if (ans.matchesType(ot)) U.log("*** Annotation should not match types " + otherTypes + "\n" + ans);
		return ans;
	}
	
	public BitSet typeBits() {
		Annotator.checkStatic("Start of Annotation.typeBits: " + this);
		return interpSet.typeBits();
	}
	
	public void show() {
		U.log(this.toShow());
	}
	
	public String toShow() {
		return toShow(0);
	}
	
	public String toShow(int indent) {
		StringBuilder sbs = new StringBuilder();
		for (int i = 0; i < indent; i++) sbs.append(" ");
		String leadingSpaces = sbs.toString();
		StringBuilder sb = new StringBuilder(leadingSpaces + "Annotation [");
		sb.append(begin); sb.append(","); sb.append(end); sb.append("] "); sb.append(text);
		sb.append("\n");
		sb.append(interpSet.toShow(indent + 3));
		return sb.toString();
	}
	
	public String toString() {
		return "A<[" + begin + "," + end + "] " + interpSet + ">";
	}
}
