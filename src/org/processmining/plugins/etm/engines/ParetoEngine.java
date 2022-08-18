package org.processmining.plugins.etm.engines;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.collections15.map.LRUMap;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.NAryTreeImpl;
import org.processmining.plugins.etm.model.serialization.ParetoFrontExport;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.parameters.ETMParamPareto;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvaluatedCandidate;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionUtils;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.PopulationData;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;

/**
 * A specific engine that does not evolve a single {@link NAryTree} but a whole
 * group of Trees in a {@link ParetoFront}.
 * 
 * @author jbuijs
 * 
 */
// FIXME check all class contents
// FIXME Test Class thoroughly
public class ParetoEngine extends LoggingEvolutionEngine<ParetoFront> {

	//FIXME use pragmatic approach to allow more detailed logging by (specialized) loggers, not via populationData etc.

	private ParetoFront paretoFront;

	/*-
	private ParetoFitnessEvaluator paretoFitnessEvaluator;
	
	private Set<TreeFitnessInfo> ignoredDimensions;
	
	private CentralRegistry registry;
	/**/

	private ETMParamPareto params;

	/**
	 * We keep track of the trees, and their fitness details, that we put in our
	 * Pareto front at least once. We do this for two reasons: 1. if we
	 * encounter this tree again then we do not need to add it to our Pareto
	 * front and we can keep mutating it 2. we do not need to calculate the
	 * fitness again
	 */
	//private HashMap<String, Map<String, Double>> treeParetoFrontCache = new HashMap<String, Map<String, Double>>();
	//private LRUMap<String, Map<String, Double>> treeParetoFrontCache = new LRUMap<String, Map<String, Double>>(100);

	/**
	 * We ourselves also keep track of all the trees that did not make it into
	 * the Pareto front so, if we encounter them again, we do not need to
	 * recalculate the fitness values.
	 */
	//TODO useful? Pareto dominance is calculated efficiently, memory is a problem.
	private LRUMap<NAryTree, Boolean> rejectedTreesCache = new LRUMap<NAryTree, Boolean>(400);

	/**
	 * A boolean indicating whether all limits that were set are correctly apply
	 */
	private boolean updatedAllLimits = false;

	public ParetoEngine(ETMParamPareto params) {
		super(params);
		this.params = params;
	}

	/*-* /
	public ParetoEngine(CentralRegistry registry, CandidateFactory<NAryTree> candidateFactory,
			EvolutionaryOperator<NAryTree> evolutionScheme, TreeFitnessAbstract dimensionEvaluator,
			ParetoFitnessEvaluator paretoFitnessEvaluator, SelectionStrategy<? super NAryTree> selectionStrategy,
			Random rng) {
		super(candidateFactory, evolutionScheme, dimensionEvaluator, selectionStrategy, rng);

		this.registry = registry;

		//By default, we don't ignore any dimension, we get that from the parameters that should call the setter method
		ignoredDimensions = new HashSet<TreeFitnessInfo>();

		if (paretoFitnessEvaluator == null) {
			throw new IllegalArgumentException("Pareto Fitness Evaluator can not be null");
		}

		this.paretoFitnessEvaluator = paretoFitnessEvaluator;
	}/**/

	/*-
	public ParetoEngine(CandidateFactory<Tree> candidateFactory, EvolutionaryOperator<Tree> evolutionScheme,
			FitnessEvaluator<? super Tree> fitnessEvaluator, SelectionStrategy<? super Tree> selectionStrategy,
			Random rng) {
		//this(candidateFactory, evolutionScheme, fitnessEvaluator, new ParetoFitnessEvaluator(), selectionStrategy, rng);

		super(candidateFactory, evolutionScheme, fitnessEvaluator, selectionStrategy, rng);

		if (!(fitnessEvaluator instanceof ParetoFitnessEvaluator)) {
			throw new IllegalArgumentException(
					"The Pareto engine requires a special Pareto Fitness Evaluator (The ParetoFitnessEvaluator)");
		}

		//Ignore these dimensions when doing Pareto things:
		ignoredDimensions = new HashSet<String>();
	}/**/

