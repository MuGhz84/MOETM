package org.processmining.plugins.etm.engines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvaluatedCandidate;

/**
 * Specific implementation of an evolution engine for {@link NAryTree}s. Adds
 * functionality such as logging whole populations for analysis.
 * 
 * @author jbuijs
 * 
 */
public class TreeEvolutionEngine extends LoggingEvolutionEngine<NAryTree> {

	public TreeEvolutionEngine(ETMParam param) {
		super(param);
	}

	/*-
	private TreeEvolutionEngine(CandidateFactory<NAryTree> candidateFactory,
			EvolutionaryOperator<NAryTree> evolutionScheme, FitnessEvaluator<? super NAryTree> fitnessEvaluator,
			SelectionStrategy<? super NAryTree> selectionStrategy, Random rng) {
		super(candidateFactory, evolutionScheme, fitnessEvaluator, selectionStrategy, rng);
	}/**/

	/**
	 * The ETM parameter object to get certain parameter settings from
	 */
	//private ETMParam params;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<EvaluatedCandidate<NAryTree>> nextEvolutionStep(
			List<EvaluatedCandidate<NAryTree>> evaluatedPopulation, int eliteCount, Random rng) {
		centralRegistry.increaseGeneration();

		System.gc();

		//Copy, paste and adjust from generational evolution engine.
		List<NAryTree> population = new ArrayList<NAryTree>(evaluatedPopulation.size());

		// First perform any elitist selection.
		List<NAryTree> elite = new ArrayList<NAryTree>(eliteCount);
		Iterator<EvaluatedCandidate<NAryTree>> iterator = evaluatedPopulation.iterator();
		while (elite.size() < eliteCount && iterator.hasNext()) {
			NAryTree candidate = iterator.next().getCandidate();
			//Prevent exact duplicate elite trees...
			if (!elite.contains(candidate)) {
//				System.out.println(centralRegistry.getFitness(candidate).getConfidenceLevel());
				//Deep clone the elite since somewhere it might be that trees are still touched... :(
				elite.add(new NAryTreeImpl(candidate));
			}
		}

		/*
		 * It could be that there are more duplicates than
		 * populationsize-eliteCount, e.g. we tried all candidates but could not
		 * fill the elite with enough candidate
		 */
		if (elite.size() < eliteCount) {
			//If so, restart and add the best candidates until we have enough
			iterator = evaluatedPopulation.iterator();
			while (elite.size() < eliteCount) {
				elite.add(iterator.next().getCandidate());
			}
		}

		// Then select candidates that will be operated on to create the evolved
		// portion of the next generation.
		population.addAll(selectionStrategy.select(evaluatedPopulation, fitnessEvaluator.isNatural(),
				evaluatedPopulation.size() - eliteCount, rng));
		// Then evolve the population.
		population = evolutionScheme.apply(population, rng);
		// When the evolution is finished, add the elite to the population.

		population.addAll(elite);
//		for (int i = 0; i < elite.size(); i++) {
//			if (centralRegistry.getFitness(elite.get(i))
//					.getConfidenceLevel() >= centralRegistry.CONFIDENCE_LEVEL_LIMIT) {
//				centralRegistry.restartFitness(elite.get(i));
//			}
//		}
//	
		List<EvaluatedCandidate<NAryTree>> newEvaluatedPopulation = evaluatePopulation(population);
		if (params != null && params.getListeners() != null & newEvaluatedPopulation.get(0) != null) {
			params.getListeners().fireGenerationFinished(newEvaluatedPopulation.get(0).getCandidate());
		}
//		Iterator<Trace> it = centralRegistry.getaStarAlgorithm().traceIterator();
//		while (it.hasNext()) {
//			final Trace originalTrace = it.next();
//			System.out.println("**************");
//			System.out.println("Trace: " + originalTrace);
//			System.out.println("New candidate: " + elite.get(0));
//			System.out.println("REP alignment: " + TreeRecord.getHistory(centralRegistry.getFitness(elite.get(0)).behaviorCounter.getAlignment(originalTrace)));
//
//			System.out.println("**************");
//		}
		return newEvaluatedPopulation;
	}

	/**
	 * Builds a string that described the whole provided result such that it can
	 * be logged
	 */
	public String logResult(List<EvaluatedCandidate<NAryTree>> result) {
		StringBuilder str = new StringBuilder();

		for (EvaluatedCandidate<NAryTree> cand : result) {
			NAryTree tree = cand.getCandidate();

			String detailedFitness = "";
			if (centralRegistry != null && centralRegistry.isFitnessKnown(tree)) {
				detailedFitness = centralRegistry.getFitness(tree).toString();
			}

			//Log the fitnessValue tree detailedFitness
			str.append(String.format("f: %2.10f  %s  %s \r\n", cand.getFitness(),
					TreeUtils.toString(tree, centralRegistry.getEventClasses()), detailedFitness));
		}

		return str.toString();
	}
}
