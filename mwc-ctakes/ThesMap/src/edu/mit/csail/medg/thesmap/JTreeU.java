package edu.mit.csail.medg.thesmap;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/** JTreeU implements a specialization of JTree that supports selection of any
 * subset of the nodes of the tree.  It should probably be simple to achieve this
 * in JTree, but I have not been able to figure out how to do it easily, hence 
 * that behavior is being encapsulated here.  
 * 
 * The desired behavior:
 * 1. Clicking on any unselected node in the tree selects that node and all its children.
 * 2. Clicking on any selected node de-selects it and all its children.
 * 3. Multiple selections are always allowed.
 * 
 * As a consequence of the above, one can create selections where a subtree is selected,
 * then some of its subtrees or nodes are de-selected.  If the node is then clicked, all
 * its subtrees are de-selected.  And vice versa.
 * 
 * Default behavior is that all nodes are expanded (visible).
 * 
 */

/**
 * @author psz
 *
 */
public class JTreeU extends JTree implements MouseListener{

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public JTreeU() {
		commonInit();
	}

	/**
	 * @param value
	 */
	public JTreeU(Object[] value) {
		super(value);
		commonInit();
	}

	/**
	 * @param value
	 */
	public JTreeU(Vector<?> value) {
		super(value);
		commonInit();
	}

	/**
	 * @param value
	 */
	public JTreeU(Hashtable<?, ?> value) {
		super(value);
		commonInit();
	}

	/**
	 * @param root
	 */
	public JTreeU(TreeNode root) {
		super(root);
		commonInit();
	}

	/**
	 * @param newModel
	 */
	public JTreeU(TreeModel newModel) {
		super(newModel);
		commonInit();
	}

	/**
	 * @param root
	 * @param asksAllowsChildren
	 */
	public JTreeU(TreeNode root, boolean asksAllowsChildren) {
		super(root, asksAllowsChildren);
		commonInit();
	}
	
	/**
	 * Common initialization for any way to create a JTreeU.
	 * This sets the discontiguous selection mode,
	 * makes itself a mouse listener,
	 * changes the UI so that any mouse event toggles the selection,
	 * and expands the whole tree.
	 */
	private void commonInit() {
//		setFont(getFont().deriveFont(8f));    
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		addMouseListener(this);
		setUI(new MultiSelectionTreeUI());
		for (int i = 0; i < getRowCount(); i++) expandRow(i);
	}
	
	/**
	 * Determines if treePath is currently selected.
	 * @param treePath the path to check
	 * @return boolean whether it is selected
	 */
	public boolean isSelected(TreePath treePath) {
		return isSelected(treePath, getSelectionPaths());
	}
	
	/**
	 * Determines if treePath is currently selected.
	 * This is an optimization on the single-argument version,
	 * to allow fetching the array of selected paths only once.
	 * @param treePath treePath the path to check
	 * @param allSelected array of selected paths
	 * @return whether it is selected
	 */
	public boolean isSelected(TreePath treePath, TreePath[] allSelected) {
		if (allSelected != null)
			for (int i = 0; i < allSelected.length; i++)
				if (treePath.equals(allSelected[i])) return true;
		return false;

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// We manage selection ourselves, except that JTree has
		// already toggled the selection of the TreePath that was
		// clicked on. We propagate that to its children.
		TreePath thisPath = getPathForLocation(e.getX(), e.getY());
		if (thisPath != null) {
			if (isSelected(thisPath)) selectTree(thisPath);
			else deselectTree(thisPath);
		}
	}
	
	/**
	 * Recursively adds all nodes starting at path to JTreeU selection.
	 * Made complex by trying to do them all at once, to reduce firing listeners.
	 * @param path
	 * @param allSelected
	 */
	public void selectTree(TreePath path) {
		ArrayList<TreePath> pathsToAdd = new ArrayList<TreePath>(); 
		traceSubtrees(path, pathsToAdd, true);
		if (pathsToAdd.size() > 0) {
			TreePath[] thePaths = new TreePath[pathsToAdd.size()];
			addSelectionPaths(pathsToAdd.toArray(thePaths));
		}
	}
	
	public void deselectTree(TreePath path) {
		ArrayList<TreePath> pathsToAdd = new ArrayList<TreePath>(); 
		traceSubtrees(path, pathsToAdd, false);
		if (pathsToAdd.size() > 0) {
			TreePath[] thePaths = new TreePath[pathsToAdd.size()];
			removeSelectionPaths(pathsToAdd.toArray(thePaths));
		}
	}
	
	public void traceSubtrees(TreePath path, ArrayList<TreePath> paths, boolean select) {
		boolean isSel = isSelected(path);
		if ((select && !isSel) || (!select && isSel)) paths.add(path);
		TreeNode node = (TreeNode) path.getLastPathComponent();
		for (int i = 0; i < node.getChildCount(); i++) {
			traceSubtrees(path.pathByAddingChild(node.getChildAt(i)),  paths, select);
		}
	}
	
	public void deselectAll() {
		clearSelection();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	public class MultiSelectionTreeUI extends BasicTreeUI
	{

	    @Override
	    protected boolean isToggleSelectionEvent( MouseEvent event )
	    {
	        return 
	        		(SwingUtilities.isLeftMouseButton( event ) ||
	        		SwingUtilities.isMiddleMouseButton(event) ||
	        		SwingUtilities.isRightMouseButton(event));
	    }
	}


}
