package org.processmining.plugins.etm.parameters;

import java.util.List;

import org.processmining.plugins.etm.CentralRegistryConfigurable;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;

/**
 * This parameter object is special for mining process trees with
 * configurations, and is thus suited for multiple event logs.
 * 
 * @author jbuijs
 * 
 */
public class ETMParamConfigurable extends ETMParam {

	protected CentralRegistryConfigurable centralRegistry;

	//TODO apply/check all java bean properties
	//TODO implement null constructor

	public ETMParamConfigurable(CentralRegistryConfigurable registry, TreeFitnessAbstract fitnessEvaluator,
			List<EvolutionaryOperator<NAryTree>> evolutionaryOperators, int populationSize, int eliteCount) {
		super(registry, fitnessEvaluator, evolutionaryOperators, populationSize, eliteCount);
		this.centralRegistry = registry;
	}

	public CentralRegistryConfigurable getCentralRegistry() {
		return centralRegistry;
	}

	public void setCentralRegistry(CentralRegistryConfigurable registry) {
		this.centralRegistry = registry;
	}

}
