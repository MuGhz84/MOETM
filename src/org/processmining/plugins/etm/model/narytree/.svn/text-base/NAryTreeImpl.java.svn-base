package org.processmining.plugins.etm.model.narytree;

import gnu.trove.list.TByteList;
import gnu.trove.list.TIntList;
import gnu.trove.list.TShortList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TShortArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.List;

/**
 * This class implements n-ary process trees. Trees are represented by 3 arrays.
 * The root node has index 0 The children of node m are located at indices m+1
 * upto but excluding next[m]. The type of node m is stored in type[m], where a
 * negative constant is a type for an operator node and a semi-positive value
 * indicates a leaf. The parent of node m is stored in parent[m]
 * 
 * @author bfvdonge
 * 
 */
public class NAryTreeImpl implements NAryTree {

	/**
	 * Configurations are stored in a byte-array, using 2 bits per node (rounded
	 * up to the nearest whole byte)
	 */
	protected byte[][] configurations = new byte[1][];

	/**
	 * The current number of configurations
	 */
	protected int numConfigurations = 0;

	/**
	 * stores the index of the next node in the subtree under m
	 */
	protected final int[] next;

	/**
	 * stores the type of a node m. If the value is negative, it is one of the
	 * given constants AND, OR, XOR, LOOP. If the value if semi-positive a leaf
	 * is implied
	 */
	protected final short[] type;

	/**
	 * stores the type of a node m for each configuration. If the value is
	 * negative, it is one of the given constants AND, OR, XOR, LOOP. If the
	 * value if semi-positive a leaf is implied
	 */
	protected short[][] types = new short[1][];

	/**
	 * stored the index of the parent of a node m. parent[0]=NONE and for all
	 * other elements m, parent[m]>=0
	 */
	protected final int[] parent;

	/**
	 * stores the index of the next leaf
	 */
	protected final int[] leafs;

	private int hashCode = -1;

	public NAryTreeImpl(NAryTree original) {
		int size = original.size();
		this.type = new short[size];
		this.parent = new int[size];
		this.next = new int[size];
		this.leafs = new int[size];
		//this.types is created and filled during the addConfiguration cycle below
		for (int i = size; i-- > 0;) {
			this.type[i] = original.getTypeFast(i);
			this.parent[i] = original.getParentFast(i);
			this.next[i] = original.getNextFast(i);
			this.leafs[i] = original.getNextLeafFast(i);
		}
		//And deep copy the configurations
		for (int c = 0; c < original.getNumberOfConfigurations(); c++) {
			//This works because 'Configuration' objects are newly created and interpreted back to our internal notation...
			addConfiguration(original.getConfiguration(c));
		}
	}

	public NAryTreeImpl(int[] next, short[] type, int[] parent) {
		this.type = type;
		this.parent = parent;
		this.next = next;
		this.leafs = new int[parent.length];
		this.configurations = new byte[0][];
		setupLeafs();

		assert isConsistent();
	}

	protected NAryTreeImpl(int size, int numConfigurations) {
		this.type = new short[size];
		this.parent = new int[size];
		this.next = new int[size];
		this.leafs = new int[size];
		this.configurations = new byte[0][];

		this.configurations = new byte[numConfigurations][size];
		this.numConfigurations = numConfigurations;
		this.types = new short[numConfigurations][size];

		setupLeafs();
	}

	public NAryTreeImpl(TIntList next, TShortList type, TIntList parent) {
		this.type = type.toArray();
		this.parent = parent.toArray();
		this.next = next.toArray();
		this.leafs = new int[parent.size()];
		setupLeafs();

		this.configurations = new byte[0][];

		assert isConsistent();
	}

	public NAryTreeImpl(List<Integer> next, List<Short> type, List<Integer> parent) {
		this.type = new short[type.size()];
		for (int i = 0; i < this.type.length; i++) {
			this.type[i] = type.get(i).shortValue();
		}
		this.parent = new int[parent.size()];
		for (int i = 0; i < this.parent.length; i++) {
			this.parent[i] = parent.get(i).intValue();
		}
		this.next = new int[next.size()];
		for (int i = 0; i < this.next.length; i++) {
			this.next[i] = next.get(i).intValue();
		}
		this.leafs = new int[next.size()];
		setupLeafs();

		this.configurations = new byte[0][];

		assert isConsistent();
	}

	private void setupLeafs() {
		int last = leafs.length;
		for (int i = leafs.length; i-- > 0;) {
			leafs[i] = last;
			if (isLeaf(i)) {
				last = i;
			}
		}
	}

