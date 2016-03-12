package edu.mit.csail.medg.thesmap;

public class InterpretationDateRange extends InterpretationNumeric {
	
	static final String cuiForDate = "C0805843"; //  Date Range
	static final String tuiForDate = "T079";
	static final String styForDate = "Temporal Concept";
	static final int[] monthLengths = {31, 28, 31, 30, 31, 30, 31, 30, 30, 31, 30, 31};
	static final int centuryBreak = 30;
	
	Integer year1 = null;
	Integer month1 = null;
	Integer day1 = null;
	Integer year2 = null;
	Integer month2 = null;
	Integer day2 = null;
	

	public InterpretationDateRange(Integer y1, Integer m1, Integer d1, 
			Integer y2, Integer m2, Integer d2, String text) {
		super(cuiForDate, text, tuiForDate, styForDate);
		type = AnnotatorNumeric.name;
		this.year1 = y1;
		this.month1 = m1;
		this.day1 = d1;
		this.year2 = y2;
		this.month2 = m2;
		this.day2 = d2;
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
	
	public static Interpretation makeInterpretationDateRange(String yr1, String mo1, String da1, 
			String yr2, String mo2, String da2, String text) {
		Interpretation date1 = InterpretationDate.makeInterpretationDate(yr1, mo1, da1, text);
		if (date1.isNullInterpretation()) return date1;
		Interpretation date2 = InterpretationDate.makeInterpretationDate(yr2, mo2, da2, text);
		if (date2.isNullInterpretation()) return date2;
		InterpretationDate dt1 = (InterpretationDate)date1;
		InterpretationDate dt2 = (InterpretationDate)date2;
		return new InterpretationDateRange(dt1.year, dt1.month, dt1.day, dt2.year, dt2.month, dt2.day, text);
	}
	
	public String toShow(int indent) {
		if (isNullInterpretation()) return "NullInterpretation";
		return super.toShow(indent) + " (" + year1 + "-" + month1 + "-" + day1 + 
				"--" + year2 + "-" + month2 + "-" + day2 + ")";
	}
	
	public String toString() {
		return super.toString() + " (" + year1 + "-" + month1 + "-" + day1 + 
				"--" + year2 + "-" + month2 + "-" + day2 + ")";
	}

}
