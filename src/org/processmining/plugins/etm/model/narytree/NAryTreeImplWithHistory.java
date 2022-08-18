/**
 * 
 */
package org.processmining.plugins.etm.model.narytree;

import gnu.trove.list.TIntList;
import gnu.trove.list.TShortList;

import java.util.List;

//FIXME is this the best way? Borja originally had this hacked into main NAryTree class. Wouldn't it be better to outsource this to a map somewhere in the registry?

/**
 * @author jbuijs
 * @author Borja
 *
 * This class extends the regular NAryTreeImpl to keep track of the ancestory
 *
 */
public class NAryTreeImplWithHistory extends NAryTreeImpl {

	/**
	 * Stores the id to correctly retrieve its parent in the history log. -1
	 * means it is from the initial population. It has to be included in the
	 * constructors, otherwise when copying the elite solutions, the new tree
	 * loses the identifier, breaking the family bonds of its tree of life.
	 */
	protected int treeOfLifeID = -1;

	/**
	 * @param original
	 */
	public NAryTreeImplWithHistory(NAryTree original) {
		super(original);
		if (original instanceof NAryTreeImplWithHistory) {
			this.treeOfLifeID = ((NAryTreeImplWithHistory) original).getTreeOfLifeID();
		}
	}

	private int getTreeOfLifeID() {
		return treeOfLifeID;
	}

	/**
	 * @param next
	 * @param type
	 * @param parent
	 */
	public NAryTreeImplWithHistory(int[] next, short[] type, int[] parent) {
		super(next, type, parent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param size
	 * @param numConfigurations
	 */
	public NAryTreeImplWithHistory(int size, int numConfigurations) {
		super(size, numConfigurations);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param next
	 * @param type
	 * @param parent
	 */
	public NAryTreeImplWithHistory(TIntList next, TShortList type, TIntList parent) {
		super(next, type, parent);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param next
	 * @param type
	 * @param parent
	 */
	public NAryTreeImplWithHistory(List<Integer> next, List<Short> type, List<Integer> parent) {
		super(next, type, parent);
		// TODO Auto-generated constructor stub
	}

	public NAryTreeImplWithHistory swap(int node1, int node2) {
		NAryTreeImplWithHistory tree = this.swap(node1, node2);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory add(NAryTree source, int node, int par, int location) {
		NAryTreeImplWithHistory tree = this.add(source, node, par, location);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory addParent(int node, short newType, byte configurationType) {
		NAryTreeImplWithHistory tree = this.addParent(node, newType, configurationType);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory addChild(int operatorNode, int location, short leafType, byte configurationType) {
		NAryTreeImplWithHistory tree = this.addChild(operatorNode, location, leafType, configurationType);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory replace(int node, NAryTree source, int srcNode) {
		NAryTreeImplWithHistory tree = this.replace(node, source, srcNode);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory remove(int node) {
		NAryTreeImplWithHistory tree = this.remove(node);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTreeImplWithHistory move(int node, int newParent, int location) {
		NAryTreeImplWithHistory tree = this.move(node, newParent, location);
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	public NAryTree applyHidingAndOperatorDowngrading(int configurationNumber) {
		NAryTreeImplWithHistory tree = new NAryTreeImplWithHistory(
				this.applyHidingAndOperatorDowngrading(configurationNumber));
		tree.setTreeOfLifeID(this.treeOfLifeID);
		return tree;
	}

	private void setTreeOfLifeID(int id) {
		this.treeOfLifeID = id;
	}

}
