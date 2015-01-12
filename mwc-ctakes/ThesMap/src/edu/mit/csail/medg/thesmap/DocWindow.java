package edu.mit.csail.medg.thesmap;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URI;

public class DocWindow extends JFrameW {
	
	private static final long serialVersionUID = 1L;
	DocWindow myself;
	public static String defaultTitle = "Thesaurus Map";
	
	public DocWindow() {
		super(defaultTitle);
	}
	
	public DocWindow(File file) {
		super(file.getName());
		myself = this;
	}
	
	public DocWindow(URI uri) {
		super(uri.getQuery());
		myself = this;
	}
	
//	public DocWindow(Subject subj) {
//		super("Subject " + subj.id);
//		myself = this;
//	}
	

	@Override
	public void initializeMenus() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeContent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSizeAndLocation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// TODO Auto-generated method stub
		
	}

}
