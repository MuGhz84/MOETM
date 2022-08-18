package org.processmining.plugins.etm.ui.plugins;

import java.util.Random;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.ETMPareto;
import org.processmining.plugins.etm.fitness.metrics.ParetoFitnessEvaluator;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.parameters.ETMParamPareto;
import org.processmining.plugins.etm.ui.wizards.ETMParameterWizard;
import org.processmining.processtree.ProcessTree;

public class ETMParetoLivePlugin {

	@Plugin(name = "Mine Pareto front with ETMd in Live mode", parameterLabels = { "Event Log" }, returnLabels = { "Running ETMd Live Pareto instance" }, returnTypes = { ETMPareto.class }, userAccessible = true, help = "Mine Pareto front with ETMd in Live mode", handlesCancel = true, keywords = {
			"ETM", "ETMd", "Process Tree", "Evolutionary", "Evolutionary Tree Miner", "Genetic", "Genetic Miner",
			"Pareto", "Pareto front", "Live" }, categories = { PluginCategory.Discovery })
	@UITopiaVariant(uiLabel = "Mine Pareto front with ETMd in Live mode", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public static ETMPareto etmParetoLiveNoSeed(final UIPluginContext context, XLog eventlog) {
		return etmParetoLiveWithSeed(context, eventlog);
	}

	@Plugin(name = "Mine Pareto front with ETMr in Live mode", parameterLabels = { "Event Log", "Seed trees" }, returnLabels = { "Running ETMr Live Pareto instance" }, returnTypes = { ETMPareto.class }, userAccessible = true, help = "ETM Pareto Live", handlesCancel = true, keywords = {
			"ETM", "ETMd", "Process Tree", "Evolutionary", "Evolutionary Tree Miner", "Genetic", "Genetic Miner",
			"Pareto", "Pareto front", "Live" }, categories = { PluginCategory.Discovery })
	@UITopiaVariant(uiLabel = "Mine Pareto front with ETMr in Live mode", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public static ETMPareto etmParetoLiveWithSeed(final UIPluginContext context, XLog eventlog, ProcessTree... trees) {
		Progress progress = context.getProgress();

		ETMParamPareto params = ETMParameterWizard.getExistingParamObject(context, ETMParamPareto.class);

		if (params == null) {
			CentralRegistry registry = new CentralRegistry(context, eventlog, new Random());
			params = new ETMParamPareto(registry, null, new ParetoFitnessEvaluator(registry), null, -1, -1);
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

		ETMPareto ga = new ETMPareto(params, context.getProvidedObjectManager());

		// Create the thread supplying it with the runnable object
		Thread thread = new Thread(ga);

		// Start the thread
		thread.start();

		context.getProvidedObjectManager().getProvidedObjectLifeCylceListeners().add(ga);

		return ga;
	}

	@Plugin(name = "Mine Pareto front with ETMc in Live mode", parameterLabels = { "Event Logs" }, returnLabels = { "Running ETMd Live Pareto instance" }, returnTypes = { String.class }, userAccessible = true, help = "Mine Pareto front with ETMc in Live mode", handlesCancel = true
	//,keywords = {"ETM", "ETMd", "Process Tree", "Evolutionary", "Evolutionary Tree Miner", "Genetic", "Genetic Miner"},
	//categories = {PluginCategory.Discovery}
	)
	@UITopiaVariant(uiLabel = "Mine Pareto front with ETMc in Live mode", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public static String etmConfigParetoLive(final UIPluginContext context, XLog... eventlog) {
		//FIXME implement!
		return "CURRENTLY UNDER DEVELOPMENT, sorry. Joos Buijs";
	}
}
