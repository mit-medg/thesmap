package edu.mit.csail.medg.thesmap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * An Interpretation is the result of canonicalizing and looking up a phrase
 * from text by an annotator using any of different annotation methods.
 * @author psz
 *
 */
public class Interpretation {
	
	String cui, str, tui, sty;
	String type = null;
	BitSet currentTypes = null;
	int annotatorValue = 0; // Value for which annotators have been flagged.
	
	// A singleton indicating no Interpretation.  There is only one, independent 
	// of the type.
	public static Interpretation nullInterpretation = new Interpretation();
	
	public Interpretation() {
		currentTypes = new BitSet();
	}
	
	public static Interpretation makeInterpretation(ResultSet rs) {
		Interpretation i = new Interpretation();
		try {
			i.cui = rs.getString(1);
			i.str = rs.getString(2);
			i.tui = rs.getString(3);
			i.sty = rs.getString(4);
		} catch (SQLException e) {
			i = nullInterpretation;
		}
		return i;
	}
	
	public static Interpretation makeInterpretation(String type, ResultSet rs) {
		Interpretation i = makeInterpretation(rs);
		i.type = type;
		i.updateTypeBits(type);
		i.updateAnnotatorValue(type);
		return i;
	}
	
	/**
	 * Determines whether two Interpretations are equal.
	 * They are if they have the same type, cui and tui.
	 * Because str and sty are derived from cui and tui,
	 * we need not check these. (!)
	 * @param other
	 * @return
	 */
	public boolean equals(Interpretation other) {
		return (cui.equals(other.cui)
				&& tui.equals(other.tui)
				&& type.equals(other.type)
//				&& str.equals(other.str)
//				&& sty.equals(other.sty)
				);
	}
	
	public boolean isNullInterpretation() {
		return this == nullInterpretation;
	}
	
	public String toString() {
		if (isNullInterpretation()) return "NullInterpretation";
		else return str + " (" + cui + "): " + sty + " (" + tui + ")";
	}
	
	public String toShow() {
		return toShow(0);
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		StringBuilder sbs = new StringBuilder();
		for (int i = 0; i < indent; i++) sbs.append(" ");
		String leadingSpaces = sbs.toString();
		
		// When showing the result, show it with the different annotator types.
		ArrayList<String> names = Annotator.getNames(currentTypes);
		String annotatorNames = "[";
		for (String ann: names) {
			annotatorNames += ann +", ";
		}
		// substring to get rid of the last ', '
		annotatorNames = annotatorNames.substring(0, annotatorNames.length()-2) + "]";
		return leadingSpaces + annotatorNames +
				" [" + cui + "," + tui + "] " + str + " (" + sty + ")";
	}
	
	public boolean matchesTui(String tui) {
		return tui.equals(this.tui);
	}
	
	// Return true if the CUI and TUI matches.
	public boolean matchesCuiTui(String cui, String tui) {
		return cui.equals(this.cui) && tui.equals(this.tui);
	}
	
	public boolean matchesTui(ArrayList<String> tuis) {
		return tuis == null || tuis.contains(this.tui);
	}
	
	public boolean matchesType(ArrayList<String> types) {
		return types == null || types.contains(this.type);
	}
	
	public boolean matchesTuiAndType(ArrayList<String> tuis, ArrayList<String> types) {
		return ((tuis == null) || tuis.contains(tui)) && ((types == null) || types.contains(type));
	}
	
	/** 
	 * Keeps track of the types are associated with this interpretation.
	 * @return
	 */
	public BitSet updateTypeBits(String type) {
		currentTypes.or(Annotator.getBitSet(type));
		return currentTypes;
	}
	
	public int getAnnotatorValues() {
		return annotatorValue;
	}
	
	/** 
	 * Update the annotator view flag (AVF) value with the new annotator value.
	 * @param type
	 * @return
	 */
	public int updateAnnotatorValue(String type) {
		annotatorValue += Annotator.getAnnotatorFlagValue(type);
		return annotatorValue;
	}
	
	public BitSet typeBits() {
//		Annotator.checkStatic("Checking in Interpretation " + this);	
		BitSet ans = Annotator.getBitSet(type); //new BitSet();
		return ans;
	}
	
	

}
