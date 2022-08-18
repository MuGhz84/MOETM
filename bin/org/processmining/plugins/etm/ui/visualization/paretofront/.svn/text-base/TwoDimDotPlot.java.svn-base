package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.fitness.metrics.PrecisionEscEdges;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class TwoDimDotPlot extends AbstractParetoFrontChartNavigator implements ChartProgressListener {
	/*-
	 * TODO TwoDimDotPlot:
	 * - allow selection to set filter for the two dimensions (category markers)
	 */

	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the Pareto Front object
	 */
	protected ParetoFront paretoFront;

	protected ParetoVisualization paretoVisualization;

	/**
	 * Reference to the registry from where we get the values.
	 */
	protected CentralRegistry registry;

	/**
	 * The currently set X dimension
	 */
	protected TreeFitnessInfo xDim;
	/**
	 * The currently set Y dimension
	 */
	protected TreeFitnessInfo yDim;

	/**
	 * The chart panel, that is placed on the contentsPanel
	 */
	protected ChartPanel chartPanel;

	/**
	 * JPanel with all the settings for the chart
	 */
	protected ProMPropertiesPanel settingsPanel;
	protected ProMComboBox xDimensionDropdown;
	protected ProMComboBox yDimensionDropdown;

	public TwoDimDotPlot(String title, ParetoFront front, ParetoVisualization paretoVisualization) {
		super(title, paretoVisualization.navigationPanel);
		this.paretoFront = front;
		this.paretoVisualization = paretoVisualization;
		this.registry = paretoFront.getRegistry();

		xDim = FitnessReplay.info;
		yDim = PrecisionEscEdges.info;

		initializeContentsPanel();
		updateData(true);

		//Set the preferred height which is the chart + 2 ProM dropdown boxes
		this.setContentSize(new Dimension((int) (chartWidth * 1.5), this.chartHeigth + 2 * 30));
		//this.setContentSize(new Dimension(getChartPanel().getSize().width, getChartPanel().getSize().height + 2	* getxDimensionDropdown().getSize().height));
	}

	public TwoDimDotPlot(ParetoFront front, ParetoVisualization paretoVisualization) {
		this("Two dimensional dot plot", front, paretoVisualization);
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			GenericDotPlotChart.updateData(getChartPanel(), paretoFront, xDim, yDim);
		}
	}

	public void updateSelectedModel(NAryTree model) {
		GenericDotPlotChart.updateSelectedModel(getChartPanel(), model, registry, xDim, yDim);
	}

	public void chartProgress(ChartProgressEvent event) {
		if (event.getType() == ChartProgressEvent.DRAWING_FINISHED) {
			if (chartPanel != null) {
				JFreeChart chart = this.chartPanel.getChart();
				if (chart != null) {
					XYPlot plot = chart.getXYPlot();
					double xx = plot.getDomainCrosshairValue();
					double yy = plot.getRangeCrosshairValue();

					//Get the corresponding tree(s)
					HashMap<TreeFitnessInfo, Double> values = new HashMap<TreeFitnessInfo, Double>();
					values.put(xDim, xx);
					values.put(yDim, yy);
					Set<NAryTree> trees = paretoFront.getTreeWithValues(values);
					//Only set the new current tree if we actually got trees and if the currently selected tree is not in the list of trees at that point (which indicates that the tree was updated from another source)
					if (!trees.isEmpty() && !trees.contains(paretoVisualization.currentTree)) {
						//There are multiple, we can only show one, show a random one...
						paretoVisualization.updateTree(trees.iterator().next());
					}
				}
			}
		}
	}

	/*
	 * GUI getters to initialize the objects
	 */

	protected void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();
		//		contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
		contentsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		contentsPanel.add(getChartPanel(), c);

		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		contentsPanel.add(SlickerFactory.instance().createLabel("Dimension for X-axis: "), c);
		c.gridx = 1;
		contentsPanel.add(getxDimensionDropdown(), c);

		c.gridx = 0;
		c.gridy = 2;
		contentsPanel.add(SlickerFactory.instance().createLabel("Dimension for Y-axis: "), c);
		c.gridx = 1;
		contentsPanel.add(getyDimensionDropdown(), c);
	}

	protected ChartPanel getChartPanel() {
		if (chartPanel == null) {
			chartPanel = GenericDotPlotChart.getChartPanel(xDim, yDim, registry, paretoVisualization.currentTree, this);

			//We created the chart, now update it with the latest data
			updateData(true);
		}
		return chartPanel;
	}

	/**
	 * @return the xDimensionDropdown
	 */
	public ProMComboBox getxDimensionDropdown() {
		if (xDimensionDropdown == null) {
			xDimensionDropdown = getGenericDimensionDropdown(true);
			xDimensionDropdown.setSelectedItem(xDim);
		}
		return xDimensionDropdown;
	}

	/**
	 * @return the yDimensionDropdown
	 */
	public ProMComboBox getyDimensionDropdown() {
		if (yDimensionDropdown == null) {
			yDimensionDropdown = getGenericDimensionDropdown(false);
			yDimensionDropdown.setSelectedItem(yDim);
		}
		return yDimensionDropdown;
	}

	@SuppressWarnings("unchecked")
	private ProMComboBox getGenericDimensionDropdown(boolean forX) {
		//ProMComboBox combobox = new ProMComboBox(TreeFitnessInfoForComboBoxWrapper.wrap(paretoFront.getDimensions()));
		ProMComboBox combobox = new ProMComboBox(paretoFront.getDimensions());

		combobox.setRenderer(new TreeFitnessInfo.TreeFitnessInfoComboboxRenderer(combobox.getRenderer()));

		combobox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Object item = e.getItem();
					if (item instanceof TreeFitnessInfo) {
						TreeFitnessInfo newDim = (TreeFitnessInfo) item;
						if (e.getSource() == getxDimensionDropdown()) {
							//Set the new X dimension axis
							xDim = newDim;
							getChartPanel().getChart().getXYPlot().getDomainAxis().setLabel(xDim.getName());
							updateData(true);
						} else if (e.getSource() == getyDimensionDropdown()) {
							yDim = newDim;
							getChartPanel().getChart().getXYPlot().getRangeAxis().setLabel(yDim.getName());
							updateData(true);
						}
						//Redraw the crossbars
						updateSelectedModel(paretoVisualization.currentTree);
					}
				}

			}
		});

		return combobox;
	}
}
