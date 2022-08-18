package org.processmining.plugins.etm.mutation.mutators;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * Randomly mutates/changes a single node
 * 
 * @author jbuijs
 */
//FIXME check all class contents
//FIXME Test Class thoroughly
public class MutateSingleNodeRandom extends TreeMutationAbstract {

	private String key = "MutateSingleNodeRandom";

	public MutateSingleNodeRandom(CentralRegistry registry) {
		super(registry);
	}

	/**
	 * @see TreeMutationAbstract#mutate(Node)
	 */
	public NAryTree mutate(NAryTree tree, int node) {
		assert tree.isConsistent();

		//Deep clone the tree first...
		NAryTree mutatedTree = new NAryTreeImpl(tree);

		//Select a node
		int nodeToBeMutated = registry.getRandom().nextInt(mutatedTree.getNext(node) - node) + node;

		//If the node is a leaf

		//Guess a new class
		short newType;
		do {
			//For leafs, get a new event class type
			if (mutatedTree.isLeaf(nodeToBeMutated)) {
				//We might also change the leaf into a tau
				int guess = registry.getRandom().nextInt(registry.getEventClasses().size() + 1);
				if (guess == registry.getEventClasses().size())
					newType = NAryTree.TAU;
				else
					newType = (short) guess;
			} else {
				/*
				 * For operator nodes get a new operator type. Now, this is a
				 * good idea, except for loops. Here we choose never to change a
				 * node into a loop and trust on the 'add node random' mutation
				 * to add loop nodes to this parent if it needs looping.
				 */
				newType = TreeUtils.getRandomOperatorType(registry.getRandom(), 2); //was 3
			}

		} while (newType == mutatedTree.getType(nodeToBeMutated));
		//While we did not guess a new class

		//Set a new class
		mutatedTree.setType(nodeToBeMutated, newType);
		assert mutatedTree.isConsistent();

		didChange(nodeToBeMutated, NAryTreeHistory.TypesOfChange.OTHER);

		return mutatedTree;
	}

	/**
	 * @see TreeMutationAbstract#getKey()
	 */
	public String getKey() {
		return key;
	}
}