	/**
	 * {@inheritDoc}
	 */
	public List<EvaluatedCandidate<NAryTree>> evolvePopulation(int populationSize, int eliteCount,
			Collection<NAryTree> seedCandidates, TerminationCondition... conditions) {
		//TODO find a better way than copying code and adjusting from the abstract class...

		if (eliteCount < 0 || eliteCount >= populationSize) {
			throw new IllegalArgumentException("Elite count must be non-negative and less than population size.");
		}
		if (conditions.length == 0) {
			throw new IllegalArgumentException("At least one TerminationCondition must be specified.");
		}

		satisfiedTerminationConditions = null;
		long startTime = System.currentTimeMillis();

		/*-
		 * START PARETO
		 */

		//Make sure that the registry cache can at least contain 1 population fully
		params.getCentralRegistry().increaseFitnessCache(populationSize);

		List<NAryTree> population = candidateFactory.generateInitialPopulation(populationSize, seedCandidates, rng);

		// Calculate the fitness scores for each member of the initial population.
		List<EvaluatedCandidate<NAryTree>> evaluatedPopulation = evaluatePopulation(population);

		//Use the first result to get the dimensions to use
		List<TreeFitnessInfo> dimensions = ETMParamFactory.extractConsideredDimensions(params);
		dimensions.removeAll(params.getIgnoredDimensions());
		paretoFront = new ParetoFront(params.getCentralRegistry(), params.getParetoFrontMaxSize(), dimensions);
		params.getParetoFitnessEvaluator().setParetoFront(paretoFront);

		updateParetoFrontLimits();

		for (EvaluatedCandidate<NAryTree> candidate : evaluatedPopulation) {
			NAryTree tree = candidate.getCandidate();
			//Not added to the front before
			if (params.getCentralRegistry().isFitnessKnown(tree) && !rejectedTreesCache.containsKey(tree)) {
				paretoFront.consider(tree);
			}
		}

		//Update registry cache to contain minimally the current PF + population size
		params.getCentralRegistry().increaseFitnessCache(paretoFront.size() + populationSize);

		//Now set the overall fitness of each tree in the population, using the current Pareto front
		evaluatedPopulation = reEvaluate(evaluatedPopulation);
		paretoFront.reduceSize();//we need PF fitness to properly reduce the size
		EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, params.getParetoFitnessEvaluator().isNatural());
		PopulationData<NAryTree> data = EvolutionUtils.getPopulationData(evaluatedPopulation, params
				.getParetoFitnessEvaluator().isNatural(), eliteCount, params.getCentralRegistry()
				.getCurrentGeneration(), startTime);
		// Notify observers of the state of the population.
		notifyPopulationChange(data);
		params.getListeners().fireGenerationFinished(paretoFront);

		/*
		 * END PARETO
		 */

