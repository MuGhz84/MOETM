package org.processmining.plugins.etm.ui.wizards;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMHeaderPanel;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.mutation.GuidedTreeMutationCoordinator;
import org.processmining.plugins.etm.mutation.TreeCrossover;
import org.processmining.plugins.etm.mutation.TreeMutationAbstract;
import org.processmining.plugins.etm.mutation.TreeMutationCoordinator;
import org.processmining.plugins.etm.mutation.mutators.AddNodeRandom;
import org.processmining.plugins.etm.mutation.mutators.MutateSingleNodeRandom;
import org.processmining.plugins.etm.mutation.mutators.NormalizationMutation;
import org.processmining.plugins.etm.mutation.mutators.RemoveSubtreeRandom;
import org.processmining.plugins.etm.mutation.mutators.RemoveUselessNode;
import org.processmining.plugins.etm.mutation.mutators.maikelvaneck.MutateSingleNodeGuided;
import org.processmining.plugins.etm.mutation.mutators.maikelvaneck.ReplaceTreeBySequenceMutation;
import org.processmining.plugins.etm.parameters.ETMParamAbstract;
import org.uncommons.maths.random.Probability;
import org.uncommonseditedbyjoosbuijs.watchmaker.framework.EvolutionaryOperator;

/**
 * Step 3: Evolutionary Operator settings
 * 
 * @author jbuijs
 * 
 */
class ETMOperatorsSettingsStep extends ProMHeaderPanel implements ProMWizardStep<ETMParamAbstract> {
	//TODO make this wizard step as generic as the quality step (ie auto discovery of operators, show list, etc.)

	private static final long serialVersionUID = 7984729188459814232L;

	private ETMParamAbstract model;

	private final ProMPropertiesPanel propertiesPanel;

	private ProMTextField crossoverChance;

	private ProMTextField randomTreeCreationChance;

	private ProMTextField randomNodeAddChance;

	private ProMTextField randomNodeRemoveChance;

	private ProMTextField randomNodeMutationChance;

	private ProMTextField randomTreeNormalizationChance;

	private ProMTextField uselessNodeRemovalChance;

	private JCheckBox increaseEventclasses;

