package org.processmining.plugins.etm.mutation.mutators.maikelvaneck;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * 
 * @author Maikel van Eck
 */
public class ReplaceTreeByLeafMutation extends TreeMutationAbstract {

	public ReplaceTreeByLeafMutation(CentralRegistry registry) {
		super(registry);
	}

	public NAryTree mutate(NAryTree tree) {
		return mutate(tree,0);
	}
	
	public NAryTree mutate(NAryTree tree, int node) {
		NAryTree newTree = LeafFactory.generateRandomCandidate(registry);
		return tree.replace(node, newTree, 0);
	}
	
}
