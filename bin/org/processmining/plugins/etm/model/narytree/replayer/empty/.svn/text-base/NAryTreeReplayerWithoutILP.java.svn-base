package org.processmining.plugins.etm.model.narytree.replayer.empty;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.set.TIntSet;

import java.util.Map;

import nl.tue.astar.AStarObserver;
import nl.tue.astar.AStarThread.Canceller;
import nl.tue.astar.Trace;

import org.processmining.plugins.boudewijn.treebasedreplay.astar.ModelPrefix;
import org.processmining.plugins.boudewijn.treebasedreplay.astar.TreeMarkingVisit;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.AStarAlgorithm;
import org.processmining.plugins.etm.model.narytree.replayer.AbstractNAryTreeReplayer;
import org.processmining.plugins.etm.model.narytree.replayer.NAryTreeHead;
import org.processmining.plugins.etm.model.narytree.replayer.NAryTreePostProcessor;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;

public class NAryTreeReplayerWithoutILP extends
		AbstractNAryTreeReplayer<NAryTreeHead, NAryTreeEmptyTail, NAryTreeEmptyDelegate> {

	private NAryTreePostProcessor<NAryTreeHead, NAryTreeEmptyTail> postProcessor;

	public NAryTreeReplayerWithoutILP(AStarAlgorithm aStarLogAbstraction, Canceller canceller, NAryTree tree,
			int configurationNumber, int[] node2cost, Map<TreeMarkingVisit<ModelPrefix>, TIntSet> marking2modelmove,
			TObjectIntMap<TreeMarkingVisit<ModelPrefix>> marking2visitCount, int[] syncMoveCount, int[] aSyncMoveCount,
			int[] moveCount, Map<Trace, TreeRecord> alignments, AStarObserver... observers) {
		super(aStarLogAbstraction, canceller, tree, configurationNumber, node2cost, false, observers);
		this.postProcessor = new NAryTreePostProcessor<NAryTreeHead, NAryTreeEmptyTail>(delegate, tree,
				configurationNumber, marking2modelmove, marking2visitCount, syncMoveCount, aSyncMoveCount, moveCount,
				alignments);
	}

	protected int calculateCostAndPostProcess(VerboseLevel verbose, Trace trace, TreeRecord r, int frequency) {

		postProcessor.process(this, verbose, trace, r, frequency);

		int cost = frequency * r.getCostSoFar();
		TreeRecord rec = r;
		do {
			cost -= frequency * ((rec.getModelMove() < tree.size() ? 1 : 0) + rec.getInternalMovesCost());
			rec = rec.getPredecessor();
		} while (rec.getPredecessor() != null);
		cost -= frequency * rec.getInternalMovesCost();
		return cost;
	}

	protected NAryTreeHead createInitialHead(Trace trace) {
		return new NAryTreeHead(delegate, trace);
	}

	protected NAryTreeEmptyDelegate constructDelegate(AStarAlgorithm algorithm, NAryTree tree, int configurationNumber,
			int[] node2cost, int threads, boolean useOrRows) {
		return new NAryTreeEmptyDelegate(algorithm, tree, configurationNumber, node2cost, threads);
	}

}