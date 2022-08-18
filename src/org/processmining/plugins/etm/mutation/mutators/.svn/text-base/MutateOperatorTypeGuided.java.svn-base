package org.processmining.plugins.etm.mutation.mutators;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.BehaviorCounter;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * DISABLED 
 */
//FIXME check all class contents
//FIXME unimplemented code
public class MutateOperatorTypeGuided extends TreeMutationAbstract {

	private String key = "MutateOperatorTypeGuided";

	public MutateOperatorTypeGuided(CentralRegistry registry) {
		super(registry);
	}

	/**
	 * Walks through the tree to find an operator node that behaves differently
	 * than it should. Then corrects the most serious misbehavior.
	 * 
	 * @return changed node
	 */
	public NAryTree mutate(NAryTree tree, int node) {
		if(true){
			noChange();
			return tree;
		}
		
		assert tree.isConsistent();
		/*
		 * First, find the best candidates so walk through the tree and find
		 * those operators that behaved differently just too often
		 */
		//stores the fraction of 'different' behavior (e.g. fraction notUsed or balance between L and R for XOR)

		//Remember the worst operator node we encountered
		int worstNode = -1;
		double worstScore = 0;

		//1. loop through all nodes, searching for a place to mutate
		@SuppressWarnings("unused")
		BehaviorCounter b = registry.getFitness(tree).behaviorCounter;
		@SuppressWarnings("unused")
		int endOfSubtree = tree.getNext(node);
		//FIXME the behaviour counter does not record the required information so disabled function below
		/*-
		for (int i = node; i < endOfSubtree; i++) {
			//Lets start by the fraction of times the node is not used
			double score = 0;
			if (b.countTotal() > 0)
				score = ((double) b.getNotUsed() / b.countTotal());

			switch (tree.getType(i)) {
				case NAryTree.SEQ :
					break;
				case NAryTree.OR :
					//Check for behavior as XOR and AND (so fall through once)
				case NAryTree.XOR :
					//For XOR check also for an unbalance in L vs R
					if (b.countTotal() > 0) {
						score = Math.max(
								score,
								Math.max(((double) b.getBehavedAsL() / b.countTotal()),
										((double) b.getBehavedAsR() / b.countTotal())));
					}

					if (tree.getType(i) == NAryTree.XOR)
						//Stop here for XORs
						break;
					//continue for OR
					//$FALL-THROUGH$
				case NAryTree.AND :
					//check for unbalance between SEQuential behavior
					if (b.countTotal() > 0) {
						score = Math.max(
								score,
								Math.max(((double) b.getBehavedAsSEQLR() / b.countTotal()),
										((double) b.getBehavedAsSEQRL() / b.countTotal())));
					}
					break;
				case NAryTree.LOOP :
					//Check for low amounts of loop block executions
					//arbitrary score (wrt the others), L=2->score~.7
					score = Math.max(score, (1.0 / Math.sqrt(b.getBehavedAsL())));
					break;
				default :
					break;
			}

			//If we have a score of (almost) 1 then there certainly is no need to look any further
			if (worstScore > .9999) {
				worstScore = score;
				worstNode = i;
				break;
			}

			//Remember the worst node encountered so far
			if (score > worstScore && (registry.getRandom().nextBoolean() || worstScore == 0)) {
				worstScore = score;
				worstNode = i;
			}

		}/**/

		//remark: using the TObject forEachEntry call does not work because updating vars does not work (for me)
		/*-
		for (Node candidateNode : candidates.keySet()) {
			if (candidates.get(candidateNode) > worstScore) {
				worstNode = candidateNode;
				worstScore = candidates.get(candidateNode);
			}
		}/**/

		//If there is no 'worst' node then stop...
		if (worstNode == -1) {
			noChange();
			return tree;
		}

		//Get more information about the behavior
		double notUsedScore = 0;
		/*-
		if (b.countTotal() > 0) {
			notUsedScore = (b.getNotUsed() / b.countTotal());
		}/**/

		if (notUsedScore == worstScore) {
			//This node is not used so remove
			didChange(tree.getParent(worstNode), NAryTreeHistory.TypesOfChange.OTHER);
			NAryTree newTree = tree.remove(worstNode);
			assert newTree.isConsistent();
			return newTree;
		}

		/*
		 * If the node is used, lets see how we should improve this one
		 */
		/*-
		switch (tree.getType(worstNode)) {
			case NAryTree.SEQ :
				//SEQ only has the 'not used' score so we should not have ended up here...
				noChange();
				return tree;
			case NAryTree.OR :
			case NAryTree.XOR :
				//Check which behavior is bad
				if (b.getMoveCount()[i] > 0) {
					NAryTree newTree;
					
					if (((float) b.getBehavedAsL() / b.countTotal()) == worstScore) {
						//The XOR mainly executed its left child
						//So, remove XOR and right child
						worstNode = worstNode.removeChild(Node.RIGHTCHILDINDEX);
					} else if (((float) b.getBehavedAsR() / b.countTotal()) == worstScore) {
						//other way round
						check(node);
						worstNode = worstNode.removeChild(Node.LEFTCHILDINDEX);
					}
					didChange(worstNode);

					noChange(); //TEMPORARILY of course
					return tree;
				}

				//If an XOR makes it here we didn't do skwat
				if (tree.getType(worstNode) == NAryTree.XOR) {
					noChange();
					return tree;
				}
				//OR:
				//$FALL-THROUGH$
			case NAryTree.AND :
				if (b.countTotal() > 0) {
					boolean changingRoot = false;
					if (worstNode == node) {
						changingRoot = true;
					}

					if (((float) b.getBehavedAsSEQLR() / b.countTotal()) == worstScore) {
						worstNode.setType(Type.SEQ);
					} else if (((float) b.getBehavedAsSEQRL() / b.countTotal()) == worstScore) {
						check(node);
						worstNode.setType(Type.SEQ);
						worstNode.swapChildren();
					}

					didChange(worstNode);

					noChange();
					return tree;
				}
				noChange();
				return tree;
			case NAryTree.LOOP :
				if ((float) (1 / Math.sqrt(b.getBehavedAsL())) == worstScore) {
					check(node);
					worstNode.setType(Type.SEQ);
					//worstNode.removeChild(Node.RIGHTCHILDINDEX);
					didChange(worstNode);
					check(node);
					return node;
				}
				
				noChange();
				return tree;
		}/**/

		//If we made it here then we did not change anything
		noChange();
		return tree;
	}

	/**
	 * @see TreeMutationAbstract#getKey()
	 */
	public String getKey() {
		return key;
	}
}
