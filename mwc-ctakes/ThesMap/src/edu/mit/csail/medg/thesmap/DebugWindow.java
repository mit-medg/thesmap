package edu.mit.csail.medg.thesmap;

import java.awt.GraphicsConfiguration;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class DebugWindow extends JFrameW implements ClipboardOwner{

	private static final long serialVersionUID = 1L;
	
	JTextArea textArea = null;
	JFrameW myFrame = null;

	public DebugWindow() {
		super();
		myFrame = this;
	}

	public DebugWindow(GraphicsConfiguration gc) {
		super(gc);
		myFrame = this;
	}

	public DebugWindow(String title) {
		super(title);
		myFrame = this;
	}

	public DebugWindow(String title, GraphicsConfiguration gc) {
		super(title, gc);
		myFrame = this;
	}

	@Override
	public void initializeMenus() {

		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu windowMenu = makeWindowMenu();
		
		fileMenu.add(makeClose());
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		setJMenuBar(menuBar);
	
		// Edit menu has Cut, Copy, Paste, Clear and Select All
//		JMenuItem cutMI = new JMenuItem("Cut");
//		cutMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, accelMask));
//		cutMI.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// Cut the text to the clipboard.
//				String selection = textArea.getSelectedText();
//				if (selection.length() == 0) selection = textArea.getText();
//				getToolkit().getSystemClipboard().setContents(
//						new StringSelection(selection), myFrame);
//				textArea.replaceSelection(null);
//			}
//		});

		JMenuItem copyMI = new JMenuItem("Copy");
		copyMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, accelMask));
		copyMI.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Copy the text to the clipboard.
				String selection = textArea.getSelectedText();
				if (selection.length() == 0) selection = textArea.getText();
				getToolkit().getSystemClipboard().setContents(
						new StringSelection(selection), myFrame);
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
		
//		editMenu.add(cutMI);
		editMenu.add(copyMI);
//		editMenu.add(pasteMI);
//		editMenu.add(clearMI);
		editMenu.add(selectAllMI);

	}

	@Override
	public void initializeContent() {
		textArea = new JTextArea();
		JScrollPane textAreaScroll = 
				new JScrollPane(textArea, 
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(textAreaScroll);
	}

	@Override
	public void setSizeAndLocation() {
		setLocation(1100,100);
		setSize(500,200);		
	}
	
	public void println(String s) {
		textArea.setText(s);
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Nothing to do!
	}

}
