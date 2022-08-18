package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.util.RelativeDateFormat;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

public class TimeSeriesPlot extends AbstractParetoFrontChartNavigator {

	private static final long serialVersionUID = 1L;

	/**
	 * Reference to the Pareto Front object
	 */
	private ParetoFront paretoFront;

	/**
	 * The chart panel, that is placed on the contentsPanel
	 */
	private ChartPanel chartPanel;

	private TimeSeries series;

	public TimeSeriesPlot(ParetoFront front, LiveETMParetoVisualization liveETMParetoVisualization) {
		super("Pareto front size over time", liveETMParetoVisualization.paretoVis.navigationPanel);
		this.paretoFront = front;

		initializeContentsPanel();
		updateData(true);

		setContentSize(new Dimension(500, 500));
	}

	public void updateData(boolean updateVis) {

		//TODO we're ignoring the updateVis boolean since no addOrUpdate method takes a notify boolean and replicating addOrUpdate is too difdicult
		series.addOrUpdate(new Second(), paretoFront.size());
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

			series = new TimeSeries("Pareto front size");
			TimeSeriesCollection dataset = new TimeSeriesCollection(series);

			final JFreeChart chart = ChartFactory.createTimeSeriesChart("Pareto front size over time", "Time",
					"Pareto front size", dataset, false, true, false);
			final XYPlot plot = chart.getXYPlot();

			plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			/*-
			ValueAxis axis = plot.getDomainAxis();
			axis.setAutoRange(true);
			//axis.setFixedAutoRange(60000.0); // 60 seconds
			axis = plot.getRangeAxis();
			axis.setRange(0.0, 200.0);
			/**/

			/*-
			// set chart background
			chart.setBackgroundPaint(Color.white);

			// set a few custom plot features
			plot.setBackgroundPaint(new Color(0xffffe0));
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.lightGray);
			plot.setRangeGridlinePaint(Color.lightGray);
			/**/

			/*-*/
			//Specific date axis
			DateAxis dateaxis = (DateAxis) plot.getDomainAxis();
			Date now = new Date();
			RelativeDateFormat relativedateformat = new RelativeDateFormat(now);
			relativedateformat.setShowZeroDays(false);
			relativedateformat.setSecondFormatter(new DecimalFormat("00"));
			dateaxis.setDateFormatOverride(relativedateformat);
			//plot.setRangeAxis(dateaxis);
			/**/
			dateaxis.setAutoRange(true);
			//dateaxis.setMinimumDate(now);

			plot.getRangeAxis().setAutoRange(true);

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

			chartPanel.setMaximumSize(new Dimension(500, 500));
			chartPanel.setMinimumSize(new Dimension(500, 500));

			//Save memory consumption
			chartPanel.getChartRenderingInfo().setEntityCollection(null);

			//We created the chart, now update it with the latest data
			updateData(true);
		}
		return chartPanel;
	}

}
