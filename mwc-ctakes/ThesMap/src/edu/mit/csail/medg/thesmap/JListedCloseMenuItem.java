package edu.mit.csail.medg.thesmap;
import java.awt.event.ActionEvent;


/** JListedCloseMenuItem is a specialization of JListedFrame that works with
 * JListedFrame, which keeps track of the open "windows" in an application and
 * updates the Window menu each time a new window is created or closed.
 * @author psz
 *
 */
public class JListedCloseMenuItem extends JListedMenuItem {
	private static final long serialVersionUID = 1L;

	// This should never be called, unless we want a name other than "Close".
	public JListedCloseMenuItem(String name, JListedFrame backpointer) {
		super(name, backpointer);
	}

	public JListedCloseMenuItem(JListedFrame backpointer) {
		super("Close", backpointer);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Close my JListedFrame, and that will adjust all the
		// Window JMenus to no longer list this frame.
		myFrame.close();
	}
}
