package org.processmining.plugins.etm.mutation.mutators.maikelvaneck;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * 
 * @author Maikel van Eck
 */
public class ReplaceTreeByIntelligentTreeMutation extends TreeMutationAbstract {

	public ReplaceTreeByIntelligentTreeMutation(CentralRegistry registry) {
		super(registry);
	}

	public NAryTree mutate(NAryTree tree) {
		return mutate(tree,0);
	}
	
	public NAryTree mutate(NAryTree tree, int node) {
		NAryTree newTree = IntelligentTreeFactory.generateRandomCandidate(registry);
		return tree.replace(node, newTree, 0);
	}

}
