package org.processmining.plugins.etm.parameters;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;

/**
 * Parameter class for the {@link ETM} algorithm.
 * 
 * @author jbuijs
 * 
 */
//FIXME Test Class thoroughly
public class ETMParam extends ETMParamAbstract<NAryTree> {

	//TODO apply/check all java bean properties

	public ETMParam(CentralRegistry registry, TreeFitnessAbstract fitnessEvaluator,
			List<EvolutionaryOperator<NAryTree>> evolutionaryOperators, int populationSize, int eliteCount) {
		super(registry, fitnessEvaluator, evolutionaryOperators, populationSize, eliteCount);
	}

	public ETMParam(CentralRegistry registry, TreeFitnessAbstract evaluator,
			ArrayList<EvolutionaryOperator<NAryTree>> evolutionaryOperators, int popSize, int eliteSize) {
		super(registry, evaluator, evolutionaryOperators, popSize, eliteSize);
	}

	//Package restricted empty constructor
	ETMParam() {
		super();
	}

}
