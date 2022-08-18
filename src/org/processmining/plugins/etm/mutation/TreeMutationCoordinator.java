package org.processmining.plugins.etm.mutation;

// =============================================================================
// Copyright 2006-2010 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// =============================================================================

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeHistory;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;

/**
 * Mutation operator for the trees of {@link Node}s.
 * 
 * @author jbuijs
 */
// FIXME check all class contents
// FIXME Test Class thoroughly
public class TreeMutationCoordinator implements EvolutionaryOperator<NAryTree> {
	private LinkedHashMap<TreeMutationAbstract, Double> mutators;
	private double totalChance = 0;
	private boolean preventDuplicates;
	protected int locationOfLastChange;
	protected NAryTreeHistory.TypesOfChange typeOfChange;

	/**
	 * The tree mutation operator requires a map of mutators, with weights
	 * assigned to them, to select which one to apply.
	 * 
	 * @param mutators
	 */
	public TreeMutationCoordinator(LinkedHashMap<TreeMutationAbstract, Double> mutators, boolean preventDuplicates) {
		this.mutators = mutators;
		this.preventDuplicates = preventDuplicates;

		calculateTotalChance();
	}

	/**
	 * Applies mutation functions to the tree, depending on the tree's fitness
	 * characteristics and the provided probabilities
	 */
	public List<NAryTree> apply(List<NAryTree> selectedCandidates, Random rng) {
		List<NAryTree> mutatedPopulation = new ArrayList<NAryTree>(selectedCandidates.size());

		for (NAryTree tree : selectedCandidates) {
			assert tree.isConsistent();

			NAryTree mutatedTree = apply(tree, rng);

			/*
			 * If we don't allow duplicates (e.g. first part is not true) then
			 * we continue applying until we find a tree that is not already in
			 * the mutated population
			 */
			while (preventDuplicates && mutatedPopulation.contains(mutatedTree)) {
				mutatedTree = apply(tree, rng);
			}

			assert mutatedTree.isConsistent();

			//And add the mutated tree
			mutatedPopulation.add(mutatedTree);
		}

		return mutatedPopulation;
	}

	/**
	 * Applies mutation functions to the tree, depending on the tree's fitness
	 * characteristics and the provided probabilities
	 */
	public NAryTree apply(NAryTree tree, Random rng) {
		NAryTree mutatedTree;
		TreeMutationAbstract mutator;

		int nrTries = TreeMutationAbstract.MAXTRIES;
		do {
			//Get a mutator
			mutator = getMutatorForChance(rng.nextDouble() * totalChance);
			//Get a mutated tree
			mutatedTree = mutator.mutate(tree);

			assert mutatedTree.isConsistent();
			//Keep trying until one of them actually mutates...
			nrTries--;
		} while ((!mutator.changedAtLastCall()) && nrTries > 0);
		// We have to save the location of the last change
		this.locationOfLastChange = mutator.locationOfLastChange;
		this.typeOfChange = mutator.typeOfChange;
		return mutatedTree;
	}

	public LinkedHashMap<TreeMutationAbstract, Double> getMutators() {
		return mutators;
	}

	private void calculateTotalChance() {
		totalChance = 0;
		for (Double weight : mutators.values()) {
			totalChance += weight;
		}
	}

	private TreeMutationAbstract getMutatorForChance(double chance) {
		if (mutators.size() == 1)
			return mutators.keySet().iterator().next();

		double chanceSoFar = 0;
		for (Map.Entry<TreeMutationAbstract, Double> entry : mutators.entrySet()) {
			chanceSoFar += entry.getValue();
			if (chance <= chanceSoFar) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void addMutator(TreeMutationAbstract mutator, Double chance) {
		mutators.put(mutator, chance);
		calculateTotalChance();
	}

	public boolean isPreventDuplicates() {
		return preventDuplicates;
	}

	public void setPreventDuplicates(boolean preventDuplicates) {
		this.preventDuplicates = preventDuplicates;
	}
}