	public boolean isConsistent() {
		boolean consistent = (this.type.length == this.parent.length) && (this.parent.length == this.next.length)
				&& (this.configurations.length == 0 || this.configurations.length == this.types.length);

		/*- invariants
		 * 1) (type[m] >= 0) == (next[m] == m+1); A leaf has no children
		 * 2) (parent[n] == m) => (m + 1 <= n < next[m]); Each node is in the subtree of its parent
		 */

		consistent &= getParent(0) == NONE; // root has no parent
		consistent &= getNext(0) == size(); // next node out of this subtree is the totals size of the tree (e.g. not existent)
		consistent &= getType(0) >= 0 ? (getNext(0) == 1) : true; //if root is leaf then next node out of subtree is node 1, if root is operator then this does not hold

		//Now while consistent, for each node
		for (int i = 1; consistent && i < size(); i++) {
			consistent &= (getType(i) >= 0) == (getNext(i) == i + 1); //if leaf then next node out of subtree is the next node...
			int m = getParent(i);
			consistent &= m >= 0; //parent is root or later
			consistent &= (m + 1 <= i) && (i < getNext(i)); //parent has index lower than child and next node out of this subtree is greater than current node
			//Added by Joos: if the node at i+1 refers to node i as the parent then the Next of i should be => next of i+1
			if (i < size() - 1) {
				if (getParent(i + 1) == i) {
					consistent &= (getNext(i) >= getNext(i + 1));
				}
			}
			//Added by Joos: if operator is LOOP then exactly 3 children
			if (getType(i) == NAryTree.LOOP) {
				if (nChildren(i) != 3) {
					consistent = false;
				}
			}
		}

		return consistent;

	}

