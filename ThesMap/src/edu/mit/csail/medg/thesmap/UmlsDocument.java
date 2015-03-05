package edu.mit.csail.medg.thesmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.BitSet;

import javax.swing.JComboBox;
import javax.swing.SwingWorker;

/**
 * This class is a non-interactive version of UmlsWindow, for batch processing.
 * It opens a given file, annotates it, and writes the results to a csv file
 * with the same name.csv.
 * 
 * @author psz
 *
 */

public class UmlsDocument extends SwingWorker<Void, String> implements
		PropertyChangeListener {

	String text = null;
	File inFile = null;
	AnnotationSet annSet = null;
	BitSet chosenAnnotators = null;
	BitSet doneBits = null;
	UmlsWindow window = null;
	BatchWindow dirWindow = null;
	String docID = null;
	
	SemanticTree semanticTypes;
	String currentTuiSelection;

	// private static final Pattern spaces = Pattern.compile("\\s+|$");

	UmlsDocument(BatchWindow dirWindow, File inFile,
			BitSet chosenAnnotators, BitSet doneBits, String tuiSelection) {
		this.dirWindow = dirWindow;
		this.inFile = inFile;
		this.chosenAnnotators = chosenAnnotators;
		this.doneBits = doneBits;
		annSet = new AnnotationSet();
		window = new UmlsWindow(inFile, true);
		docID = inFile.getName();
		this.currentTuiSelection = tuiSelection;

		// Listen for when annotations are done for this particular file.
		window.addPropertyChangeListener(this);
	}
	
	/** 
	 * Allows you to directly process text in a String format
	 * instead of reading from a file. 
	 * @param dirWindow
	 * @param text
	 * @param chosenAnnotators
	 * @param doneBits
	 */
	UmlsDocument(BatchWindow dirWindow, String text, String id,
			BitSet chosenAnnotators, BitSet doneBits, String tuiSelection) {
		this.dirWindow = dirWindow;
		this.text = text;
		docID = id;
		this.chosenAnnotators = chosenAnnotators;
		this.doneBits = doneBits;
		annSet = new AnnotationSet();
		window = new UmlsWindow(docID, true);
		this.currentTuiSelection = tuiSelection;

		// Listen for when annotations are done for this particular file.
		window.addPropertyChangeListener(this);
	}

	private String getContent(InputStream is) throws IOException {
		// This works for either File or Uri input
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = in.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Invoke each feasible Annotator unless its annotations are already
	 * recorded in the current AnnotationSet.
	 * 
	 * @param source
	 */
	private void doAnnotations() {
		// Check which annotations are checked (chosenAnnotators) but not yet
		// annotated
		BitSet needToAnnotate = new BitSet();
		needToAnnotate.or(chosenAnnotators);
		needToAnnotate.andNot(annSet.typeBits());
		doneBits = new BitSet();
		if (!needToAnnotate.isEmpty()) {
			int i = -1;
			while ((i = needToAnnotate.nextSetBit(i + 1)) >= 0) {
				U.log("Try to run Annotator " + Annotator.getName(i) + "for "
						+ docID);
				Annotator ann = Annotator.makeAnnotator(Annotator.getName(i),
						window);
				ann.execute();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (prop.equals("Thesaurus Map")
				&& evt.getNewValue().equals("complete")) {
			firePropertyChange(docID, "processing", "done");
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		if (inFile != null) {
			FileInputStream is = null;
			try {
				is = new FileInputStream(inFile);
				text = getContent(is);
			} catch (FileNotFoundException e) {
				System.err.println("File " + inFile + " not found: "
						+ e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error reading " + is + ": " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
		if (window.textArea == null) {
			window.textArea = new JTextAreaU(25, 80, window);
		}
		window.textArea.setText(text);
		window.annSet = annSet;
		window.needToAnnotate = chosenAnnotators;
		
		// Set which TUIs to annotate.
		semanticTypes = new SemanticTree(SemanticEntity.top);
		semanticTypes.setRootVisible(true);

		window.semanticTypes = semanticTypes;
		window.currentTuiSelection = currentTuiSelection;

		doAnnotations();
		
		return null;

	}

}
