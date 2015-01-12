package edu.mit.csail.medg.thesmap;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

/**
 * TheMap's version of a JTextArea.
 * We extend the basic behavior so that we can catch events that change the content
 * of the text, in order to reset the Annotations.
 * We catch Paste events also.
 * We also use this to implement highlighting.
 * @author psz
 *
 */
public class JTextAreaU extends JTextArea implements MouseMotionListener, DocumentListener {
	
	private static final long serialVersionUID = 1L;

	public UmlsWindow myWindow = null;
	private int oldPos = -1;
	Highlighter hilit;
	Highlighter.HighlightPainter painter;
	 
	public JTextAreaU(UmlsWindow w) {
		super();
		init(w);
	}
	
	public JTextAreaU(Document doc, UmlsWindow w) {
		super(doc);
		init(w);
	}
	
	public JTextAreaU(int rows, int columns, UmlsWindow w) {
		super(rows, columns);
		init(w);
	}
	
	public JTextAreaU(String string, UmlsWindow w) {
		super(string);
		init(w);
	}
	
	public JTextAreaU(String string, int rows, int columns, UmlsWindow w) {
		super(string, rows, columns);
		init(w);
	}
	
	void init(UmlsWindow w) {
		myWindow = w;
		hilit = new DefaultHighlighter();
		setHighlighter(hilit);
		addMouseMotionListener(this);
		getDocument().addDocumentListener(this);
	}
	
	@Override
	public void setText(String str) {
		super.setText(str);
		myWindow.resetAnnotations();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// Nothing to do.
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		JTextArea ta = (JTextArea)e.getComponent();
		Integer pos = ta.viewToModel(p);
//		U.log("Mouse moved: " + p.toString() + " posn="+pos);
		if (pos != oldPos) { // Don't redisplay if position did not change.
			oldPos = pos;
			myWindow.showContextAnnotations(pos);
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		//U.p("Insert");
		myWindow.resetAnnotations();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		//U.p("Remove");
		myWindow.resetAnnotations();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		//U.p("Change");
		myWindow.resetAnnotations();
	}
	
	public void removeAllHighlights() {
		hilit.removeAllHighlights();
	}
	
	public void addHighlight(int start, int end, Color col) {
		painter = new DefaultHighlighter.DefaultHighlightPainter(col);
		try {
			hilit.addHighlight(Math.max(0, start), Math.min(getText().length(), end), painter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}
