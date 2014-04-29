package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;

import javax.swing.tree.TreePath;

public class SemanticTree extends JTreeU {

	private static final long serialVersionUID = 1L;
	
	public SemanticTree(SemanticEntity top) {
		super(top);
	}
	
	public ArrayList<String> getSelectedTuis() {
		ArrayList<String> selectedTuis = null;
		TreePath[] selectedSems = getSelectionPaths();
		if (selectedSems != null) {
			selectedTuis = new ArrayList<String>();
			for (int i = 0; i < selectedSems.length; i++) {
				TreePath tp = selectedSems[i];
				SemanticEntity sem = (SemanticEntity)tp.getLastPathComponent();
				selectedTuis.add(sem.tui);
			}
		}
		return selectedTuis;
	}

}
