/**
 * This implements a resource connector to a MySQL version of the UMLS
 * Metathesaurus.
 * When it is first initialized, we also init on SemanticEntity, to make sure
 * we have cached in Java structures the TUI tree from UMLS. 
 */
package edu.mit.csail.medg.thesmap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
//import java.sql.Type;

/**
 * @author psz
 *
 */
public class ResourceConnectorASD extends ResourceConnector {

	static ResourceConnectorPool<ResourceConnectorASD> pool = 
			new ResourceConnectorPool<ResourceConnectorASD>();
	
	Connection conn = null;
	Statement stmt = null;
	ResultSet rs = null;
	static final String insertTupleStmtT = "insert into tup3(id,w1,w2,w3,count) values(?,?,?,?,?)";
	static PreparedStatement insertTupleStmt;
	static final String insertTupleStmtNoIdT = "insert into tup3(w1, w2, w3, count) values(?,?,?,?)";
	static PreparedStatement insertTupleStmtNoId;
	static final String updateTupleStmtT = 
			"update tup3 set count = count+? where id=? and w1=? and w2=? and w3=?";
	static PreparedStatement updateTupleStmt;
	static final String updateTupleStmtNoIdT = 
			"update tup3 set count = count+? where w1=? and w2=? and w3=?";
	static PreparedStatement updateTupleStmtNoId;
	static final String selectTupleStmtT =
			"select * from tup3 where w1=? and w2=? and w3=?";
	static PreparedStatement selectTupleStmt;

	static final String selectBlobStmtT = 
			"select id, encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, " + 
						"instance_num, observation_blob, concept_type, concept "
						+ "from observation_fact where encounter_num=? "
						+ "and observation_blob is not null and observation_blob!='' " 
						+ "and concept_type=? " 
//						+ "order by encounter_num, start_date, provider_id"
						;
	static PreparedStatement selectBlobStmt;
	static final String selectAllBlobStmtT1 = 
			"select id, encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, " + 
					"instance_num, observation_blob, concept_type, concept " +
					"from observation_fact where " +
						"observation_blob is not null and observation_blob!='' and concept_type=? " +
						"order by encounter_num, start_date, provider_id";
	static PreparedStatement selectAllBlobStmt;
	static final String selectCountStmtT =
			"select count(*) from observation_fact where encounter_num=? and concept_type=?";
	PreparedStatement selectCountStmt;
	static final String selectCountAllStmtT =
			"select count(*) from observation_fact where concept_type=?";
	PreparedStatement selectCountAllStmt;
	boolean inTransaction = false;
	
	PreparedStatement lookupWjl;
	static final String lookupWjlT = "select id from wjl where id=? limit 1";
	PreparedStatement insertWjl;
	static final String insertWjlT = "insert into wjl values(?,?,?,?,?,?,?,?,?)";
	
	/*
	 * We add a unique index to every row of observation_fact, so we don't need to use the compound key
	 * to reference each row:
	 * 
	alter table observation_fact add column id integer not null first;
	SELECT @i:=0;
	UPDATE observation_fact SET id = @i:=@i+1;
	create index X_obs_fact_id on observation_fact(id);
	 */
	
	/*
	 * We create a table to hold information about tuples, and another to tie tuple information
	 * to observation_fact.  That latter table is indexed by a primary key consisting of 
	 * encounter_num, concept_cd, provider_id, start_date, modifier_cd, instance_num.
	 * The easiest thing to do would be to add an ID column to observation_fact, to make it more concise
	 * to refer to entries.  We do that, rather than creating a join table or replicating all the keys in
	 * referring to a unique entry:
	 * alter table observation_fact add column id integer not null auto_increment unique key first; 
	 * 
	 * We then create a tup3 table that associates three tokens with a count and with an observation_fact ID,
	 * except when we compute counts over the corpus, in which case we omit the ID.
	 * 
	 * This is the definition of tup3:
	create table tup3(
       id integer null default null,
       w1 varchar(255) binary not null,
       w2 varchar(255) binary not null,
       w3 varchar(255) binary not null,
       count bigint not null default 1);
    alter table tup3 add unique index X_W123 (id, w1, w2, w3);
     * I also assume that something will go wrong in processing these data, so it is worthwhile to keep
     * track of where we are in the process so we can clean up and restart when something goes bad.
     * To support this, we will process the encounters in numerical order of encounter_num, and keep in
     * the database a record of the current encounter_num being processed.  On a restart, we delete any
     * data created from that encounter_num (assuming it's partial), and re-start with the same encounter.
     * To make this persistent, we maintain a table with a single row that holds the current encounter_num.
	 create table current_enc_num(encounter_num integer null default null);
	 insert into current_enc_num values(null);
	 */
	
