package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class GridDotPlots extends AbstractParetoFrontChartNavigator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParetoFront paretoFront;
	private ParetoVisualization paretoVisualization;
	private CentralRegistry registry;

	/**
	 * The different chartpanels in an nrDim*nrDim grid. Note that the diagonal
	 * with equal indices are null!
	 */
	private ChartPanel[][] panels;
	private TreeFitnessInfo[] dimensions;

	public GridDotPlots(ParetoFront front, ParetoVisualization paretoVisualization) {
		super("Grid dot plot", paretoVisualization.navigationPanel);
		this.paretoFront = front;
		this.paretoVisualization = paretoVisualization;
		this.registry = paretoFront.getRegistry();
		this.dimensions = paretoFront.getDimensions();

		panels = new ChartPanel[dimensions.length][dimensions.length];

		initializeContentsPanel();

		updateData(true);
	}

	private void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();
		contentsPanel.setMaximumSize(new Dimension(paretoVisualization.getSize(null).width / 2, paretoVisualization
				.getSize(null).width));
		GridLayout layout = new GridLayout(dimensions.length + 1, dimensions.length + 1);
		contentsPanel.setLayout(layout);

		//Create chart panels and show them in a grid
		for (int y = -1; y < dimensions.length; y++) {
			if (y == -1) {
				//Add column headers
				contentsPanel.add(new JLabel(""));
				for (int x = 0; x < dimensions.length; x++) {
					contentsPanel.add(new JLabel(dimensions[x].getName()));
				}
			} else {
				//Not the header row
				for (int x = -1; x < dimensions.length; x++) {
					if (x == -1) {
						//Add row header
						contentsPanel.add(new JLabel(dimensions[y].getName()));
					} else if (x != y) {
						ChartPanel chartPanel = GenericDotPlotChart.getChartPanel(dimensions[x], dimensions[y],
								registry, paretoVisualization.currentTree, null);
						panels[x][y] = chartPanel;
						contentsPanel.add(chartPanel);
					} else {
						contentsPanel.add(new JLabel("-"));
					}
				}
			}
		}
	}

	protected ChartPanel getChartPanel() {
		return null;
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			for (int x = 0; x < dimensions.length; x++) {
				for (int y = 0; y < dimensions.length; y++) {
					if (x != y) {
						GenericDotPlotChart.updateData(panels[x][y], paretoFront, dimensions[x], dimensions[y]);
					}
				}
			}
		}
	}

	public void updateSelectedModel(NAryTree model) {
		for (int x = 0; x < dimensions.length; x++) {
			for (int y = 0; y < dimensions.length; y++) {
				if (x != y) {
					GenericDotPlotChart
							.updateSelectedModel(panels[x][y], model, registry, dimensions[x], dimensions[y]);
				}
			}
		}
	}

}
