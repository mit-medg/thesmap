package edu.mit.csail.medg.thesmap;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;

/**
 * JListedFrame manages maintenance of the set of WindowMenuItems that appear under
 * the Window menu of a Java application so that appropriate items are added and removed
 * as JListedFrames are created and destroyed.
 * This manages its own Widow menu and its MenuItems, but expects to be overridden
 * so that the Window menu can be incorporated into a MenuBar of the application that
 * is using this.
 * @author psz
 *
 */
public class JListedFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public static ArrayList<JListedFrame> allWindows = new ArrayList<JListedFrame>();
	
	public static boolean quitAfterClosingAll = true;

	public JMenu myWindowMenu = null;
	
	
	public JListedFrame() {
		super();
		initialize();
	}
	
	public JListedFrame(GraphicsConfiguration gc) {
		super();
		initialize();
	}
	
	public JListedFrame(String title) {
		super(title);
		initialize();
	}
	
	public JListedFrame(String title, GraphicsConfiguration gc) {
		super(title, gc);
		initialize();
	}
	
	private void initialize () {
		allWindows.add(this);
		makeWindowMenu();
	}
	
	/**
	 * Remove myself from all the Window menus of other JListedFrames
	 * If I am the last window and quitAfterClosingAll is true, then 
	 * tell TheMap to close as well.
	 */
	public void close() {
		for (JListedFrame f: JListedFrame.allWindows) {
			if (this != f) {	// Don't bother for my own self
				JMenu menu = f.myWindowMenu;
				for (int mi = 0; mi < menu.getMenuComponentCount(); mi++) {
					Component c = menu.getComponent(mi);
					if (c instanceof JListedMenuItem) {
						JListedMenuItem wm = (JListedMenuItem) c;
						if (wm.myFrame == f) {
							menu.remove(wm);
							break;
						}
					}
				}
			}
		}
		allWindows.remove(this);
		dispose();
		if (quitAfterClosingAll && allWindows.size() <= 0) 
			ThesMap.close();
	}
	
	/**
	 * Creates the JMenu to represent the windows for the entire
	 * application, based on the current windows in allWindows.
	 * Sets myWindowMenu to this newly created one.
	 */
	public JMenu makeWindowMenu() {
		myWindowMenu = new JMenu("Window");
		// For each existing JListedFrame, add a Window menu item
		// to select this one.
		for (JListedFrame f: allWindows) {
			if (this != f)
				f.myWindowMenu.add(new JListedMenuItem(getTitle(), this));
		}
		// Create the JMenu for this JListedFrame
		// Note that we include a disabled one for self.
		myWindowMenu = new JMenu("Window");
		for (JListedFrame f: allWindows) {
			// Add a JMenuItem for each existing JListedFrame
			JListedMenuItem jlmi = new JListedMenuItem(f.getTitle(), f);
			if (this == f) jlmi.setEnabled(false);
			myWindowMenu.add(jlmi);
		}
		return myWindowMenu;
	}
	

}
