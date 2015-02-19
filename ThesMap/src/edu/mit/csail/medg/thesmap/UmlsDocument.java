package edu.mit.csail.medg.thesmap;

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

import edu.mit.csail.medg.thesmap.UmlsWindow.MethodChooser;

/**
 * This class is a non-interactive version of UmlsWindow, for batch processing.
 * It opens a given file, annotates it, and writes the results to a csv file
 * with the same name.csv.
 * 
 * @author psz
 *
 */

public class UmlsDocument implements Runnable, PropertyChangeListener {

	String text = null;
	File inFile;
	AnnotationSet annSet = null;
	BitSet chosenAnnotators = null;
	BitSet doneBits = null;
	UmlsWindow window = null;

	// private static final Pattern spaces = Pattern.compile("\\s+|$");

	UmlsDocument(File inFile, BitSet chosenAnnotators, BitSet doneBits) {
		this.inFile = inFile;
		this.chosenAnnotators = chosenAnnotators;
		this.doneBits = doneBits;
		annSet = new AnnotationSet();
		window = new UmlsWindow(inFile, true);
	}

	public void run() {

		FileInputStream is = null;
		try {
			is = new FileInputStream(inFile);
			text = getContent(is);
			if (window.textArea == null) {
				window.textArea = new JTextAreaU(25, 80, window);
			}
			window.textArea.setText(text);
			window.annSet = annSet;
			window.needToAnnotate = chosenAnnotators;
			doAnnotations();

//			try {
//				// Temporarily waiting before saving to file. Need to fix so that it knows when the annotation is done.
//				Thread.sleep(10000);
//				UmlsWindow.saveAnnotations(csvFile(inFile), window.annSet);
//				System.out.println("Just saved to file");
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		} catch (FileNotFoundException e) {
			System.err.println("File " + inFile + " not found: "
					+ e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading " + is + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
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
						+ inFile.getName());
				Annotator ann = Annotator.makeAnnotator(Annotator.getName(i),
						window);
				ann.addPropertyChangeListener(this);
				ann.execute();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
	}

}
