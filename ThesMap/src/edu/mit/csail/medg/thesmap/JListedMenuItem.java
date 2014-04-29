package edu.mit.csail.medg.thesmap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;


public class JListedMenuItem extends JMenuItem implements ActionListener {

	private static final long serialVersionUID = 1L;

	protected JListedFrame myFrame = null;
	
	public JListedMenuItem(String name, JListedFrame backpointer) {
		super(name);
		myFrame = backpointer;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		myFrame.setVisible(true);
		myFrame.toFront();
	}
}
