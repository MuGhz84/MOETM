package org.processmining.plugins.etm.mutation.mutators.maikelvaneck;

import gnu.trove.map.TObjectIntMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import nl.tue.astar.Trace;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.CentralRegistryConfigurable;
import org.processmining.plugins.etm.model.narytree.Configuration;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.factories.AbstractCandidateFactory;

/**
 * Selects a random trace from the log and builds a sequence tree, might
 * introduce a loop if an activity occurs multiple times within a trace
 * 
 * @author Maikel van Eck
 */
public class SequenceFactory extends AbstractCandidateFactory<NAryTree> {
	//Thesis 3.4.1 and appendix A comparison
	private CentralRegistry registry;

	public SequenceFactory(CentralRegistry registry) {
		if (registry == null) {
			throw new IllegalArgumentException("The central bookkeeper can not be empty");
		}

		this.registry = registry;
	}

	public NAryTree generateRandomCandidate(Random rng) {
		return generateRandomCandidate(registry);
	}

	public static NAryTree generateRandomCandidate(CentralRegistry registry) {
		// Get the converted log
		TObjectIntMap<Trace> converted = registry.getaStarAlgorithm().getConvertedLog();

		// Randomly select a trace from the log
		int traceNr = registry.getRandom().nextInt(registry.getLog().size());
		int currentTrace = 0;
		while (traceNr - converted.get(converted.keys()[currentTrace]) >= 0 && currentTrace < converted.keys().length) {
			traceNr -= converted.get(converted.keys()[currentTrace]);
			currentTrace += 1;
		}

		Trace trace = (Trace) converted.keys()[currentTrace];
		NAryTree tree = null;

		//System.out.println(trace);

		boolean detectLoops = false;
		if (detectLoops && registry.getRandom().nextDouble() < 0.5) {
			// Detect loops
			HashMap<Integer, Integer> eventCounter = new HashMap<Integer, Integer>();
			for (int i = 0; i < trace.getSize(); i++) {
				// Count how often each event class occurs in the trace
				if (eventCounter.containsKey(trace.get(i))) {
					eventCounter.put(trace.get(i), eventCounter.get(trace.get(i)) + 1);
				} else {
					eventCounter.put(trace.get(i), 1);
				}
			}

			//System.out.println(eventCounter);

			ArrayList<Integer> seqList = new ArrayList<Integer>();
			ArrayList<Integer> loopDoList = new ArrayList<Integer>();
			ArrayList<Integer> loopRedoList = new ArrayList<Integer>();
			int firstLoopEvent = -1;
			for (int i = 0; i < trace.getSize(); i++) {
				if (eventCounter.get(trace.get(i)) == 1) {
					seqList.add(trace.get(i));
				} else if (eventCounter.get(trace.get(i)) != -1) {
					if (registry.getRandom().nextDouble() < 0.5) {
						loopDoList.add(trace.get(i));
					} else {
						loopRedoList.add(trace.get(i));
					}
					eventCounter.put(trace.get(i), -1);
					if (firstLoopEvent == -1) {
						firstLoopEvent = i;
						seqList.add(-1);
					}
				}
			}

			//System.out.println(seqList);
			//System.out.println(loopList);
			//System.out.println(loopDoList);
			//System.out.println(loopRedoList);
			//System.out.println(firstLoopEvent);

			// Build the loopTree
			NAryTree loopTree = TreeUtils.fromString("LOOP( LEAF: tau , LEAF: tau , LEAF: tau )",
					registry.getEventClasses());
			if (loopDoList.size() == 1) {
				loopTree.setType(1, loopDoList.get(0).shortValue());
			} else if (loopDoList.size() > 1) {
				loopTree.setType(1, NAryTree.SEQ);
				for (int i = 0; i < loopDoList.size(); i++) {
					loopTree = loopTree.addChild(1, loopTree.nChildren(1), loopDoList.get(i).shortValue(),
							Configuration.NOTCONFIGURED);
				}
			}
			if (loopRedoList.size() == 1) {
				loopTree.setType(loopTree.getChildAtIndex(0, 1), loopRedoList.get(0).shortValue());
			} else if (loopRedoList.size() > 1) {
				loopTree.setType(loopTree.getChildAtIndex(0, 1), NAryTree.SEQ);
				for (int i = 0; i < loopRedoList.size(); i++) {
					loopTree = loopTree.addChild(loopTree.getChildAtIndex(0, 1),
							loopTree.nChildren(loopTree.getChildAtIndex(0, 1)), loopRedoList.get(i).shortValue(),
							Configuration.NOTCONFIGURED);
				}
			}

			//System.out.println(loopTree);

			tree = TreeUtils.fromString(" LEAF: tau ", registry.getEventClasses());
			tree.setType(0, NAryTree.SEQ);
			for (int i = 0; i < seqList.size(); i++) {
				if (seqList.get(i) == -1) {
					tree = tree.add(loopTree, 0, 0, tree.nChildren(0));
				} else {
					tree = tree
							.addChild(0, tree.nChildren(0), seqList.get(i).shortValue(), Configuration.NOTCONFIGURED);
				}
			}
		} else {
			// Create a SEQ-tree
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

			tree = new NAryTreeImpl(next, type, parent);
		}

		//System.out.println(tree);

		//Now make sure the tree has enough configurations, if required
		if (registry instanceof CentralRegistryConfigurable) {
			CentralRegistryConfigurable cr = (CentralRegistryConfigurable) registry;
			while (tree.getNumberOfConfigurations() < cr.getNrLogs()) {
				tree.addConfiguration(new Configuration(new boolean[tree.size()], new boolean[tree.size()]));
			}
		}

		assert tree.isConsistent();

		return tree;
	}

}
