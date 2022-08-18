package org.processmining.plugins.etm.ui.visualization.paretofront;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartTheme;
import org.processmining.framework.util.ui.widgets.ProMScrollContainer;

public abstract class AbstractParetoFrontChartNavigator extends AbstractParetoFrontNavigator {

	public final int chartWidth = 500;
	public final int chartHeigth = 500;

	public AbstractParetoFrontChartNavigator(String title, ProMScrollContainer parent) {
		super(title, parent);
	}

	public AbstractParetoFrontChartNavigator(String title, ProMScrollContainer parent, boolean minimized) {
		super(title, parent, minimized);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void applyChartTheme(ChartTheme theme) {
		if (getChartPanel() != null) {
			theme.apply(getChartPanel().getChart());
		}
	}

	protected abstract ChartPanel getChartPanel();

}