	/*
	 * We also create a table wjl to record the results of processing notes via Bill Long's parser:
	 * 
	drop table if exists wjl;

	create table wjl (
       id integer not null,	-- this is the observation_fact identifier of the note
       start integer not null,	-- character position of the start of the string
       end integer not null,	-- character position of the end of the string
       type varchar(25) not null,	-- type from WJL's parser, e.g., _symptom
       cui char(8) not null,	-- CUI
       concept varchar(255) not null,	-- preferred name of the CUI
       tui varchar(255) not null,	-- TUI
       str varchar(255) not null,	-- actual character string that gave rise to this interpretation
       truth varchar(25),	-- Negex's truth indicator; could be other than truth
       index wjl_id (id)	-- index by id of the Note
      );
	 * and eventually we index these
    alter table wjl add unique index X_wjl (id, start, end, type, cui, tui); 
	 * This will hold the results of annotations such as
	 <B class="red" title="Percutaneous coronary intervention" cui="C1532338" type="_procedure" 
	 	truth="false" tui="T061" from="1528" to="1564" onClick="doRcd(this)">
	 no percutaneous coronary intervention
	 </B>
	 */
	static boolean broken = false;
	
	public ResourceConnectorASD() {
		super("ASD");
		ThesProps prop = ThesMap.prop;
		String dbhost = prop.getProperty(ThesProps.chdbhostName);
		String dbuser = prop.getProperty(ThesProps.chdbuserName);
		String dbpassword = prop.getProperty(ThesProps.chdbpasswordName);
		String db = prop.getProperty(ThesProps.chdbName);
		String dbUrl = "jdbc:mysql://" + dbhost + "/" + db;
		U.log("Trying to open connection to "+db+" on " +dbhost + " via "+dbuser+"/"+dbpassword);
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, dbuser, dbpassword);
			insertTupleStmt = conn.prepareStatement(insertTupleStmtT);
			updateTupleStmt = conn.prepareStatement(updateTupleStmtT);
			insertTupleStmtNoId = conn.prepareStatement(insertTupleStmtNoIdT);
			updateTupleStmtNoId = conn.prepareStatement(updateTupleStmtNoIdT);
			selectTupleStmt = conn.prepareStatement(selectTupleStmtT);
			selectBlobStmt = conn.prepareStatement(selectBlobStmtT);
			selectAllBlobStmt = conn.prepareStatement(selectAllBlobStmtT1);
			selectCountStmt = conn.prepareStatement(selectCountStmtT);
			selectCountAllStmt = conn.prepareStatement(selectCountAllStmtT);
			lookupWjl = conn.prepareStatement(lookupWjlT);
			insertWjl = conn.prepareStatement(insertWjlT);
			stmt = conn.createStatement();
			
