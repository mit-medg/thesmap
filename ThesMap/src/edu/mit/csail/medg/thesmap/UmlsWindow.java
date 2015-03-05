package edu.mit.csail.medg.thesmap;


import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * This class represents one window in the UmlsLookup program.
 * Each window contains the text to be analyzed, a panel that shows
 * interpretations of that text when part of it is moused over, and
 * a tree panel that shows the categories of concepts to be highlighted.
 * Each also provides check boxes to determine which annotations are
 * to be computed and shown.
 * In addition, the windows have menus associated that run the program.
 * 
 * @author psz
 *
 */
public class UmlsWindow extends JFrameW 
	implements Runnable
	, ClipboardOwner
	, TreeSelectionListener
//	, PropertyChangeListener 
	{
	
	// The collection of all windows is defined in JListedFrame:
	// public static ArrayList<UmlsWindow> windows = new ArrayList<UmlsWindow>();
	public static final String defaultTitle = "Thesaurus Map";
	public static final String annotateButtonLabel = "Annotate";
	public static final String annotateButtonLabelRunning = "Annotating...";
	public static final int ANN_RUNNING = 1;
	public static final int ANN_STOPPED = 0;
	public static final String blankExpl = " ";
	public static final int nColors = 20;
	
	// Default font size for the Semantic Tree
	float fontSize = 9.0f;
	
	// The data source:
	private File inputFile = null;
	private URI inputUri = null;
	private Subject subject = null;
	private String docID = null;
	private UmlsWindow thisWindow = null;		// self-reference
	private AnnotationsWindow annWindow = null; // Reference to the Annotations Window.
	
	// The interpretations:
//	int annotationMethod = 0;	// The Annotator index by which to annotate.
//	int lastAnnotationMethod = 0;	// Tracks to see if we've actually changed
	public AnnotationSet annSet = null;	
	protected BitSet chosenAnnotators = new BitSet();
	protected BitSet doneBits = new BitSet();
	protected BitSet needToAnnotate = new BitSet();
	
	// Boolean to directly save to file when done. If true, then save to file.
	protected boolean saveFileFlag = false;
	protected static SaveAnnotationsDBConnector dbConnector = null;

	// List of files with sublists of TUIs
	protected String[] tuiLists = {"All", "WJL", "ASD"}; 
	protected String currentTuiSelection = "All";
	
	// Creating a UmlsWindow doesn't start running it;
	// We invokeLater to do so, as it is Runnable.
	// Thus, the loading of the source happens in the new thread.

	public UmlsWindow() {
		super(defaultTitle);
		thisWindow = this;
	}
	
	public UmlsWindow(File file) {
		super(file.getName());
		inputFile = file;
		thisWindow = this;
	}
	
	public UmlsWindow(File inFile, boolean saveFile) {
		super(defaultTitle);
		thisWindow = this;
		docID = inFile.getName();
		saveFileFlag = saveFile;
		dbConnector = new SaveAnnotationsDBConnector();
	}
	
	public UmlsWindow(String id, boolean saveFile) {
		super(defaultTitle);
		thisWindow = this;
		docID = id;
		saveFileFlag = saveFile;
		dbConnector = new SaveAnnotationsDBConnector();
	}
	
	public UmlsWindow(URI uri) {
		super(uri.getPath());
		inputUri = uri;
		thisWindow = this;
	}
	
	public UmlsWindow(Subject subj) {
		super("Subject " + subj.id);
		subject = subj;
		thisWindow = this;
	}
	

	@Override
	public void run() {
		thisWindow = this;
		super.run();
		Dimension frameDim = getContentPane().getSize();
		Dimension desired = new Dimension(2 * frameDim.width / 3, 2 * frameDim.height / 3);
		textAreaScroll.setPreferredSize(desired);
		
		// If not batch processing, create the Annotations Window.
		if (saveFileFlag == false) {
			setAnnotationsWindow();
		}

		//		System.out.println("Frame: "+frameDim+", Want: "+desired);
	}

	public void initializeMenus() {
		// We create a menu bar with File, Edit, Window and Help menus.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu windowMenu = makeWindowMenu();
		JMenu helpMenu = new JMenu("Help");
		
		int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
		// The File menu has New, Open..., Save Annotations..., Close. Should also have Print, but not yet.
		JMenuItem newMI = new JMenuItem("New");
		newMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, accelMask));
		newMI.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new UmlsWindow());
			}
		});
		fileMenu.add(newMI);
		
		JMenuItem openMI = new JMenuItem("Open...");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, accelMask));
		openMI.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				File chosenFile = null;
				int val = chooser.showOpenDialog(thisWindow);
				if (val == JFileChooser.APPROVE_OPTION) {
					chosenFile = chooser.getSelectedFile();
				}
				if (chosenFile != null) 
					SwingUtilities.invokeLater(new UmlsWindow(chosenFile));
			}
		});
		fileMenu.add(openMI);
		
		JMenuItem saveMI = new JMenuItem("Save Annotations to .csv file");
		saveMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));
		saveMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Save the annotations on this document.
				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
				File chosenFile = null;
				int val = chooser.showSaveDialog(thisWindow);
				if (val == JFileChooser.APPROVE_OPTION) {
					chosenFile = chooser.getSelectedFile();
				}
				if (chosenFile != null)
					saveAnnotations(chosenFile, annSet);
			}
		});
		fileMenu.add(saveMI);
		
		// Could be used if the user wants to directly save to db rather than to file.
		// TODO(mwc): Give the user the option of which database that it should be stored at.
