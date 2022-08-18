package org.processmining.plugins.etm.ui.plugins;

import java.util.List;
import java.util.Random;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.model.narytree.conversion.ProcessTreeToNAryTree;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.ui.wizards.ETMParameterWizard;
import org.processmining.processtree.ProcessTree;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.TerminationCondition;

@Plugin(name = "Mine a Process Tree with ETMd", parameterLabels = { "Event log", "ETM Parameters", "Seed Trees" }, returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, userAccessible = true, help = "Mine a Process Tree with the ETM", handlesCancel = true, keywords = {
		"ETM", "ETMd", "Process Tree", "Evolutionary", "Evolutionary Tree Miner", "Genetic", "Genetic Miner" })
//FIXME check all class contents
public class ETMPlugin {

	@PluginVariant(variantLabel = "Mine a Process Tree with ETMd", requiredParameterLabels = { 0 })
	@UITopiaVariant(uiLabel = "Mine a Process Tree with ETMd", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public ProcessTree withoutSeed(final UIPluginContext context, XLog eventlog) {
		return withSeed(context, eventlog);
	}

	@PluginVariant(variantLabel = "Mine a Configured Process Tree with ETMc", requiredParameterLabels = { 0 })
	@UITopiaVariant(uiLabel = "Mine Configured Process Tree with ETMc", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public ProcessTree mineConfigurable(final UIPluginContext context, XLog... logs) {

		ETMParam params = ETMParameterWizard.getExistingParamObject(context, ETMParam.class);

		if (params == null) {
			//CentralRegistryConfigurable cReg = new CentralRegistryConfigurable(context, XLogInfoImpl.NAME_CLASSIFIER, ETMParamAbstract.createRNG(), logs);
			params = ETMParamFactory.buildStandardParamConfigurable(context, logs);
		}

		params = (ETMParam) ETMParameterWizard.apply(context, params);

		return withSeedParams(context, params.getCentralRegistry().getLog(), params);

	}

	@PluginVariant(variantLabel = "Mine a Configured Process Tree with ETMc", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(uiLabel = "Mine Configured Process Tree with ETMc", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public ProcessTree mineConfigurableWithSeed(final UIPluginContext context, ProcessTree processTree, XLog... logs) {
		ETMParam params = ETMParameterWizard.getExistingParamObject(context, ETMParam.class);

		if (params == null) {
			//CentralRegistryConfigurable cReg = new CentralRegistryConfigurable(context, XLogInfoImpl.NAME_CLASSIFIER, ETMParamAbstract.createRNG(), logs);
			params = ETMParamFactory.buildStandardParamConfigurable(context, logs);
		}

		NAryTree[] seed = new NAryTree[1];
		ProcessTreeToNAryTree convertor = new ProcessTreeToNAryTree(params.getCentralRegistry().getEventClasses());

		seed[0] = convertor.convert(processTree);
		params.setSeed(seed);

		params = (ETMParam) ETMParameterWizard.apply(context, params);

		return withSeedParams(context, params.getCentralRegistry().getLog(), params);

	}

	/*-
	@PluginVariant(variantLabel = "Mine a Process Tree with ETMd", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(
			uiLabel = "Mine a Process Tree using the ETM algorithm",
				affiliation = "Eindhoven University of Technology",
				author = "J.C.A.M.Buijs",
				email = "j.c.a.m.buijs@tue.nl",
				pack = "EvolutionaryTreeMiner")
	public ProcessTree withoutParameters(final PluginContext context, XLog eventlog, ETMParam params) {
		return withSeedParams(context, eventlog, params);
	}/**/

	@PluginVariant(variantLabel = "Mine a Process Tree with ETMr", requiredParameterLabels = { 0, 1 })
	@UITopiaVariant(uiLabel = "Mine a Process Tree with ETMr", affiliation = "Eindhoven University of Technology", author = "J.C.A.M.Buijs", email = "j.c.a.m.buijs@tue.nl", pack = "EvolutionaryTreeMiner")
	public ProcessTree withSeed(final UIPluginContext context, XLog eventlog, ProcessTree... trees) {

		Progress progress = context.getProgress();

		ETMParam params = ETMParameterWizard.getExistingParamObject(context, ETMParam.class);

		if (params == null) {
			CentralRegistry registry = new CentralRegistry(context, eventlog, new Random());
			params = new ETMParam(registry, null, null, -1, -1);
		}

		if (trees != null && trees.length > 0) {
			NAryTree[] seed = new NAryTree[trees.length];
			ProcessTreeToNAryTree convertor = new ProcessTreeToNAryTree(params.getCentralRegistry().getEventClasses());

			for (int i = 0; i < trees.length; i++) {
				seed[i] = convertor.convert(trees[i]);
			}
			params.setSeed(seed);
		}

		params = (ETMParam) ETMParameterWizard.apply(context, params);

		return withSeedParams(context, eventlog, params, trees);
	}

	@PluginVariant(variantLabel = "Mine a Process Tree with ETMd", requiredParameterLabels = { 0, 1 })
	public ProcessTree withoutSeedParams(final PluginContext context, XLog eventlog, ETMParam params) {
		return withSeedParams(context, eventlog, params);
	}

	@PluginVariant(variantLabel = "Mine a Process Tree with ETMr", requiredParameterLabels = { 0, 1 })
	public ProcessTree withSeedParams(final PluginContext context, XLog eventlog, ETMParam params, ProcessTree... trees) {
		Progress progress = context.getProgress();

		//If ga is null then the user cancelled the wizard so stop
		if (params == null) {
			context.getFutureResult(0).cancel(true);
			System.out.println("Parameter object is NULL, stopping.");
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

		ETM etm = new ETM(params);
		System.out.println("Starting the ETM");
		etm.run();

		List<TerminationCondition> stopped = etm.getSatisfiedTerminationConditions();
		for (TerminationCondition cond : stopped) {
			System.out.println(cond.toString());
		}

		NAryTree tree = etm.getResult();

		System.out.println("Tree: " + TreeUtils.toString(tree, params.getCentralRegistry().getEventClasses()));
		System.out.println("Fitness: " + params.getCentralRegistry().getFitness(tree).fitnessValues);

		/*-
		NAryTree normalizedTree = TreeUtils.normalize(tree);

		System.out.println("Normalized tree: "
				+ TreeUtils.toString(normalizedTree, realParams.getCentralRegistry().getEventClasses()));
		realParams.getFitnessEvaluator().getFitness(normalizedTree, null);
		System.out.println("Fitness: " + realParams.getCentralRegistry().getFitness(normalizedTree).fitnessValues);
		/**/

		/*-* /
		context.addConnection(new NAryTreeToXEventClassesConnection(tree, realParams.getCentralRegistry()
				.getEventClasses()));/**/

		System.out.println("Discovered tree: "
				+ TreeUtils.toString(tree, params.getCentralRegistry().getEventClasses()));

		return NAryTreeToProcessTree.convert(params.getCentralRegistry().getEventClasses(), tree,
				"Process tree discovered by the ETM algorithm");

		//		return NAryTreeToProcessTree.convertWithConfiguration(params.getCentralRegistry().getEventClasses(), tree,
		//				"Process tree discovered by the ETM algorithm");
	}

}
