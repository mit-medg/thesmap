package edu.mit.csail.medg.thesmap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;


	
	public class Subject {
		
		static final SimpleDateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		public int id = -1;
		public String sex = null;
		public Date dob = null;
		public Date dod = null;
		public String expired = null;
		public int age = -1;
		
		public Subject(ResultSet rs) {
			try {
				id = rs.getInt(1);
				sex = rs.getString(2);
				dob = rs.getTimestamp(3);
				dod = rs.getTimestamp(4);
				expired = rs.getString(5);
				age = rs.getInt(6);
			} catch (SQLException e) {}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer("<Subject ");
			sb.append((id==-1) ? "Unknown": id);
			if (sex != null) sb.append(" " + sex);
			if (dob != null) sb.append(", dob=" + myDateFormat.format(dob));
			if (dod != null) sb.append(", dod=" + myDateFormat.format(dod));
			if (age != -1) sb.append(", age@discharge=" + age);
			return sb.toString();
		}
		
	}
