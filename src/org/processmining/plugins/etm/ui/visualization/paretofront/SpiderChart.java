package org.processmining.plugins.etm.ui.visualization.paretofront;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class SpiderChart extends AbstractParetoFrontChartNavigator {

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
	 * The chart panel, that is placed on the contentsPanel
	 */
	private ChartPanel chartPanel;

	public SpiderChart(ParetoFront front, ParetoVisualization paretoVisualization) {
		super("Spider Chart", paretoVisualization.navigationPanel);
		this.paretoFront = front;
		this.registry = paretoFront.getRegistry();

		initializeContentsPanel();
		updateData(true);

		setContentSize(new Dimension(500, 500));
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			SpiderWebPlot plot = (SpiderWebPlot) getChartPanel().getChart().getPlot();

			DefaultCategoryDataset dataset = (DefaultCategoryDataset) plot.getDataset();

			//Clear
			dataset.clear();

			//And rebuild the dataset
			for (NAryTree tree : paretoFront.getFront()) {
				TObjectDoubleHashMap<TreeFitnessInfo> fitness = registry.getFitness(tree).fitnessValues;
				for (TreeFitnessInfo dimension : paretoFront.getDimensions()) {
					dataset.addValue(fitness.get(dimension), tree.toString(), dimension.toString());
				}
			}

			/*-
			XYSeriesCollection dataset = new XYSeriesCollection();
			XYSeries subFront = new XYSeries(XDim.getCode() + " v.s. " + YDim.getCode());
			XYSeries others = new XYSeries("Trees that are not on this subfront");
			XYSeries locked = new XYSeries("Current Tree that should not be in the Pareto front any more");
			for (NAryTree tree : paretoFront.getFront()) {
				TObjectDoubleHashMap<TreeFitnessInfo> fitness = registry.getFitness(tree).fitnessValues;
				if (paretoFront.shouldBeRemovedButIsLocked(tree)) {
					locked.add(fitness.get(XDim), fitness.get(YDim));
				} else if (subParetoFront.inParetoFront(tree)) {
					subFront.add(fitness.get(XDim), fitness.get(YDim));
				} else {
					others.add(fitness.get(XDim), fitness.get(YDim));
				}
			}
			dataset.addSeries(subFront);
			dataset.addSeries(others);
			dataset.addSeries(locked);
			/**/

			plot.setDataset(dataset);
		}
	}

	public void updateSelectedModel(NAryTree model) {
		//We don't respond
	}

	/*-
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
					values.put(XDim, xx);
					values.put(YDim, yy);
					Set<NAryTree> trees = paretoFront.getTreeWithValues(values);
					if (!trees.isEmpty()) {
						//There are multiple, we can only show one, show a random one...
						paretoVisualization.updateTree(trees.iterator().next());
					}
				}
			}
		}
	}/**/

	private void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();
		contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
		contentsPanel.add(getChartPanel());
	}

	protected ChartPanel getChartPanel() {
		if (chartPanel == null) {
			CategoryDataset dataset = new DefaultCategoryDataset();

			SpiderWebPlot spiderwebplot = new SpiderWebPlot(dataset);
			JFreeChart chart = new JFreeChart("Spider Web Chart", TextTitle.DEFAULT_FONT, spiderwebplot, false);

			/*-
			//Add lines between the dots to show a hint of what the Pareto front looks like
			XYItemRenderer renderer = plot.getRenderer();
			if (renderer instanceof XYLineAndShapeRenderer) {
				//note: it is not a good idea to make it a spline (=curved) rendered, you can get dips or bumps giving strange indications.
				XYLineAndShapeRenderer lineRenderer = (XYLineAndShapeRenderer) renderer;
				//Only draw lines for this subfront, which is(/should be) the first series
				lineRenderer.setSeriesLinesVisible(0, true);
			}

			//chart.removeLegend();

			//We listen to changes in order to update the model when the user clicks
			chart.addProgressListener(this);
			/**/

			chartPanel = new ChartPanel(chart);

			//Save memory consumption
			chartPanel.getChartRenderingInfo().setEntityCollection(null);

			//We created the chart, now update it with the latest data
			updateData(true);
		}
		return chartPanel;
	}

}