	public ETMOperatorsSettingsStep(ETMParamAbstract param) {
		//call constructor of header panel...
		super(null);

		propertiesPanel = new ProMPropertiesPanel(null);

		//increaseEventclasses = propertiesPanel.addCheckBox("Steadily increase the number of activities considered",false);
		TooltipHelper
				.addTooltip(
						propertiesPanel,
						increaseEventclasses,
						"<HTML>Enabling this feature makes the ETM consider only few activities at first, steadily increasing them. <BR /> "
								+ "NOTE that at each increase, all known trees have to be re-evaluated which can take some time <BR /> "
								+ "and might result in a decrease in (overall) fitness!</HTML>");

		//propertiesPane.add(SlickerFactory.instance().createLabel("Chances for main evolutionary operators:"));
		crossoverChance = propertiesPanel.addTextField("CrossOver", "" + 0.25);
		TooltipHelper.addTooltip(propertiesPanel, crossoverChance,
				"Chance of Crossover, with respect to random tree creation an mutation (in general).");
		randomTreeCreationChance = propertiesPanel.addTextField("Random Tree Creation", "" + 0.25);
		TooltipHelper.addTooltip(propertiesPanel, randomTreeCreationChance,
				"Chance of random tree creation, with respect to crossover an mutation (in general).");
		//Mutators:
		//propertiesPane.add(SlickerFactory.instance().createLabel("Chances for mutation operators:"));
		randomNodeAddChance = propertiesPanel.addTextField("Random Node addition", "" + 1);
		TooltipHelper.addTooltip(propertiesPanel, randomNodeAddChance,
				"If mutation is applied, the chance that a random node is added to the tree");
		randomNodeRemoveChance = propertiesPanel.addTextField("Random Node removal", "" + 1);
		TooltipHelper.addTooltip(propertiesPanel, randomNodeRemoveChance,
				"If mutation is applied, the chance that a random node is removed from the tree");
		randomNodeMutationChance = propertiesPanel.addTextField("Random Node mutation", "" + 1);
		TooltipHelper
				.addTooltip(propertiesPanel, randomNodeMutationChance,
						"If mutation is applied, the chance that a random node is changed (e.g. operator type of activity) to the tree");
		randomTreeNormalizationChance = propertiesPanel.addTextField("Normalization", "" + 0.1);
		TooltipHelper
				.addTooltip(
						propertiesPanel,
						randomTreeNormalizationChance,
						"<html>If mutation is applied, the chance that the tree is normalized <br> "
								+ "(e.g. children with the same operator type as their parent are merged with their parent etc.)</html>");
		uselessNodeRemovalChance = propertiesPanel.addTextField("Useless node removal", "" + 0.1);
		TooltipHelper
				.addTooltip(
						propertiesPanel,
						uselessNodeRemovalChance,
						"<html>If mutation is applied, the chance that all useless nodes (e.g. tau's in a sequence) are removed. <br>"
								+ "Note that these 'strange' constructs can be good for evolution, so don't set too high</html>");

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

		CentralRegistry centralRegistry = param.getCentralRegistry();

		//TODO make fault tolerant
		//First get the values from the ~textfields
		double crossoverChanceValue = Double.parseDouble(crossoverChance.getText());
		double randomTreeCreationChanceValue = Double.parseDouble(randomTreeCreationChance.getText());
		double randomNodeAddChanceValue = Double.parseDouble(randomNodeAddChance.getText());
		double randomNodeRemoveChanceValue = Double.parseDouble(randomNodeRemoveChance.getText());
		double randomNodeMutationChanceValue = Double.parseDouble(randomNodeMutationChance.getText());
		double randomTreeNormalizationChanceValue = Double.parseDouble(randomTreeNormalizationChance.getText());
		double uselessNodeRemovalChanceValue = Double.parseDouble(uselessNodeRemovalChance.getText());

		//And construct the list of operators from scratch
		List<EvolutionaryOperator<NAryTree>> evolutionaryOperators = new ArrayList<EvolutionaryOperator<NAryTree>>();

		//Event Class 'mutator' should be first and always called!
		/*-
		if (increaseEventclasses.isSelected()) {
			evolutionaryOperators.add(new GraduallyConsiderMoreEventClasses(centralRegistry));
		}/**/

		//Crossover
		evolutionaryOperators.add(new TreeCrossover<NAryTree>(1, new Probability(crossoverChanceValue),centralRegistry));

		//FIXME hard-coded changes to enable Maikels smarter mutations. 
		//NOTE that the treefactory is set in 
		
		LinkedHashMap<TreeMutationAbstract, Double> smartMutators = new LinkedHashMap<TreeMutationAbstract, Double>();
		smartMutators.put(new MutateSingleNodeGuided(centralRegistry), 0.25);
//		smartMutators.put(new RemoveActivityGuided(centralRegistry), 0.25);

		//Collection of dumb mutators
		LinkedHashMap<TreeMutationAbstract, Double> dumbMutators = new LinkedHashMap<TreeMutationAbstract, Double>();
		dumbMutators.put(new AddNodeRandom(centralRegistry), randomNodeAddChanceValue);
		dumbMutators.put(new MutateSingleNodeRandom(centralRegistry), randomNodeMutationChanceValue);
		dumbMutators.put(new RemoveSubtreeRandom(centralRegistry), randomNodeRemoveChanceValue);
		dumbMutators.put(new NormalizationMutation(centralRegistry), randomTreeNormalizationChanceValue);
		dumbMutators.put(new RemoveUselessNode(centralRegistry), uselessNodeRemovalChanceValue);
		//dumbMutators.put(new ReplaceTreeMutation(centralRegistry), randomTreeCreationChanceValue);
		dumbMutators.put(new ReplaceTreeBySequenceMutation(centralRegistry), randomTreeCreationChanceValue);

		TreeMutationCoordinator dumbCoordinator = new TreeMutationCoordinator(dumbMutators, false);

		GuidedTreeMutationCoordinator smartCoordinator = new GuidedTreeMutationCoordinator(centralRegistry, 0.25, true,
				smartMutators, dumbCoordinator);

		//		evolutionaryOperators.add(dumbCoordinator);
		evolutionaryOperators.add(smartCoordinator);

		param.setEvolutionaryOperators(evolutionaryOperators);

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
		if (!(component instanceof ETMOperatorsSettingsStep)) {
			return model;
		}
		return ((ETMOperatorsSettingsStep) component).updateModel(model);
	}

	public boolean canApply(ETMParamAbstract model, JComponent component) {
		return true;
	}

	public JComponent getComponent(ETMParamAbstract model) {
		setModel(model);
		return this;
	}

	public String getTitle() {
		return "ETM: Process Tree Evolutionary Operator Settings";
	}
}