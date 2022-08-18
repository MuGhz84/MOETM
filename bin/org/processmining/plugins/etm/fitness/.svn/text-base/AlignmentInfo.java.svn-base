package org.processmining.plugins.etm.fitness;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections15.map.LRUMap;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.replayer.AStarAlgorithm;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;

/**
 * Quick class to store alignments in which can be necessary for some
 * experiments etc.
 * 
 * @author jbuijs
 * 
 */
public class AlignmentInfo {
	private Map<NAryTree, TreeRecord> alignmentCache = null;
	private AStarAlgorithm algorithm;

	public AlignmentInfo(Map<NAryTree, TreeRecord> alignmentCache, AStarAlgorithm algorithm) {
		this.algorithm = algorithm;
		this.alignmentCache = alignmentCache;
	}

	public synchronized void put(NAryTree candidate, TreeRecord lastRecord) {
		alignmentCache.put(candidate, lastRecord);
	}

	public AlignmentInfo(int maxSize) {
		alignmentCache = Collections.synchronizedMap(new LRUMap<NAryTree, TreeRecord>(maxSize));
	}

	/**
	 * @return the alignmentCache
	 */
	public Map<NAryTree, TreeRecord> getAlignmentCache() {
		return alignmentCache;
	}

	/**
	 * @return the algorithm
	 */
	public AStarAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(AStarAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
}