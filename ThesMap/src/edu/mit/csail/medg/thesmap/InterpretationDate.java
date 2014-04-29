package edu.mit.csail.medg.thesmap;

public class InterpretationDate extends InterpretationNumeric {
	
	static final String cuiForDate = "C0011008";
	static final String tuiForDate = "T079";
	static final String styForDate = "Temporal Concept";
	static final int[] monthLengths = {31, 28, 31, 30, 31, 30, 31, 30, 30, 31, 30, 31};
	static final int centuryBreak = 30;
	
	Integer year = null;
	Integer month = null;
	Integer day = null;
	

	public InterpretationDate(Integer y, Integer m, Integer d, String text) {
		super(cuiForDate, text, tuiForDate, styForDate);
		type = AnnotatorNumeric.name;
		this.year = y;
		this.month = m;
		this.day = d;
	}
	
	static boolean isMonthLengthOk(Integer y, Integer m, Integer d) {
		if (d == null) return true;
		if (m == null) return d > 0 && d <= 31;
		if (m > 0 && m <= 12 && d > 0) {
			if (m == 2 && d == 29 && (y == null || (y%4 == 0 && (y%100 != 0 || y%400 == 0)))) {
				// Leap day
				return true;
			} else if (d <= monthLengths[m - 1]) return true;
		}
		return false;
	}
	
	public static Interpretation makeInterpretationDate(String yr, String mo, String da, String text) {
		Integer y = (yr == null) ? null : new Integer(yr);
		Integer m = new Integer(mo);
		Integer d = new Integer(da);
		if (yr != null && yr.length() <= 2) {
			// 2-digit year, so we heuristically adjust it to the right century
			if (y >= centuryBreak) y += 1900;
			else y += 2000;
		}
		if (isMonthLengthOk(y, m, d)) return new InterpretationDate(y, m, d, text);
		// Heuristic: interchange month and day to see if they work.
		if (isMonthLengthOk(y, d, m)) return new InterpretationDate(y, d, m, text);
		return nullInterpretation;
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		return super.toShow(indent) + " (" + year + "-" + month + "-" + day + ")";
	}
	
	public String toString() {
		return super.toString() + " (" + year + "-" + month + "-" + day + ")";
	}

}
