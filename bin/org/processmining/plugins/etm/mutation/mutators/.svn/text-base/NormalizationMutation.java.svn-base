package org.processmining.plugins.etm.mutation.mutators;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * This mutation normalizes the process tree.
 * 
 * @author jbuijs
 *
 */
public class NormalizationMutation extends TreeMutationAbstract {

	@SuppressWarnings("unused")
	private String key = "TreeNormalization";

	public NormalizationMutation(CentralRegistry registry) {
		super(registry);
	}

	public NAryTree mutate(NAryTree tree) {
		return mutate(tree, 0);
	}

	public NAryTree mutate(NAryTree tree, int node) {
		NAryTree normalized = TreeUtils.normalize(tree);

		if (normalized.equals(tree)) {
			noChange();
		} else {
			didChange(node, NAryTreeHistory.TypesOfChange.OTHER);
		}

		return normalized;
	}

}
