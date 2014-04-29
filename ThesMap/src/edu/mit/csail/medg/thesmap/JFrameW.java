package edu.mit.csail.medg.thesmap;


import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public abstract class JFrameW extends JFrame 
	implements Runnable, ClipboardOwner {

	private static final long serialVersionUID = 1L;
	
	public static ArrayList<JFrameW> windows = new ArrayList<JFrameW>();

	public JMenu myWindowMenu = null;

	public JFrameW() throws HeadlessException {
		super();
		init();
	}

	public JFrameW(GraphicsConfiguration gc) {
		super(gc);
		init();	
	}

	public JFrameW(String title) throws HeadlessException {
		super(title);
		init();
	}

	public JFrameW(String title, GraphicsConfiguration gc) {
		super(title, gc);
		init();
	}
	
	void init() {
		windows.add(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new JFrameWindowAdapter());
	}
	
	public void run() {
		setSizeAndLocation();
		initializeMenus();
		initializeContent();
		setVisible(true);
	}
	
	public abstract void initializeMenus();
	
	public abstract void initializeContent();
	
	public abstract void setSizeAndLocation(); 
	
	/**
	 * Creates the JMenu to represent the windows for the entire
	 * application, based on the current windows in allWindows.
	 * Sets myWindowMenu to this newly created one.
	 */
	public JMenu makeWindowMenu() {
		myWindowMenu = new JMenu("Window");
//		System.out.println("Number of windows = " + windows.size());
		// For each existing JListedFrame, add a Window menu item
		// to select this one.
		for (JFrameW f: windows) {
//			System.out.println(f.getTitle());
			if (this != f)
				f.myWindowMenu.add(new JMenuItemW(getTitle(), this));
		}
		// Create the JMenu for this JListedFrame
		// Note that we include a disabled one for self.
		myWindowMenu = new JMenu("Window");
		for (JFrameW f: windows) {
			// Add a JMenuItem for each existing JListedFrame
			JMenuItemW jlmi = new JMenuItemW(f.getTitle(), f);
			if (this == f) jlmi.setEnabled(false);
			myWindowMenu.add(jlmi);
		}
		return myWindowMenu;
	}
	
	/**
	 * Creates a Close menu item that should go into the Edit menu
	 * created by the instance's initializeMenus.
	 */
	public JMenuItemCloseW makeClose() {
		JMenuItemCloseW mi = new JMenuItemCloseW(this);
		return mi;
	}
	
	/**
	 * Closes the current JFrameW and removes the Window menu items
	 * (JMenuItemW) that reference it from the Window menus of other
	 * JFrameW's.
	 */
	public void close() {
//		System.out.println("Close called on " + getTitle());
		forgetWindow();
		dispose();
	}
	
	private void forgetWindow() {
//		System.out.println("Forgetting " + getTitle());
		for (JFrameW f: windows) {
			if (this != f) { // Don't bother for my own Window menu
//				System.out.println("Remove Window Menu Item for " + getTitle() + " from " + f.getTitle());
				JMenu menu = f.myWindowMenu;
//				System.out.println("Number of Item in Window menu = " + menu.getItemCount());
				for (int mi = 0; mi < menu.getItemCount(); mi++) {
					JMenuItem c = menu.getItem(mi);
					if (c instanceof JMenuItemW) {
						if (((JMenuItemW)c).myFrame == this) {
//							System.out.println("Actually removing item " + mi + ": " + c.getText());
							menu.remove(mi);
							break;
						}
					}
				}
//				System.out.println("Window Menu of " + f.getTitle() + " retains " + f.myWindowMenu.getItemCount() + " elements.");
			}
		}
		windows.remove(this);
//		System.out.println("Remaining " + windows.size() + " windows:");
//		for (JFrameW f: windows) System.out.println(f.getTitle());
	}
	
	/**
	 * We define JMenuItemW as a JMenuItem that builds in an
	 * action that selects the associated JFrameW when selected.
	 */
	public class JMenuItemW extends JMenuItem implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		protected JFrameW myFrame = null;
		
		public JMenuItemW(String name, JFrameW myFrame) {
			super(name);
			this.myFrame = myFrame;
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Window selected: " + myFrame.getTitle());
			myFrame.setVisible(true);
			myFrame.toFront();
		}
	}
	
	public class JMenuItemCloseW extends JMenuItem implements ActionListener {

		private static final long serialVersionUID = 1L;
		protected JFrameW myFrame = null;
		
		public JMenuItemCloseW(JFrameW myFrame) {
			this("Close", myFrame);
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, accelMask));
			addActionListener(this);
		}

		public JMenuItemCloseW(String name, JFrameW myFrame) {
			super(name);
			this.myFrame = myFrame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
//			System.out.println("Closing " + myFrame.getTitle());
			myFrame.dispose();
		}
		
	}
	
	public class JFrameWindowAdapter extends WindowAdapter {
		public void windowClosed(WindowEvent e) {
			JFrameW me = ((JFrameW)e.getWindow());
//			System.out.println("WindowClosed event for " + me.getTitle());
			me.forgetWindow();
			super.windowClosed(e);
		}
	}
	
	static int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

}
