package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.DefaultIntervalXYDataset;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class DimensionBarChart extends AbstractParetoFrontChartNavigator {

	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the Pareto Front object
	 */
	private ParetoFront paretoFront;

	/**
	 * Reference to the registry from where we get the values.
	 */
	private CentralRegistry registry;

	/**
	 * The currently set X dimension
	 */
	private TreeFitnessInfo dimension;

	/**
	 * Sets the number of bars/buckets to create
	 */
	private int nrBars;

	/**
	 * The chart panel, that is placed on the contentsPanel
	 */
	private ChartPanel chartPanel;

	private int preferredHeight = 300;

	public DimensionBarChart(ParetoFront front, ParetoVisualization paretoVisualization, TreeFitnessInfo dim) {
		super(dim.getName() + " Values Bar Chart", paretoVisualization.navigationPanel, true);
		this.paretoFront = front;
		this.registry = paretoFront.getRegistry();

		dimension = dim;
		nrBars = 10;

		initializeContentsPanel();
		updateData(true);

		setContentSize(new Dimension(500, preferredHeight));
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			XYPlot plot = getChartPanel().getChart().getXYPlot();

			HistogramDataset dataset = new HistogramDataset();

			Collection<NAryTree> front = paretoFront.getFront();

			double[] values = new double[front.size()];

			int i = 0;
			Iterator<NAryTree> it = front.iterator();
			while (it.hasNext()) {
				values[i++] = registry.getFitness(it.next()).fitnessValues.get(dimension);
			}

			//dataset.addSeries(dimension.getCode(), values, 50, 0, 50);
			dataset.addSeries(dimension.getCode(), values, nrBars);

			plot.setDataset(dataset);
		}
	}

	public void updateSelectedModel(NAryTree model) {
		//TODO see what we can do, e.g. give 1 bar a different color?
	}

	/*
	 * GUI getters to initialize the objects
	 */

	private void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();
		//contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
		contentsPanel.setLayout(new GridBagLayout());
		contentsPanel.add(getChartPanel());
	}

	protected ChartPanel getChartPanel() {
		if (chartPanel == null) {
			DefaultIntervalXYDataset intervalxydataset = new DefaultIntervalXYDataset();
			JFreeChart chart = ChartFactory.createXYBarChart("Number of models per value range", dimension.getName(),
					false, "", intervalxydataset, PlotOrientation.VERTICAL, false, false, false);

			chartPanel = new ChartPanel(chart, false);

			//Save memory consumption
			chartPanel.getChartRenderingInfo().setEntityCollection(null);

			chartPanel.setMaximumSize(new Dimension(500, preferredHeight));
			chartPanel.setMinimumSize(new Dimension(500, preferredHeight));
			//chartPanel.setMaximumSize(new Dimension(500, 750));
			//			chart.getXYPlot().getRangeAxis().setRange(0, 450);
			//XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();
			//renderer.setBaseItemLabelsVisible(true);

			//We created the chart, now update it with the latest data
			updateData(true);
		}
		return chartPanel;
	}

}
