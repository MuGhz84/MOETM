package org.processmining.plugins.etm.factory;

import java.util.Random;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;

public class TreeFactoryFlower extends TreeFactoryAbstract {

	public TreeFactoryFlower(CentralRegistry registry) {
		super(registry);
	}

	public static NAryTree createFlower(CentralRegistry registry) {
		//First instantiate a XOR with only the first event class
		NAryTree xor = new NAryTreeImpl(new int[] { 2, 2 }, new short[] { NAryTree.XOR, 0 }, new int[] { NAryTree.NONE,
				0 });
		//Then add all others
		for (short i = 1; i < registry.getLogInfo().getEventClasses().size(); i++) {
			xor = xor.addChild(0, xor.nChildren(0), i, Configuration.NOTCONFIGURED);
		}

		//Then instantiate a loop with 3 TAU children
		NAryTree flower = new NAryTreeImpl(new int[] { 4, 2, 3, 4 }, new short[] { NAryTree.LOOP, NAryTree.TAU,
				NAryTree.TAU, NAryTree.TAU }, new int[] { NAryTree.NONE, 0, 0, 0 });

		//Replace the 'do' TAU with this XOR of all event classes
		flower = flower.replace(1, xor, 0);

		return flower;
	}

	public NAryTree generateRandomCandidate(Random rng) {
		return createFlower(registry);
	}
}
