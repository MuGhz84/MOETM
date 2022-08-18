package org.processmining.plugins.etm.mutation.mutators.maikelvaneck;

import java.util.Random;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.CentralRegistryConfigurable;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.factories.AbstractCandidateFactory;

/**
 * Creates a tree of a single leaf
 * 
 * @author Maikel van Eck
 */
public class LeafFactory extends AbstractCandidateFactory<NAryTree> {
	private CentralRegistry registry;

	public LeafFactory(CentralRegistry registry) {
		if (registry == null) {
			throw new IllegalArgumentException("The central bookkeeper can not be empty");
		}

		this.registry = registry;
	}

	public NAryTree generateRandomCandidate(Random rng) {
		return generateRandomCandidate(registry);
	}

	public static NAryTree generateRandomCandidate(CentralRegistry registry) {
		NAryTree tree;

		tree = TreeUtils.randomTree(registry.nrEventClasses(), 1, 1, 1, registry.getRandom());

		//Now make sure the tree has enough configurations, if required
		if (registry instanceof CentralRegistryConfigurable) {
			CentralRegistryConfigurable cr = (CentralRegistryConfigurable) registry;
			while (tree.getNumberOfConfigurations() < cr.getNrLogs()) {
				tree.addConfiguration(new Configuration(new boolean[tree.size()], new boolean[tree.size()]));
			}
		}

		return tree;
	}
}
