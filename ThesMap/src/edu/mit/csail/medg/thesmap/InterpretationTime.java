package edu.mit.csail.medg.thesmap;

public class InterpretationTime extends InterpretationNumeric {
	
	static final String cuiForTime = "C0040223";
	static final String tuiForTime = "T079";
	static final String styForTime = "Temporal Concept";
	
	Integer hour = null;
	Integer minute = null;
	Integer second = null;
	
	public InterpretationTime(Integer h, Integer m, Integer s, String text) {
		super(cuiForTime, text, tuiForTime, styForTime);
		type = AnnotatorNumeric.name;
		this.hour = h;
		this.minute = m;
		this.second = s;
	}
	
	public static Interpretation makeInterpretationTime(String hr, String mn, String sc, String ampm, String text) {
		// Assumes only sc can be null, and hr, mn, sc strings are valid numbers.
		Interpretation ans = nullInterpretation;
		Integer h = new Integer(hr);
		Integer m = new Integer(mn);
		Integer s = null;
		try {
			s = new Integer(sc);
		} catch (NumberFormatException e) {}
		if (h != null && h >= 0 && h < 24 && m != null && m >= 0 && m < 60 &&
				(s == null || (s >= 0 && s < 60))) {
			// Proper time; adjust for PM
			if (ampm == null) ans = new InterpretationTime(h, m, s, text);
			else if (ampm.equalsIgnoreCase("PM")) {
				if (h < 12) ans = new InterpretationTime(h + 12, m, s, text);
				else if (h == 12) ans = new InterpretationTime(h, m, s, text);
				// else invalid
			} else if (ampm.equalsIgnoreCase("AM")) {
				if (h < 12) ans = new InterpretationTime(h, m, s, text);
				if (h == 12) ans = new InterpretationTime(0, m, s, text);
				// else invalid
			}
		}
		return ans;
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		return super.toShow(indent) + " (" + hour + ", " + minute + ", " + second + ")";
	}
	
	public String toString() {
		return super.toString() + " (" + hour + ", " + minute + ", " + second + ")";
	}

}
