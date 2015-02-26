package edu.mit.csail.medg.thesmap;


import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class is similar to UMLSWindow but instead of having everything be interactive. 
 * The point of the DirectoryWindow will be to simply run the annotators over the files
 * in a particular directory or set of files given by a SQL command. 
 * 
 * @author mwc
 *
 */
public class BatchWindow extends JFrameW 
	implements Runnable
	, ClipboardOwner
	, PropertyChangeListener
	{
	
	// The collection of all windows is defined in JListedFrame:
	// public static ArrayList<UmlsWindow> windows = new ArrayList<UmlsWindow>();
	public static final String defaultTitle = "Batch ThesMap";
	public static final String annotateButtonLabel = "Annotate";
	public static final String annotateButtonLabelDirectoryMissing = "Please select directory...";
	public static final String annotateButtonLabelRunning = "Annotating...";
	public static final int ANN_RUNNING = 1;
	public static final int ANN_STOPPED = 0;
	public static final int nColors = 20;
	
	// The data source:
	private File fileDirectory = null;
	private URI inputUri = null;
	private BatchWindow thisWindow = null;		// self-reference
	
	// The interpretations:
	public AnnotationSet annSet = null;	
	BitSet chosenAnnotators = new BitSet();
	
	// Number of files to process. 
	public int numFilesTotal = 0;
	public int numFilesProcessed = 0;
	
	// Keep track of what the selected index is for the tabbed pane.
	public int currentMode = 0;
	public static final int BROWSE_MODE = 0;
	public static final int CMD_MODE = 1;
	
	// Source for database.
	private DBConnectorOpen dbConnector;

	public BatchWindow() {
		super(defaultTitle);
		thisWindow = this;
		
		// Default file directory is the current directory.
		fileDirectory = new File("").getAbsoluteFile();
	}
	
	public BatchWindow(URI uri) {
		super(uri.getPath());
		inputUri = uri;
		thisWindow = this;
		fileDirectory = new File("").getAbsoluteFile(); 
	}

	@Override
	public void run() {
		thisWindow = this;
		super.run();
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
				SwingUtilities.invokeLater(new BatchWindow());
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

	// Components of the interface
	JTabbedPane mainTabbedPane;
	JPanel mainPanel;
	JSplitPane browsePane;
	JFileChooser directoryChooser;
	JTextArea directoryPane;
	JPanel topPanel;
	MethodChooser methodChooser;
	JProgressBar pb;
	JPanel bottomPanel;
	
	// Contents for the panel with sql command.
	JLabel sqlCmdLabel;
	JLabel dbLabel;
	JLabel dbHostLabel;
	JLabel dbUserLabel;
	JLabel dbPwdLabel;
	JPanel sqlTabPane;
	JTextArea sqlText;
	JTextArea dbHostText;
	JTextArea dbText;
	JTextArea dbUserText;
	JTextArea dbPwdText;
	
	/**
	 * Create and lay out the content of the window.  The content is in two columns separated by
	 * a SplitPane so it's user-adjustable.  On the left is the SemanticTree to allow selection of
	 * the annotation types to show and, below it, a MethodChooser that allows selection of the 
	 * annotation methods to use.  On the right is a title
	 */
	public void initializeContent() {
		mainPanel = new JPanel();
		mainPanel.setLayout( new BorderLayout() );
		getContentPane().add( mainPanel );
		
		// Create the various components of the interface:
		createBrowseTab();
		createCommandTab();
		
		// Add tabbed panes to the main one. 
		mainTabbedPane = new JTabbedPane();
		mainTabbedPane.add("Browse", browsePane);
		mainTabbedPane.add("SQL Command", sqlTabPane);
		mainPanel.add(mainTabbedPane, BorderLayout.CENTER);
		
		mainTabbedPane.addChangeListener(new ChangeListener()
	    {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (mainTabbedPane.getSelectedIndex() == BROWSE_MODE) {
					currentMode = 0; 
				} else if (mainTabbedPane.getSelectedIndex() == CMD_MODE) {
					currentMode = 1;
				}
			}
	    });
	}
	
	/** 
	 * Create a tabbed pane for the use case of batch processing a particular directory.
	 */
	public void createBrowseTab() {

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

	        		int returnVal = directoryChooser.showDialog(BatchWindow.this,
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
		
		// 3. Progress bar.
		pb = new JProgressBar();
		pb.setStringPainted(true);
		
		// Create the bottom panel.
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(methodChooser, BorderLayout.CENTER);
		bottomPanel.add(pb, BorderLayout.SOUTH);

		// 4. Main panel
		browsePane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
	}
	
	/**
	 * Create a tabbed pane for the use case of batch processing using a SQL command on a particular db.
	 */
	private void createCommandTab() {
		dbHostLabel = new JLabel("Host Name: ");
		dbHostText = new JTextArea(1,30);
		dbHostText.setEditable(true);
		
		dbLabel = new JLabel("DB Name: ");
		dbText = new JTextArea(1,30);
		dbText.setEditable(true);

		dbUserLabel = new JLabel("User Name: ");
		dbUserText = new JTextArea(1,30);
		dbUserText.setEditable(true);
		
		dbPwdLabel = new JLabel("Password: ");
		dbPwdText = new JTextArea(1,30);
		dbPwdText.setEditable(true);
		
		// Load the default properties for the database to use.
		loadDefaultDB();
		
		sqlCmdLabel = new JLabel("SQL Command:");
		sqlText = new JTextArea(1,30);
		sqlText.setEditable(true);
		// Set an empty SQL command as default.
		sqlText.setText("");
		
		// 2. Create the annotator selector.
		methodChooser = new MethodChooser();
		
		// 3. Progress bar.
		pb = new JProgressBar();
		pb.setStringPainted(true);
		
		// Create the bottom panel.
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(methodChooser, BorderLayout.CENTER);
		bottomPanel.add(pb, BorderLayout.SOUTH);
		
		sqlTabPane = new JPanel();
		GroupLayout layout = new GroupLayout(sqlTabPane);
		sqlTabPane.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(dbHostLabel)
						.addComponent(dbLabel)
						.addComponent(dbUserLabel)
						.addComponent(dbPwdLabel)
						.addComponent(sqlCmdLabel))
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(dbHostText)
						.addComponent(dbText)
						.addComponent(dbUserText)
						.addComponent(dbPwdText)
						.addComponent(sqlText)))
				.addComponent(bottomPanel))
		);
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		        .addComponent(dbHostLabel)
		        .addComponent(dbHostText))
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(dbLabel)
                  .addComponent(dbText))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(dbUserLabel)
                  .addComponent(dbUserText))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(dbPwdLabel)
                  .addComponent(dbPwdText))
              .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                  .addComponent(sqlCmdLabel)
                  .addComponent(sqlText))
              .addComponent(bottomPanel)
		);
	}

	private void loadDefaultDB() {
		ThesProps prop = ThesMap.prop;
		dbHostText.setText(prop.getProperty(ThesProps.sourceHostName));
		dbText.setText(prop.getProperty(ThesProps.sourceDbName));
		dbUserText.setText(prop.getProperty(ThesProps.sourceUserName));
		dbPwdText.setText(prop.getProperty(ThesProps.sourcePasswordName));
	}
	
	public void setSizeAndLocation() {
		setDefaultLookAndFeelDecorated(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setLocation(originX, originY);
	}
		
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

	
	public void setAnnotateButtonState(int state) {
		methodChooser.setAnnotateButtonState(state);
	}
	
	public void integrate(Annotation ann) {
		annSet.integrate(ann);
	}
	
	
	// Default parameters of the window
	public static final int width = 500;
	public static final int height = 350;
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
	public void propertyChange(PropertyChangeEvent evt) {
		// This file should be equal to the file name. 
		// TODO(mwc): Check to see if the file is one that hasn't been flagged yet.
		//String prop = evt.getPropertyName();
		
		if (evt.getNewValue().equals("done")) {
			updateProgress();
		}
		
		// Allow for next batch processing to be done if we are done with the previous set.
		if (numFilesProcessed == numFilesTotal) {
			setAnnotateButtonState(ANN_STOPPED);
		}
	}
	
	public void updateProgress() {
		numFilesProcessed ++; 
		System.out.println("Done processing " + numFilesProcessed+ "/" + numFilesTotal);
		int percentage = (int)Math.round(new Double(numFilesProcessed) / numFilesTotal * 100.0);
		pb.setValue(percentage);
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
	protected class MethodChooser extends JPanel{
		
		private static final long serialVersionUID = 1L;
		static final int numberOfSelectorColumns = 2;
//		private static final int gridSpace = 6;
		private static final int spPrefW = 100;
		private static final int spMinW = 50;
		private static final int spPrefH = 30;
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
			doit = new JButton("Batch Annotate");
			doit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setAnnotateButtonState(ANN_RUNNING);
					// Reset the count of files processed to 0.
					numFilesTotal = 0; 
					numFilesProcessed = 0; 
					
					if (currentMode == BROWSE_MODE) {
						// Run annotations.
						File folder = new File(fileDirectory.getPath());
						File[] listOfFiles = folder.listFiles();
						
					    for (int i = 0; i < listOfFiles.length; i++) {
							if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) {
								String fileName = listOfFiles[i].getPath();
								U.log("Currently processing: " + fileName );
								UmlsDocument currentDocument = new UmlsDocument(thisWindow, new File(fileName), chosenAnnotators, doneBits);
								numFilesTotal++; 
								currentDocument.addPropertyChangeListener(thisWindow);
								currentDocument.execute();
							}
					    }
					} else if (currentMode == CMD_MODE) {
						dbConnector = new DBConnectorOpen(dbHostText.getText(), dbText.getText(), dbUserText.getText(), dbPwdText.getText());
						ResultSet rs = dbConnector.processSQL(sqlText.getText());
						try {						
							// Process the files accordingly.
							while (rs.next()) {
								String docId = rs.getString("DOCID");
								String text = rs.getString("TEXT");
								UmlsDocument currentDocument = new UmlsDocument(thisWindow, text, docId, chosenAnnotators, doneBits);
								numFilesTotal++; 
								currentDocument.addPropertyChangeListener(thisWindow);
								currentDocument.execute();
							}
						} catch (SQLException except) {
							U.log("Incorrect SQL command" + except.getMessage());
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

		/**
		 * Returns the index of the panel whose button label is annotatorType
		 * @param annotatorType The desired Button label
		 * @return index of the panel, or -1 if no match.
		 */
		public int getPanelIndex(String annotatorType) {
			Integer ans = Annotator.getIndex(annotatorType);
			return (ans == null) ? -1 : (int)ans;
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
