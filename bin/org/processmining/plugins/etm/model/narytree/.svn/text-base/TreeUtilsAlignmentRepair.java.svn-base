package org.processmining.plugins.etm.model.narytree;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.etm.model.narytree.NAryTreeHistory.TypesOfChange;
import org.processmining.plugins.etm.model.narytree.replayer.TreeRecord;

public class TreeUtilsAlignmentRepair {

	/**
	 * Returns the Lowest Common Ancestor (LCA) of two nodes a and b. 1. move up
	 * one node until the root, map parents 2. move up the other node, return
	 * the first match with the mapped parents of 1. Fails if the inputs nodes
	 * are not in the tree.
	 * 
	 * @param tree
	 * @param a
	 * @param b
	 * @return
	 */
	public static int LCA(NAryTree tree, int a, int b) {
		// if any of the nodes is the root node
		if (a == 0 || b == 0) {
			return 0;
		}
		byte boo[] = new byte[tree.size()];
		//TODO: merge both do-while loops.
		do {
			a = tree.getParentFast(a);
			boo[a] = 1;
		} while (a != 0);
		do {
			b = tree.getParentFast(b);
			if (boo[b] == 1) {
				return b;
			}
		} while (b != 0);

		return 0;
	}

	/** UNTESTED **/
	public static int LCAImpr(NAryTree tree, int a, int b) {
		// if any of the nodes is the root node
		if (a == 0 || b == 0) {
			return 0;
		}
		byte boo[] = new byte[tree.size()];
		do {
			if (a != 0) {
				a = tree.getParentFast(a);
				if (boo[a] == 1) {
					return a;
				} else {
					boo[a] = 1;
				}
			}
			if (b != 0) {
				b = tree.getParentFast(b);
				if (boo[b] == 1) {
					return b;
				} else {
					boo[b] = 1;
				}
			}
		} while (a != 0 && b != 0);
		return 0;
	}

	public static NAryTree emptyTree() {
		return new NAryTreeImpl(new int[0], new short[0], new int[0]);
	}

	/**
	 * Function that returns the fixed points of change of a candidate after the
	 * crossover and the mutation. If one of the points of change is discarted,
	 * it is changed as NaryTree.NONE
	 * 
	 * @param candidate
	 * @param parentHistory
	 *            of the candidate
	 * @param grandparentHistory
	 *            of the candidate
	 * @return {mutationPoint, crossoverPoint}
	 */
	public static int[] getPointsOfChange(NAryTree candidate, NAryTreeHistory parentHistory,
			NAryTreeHistory grandparentHistory) {
		//The mutation point ALWAYS dominates the crossover point.
		int mutationPoint = parentHistory.getLocationOfChange();
		int crossoverPoint = grandparentHistory.getLocationOfChange();
		final TypesOfChange typeOfMutation = parentHistory.getTypeOfChange();
		final NAryTree parent = parentHistory.getParent();
		if (crossoverPoint == NAryTree.NONE) {
			// If  the crossover changed a tree that was only a Leaf and NaryTree.getParent() returned NONE
			mutationPoint = 0;
		} else if (crossoverPoint == 0 || mutationPoint == 0) {
			// if the crossover or the mutation were performed on the root node, calculate all the alignments as normal :(
			mutationPoint = 0;
			crossoverPoint = NAryTree.NONE;
		} else if (crossoverPoint == mutationPoint) {
			// if both positions are the same, just one point.
			crossoverPoint = NAryTree.NONE;
		} else if (mutationPoint > crossoverPoint) {
			//Mutation was after the crossover point
			if (candidate.isInSubtree(crossoverPoint, mutationPoint)) {
				// the mutation is changing the node added during the crossover, so is the same node to reevaluate.
				mutationPoint = crossoverPoint;
				crossoverPoint = NAryTree.NONE;
			} else {
				//Take the lowest common parent, or return two values;
				if (typeOfMutation == TypesOfChange.ADD || typeOfMutation == TypesOfChange.OTHER) {
					int commonParent = TreeUtilsAlignmentRepair.LCA(candidate, mutationPoint, crossoverPoint);
					if (mergeChangePoints(candidate, commonParent, mutationPoint, crossoverPoint)) {
						mutationPoint = commonParent;
						crossoverPoint = NAryTree.NONE;
					}

				} else {
					int commonParent = TreeUtilsAlignmentRepair.LCA(parent, mutationPoint, crossoverPoint);
					if (mergeChangePoints(parent, commonParent, mutationPoint, crossoverPoint)) {
						mutationPoint = commonParent;
						crossoverPoint = NAryTree.NONE;
					}
				}
			}
		} else {
			//Mutation was before the crossover point, (mutationPoint < crossoverPoint)
			if (parent.isInSubtree(mutationPoint, crossoverPoint)) {
				// The mutation was performed in a higher node than the crossover
				crossoverPoint = NAryTree.NONE;
			} else {
				//The mutation point is before the crossover point, so the crossover point has to be moved accordingly:
				crossoverPoint = crossoverPoint + (candidate.size() - parent.size());
				//Take the lowest common parent, or return two values;
				if (typeOfMutation == TypesOfChange.ADD || typeOfMutation == TypesOfChange.OTHER) {
					int commonParent = TreeUtilsAlignmentRepair.LCA(candidate, mutationPoint, crossoverPoint);
					if (mergeChangePoints(candidate, commonParent, mutationPoint, crossoverPoint)) {
						mutationPoint = commonParent;
						crossoverPoint = NAryTree.NONE;
					}
				} else {
					// remember that the crossover point has been moved, so take the original!
					int commonParent = TreeUtilsAlignmentRepair.LCA(parent, mutationPoint, grandparentHistory.getLocationOfChange());
					if (mergeChangePoints(parent, commonParent, mutationPoint,
							grandparentHistory.getLocationOfChange())) {
						mutationPoint = commonParent;
						crossoverPoint = NAryTree.NONE;
					}
				}
			}
		}
		return new int[] { mutationPoint, crossoverPoint };
	}

