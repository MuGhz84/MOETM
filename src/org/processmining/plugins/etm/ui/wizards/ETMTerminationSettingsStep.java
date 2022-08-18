package org.processmining.plugins.etm.ui.wizards;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.processmining.plugins.etm.parameters.ETMParamFactory;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.ElapsedTime;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.GenerationCount;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.Stagnation;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.termination.TargetFitness;

/**
 * Step 4: Termination settings
 * 
 * @author jbuijs
 * 
 */
class ETMTerminationSettingsStep extends ProMHeaderPanel implements ProMWizardStep<ETMParamAbstract> {

	private static final long serialVersionUID = 7984729188459814232L;

	private ETMParamAbstract model;

	private final ProMPropertiesPanel propertiesPanel;

	private ProMTextField targetFitness;
	private ProMTextField maxIterations;
	private ProMTextField steadyStates;
	private ProMTextField maxDuration;

	public ETMTerminationSettingsStep(ETMParamAbstract param) {
		//call constructor of header panel...
		super(null);

		propertiesPanel = new ProMPropertiesPanel(null);

		TargetFitness tf = (TargetFitness) ETMParamFactory.getTerminationCondition(param, TargetFitness.class);
		targetFitness = propertiesPanel.addTextField("Target Fitness", tf != null ? "" + tf.getTargetFitness() : "1.0");
		TooltipHelper.addTooltip(propertiesPanel, targetFitness,
				"Stops the algorithm when at least this overall fitness score is reached");
		
		GenerationCount gc = (GenerationCount) ETMParamFactory.getTerminationCondition(param, GenerationCount.class);
		maxIterations = propertiesPanel.addTextField("Maximum Generations", gc != null ? "" + gc.getGenerationCount()
				: "1000");
		TooltipHelper.addTooltip(propertiesPanel, maxIterations, "Stops the algorithm after so many generations.");
		
		steadyStates = propertiesPanel.addTextField("Nr. steady states", "-1");
		TooltipHelper.addTooltip(propertiesPanel, steadyStates,
				"Stops the algorithm when no new candidate has been found in so many generations");
		
		maxDuration = propertiesPanel.addTextField("Max. duration", "-1");
		TooltipHelper
				.addTooltip(propertiesPanel, maxDuration,
						"Stops the algorithm as soon as this amount of time (in milliseconds, so 1000 = 1 second) has passed since the start.");

		//Add it to the wizard step...
		this.setLayout(new BorderLayout());
		this.add(propertiesPanel, BorderLayout.CENTER);
	}

	/**
	 * Update the model with the GUI settings
	 * 
	 * @param model2
	 * @return
	 */
	private ETMParamAbstract updateModel(ETMParamAbstract param) {
		model = param;

		double targetFitnessvalue = Double.parseDouble(targetFitness.getText());
		if (targetFitnessvalue > 0) {
			ETMParamFactory.addOrReplaceTerminationCondition(param, new TargetFitness(targetFitnessvalue, param
					.getFitnessEvaluator().isNatural()));
		} else {
			ETMParamFactory.removeTerminationConditionIfExists(param, TargetFitness.class);
		}

		int maxIterationsValue = Integer.parseInt(maxIterations.getText());
		if (maxIterationsValue > 0) {
			ETMParamFactory.addOrReplaceTerminationCondition(param, new GenerationCount(maxIterationsValue));
		} else {
			//If less than 0 remove!
			ETMParamFactory.removeTerminationConditionIfExists(param, GenerationCount.class);
		}

		int stagnationValue = Integer.parseInt(steadyStates.getText());
		if (stagnationValue > 0) {
			ETMParamFactory.addOrReplaceTerminationCondition(param, new Stagnation(stagnationValue, param
					.getFitnessEvaluator().isNatural()));
		} else {
			ETMParamFactory.removeTerminationConditionIfExists(param, Stagnation.class);
		}

		int maxDurationValue = Integer.parseInt(maxDuration.getText());
		if (maxDurationValue >= 0) {
			ETMParamFactory.addOrReplaceTerminationCondition(param, new ElapsedTime(maxDurationValue));
		} else {
			ETMParamFactory.removeTerminationConditionIfExists(param, ElapsedTime.class);
		}

		return model;
	}

	/**
	 * Update GUI to new model provided...
	 * 
	 * @param model2
	 */
	private void setModel(ETMParamAbstract model) {
		this.model = model;

		//TODO update UI fields
	}

	////////////////////////////////////
	// ProMWizardStep implementation  //
	////////////////////////////////////

	public ETMParamAbstract apply(ETMParamAbstract model, JComponent component) {
		if (!(component instanceof ETMTerminationSettingsStep)) {
			return model;
		}
		return ((ETMTerminationSettingsStep) component).updateModel(model);
	}

	public boolean canApply(ETMParamAbstract model, JComponent component) {
		return true;
	}

	public JComponent getComponent(ETMParamAbstract model) {
		setModel(model);
		return this;
	}

	public String getTitle() {
		return "ETM: Termination Settings";
	}
}