package org.processmining.plugins.etm.model.narytree.replayer.lp;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.set.TIntSet;

import java.util.Map;

import nl.tue.astar.AStarException;
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
import org.processmining.plugins.etm.model.narytree.replayer.hybridilp.NAryTreeHybridILPTail;

public class NAryTreeReplayerWithLP extends
		AbstractNAryTreeReplayer<NAryTreeHead, NAryTreeHybridILPTail, NAryTreeLPDelegate> {

	private NAryTreePostProcessor<NAryTreeHead, NAryTreeHybridILPTail> postProcessor;

	public NAryTreeReplayerWithLP(AStarAlgorithm aStarLogAbstraction, Canceller canceller, NAryTree tree,
			int configurationNumber, int[] node2cost, Map<TreeMarkingVisit<ModelPrefix>, TIntSet> marking2modelmove,
			TObjectIntMap<TreeMarkingVisit<ModelPrefix>> marking2visitCount, int[] syncMoveCount, int[] aSyncMoveCount,
			int[] moveCount, Map<Trace, TreeRecord> alignments, boolean useOrRows, AStarObserver... observers) {
		super(aStarLogAbstraction, canceller, tree, configurationNumber, node2cost, useOrRows, observers);
		this.postProcessor = new NAryTreePostProcessor<NAryTreeHead, NAryTreeHybridILPTail>(delegate, tree,
				configurationNumber, marking2modelmove, marking2visitCount, syncMoveCount, aSyncMoveCount, moveCount,
				alignments);

	}

	protected int calculateCostAndPostProcess(VerboseLevel verbose, Trace trace, TreeRecord r, int frequency) {

		postProcessor.process(this, verbose, trace, r, frequency);

		int cost = frequency * r.getCostSoFar();
		TreeRecord rec = r;
		while (rec.getPredecessor() != null) {
			cost -= frequency * ((rec.getModelMove() < tree.size() ? 1 : 0) + rec.getInternalMovesCost());
			rec = rec.getPredecessor();
		}
		cost -= frequency * rec.getInternalMovesCost();
		return cost;
	}

	protected NAryTreeHead createInitialHead(Trace trace) {
		return new NAryTreeHead(delegate, trace);
	}

	protected NAryTreeLPDelegate constructDelegate(AStarAlgorithm algorithm, NAryTree tree, int configurationNumber,
			int[] node2cost, int threads, boolean useOrRows) {
		return new NAryTreeLPDelegate(algorithm, tree, configurationNumber, node2cost, threads, useOrRows);
	}

	public int run(VerboseLevel verbose, int stopAt, final int minModelCost, double timeLimit) throws AStarException {
		try {
			return super.run(verbose, stopAt, minModelCost, timeLimit);
		} finally {
			delegate.deleteLPs();
		}

	}

}