	/**
	 * Sets the values of the internal arrays according to the given parameters.
	 * 
	 * @param index
	 * @param values
	 */
	protected void set(int index, short typeVal, int parentVal, int nextVal) {
		type[index] = typeVal;
		parent[index] = parentVal;
		next[index] = nextVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#setType(int,
	 * int)
	 */
	public void setType(int index, short t) {
		type[index] = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#setTye(int,
	 * int, int)
	 */
	public void setType(int par, int n, short t) {
		assert !isLeaf(par);
		setType(getChildAtIndex(par, n), t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#swap(int, int)
	 */
	public NAryTreeImpl swap(int node1, int node2) {
		if (node1 > node2) {
			int t = node1;
			node1 = node2;
			node2 = t;
		} else if (node1 == node2) {
			return new NAryTreeImpl(this);
		}
		assert node1 < node2;
		assert !isInSubtree(node2, node1);

		NAryTreeImpl tree = new NAryTreeImpl(size(), numConfigurations);

		// swapping subtrees is fairly easy, simply loop until node1
		int offset = size(node1) - size(node2);
		int j = 0;
		for (int i = 0; i < node1; i++, j++) {
			tree.set(j, type[i], parent[i], next[i] - (next[j] > node1 && next[j] <= node2 ? offset : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}
		// then copy in node 2, while changing the locations of the children and parents
		offset = node2 - node1;
		tree.set(j, type[node2], parent[node1], next[node2] - offset);
		for (int c = 0; c < numConfigurations; c++) {
			tree.setNodeConfiguration(c, j, getNodeConfiguration(c, node2));
		}

		j++;
		for (int i = node2 + 1; i < next[node2]; i++, j++) {
			tree.set(j, type[i], parent[i] - (i > node2 ? offset : 0), next[i] - offset);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}

		// then copy until node 2 is reached.
		offset = size(node1) - size(node2);
		for (int i = next[node1]; i < node2; i++, j++) {
			tree.set(j, type[i], parent[i] - (parent[i] >= node1 ? offset : 0), next[i]
					- (next[i] < next[node2] ? offset : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}

		// then copy node 1;

		// then copy node 1;
		tree.set(j, type[node1], parent[node2] - (parent[node2] > node1 ? offset : 0), next[node1] + j - node1);
		for (int c = 0; c < numConfigurations; c++) {
			tree.setNodeConfiguration(c, j, getNodeConfiguration(c, node1));
		}

		offset = j - node1;
		j++;
		for (int i = node1 + 1; i < next[node1]; i++, j++) {
			tree.set(j, type[i], parent[i] + offset, next[i] + offset);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}

		// finally the tail of the tree unchanged
		for (int i = next[node2]; i < size(); i++, j++) {
			tree.set(j, type[i], parent[i], next[i]);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}
		tree.setupLeafs();
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.NAryTree#add(org.processmining
	 * .plugins.etm.model.narytree.NAryTreeImpl, int, int, int)
	 */
	public NAryTreeImpl add(NAryTree source, int node, int par, int location) {

		// insert the entire subtree under node from source.
		// in total, we add source.size(node) nodes
		int s = source.size(node);
		NAryTreeImpl tree = new NAryTreeImpl(size() + s, numConfigurations);

		// copy the nodes until the parent is reached while increasing the 
		// value of next if the current value is greater than par.
		int i;
		for (i = 0; i <= par; i++) {
			tree.set(i, type[i], parent[i], next[i] + (next[i] > par ? s : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}

		//copy the first few children of parent, while not
		// increasing the value of next.
		int nc = 0;
		for (i = par + 1; nc < location && i < next[par]; i++) {
			tree.set(i, type[i], parent[i], next[i]);
			nc = nc + (i + 1 == size() || parent[i + 1] == par ? 1 : 0);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}
		int startFrom = i;

		// index i is the index at which the source subtree needs to be
		// inserted
		int offset = i - node;
		tree.set(i, source.getTypeFast(node), par, source.getNextFast(node) + offset);
		for (int c = 0; c < numConfigurations; c++) {
			tree.setNodeConfiguration(c, i, source.getNodeConfiguration(c, node));
		}

		i++;
		for (int j = node + 1; j < source.getNextFast(node); j++, i++) {
			tree.set(i, source.getTypeFast(j), source.getParentFast(j) + offset, source.getNextFast(j) + offset);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, source.getNodeConfiguration(c, j));
			}
		}

		// add the remaining part of the tree,
		for (int j = startFrom; j < size(); j++, i++) {
			tree.set(i, type[j], parent[j] + (parent[j] > par ? s : 0), next[j] + s);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, j));
			}
		}

		tree.setupLeafs();
		return tree;
	}

	public NAryTree addParent(int node, short newType, byte configurationType) {
		assert newType != LOOP && newType < 0;
		// insert a node of the given type as a parent of node
		// in total, we add 1 nodes

		NAryTreeImpl tree = new NAryTreeImpl(size() + 1, numConfigurations);

		// copy the nodes until the node is reached while increasing the 
		// value of next if the current value is greater than node.
		int i;
		for (i = 0; i < node; i++) {
			tree.set(i, type[i], parent[i], next[i] + (next[i] > node ? 1 : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}

		}

		// add the new node. This node will have one leaf.
		//tree.set(i, newType, parent[i], i + 2);
		//FIX by Joos: no, it will have one CHILD which can be a subtree itself...
		tree.set(i, newType, parent[i], next[i] + 1);
		for (int c = 0; c < numConfigurations; c++) {
			tree.setNodeConfiguration(c, i, configurationType);
		}

		// copy the rest
		for (i = node; i < size(); i++) {
			tree.set(i + 1, type[i], (parent[i] < node ? parent[i] : parent[i] + 1), next[i] + 1);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i + 1, getNodeConfiguration(c, i));
			}

		}
		tree.parent[node + 1] = node;

		tree.setupLeafs();
		return tree;
	}

	public NAryTree addChild(int operatorNode, int location, short leafType, byte configurationType) {
		//Updated assertion, adding a child to a loop that has less than 3 is allowed (to make it consistent again)
		assert getType(operatorNode) < 0
				&& (getType(operatorNode) != LOOP || (getType(operatorNode) == LOOP && nChildren(operatorNode) < 3));
		assert leafType >= 0;

		// insert the leaf under node
		// in total, we add 1 nodes
		NAryTreeImpl tree = new NAryTreeImpl(size() + 1, numConfigurations);

		// copy the nodes until the parent is reached while increasing the 
		// value of next if the current value is greater than par.
		int i;
		for (i = 0; i <= operatorNode; i++) {
			tree.set(i, type[i], parent[i], next[i] + (next[i] > operatorNode ? 1 : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}

		//copy the first few children of parent, while not
		// increasing the value of next.
		int nc = 0;
		for (i = operatorNode + 1; nc < location && i < next[operatorNode]; i++) {
			tree.set(i, type[i], parent[i], next[i]);
			nc = nc + (i + 1 == size() || parent[i + 1] == operatorNode ? 1 : 0);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}

		// index i is the index at which the new leaf needs to be inserted
		tree.set(i, leafType, operatorNode, i + 1);
		for (int c = 0; c < numConfigurations; c++) {
			tree.setNodeConfiguration(c, i, configurationType);
		}

		i++;

		// add the remaining part of the tree,
		for (int j = i - 1; j < size(); j++, i++) {
			tree.set(i, type[j], parent[j] + (parent[j] > operatorNode ? 1 : 0), next[j] + 1);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, j));
			}
		}

		tree.setupLeafs();
		return tree;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#replace(int,
	 * int, org.processmining.plugins.etm.model.narytree.NAryTreeImpl, int)
	 */
	public NAryTree replace(int par, int n, NAryTree source, int node) {
		if (isLeaf(par)) {
			return replace(par, source, node);
		} else {
			// compute the id of the node to replace
			return replace(getChildAtIndex(par, n), source, node);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#replace(int,
	 * org.processmining.plugins.etm.model.narytree.NAryTreeImpl, int)
	 */
	public NAryTreeImpl replace(int node, NAryTree source, int srcNode) {

		// compute the increment in size
		int incS = source.size(srcNode) - size(node);

		NAryTreeImpl tree = new NAryTreeImpl(size() + incS, numConfigurations);

		// we replace the node, hence we copy the first part
		// and the value of next is updated where applicable
		int i;
		for (i = 0; i < node; i++) {
			tree.set(i, type[i], parent[i], next[i] + (next[i] > node ? incS : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}

		// now copy the node from the source, all indices in parent and next are 
		// decreased to math the new location, except the first parent, which becomes
		// the old parent of node
		int offset = srcNode - i;
		int j = i;
		for (i = srcNode; i < source.getNextFast(srcNode); i++, j++) {
			tree.set(j, source.getTypeFast(i), //
					(i > srcNode ? source.getParentFast(i) - offset : parent[node]), //
					source.getNextFast(i) - offset);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, source.getNodeConfiguration(c, i));
			}

		}

		// now copy the remaining nodes from the original
		for (i = next[node]; i < size(); i++, j++) {
			tree.set(j, type[i],//
					(parent[i] < node ? parent[i] : parent[i] + incS),//
					next[i] + incS);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}

		tree.setupLeafs();
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#remove(int,
	 * int)
	 */
	public NAryTree remove(int par, int index) {
		if (isLeaf(par)) {
			return new NAryTreeImpl(this);
		}
		return remove(getChildAtIndex(par, index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#remove(int)
	 */
	public NAryTree remove(int node) {
		node = getHighestParentWithOneChild(node);
		if (node == 0 || type[parent[node]] == LOOP) {
			return new NAryTreeImpl(this);
		}

		int s = size(node);

		NAryTreeImpl tree = new NAryTreeImpl(size() - s, numConfigurations);

		// copy the first part upto node. If next[i] > node then reduce next[i] by size; 
		for (int i = 0; i < node; i++) {
			tree.set(i, type[i], parent[i], next[i] - (next[i] > node ? s : 0));

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, i, getNodeConfiguration(c, i));
			}
		}
		int j = node;
		// copy the remainder and reduce parent[i] if necessary
		for (int i = next[node]; i < size(); i++, j++) {
			tree.set(j, type[i], parent[i] - (parent[i] > node ? s : 0), next[i] - s);

			for (int c = 0; c < numConfigurations; c++) {
				tree.setNodeConfiguration(c, j, getNodeConfiguration(c, i));
			}
		}

		tree.setupLeafs();
		return tree;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#move(int, int,
	 * int)
	 */
	public NAryTree move(int node, int newParent, int location) {
		assert (node > 0 && getType(parent[node]) != LOOP && getType(newParent) != LOOP);

		// first add the subtree into the tree
		final NAryTree tree = add(this, node, newParent, location);

		// then remove node from the new tree.
		if (node < newParent) {
			// the node to be removed resides in the new tree at the same index as in the original tree
			return tree.remove(node);
		} else {
			// the node to be removed has shifted right by the difference in size between the two trees
			return tree.remove(node + (tree.size() - size()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#move(int, int,
	 * int, int)
	 */
	public NAryTree move(int par, int n, int newParent, int location) {
		assert getType(par) != LOOP;
		return move(getChildAtIndex(par, n), newParent, location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#getType(int)
	 */
	public short getType(int node) {
		return node >= 0 && node < size() ? type[node] : NONE;
	}

	public short getTypeFast(int node) {
		return type[node];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.NAryTree#getChildAtIndex
	 * (int, int)
	 */
	public int getChildAtIndex(int par, int n) {
		int nc = 0;
		int found = par + 1;
		int j = par + 1;
		while (nc <= n && j < next[par]) {
			if (parent[j] == par) {
				nc++;
				found = j;
			}
			j = next[j];
		}
		// found is the n-th child;
		return found;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#size(int)
	 */
	public int size(int node) {
		return next[node] - node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#nChildren(int)
	 */
	public int nChildren(int node) {
		int nc = 0;
		int j = node + 1;
		while (j < next[node]) {
			nc += (parent[j] == node ? 1 : 0);
			j = next[j];
		}
		return nc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.plugins.etm.model.narytree.NAryTree#isInSubtree(int,
	 * int)
	 */
	public boolean isInSubtree(int par, int child) {
		// check if child occurs in the subtree of par
		if (par == 0) {
			return true;
		}
		//return child >= parent && child < next[parent];
		return child >= par && child < next[par];
	}

	/**
	 * returns true if all four internal arrays are identical.
	 */
	public boolean equals(Object o) {
		if (o instanceof NAryTreeImpl) {
			NAryTreeImpl tree = (NAryTreeImpl) o;
			return Arrays.equals(type, tree.type) && Arrays.equals(parent, tree.parent)
					&& Arrays.equals(next, tree.next) && Arrays.deepEquals(configurations, tree.configurations);
		} else {
			return false;
		}
	}

	/**
	 * returns hashcode based on the internal arrays
	 */
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = Arrays.hashCode(type) + 37 * Arrays.hashCode(parent) + 37 * 37 * Arrays.hashCode(next) + 37 * 37
					* 37 * Arrays.deepHashCode(configurations);
		}

		return hashCode;
	}

	/**
	 * returns the highest parent of the given node, such that this parent has
	 * one child and all intermediate parents also have one child.
	 * 
	 * @param node
	 * @return
	 */
	protected int getHighestParentWithOneChild(int node) {
		int par = node;
		//FIXME Joos@Boudewijn please double check if node -> next[node] was a good fix (meaning of next is here still interpreted as last)
		while (par > 0 && nChildren(parent[par]) <= 1) {
			//while (next[par] == node && parent[par] == par - 1) {
			// parent is a parent with one element in its subtree
			par = parent[par];
		}
		return par;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#isLeaf(int)
	 */
	public boolean isLeaf(int node) {
		return type[node] >= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#size()
	 */
	public int size() {
		return parent.length;
	}

	public String toInternalString() {
		return "T:" + toString(type) + "\n" + //
				"N:" + toString(next) + "\n" + //
				"P:" + toString(parent) + "\n" + //
				"C:" + toString(configurations);
	}

	private String toString(byte[][] configurations2) {
		//Now add the configurations
		StringBuilder b = new StringBuilder();
		b.append("[ ");
		for (int c = 0; c < configurations2.length; c++) {
			b.append(new Configuration(configurations2[c]).toString());
		}
		return b.append(" ]").toString();
	}

	public String toString() {
		return TreeUtils.toString(this);
	}

	/* STATIC METHODS */

	private static String toString(int[] a) {
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			if (a[i] >= 0 && a[i] < 10) {
				b.append("  ");
				b.append(a[i]);
			} else if (a[i] >= 10 && a[i] < 100) {
				b.append(" ");
				b.append(a[i]);
			} else if (a[i] >= 100) {
				b.append(a[i]);
			} else {
				if (a[i] == XOR) {
					b.append("XOR");
				} else if (a[i] == OR) {
					b.append("OR ");
				} else if (a[i] == ILV) {
					b.append("ILV ");
				} else if (a[i] == SEQ) {
					b.append("SEQ");
				} else if (a[i] == AND) {
					b.append("AND");
				} else if (a[i] == LOOP) {
					b.append(" LP");
				} else if (a[i] == NONE) {
					b.append("   ");
				} else {
					b.append(a[i]);
				}
			}
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	private static String toString(short[] a) {
		if (a == null)
			return "null";
		int iMax = a.length - 1;
		if (iMax == -1)
			return "[]";

		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0;; i++) {
			if (a[i] >= 0 && a[i] < 10) {
				b.append("  ");
				b.append(a[i]);
			} else if (a[i] >= 10 && a[i] < 100) {
				b.append(" ");
				b.append(a[i]);
			} else if (a[i] >= 100 && a[i]!= TAU) {
				b.append(a[i]);
			} else {
				if (a[i] == TAU) {
					b.append("TAU");
				} else if (a[i] == XOR) {
					b.append("XOR ");
				} else if (a[i] == OR) {
					b.append("OR ");
				} else if (a[i] == ILV) {
					b.append("ILV ");
				} else if (a[i] == REVSEQ) {
					b.append("RSQ");
				} else if (a[i] == SEQ) {
					b.append("SEQ");
				} else if (a[i] == AND) {
					b.append("AND");
				} else if (a[i] == LOOP) {
					b.append(" LP");
				} else if (a[i] == NONE) {
					b.append("   ");
				} else if (a[i] == TAU) {
					b.append("TAU");
				} else {
					b.append(a[i]);
				}
			}
			if (i == iMax)
				return b.append(']').toString();
			b.append(", ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.plugins.etm.model.narytree.NAryTree#getParent(int)
	 */
	public int getParent(int node) {
		return node > 0 && node < size() ? parent[node] : NONE;
	}

	public int getParentFast(int node) {
		return parent[node];
	}

	/**
	 * returns the first node not in the subtree of the given node .
	 * 
	 * @param node
	 * @return
	 */
	public int getNext(int node) {
		return node > 0 && node < size() ? next[node] : size();
	}

	public int getNextFast(int node) {
		return next[node];
	}

	public int numLeafs() {
		int l = 0;
		int i = 0;
		do {
			l++;
			i = getNextLeafFast(i);
		} while (i < size());
		//		for (int i = 0; i < type.length; i++) {
		//			l += (type[i] >= 0 ? 1 : 0);
		//		}
		return l;
	}

	public int compareTo(NAryTree o) {
		if (size() != o.size()) {
			return size() - o.size();
		} else {
			for (int i = 0; i < size(); i++) {
				if (type[i] != o.getTypeFast(i)) {
					return type[i] - o.getTypeFast(i);
				} else if (next[i] != o.getNextFast(i)) {
					return next[i] - o.getNextFast(i);
				} else if (parent[i] != o.getParentFast(i)) {
					return parent[i] - o.getParentFast(i);
				}
			}
			return 0;
		}
	}

	public int getNextLeaf(int node) {
		return node > 0 && node < size() ? leafs[node] : size();
	}

	public int getNextLeafFast(int node) {
		return leafs[node];
	}

	public void addConfiguration(Configuration configuration) {
		assert configuration.conf.length == size();

		//See if there is space to store another configuration, if not, double the space.
		if (configurations.length == numConfigurations) {
			int newSize = configurations.length == 0 ? 1 : 2 * configurations.length;
			byte[][] newConf = new byte[newSize][];
			short[][] newTypes = new short[newSize][];
			for (int i = configurations.length; i-- > 0;) {
				newConf[i] = configurations[i];
				newTypes[i] = types[i];
			}
			configurations = newConf;
			types = newTypes;
		}
		// now copy the blockeable and hideable configuration into the stored configurations
		//TODO check if provided downgrades are valid
		configurations[numConfigurations] = new byte[configuration.conf.length];
		types[numConfigurations] = new short[configuration.conf.length];
		for (int i = configuration.conf.length; i-- > 0;) {
			configurations[numConfigurations][i] = configuration.conf[i];

			switch (configurations[numConfigurations][i]) {
				case Configuration.AND :
					types[numConfigurations][i] = NAryTree.AND;
					break;
				case Configuration.XOR :
					types[numConfigurations][i] = NAryTree.XOR;
					break;
				case Configuration.SEQ :
					types[numConfigurations][i] = NAryTree.SEQ;
					break;
				case Configuration.REVSEQ :
					types[numConfigurations][i] = NAryTree.REVSEQ;
					break;
				default :
					types[numConfigurations][i] = getTypeFast(i);
			}

		}
		numConfigurations++;
	}

	public void removeConfiguration(int configurationNumber) {
		assert configurationNumber < numConfigurations;

		for (int i = configurationNumber; i < --numConfigurations;) {
			configurations[i] = configurations[++i];
		}
	}

	public int getNumberOfConfigurations() {
		return numConfigurations;
	}

	public Configuration getConfiguration(int configurationNumber) {
		return new Configuration(configurations[configurationNumber]);
	}

	public boolean isBlocked(int configurationNumber, int node) {
		if (configurationNumber < 0 || configurationNumber > numConfigurations - 1) {
			return false;
		}
		return configurations[configurationNumber][node] == Configuration.BLOCKED;
	}

	public boolean isHidden(int configurationNumber, int node) {
		if (configurationNumber < 0 || configurationNumber > numConfigurations - 1) {
			return false;
		}
		return configurations[configurationNumber][node] == Configuration.HIDDEN;
	}

	public boolean isDowngraded(int configurationNumber, int node) {
		if (configurationNumber < 0 || configurationNumber > numConfigurations - 1) {
			return false;
		}
		return configurations[configurationNumber][node] == SEQ || configurations[configurationNumber][node] == XOR
				|| configurations[configurationNumber][node] == AND
				|| configurations[configurationNumber][node] == REVSEQ;
	}

	public void setNodeConfiguration(int configurationNumber, int node, byte configurationOption) {
		configurations[configurationNumber][node] = configurationOption;
	}

	public byte getNodeConfigurationFast(int configurationNumber, int node) {
		return configurations[configurationNumber][node];
	}

	public byte getNodeConfiguration(int configurationNumber, int node) {
		if (configurations.length <= configurationNumber)
			return Configuration.NOTCONFIGURED;
		else if (configurations[configurationNumber] == null || configurations[configurationNumber].length < node)
			return Configuration.NOTCONFIGURED;
		else
			return configurations[configurationNumber][node];
	}

	public short getType(int configurationNumber, int node) {
		if (node < 0 || node >= size()) {
			return NONE;
		} else {
			return getTypeFast(configurationNumber, node);
		}
	}

	public short getTypeFast(int configurationNumber, int node) {
		if (configurationNumber == -1) {
			return getTypeFast(node);
		} else {
			return types[configurationNumber][node];
		}

		//		switch (configurations[configurationNumber][node]) {
		//			case Configuration.AND :
		//				return NAryTree.AND;
		//			case Configuration.XOR :
		//				return NAryTree.XOR;
		//			case Configuration.SEQ :
		//				return NAryTree.SEQ;
		//			case Configuration.REVSEQ :
		//				return NAryTree.REVSEQ;
		//			default :
		//				return getTypeFast(node);
		//		}
	}

	public NAryTree applyConfiguration(int configurationNumber) {

		NAryTree configuredTree = this.applyHidingAndOperatorDowngrading(configurationNumber);

		//Now recursively apply blocking
		int next;
		configurationNumber = 0; //the returned configuredTree has only 1 configuration left!
		for (int node = 0; node < configuredTree.size(); node = next) {
			if (configuredTree.isBlocked(configurationNumber, node)) {
				//Find out what the effect is of this block
				int blockedNode = configuredTree.findBlockEffect(node);
				//Now apply the blocking:
				//If the blocked node is the REDO part of an LOOP then the LOOP is replaced by SEQ(DO, EXIT)
				int p = configuredTree.getParent(blockedNode);
				if (configuredTree.getType(p) == NAryTree.LOOP && configuredTree.getChildAtIndex(p, 1) == blockedNode) {
					NAryTree seqDoExit = new NAryTreeImpl(new int[] { 1 }, new short[] { SEQ }, new int[] { NONE });
					//Add the DO part as first child of SEQ
					seqDoExit = seqDoExit.add(configuredTree, configuredTree.getChildAtIndex(p, 0), 0, 0);
					//Add the EXIT part as second child of SEQ
					seqDoExit = seqDoExit.add(configuredTree, configuredTree.getChildAtIndex(p, 2), 0, 1);
					//And replace
					configuredTree = configuredTree.replace(p, seqDoExit, 0);
					//We can not determine the next node, so move up to the parent and do little extra work
					next = p;
				} else {
					//Otherwise the blocked node should be removed
					if (blockedNode == 0) {
						return new NAryTreeImpl(new int[] { 1 }, new short[] { TAU }, new int[] { NONE });//TreeUtils.fromString("LEAF: tau");
					}
					configuredTree = configuredTree.remove(blockedNode);
					//We can not determine the next node, so move up to the parent and do little extra work
					next = p;
				}
			} else {
				next = node + 1;
			}
		}

		//It could be that the whole tree became blocked. We want this to result in a TAU root.
		//		if (configuredTree.size() == 0) {

		//		}

		configuredTree.removeAllConfigurations();

		assert (configuredTree.isConsistent());

		return configuredTree;
	}

	/**
	 * Finds the effect of blocking a particular node. If a child of a SEQ or
	 * AND operator is blocked then the operator itself is blocked. The same for
	 * DO and EXIT children of a LOOP operator. Also, if the only (remaining)
	 * child of an operator is blocked, the operator itself is also blocked.
	 * 
	 * @param blockedNode
	 *            the node that is know to be blocked
	 * @return the ID of the node that is also blocked as an effect of the block
	 *         of the provided node. If there is no cascading effect than the
	 *         blockedNode is returned.
	 */
	public int findBlockEffect(int blockedNode) {
		if (blockedNode == 0) {
			//Can not move up!
			return 0;
		}

		int p = getParent(blockedNode);
		short pType = getTypeFast(p);
		//blocking children of SEQ and AND results in that node to be blocked too

		if (pType == NAryTree.SEQ || pType == NAryTree.AND) {
			return findBlockEffect(p);
			//Blocking DO or EXIT of a LOOP results in whole LOOP to be blocked
		} else if (pType == NAryTree.LOOP
				&& (getChildAtIndex(p, 0) == blockedNode || getChildAtIndex(p, 2) == blockedNode)) {
			return p;
			//Also, if the blocked node is the only remaining child then the parent itself is also blocked
		} else if (nChildren(p) == 1) {
			return findBlockEffect(p);
		} else {//no cascading effect...
			return blockedNode;
		}
	}

	/**
	 * Apply all hiding and downgrading configurations of the provided
	 * configuration number. ERGO: blocking is not applied! applyConfiguration
	 * is the preferred method to apply configurations!!! This method only
	 * applies particular configuration options!!!
	 * 
	 * @param configurationNumber
	 * @return
	 */
	public NAryTree applyHidingAndOperatorDowngrading(int configurationNumber) {
		TIntList cNext = new TIntArrayList();
		TShortList cType = new TShortArrayList();
		TIntList cParent = new TIntArrayList();
		TByteList cConfig = new TByteArrayList();

		TIntIntMap old2new = new TIntIntHashMap();

		int nNode = 0;
		int next;
		for (int node = 0; node < type.length; node = next) {

			if (isHidden(configurationNumber, node)) {
				if ((getType(parent[node]) == NAryTree.AND || getType(parent[node]) == NAryTree.SEQ || getType(parent[node]) == NAryTree.REVSEQ)
						&& getNext(parent[node]) > getNext(node)) {
					// remove any hidden subtree of the node
					next = getNextFast(node);
					continue;
				} else {
					// replace the hidden node by a TAU
					cType.add(NAryTree.TAU);
					cConfig.add(Configuration.NOTCONFIGURED);
					next = getNextFast(node);
				}
			} else if (isDowngraded(configurationNumber, node)) {
				//apply downgrade
				//TODO check if downgrade was allowed, if not ignore ?
				cType.add(getNodeConfiguration(configurationNumber, node));
				cConfig.add(Configuration.NOTCONFIGURED);
				next = node + 1;
			} else {
				// add the node and it's subtree
				cType.add(type[node]);
				//And keep blocking configurations
				if (isBlocked(configurationNumber, node)) {
					cConfig.add(Configuration.BLOCKED);
				} else {
					cConfig.add(Configuration.NOTCONFIGURED);
				}
				next = node + 1;
			}
			if (node > 0) {
				cParent.add(old2new.get(getParentFast(node)));
			} else {
				cParent.add(NAryTree.NONE);
			}

			old2new.put(node, nNode);
			nNode++;
		}

		// build the "next" array.
		cNext.add(cParent.size());
		next: for (int n1 = 1; n1 < cParent.size(); n1++) {
			int p = cParent.get(n1);
			for (int n2 = n1 + 1; n2 < cParent.size(); n2++) {
				if (cParent.get(n2) == p) {
					cNext.add(n2);
					continue next;
				}
			}
			// did not find another node with the same parent.
			cNext.add(cNext.get(p));
		}

		//Special case (as found by the ETM):empty tree should be tau
		//		if (cNext.size() == 1 && cType.size() == 0 && cParent.size() == 0) {
		//			return TreeUtils.fromString("LEAF: tau");
		//		}

		NAryTree result = new NAryTreeImpl(cNext, cType, cParent);
		result.addConfiguration(new Configuration(cConfig.toArray()));

		assert (result.isConsistent());

		return result;
	}

	public int countNodes(short type) {
		int cnt = 0;
		for (int i = this.type.length; i-- > 0;) {
			cnt += this.type[i] == type ? 1 : 0;
		}
		return cnt;
	}

	public void removeAllConfigurations() {
		this.configurations = new byte[0][];
		this.numConfigurations = 0;
	}

	protected static double expectedCol(int keys, long vals) {
		double n = keys;
		double d = vals;
		return n - d + d * Math.pow(1 - 1 / d, n);
	}

	public static void main(String[] args) {

		int col = 1 << 20;

		System.out.println("Collision test on  " + col + " semi-random trees.");
		System.out.println("Expecting " + expectedCol(col, 1L << 32) + " collisions.");

		TIntIntMap codes1 = new TIntIntHashMap(2 * col);
		TIntIntMap codes2 = new TIntIntHashMap(2 * col);

		NAryTreeImpl tree = null;

		long t1, t2, t3;
		long time1 = 0, time2 = 0;
		;
		int cnt1 = 0, cnt2 = 0;
		for (int i = 0; i < col; i++) {
			if (i % 2 == 0 || tree.size() == 1) {
				tree = TreeUtils.randomTree(8, .4, 15, 80);
			} else {
				if (tree.getType(0) != OR) {
					tree.setType(0, NAryTree.OR);
				} else {
					tree.setType(0, NAryTree.AND);
				}
				tree.hashCode = -1;
			}
			t1 = System.nanoTime();
			if (codes1.adjustOrPutValue(tree.hashCode(), 1, 1) > 1) {
				cnt1++;
			}
			t2 = System.nanoTime();
			if (codes2.adjustOrPutValue(tree.hashCode(0), 1, 1) > 1) {
				cnt2++;
			}
			t3 = System.nanoTime();
			time1 += t2 - t1;
			time2 += t3 - t2;
		}
		System.out.println(cnt1 + " collisions and " + codes1.size() + " unique hashCodes in " + time1 / 1000.0
				/ 1000.0 / 1000.0 + " seconds");
		System.out.println(cnt2 + " collisions and " + codes2.size() + " unique hashCodes in " + time2 / 1000.0
				/ 1000.0 / 1000.0 + " seconds");

		System.out.println("Done.");

	}

	public int hashCode(int node) {
		if (type[node] >= 0) {
			return (type[node] + 1);
		} else {
			int ch = node + 1;

			//			public static final short TAU = Short.MAX_VALUE;
			//			public static final short XOR = -1;
			//			public static final short AND = -2;
			//			public static final short OR = -3;
			//			public static final short LOOP = -4;
			//			public static final short SEQ = -8;
			//			public static final short REVSEQ = -16;
			//			public static final short NONE = -64;
			assert type[node] != NONE;

			//			int h = (type[node] < -4 ? -AND : -type[node]);
			int h = -type[node];
			int c = 1;
			do {
				h += 31 * hashCode(ch);
				//				h = 31 * h + c * hashCode(ch);
				ch = next[ch];
				c++;
			} while (ch < type.length && parent[ch] == node);
			return h;
		}

	}

}
