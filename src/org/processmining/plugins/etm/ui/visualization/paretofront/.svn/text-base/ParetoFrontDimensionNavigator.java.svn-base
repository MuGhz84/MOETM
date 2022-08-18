package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.narytree.NAryTree;

import com.fluxicon.slickerbox.factory.SlickerFactory;

// FIXME check all class contents
// FIXME Test Class thoroughly
// TODO Improve based on suggestions by VIS group
@SuppressWarnings("serial")
public class ParetoFrontDimensionNavigator extends AbstractParetoFrontNavigator {
	private final CentralRegistry registry;

	private ParetoVisualization paretoVisualization;
	private TreeFitnessInfo dim;

	private boolean isHorizontal = true;

	/*
	 * GUI contents
	 */
	private JButton worstButton;
	private JButton worseButton;
	private JButton betterButton;
	private JButton bestButton;

	private JLabel dimensionValueLabel;
	private JLabel worstLabel;
	private JLabel worseLabel;
	private JLabel nrWorseLabel;
	private JLabel currentLabel;
	private JLabel betterLabel;
	private JLabel nrBetterLabel;
	private JLabel bestLabel;

	public ParetoFrontDimensionNavigator(ParetoVisualization paretoVisualization, TreeFitnessInfo dim) {
		this(paretoVisualization, dim, true);
	}

	public ParetoFrontDimensionNavigator(ParetoVisualization paretoVisualization, TreeFitnessInfo dim,
			boolean isHorizontal) {
		super(dim.getName(), paretoVisualization.navigationPanel);
		this.paretoVisualization = paretoVisualization;
		this.dim = dim;
		this.isHorizontal = isHorizontal;
		this.registry = paretoVisualization.paretoFront.getRegistry();

		initializeContentsPanel();

		updateData(true);
	}

