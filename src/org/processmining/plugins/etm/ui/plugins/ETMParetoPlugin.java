package org.processmining.plugins.etm.ui.plugins;

import java.util.List;
import java.util.Random;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.ETMPareto;
import org.processmining.plugins.etm.fitness.metrics.ParetoFitnessEvaluator;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.parameters.ETMParamPareto;
import org.processmining.plugins.etm.ui.wizards.ETMParameterWizard;
import org.processmining.processtree.ProcessTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.TargetFitness;

/*-*/
@Plugin(
		name = "Mine Pareto front with ETMd",
			parameterLabels = { "Event log", "ETM Parameters", "Seed Process Trees" },
			returnLabels = { "Process Tree" },
			returnTypes = { ParetoFront.class },
			userAccessible = true,
			help = "Mine a Pareto Front of Process Trees using the ETM algorithm",
			handlesCancel = true,
			keywords = {"ETM", "ETMd", "Process Tree", "Evolutionary", "Evolutionary Tree Miner", "Genetic", "Genetic Miner", "Pareto", "Pareto front"},
			categories = {PluginCategory.Discovery})
/**/
// FIXME check all class contents
public class ETMParetoPlugin {

	@PluginVariant(variantLabel = "ETM Pareto without seed", requiredParameterLabels = { 0 })
	@UITopiaVariant(
			uiLabel = "Mine Pareto front with ETMd",
				affiliation = "Eindhoven University of Technology",
				author = "J.C.A.M.Buijs",
				email = "j.c.a.m.buijs@tue.nl",
				pack = "EvolutionaryTreeMiner")
	public static ParetoFront ETMParetoNoSeed(final UIPluginContext context, XLog eventlog) {
		return ETMParetoWithSeed(context, eventlog, null);
	}