	/**
	 * Function to decide if to take the LCA between the crossover and the
	 * mutation point (FALSE), or use both points instead (TRUE).
	 */
	private static boolean mergeChangePoints(NAryTree candidate, int commonParent, int mutationPoint,
			int crossoverPoint) {
		int sizeCommon = candidate.size(commonParent)
				- (candidate.size(mutationPoint) + candidate.size(crossoverPoint));
		return sizeCommon < candidate.size() / 2 ? true : false;
		//		return true;
	}

	/*** performance issue? recursive VS iteration ***/

	// DISABLED!!!!! THIS IS FOR THE PREVIOUS VERSION BASED ON THE BLOCKS, AND IT'S OUTDATED FOR THE NEW VERSION.
	// JVM does not handle very well recursive calls. 
	//	public static TreeRecord recursiveFixAlignment(TreeRecord chunkRecord, TreeRecord startRecord, int shiftModel,
	//			int shiftSubtree, int shiftEvent, int endScope, int sizeSubtree, int sizeTree) {
	//
	//		if (chunkRecord.getBacktraceSize() == 1) {
	//			startRecord.shiftInternalMoves(endScope, shiftSubtree, shiftModel, chunkRecord.getPredecessor());
	//			return chunkRecord.shift(startRecord, shiftSubtree, shiftEvent, startRecord.getBacktraceSize(), sizeSubtree,
	//					sizeTree);
	//		} else {
	//			return chunkRecord.shift(
	//					recursiveFixAlignment(chunkRecord.getPredecessor(), startRecord, shiftModel, shiftSubtree,
	//							shiftEvent, endScope, sizeSubtree, sizeTree),
	//					shiftSubtree, shiftEvent, startRecord.getBacktraceSize(), sizeSubtree, sizeTree);
	//		}
	//	}

	// The iterative approach requires to retrieve the history of the chunk
	 /*-
		// FIXME JBUIJS DISABLED MIGRATION FROM BORJA CODE
	public static TreeRecord iterativeFixAlignment(List<TreeRecord> history, TreeRecord startRecord, int shiftModel,
			int shiftSubtree, int shiftEvent, int endSubtree, int sizeSubtree, int sizeTree) {
		TreeRecord last;
		int backTraceCounter = startRecord.getBacktraceSize() + 1;
		if (history.size() > 1) {
			TreeRecord aux = history.get(0);
			int pointOfStart = 1;
			if (aux.getModelMove() == AStarThread.NOMOVE && aux.getMovedEvent() == AStarThread.NOMOVE) {
				startRecord.mergeMoves(endSubtree, shiftSubtree, shiftSubtree, history.get(0));
				aux = history.get(1);
				pointOfStart++;
			}
			last = aux.shift(startRecord, shiftSubtree,
					aux.getMovedEvent() == AStarThread.NOMOVE ? AStarThread.NOMOVE : shiftEvent++, backTraceCounter++,
					sizeSubtree, sizeTree);
			final int sizeHistory = history.size();
			for (int i = pointOfStart; i < sizeHistory; i++) {
				aux = history.get(i);
				last = aux.shift(last, shiftSubtree,
						aux.getMovedEvent() == AStarThread.NOMOVE ? AStarThread.NOMOVE : shiftEvent++,
						backTraceCounter++, sizeSubtree, sizeTree);
			}
		} else if (history.size() == 1) {
			TreeRecord aux = history.get(0);
			if (aux.getModelMove() == AStarThread.NOMOVE && aux.getMovedEvent() == AStarThread.NOMOVE) {
				startRecord.mergeMoves(endSubtree, shiftSubtree, shiftSubtree, history.get(0));
				last = startRecord;
			} else {
				last = aux.shift(startRecord, shiftSubtree,
						aux.getMovedEvent() == AStarThread.NOMOVE ? AStarThread.NOMOVE : shiftEvent++,
						backTraceCounter++, sizeSubtree, sizeTree);
			}
		} else {
			last = startRecord;
		}

		return last;
	}/**/

	public static List<TreeRecord> splitChunk(List<TreeRecord> history, int start, Integer numLogMoves,
			int subtreeSize) {
		int auxCounter = 0;
		final int sizeHistory = history.size();
		List<TreeRecord> newList = new ArrayList<>(sizeHistory);
		for (int i = start; i < sizeHistory; i++) {
			TreeRecord record = history.get(i);
			if (record.getMovedEvent() >= 0) {
				auxCounter++;
			}
			newList.add(record);

			if (numLogMoves == 0 && auxCounter > 0) {
				newList.remove(newList.size() - 1);
				break;
			}

			if (auxCounter >= numLogMoves) {
				boolean moreJoins = true;
				int nextInt = i+1;
				do {
					if (nextInt < sizeHistory) {
						TreeRecord recordAux = history.get(nextInt);
						if (recordAux.getModelMove() > subtreeSize) {
							newList.add(recordAux);
							nextInt++;
						} else {
							moreJoins = false;
						}
					} else {
						moreJoins = false;
					}
				} while (moreJoins);
				break;
			}
		}
		return newList;
	}

	/**
	 * Returns the lowest parent that is equal to the input parent. If no parent
	 * is present with the given type, the same node is returned
	 */
	public static int replaceWithParentType(int node, final int TYPE, final NAryTree tree) {
		int auxNode = node;
		
		while (auxNode != NAryTree.NONE) {
			auxNode = tree.getParentFast(auxNode);
			if (tree.getType(auxNode) == TYPE) {
				node = auxNode;
			}
		}
		return node;
	}

}
