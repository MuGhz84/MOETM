package org.processmining.plugins.etm.termination;

import java.util.Set;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitness;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.PopulationData;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;

public class TerminateAtQDimValue implements TerminationCondition {

	private CentralRegistry registry;
	private Set<Pair<TreeFitnessInfo, Double>> limits;

	/**
	 * Stop the algorithm as soon as the best candidate has the following
	 * minimal quality dimension values
	 * 
	 * @param dateTime
	 */
	public TerminateAtQDimValue(CentralRegistry registry, Set<Pair<TreeFitnessInfo, Double>> limits) {
		this.registry = registry;
		this.limits = limits;
	}

	public boolean shouldTerminate(PopulationData<?> populationData) {
		Object treeObject = populationData.getBestCandidate();
		NAryTree tree = null;

		if (treeObject instanceof NAryTree) {
			tree = (NAryTree) treeObject;
		} else {
			return false;
		}

		TreeFitness fitness = registry.getFitness(tree);

		for (Pair<TreeFitnessInfo, Double> pair : limits) {
			if (fitness.fitnessValues.containsKey(pair.getFirst())) {
				//if is natural bigger values are better so if value < limit then don't stop, or isNatural = false and <= limit
				if (pair.getFirst().isNatural()) {
					if (fitness.fitnessValues.get(pair.getFirst()) < pair.getSecond()) {
						return false; //don't stop
					}
				} else {
					if (fitness.fitnessValues.get(pair.getFirst()) > pair.getSecond()) {
						return false; //don't stop
					}
				}

			}
			//Else, there is no reason to stop
		}

		return true;
	}
}
