package org.processmining.plugins.etm.factory;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;

import java.util.HashMap;
import java.util.Random;

import nl.tue.astar.Trace;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.replayer.AStarAlgorithm;

public class TreeFactoryXORofSEQ extends TreeFactoryAbstract {

	public TreeFactoryXORofSEQ(CentralRegistry registry) {
		super(registry);
	}

	public NAryTree generateRandomCandidate(Random rng) {
		return createXORofSEQ(registry);
	}

	public static NAryTree createXORofSEQ(CentralRegistry registry) {
		return createXORofSEQ(registry, 1.0);
	}

	public static NAryTree createXORofSEQ(CentralRegistry registry, double fraction) {
		HashMap<XEventClass, Integer> logCosts = new HashMap<XEventClass, Integer>();
		for (XEventClass eventClass : registry.getEventClasses().getClasses()) {
			logCosts.put(eventClass, 1);
		}
		//Instantiate an alg. for the whole log
		AStarAlgorithm algorithm = new AStarAlgorithm(registry.getLog(), registry.getEventClasses(), logCosts);

		//Get the converted log (clustered on event order)
		TObjectIntMap<Trace> converted = algorithm.getConvertedLog();

		//Get the iterator
		TObjectIntIterator<Trace> itClusters = converted.iterator();

		//The number of clusters to obtain, minimally 1
		int nrClustersToInclude = Math.max((int) Math.round(converted.size() * fraction), 1);

		//Now reduce the number of clusters to the desired size
		while (converted.size() > nrClustersToInclude) {
			itClusters.advance();
			itClusters.remove();
		}

		//Now make sequences for each of the trace clusters and add it in one big XOR
		NAryTree tree = null;
		//new NAryTreeImpl(new int[] { 0 }, new short[] { NAryTree.XOR }, new int[] { NAryTree.NONE });

		itClusters = converted.iterator();
		while (itClusters.hasNext()) {
			itClusters.advance();
			Trace trace = itClusters.key();

			int[] next = new int[trace.getSize() + 1];
			short[] type = new short[trace.getSize() + 1];
			int[] parent = new int[trace.getSize() + 1];

			next[0] = trace.getSize() + 1;
			type[0] = NAryTree.SEQ;
			parent[0] = NAryTree.NONE;

			for (int i = 0; i < trace.getSize(); i++) {
				next[i + 1] = i + 2;
				type[i + 1] = (short) trace.get(i);
				parent[i + 1] = 0;
			}

			NAryTree seqTree = new NAryTreeImpl(next, type, parent);
			assert seqTree.isConsistent();

			if (tree != null) {
				tree = tree.add(seqTree, 0, 0, tree.nChildren(0));
			} else {
				tree = seqTree.addParent(0, NAryTree.XOR, Configuration.NOTCONFIGURED);
			}
		}

		assert tree.isConsistent();

		return tree;
	}
}
