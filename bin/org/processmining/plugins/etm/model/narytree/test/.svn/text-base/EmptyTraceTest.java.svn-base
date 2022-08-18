package org.processmining.plugins.etm.model.narytree.test;

import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.hash.TObjectShortHashMap;

import java.util.Random;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.fitness.metrics.PrecisionEscEdges;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.Simulator;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.parameters.ETMParam;

public class EmptyTraceTest {

	public static void main(String[] arg) {
		// Build a tree
		int minNodes = 2;
		int maxNodes = 3;
		double noise = 0.0;
		int numTraces = 250;
		String[] activities = new String[] { "A", "B" };
		TObjectShortMap<String> map = new TObjectShortHashMap<String>();
		for (short i = 0; i < activities.length; i++) {
			map.put(activities[i], i);
		}
		NAryTree tree = TreeUtils.randomTree(map.keySet().size(), 0.4, minNodes, maxNodes, new Random(34121));
		boolean[] b = new boolean[tree.size()];
		boolean[] h = new boolean[tree.size()];
		Configuration c = new Configuration(b, h);
		tree.addConfiguration(c);

		// Generate a log
		Simulator sim = new Simulator(tree, 0, new Random(5897630));
		String[][] traces = new String[numTraces][];
		traces[0] = new String[] {};
		for (int i = 1; i < numTraces; i++) {
			traces[i] = sim.getRandomTrace(activities, noise);
		}
		XLog eventlog = LogCreator.createLog(traces);
		CentralRegistry centralRegistry = new CentralRegistry(eventlog, ETMParam.createRNG());

		System.out.println("Tree:" + tree);
		System.out.println("Log:" + centralRegistry.getaStarAlgorithm().getConvertedLog());

		FitnessReplay fr = new FitnessReplay(centralRegistry, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});

		// Setup stuff for ETM
		Canceller canceller = new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		};
		double fitnessLimit = -1;
		double maxFTime = 1.0;

		PrecisionEscEdges pe = new PrecisionEscEdges(centralRegistry);

		double frVal = fr.getFitness(tree, null);
		double peVal = pe.getFitness(tree, null);

		System.out.println("Vals: " + frVal + " and " + peVal);
	}

}
