package org.processmining.plugins.etm.ui.plugins;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.etm.CentralRegistryConfigurable;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.factory.TreeFactoryCoordinator;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract;
import org.processmining.plugins.etm.fitness.metrics.ConfigurationFitness;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.fitness.metrics.Generalization;
import org.processmining.plugins.etm.fitness.metrics.OverallFitness;
import org.processmining.plugins.etm.fitness.metrics.PrecisionEscEdges;
import org.processmining.plugins.etm.fitness.metrics.SimplicityUselessNodes;
import org.processmining.plugins.etm.logging.EvolutionLogger;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.mutation.GuidedTreeMutationCoordinator;
import org.processmining.plugins.etm.mutation.TreeCrossover;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;
import org.processmining.plugins.etm.mutation.TreeMutationCoordinator;
import org.processmining.plugins.etm.mutation.mutators.AddNodeRandom;
import org.processmining.plugins.etm.mutation.mutators.ConfigurationMutator;
import org.processmining.plugins.etm.mutation.mutators.MutateSingleNodeRandom;
import org.processmining.plugins.etm.mutation.mutators.NormalizationMutation;
import org.processmining.plugins.etm.mutation.mutators.RemoveSubtreeRandom;
import org.processmining.plugins.etm.mutation.mutators.RemoveUselessNode;
import org.processmining.plugins.etm.mutation.mutators.ReplaceTreeMutation;
import org.processmining.plugins.etm.mutation.mutators.ShuffleCluster;
import org.processmining.plugins.etm.parameters.ETMParamConfigurable;
import org.processmining.plugins.etm.termination.ProMCancelTerminationCondition;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.configuration.controlflow.ControlFlowConfiguration;
import org.processmining.processtree.configuration.controlflow.ControlFlowConfigurationArray;
import org.processmining.processtree.configuration.controlflow.impl.ControlFlowConfigurationArrayImpl;
import org.processmining.processtree.connections.ControlFlowConfigurationArrayToProcessTreeConnection;
import org.uncommons.maths.random.Probability;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.GenerationCount;

/*-* /
 @Plugin(
 name = "Mine Configured Process Tree with ETMc",
 parameterLabels = { "Event log" },
 returnLabels = { "Process Tree", "Configuration Array" },
 //returnTypes = { ProcessTree.class, ConfigurationArray.class },
 returnTypes = { ProcessTree.class, Configuration.class },
 userAccessible = true,
 help = "Mine Configured Process Tree with ETMc",
 handlesCancel = true)/**/
public class ETMConfigurableTreesPlugin {

	//FIXME DELETE this class and 'move' code to the other 3 plugin classes and implement plug-in facades
	//FIXME make a version that re-uses the GUI part for the normal parameter object...
	
	

