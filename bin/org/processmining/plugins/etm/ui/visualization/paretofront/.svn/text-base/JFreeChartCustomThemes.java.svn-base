package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;

public class JFreeChartCustomThemes extends StandardChartTheme {

	private static final long serialVersionUID = 1L;

	public static enum Themes {
		PRINT("Print theme"), UITOPIA("UITopia theme");

		private String text;

		private Themes(String text) {
			this.text = text;
		}

		public String toString() {
			return text;
		}
	}

	public JFreeChartCustomThemes(String name) {
		super(name);
	}

	//version 1.0.14 constructor 
	/*-
	public JFreeChartCustomThemes(String name, boolean shadow) {
		super(name, shadow);
	}/**/

	public static ChartTheme getTheme(Themes theme) {
		switch (theme) {
			case PRINT :
				return getPrintTheme();
			default :
				return getUITopiaTheme();
		}
	}

	public static ChartTheme getPrintTheme() {
		StandardChartTheme baseTheme = new StandardChartTheme("Print Theme");
		baseTheme.setChartBackgroundPaint(Color.white);
		baseTheme.setPlotBackgroundPaint(Color.white);
		//Make sure color is always black but stroking changes
		baseTheme.setDrawingSupplier(new DefaultDrawingSupplier(new Paint[] { Color.BLACK },
				new Paint[] { Color.gray }, new Stroke[] { new BasicStroke(2.0f), new BasicStroke(1.0f),
						new BasicStroke(0.5f) }, new Stroke[] { new BasicStroke(0.5f) },
				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
		return baseTheme;
	}

	public static ChartTheme getUITopiaTheme() {
		ChartTheme baseTheme = StandardChartTheme.createDarknessTheme();
		//TODO alter the theme
		return baseTheme;
	}

}