//		JMenuItem savedbMI = new JMenuItem("Save Annotations to Database");
//		saveMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask));
//		saveMI.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// Save the annotations from file. User specifies the file to choose to save.
//				JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
//				File chosenFile = null;
//				int val = chooser.showSaveDialog(thisWindow);
//				if (val == JFileChooser.APPROVE_OPTION) {
//					chosenFile = chooser.getSelectedFile();
//				}
//				if (chosenFile != null)
//					saveAnnotationsToDB(chosenFile);
//			}
//		});
//		fileMenu.add(savedbMI);
		
		fileMenu.add(makeClose());
		
		// Add a Quit menu item except on Mac, where the application menu already has one.
		if (!System.getProperty("os.name").contains("OS X")) {
			quitMenuItem = new JMenuItem("Quit");
			quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelMask));
			quitMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Could close all open windows here, but probably not necessary.
					ThesMap.close();
				}
				
			});
			fileMenu.add(quitMenuItem);
		}
		
		// Edit menu has Cut, Copy, Paste, Clear and Select All
		// !!!!! Look at http://docs.oracle.com/javase/tutorial/uiswing/components/generaltext.html
		// for easier ways to implement these !!!!!!

		// Whenever the text is changed, we should reset the Annotations
		// and repaint (get rid of) any highlights.
		// But this is already taken care of by the listeners in JTextAreaU.
		JMenuItem cutMI = new JMenuItem("Cut");
		cutMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelMask));
		cutMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Cut the text to the clipboard.
				String selection = textArea.getSelectedText();
				if (selection.length() == 0) selection = textArea.getText();
				getToolkit().getSystemClipboard().setContents(
						new StringSelection(selection), thisWindow);
				textArea.replaceSelection(null);
			}
		});

		JMenuItem copyMI = new JMenuItem("Copy");
		copyMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelMask));
		copyMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Copy the text to the clipboard.
				String selection = textArea.getSelectedText();
				if (selection.length() == 0) selection = textArea.getText();
				getToolkit().getSystemClipboard().setContents(new StringSelection(selection), thisWindow);
			}
		});
		
		JMenuItem pasteMI = new JMenuItem("Paste");
		pasteMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, accelMask));
		pasteMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Paste the text from the clipboard.
				Clipboard clip = getToolkit().getSystemClipboard();
				Transferable cont = clip.getContents(thisWindow);
				String incoming = "";
				if ((cont != null) &&
						cont.isDataFlavorSupported(DataFlavor.stringFlavor))
					try {
						incoming = (String) cont.getTransferData(DataFlavor.stringFlavor);
					} catch (UnsupportedFlavorException err) {
						System.err.println(err); err.printStackTrace();
					} catch (IOException err) {
						System.err.println(err); err.printStackTrace();
					}
				int start = textArea.getSelectionStart();
				int end = textArea.getSelectionStart();
				if (start == end) 
					textArea.insert(incoming, start);
				else if (start == 0 && end == textArea.getText().length())
					textArea.setText(incoming);
				else textArea.replaceRange(incoming, start, end);
			}
		});
		
		JMenuItem clearMI = new JMenuItem("Clear");
		clearMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.replaceRange(null,  0, textArea.getText().length());
			}
			
		});
		
		JMenuItem selectAllMI = new JMenuItem("Select All");
		selectAllMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, accelMask));
		selectAllMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setSelectionStart(0);
				textArea.setSelectionEnd(textArea.getText().length());
			}
		});
		
		editMenu.add(cutMI);
		editMenu.add(copyMI);
		editMenu.add(pasteMI);
		editMenu.add(clearMI);
		editMenu.add(selectAllMI);
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
	}
	
	/**
	 * Create and lay out the content of the window.  The content is in two columns separated by
	 * a SplitPane so it's user-adjustable.  On the left is the SemanticTree to allow selection of
	 * the annotation types to show and, below it, a MethodChooser that allows selection of the 
	 * annotation methods to use.  On the right is a title
	 */
	public void initializeContent() {
		// Create the various components of the interface:
		// 1. A label specifying the source
//		sourceLabel = new JLabel(getTitle());
		// 2. The text area for displaying the source text
		textArea = new JTextAreaU(25, 80, this);
		textAreaScroll = new JScrollPane(textArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		// 3. The explanation area
//		expl = new JTextArea(blankExpl, 10, 80);
//		explScroll = new JScrollPane(expl,
//				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//		// 4. Assemble the right panel
//		rightMainPanel = textAreaScroll;
//		rightMainPanel.setResizeWeight(0.8d);
		// 5. Create the tree display		
		semanticTypes = new SemanticTree(SemanticEntity.top);
		semanticTypes.setRootVisible(true);
		semanticTypes.addTreeSelectionListener(this);
		
		// Allow for the option to select a different TUI list.
		tuiSelector = new JComboBox<String>(tuiLists);
		tuiSelector.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Options: {"All", "WJL", "ASD"}
				// All - keep all semantic types; WJL - use Bill's options. 
				// ASD - shortened list for ASD application.
				JComboBox cb = (JComboBox)e.getSource();
		        String selectedList = (String)cb.getSelectedItem();
		        semanticTypes.setCurrentTuiSelection(selectedList);
		        semanticTypes.updateCurrentTuiSelection();
		        currentTuiSelection = selectedList;
			}
		});
		
		treeScroll = new JScrollPane(semanticTypes,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		treeScroll.setFont(treeScroll.getFont().deriveFont(fontSize));
		// 6. Create the lookup method selector
		methodChooser = new MethodChooser();
		// 7. Assemble the left panel
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(tuiSelector, BorderLayout.NORTH);
		leftPanel.add(treeScroll, BorderLayout.CENTER);
		leftPanel.add(methodChooser, BorderLayout.SOUTH);
		// 8. Combine left and right via a JSplitPane and add that to the JFrame's content
		mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftPanel, textAreaScroll);
		mainPanel.setResizeWeight(0.3);
		add(mainPanel);
		
		// Now get the content, as specified when this UmlsWindow was created.
		if (inputFile != null) {
			try {
				FileInputStream is = new FileInputStream(inputFile);
				setContent(is);
				is.close();
			} catch (IOException e) {
				System.err.println("Error reading file " + inputFile + ": " + e.getMessage());
				e.printStackTrace(System.err);
			}
			
		} else if (inputUri != null) {
			try {
				setContent(inputUri.toURL().openStream());
			} catch (IOException e) {
				System.err.println("Error opening URI " + inputUri + ": " + e.getMessage());
			}
			
		} else if (subject != null) {
			
		}	// Otherwise, pasted must be true and we just wait for the user to paste content.
	}
	
	private void setContent(InputStream is) {
		// This works for either File or Uri input
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		try {
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (IOException e) {
			System.err.println("Error reading " + is + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
		textArea.setText(sb.toString());
		textArea.setCaretPosition(0);
	}
	
	private void setAnnotationsWindow() {
		try {
			annWindow = new AnnotationsWindow();
			SwingUtilities.invokeLater(annWindow);
		} catch (Exception e) {
			System.err.println("Unable to initiate AnnotationsWindow.");
			e.printStackTrace();
			annWindow = null;
		}
	}
	
	// Components of the interface
	JSplitPane mainPanel;
	JPanel leftPanel;
//	JPanel rightPanel;
	JSplitPane rightMainPanel;
	JTextAreaU textArea;
//	JTextArea expl;
	JButton processButton;
//	JPanel toprightPanel;
	SemanticTree semanticTypes;
	MethodChooser methodChooser;
//	ButtonGroup methodGroup;
	JScrollPane textAreaScroll;
//	JScrollPane explScroll;
	JScrollPane treeScroll;
	JComboBox tuiSelector;



	public void setSizeAndLocation() {
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setLocation(originX, originY);
	}
	
	public void resetAnnotations() {
		// U.p("Resetting annotations.");
		annSet = new AnnotationSet();
		showAnnotations();
	}
		
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	public void saveAnnotations(File chosenFile, AnnotationSet annSet) {
		ArrayList<String> selectedTuis = semanticTypes.getSelectedTuis();
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try {
			fos = new FileOutputStream(chosenFile);
			try {
				osw = new OutputStreamWriter(fos, "UTF-8");
				if (osw != null) {
					for (Annotation ann: annSet) {
						for (Interpretation i: ann.getInterpretationSet().getInterpretations()) {
							String preferredText = i.str;
							if (preferredText == null) {
								preferredText = "null";
							}
							// If all, then all TUIs should be saved. If a specific TUI list is selected, only those should be shown.
							if (currentTuiSelection.equals("All") || selectedTuis.contains(i.tui)) {
								osw.write(ann.begin + "," + ann.end + "," + i.cui + "," + i.tui 
										+ ",\"" + fixq(preferredText) + "\"," + i.type + "," + chosenFile.getName() + "\n");
							}
						}
					}
				}
				osw.close();
				fos.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		U.log("Saved " + chosenFile + " to file");
	}
	
	static String fixq(String str) {
		return str.replaceAll("\"", "\"\"");
	}
	
	public static void saveCSVAnnotationsToDB(File csvFileOutput) {
		((SaveAnnotationsDBConnector) dbConnector).saveCSVToDB(csvFileOutput);
		U.log("Saved " + csvFileOutput.getName() + " to database");
	}
	
	/** 
	 * Insert annotations directly to database without saving to csv first.
	 * @param annSet
	 */
	public void saveAnnotationsToDB(String currentDoc, AnnotationSet annSet) {
		ArrayList<String> selectedTuis = semanticTypes.getSelectedTuis();
		for (Annotation ann: annSet) {
			for (Interpretation i: ann.getInterpretationSet().getInterpretations()) {
				String preferredText = i.str;
				if (preferredText == null) {
					preferredText = "null";
				}
				if (currentTuiSelection.equals("All") || selectedTuis.contains(i.tui)) {
					((SaveAnnotationsDBConnector) dbConnector).insertEntry(ann.begin, ann.end, i.cui, i.tui, fixq(preferredText), i.type, currentDoc);
				}
			}
		}
		U.log("Saved " + currentDoc + " to database");
	}

	/**
         * Shows the annotations in the Annotation Window.
         * This will not be called if using the batch version.
         * @param pos
         */
	public void showContextAnnotations(int pos) {		
		String text = textArea.getText();
//		U.debug("showContextAnnotations at " + pos + ": \"" + text.substring(pos, (int)Math.min(pos+8, text.length())) + "\"");
//		System.out.println(+" " + ((annSet==null) ? "no annotations" : ""));
		if (annSet != null) {
			ArrayList<String> selectedTuis = semanticTypes.getSelectedTuis();
			ArrayList<String> selectedTypes = methodChooser.getSelectedMethods();
			ArrayList<Annotation> relevantAnnotations = annSet.hittingSet(pos, selectedTuis, selectedTypes);
			StringBuilder sb = new StringBuilder();
			if (relevantAnnotations != null) {
				for (Annotation ann: relevantAnnotations) {
					String relevantText = ann.interpSet.toShow(selectedTuis, selectedTypes, 0);
					if (relevantText != null && relevantText.length() > 0) {
					sb.append(text.subSequence(ann.begin, ann.end));
					sb.append("\n");
					sb.append(relevantText);
					}
				}
			}
			String newExpl = sb.toString();
			if (newExpl.length() == 0) newExpl = blankExpl;
			// Change explanation only if it is actually different.
			if (!newExpl.equals(annWindow.getText())) {
				annWindow.setText(newExpl);
			}
		}
	}
	
//	private class AnnotatorWorker extends SwingWorker<String, Double> {
//		
//		int phraseLength;
////		final DecimalFormat percentFormatter = new DecimalFormat("##0.0");
//		
//		private AnnotatorWorker(int pl) {
//			phraseLength = pl;
//		}
//
//		@Override
//		protected String doInBackground() throws Exception {
//			// Actually perform the annotation here
//			processButton.setEnabled(false);
//			processButton.setText("Annotating...");
//			annotate(phraseLength);
//			return null;
//		}
//		
//		@Override
//		protected void done() {
//			try {
//				processButton.setText(annotateButtonLabel);
//				processButton.setEnabled(true);
//			} catch (Exception ignore) {}
//		}
//		
//		public synchronized void annotate(int phraseLength) {
//			Annotator ann = Annotator.getAnnotator(annotationMethod);
//			
//			if (annotationMethod == annotateUMLS) annotateUMLS(phraseLength);
//			else if (annotationMethod == annotateMetaMap) annotateMM();
//			else if (annotationMethod == annotatecTakes) annotatecTakes();
//			else U.pe("Unknown annotationMethod " + annotationMethod);
//		}
//		
//		/**
//		 * Creates annotations on the text of the textArea component of UMLSWindow.
//		 * Algorithm:
//		 * We more forward by words, as identified when we tokenize by spaces.
//		 * At the end of each word, we look back to a maximum of n words to find
//		 * phrases that might have interpretations. E.g., for n=3, we consider the
//		 * last word, the last two words and the last three words.
//		 * We then prune interpretations that are also found from shorter subphrases
//		 * of a phrase.  We do this by looking back to phrases that overlap the 
//		 * current one. If the current gives the same interpretation
//		 * @param phraseLength
//		 */
//		public synchronized void annotateUMLS(int phraseLength) {
////			long startTime = System.nanoTime();
//			annSet = new AnnotationSet(phraseLength);
//			SpaceRecord sr = new SpaceRecord(phraseLength);
//			String text = textArea.getText();
//			double textl = text.length();
//			// Tokenize by spaces
//			Matcher m = spaces.matcher(text);
//			while (m.find()) {
//				int here = m.start();	// next found space beginning
//				//expl.setText("Annotating: "+ percentFormatter.format(here/textl/100.0) + "% done.");
//				setProgress((int)Math.round((new Double(here))/textl*100.0));
//				sr.add(m.end());		// end of space = beginning of next word
//				//TheMap.log(sr.toShow());
//				// Consider all phrases starting phraseLength words back from here
//				for (int back = 1; back <= phraseLength; back++) {
//					int start = sr.getPrevStart(back);
//					if (start >= 0) {
//						String phrase = text.substring(start, here);
//						InterpretationSet i = InterpretationSet.lookup(phrase);
//						if (i != null && i != InterpretationSet.nullInterpretationSet) {
//							//TheMap.log("["+start+","+here+"] "+text.substring(start, here) + "=>\n" + i.toShow(1));
//							// Add these interpretations to the annotations unless they duplicate
//							// or cover the same interpretations that are already present.
//							// See note in documentation. 
//							annSet.integrate(new Annotation(start, here, phrase, i));
//						}
//					}
//				}
//			}
////			long diff = System.nanoTime() - startTime;
////			System.out.println("Elapsed time (ms): " + diff/1000000);
////			for (Annotation a: annSet) System.out.println(a.toShow());
//		}
//		
//		public synchronized void annotateMM() {
//			
//			MMAnnotator mma = new MMAnnotator(thisWindow);
//			mma.execute();
//	    }
//	
//		public synchronized void annotatecTakes() {
//			U.pe("cTakes annotations are not yet implemented.");
//		}
//		
//	}
	
	static String summarize(List<String> strings) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String s: strings) {
			sb.append(sep);
			sb.append(s);
			sep = ", ";
		}
		return sb.toString();
	}
	
	public void setAnnotateButtonState(int state) {
		methodChooser.setAnnotateButtonState(state);
	}
	
	public void integrate(Annotation ann) {
		annSet.integrate(ann);
	}
	
	public void showAnnotations() {
		// Repaint the annotations, using an asynchronous task in case it is slow.
		// Is there a potential race condition in case we change the text before
		// the highlighter finishes?  Probably yes.  Need to think about how to
		// abort any running highlighter if we again call showAnnotations, or defer
		// starting a new one until the old one finishes.
		AnnotationHighlighter annTask = new AnnotationHighlighter(this);
		annTask.execute();
	}
	
	// Default parameters of the window
	public static final int width = 1000;
	public static final int height = 700;
	public static final int originX = 20;
	public static final int originY = 50;
	//public static final String title = "UMLS Lookup";
	private static final long serialVersionUID = 1L;
	
	// Menu structure
	JMenuBar myMenuBar = null;
	JMenu fileMenu, editMenu, windowMenu, helpMenu;
	JMenuItem openMenuItem, closeMenuItem, printMenuItem, quitMenuItem;
	JMenuItem faqMenuItem;
	JMenuItem copyMenuItem, pasteMenuItem, cutMenuItem;
	int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// We use this to repaint the highlights, assuming that semanticTypes has been updated
		// before this valueChanged listener is invoked.
		showAnnotations();
	}

	public void annotatorDone(String annotatorName) {
		doneBits.set(Annotator.getIndex(annotatorName));
		if (doneBits.equals(needToAnnotate) && saveFileFlag) {
			// If this is called from UmlsDocument, then we can save to file.
			//File csvFileOutput = csvFile(inputFile);
			//saveAnnotations(csvFileOutput, annSet);
			//saveCSVAnnotationsToDB(csvFileOutput);
			
			// Directly save to the database without saving to file
			saveAnnotationsToDB(docID, annSet);
			firePropertyChange(defaultTitle, "processing", "complete");
		}
		if (methodChooser != null ) {
			methodChooser.annotatorDone(annotatorName);
		}
	}
	
	private static File csvFile(File inFile) {
		String name = inFile.getName();
		int dot = name.lastIndexOf('.');
		String newName = (dot < 1) ? name + ".csv" : name.substring(0, dot)
				+ ".csv";
		return new File(inFile.getParentFile(), newName);
	}
	
	public void setProgress(String annotatorName, int percent) {
		methodChooser.setProgress(annotatorName, percent);
	}
	
	/**
	 * MethodChoose implements a JPanel interface that permits selection of
	 * which types of Annotation should be computed and displayed for this
	 * window. The chooser lays out choices in two columns. Each choice 
	 * includes a checkbox to indicate whether that annotation type is to be 
	 * shown, and a progress indicator that shows the extent to which
	 * that type of annotation has been computed. 
	 * @author psz
	 *
	 */
	protected class MethodChooser extends JPanel implements PropertyChangeListener{
		
		private static final long serialVersionUID = 1L;
		static final int numberOfSelectorColumns = 2;
//		private static final int gridSpace = 6;
		private static final int spPrefW = 200;
		private static final int spMinW = 100;
		private static final int spPrefH = 58;
		protected SelectPanel[] panels;

		JButton doit;
		

		MethodChooser() {
			setLayout(new BorderLayout());
			int nMethods = Annotator.annotationTypes.size();
			if (nMethods > 0) {
				int annIndexSize = Annotator.annotationIndex.size();
				U.log("Creating MethodChooser for " + nMethods + " methods; " + annIndexSize + ".");
				for (int i = 0; i < annIndexSize; i++) {
					String nm = Annotator.getName(i);
					U.log("   " + nm);
				}
			}
			panels = new SelectPanel[nMethods];
			JPanel selectors = new JPanel();
			selectors.setLayout(new GridLayout(0, numberOfSelectorColumns));
			for (int i = 0; i < Annotator.annotationIndex.size(); i++) {
				selectors.add(new SelectPanel(Annotator.getName(i), i));
			}
			add(selectors, BorderLayout.CENTER);
			doit = new JButton("Annotate");
			doit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					doAnnotations();
					
				}
				
			});
			add(doit, BorderLayout.SOUTH);
		}

		public void annotatorDone(String annotatorName) {
			U.log("Annotator " + annotatorName + " reports done.");
			setProgress(annotatorName, 100);
			if (doneBits.equals(needToAnnotate)) {
				// We've completed all the annotations
				setAnnotateButtonState(ANN_STOPPED);
				showAnnotations();
			}
		}
		
		/**
		 * Invoke each feasible Annotator unless its annotations are already recorded in
		 * the current AnnotationSet.
		 * @param source
		 */
		private void doAnnotations() {
			// Check which annotations are checked (chosenAnnotators) but not yet annotated
			needToAnnotate = new BitSet();
			needToAnnotate.or(chosenAnnotators);
			needToAnnotate.andNot(annSet.typeBits());
			doneBits = new BitSet();
			if (!needToAnnotate.isEmpty()) {
				setAnnotateButtonState(ANN_RUNNING);
				int i = -1;			
				while ((i = needToAnnotate.nextSetBit(i + 1)) >= 0) {
					U.log("Try to run Annotator " + Annotator.getName(i));
					Annotator ann = Annotator.makeAnnotator(Annotator.getName(i), thisWindow);
					ann.addPropertyChangeListener(this);
					ann.execute();
				}
			}
		}


		public void setAnnotateButtonState(int state) {
			if (state == ANN_RUNNING) {
				doit.setEnabled(false);
				doit.setText(annotateButtonLabelRunning);
			} else {
				doit.setEnabled(true);
				doit.setText(annotateButtonLabel);
			}
		}
		
		public ArrayList<String> getSelectedMethods() {
			ArrayList<String> ans = new ArrayList<String>();
			for (SelectPanel p: panels) {
				if (p.cb.getState()) ans.add(p.cb.getLabel());
			}
			return ans.size() > 0 ? ans : null;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			String prop = evt.getPropertyName();
			// Only pay attention if property matches one of the annotatorTypes
			int index = getPanelIndex(prop);
			if (index >= 0) {
				int percent = (int)evt.getNewValue();
				if (percent < 0) setIndeterminateProgress(prop, true);
				else setProgress(prop, percent);	
			}
		}
		
		/**
		 * Returns the index of the panel whose button label is annotatorType
		 * @param annotatorType The desired Button label
		 * @return index of the panel, or -1 if no match.
		 */
		public int getPanelIndex(String annotatorType) {
			Integer ans = Annotator.getIndex(annotatorType);
			return (ans == null) ? -1 : (int)ans;
		}