		List<TerminationCondition> satisfiedConditions = EvolutionUtils.shouldContinue(data, conditions);
		while (satisfiedConditions == null) {
			evaluatedPopulation = nextEvolutionStep(evaluatedPopulation, eliteCount, rng);
			EvolutionUtils.sortEvaluatedPopulation(evaluatedPopulation, params.getParetoFitnessEvaluator().isNatural());
			data = EvolutionUtils.getPopulationData(evaluatedPopulation,
					params.getParetoFitnessEvaluator().isNatural(), eliteCount, params.getCentralRegistry()
							.getCurrentGeneration(), startTime);
			// Notify observers of the state of the population.
			notifyPopulationChange(data);
			satisfiedConditions = EvolutionUtils.shouldContinue(data, conditions);
		}
		this.satisfiedTerminationConditions = satisfiedConditions;
		return evaluatedPopulation;

	}

	/**
	 * Checks if the pareto front limits are to be applied and tries to apply
	 * them. If they can not all be applied, it tries to apply them in the next
	 * generation.
	 */
	private void updateParetoFrontLimits() {
		//Set/copy upper and lower limits if it is not postponed to a later generation
		if (params.getGenerationWhenLimitsAreApplied() <= params.getCentralRegistry().getCurrentGeneration()
				&& !updatedAllLimits) {
			/*
			 * It could be that we did not apply/update all limits, since it
			 * would result in an empty Pareto front. If this happens, we will
			 * try to apply the limits in the next round properly.
			 */
			boolean updatedAllLimitsThisRound = true;
			for (Entry<TreeFitnessInfo, Double> upperLimit : params.getUpperlimits().entrySet()) {
				updatedAllLimitsThisRound = paretoFront.updateUpperLimit(upperLimit.getKey(), upperLimit.getValue());
				if (!updatedAllLimitsThisRound) {
					params.setGenerationWhenLimitsAreApplied(params.getCentralRegistry().getCurrentGeneration() + 1);
					break;
				}
			}
			if (updatedAllLimitsThisRound) {
				for (Entry<TreeFitnessInfo, Double> lowerLimit : params.getLowerlimits().entrySet()) {
					updatedAllLimitsThisRound = paretoFront
							.updateLowerLimit(lowerLimit.getKey(), lowerLimit.getValue());
					if (!updatedAllLimitsThisRound) {
						params.setGenerationWhenLimitsAreApplied(params.getCentralRegistry().getCurrentGeneration() + 1);
						break;
					}
				}
			}

			if (updatedAllLimitsThisRound) {
				updatedAllLimits = true;
			}
		}
	}

	private List<EvaluatedCandidate<NAryTree>> reEvaluate(List<EvaluatedCandidate<NAryTree>> evaluatedPopulation) {
		//Update population
		ArrayList<EvaluatedCandidate<NAryTree>> updatedList = new ArrayList<EvaluatedCandidate<NAryTree>>();
		List<NAryTree> population = new ArrayList<NAryTree>();
		for (EvaluatedCandidate<NAryTree> candidate : evaluatedPopulation) {
			population.add(candidate.getCandidate());
		}
		for (EvaluatedCandidate<NAryTree> candidate : evaluatedPopulation) {
			updatedList.add(new EvaluatedCandidate<NAryTree>(candidate.getCandidate(), params
					.getParetoFitnessEvaluator().getFitness(candidate.getCandidate(), population)));
		}
		//Update front
		paretoFront.reEvaluateParetoFitness();
		//Return updated population
		return updatedList;
	}

	protected List<EvaluatedCandidate<NAryTree>> nextEvolutionStep(
			List<EvaluatedCandidate<NAryTree>> evaluatedPopulation, int eliteCount, Random rng) {

		params.getCentralRegistry().increaseGeneration();

		System.gc();

		updateParetoFrontLimits();

		/*
		 * Based on implementation in generational evolution engine class:
		 */
		List<NAryTree> population = new ArrayList<NAryTree>(evaluatedPopulation.size());

		/*
		 * Then select candidates that will be operated on to create the evolved
		 * portion of the next generation.
		 */
		population.addAll(selectionStrategy.select(evaluatedPopulation, params.getParetoFitnessEvaluator().isNatural(),
				evaluatedPopulation.size() - eliteCount, rng));
		//popSize, rng));
		// Then evolve the population.
		population = evolutionScheme.apply(population, rng);

		//TODO reconsider approach based on literature study
		/*
		 * After receiving the population, selecting a sub-population for
		 * evolution and applying the evolution, this engine does the following
		 * things differently than other engines. It then first evaluates, on
		 * the new population, the fitness dimensions that need to be considered
		 * in the Pareto Front. It then updates the Pareto Front. Next, it adds
		 * the 'elite' to the population which contains a selection of Pareto
		 * Front members, possibly extended with duplicates form the Pareto
		 * front to reach the number of elite candidates required. (Although
		 * this will introduce duplicates, in the next generation a better set
		 * of 'base' trees will be used to help extend the Pareto front to a
		 * reasonable size.). Finally, it returns the evaluated population.
		 */
		/*
		 * IDEAS: have the front not only contain 'perfect' candidates, select
		 * elite using more advanced fitness (e.g. combi of dominations,
		 * dominance, ...), elite should not contain duplicates.
		 */

		/*
		 * First, evaluate the dimensions needed in the Pareto Front, using the
		 * 'normal' fitnes evaluator provided to the engine
		 */
		List<EvaluatedCandidate<NAryTree>> newEvaluatedPopulation = evaluatePopulation(population);

		/*
		 * Then, update the Pareto front with unseen trees if they are not
		 * dominated by other members in the Pareto front
		 */
		//TODO use considerAll which might have better performance on more advanced fronts...
		for (EvaluatedCandidate<NAryTree> candidate : newEvaluatedPopulation) {
			NAryTree tree = candidate.getCandidate();
			//Not added to the front before
			if (params.getCentralRegistry().isFitnessKnown(tree) && !rejectedTreesCache.containsKey(tree)) {
				paretoFront.consider(tree);
			}
		}
		paretoFront.reduceSize();

		//Update registry cache to contain minimally the current PF + population size
		params.getCentralRegistry().increaseFitnessCache(paretoFront.size() + evaluatedPopulation.size());

		/*
		 * Next, extend the population with a selection of the elite. E.g.
		 * select #eliteCount trees from the Pareto front, possibly including
		 * duplicates if the Pareto front is not big/detailed enough yet.
		 */
		/*
		 * TODO here we need to prevent duplicates to improve diversity, hence
		 * we either need the Pareto front to contain more candidates than just
		 * the best front so far OR we should be able to calculate a Pareto
		 * dominance fitness over the whole current population and then if
		 * pareto front is smaller than elite size fill it up with best
		 * candidates of the population
		 */
		//TODO also add randomness in case the front is larger than the elite size
		List<NAryTree> elite = new ArrayList<NAryTree>();
		Iterator<NAryTree> frontIt = paretoFront.getFront().iterator();
		if (paretoFront.getFront().size() < eliteCount) {
			//We need to duplicate trees from the Pareto front in our elite

			//First, add the complete front
			for (NAryTree tree : paretoFront.getFront()) {
				elite.add(new NAryTreeImpl(tree));
			}

			//Now keep adding additional trees from the front until we reach the desired size
			while (elite.size() < eliteCount) {
				//If we reached the end of the set, re initialize the iterator
				if (!frontIt.hasNext()) {
					frontIt = paretoFront.getFront().iterator();
				}
				//Add the tree to the elite
				elite.add(frontIt.next());
			}
		} else {
			//We need only a selection of the front to be added to the elite
			while (elite.size() < eliteCount) {
				elite.add(frontIt.next());
			}
		}

		for (NAryTree tree : elite) {
			//FIXME is score of 0 correct?
			newEvaluatedPopulation.add(new EvaluatedCandidate<NAryTree>(tree, 0));
		}

		params.getListeners().fireGenerationFinished(paretoFront);

		/*
		 * Now, using the updated Pareto front, calculate the pareto fitness for
		 * all trees in the population
		 */
		return reEvaluate(newEvaluatedPopulation);
	}

	public ParetoFront getParetoFront() {
		return paretoFront;
	}

	public String logResult(List<EvaluatedCandidate<NAryTree>> result) {
		return ParetoFrontExport.exportToString(paretoFront);
	}

}
