package edu.mit.csail.medg.thesmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.mit.csail.medg.thesmap.ResourceConnectorASD.ASDNote;
import edu.mit.csail.medg.thesmap.ResourceConnectorASD.ASDNotesIterator;
import edu.mit.csail.medg.thesmap.Trie.TrieEntry;

public class NTuples {
	public static ThesProps prop = null; 
	public static final Pattern splitPatternPunct = Pattern.compile("(\\s+)|(\\p{Punct})");
	public static final Pattern splitPattern = Pattern.compile("\\W+");
	public static String text = "Now is the time for all good men to come to \n" +
			"the aid-of their123 country and the time for all of us to weep.";
	
	public boolean includePunctuation = false;
	public int n = 3;
	public Buffer buffer = new Buffer(n);
	
	public Trie<String, Integer> resultTrie = new Trie<String, Integer>();
	public static ResourceConnectorASD rc = null;
	ResourceConnectorWjl wjl = null;

	public NTuples() {
		if (rc == null) rc = ResourceConnectorASD.get();
	}
	
	public NTuples(boolean punctuation) {
		this();
		includePunctuation = punctuation;
	}
	
	public static void main(String[] args) throws SQLException {
		// Set up for OS X interface if we are running on such a system
		ThesMap.setUpOSX();		
		U.setLogging(U.LOG_TO_LOG_WINDOW);

		U.log("Starting NTuples");

		// Find and load the ThesMap.properties file.
		prop = new ThesProps();
		if (ThesMap.prop == null) ThesMap.prop = prop;
		U.log(prop.toShow());

		NTuples me = new NTuples();
		long startTime = System.nanoTime();
//		me.test();
//		me.generateAllTuplesToDB(null, false);
//		me.generateAllTries(10);
//		me.testupdate();
//		me.extractNotes("/Users/psz/Desktop/ASDNotes/", 100);
		me.parseWJL(20);
		long diff = System.nanoTime() - startTime;
		U.log("NTuples elapsed time (sec): " + diff/1000000000);
		rc.close();
//		System.exit(0);
	}
	
	private void extractNotes(String fileRoot) {
		extractNotes(fileRoot, 10);
	}
	
