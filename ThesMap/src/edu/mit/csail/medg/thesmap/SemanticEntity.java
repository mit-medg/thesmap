package edu.mit.csail.medg.thesmap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A Semantic Entity represents one of the UMLS Semantic Types.
 * The types are named by TUIs, and have an abbreviation, a short name and
 * a description.
 * We are interested only in the Entity and Event trees, not the relations under
 * associated_with.  These two trees are distinct, with no common superclass in
 * the semantic network, so we have multiple tops.
 * @author psz
 *
 */
public class SemanticEntity extends DefaultMutableTreeNode implements TreeNode {
	
	public static HashMap<String, SemanticEntity> sems = 
			new HashMap<String, SemanticEntity>();
	public static HashMap<String, SemanticEntity> semsByName = 
			new HashMap<String, SemanticEntity>();
	public static HashMap<String, SemanticEntity> semsByAbr =
			new HashMap<String, SemanticEntity>();
	public static SemanticEntity top = null; 
	
	public String tui, name, def, abr;
	public SemanticEntity up;
	public ArrayList<SemanticEntity> down;
	
	public static final String getTuisQuery = "select ui, sty_rl, def, abr from srdef where rt='STY'";
	public static final String getIsaQuery = // "select ui1, ui3 from srstre1 where ui2='T186'";
			"select sty_rl1, sty_rl2 from srstr where rl='isa'";
//	public static final String getSemQuery =
//			"select d.ui, d.sty_rl, h.ui3 from srdef d join srstre1 h on d.ui=h.ui1 " +
//			"where h.ui2='T186' and d.rt='STY'";

	public SemanticEntity(ResultSet rs) {
		try {
			tui = rs.getString("UI");
			name = rs.getString("STY_RL");
			def = rs.getString("DEF");
			abr = rs.getString("ABR");
			up = null;
			down = new ArrayList<SemanticEntity>();
			sems.put(tui, this);
			semsByName.put(name, this);
			semsByAbr.put(abr, this);
		} catch (SQLException e) {
			System.err.println("Error in getting SemanticEntity from UMLS database: " + 
					e.getMessage());
			e.printStackTrace();
		}
	}
	
	public SemanticEntity(String tui, String name, String def, String abr) {
		this.tui = tui;
		this.name = name;
		this.def = def;
		this.abr = abr;
		this.up = null;
		this.down = new ArrayList<SemanticEntity>();
	}
	
	public static SemanticEntity semanticEntityFromAbr(String abr) {
		return semsByAbr.get(abr);
	}
	
	/**
	 * Caches the UMLS TUI hierarchy as a tree of SemanticEntity's, rooted
	 * at top.  If this has already been done, then skip re-doing it, because
	 * we assume that this hierarchy does not change during a run of the program.
	 * @param umls ResourceConnectorUmls that is an open connection to the
	 * UMLS metathesaurus database.
	 */
	public static void init(ResourceConnectorUmls umls) {
		if (top != null) return;
		try {
			// First populate the sems table with entries for all the STY TUIs.
			if (umls == null) {
				System.err.println("Must initialize Umls before SemanticEntity.");
			}
			Statement stmt = umls.conn.createStatement();
			ResultSet rs = stmt.executeQuery(getTuisQuery);
			while (rs.next()) {
				SemanticEntity newOne = new SemanticEntity(rs);
				sems.put(newOne.tui, newOne);
				semsByName.put(newOne.name, newOne);
			}
			top = new SemanticEntity("TOP", "Entity or Event", "", "top");
			U.log("Semantic Net has "+sems.size()+"("+semsByName.size()+") elements.");
			// Record the isa relationships among these
			rs = stmt.executeQuery(getIsaQuery);
			while (rs.next()) {
				SemanticEntity lower = semsByName.get(rs.getString("STY_RL1"));
				SemanticEntity higher = semsByName.get(rs.getString("STY_RL2"));
				if (lower != null) {	// ignore non-STY relations
					if (higher == null) higher = top;
					if (lower.up == null) lower.up = higher;
					else System.err.println(lower + 
							" has multiple superiors in semantic network: " +
							lower.up + " and " + higher);
					higher.down.add(lower);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error getting semantic network: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void showSemanticNetwork() {
		top.showSub(0);
	}
	
	private void showSub(int indent) {
		for (int i = 0; i < indent; i++) System.out.print(" ");
		U.log(this.toString());
		for (SemanticEntity sub: down) sub.showSub(indent+3);
	}
	
	public String toString() {
		return "<" + tui + ":" + name + ">";
	}
	
	public String getTuiString() {
		return tui;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SemanticEntity) {
			SemanticEntity o = (SemanticEntity)other;
			if (tui==o.tui && name==o.name && def==o.def) return true; 
		}
		return false;
	}

	@Override
	public SemanticEntity getChildAt(int childIndex) {
		return down.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return down.size();
	}

	@Override
	public SemanticEntity getParent() {
		return up;
	}

	@Override
	public int getIndex(TreeNode node) {
		return down.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public boolean isLeaf() {
		return down.size() == 0;
	}
	
	public void selectChildren(JTree tree, TreePath path) {
		Enumeration<SemanticEntity> kids = children();
		while (kids.hasMoreElements()) {
			SemanticEntity kid = kids.nextElement();
			tree.addSelectionPath(path.pathByAddingChild(kid));
		}
	}

	@Override
	public Enumeration<SemanticEntity> children() {
		return new SemanticEntityEnumeration(this);
	}
	
	public class SemanticEntityEnumeration implements Enumeration<SemanticEntity> {
		SemanticEntity entity = null;
		int lastEnumerated = -1;
		
		public SemanticEntityEnumeration(SemanticEntity me) {
			entity = me;
			lastEnumerated = -1;
		}
		
		@Override
		public boolean hasMoreElements() {
			return (lastEnumerated + 1) < entity.down.size();
		}
		@Override
		public SemanticEntity nextElement() {
			return entity.down.get(++lastEnumerated);
		}
		
	}

}