//		public int getPanelIndex(String annotatorType) {
//			for (int i = 0; i < panels.length; i++) {
//				if (panels[i].cb.getLabel().equals(annotatorType)) return i;
//			}
//			return -1;
//		}
		
		public void setProgress(int annotatorIndex, int percent) {
			JProgressBar pb = panels[annotatorIndex].pb;
			if (pb.isIndeterminate()) pb.setIndeterminate(false);
			pb.setValue(percent);
		}
		
		public void setProgress(String annotatorType, int percent) {
			int index = getPanelIndex(annotatorType);
			if (index >= 0) setProgress(index, percent);
		}
		
		public void setIndeterminateProgress(int index, boolean val) {
			panels[index].pb.setIndeterminate(val);
		}
		
		public void setIndeterminateProgress(String annotatorType, boolean val) {
			int index = getPanelIndex(annotatorType);
			if (index >= 0) setIndeterminateProgress(index, val);
		}

		protected class SelectPanel extends JPanel {

			private static final long serialVersionUID = 1L;
			int colorIndex;
			Checkbox cb;
			JProgressBar pb;

			/**
			 * Create the panel.
			 */
			public SelectPanel(String name, int colorNumber) {
				colorIndex = colorNumber;
				setPreferredSize(new Dimension(spPrefW, spPrefH));
				setMinimumSize(new Dimension(spMinW, spPrefH));
				setBorder(new EmptyBorder(6, 6, 6, 6));
				setBackground(Color.WHITE);
				setLayout(new GridLayout(0, 1, 0, 0));

				cb = new Checkbox(name);
				cb.setState(true);
				chosenAnnotators.set(Annotator.getIndex(name));
				cb.setBackground(AnnotationHighlight.getColor(colorNumber));
				cb.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						String source = ((Checkbox)e.getItemSelectable()).getLabel();
						Integer annotationTypeIndex = Annotator.getIndex(source);
						if (annotationTypeIndex == null) {
							// Ignore selection of anything other than the possible Annotators.
							return;
						}
						if (e.getStateChange() == ItemEvent.DESELECTED) {
							chosenAnnotators.clear(annotationTypeIndex);
						} else {
							chosenAnnotators.set(annotationTypeIndex);
						}
						showAnnotations();
					}
				});
				add(cb);

				pb = new JProgressBar();
				pb.setStringPainted(true);
				pb.setBackground(Color.WHITE);
				add(pb);
				
				panels[colorNumber] = this;
			}
		}
	}
	
}
