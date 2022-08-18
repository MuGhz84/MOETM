package org.processmining.plugins.etm.mutation.mutators;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.BehaviorCounter;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;

/**
 * DISABLED
 */
//FIXME check all class contents
//FIXME UNTESTED code
public class MutateLeafClassGuided extends TreeMutationAbstract {
	private String key = "ChangeLeafClassGuided";

	public MutateLeafClassGuided(CentralRegistry registry) {
		super(registry);
	}

	/**
	 * @see TreeMutationAbstract#mutate(Node)
	 */
	public NAryTree mutate(NAryTree treeOriginal, int node) {
		if(true){
			noChange();
			return treeOriginal;
		}
		
		NAryTree mutatedTree = new NAryTreeImpl(treeOriginal);
		
		assert mutatedTree.isConsistent();

		//Global memory to find the best leaf to change
		int nrLM = 0; //count log moves
		int leafToChange = -1; //the leaf we should change
		XEventClass changeInto = null; //into ...

		@SuppressWarnings("unused")
		BehaviorCounter b = registry.getFitness(mutatedTree).behaviorCounter;
		@SuppressWarnings("unused")
		int i = mutatedTree.isLeaf(0) ? 0 : mutatedTree.getNextLeafFast(0);
		//FIXME behaviour counter does not record the information we need
		/*-
		do {
			if (tree.getType(i) != NAryTree.TAU) {
				TObjectIntMap<XEventClass> LMcounts = b.getLogMoveCount(); //inserted act

				for (XEventClass ec : LMcounts.keySet()) {
					//FIXME might go wrong since LMcounts contains more than the actual 'R's (check with new behC impl)
					if (!registry.getEventClassByID(tree.getType(i)).equals(ec) && LMcounts.get(ec) > nrLM
							&& b.getBehavedAsR() > 0) {
						//Add some randomness in case the most nrLM is not improving the model
						if (registry.getRandom().nextBoolean() || nrLM == 0) {
							//Keep track of what leaf should be changed into what and its 'weight'
							nrLM = LMcounts.get(ec);
							leafToChange = i;
							changeInto = ec;
						}
					}
				}

			}
			i = tree.getNextLeafFast(i);
		} while (i < tree.size());
		/**/

		//Now change the leaf IFF we found one
		if (nrLM > 0) {
			mutatedTree.setType(leafToChange, registry.getEventClassID(changeInto));
			assert mutatedTree.isConsistent();
			didChange(leafToChange, NAryTreeHistory.TypesOfChange.OTHER);
			return mutatedTree;
		}

		noChange();
		return mutatedTree;
	}

	/**
	 * @see TreeMutationAbstract#getKey()
	 */
	public String getKey() {
		return key;
	}

}
