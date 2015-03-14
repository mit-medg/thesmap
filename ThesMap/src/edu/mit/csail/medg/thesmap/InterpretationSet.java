package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Am InterpretationSet holds a set of possible Interpretations of a phrase.
 * 
 * @author psz
 *
 */
public class InterpretationSet {
	
	public ArrayList<Interpretation> interpretations = new ArrayList<Interpretation>();
	
	public static final InterpretationSet nullInterpretationSet = new InterpretationSet();
	
	public InterpretationSet() {
		
	}
	
	public InterpretationSet(InterpretationSet old) {
		this();
		add(old);
	}
	
	/**
	 * Adds an Interpretation to this InterpretationSet if it is not the
	 * null InterpretatonSet and if it is not already contained in the set.
	 * @param i
	 */
	public void add(Interpretation i) {
//		U.log("Trying to add <" + i.toString() + "> to ("+this.size()+")<" + this.toString() + ">" );
		
		if (this == nullInterpretationSet)
			System.err.println("Error adding Interpretation "+i+" to nullInterpretationSet!");
		else if (i != Interpretation.nullInterpretation && !contains(i)) {
			// If the same CUI and TUI, just update the interpretation.
			if (!matchesCuiTui(i)) {
				interpretations.add(i);
			}
		}
//		U.log("Result is (" + this.size() + ")<" + this.toString() + ">" );
		
	}
	
	public void add(InterpretationSet s) {
		for (Interpretation i: s.interpretations) add(i); 
	}
	
	public void remove(Interpretation i) {
		interpretations.remove(i);
	}
	
	public void remove(InterpretationSet is) {
		for (Interpretation i: is.interpretations) remove(i);
	}
	
	/**
	 * Update an existing Interpretation with the new type if it has the same TUI and CUI.
	 * @param i
	 * @return
	 */
	public boolean matchesCuiTui(Interpretation i) {
		for (Interpretation existing: interpretations) {
			if (existing.matchesCuiTui(i.cui, i.tui)) {
				existing.updateTypeBits(i.type);
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(Interpretation i) {
//		U.log("Is <" + i.toString() + "> contained in \n"+this.toShow() + "?");
		for (Interpretation existing: interpretations) {
			if (existing.equals(i)) {
//				U.log("YES");
				return true;
			}
		}
//		U.log("NO");
		return false;
	}
	
	public int size() {
		return interpretations.size();
	}
	
	public boolean isNull() {
		return (this == nullInterpretationSet);
	}
	
	public ArrayList<Interpretation> getInterpretations() {
		return interpretations;
	}
	
	/**
	 * Determines whether any Interpretation in the set matches the given tui
	 * @param tui
	 * @return boolean whether any Interpretation in the set matches the given tui
	 */
	public boolean matchesTui(String tui) {
		for (Interpretation i: interpretations) 
			if (i.matchesTui(tui)) return true;
		return false;
	}
	
	/**
	 * Determines whether any Interpretation in the set matches any of the given tuis
	 * If tuis is null, it is said to match.
	 * @param tuis
	 * @return boolean whether any tuis were matched
	 */
	public boolean matchesTui(ArrayList<String> tuis) {
		for (Interpretation i: interpretations) 
			if (i.matchesTui(tuis)) return true;
		return false;
	}
	
	public boolean matchesType(ArrayList<String> types) {
		for (Interpretation i: interpretations)
			if (i.matchesType(types)) return true;
		return false;
	}
	
	/**
	 * Creates a subset of the InterpretationSet holding only Interpretations matching tui
	 * @param tui
	 * @return InterpretationSet of Interpretations matching tui
	 */
	public InterpretationSet matchingTui(String tui) {
		ArrayList<String> tuis = new ArrayList<String>();
		tuis.add(tui);
		return matchingTui(tuis);
	}
	
	/**
	 * Creates a subset of the InterpretationSet holding only Interpretations matching any of tuis.
	 * It tuis is null, all are considered to match. 
	 * @param tui
	 * @return InterpretationSet of Interpretations matching tuis
	 */
	public InterpretationSet matchingTui(ArrayList<String> tuis) {
		if (tuis == null) return this;
		InterpretationSet is = new InterpretationSet();
		for (Interpretation i: interpretations) {
			if (i.matchesTui(tuis)) is.add(i);
		}
		return (is.size() == 0) ? nullInterpretationSet : is;
	}
	
	public InterpretationSet matchingTuiAndType(ArrayList<String> tuis, ArrayList<String> types) {
		InterpretationSet is = new InterpretationSet();
		for (Interpretation i: interpretations) {
			if (i.matchesTuiAndType(tuis, types)) is.add(i);
		}
		return (is.size() == 0) ? nullInterpretationSet : is;
	}
	
	public BitSet typeBits() {
		BitSet ans = new BitSet();
		for (Interpretation i: interpretations) {
//			Annotator.checkStatic("Checking in InterpretationSet " + this);
			BitSet ib = i.typeBits();
			ans.or(ib);
		}
		return ans;
	}
	
	public String toShow(int indent) {
		return toShow(null, null, indent);
	}

	public String toShow() {
		return toShow(null, null, 0);
	}
	
	/**
	 * Returns a String that shows the content of this InterpretationSet,
	 * but including only Interpretations whose TUIs are included in tuis.
	 * If tuis is null, we include all. indent gives indentation
	 * @param tuis the set of TUIs to include, or null for all
	 * @param indent number of characters to indent at each line
	 * @return the description of the InterpretationSet
	 */
	public String toShow(ArrayList<String> tuis, ArrayList<String> types, int indent) {
		StringBuilder sbs = new StringBuilder();
		for (int i = 0; i < indent; i++) sbs.append(" ");
//		String leadingSpaces = sbs.toString();
		boolean gotOne = false;
		StringBuilder sb = new StringBuilder();
		for (Interpretation i: interpretations) {
//			U.log("Match " + tuis + " and " + types + " against " + i);
			if (i.matchesTuiAndType(tuis, types)) {
				gotOne = true;
				sb.append(i.toShow(indent + 3));
				sb.append("\n");
//				U.log("YES");
			} // else U.log("NO");
		}
		return gotOne ? sb.toString() : "";
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (Interpretation i: interpretations) {
			sb.append(sep);
			sb.append(i.toString());
			sep = "\n";
		}
		return sb.toString();
	}


}
