package edu.mit.csail.medg.thesmap;


import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
 * This class is similar to UMLSWindow but instead of having everything be interactive. 
 * The point of the DirectoryWindow will be to simply run the annotators over the files
 * in a particular directory. 
 * 
 * @author mwc
 *
 */
public class DirectoryWindow extends JFrameW 
	implements Runnable
	, ClipboardOwner
	{
	
	// The collection of all windows is defined in JListedFrame:
	// public static ArrayList<UmlsWindow> windows = new ArrayList<UmlsWindow>();
	public static final String defaultTitle = "Batch ThesMap";
	public static final String annotateButtonLabel = "Annotate";
	public static final String annotateButtonLabelDirectoryMissing = "Please select directory...";
	public static final String annotateButtonLabelRunning = "Annotating...";
	public static final int ANN_RUNNING = 1;
	public static final int ANN_STOPPED = 0;
	public static final String blankExpl = "\n\n\n\n\n";
	public static final int nColors = 20;
	
	// The data source:
	private File fileDirectory = null;
	private URI inputUri = null;
	private DirectoryWindow thisWindow = null;		// self-reference
	
	// The interpretations:
	public AnnotationSet annSet = null;	
	BitSet chosenAnnotators = new BitSet();
	
	// Creating a UmlsWindow doesn't start running it;
	// We invokeLater to do so, as it is Runnable.
	// Thus, the loading of the source happens in the new thread.

	public DirectoryWindow() {
		super(defaultTitle);
		thisWindow = this;
		
		// Default file directory is the current directory.
		fileDirectory = new File("").getAbsoluteFile();
	}
	
	public DirectoryWindow(URI uri) {
		super(uri.getPath());
		inputUri = uri;
		thisWindow = this;
		fileDirectory = new File("").getAbsoluteFile(); 
	}

	@Override
	public void run() {
		thisWindow = this;
		super.run();
		Dimension frameDim = getContentPane().getSize();
		Dimension desired = new Dimension(2 * frameDim.width / 3, 2 * frameDim.height / 3);
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
				SwingUtilities.invokeLater(new DirectoryWindow());
			}
		});
		fileMenu.add(newMI);
		
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

		// 1. Select the directory to annotate.
		directoryPane = new JTextArea(1,30);
		directoryPane.setEditable(false);
		directoryPane.setText(new File("").getAbsoluteFile().getAbsolutePath());
 
        JButton browseButton = new JButton("Browse");
        // When the browse button is pressed, a JFileChooser is created to select the directory.
        browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	            if (e.getActionCommand().equals("Browse")) {
	            	if (directoryChooser == null) {
		        		directoryChooser = new JFileChooser();
		        		directoryChooser.setCurrentDirectory(new java.io.File("."));
		        		directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		        		directoryChooser.setAcceptAllFileFilterUsed(false);
	            	}

	        		int returnVal = directoryChooser.showDialog(DirectoryWindow.this,
                             "Open");
	        		
	        		if (returnVal == JFileChooser.APPROVE_OPTION) {
		            	fileDirectory = directoryChooser.getSelectedFile();
		            	directoryPane.setText(fileDirectory.getAbsolutePath());
		            	System.out.println("Directory selected: " + fileDirectory);
	                } else {
		            	System.out.println("No directory selected: " + fileDirectory);
	                }
	            }
	            else {
	            	System.out.println("nothing pressed" + e.getActionCommand());
	            }
	         }
		});
        
        // Create the panel to browse for the directory of choice.
        topPanel = new JPanel();
        topPanel.add(directoryPane);
        topPanel.add(browseButton);
		
		// 2. Create the lookup method selector
		methodChooser = new MethodChooser();

		// 3. Main panel
		mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, methodChooser);
		//mainPanel.setResizeWeight(0.3);
		add(mainPanel);
	}
	
	// Components of the interface
	JSplitPane mainPanel;
	JFileChooser directoryChooser;
	JTextArea directoryPane;
	JPanel topPanel;
	MethodChooser methodChooser;
	

	public void setSizeAndLocation() {
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setLocation(originX, originY);
	}
		
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	public static void saveAnnotations(File chosenFile, AnnotationSet annSet) {
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
							osw.write(ann.begin + "," + ann.end + "," + i.cui + "," + i.tui 
									+ ",\"" + fixq(preferredText) + "\"," + i.type + "\n");
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
	}
	
	static String fixq(String str) {
		return str.replaceAll("\"", "\"\"");
	}
	
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
	
	
	// Default parameters of the window
	public static final int width = 500;
	public static final int height = 200;
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

	public void annotatorDone(String annotatorName) {
	//	methodChooser.annotatorDone(annotatorName);
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
		private static final int spPrefW = 100;
		private static final int spMinW = 50;
		private static final int spPrefH = 20;
		protected SelectPanel[] panels;
		BitSet needToAnnotate;
		protected BitSet doneBits;
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
			doit = new JButton("Annotate directory");
			doit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// Check to see if a directory has been selected.
					//TODO(mwc): Check to see if a directory has been selected.
					
					// Run annotations.
					File folder = new File(fileDirectory.getPath());
					File[] listOfFiles = folder.listFiles();

				    for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) {
							String fileName = listOfFiles[i].getPath();
							U.log("Currently processing: " + fileName );
							SwingUtilities.invokeLater(new UmlsDocument(new File(fileName), chosenAnnotators, doneBits));
						}
				    }
				}
			});
			add(doit, BorderLayout.SOUTH);
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
				setLayout(new GridLayout(0, 1, 0, 0));

				cb = new Checkbox(name);
				cb.setState(true);
				chosenAnnotators.set(Annotator.getIndex(name));
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
						
					}
				});
				add(cb);
				
				panels[colorNumber] = this;
			}
		}
	}
	
}
