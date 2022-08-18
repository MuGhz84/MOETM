package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class TimePerGenerationPlot extends AbstractParetoFrontChartNavigator {

	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the Pareto Front object
	 */
	private ParetoFront paretoFront;

	/**
	 * The chart panel, that is placed on the contentsPanel
	 */
	private ChartPanel chartPanel;

	private XYSeries series;

	private long lastTime;

	public TimePerGenerationPlot(ParetoFront front, LiveETMParetoVisualization liveETMParetoVisualization) {
		super("Time per generation", liveETMParetoVisualization.paretoVis.navigationPanel);
		this.paretoFront = front;

		lastTime = new Date().getTime();

		initializeContentsPanel();
		updateData(true);

		setContentSize(new Dimension(500, 500));
	}

	public void updateData(boolean updateVis) {
		//TODO we're ignoring the updateVis boolean since no addOrUpdate method takes a notify boolean and replicating addOrUpdate is too difficult
		long currentTime = new Date().getTime();
		double elapsedTime = currentTime - lastTime;
		lastTime = currentTime;
		series.addOrUpdate(paretoFront.getRegistry().getCurrentGeneration(), elapsedTime);
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

			//series = new TimeSeries("Time per generation");
			//TimeSeriesCollection dataset = new TimeSeriesCollection(series);
			series = new XYSeries("TimePerGeneration");
			XYDataset dataset = new XYSeriesCollection(series);

			final JFreeChart chart = ChartFactory.createXYLineChart("Time per generation", "Generation",
					"Elapsed Time", dataset, PlotOrientation.VERTICAL, false, true, false);
			final XYPlot plot = chart.getXYPlot();

			plot.getDomainAxis().setAutoRange(true);
			plot.getRangeAxis().setAutoRange(true);

			chartPanel = new ChartPanel(chart, false);

			chartPanel.setMaximumSize(new Dimension(500, 500));
			chartPanel.setMinimumSize(new Dimension(500, 500));

			//Save memory consumption
			chartPanel.getChartRenderingInfo().setEntityCollection(null);

			//We created the chart, now update it with the latest data
			updateData(true);

			DateAxis rangeAxis = new DateAxis();
			RelativeDateFormat rdf = new RelativeDateFormat();
			rdf.setShowZeroDays(false);
			rdf.setShowZeroHours(false);
			//Show milliseconds!
			//rdf.setSecondFormatter(new DecimalFormat("00"));
			rangeAxis.setDateFormatOverride(rdf);
			plot.setRangeAxis(rangeAxis);

			plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		}
		return chartPanel;
	}

}
