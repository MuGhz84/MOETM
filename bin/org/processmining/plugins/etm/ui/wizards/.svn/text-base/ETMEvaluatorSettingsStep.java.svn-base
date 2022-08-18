package org.processmining.plugins.etm.ui.wizards;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract;
import org.processmining.plugins.etm.fitness.TreeFitnessAbstract.TreeFitnessGUISettingsAbstract;
import org.processmining.plugins.etm.fitness.metrics.ConfigurationFitness;
import org.processmining.plugins.etm.fitness.metrics.MultiThreadedFitnessEvaluator;
import org.processmining.plugins.etm.fitness.metrics.OverallFitness.OverallFitnessGUI;
import org.processmining.plugins.etm.fitness.metrics.ParetoFitnessEvaluator.ParetoFitnessEvaluatorGUI;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.processmining.plugins.etm.parameters.ETMParamPareto;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Step 2: fitness settings such as weights and enabling/disabling certain
 * metrics.
 * 
 * @author jbuijs
 * 
 */
class ETMEvaluatorSettingsStep extends ProMHeaderPanel implements ProMWizardStep<ETMParamAbstract> {

	private static final long serialVersionUID = 7984729188459814232L;

	private ETMParamAbstract model;

	private TreeFitnessGUISettingsAbstract fitnessGUI;

	//	private RoundedPanel nrThreads;

	private ProMComboBox nrThreadsCombo;

	private ProMTextField alphaTxtfield;

	public ETMEvaluatorSettingsStep(UIPluginContext context, final ETMParamAbstract param) {
		//call constructor of header panel...
		super(null);

		if (param instanceof ETMParamPareto) {
			fitnessGUI = new ParetoFitnessEvaluatorGUI((ETMParamPareto) param, context);
		} else {
			fitnessGUI = new OverallFitnessGUI(param, context);
		}

		RoundedPanel nrThreads = new RoundedPanel();
		nrThreads.setLayout(new BoxLayout(nrThreads, BoxLayout.LINE_AXIS));

		ArrayList<Integer> nrThreadsChoices = new ArrayList<Integer>();
		for (int t = 1; t <= Runtime.getRuntime().availableProcessors(); t++) {
			nrThreadsChoices.add(t);
		}

		nrThreadsCombo = new ProMComboBox(nrThreadsChoices);
		nrThreadsCombo.setSelectedIndex(nrThreadsChoices.size() - 1);
		JLabel nrThreadsLabel = SlickerFactory.instance().createLabel(
				"Select number of CPU cores to use (1 disables multithreading):");
		nrThreads.add(nrThreadsLabel);
		nrThreads.add(nrThreadsCombo);

		this.add(nrThreads);

		/*
		 * In case of configuration version, add a configuration alpha textfield
		 */
		//		if(param instanceof ETMParamConfigurable || param instanceof ETMParamParetoConfigurable && ){
		/*-
		if (param.getFitnessEvaluator() instanceof ConfigurationFitness) {
			ConfigurationFitness configFitness = (ConfigurationFitness) param.getFitnessEvaluator();

			RoundedPanel configAlpha = new RoundedPanel();
			configAlpha.setLayout(new BoxLayout(configAlpha, BoxLayout.LINE_AXIS));

			JLabel alphaLabel = SlickerFactory.instance().createLabel("Configuration Alpha: ");
			String alphaTooltipText = "";
			alphaLabel.setToolTipText(alphaTooltipText);
			configAlpha.add(alphaLabel);
			alphaTxtfield = new ProMTextField("" + configFitness.getAlpha());
			alphaTxtfield.setToolTipText(alphaTooltipText);

			this.add(configAlpha);
		}
		/**/

		this.add(fitnessGUI);

		fitnessGUI.setMinimumSize(new Dimension(700, 350));
	}

	/**
	 * Update the model with the GUI settings
	 * 
	 * @param registry
	 * 
	 * @param model2
	 * @return
	 */
	private ETMParamAbstract updateModel(ETMParamAbstract ga, CentralRegistry registry) {
		model = ga;

		/*
		 * In case of configuration version, add a configuration alpha textfield
		 */
		if (ga.getFitnessEvaluator() instanceof ConfigurationFitness) {
			ConfigurationFitness configFitness = (ConfigurationFitness) ga.getFitnessEvaluator();

			configFitness.setAlpha(Double.parseDouble(alphaTxtfield.getText()));
		}

		if (fitnessGUI instanceof OverallFitnessGUI) {
			model.setFitnessEvaluator(new MultiThreadedFitnessEvaluator(registry, ((OverallFitnessGUI) fitnessGUI)
					.getTreeFitnessInstance(model), (Integer) nrThreadsCombo.getSelectedItem()));

		} else if (fitnessGUI instanceof ParetoFitnessEvaluatorGUI) {
			model.setFitnessEvaluator(new MultiThreadedFitnessEvaluator(registry,
					((ParetoFitnessEvaluatorGUI) fitnessGUI).getTreeFitnessInstance((ETMParamPareto) model),
					(Integer) nrThreadsCombo.getSelectedItem()));

		} else {
			//FIXME currently this is never triggered but I guess it won't work if it is...
			model.setFitnessEvaluator(new MultiThreadedFitnessEvaluator(registry, fitnessGUI.getTreeFitnessInstance(
					ga.getCentralRegistry(), TreeFitnessAbstract.class), (Integer) nrThreadsCombo.getSelectedItem()));

		}

		return model;
	}

	/**
	 * Set the model and update the visualization accordingly
	 * 
	 * @param model
	 */
	private void setModel(ETMParamAbstract model) {
		//TODO implement
	}

	////////////////////////////////////
	// ProMWizardStep implementation  //
	////////////////////////////////////

	public ETMParamAbstract apply(ETMParamAbstract model, JComponent component) {
		if (!(component instanceof ETMEvaluatorSettingsStep)) {
			return model;
		}
		return ((ETMEvaluatorSettingsStep) component).updateModel(model, model.getCentralRegistry());
	}

	public boolean canApply(ETMParamAbstract model, JComponent component) {
		return true;
	}

	public JComponent getComponent(ETMParamAbstract model) {
		setModel(model);
		return this;
	}

	public String getTitle() {
		return "ETM: Quality Calculation Settings";
	}

}