	private void extractNotes(String fileRoot, Integer maxNum) {
		int total = rc.getCountAll("Note");
		int done = 0;
		int atATime = 100000;
		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
		if (total == 0) {
			U.log("No Notes found!");
		} else {
			String which = (maxNum != null) ? maxNum.toString() : "all";
			U.debug("Fetching " + which + " notes.");
			ResultSet rs;
			File theDir;
			try {
				for (done = 0; done < total; ) {
					theDir = new File(fileRoot, "Notes_" + (done+1));
					if (!theDir.exists()) {
						U.log("creating directory: " + theDir);
						try{
							theDir.mkdir();
						} catch(SecurityException se) {
							se.printStackTrace();
						}  
					}
					rs = rc.getAllNotes("Note", done + 1, atATime);
					int counter = 0;
					while (rs.next()) {
						int id = rs.getInt(1);
						String note = rs.getNString(2);
						Writer writer = null;
						try {
							writer = new BufferedWriter(new OutputStreamWriter(
									new FileOutputStream(theDir + "/Note_" + id + ".txt"), "utf-8"));
							writer.write(note);
							writer.close();
							U.debug("Wrote note " + (++counter) + ": " + id);
						} catch (IOException ex) {
							ex.printStackTrace();
						} finally {
							try {writer.close();} catch (Exception ex) {}
						}		
					}
					done += counter;
				}
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

//	private void extractNotes(String fileRoot, Integer maxNum) {
//		int total = rc.getCountAll("Note");
//		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
//		if (total == 0) {
//			U.log("No Notes found!");
//		} else {
//			String which = (maxNum != null) ? maxNum.toString() : "all";
//			U.debug("Fetching " + which + " notes.");
//			ResultSet rs;
//			try {
//				rs = rc.getAllNotes("Note", 100001, 100000);
//				int counter = 0;
//				while (rs.next()) {
//					int id = rs.getInt(1);
//					String note = rs.getNString(2);
//					Writer writer = null;
//					try {
//						writer = new BufferedWriter(new OutputStreamWriter(
//								new FileOutputStream(fileRoot + "/Note_" + id + ".txt"), "utf-8"));
//						writer.write(note);
//						writer.close();
//						U.debug("Wrote note " + (++counter) + ": " + id);
//					} catch (IOException ex) {
//						ex.printStackTrace();
//					} finally {
//						try {writer.close();} catch (Exception ex) {}
//					}			
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}

//	private void extractNotes(String fileRoot, Integer maxNum) {
//		int total = rc.getCountAll("Note");
//		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
//		if (total == 0) {
//			U.log("No Notes found!");
//		} else {
//			int counter = 0;
//			String which = (maxNum != null) ? maxNum.toString() : "all";
//			U.debug("Fetching " + which + " notes.");
//			Iterator<ASDNote> it = rc.new ASDNoteList("Note").iterator();
//			while (((maxNum == null) || counter < maxNum) && it.hasNext()) {
//				ASDNote theNote = it.next();
//				U.debug("Document " + (++counter) + "/" + ((maxNum != null) ? maxNum.toString() : total) + ": " + theNote.toString());
//				U.log(theNote.toString());
//				Writer writer = null;
//				try {
//				    writer = new BufferedWriter(new OutputStreamWriter(
//				          new FileOutputStream(fileRoot + "/Note_" + theNote.id + ".txt"), "utf-8"));
//				    writer.write(theNote.note);
//				} catch (IOException ex) {
//				  // report
//				} finally {
//				   try {writer.close();} catch (Exception ex) {}
//				}			
//			}
////			U.log(resultTrie.toShow());
//			for (TrieEntry te: resultTrie) {
//				U.log(te.toString());
//			}
//		}
//	}

	private void generateAllTries() {
		generateAllTries(null);
	}
	
	private void generateAllTries(Integer maxNum) {
		int total = rc.getCountAll("Note");
		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
		if (total == 0) {
			U.log("No Notes found!");
		} else {
			int counter = 0;
			String which = (maxNum != null) ? maxNum.toString() : "all";
			U.debug("Fetching " + which + " notes.");
			Iterator<ASDNote> it = rc.new ASDNoteList("Note").iterator();
			while (((maxNum == null) || counter < maxNum) && it.hasNext()) {
				ASDNote theNote = it.next();
				U.debug("Document " + (++counter) + "/" + ((maxNum != null) ? maxNum.toString() : total) + ": " + theNote.toString());
				U.log(theNote.toString());
				try {
					processToTries(theNote);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			U.log(resultTrie.toShow());
			for (TrieEntry te: resultTrie) {
				U.log(te.toString());
			}
		}
	}
	
	public void processToTries(ASDNote theNote) throws SQLException {
		String text = theNote.note;
		Pattern useMe = (includePunctuation) ? splitPatternPunct : splitPattern;
		Matcher m = useMe.matcher(text);
		int prevEnd = 0;
		while (m.find(prevEnd)) {

			int s = m.start();
			if (s > prevEnd) {
				buffer.add(text.substring(prevEnd, s));
				recordToTrie(buffer);
			}
			if (includePunctuation) {
				s = m.start(2);
				if (s >= 0) {
					// The split was on punctuation, which we also record
					int e = m.end(2);
					buffer.add(text.substring(s, e));
					recordToTrie(buffer);
				}
			}
			prevEnd = m.end();
		}
		//		String[] tokens = splitPattern.split(text);
		//		for (int i=0; i<tokens.length; i++) {
		//			System.out.println(tokens[i]);
		//		}
		// append n-1 blanks
		for (int i = 1; i < n; i++) {
			buffer.add("");
			recordToTrie(buffer);
		}
	}

	private void testupdate() throws SQLException {
		buffer.add("to");
		buffer.add("the");
		buffer.add("station");
		recordToDB(buffer);
		recordToDB(buffer);
	}

	public void generateAllTuplesToDB(Integer maxNum, boolean byNote) throws SQLException {
		int total = rc.getCountAll("Note");
		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
		if (total == 0) {
			U.log("No Notes found!");
		} else {
			ASDNotesIterator it = rc.new ASDNotesIterator("Note");
			int counter = 0;
			String which = (maxNum != null) ? maxNum.toString() : "all";
			U.debug("Fetching " + which + " notes.");
//			rc.beginTransaction();
			while (((maxNum == null) || counter < maxNum) && it.hasNext()) {
				ResultSet rs = it.next();
				long startTime = System.nanoTime();
				String theNote = rs.getNString(2);
				int id = rs.getInt(1);
				String tracer = "Document " + (++counter) + "/" + ((maxNum != null) ? maxNum.toString() : total) + ": " 
						+ id + " (" + theNote.length() + ")"; 
				U.debug(tracer);
				processToDB(id, theNote);
				U.log(tracer + " in " + (System.nanoTime() - startTime)/1000000 + " msec");
			}
//			rc.endTransaction();
//			it.close();
		}
		
	}

//	public void generateAllTuplesToDB(Integer maxNum, boolean byNote) throws SQLException {
//		int total = rc.getCountAll("Note");
//		U.log(total + " notes; processing " + ((maxNum==null) ? "all" : maxNum) + ".");
//		if (total == 0) {
//			U.log("No Notes found!");
//		} else {
//			int counter = 0;
//			String which = (maxNum != null) ? maxNum.toString() : "all";
//			U.debug("Fetching " + which + " notes.");
//			Iterator<ASDNote> it = rc.new ASDNoteList("Note").iterator();
////			rc.beginTransaction();
//			while (((maxNum == null) || counter < maxNum) && it.hasNext()) {
//				long startTime = System.nanoTime();
//				ASDNote theNote = it.next();
//				U.debug("Document " + (++counter) + "/" + ((maxNum != null) ? maxNum.toString() : total) + ": " + theNote.toString());
////				process(theNote);
//				U.log(theNote.toString() + " in " + (System.nanoTime() - startTime)/1000000 + " sec");
//			}
////			rc.endTransaction();
////			it.close();
//		}
//		
//	}
	
	public void processToDB(ASDNote note) throws SQLException {
		processToDB(note.id, note.note);
	}
	
	public void processToDB(int id, String text) throws SQLException {

		Pattern useMe = (includePunctuation) ? splitPatternPunct : splitPattern;
		Matcher m = useMe.matcher(text);
		int prevEnd = 0;
		while (m.find(prevEnd)) {

			int s = m.start();
			if (s > prevEnd) {
				buffer.addToDB(text.substring(prevEnd, s), id);
			}
			if (includePunctuation) {
				s = m.start(2);
				if (s >= 0) {
					// The split was on punctuation, which we also record
					int e = m.end(2);
					buffer.addToDB(text.substring(s, e), id);
				}
			}
			prevEnd = m.end();
		}
		//		String[] tokens = splitPattern.split(text);
		//		for (int i=0; i<tokens.length; i++) {
		//			System.out.println(tokens[i]);
		//		}
		// append n-1 blanks
		for (int i = 1; i < n; i++) {
			buffer.addToDB("", id);
		}
	}
	
	public void recordToDB(Buffer buf) throws SQLException {
		recordToDB(buf, null);
	}
	
	public void recordToDB(Buffer buf, Integer id) throws SQLException {
		String[] tuple = buf.getAll();
		rc.insertOrUpdateCount(tuple, 1, id);
	}

	private boolean allNumbers(String[] tuple) {
		for (int i = 0; i < tuple.length; i++) {
			try {
				Integer.parseInt(tuple[i]);
			} catch (NumberFormatException e) {
				return false;
			}
			
		}
		return true;
	}
	
	boolean isNumeric(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void buildTrie() {

		Pattern useMe = (includePunctuation) ? splitPatternPunct : splitPattern;
		Matcher m = useMe.matcher(text);
		int prevEnd = 0;
		while (m.find(prevEnd)) {

			int s = m.start();
			if (s > prevEnd) {
				buffer.add(text.substring(prevEnd, s));
				recordToTrie(buffer);
			}
			if (includePunctuation) {
				s = m.start(2);
				if (s >= 0) {
					// The split was on punctuation, which we also record
					int e = m.end(2);
					buffer.add(text.substring(s, e));
					recordToTrie(buffer);
				}
			}
			prevEnd = m.end();
		}
		//		String[] tokens = splitPattern.split(text);
		//		for (int i=0; i<tokens.length; i++) {
		//			System.out.println(tokens[i]);
		//		}
		// append n-1 blanks
		for (int i = 1; i < n; i++) {
			buffer.add("");
			recordToTrie(buffer);
		}
		
		U.log(resultTrie.toString());
		for (Trie<String, Integer>.TrieEntry e: resultTrie) {
			U.log(e.toString());
		}

	}

	private void recordToTrie(Buffer buf) {
//		U.log("Recording " + buf);
		String[] path = buf.getAll();
		Trie<String, Integer> here = resultTrie.get(path);
		if (here == null) resultTrie.add(path, 1);
		else here.data++;
//		U.log(resultTrie.toString());
	}
	
	private void recordTrieToDB(Trie trie, Integer id) {
		
	}	
	
	/*
	 * We use Bill Long's (WJL) parser to identify clinically significant entities and modifiers in the text.
	 * 
	 */
	private void parseWJL(Integer maxNum) {
		if (wjl == null) wjl = ResourceConnectorWjl.get();
		int total = rc.getCountAll("Note");
		int max = (maxNum == null) ? total : maxNum;
//		String which = (maxNum != null) ? maxNum.toString() : "all";
		int done = 0;
		int atATime = 100000;
		if (total == 0) {
			U.log("No Notes found!");
		} else {
			U.log(total + " notes; processing " + max + ".");
			U.debug("Fetching " + max + " notes.");
			ResultSet rs;
			try {
				for (; done < max; ) {
					rs = rc.getAllNotes("Note", done + 1, atATime);
					while (rs.next()) {
						int id = rs.getInt(1);
						String text = rs.getNString(2);
						text = text.replace('\0', ' ');
						done++;
						U.debug(done + "/" + total + ", id=" + id + "(" + text.length() + ")");
						processWJL(text, id);
					}
				}
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	public void processWJL (String text, int id) {
		// If we have already recorded wjl data for this id, skip doing it again.
		if (rc.existsWjl(id)) return;
		try {
			org.w3c.dom.Document doc = wjl.lookup(text);
			if (doc == null) {
				// An error occurred 
				U.log("Null Document " + id);
			}
			doc.getDocumentElement().normalize();
//			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("B");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
//				System.out.println("\nCurr Elt: " + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) nNode;
					// <B class=BLUE title="_FINDING:C0020649:Hypotension" onClick=doRcd(this)>hypotension</B>
//					System.out.println("class="+e.getAttribute("class"));
//					System.out.println("title="+e.getElementsByTagName("title"));
					// Example element:
					// <B class="red" title="Chief complaint" cui="C0277786" type="_finding" truth="false" 
					//	  tui="T033" from="1023" to="1051">without subjective complaints</B>

					String title = e.getAttribute("title");
					String cui = e.getAttribute("cui");
					String type = e.getAttribute("type");
					String truth = e.getAttribute("truth");
					String tui = e.getAttribute("tui");
					String item = e.getTextContent();
					Integer from = new Integer(e.getAttribute("from"));
					Integer to = 1 + new Integer(e.getAttribute("to")); // returns last char, not next!
					String itemFromText = text.substring(from, to);
					if (!itemFromText.equals(item)) {
						U.log("WJL text mismatch between \"" + item + "\" and \"" + itemFromText + "\"");
					}
//					U.log("id="+id+"("+from+","+to+") type="+type+", cui="+cui+", str="+str+", tui="+tui+": "+ item);
					//recordWJL(int id, Integer from, Integer to, String type,
					//	String cui, String prefName, String tui, String item)
					rc.recordWJL(id, from, to, type, cui, title, tui, item, truth);
				}
			}
		} 
		catch (Exception e) {
			U.log("\n*****************\n" + e.toString());
			U.log(id + ": " + text + "\n*****************\n");
			e.printStackTrace();
		}
	}

	/**
	 * A Buffer is a fixed-size circular buffer of Strings to which one can add
	 * elements, overwriting the oldest. One may also retrieve elements 0..n
	 * @author psz
	 *
	 */
	class Buffer {
		
		int tupleLength;
		int indx; 
		String[] buf;

		Buffer(int n) {
			tupleLength = n;
			buf = new String[tupleLength];
			indx = tupleLength - 1;
			for (int i = 0; i < tupleLength; i++) buf[i] = "";
		}

		Buffer() {
			this(n);	// Defaults to 3 tokens
		}

		private int size() {
			return tupleLength;
		}

		private boolean add(String s) {
			if (!isNumeric(s) && !stopWords.contains(s)) {
				indx = (indx + 1) % tupleLength;
				buf[indx] = s;
				return true;
			} else return false;
		}
		
		private void addToDB(String s, Integer id) throws SQLException {
			if (add(s)) recordToDB(this, id);
		}

		/**
		 * Retrieves the i-th element of a tuple (0-indexed)
		 * @param i
		 * @return the ith element
		 */
		private String get(int i) {
			return buf[(indx + i + 1) % tupleLength];
		}

		private String[] getAll() {
			String[] ans = new String[tupleLength];
			for (int i = 0; i < tupleLength; i++) ans[i] = get(i);
			return ans;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			String sep = "[";
			for (int i = 0; i < n; i++) {
				sb.append(sep);
				sb.append("\"" + get(i) + "\"");
				sep = " ";
			}
			sb.append("]");
			return sb.toString();
		}
	}
	
	private static final Set<String> stopWords = new HashSet<String>(Arrays.asList(
		     new String[] {"a", "an", "and", "are", "as", "at", "be", "but", "by",
		    			"for", "if", "in", "into", "is", "it",
		    			"no", "not", "of", "on", "or", "such",
		    			"that", "the", "their", "then", "there", "these",
		    			"they", "this", "to", "was", "will", "with"}
		));

}
