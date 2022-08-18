package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class GenerationSeriesPlot extends AbstractParetoFrontChartNavigator {

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

	private XYSeries series;

	public GenerationSeriesPlot(ParetoFront front, LiveETMParetoVisualization liveParetoVisualization) {
		super("Pareto front size per generation", liveParetoVisualization.paretoVis.navigationPanel);
		this.paretoFront = front;
		this.registry = paretoFront.getRegistry();

		initializeContentsPanel();
		updateData(true);

		setContentSize(new Dimension(500, 500));
	}

	public void updateData(boolean updateVis) {
		series.add(registry.getCurrentGeneration(), paretoFront.size(), updateVis);
	}

	public void updateSelectedModel(NAryTree model) {
		//We don't respond to GUI changes
	}

	/*
	 * GUI getters to initialize the objects
	 */

	private void initializeContentsPanel() {
		JPanel contentsPanel = getContentPanel();
		contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.Y_AXIS));
		contentsPanel.add(getChartPanel());
	}

	protected ChartPanel getChartPanel() {
		if (chartPanel == null) {

			series = new XYSeries("PFSizePerGeneration");
			series.add(0, 0);
			XYDataset dataset = new XYSeriesCollection(series);
			JFreeChart chart = ChartFactory.createXYLineChart("Pareto front size per generation", "Generation",
					"Pareto front size", dataset, PlotOrientation.VERTICAL, false, false, false);

			final XYPlot plot = chart.getXYPlot();

			plot.getDomainAxis().setAutoRange(true);
			plot.getRangeAxis().setAutoRange(true);

			plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			//slightly not working data axis
			/*-  
			DateAxis dateaxis = (DateAxis) plot.getDomainAxis();
			Minute minute = new Minute(0, 9, 1, 10, 2006);
			RelativeDateFormat relativedateformat = new RelativeDateFormat(minute.getFirstMillisecond());
			relativedateformat.setSecondFormatter(new DecimalFormat("00"));
			dateaxis.setDateFormatOverride(relativedateformat);
			/**/

			/*-
			// set the plot's axes to display integers
			TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
			NumberAxis domain = (NumberAxis) plot.getDomainAxis();
			domain.setStandardTickUnits(ticks);
			NumberAxis range = (NumberAxis) plot.getRangeAxis();
			range.setStandardTickUnits(ticks);

			// render shapes and lines
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
			plot.setRenderer(renderer);
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);

			// set the renderer's stroke
			Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
			renderer.setBaseOutlineStroke(stroke);

			// label the points
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(2);
			XYItemLabelGenerator generator = new StandardXYItemLabelGenerator(
					StandardXYItemLabelGenerator.DEFAULT_ITEM_LABEL_FORMAT, format, format);
			renderer.setBaseItemLabelGenerator(generator);
			renderer.setBaseItemLabelsVisible(true);
			/**/

			chartPanel = new ChartPanel(chart, false);

			//Save memory consumption
			chartPanel.getChartRenderingInfo().setEntityCollection(null);

			//For paper chart generation square charts are prettiest
			chartPanel.setMaximumSize(new Dimension(500, 500));
			chartPanel.setMinimumSize(new Dimension(500, 500));

			//We created the chart, now update it with the latest data
			updateData(true);
		}
		return chartPanel;
	}

}
