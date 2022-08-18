package org.processmining.plugins.etm.mutation.mutators;

import java.util.Random;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * Randomly instantiates a leaf node and adds it to a randomly chosen parent
 * which will add it to its children
 */
//FIXME check all class contents
//FIXME Test Class thoroughly
public class AddNodeRandom extends TreeMutationAbstract {

	@SuppressWarnings("unused")
	private String key = "AddNodeRandom";

	public AddNodeRandom(CentralRegistry registry) {
		super(registry);
	}

	public NAryTree mutate(NAryTree tree) {
		return mutate(tree, 0);
	}

	/**
	 * @see TreeMutationAbstract#mutate(Node)
	 */
	public NAryTree mutate(NAryTree tree, int node) {
		assert tree.isConsistent();

		Random rng = registry.getRandom();

		//Select a node within the subtree of the given node (including the provided node itself)
		//TODO test code below
		int subtreeSize = tree.getNext(node) - node;
		int selectedNode;

		//This is a nice idea, except for loops, which have a strict 3 child policy
		int nrTries = MAXTRIES;
		do {
			if (nrTries == 0) {
				//ABORT
				noChange();
				return tree;
			}
			//Try to select a none loop node for a limited time
			selectedNode = rng.nextInt(subtreeSize) + node;
			nrTries--;
		} while (tree.getType(selectedNode) == NAryTree.LOOP);

		NAryTree newSubtree;
		//First test the type of the randomly selected node
		if (tree.isLeaf(selectedNode) || rng.nextBoolean()) {
			//Give leafs always, and operator nodes sometimes, a new operator parent with only them as child

			//Get a random parent operator type
			short parentType = TreeUtils.getRandomOperatorType(rng, 2); //Don't restrict type, otherwise we will never introduce Loops in mutation
			//was 4
			if (parentType == NAryTree.LOOP) {
				//For loops, we first need to create the loop as a tree
				NAryTree loopTree = new NAryTreeImpl(new int[] { 4, 2, 3, 4 }, new short[] { NAryTree.LOOP,
						NAryTree.TAU, NAryTree.TAU, NAryTree.TAU }, new int[] { NAryTree.NONE, 0, 0, 0 });
				assert loopTree.isConsistent();
				//Now copy the selected node into the loop body part of our loop
				NAryTree newLoopTree = loopTree.replace(1, tree, selectedNode);
				//And then replace the selected node by the newly created loop
				newSubtree = tree.replace(selectedNode, newLoopTree, 0);
				assert newSubtree.isConsistent();
			} else { //inserts a new node of type as a parent to the provided node
				newSubtree = tree.addParent(selectedNode, parentType, Configuration.NOTCONFIGURED);
				assert newSubtree.isConsistent();
			}
			didChange(selectedNode, NAryTreeHistory.TypesOfChange.OTHER); // 
//			didChange(tree.getParent(selectedNode), NAryTreeHistory.TypesOfChange.OTHER); // 
		} else {
			//Otherwise give the operator node a new child
			short leafClass = registry.getEventClassID(registry.getRandomEventClass(rng));
			int childPos = tree.nChildren(node) > 0 ? rng.nextInt(tree.nChildren(node)) : 0;
			newSubtree = tree.addChild(selectedNode, childPos, leafClass, Configuration.NOTCONFIGURED);
//			didChange(newSubtree.getChildAtIndex(selectedNode, childPos), NAryTreeHistory.TypesOfChange.ADD); // 
			didChange(selectedNode, NAryTreeHistory.TypesOfChange.ADD); 
			assert newSubtree.isConsistent();
		}
		assert newSubtree.isConsistent();
		return newSubtree;
	}

	public boolean changedAtLastCall() {
		return changedAtLastCall;
	}

	public int locationOfLastChange() {
		return locationOfLastChange;
	}
}
