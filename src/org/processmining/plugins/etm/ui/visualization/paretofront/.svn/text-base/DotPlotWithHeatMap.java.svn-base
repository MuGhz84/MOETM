package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.fitness.metrics.EditDistanceWrapperRTEDAbsolute;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Extension of the two dim dot plot where a third dimension is encoded in the
 * color of the dots
 * 
 * @author jbuijs
 * 
 */
public class DotPlotWithHeatMap extends TwoDimDotPlot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The currently set Z (color) dimension
	 */
	protected TreeFitnessInfo zDim;

	protected ProMComboBox zDimensionDropdown;

	public DotPlotWithHeatMap(ParetoFront front, ParetoVisualization paretoVisualization) {
		super("Two dimensional heat map", front, paretoVisualization);

		//TODO select a dimension that is present in the front, this one not always is
		zDim = EditDistanceWrapperRTEDAbsolute.info;

		updateChart();

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridy = 4;
		c.gridx = 0;
		getContentPanel().add(SlickerFactory.instance().createLabel("Dimension for color: "), c);
		c.gridx = 1;
		getContentPanel().add(getzDimensionDropdown(), c);

		this.setContentSize(new Dimension((int) (chartWidth * 1.5), chartHeigth + 3 * 30));
	}

	public void updateData(boolean updateVis) {
		if (updateVis) {
			GenericDotPlotChart.updateData(getChartPanel(), paretoFront, xDim, yDim, zDim);
		}
	}

	/**
	 * Updates the XYPlot to include a paintscale etc.
	 */
	private void updateChart() {
		//From http://code.google.com/p/swing-ui-hxzon/source/browse/trunk/jfreechart/org/jfree/chart/demo/XYShapeRendererDemo1.java
		XYShapeRenderer xyshaperenderer = new XYShapeRenderer();
		//It could be that the constructor did not set the zDim yet, then use the dummy value of 1

		PaintScale paintScale;
		if (zDim == null || paretoFront.getBest(zDim) == null) {
			paintScale = new GrayPaintScale(0D, 1D);
		} else {
			double bestValue = registry.getFitness(paretoFront.getBest(zDim)).fitnessValues.get(zDim);
			double worstValue = registry.getFitness(paretoFront.getWorst(zDim)).fitnessValues.get(zDim);
			paintScale = zDim.isNatural() ? new GrayPaintScale(worstValue, bestValue * 1.1) : new GrayPaintScale(
					bestValue, worstValue * 1.1);
		}
		xyshaperenderer.setPaintScale(paintScale);

		//Smaller dots
		Shape dot = new Ellipse2D.Double(0, 0, 5.0, 5.0);
		xyshaperenderer.setSeriesShape(1, dot);
		xyshaperenderer.setSeriesShape(2, dot);
		//And for elements on the subfront:
		Shape rectangle = new Rectangle(5, 5);
		xyshaperenderer.setSeriesShape(0, rectangle);

		XYPlot xyplot = chartPanel.getChart().getXYPlot();
		xyplot.setDomainPannable(true);
		xyplot.setRangePannable(true);
		xyplot.setRenderer(xyshaperenderer);

		JFreeChart jfreechart = chartPanel.getChart();
		jfreechart.removeLegend();
		NumberAxis numberaxis2 = new NumberAxis(zDim == null ? "-" : zDim.getName());
		numberaxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		PaintScaleLegend paintscalelegend = new PaintScaleLegend(paintScale, numberaxis2);
		paintscalelegend.setPosition(RectangleEdge.RIGHT);
		paintscalelegend.setMargin(4D, 4D, 40D, 4D);
		paintscalelegend.setAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
		//Make sure there is only one subtitle
		if (jfreechart.getSubtitleCount() > 0) {
			jfreechart.removeSubtitle(jfreechart.getSubtitle(0));
		}
		//Legend:
		jfreechart.addSubtitle(paintscalelegend);

		JFreeChartCustomThemes.getPrintTheme().apply(jfreechart);

		//We created the chart, now update it with the latest data
		updateData(true);
	}

	/*-*/
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
	}/**/

	/**
	 * @return the zDimensionDropdown
	 */
	@SuppressWarnings("unchecked")
	public ProMComboBox getzDimensionDropdown() {
		if (zDimensionDropdown == null) {
			ProMComboBox combobox = new ProMComboBox(paretoFront.getDimensions());

			combobox.setRenderer(new TreeFitnessInfo.TreeFitnessInfoComboboxRenderer(combobox.getRenderer()));

			combobox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Object item = e.getItem();
						if (item instanceof TreeFitnessInfo) {
							TreeFitnessInfo newDim = (TreeFitnessInfo) item;
							//Set the new z dimension axis
							zDim = newDim;
							//getChartPanel().getChart().getXYPlot().getDomainAxis().setLabel(xDim.getName());
							try {
								if (getChartPanel().getChart().getSubtitleCount() > 0) {
									PaintScaleLegend paintScaleLegend = (PaintScaleLegend) getChartPanel().getChart()
											.getSubtitle(0);
									paintScaleLegend.getAxis().setLabel(zDim.getName());
								}
							} catch (ClassCastException cce) {
								//nothing
							}
							updateChart();
							updateData(true);
							//Redraw the crossbars
							updateSelectedModel(paretoVisualization.currentTree);
						}
					}

				}
			});

			zDimensionDropdown = combobox;
		}
		return zDimensionDropdown;
	}

}