			initialized = true;
		}
		catch (ClassNotFoundException e) {
			System.err.println("Unable to load MySQL driver: " + e);
			initialized = false;
		}
		catch (SQLException e) {
			System.err.println("Unable to connect to UMLS database \"" + db + "\" on " 
								+ dbhost + " as \"" + dbuser + "\": " + e.getMessage());
			//e.printStackTrace(System.err);
			initialized = false;
		}
		if (initialized) pool.add(this);
	}
	
	public static ResourceConnectorASD get() {
		if (broken) return null;
		ResourceConnectorASD ans = pool.getNext();
		if (ans == null) {
			pool.add(new ResourceConnectorASD());
			ans = pool.getNext();
			if (!ans.initialized) {
				broken = true;
				ans = null;
			}
		}
		return ans;
	}
	
	public static void assurePoolSize(int n) {
		for (; n < pool.size(); n++) {
			pool.add(new ResourceConnectorASD());
		}
	}
	
	public void close() {
		if (conn!=null) {
			try{ 
				U.p("Closing " + name + " Connection.");
				if (rs!=null) rs.close();
			} catch (SQLException e) {}
			try{
				if (insertTupleStmt!=null) insertTupleStmt.close();
			} catch (SQLException e) {}
			try{
				if (updateTupleStmt!=null) updateTupleStmt.close();
			} catch (SQLException e) {}
			try{
				if (selectTupleStmt!=null) selectTupleStmt.close();
			} catch (SQLException e) {}
			try{
				if (selectBlobStmt!=null) selectBlobStmt.close();
			} catch (SQLException e) {}
			try{
				if (insertWjl!=null) insertWjl.close();
			} catch (SQLException e) {}
			try{
				if (lookupWjl!=null) lookupWjl.close();
			} catch (SQLException e) {}
			try{
				if (stmt!=null) stmt.close();
			} catch (SQLException e) {}
			try{
				if (conn!=null) conn.close();
			} catch (SQLException e) {}
		}
	}
	/**
	 * This updates the count of a given tuple by incrementing its count field in the database.
	 * There are three ways to do this, and we will use timing experiments to determine which is best:
	 * 1. Read the existing value and then, if it exists, update it; otherwise insert it.
	 * 2. Try inserting it and check for error; then update.  This assumes that most entries are new.
	 * 3. Try update and check for error; then insert. This assumes most entries are updates.
	 * I don't know a priori whether the error condition takes longer to handle.
	 * @param tuple
	 * @param increment
	 * @throws SQLException
	 */
	public void updateExistingCount(String[] tuple, int increment, Integer id) throws SQLException {
//		U.log("Update " + tuple[0] + ", " + tuple[1] + ", " + tuple[2] + " (" + id + ") +" + increment);
		if (id == null) {
			updateTupleStmtNoId.setInt(1, increment);
			updateTupleStmtNoId.setString(2, tuple[0]);
			updateTupleStmtNoId.setString(3, tuple[1]);
			updateTupleStmtNoId.setString(4, tuple[2]);
			updateTupleStmtNoId.execute();
		} else {
			updateTupleStmt.setInt(1, increment);
			updateTupleStmt.setInt(2, id);
			updateTupleStmt.setString(3, tuple[0]);
			updateTupleStmt.setString(4, tuple[1]);
			updateTupleStmt.setString(5, tuple[2]);
			updateTupleStmt.execute();
		}
	}
	
	public void insertNewCount(String[] tuple, int count, Integer id) throws SQLException {
//		U.log("Insert " + tuple[0] + ", " + tuple[1] + ", " + tuple[2] + " (" + id + ") +" + count);
		if (id == null) {
			insertTupleStmtNoId.setString(1, tuple[0]);
			insertTupleStmtNoId.setString(2, tuple[1]);
			insertTupleStmtNoId.setString(3, tuple[2]);
			insertTupleStmtNoId.setInt(4, count);
			insertTupleStmtNoId.execute();
		} else {
			insertTupleStmt.setInt(1, id);
			insertTupleStmt.setString(2, tuple[0]);
			insertTupleStmt.setString(3, tuple[1]);
			insertTupleStmt.setString(4, tuple[2]);
			insertTupleStmt.setInt(5, count);
			insertTupleStmt.execute();
		}
	}
	
	public void insertOrUpdateCount(String[] tuple, int increment, Integer id) throws SQLException{
		try {
			insertNewCount(tuple, increment, id);
		} catch (SQLException e) {
			updateExistingCount(tuple, increment, id);
		}
	}
	
	public int getCountAll(String noteType) {
		ResultSet rs = null;
		try {
			selectCountAllStmt.setString(1, noteType);
			rs = selectCountAllStmt.executeQuery();
			if (rs.next()) {
				int ans = rs.getInt(1);
				rs.close();
				return ans;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return 0;
	}
	
	public ResultSet getNotes(Integer encounterNum, String noteType) throws SQLException{
		if (encounterNum != null) {
			selectBlobStmt.setInt(1, encounterNum);
			selectBlobStmt.setString(2, noteType);
			return selectBlobStmt.executeQuery();
		} else {
			selectAllBlobStmt.setString(1, noteType);
			return selectAllBlobStmt.executeQuery();
		}
		
	}
	
	public void beginTransaction() {
		if (!inTransaction) {
			try {
				stmt.execute("start transaction");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void endTransaction() {
		if (inTransaction) {
			try {
				stmt.execute("commit");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean existsWjl(int id) {
		ResultSet rs = null;
		boolean ans = false;
		try {
			lookupWjl.setInt(1, id);
			rs = lookupWjl.executeQuery();
			if (rs.next()) ans=true;
		} 
		catch (SQLException e) {} 
		finally { 
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return ans;
	}

	public void recordWJL(int id, Integer from, Integer to, String type,
			String cui, String prefName, String tui, String item, String truth) throws SQLException {
		/*
       id integer not null,	-- this is the observation_fact identifier of the note
       start integer not null,	-- character position of the start of the string
       end integer not null,	-- character position of the end of the string
       type varchar(25) not null,	-- type from WJL's parser, e.g., _symptom
       cui char(8) not null,	-- CUI
       concept varchar(255) not null,	-- preferred name of the CUI
       tui varchar(255) not null,	-- TUI
       str varchar(255) not null,	-- actual character string that gave rise to this interpretation
       truth varchar(25),	-- Negex's truth indicator; could be other than truth
       */
		insertWjl.setInt(1, id);
		insertWjl.setInt(2, from);
		insertWjl.setInt(3, to);
		insertWjl.setString(4, type);
		insertWjl.setString(5, cui);
		insertWjl.setNString(6, prefName);
		insertWjl.setString(7, tui);
		insertWjl.setString(8, item);
		insertWjl.setString(9, truth);
		insertWjl.execute();
	}
	
	public ResultSet getAllNotes(String noteType, Integer start, Integer limit) throws SQLException {
		String q = "select id, observation_blob from observation_fact where concept_type='" + noteType + "' " +
				"and observation_blob is not null and observation_blob != ''";
		if (limit != null && start != null) q = q + " limit " + start + "," + limit;
		return stmt.executeQuery(q); 
	}
	
	public ASDNotesIterator getNotesIterator(String noteType) {
		return new ASDNotesIterator(noteType);
	}
	
	public class ASDNotesIterator implements Iterator<ResultSet> {
		String noteType;
		int doneSoFar = 0;
		int total = 0;
		public int chunkSize = 1000;
		ResultSet rs = null;
		
		public ASDNotesIterator(String noteType) {
			this.noteType = noteType;
			doneSoFar = 0;
			total = getCountAll(noteType);
			try {
				rs = getAllNotes(noteType, 1, chunkSize);
			} catch (SQLException e) {
				e.printStackTrace();
				rs = null;
			}
		}

		@Override
		public boolean hasNext() {
			try {
				if (rs.next()) {
					doneSoFar++;
					return true;
				}
				else if (doneSoFar < total) {
					rs = getAllNotes(noteType, ++doneSoFar, chunkSize);
					return rs.next();
				} else return false;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			} 
		}

		@Override
		public ResultSet next() {
			return rs;
		}

		@Override
		public void remove() {
			// No remove method
			System.err.println("Cannot remove() from ASDNotesIterator.");
			
		}
		
	}
	
	public class ASDNote {
		/*
		 * select id, encounter_num, concept_cd, provider_id, start_date, modifier_cd, instance_num, observation_blob 
		 * from observation_fact where observation_blob is not null and observation_blob!='' and concept_type='Note' 
		 * order by encounter_num, start_date, provider_id limit 3;
		 */
		public int id;
		public int encounter_num;
		public int patient_num;
		public String concept_cd;
		public String provider_id;
		public Date start_date;
		public String modifier_cd;
		public int instance_num;
		public String note;
		public String concept_type;
		public String concept;
		
		public ASDNote (ResultSet rs) throws SQLException{
			id = rs.getInt("id");
			encounter_num = rs.getInt("encounter_num");
			patient_num = rs.getInt("patient_num");
			concept_cd = rs.getString("concept_cd");
			provider_id = rs.getString("provider_id");
			start_date = rs.getTimestamp("start_date");
			modifier_cd = rs.getString("modifier_cd");
			instance_num = rs.getInt("instance_num");
			note = rs.getString("observation_blob");
			concept_type = rs.getString("concept_type");
			concept = rs.getString("concept");
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(id).append(":");
			sb.append(concept_cd);
			sb.append(", enc=").append(encounter_num).append(", pat=").append(patient_num);
			if (note == null) sb.append(" NULL");
			else sb.append(" (").append(note.length()).append(")");
			return sb.toString();
		}
	}
	
	/**
	 * ASDNoteList implements an iterable list of notes of type noteType.
	 * This is done because our jdbc driver blows up with a heap memory error if we try to fetch 
	 * a very large number of row.  Here, we restrict fetching to do so by encounter_num.
	 * 
	 * The ASDNoteList fetches is just a shell so we can get its Iterator.
	 * That does a query for the set of distinct encounter_nums and then iterates over
	 * those.  Each iteration in turn fetches notes of the right type for that encounter. 
	 * @author psz
	 *
	 */
	public class ASDNoteList implements Iterable<ASDNote> {
		public String noteType;
		public int chunkSize = 1000;
		public int totalSize;
		
		public ASDNoteList(String noteType) {
			this.noteType = noteType;
			totalSize = getCountAll(noteType);
		}

		@Override
		public Iterator<ASDNote> iterator() {
			return new ASDNoteListIterator(noteType, chunkSize);
		}
		
		public class ASDNoteListIterator implements Iterator<ASDNote> {
			static final String selectAllEncounterNumsT = 
					"select distinct encounter_num from observation_fact where concept_type=? order by encounter_num";
			PreparedStatement encStmt = null; 
			static final String selectAllBlobStmtT = 
					"select id, encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, " + 
							"instance_num, observation_blob, concept_type, concept " 
							+ "from observation_fact "
							+ "where observation_blob is not null "
//							+ "and observation_blob!='' "
							+ "and concept_type=? "
							+ "and encounter_num=? "
							+ "order by encounter_num, start_date, provider_id "
							+ "limit ?,?";
			PreparedStatement selectAllBlobStmt = null;	
			public String noteType;
			public int fetched = 0;

			ResultSet encRS = null;
			ResultSet noteRS = null;
			ASDNote nextASDNote = null;

			public ASDNoteListIterator(String noteType, int newChunkSize) {
				this.noteType = noteType;
				chunkSize = newChunkSize;
				try {
					encStmt = conn.prepareStatement(selectAllEncounterNumsT);
					selectAllBlobStmt = conn.prepareStatement(selectAllBlobStmtT);
					encStmt.setString(1, noteType);
					encRS = encStmt.executeQuery();
					U.log("Returned from MySQL select of encounter_num's.");
//					nextEnc = encRS.next();
				} catch (SQLException e) {
					e.printStackTrace();
//				} finally {
//					try { if (encStmt != null) encStmt.close(); } catch (SQLException e) {}
//					try { if (selectAllBlobStmt != null) selectAllBlobStmt.close(); } catch (SQLException e) {}
				}
				
			}

			/*
			 * (non-Javadoc)
			 * @see java.util.Iterator#hasNext()
			 * There is no hasNext() operation of ResultSet, so we actually have to fetch the next element on
			 * a call to hasNext() and save it, to return on the call to next().
			 */
			@Override
			public boolean hasNext() {
				if (nextASDNote != null) return true;
				try {
					while (true) {
						if (noteRS != null && noteRS.next()) {
							nextASDNote = new ASDNote(noteRS);
							fetched++;
							return true;
						} else if (encRS != null && encRS.next()) {
							if (noteRS != null) noteRS.close();
							int enc = encRS.getInt(1);
							selectAllBlobStmt.setString(1, noteType);
							selectAllBlobStmt.setInt(2, enc);
							selectAllBlobStmt.setInt(3, fetched);
							selectAllBlobStmt.setInt(4, chunkSize);
							noteRS = selectAllBlobStmt.executeQuery();
							continue;
						} else {
							if (encRS != null) encRS.close();
							return false;
						}
					} 
				}
				catch (SQLException e) {
					e.printStackTrace();
					try { if (noteRS != null) noteRS.close(); } catch (SQLException ex) {}
					try { if (encRS != null) encRS.close(); } catch (SQLException ex) {}
					return false;
				}
			}

			@Override
			public ASDNote next() {
				// Just return the item found by hasNext()
				if (nextASDNote == null) U.pe("ASDNoteList.next() called but is empty!");
				ASDNote ans = nextASDNote;
				nextASDNote = null;
				return ans;
			}

			@Override
			public void remove() {
				System.err.println("Remove of ASDNoteIterator not implemented.");
			}
			
			public void close() {
				try { 
					if (noteRS != null) noteRS.close();
				} catch (SQLException e) {}
				try {
					if (encRS != null) encRS.close();
				} catch (SQLException e) {}
			}
			
		}
		
	}

//	public class ASDNoteList implements Iterable<ASDNote> {
//		public String noteType;
//		public int chunkSize = 1000;
//		public int totalSize;
//		
//		public ASDNoteList(String noteType) {
//			this.noteType = noteType;
//			totalSize = getCountAll(noteType);
//		}
//
//		@Override
//		public Iterator<ASDNote> iterator() {
//			return new ASDNoteListIterator(noteType, chunkSize);
//		}
//		
//		public class ASDNoteListIterator implements Iterator<ASDNote> {
//			static final String selectAllEncounterNumsT = 
//					"select distinct encounter_num from observation_fact where concept_type=? order by encounter_num";
//			PreparedStatement encStmt = null; 
//			static final String selectAllBlobStmtT = 
//					"select id, encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, " + 
//							"instance_num, observation_blob, concept_type, concept " +
//							"from observation_fact where " +
//								"observation_blob is not null and observation_blob!='' and concept_type=? " +
//								"and encounter_num=? " +
//								"order by encounter_num, start_date, provider_id " +
//								"limit ?,?";
//			PreparedStatement selectAllBlobStmt = null;	
//			public String noteType;
//			public int fetched = 0;
//
//			ResultSet encRS = null;
//			ResultSet noteRS = null;
//			ASDNote nextASDNote = null;
//
//			public ASDNoteListIterator(String noteType, int newChunkSize) {
//				this.noteType = noteType;
//				chunkSize = newChunkSize;
//				try {
//					encStmt = conn.prepareStatement(selectAllEncounterNumsT);
//					selectAllBlobStmt = conn.prepareStatement(selectAllBlobStmtT);
//					encStmt.setString(1, noteType);
//					encRS = encStmt.executeQuery();
//					U.log("Returned from MySQL select of encounter_num's.");
////					nextEnc = encRS.next();
//				} catch (SQLException e) {
//					e.printStackTrace();
////				} finally {
////					try { if (encStmt != null) encStmt.close(); } catch (SQLException e) {}
////					try { if (selectAllBlobStmt != null) selectAllBlobStmt.close(); } catch (SQLException e) {}
//				}
//				
//			}
//
//			/*
//			 * (non-Javadoc)
//			 * @see java.util.Iterator#hasNext()
//			 * There is no hasNext() operation of ResultSet, so we actually have to fetch the next element on
//			 * a call to hasNext() and save it, to return on the call to next().
//			 */
//			@Override
//			public boolean hasNext() {
//				if (nextASDNote != null) return true;
//				try {
//					while (true) {
//						if (noteRS != null && noteRS.next()) {
//							nextASDNote = new ASDNote(noteRS);
//							fetched++;
//							return true;
//						} else if (encRS != null && encRS.next()) {
//							int enc = encRS.getInt(1);
//							selectAllBlobStmt.setString(1, noteType);
//							selectAllBlobStmt.setInt(2, enc);
//							selectAllBlobStmt.setInt(3, fetched);
//							selectAllBlobStmt.setInt(4, chunkSize);
//							noteRS = selectAllBlobStmt.executeQuery();
//							continue;
//						} else return false;
//					} 
//				}
//				catch (SQLException e) {
//					return false;
//				}
//			}
//
//			@Override
//			public ASDNote next() {
//				// Just return the item found by hasNext()
//				if (nextASDNote == null) U.pe("ASDNoteList.next() called but is empty!");
//				ASDNote ans = nextASDNote;
//				nextASDNote = null;
//				return ans;
//			}
//
//			@Override
//			public void remove() {
//				System.err.println("Remove of ASDNoteIterator not implemented.");
//			}
//			
//			public void close() {
//				try { 
//					if (noteRS != null) noteRS.close();
//				} catch (SQLException e) {}
//				try {
//					if (encRS != null) encRS.close();
//				} catch (SQLException e) {}
//			}
//			
//		}
//		
//	}

	
}
