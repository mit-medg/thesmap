package edu.mit.csail.medg.thesmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class SemanticTree extends JTreeU {

	private static final long serialVersionUID = 1L;
	private HashMap<String, ArrayList<String>> selectFileTuis;
	private String currentTUISelection;
	private ArrayList<String> deselectedTUIs;
	
	
	public SemanticTree(SemanticEntity top) {
		super(top);
		selectFileTuis = new HashMap<String, ArrayList<String>>();
		loadFile("ASD");
	}
	
	/**
	 * Loads a file that tells you what TUIs should be selected.
	 * @param inFile
	 */
	private void loadFile(String inFile) {
		// Opens the file 
		
		// Loads the tuis in the file
		
		// Currently test with a forced one.
		//String[] arr = {"T079", "T064", "T066"};
		ArrayList<String> currTuis = new ArrayList<String>();
		currTuis.add("T079");
		currTuis.add("T064");
		currTuis.add("T066");
		selectFileTuis.put(inFile, currTuis);
	}
	
	/** 
	 * Set the current TUI selection to what is selected by the dropdown. 
	 * If nothing is selected, then all TUIs are acceptable.
	 * @param listSelection
	 */
	public void setCurrentTuiSelection(String listSelection) {
		if (selectFileTuis.containsKey(listSelection)) {
			currentTUISelection = listSelection;
			deselectedTUIs = selectFileTuis.get(listSelection);
		} else {
			currentTUISelection = "All";
			deselectedTUIs.clear();
		}
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

	/** 
	 * Find the Treepath of the particular TUI string.
	 * @param root
	 * @param s
	 * @return TreePath - TreePath where the Tui is located.
	 */
	private TreePath find(SemanticEntity root, String s) {
	    @SuppressWarnings("unchecked")
	    Enumeration<SemanticEntity> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	    	SemanticEntity node = e.nextElement();
	        if (node.getTuiString().equalsIgnoreCase(s)) {
	            return new TreePath(node.getPath());
	        }
	    }
	    return null;
	}
	
	/**
	 * Update which Tuis are selected.
	 */
	public void updateCurrentTuiSelection() {
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		TreePath[] paths = new TreePath[deselectedTUIs.size()];
		int count = 0;
		for (String tui: deselectedTUIs) {
			TreePath path = find((SemanticEntity) this.treeModel.getRoot(), tui);
			paths[count++] = path;
		}
		this.setSelectionPaths(paths);
	}
	

}
