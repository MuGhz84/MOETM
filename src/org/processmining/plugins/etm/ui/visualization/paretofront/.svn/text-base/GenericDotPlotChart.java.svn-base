package org.processmining.plugins.etm.ui.visualization.paretofront;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;
import java.util.Iterator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

/**
 * Class with static methods to quickly create dot plots on 2 dimensions,
 * indicate the currently selected model and update a given dotplot
 * 
 * @author jbuijs
 * 
 */
public class GenericDotPlotChart {

	/**
	 * Creates a dot plot chart panel
	 * 
	 * @param xDim
	 * @param yDim
	 * @param registry
	 * @param currentTree
	 * @param listener
	 * @return
	 */
	public static ChartPanel getChartPanel(TreeFitnessInfo xDim, TreeFitnessInfo yDim, CentralRegistry registry,
			NAryTree currentTree, ChartProgressListener listener) {
		XYSeriesCollection dataset = new XYSeriesCollection();

		JFreeChart chart = ChartFactory.createScatterPlot("2 Dimensional Scatter Plot", xDim.getName(), yDim.getName(),
				dataset, PlotOrientation.VERTICAL, false, true, false);

		/*
		 * Enable crosshairs when clicking in the chart, showing lines to the
		 * axes helping to estimate the values.
		 */
		XYPlot plot = chart.getXYPlot();
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		//We fix the range to 0...1 
		/*-*/
		plot.getDomainAxis().setRange(0, 1);
		plot.getRangeAxis().setRange(0, 1);
		plot.getDomainAxis().setLowerBound(0);
		plot.getRangeAxis().setLowerBound(0);
		/**/

		//We don't want zooming, autozoom works best (or does it?)
		//getChartPanel().setDomainZoomable(false);
		//getChartPanel().setRangeZoomable(false);

		plot.setDomainCrosshairValue(registry.getFitness(currentTree).fitnessValues.get(xDim), false);
		plot.setRangeCrosshairValue(registry.getFitness(currentTree).fitnessValues.get(yDim), false);

		//Add lines between the dots to show a hint of what the Pareto front looks like
		XYItemRenderer renderer = plot.getRenderer();
		if (renderer instanceof XYLineAndShapeRenderer) {
			//note: it is not a good idea to make it a spline (=curved) rendered, you can get dips or bumps giving strange indications.
			XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer) renderer;
			//Only draw lines for this subfront, which is(/should be) the first series
			lineRenderer.setSeriesLinesVisible(0, true);
		}

		//Smaller dots
		Shape dot = new Ellipse2D.Double(0, 0, 2.0, 2.0);
		renderer.setSeriesShape(1, dot);
		//And for elements on the subfront:
		Shape rectangle = new Rectangle(2, 2);
		renderer.setSeriesShape(0, rectangle);

		//We listen to changes in order to update the model when the user clicks
		chart.addProgressListener(listener);

		ChartPanel chartPanel = new ChartPanel(chart, false);

		//Save memory consumption
		chartPanel.getChartRenderingInfo().setEntityCollection(null);

		//For paper chart generation square charts are prettiest
		chartPanel.setMaximumSize(new Dimension(500, 500));
		chartPanel.setMinimumSize(new Dimension(500, 500));
		chartPanel.setPreferredSize(new Dimension(500, 500));