	@PluginVariant(variantLabel = "Mine Pareto front with ETMr", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(
			uiLabel = "Mine Pareto front with ETMr",
				affiliation = "Eindhoven University of Technology",
				author = "J.C.A.M.Buijs",
				email = "j.c.a.m.buijs@tue.nl",
				pack = "EvolutionaryTreeMiner")
	public static ParetoFront ETMParetoWithSeed(final UIPluginContext context, XLog eventlog, ProcessTree... trees) {
		Progress progress = context.getProgress();

		ETMParamPareto params = ETMParameterWizard.getExistingParamObject(context, ETMParamPareto.class);

		if (params == null) {
			CentralRegistry registry = new CentralRegistry(context, eventlog, XLogInfoImpl.NAME_CLASSIFIER, new Random());
			params = new ETMParamPareto(registry, null, new ParetoFitnessEvaluator(registry), null, -1, -1);
			ETMParamFactory.addOrReplaceTerminationCondition(params, new TargetFitness(-1, false));
		}

		if (trees != null && trees.length > 0) {
			NAryTree[] seed = new NAryTree[trees.length];
			ProcessTreeToNAryTree convertor = new ProcessTreeToNAryTree(params.getCentralRegistry().getEventClasses());

			for (int i = 0; i < trees.length; i++) {
				seed[i] = convertor.convert(trees[i]);
			}
			params.setSeed(seed);
		}

		params = (ETMParamPareto) ETMParameterWizard.apply(context, params);

		//If ga is null then the user cancelled the wizard so stop
		if (params == null) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		//ELSE continue of course...

		progress.inc();
		try {
			PackageManager.getInstance().findOrInstallPackages("LpSolve");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//FIXME enabled detailed logging for now
		//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); //TODO make parameter?
		//realParams.setPath("E://ETMlocalLogs//" + df.format(new Date())); //TODO make parameter?
		//realParams.setLogModulo(1000);

		ETMPareto etm = new ETMPareto(params);
		etm.run();

		ParetoFront front = etm.getResult();

		System.out.println(front);

		List<TerminationCondition> stopped = etm.getSatisfiedTerminationConditions();
		for (TerminationCondition cond : stopped) {
			System.out.println(cond.toString());
		}

		return front;

		/*-
		final Progress progress = context.getProgress();

		context.log("Starting Genetic Algorithm...");
		progress.setMinimum(0);
		progress.setIndeterminate(true); //set indeterminate for now...

		ETMPareto etm = new ETMPareto(buildDefaultParetoParams(context, eventlog));
		etm.run();
		ParetoFront front = etm.getResult();

		System.out.println(front);

		List<TerminationCondition> stopped = etm.getSatisfiedTerminationConditions();
		for (TerminationCondition cond : stopped) {
			System.out.println(cond.toString());
		}

		return front;
		/**/
	}

	@Plugin(
			name = "Mine Pareto front with ETMc",
				parameterLabels = { "Event Logs" },
				returnLabels = { "Running ETMd Live Pareto instance" },
				returnTypes = { String.class },
				userAccessible = true,
				help = "Mine Pareto front with ETMc",
				handlesCancel = true)
	@UITopiaVariant(
			uiLabel = "Mine Pareto front with ETMc",
				affiliation = "Eindhoven University of Technology",
				author = "J.C.A.M.Buijs",
				email = "j.c.a.m.buijs@tue.nl",
				pack = "EvolutionaryTreeMiner")
	public static String etmConfigPareto(final UIPluginContext context, XLog... eventlog) {
		return "CURRENTLY UNDER DEVELOPMENT, sorry. Joos Buijs"; 
	}
	
	/*-
	public static ETMParamPareto buildDefaultParetoParams(PluginContext context, XLog eventlog, NAryTree... seed) {
		int maxGen = -1;//1000;
		int popSize = 200;
		int eliteSize = 10;
		int nrRandomTrees = 5;
		double crossOverChance = 0.1;
		double maxF = -1;
		double maxFTime = -1;

		double targetFitness = -1;
		double FrWeight = 10;
		double PeWeight = 1;
		double GeWeight = -1;
		double SdWeight = 1;

		double chanceOfRandomMutation = .25;

		ETMParamPareto etmParam = ETMParamFactory.buildETMParamPareto(eventlog, context, popSize, eliteSize,
				nrRandomTrees, crossOverChance, chanceOfRandomMutation, maxGen, targetFitness, FrWeight, maxF,
				maxFTime, PeWeight, GeWeight, SdWeight, seed, 0.0001);

		etmParam.addIgnoredDimension(OverallFitness.info);
		etmParam.updateLowerLimit(SimplicityUselessNodes.info, .8);
		etmParam.updateLowerLimit(SimplicityMixed.info, .8);

		/*-
		//Update edit distance metric for absolute one!
		TreeFitnessAbstract eval = etmParam.getFitnessEvaluator();
		
		if(eval instanceof MultiThreadedFitnessEvaluator){
			MultiThreadedFitnessEvaluator mtEval = (MultiThreadedFitnessEvaluator) eval;
			for(TreeFitnessAbstract evalInd : mtEval.getEvaluators()){
				if(evalInd instanceof OverallFitness){
					OverallFitness evalIndOf = (OverallFitness) evalInd;
					for( Entry<TreeFitnessAbstract, Double> entry : evalIndOf.getEvaluators().entrySet()){
						if(entry.getKey() instanceof EditDistanceWrapperRTEDRelative){
						}
						//Ah, man, this is stupid... fixing things, there should be a better way...
					}
				}
			}
		}/**/

		//TODO remove debug code
		/*
		 * !Simplicity size test!
		 */
		/*-
		TreeFitnessAbstract eval = etmParam.getFitnessEvaluator();
		if (eval instanceof OverallFitness) {
			OverallFitness of = (OverallFitness) eval;
			LinkedHashMap<TreeFitnessAbstract, Double> newOfList = new LinkedHashMap<TreeFitnessAbstract, Double>();
			for (Entry<TreeFitnessAbstract, Double> entry : of.getEvaluators().entrySet()) {
				if (!(entry instanceof SimplicityDuplMissingAct)) {
					newOfList.put(entry.getKey(), entry.getValue());
				}
			}
			newOfList.put(new SimplicityTreeSize(4), 1000d);

			OverallFitness newOf = new OverallFitness(newOfList);
			etmParam.setFitnessEvaluator(newOf);
		}/*-* /

		//TODO fix in GUI, not hardcore
		//Stopping at a certain target fitness makes no sense for a Pareto front
		Iterator<TerminationCondition> it = etmParam.getTerminationConditions().iterator();
		while (it.hasNext()) {
			if (it.next() instanceof TargetFitness) {
				it.remove();
			}
		}

		etmParam.addTerminationConditionExternal();

		try {
			PackageManager.getInstance().findOrInstallPackages("LpSolve");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Construct and set the logging path
		long expStart = System.currentTimeMillis();
		DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd_HH_mm");
		String date = dateFormat.format(new Date(expStart));
		//FIXME fixed string!
		String path = "E:\\ETMlocalLogs\\" + date;
		//		String path = null;
		etmParam.setPath(path);

		return etmParam;
	}/**/
}
