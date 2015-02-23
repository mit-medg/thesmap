package edu.mit.csail.medg.thesmap;

import java.awt.GraphicsConfiguration;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class AnnotationsWindow extends JFrameW implements ClipboardOwner{
	
	public static final String defaultTitle = "Annotations";
	private static final long serialVersionUID = 1L;
	
	public static final int width = 500;
	public static final int height = 500;
	public static final int originX = 0;
	public static final int originY = 1000;
	
	JTextArea textArea = null;
	JFrame myFrame = null;

	public AnnotationsWindow() {
		super(defaultTitle);
		myFrame = this;
	}

	public AnnotationsWindow(GraphicsConfiguration gc) {
		super(gc);
		myFrame = this;
	}

	public AnnotationsWindow(String title) {
		super(title);
		myFrame = this;
	}

	public AnnotationsWindow(String title, GraphicsConfiguration gc) {
		super(title, gc);
		myFrame = this;
	}

	@Override
	public void initializeMenus() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu windowMenu = makeWindowMenu();
		
		fileMenu.add(makeClose());
		menuBar.add(fileMenu);
		menuBar.add(windowMenu);
		setJMenuBar(menuBar);
	}

	@Override
	public void initializeContent() {
		textArea = new JTextArea("");
		JScrollPane textAreaScroll = 
				new JScrollPane(textArea, 
						JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
						JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		add(textAreaScroll);
	}

	@Override
	public void setSizeAndLocation() {
		setLocation(800,0);
		setSize(500,500);		
	}
	
	public void setText(String s) {
		textArea.setText(s);
		textArea.getCaret().setDot(0);
	}
	
	public String getText() {
		return textArea.getText();
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Nothing to do!
	}
	

}
