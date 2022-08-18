package org.processmining.plugins.etm.mutation.mutators;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.BehaviorCounter;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * DISABLED Smartly adds an activity where logmoves are detected.
 */
//FIXME check all class contents
//FIXME UNTESTED code
public class InsertActivityGuided extends TreeMutationAbstract {

	@SuppressWarnings("unused")
	private String key = "insertActivityGuided";

	public InsertActivityGuided(CentralRegistry registry) {
		super(registry);
	}

	/**
	 * @see TreeMutationAbstract#mutate(Node)
	 */
	public NAryTree mutate(NAryTree tree, int node) {
		assert tree.isConsistent();
		
		if(true){
			noChange();
			return tree;
		}

		//Insert an activity indicated by logmoves

		int mostLM = 0;
		int mostLMNode = -1;
		XEventClass eventclass = null;

		//1. loop through all nodes, searching for a log move
		int endOfSubtree = tree.getNext(node);
		@SuppressWarnings("unused")
		BehaviorCounter b = registry.getFitness(tree).behaviorCounter;
		for (int i = node; i < endOfSubtree; i++) {

			//FIXME the current behavior counter does not track log moves
			/*-
			if (!b.getLogMoveCount().isEmpty()) {
				// accessing keys/values through an iterator:
				for (TObjectIntIterator<XEventClass> it = b.getLogMoveCount().iterator(); it.hasNext();) {
					it.advance();
					//Decide whether we remember this node if # is bigger, different clazz and well, chance
					if (it.value() > mostLM) {
						if (registry.getRandom().nextBoolean() || mostLM == 0) {
							//							System.out.println("Updated MostLM to " + it.value() + "("+it.key()+")");
							mostLMNode = i;
							eventclass = it.key();
							mostLM = it.value();
						}
					}
				}
			}/**/
		}

		//2. And add that log move activity next to it (e.g. in a sequence on the same level)
		if (mostLM > 0) {
			/*-
			if (parent == NAryTree.NONE) {
				parent = new Node(Type.SEQ, mostLMNode, new Node(eventclass));
			} else {
				//If the mostLMNode is executed more often than half the log moves of the new activity, then we will not improve so add a XOR(tau, act) :)
				Node newLeaf = new Node(eventclass);
				if (mostLMNode.getBehavior().getBehavedAsL() > 2 * mostLM) {
					newLeaf = new Node(Type.XOR, newLeaf, new Node((XEventClass) null));
				}

				parent.replaceChild(mostLMNode, new Node(Type.SEQ, mostLMNode, newLeaf));
			}/**/

			//1. first copy the mostLMNode to another tree
			NAryTree newTree = new NAryTreeImpl(new int[] {}, new short[] {}, new int[] {});
			newTree.add(tree, mostLMNode, 0, 0);
			assert newTree.isConsistent();

			//2. add a sequence above
			NAryTree seqTree = new NAryTreeImpl(new int[] { NAryTree.NONE }, new short[] { NAryTree.SEQ },
					new int[] { NAryTree.NONE });
			assert seqTree.isConsistent();
			newTree.add(seqTree, 0, 0, 0);
			assert newTree.isConsistent();

			//3. fill in the right part of the sequence
			NAryTree newSubtree;

			//TODO behaviour counter does not keep the correct information, therefore we currently abuse the move count but this should be corrected
			if (registry.getFitness(tree).behaviorCounter.getMoveCount()[mostLMNode] > 2 * mostLM) {
				/*
				 * If the mostLMNode is executed more often than half the log
				 * moves of the new activity, then we will not improve by adding
				 * a forced activity so add a XOR(act,tau) :)
				 */
				newSubtree = new NAryTreeImpl(new int[] { 2, 2, 2 }, new short[] { NAryTree.XOR,
						registry.getEventClassID(eventclass), NAryTree.TAU }, new int[] { NAryTree.NONE, 0, 0 });
			} else {
				//otherwise just add a leaf
				newSubtree = new NAryTreeImpl(new int[] { 1 }, new short[] { registry.getEventClassID(eventclass) },
						new int[] { NAryTree.NONE });
			}
			//Add the new subtree to the right in the sequence with the original node
			newTree.add(newSubtree, 0, 0, 1);
			assert newTree.isConsistent();

			//4. now replace the mostLMNode with the sequence of mostLMNode and the LM (possibly in a XOR with tau) 
			NAryTree newerTree = tree.replace(tree.getParent(mostLMNode), newTree, 0);
			assert newerTree.isConsistent();
			return newerTree;
		}

		noChange();
		return tree;
	}
}
