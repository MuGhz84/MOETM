package org.processmining.plugins.etm.ui.wizards;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.processmining.plugins.etm.parameters.ETMParamFactory;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Step 1: general settings such as population size and ratios.
 * 
 * @author jbuijs
 * 
 */
class ETMGeneralSettingsStep extends ProMHeaderPanel implements ProMWizardStep<ETMParamAbstract> {

	private static final long serialVersionUID = 7984729188459814232L;

	private ProMTextField popSize;
	private ProMTextField eliteCount;
	private ProMTextField logModulo;

	private JComboBox eventClassifierCombobox;

	private ETMParamAbstract param;

	public ETMGeneralSettingsStep(ETMParamAbstract param) {
		//call constructor of header panel...
		super(null);

		ProMPropertiesPanel propertiespanel = new ProMPropertiesPanel(null);

		String popSizeTooltip = "Sets the number of candidates that are evolved and evaluated in each generation (/round).";
		popSize = propertiespanel.addTextField("Population Size", "" + ETMParamFactory.STD_POPSIZE);
		TooltipHelper.addTooltip(propertiespanel, popSize, popSizeTooltip);

		String elitecountTooltip = "<html>Sets the number of candidates that is maintained between generations <br>"
				+ "to prevent degeneration of the quality. <br>"
				+ "Advise is to set it to 1/4 or 1/5 of the population size <br/>"
				+ "or to 1 if you're running in Pareto front mode.</html>";
		eliteCount = propertiespanel.addTextField("Elite count", "" + ETMParamFactory.STD_ELITE_COUNT);
		TooltipHelper.addTooltip(propertiespanel, eliteCount, elitecountTooltip);

		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();

		classifiers.addAll(param.getCentralRegistry().getLog().getClassifiers());

		//TODO prevent duplication :) (e.g. make standard list and custom copy if not in classifiers list)
		//Add some standard classifiers :)
		boolean containsEventNameClassifier = false;
		boolean containsResourceClassifier = false;
		boolean containsLifeCycleClassifier = false;

		classifiers.add(new XEventNameClassifier());
		classifiers.add(new XEventResourceClassifier());
		classifiers.add(new XEventLifeTransClassifier());

		JLabel eventClassifierLabel = SlickerFactory.instance().createLabel("Event Classifier");
		String eventClassifierTooltip = "<html>Set the event classifier to use during discovery. <br> "
				+ "The event classifier determines what the notion of an activity is.</htmnl>";
		eventClassifierLabel.setToolTipText(eventClassifierTooltip);

		eventClassifierCombobox = propertiespanel.addComboBox("Event Classifier",
				classifiers.toArray(new XEventClassifier[classifiers.size()]));
		TooltipHelper.addTooltip(propertiespanel, eventClassifierCombobox, eventClassifierTooltip);

		String logIntervalTooltip = "Sets the number of generations after which the progress is communicated.";
		logModulo = propertiespanel.addTextField("Message interval", "" + 10);
		TooltipHelper.addTooltip(propertiespanel, logModulo, logIntervalTooltip);

		//Add it to the wizard step...
		this.setLayout(new BorderLayout());
		this.add(propertiespanel, BorderLayout.CENTER);
	}

	/**
	 * Update the model with the GUI settings
	 * 
	 * @param model2
	 * @return
	 */
	private ETMParamAbstract updateModel(ETMParamAbstract param) {
		this.param = param;
		this.param.setPopulationSize(Integer.parseInt(popSize.getText()));

		int eliteCounti = Integer.parseInt(eliteCount.getText());
		if (eliteCounti > this.param.getPopulationSize())
			eliteCounti = Math.max((int) (0.3 * this.param.getPopulationSize()), 1);
		this.param.setEliteCount(eliteCounti);

		this.param.setLogModulo(Integer.parseInt(logModulo.getText()));

		this.param.getCentralRegistry().updateEventClassifier(
				(XEventClassifier) eventClassifierCombobox.getSelectedItem());

		/*-
		XEventClassifier eventClassifier = (XEventClassifier) eventClassifierCombobox.getSelectedItem();
		this.param.setCentralRegistry(new CentralRegistry(oldRegistry.getContext(), oldRegistry.getLog(),
				eventClassifier, oldRegistry.getRandom()));
		/**/

		return this.param;
	}

	/**
	 * Update GUI to new model provided...
	 * 
	 * @param param
	 */
	private void setModel(ETMParamAbstract param) {
		this.param = param;
	}

	////////////////////////////////////
	// ProMWizardStep implementation  //
	////////////////////////////////////

	public ETMParamAbstract apply(ETMParamAbstract model, JComponent component) {
		if (!(component instanceof ETMGeneralSettingsStep)) {
			return model;
		}
		return ((ETMGeneralSettingsStep) component).updateModel(model);
	}

	public boolean canApply(ETMParamAbstract model, JComponent component) {
		try {
			//Test if we can parse the integers
			int popSizeValue = Integer.parseInt(popSize.getText());
			int eliteSizeValue = Integer.parseInt(eliteCount.getText());

			//And if all values are positive and elite is strictly smaller than popsize
			if (popSizeValue > 0 && eliteSizeValue > 0 && popSizeValue > eliteSizeValue) {
				return true;
			} else {
				//TODO wizard just refuses to do next. Add message explaining why...
				return false;
			}
		} catch (Exception e) {
			//TODO wizard just refuses to do next. Add message explaining why...
			return false;
		}
	}

	public JComponent getComponent(ETMParamAbstract model) {
		setModel(model);
		return this;
	}

	public String getTitle() {
		return "ETM: General Settings";
	}
}