		return chartPanel;
	}

	/**
	 * Places the crosshaires such that it points to the currently selected
	 * model
	 * 
	 * @param panel
	 * @param model
	 * @param registry
	 * @param xDim
	 * @param yDim
	 */
	public static void updateSelectedModel(ChartPanel panel, NAryTree model, CentralRegistry registry,
			TreeFitnessInfo xDim, TreeFitnessInfo yDim) {
		panel.getChart().getXYPlot().setDomainCrosshairValue(registry.getFitness(model).fitnessValues.get(xDim), true);
		panel.getChart().getXYPlot().setRangeCrosshairValue(registry.getFitness(model).fitnessValues.get(yDim), true);

	}

	/**
	 * Updates the dot plot with the provided pareto front
	 * 
	 * @param panel
	 * @param paretoFront
	 * @param xDim
	 * @param yDim
	 */
	public static void updateData(ChartPanel panel, ParetoFront paretoFront, TreeFitnessInfo xDim, TreeFitnessInfo yDim) {
		XYPlot plot = panel.getChart().getXYPlot();

		ParetoFront subParetoFront = paretoFront.getFrontForDimensions(new TreeFitnessInfo[] { xDim, yDim });

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries subFront = new XYSeries(xDim.getCode() + " v.s. " + yDim.getCode());
		XYSeries others = new XYSeries("Trees that are not on this subfront");
		XYSeries locked = new XYSeries("Current Tree that should not be in the Pareto front any more");
		for (NAryTree tree : paretoFront.getFront()) {
			TObjectDoubleHashMap<TreeFitnessInfo> fitness = paretoFront.getRegistry().getFitness(tree).fitnessValues;
			if (paretoFront.shouldBeRemovedButIsLocked(tree)) {
				locked.add(fitness.get(xDim), fitness.get(yDim));
			} else if (subParetoFront.inParetoFront(tree)) {
				subFront.add(fitness.get(xDim), fitness.get(yDim));
			} else {
				others.add(fitness.get(xDim), fitness.get(yDim));
			}
		}
		dataset.addSeries(subFront);
		dataset.addSeries(others);
		dataset.addSeries(locked);

		plot.setDataset(dataset);
	}

	/**
	 * Updates the dot plot with the provided pareto front
	 * 
	 * @param panel
	 * @param paretoFront
	 * @param xDim
	 * @param yDim
	 * @param zDim
	 */
	public static void updateData(ChartPanel panel, ParetoFront paretoFront, TreeFitnessInfo xDim,
			TreeFitnessInfo yDim, TreeFitnessInfo zDim) {
		XYPlot plot = panel.getChart().getXYPlot();

		ParetoFront subParetoFront = paretoFront.getFrontForDimensions(new TreeFitnessInfo[] { xDim, yDim });

		DefaultXYZDataset zDataset = new DefaultXYZDataset();

		//First put the trees in the corresponding sets
		HashSet<NAryTree> subFront = new HashSet<NAryTree>();
		HashSet<NAryTree> others = new HashSet<NAryTree>();
		HashSet<NAryTree> locked = new HashSet<NAryTree>();

		for (NAryTree tree : paretoFront.getFront()) {
			if (paretoFront.shouldBeRemovedButIsLocked(tree)) {
				locked.add(tree);
			} else if (subParetoFront.inParetoFront(tree)) {
				subFront.add(tree);
			} else {
				others.add(tree);
			}
		}

		//Now instantiate the appropriate 2D double arrays, containing x, y and z double[] for the dimension values
		double[][] subFrontValues = new double[3][subFront.size()];
		int i = 0;
		Iterator<NAryTree> it = subFront.iterator();
		while (it.hasNext()) {
			NAryTree tree = it.next();
			TObjectDoubleHashMap<TreeFitnessInfo> fitness = paretoFront.getRegistry().getFitness(tree).fitnessValues;

			subFrontValues[0][i] = fitness.get(xDim);
			subFrontValues[1][i] = fitness.get(yDim);
			subFrontValues[2][i] = fitness.get(zDim);

			i++;
		}
		zDataset.addSeries("SubFront", subFrontValues);

		i = 0;
		it = locked.iterator();
		double[][] lockedValues = new double[3][locked.size()];
		while (it.hasNext()) {
			NAryTree tree = it.next();
			TObjectDoubleHashMap<TreeFitnessInfo> fitness = paretoFront.getRegistry().getFitness(tree).fitnessValues;

			lockedValues[0][i] = fitness.get(xDim);
			lockedValues[1][i] = fitness.get(yDim);
			lockedValues[2][i] = fitness.get(zDim);

			i++;
		}
		zDataset.addSeries("Locked Trees", lockedValues);

		i = 0;
		it = others.iterator();
		double[][] othersValues = new double[3][others.size()];
		while (it.hasNext()) {
			NAryTree tree = it.next();
			TObjectDoubleHashMap<TreeFitnessInfo> fitness = paretoFront.getRegistry().getFitness(tree).fitnessValues;

			othersValues[0][i] = fitness.get(xDim);
			othersValues[1][i] = fitness.get(yDim);
			othersValues[2][i] = fitness.get(zDim);

			i++;
		}
		zDataset.addSeries("Other trees", othersValues);

		plot.setDataset(zDataset);
	}

}