	/*-
	@PluginVariant(variantLabel = "Fixed Parameter object", requiredParameterLabels = { 0 })
	@UITopiaVariant(
			uiLabel = "Mine Configured Process Tree with ETMc",
				affiliation = "Eindhoven University of Technology",
				author = "J.C.A.M.Buijs",
				email = "j.c.a.m.buijs@tue.nl",
				pack = "EvolutionaryTreeMiner")
				/**/
	public static Object[] fixedParams(final PluginContext context, XLog... logs) {
		System.out.println("CLASSPATH: " + System.getProperty("java.class.path"));

		final Progress progress = context.getProgress();

		context.log("Starting Genetic Algorithm...");
		progress.setMinimum(0);
		progress.setIndeterminate(true); //set indeterminate for now...

		Canceller canceller = new ProMCancelTerminationCondition(context).getCanceller();

		int maxGen = 10;//1000;
		int popSize = 200;
		int eliteSize = 6;
		double crossOverChance = 0.1;

		double chanceOfRandomMutation = .25;

		CentralRegistryConfigurable registry = new CentralRegistryConfigurable(XLogInfoImpl.STANDARD_CLASSIFIER,
				new Random(), logs);

		TreeFitnessAbstract[] evaluators = new TreeFitnessAbstract[logs.length];
		for (int i = 0; i < logs.length; i++) {
			LinkedHashMap<TreeFitnessAbstract, Double> alg = new LinkedHashMap<TreeFitnessAbstract, Double>();

			FitnessReplay fr = new FitnessReplay(registry.getRegistry(i), canceller, .5, 10);
			alg.put(fr, 10.);
			alg.put(new PrecisionEscEdges(registry.getRegistry(i)), 1.);
			alg.put(new Generalization(registry.getRegistry(i)), .1);
			//			alg.put(new SimplicityMixed(), 1.);
			alg.put(new SimplicityUselessNodes(), 1.);

			evaluators[i] = new OverallFitness(registry.getRegistry(i), alg);
		}

		TreeFitnessAbstract evaluator = new ConfigurationFitness(registry, .1, false, evaluators);

		//Evolutionary Operators
		ArrayList<EvolutionaryOperator<NAryTree>> evolutionaryOperators = new ArrayList<EvolutionaryOperator<NAryTree>>();
		evolutionaryOperators.add(new TreeCrossover<NAryTree>(1, new Probability(crossOverChance), registry));

		//TODO re-enable smart mutators
		LinkedHashMap<TreeMutationAbstract, Double> smartMutators = new LinkedHashMap<TreeMutationAbstract, Double>();
		/*-*/
		//		smartMutators.put(new InsertActivityGuided(registry), 1.); //Improves F
		//		smartMutators.put(new MutateLeafClassGuided(registry), 1.); //Improves F
		//		smartMutators.put(new MutateOperatorTypeGuided(registry), 1.); //Can improve both F and P
		//		smartMutators.put(new RemoveActivityGuided(registry), 1.); //Improves F and/or P
		/**/

		LinkedHashMap<TreeMutationAbstract, Double> dumbMutators = new LinkedHashMap<TreeMutationAbstract, Double>();
		dumbMutators.put(new AddNodeRandom(registry), 1.);
		dumbMutators.put(new MutateSingleNodeRandom(registry), 1.);
		dumbMutators.put(new RemoveSubtreeRandom(registry), 1.);
		dumbMutators.put(new NormalizationMutation(registry), 1.);
		dumbMutators.put(new ReplaceTreeMutation(registry), 1.);
		dumbMutators.put(new ConfigurationMutator(registry), 1.);
		dumbMutators.put(new RemoveUselessNode(registry), 1.);
		dumbMutators.put(new ShuffleCluster(registry), 1.);
		TreeMutationCoordinator dumbCoordinator = new TreeMutationCoordinator(dumbMutators, false);

		//TODO re-enabled smart mutators
		evolutionaryOperators.add(new GuidedTreeMutationCoordinator(registry, chanceOfRandomMutation, false,
				smartMutators, dumbCoordinator));
		//		evolutionaryOperators.add(dumbCoordinator);

		ETMParamConfigurable etmParam = new ETMParamConfigurable(registry, evaluator, evolutionaryOperators, popSize,
				eliteSize);

		etmParam.setFactory(new TreeFactoryCoordinator(registry));
		etmParam.addTerminationCondition(new GenerationCount(maxGen));
		etmParam.addTerminationConditionProMCancellation(context);
		etmParam.addTerminationConditionTargetFitness(1.0, ConfigurationFitness.info.isNatural());

		EvolutionLogger<NAryTree> obs = new EvolutionLogger<NAryTree>(context, registry, false);
		obs.setProgressLevels(maxGen);
		etmParam.addEvolutionObserver(obs);

		progress.inc();

		try {
			PackageManager.getInstance().findOrInstallPackages("LpSolve");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Construct and set the logging path
		long expStart = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
		String date = dateFormat.format(new Date(expStart));
		String path = "E:\\ETMlocalLogs\\" + date;
		//		String path = null;
		etmParam.setPath(path);

		ETM etm = new ETM(etmParam);
		etm.run();
		NAryTree tree = etm.getResult();

		System.out.println(TreeUtils.toString(tree, registry.getEventClasses()));
		System.out.println("Configurations:");
		for (int i = 0; i < tree.getNumberOfConfigurations(); i++) {
			System.out.println(i + " Conf: " + tree.getConfiguration(i));
		}
		System.out.println(registry.getFitness(tree).toString());
		for (int i = 0; i < registry.getNrLogs(); i++) {
			System.out.println(i + ": " + registry.getRegistry(i).getFitness(tree.applyConfiguration(i)).toString());
		}

		registry.clearFitnessCache();
		System.out.println("Non-cached fitness:");
		evaluator.getFitness(tree, null);
		System.out.println(registry.getFitness(tree).toString());
		for (int i = 0; i < registry.getNrLogs(); i++) {
			System.out.println(i + ": " + registry.getRegistry(i).getFitness(tree.applyConfiguration(i)).toString());
		}

		List<TerminationCondition> stopped = etm.getSatisfiedTerminationConditions();
		for (TerminationCondition cond : stopped) {
			System.out.println(cond.toString());
		}

		CentralRegistryConfigurable reg = etmParam.getCentralRegistry();
		//context.addConnection(new NAryTreeToXEventClassesConnection(tree, reg.getEventClasses()));

		Pair<ProcessTree, ArrayList<ControlFlowConfiguration>> pair = NAryTreeToProcessTree.convertWithConfiguration(
				reg.getEventClasses(), tree, "Process Tree discovered by the ETM algorithm");

		ControlFlowConfigurationArray configurationArray = new ControlFlowConfigurationArrayImpl(pair.getSecond());
		ControlFlowConfigurationArrayToProcessTreeConnection connection = new ControlFlowConfigurationArrayToProcessTreeConnection(
				configurationArray, pair.getFirst());
		context.addConnection(connection);
		return new Object[] { pair.getFirst(), configurationArray };
	}
}
