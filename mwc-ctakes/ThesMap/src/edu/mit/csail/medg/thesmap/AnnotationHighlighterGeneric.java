package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/*
 * We need to fix this so that highlighting happens with the right colors!!!!
 */
public class AnnotationHighlighterGeneric extends SwingWorker<Void, Void> {
	
	JTextArea textArea;
	AnnotationSet annSet;
	SemanticTree semTree;
	BitSet chosenAnnotators;
	
	public AnnotationHighlighterGeneric(UmlsWindow w) {
		this.textArea = w.textArea;
		this.annSet = w.annSet;
		this.semTree =  w.semanticTypes;
		this.chosenAnnotators = w.chosenAnnotators;
	}
	

	@Override
	protected Void doInBackground() throws Exception {
		// Figure out the Annotations to display
		highlightSelectedAnnotations();
		return null;
	}
	
	/**
	 * Paints highlights for all the annotation semantic types selected in the SemanticTree.
	 * We collect all the starting and ending boundaries of all the Annotations in the AnnotationSet
	 * to identify the breakpoints where highlighting might change.  For each interval, we
	 * then compute the union of annotation types present in that interval that are also
	 * consistent with the selected TUIs.  
	 * 
	 * The Interpretation types that we currently follow are:
	 * UMLS - by our simple lookup scheme
	 * MM - from MetaMap
	 * CTAKES - from cTakes
	 * WJL - (maybe) from William J. Long's annotator
	 * 
	 * We assign a bit to each of these, and then a color scheme to each of the 2^4 possible combinations.
	 */
	public synchronized void highlightSelectedAnnotations() {
		Highlighter h = textArea.getHighlighter();
		h.removeAllHighlights();
		String theText = textArea.getText();
		int textl = theText.length();
		ArrayList<String> selectedTuis = semTree.getSelectedTuis();
//		System.out.println("Repainting Annotations for "+selectedTuis);
		for (Annotation ann: annSet) {
			if (ann.matchesTui(selectedTuis)) {
				if (ann.begin < 0 || ann.end > textl) {
					U.pe("Annotation outside text bounds [0,"+textl+"):");
					U.pe(ann.toShow());
				}
				try {
					h = textArea.getHighlighter();
					h.addHighlight(ann.begin, ann.end, DefaultHighlighter.DefaultPainter);
				} catch (BadLocationException e) {
					// Report error if Annotation refers outside text bounds
					U.pe("Bad location: "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
}