	/**
	 * When we get notified of a new selected model, we currently completely
	 * rebuild the layout
	 */
	public void updateSelectedModel(NAryTree model) {
		//TODO split update selected model and a real pareto Front content update
		updateData(true);
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			NAryTree worstTree = paretoVisualization.paretoFront.getWorst(dim);
			getWorstLabel().setText(String.format("%2.3f", registry.getFitness(worstTree).fitnessValues.get(dim)));

			NAryTree worseTree = paretoVisualization.paretoFront.getWorse(dim, paretoVisualization.currentTree);
			if (worseTree == null) {
				getWorseLabel().setText("NO WORSE TREE");
			} else {
				getWorseLabel().setText(String.format("%2.3f", registry.getFitness(worseTree).fitnessValues.get(dim)));
			}

			NAryTree bestTree = paretoVisualization.paretoFront.getBest(dim);
			getBestLabel().setText(String.format("%2.3f", registry.getFitness(bestTree).fitnessValues.get(dim)));

			NAryTree betterTree = paretoVisualization.paretoFront.getBetter(dim, paretoVisualization.currentTree);
			if (betterTree == null || bestTree == paretoVisualization.currentTree) {
				getBetterLabel().setText("NO BETTER TREE");
			} else {
				getBetterLabel()
						.setText(String.format("%2.3f", registry.getFitness(betterTree).fitnessValues.get(dim)));
			}
			getDimensionValueLabel()
					.setText(
							String.format("%2.3f",
									registry.getFitness(paretoVisualization.currentTree).fitnessValues.get(dim)));

			//Now also update the number of worse/better and if 0 disable the buttons
			int nrWorse = paretoVisualization.paretoFront.getNrWorse(dim, paretoVisualization.currentTree);
			getNrWorseLabel().setText(String.format("%,8d%n", nrWorse));
			if (nrWorse == 0) {
				getWorseButton().setEnabled(false);
				getWorstButton().setEnabled(false);
			} else if (nrWorse == 1) {
				getWorseButton().setEnabled(false);
				getWorstButton().setEnabled(true);
			} else {
				getWorseButton().setEnabled(true);
				getWorstButton().setEnabled(true);
			}

			int nrBetter = paretoVisualization.paretoFront.getNrBetter(dim, paretoVisualization.currentTree);
			getNrBetterLabel().setText(String.format("%,8d%n", nrBetter));
			if (nrBetter == 0) {
				getBetterButton().setEnabled(false);
				getBestButton().setEnabled(false);
			} else if (nrBetter == 1) {
				getBetterButton().setEnabled(false);
				getBestButton().setEnabled(true);
			} else {
				getBetterButton().setEnabled(true);
				getBestButton().setEnabled(true);
			}
			this.revalidate();
		}
	}

	public void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();

		//Depending on the orientation we create rows and cols and fill them
		if (isHorizontal) {
			contentsPanel.setLayout(new GridLayout(3, 5, 5, 5));

			//First row: buttons and the dimension text
			contentsPanel.add(getWorstButton());
			contentsPanel.add(getWorseButton());
			contentsPanel.add(getDummyLabel());
			contentsPanel.add(getBetterButton());
			contentsPanel.add(getBestButton());

			//Second row: Values for that dimension
			contentsPanel.add(getWorstLabel());
			contentsPanel.add(getWorseLabel());
			contentsPanel.add(getDimensionValueLabel());
			contentsPanel.add(getBetterLabel());
			contentsPanel.add(getBestLabel());

			//Third row: numbers! (better and worse)
			contentsPanel.add(getDummyLabel());
			contentsPanel.add(getNrWorseLabel());
			contentsPanel.add(getDummyLabel());
			contentsPanel.add(getNrBetterLabel());
			contentsPanel.add(getDummyLabel());
		} else {
			//Vertical!
			contentsPanel.setLayout(new GridLayout(5, 3, 5, 5));

			//First row: Worst
			contentsPanel.add(getWorstButton());
			contentsPanel.add(getWorstLabel());
			contentsPanel.add(getDummyLabel());

			//Second row: Worse
			contentsPanel.add(getWorseButton());
			contentsPanel.add(getWorseLabel());
			contentsPanel.add(getNrWorseLabel());

			//Third row: current dimension
			contentsPanel.add(getDummyLabel());
			contentsPanel.add(getDimensionValueLabel());
			contentsPanel.add(getDummyLabel());

			//Fourth row: better
			contentsPanel.add(getBetterButton());
			contentsPanel.add(getBetterLabel());
			contentsPanel.add(getNrBetterLabel());

			//Fifth row: best
			contentsPanel.add(getBestButton());
			contentsPanel.add(getBestLabel());
			contentsPanel.add(getDummyLabel());
		}
	}

	public JButton getWorstButton() {
		if (worstButton == null) {
			worstButton = SlickerFactory.instance().createButton("<<");
			worstButton.setToolTipText("Show worst process model for this dimension.");

			worstButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Update visualization with worst tree
					paretoVisualization.updateTree(paretoVisualization.paretoFront.getWorst(dim));
				}
			});
		}
		return worstButton;
	}

	public JButton getWorseButton() {
		if (worseButton == null) {
			worseButton = SlickerFactory.instance().createButton("<");
			worseButton.setToolTipText("Show next worse process model for this dimension.");

			worseButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Update visualization with one worse tree
					paretoVisualization.updateTree(paretoVisualization.paretoFront.getWorse(dim,
							paretoVisualization.currentTree));
				}
			});
		}

		return worseButton;
	}

	public JButton getBetterButton() {
		if (betterButton == null) {
			betterButton = SlickerFactory.instance().createButton(">");
			betterButton.setToolTipText("Show next better process model for this dimension.");

			betterButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Update visualization with one better tree
					paretoVisualization.updateTree(paretoVisualization.paretoFront.getBetter(dim,
							paretoVisualization.currentTree));
				}
			});
		}

		return betterButton;
	}

	public JButton getBestButton() {
		if (bestButton == null) {
			bestButton = SlickerFactory.instance().createButton(">>");
			bestButton.setToolTipText("Show best process model for this dimension.");

			bestButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					//Update visualization with one better tree
					paretoVisualization.updateTree(paretoVisualization.paretoFront.getBest(dim));
				}
			});
		}

		return bestButton;
	}

	public JLabel getWorstLabel() {
		if (worstLabel == null) {
			worstLabel = SlickerFactory.instance().createLabel("");
			worstLabel.setToolTipText("Worst fitness value for this dimension");
		}

		return worstLabel;
	}

	public JLabel getWorseLabel() {
		if (worseLabel == null) {
			worseLabel = SlickerFactory.instance().createLabel("");
			worseLabel.setToolTipText("Fitness value of the next worse process model for this dimension");
		}

		return worseLabel;
	}

	public JLabel getBetterLabel() {
		if (betterLabel == null) {
			betterLabel = SlickerFactory.instance().createLabel("");
			betterLabel.setToolTipText("Fitness value of the next better process model for this dimension");
		}

		return betterLabel;
	}

	public JLabel getBestLabel() {
		if (bestLabel == null) {
			bestLabel = SlickerFactory.instance().createLabel("");
			bestLabel.setToolTipText("Fitness value of the best process model for this dimension");
		}

		return bestLabel;
	}

	/**
	 * @return the dimensionValueLabel
	 */
	public JLabel getDimensionValueLabel() {
		if (dimensionValueLabel == null) {
			dimensionValueLabel = SlickerFactory.instance().createLabel("");
			dimensionValueLabel.setToolTipText("Fitness value for the current process model for this dimension");
		}

		return dimensionValueLabel;
	}

	public JLabel getNrWorseLabel() {
		if (nrWorseLabel == null) {
			nrWorseLabel = SlickerFactory.instance().createLabel("");
			nrWorseLabel.setToolTipText("Number of worse process trees.");
		}

		return nrWorseLabel;
	}

	public JLabel getNrBetterLabel() {
		if (nrBetterLabel == null) {
			nrBetterLabel = SlickerFactory.instance().createLabel("");
			nrBetterLabel.setToolTipText("Number of better process trees.");
		}

		return nrBetterLabel;
	}

	public static JLabel getDummyLabel() {
		return SlickerFactory.instance().createLabel("");
	}
}
