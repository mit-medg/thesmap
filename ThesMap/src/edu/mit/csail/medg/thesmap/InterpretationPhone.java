package edu.mit.csail.medg.thesmap;

public class InterpretationPhone extends InterpretationNumeric {
	
	static final String cuiForPhone = "C1515258"; //  Telephone Number
	static final String tuiForPhone = "T077";
	static final String styForPhone = "Conceptual Entity";

	
	String phoneType;
	
	InterpretationPhone(String phoneType, String number) {
		super(cuiForPhone, number, tuiForPhone, styForPhone);
		this.phoneType = phoneType;
	}
	
	public String toSting() {
		return phoneType + super.toString();
	}
	
	public String toShow() {
		return super.toShow() + " (" + phoneType + ")";
	}

}
