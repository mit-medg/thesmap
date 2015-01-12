package edu.mit.csail.medg.thesmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.TreeSet;

import javax.swing.SwingWorker;

/*
 * We need to fix this so that highlighting happens with the right colors!!!!
 */
public class AnnotationHighlighter extends SwingWorker<Void, Void> {
	
	JTextAreaU textArea;
	AnnotationSet annSet;
	SemanticTree semTree;
	BitSet chosenAnnotators;
	UmlsWindow w;
	
	public AnnotationHighlighter(UmlsWindow w) {
		this.w = w;
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
	 * The Interpretation types that we currently follow are the ones defined in Annotator, currently
	 * UMLS - by our simple lookup scheme
	 * MetaMap - from MetaMap
	 * cTakes - (maybe) from cTakes
	 * WJL - (maybe) from William J. Long's annotator
	 * 
	 * We assign a bit to each of these, and then a color scheme to each of the 2^4 possible combinations.
	 */
	public synchronized void highlightSelectedAnnotations() {
//		U.log("highlightSelectedAnnotations");
//		Annotator.checkStatic("highlightSelectedAnnotations");
//		U.log("Try UMLS: " + Annotator.getIndex("UMLS"));
		textArea.removeAllHighlights();
		String theText = textArea.getText();
		int textl = theText.length();
		ArrayList<String> selectedTuis = semTree.getSelectedTuis();
		ArrayList<String> selectedTypes = w.methodChooser.getSelectedMethods();
		// For each interval in the text defined by the start and end of annotations,
		// we compute the set of annotations that apply to that interval, and choose
		// a highlight color representing that set.
		TreeSet<Integer> breaks = annSet.getAnnotationBreaks();
//		StringBuilder sb = new StringBuilder("Breaks: ");
//		String sep = "";
//		for (Integer i: breaks) {
//			sb.append(sep).append(i);
//			sep = ",";
//		}
//		U.log(sb.toString());
//		U.log(breaks.size() + " breaks.");
		Integer start = -1;
//		for (Integer next: breaks) System.out.print(next + " ");
		for (Integer next: breaks) {
			if (start >= 0) {
//				Annotator.checkStatic("At " + start);
				ArrayList<Annotation> relevant = annSet.hittingSet(start, selectedTuis, selectedTypes);
//				U.log(start + " to " + next + ": " + relevant.size());
				BitSet relevantBits = new BitSet();
				for (Annotation ann: relevant) {
					BitSet tb = ann.typeBits();
					relevantBits.or(tb);
				}
				if (!relevantBits.isEmpty()) {
					Color relevantColor = AnnotationHighlight.getColor(relevantBits);
//					U.log("Paint " + relevantColor + " from " + start + " to " + next);
					textArea.addHighlight(start, (next == null) ? textl : next, relevantColor);
				}
			}
			start = next;
		}
//		for (Annotation ann: annSet) {
//			if (ann.matchesTuiAndType(selectedTuis, selectedTypes)) {
//				if (ann.begin < 0 || ann.end > textl) {
//					U.pe("Annotation outside text bounds [0,"+textl+"): " + ann.toShow());
//				}
//				try {
//					h = textArea.getHighlighter();
//					h.addHighlight(ann.begin, ann.end, DefaultHighlighter.DefaultPainter);
//				} catch (BadLocationException e) {
//					// Report error if Annotation refers outside text bounds
//					U.pe("Bad location: "+e.getMessage());
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
}
