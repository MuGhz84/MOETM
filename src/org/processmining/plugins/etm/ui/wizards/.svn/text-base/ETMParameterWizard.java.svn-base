package org.processmining.plugins.etm.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ToolTipManager;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.TextStep;
import org.processmining.plugins.etm.logging.EvolutionLogger;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.mutation.mutators.maikelvaneck.SequenceFactory;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.processmining.plugins.etm.termination.ExternalTerminationCondition;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionObserver;

/**
 * A Wizard and convenience functions for the ETM Parameter objects.
 * 
 * @author jbuijs
 * 
 */
public class ETMParameterWizard {

	/**
	 * Returns an existing ETMParam object in the context or NULL if none such
	 * exists. This should be used to make sure at most 1 parameter object
	 * exists, so that the user gets the same object when he runs an algorithm,
	 * preventing laborious redefinition in the wizard.
	 * 
	 * @param context
	 * @return NULL if no ETMParam exists in the context, otherwise returns the
	 *         (first) instance
	 */
	public static <P extends ETMParamAbstract> P getExistingParamObject(PluginContext context, Class<P> paramClass) {
		/*
		 * FIXME disabled this feature since re-using a parameter instance has
		 * side effects for instance for the registry, fitness cache, (pareto
		 * front?), etc. Need to properly think about what needs to be reset and
		 * implement reset functionality for the parameter class and central
		 * registry at the very least
		 */
		return null;

		/*-
		//ProcreationConfiguration config = new ProcreationConfiguration();
		for (ProvidedObjectID id : context.getProvidedObjectManager().getProvidedObjects()) {
			try {
				Class<?> clazz = context.getProvidedObjectManager().getProvidedObjectType(id);
				if (ETMParamAbstract.class.isAssignableFrom(clazz)) {
					Object o = context.getProvidedObjectManager().getProvidedObjectObject(id, false);
					if (paramClass.isInstance(o)) {
						P p = (P) o;
						return p;
					}
				}
			} catch (ProvidedObjectDeletedException _) {
				// Ignore
			}
		}

		return null;
		/**/
	}

	/**
	 * This method uses a GUI wizard, shown in the context, to allow the user to
	 * inspect and update the parameters for the ETM algorithm.
	 * 
	 * @param context
	 * @param params
	 * @return
	 */
	public static <R> ETMParamAbstract<R> apply(UIPluginContext context, ETMParamAbstract<R> params) {
		//We don't want our precious tooltips to disappear:
		ToolTipManager.sharedInstance().setDismissDelay(50000);
		ToolTipManager.sharedInstance().setInitialDelay(0);

		//Add some of the standard things that we don't need a GUI for
		ETMParamFactory.addOrReplaceTerminationCondition(params,
				ETMParamFactory.constructProMCancelTerminationCondition(context));
		//We should always have this such that an external tool can look for it and stop the ETM
		params.addTerminationCondition(new ExternalTerminationCondition());
		
//		params.setFactory(new TreeFactory(params.getCentralRegistry()));
		params.setFactory(new SequenceFactory(params.getCentralRegistry()));

		//Add standard overallFitness evaluator
		params.setFitnessEvaluator(ETMParamFactory.createStandardOverallFitness(params.getCentralRegistry()));

		//Add a logger to output to the context
		List<EvolutionObserver<NAryTree>> evolutionObservers = new ArrayList<EvolutionObserver<NAryTree>>();
		evolutionObservers.add(new EvolutionLogger<NAryTree>(context, params.getCentralRegistry(), false));
		params.setEvolutionObservers(evolutionObservers);

		//Now set up the wizard sequence:
		@SuppressWarnings("unchecked")
		ListWizard<ETMParamAbstract> wizard = new ListWizard<ETMParamAbstract>(
				new TextStep<ETMParamAbstract>(
						"ETM: Information",
						"<html>In the following wizard screens you can set up the Evolutionary Tree Miner (ETM) algorithm. <BR />"
								//+ "This is a work in progress but should be released soon.<BR />"
								+ "<p>In general, you will follow these steps:<br/>"
								+ "<ol><li>Set up general settings such as what classifier to use and how large the population will be;"
								+ "<li>Choose how to evaluate the process models;"
								+ "<li>Choose how to change the process models during evolution;"
								+ "<li>Set when to stop the algorithm.</ol><br/>"
								+ "</p><p>"
								+ "<i>NOTE: If you are unsure how to set things up, using the predefined settings will work just fine in most cases. <BR />"
								+ "However, quality and performance can be improved if you take the time to read the explanations that appear <br> "
								+ "when you hover the mouse over the text labels. <br>"
								+ " Also, by exploring the different evaluators (and in the future also different mutators) <BR />"
								+ "you will get 'better' process models and/or quicker results.</i>"
								+ "</p><br><p>"
								+ "NOTICE: this tool is in 'open beta', therefore things can go wrong and change drastically between releases. <br/>"
								+ "Furthermore, this wizard is not yet very fault tolerant, e.g. when it expects numbers, it fails when you enter letters."
								+ "</p><p>Should you have any questions, "
								+ "please post them on the ProM forum.</p><br /><br />"
								+ "Enjoy! Joos (J.C.A.M.) Buijs</html>"), new ETMGeneralSettingsStep(params),
				new ETMEvaluatorSettingsStep(context, params), new ETMTerminationSettingsStep(params),
				new ETMOperatorsSettingsStep(params));

		params = ProMWizardDisplay.show(context, wizard, params);

		if (params != null) {
			try {
				context.getProvidedObjectManager().createProvidedObject("Evolutionary Tree Miner Configuration",
						params, ETMParamAbstract.class, context);
				//And make favorite if we're in the GUI
				context.getGlobalContext().getResourceManager().getResourceForInstance(params).setFavorite(true);
			} catch (Exception e) {
				// Well, we did try... and failed :(
			}
		}

		return params;
	}

}
