package org.processmining.plugins.etm.ui.visualization.paretofront;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jfree.chart.ChartTheme;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.widgets.ProMScrollContainer;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.plugins.etm.fitness.TreeFitnessInfo;
import org.processmining.plugins.etm.fitness.metrics.ParetoFitnessEvaluator;
import org.processmining.plugins.etm.model.ParetoFront;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.connections.NAryTreeToXEventClassesConnection;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.visualization.bpmn.BPMNVisualization;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

// FIXME check all class contents
// FIXME Test Class thoroughly
/**
 * Visualizer for a {@link ParetoFront} object (including ProM
 * {@link Visualizer} annotation etc.). Extends ProMScrollablePanel such that it
 * can be included as a visual component, with additional features for updating
 * etc. In itself consists of 2 visualizations: of the model in the front and of
 * a collection of 'navigators' allowing to inspect the front and select other
 * models in the front.
 * 
 * @author jbuijs
 * 
 */
public class ParetoVisualization extends ProMSplitPane {
	//TODO make way more general, e.g. a front of models and a collection of navigators for the front (irrespective of the type of model).
	//TODO add methods to add/remove navigators which syncs this with the visualization (e.g. in GUI allow add/remove of navigators).

	private static final long serialVersionUID = 1L;

	/**
	 * The model visualization panel
	 */
	private JScrollPane treeVisScrollPane;

	private JPanel treePanel;

	/**
	 * The navigation panel, containing all navigators
	 */
	protected ProMScrollContainer navigationPanel;

	/**
	 * The current model shown (such that navigators can show this)
	 */
	protected NAryTree currentTree;

	/**
	 * The {@link ParetoFront} we're showing.
	 */
	protected ParetoFront paretoFront;

	/**
	 * A list of currently active navigators
	 */
	private List<AbstractParetoFrontNavigator> navigators = new ArrayList<AbstractParetoFrontNavigator>();

	/**
	 * A small JPanel shown below the tree with some basic stats
	 */
	private JPanel controlpanel;

	private JLabel treeStatsOverallFitnessLabel;

	private JButton printParetoFrontButton;

	private JButton popoutTreeButton;

	private JButton popoutProcessTreeButton;

	private PluginContext context;

	private JLabel paretoFrontSizeLabel;

	private ChartTheme chartTheme;

	@Plugin(
			name = "Visualize Pareto Front of Process Trees",
				returnLabels = { "Visualized Pareto Front of Process Trees" },
				returnTypes = { JComponent.class },
				parameterLabels = { "Pareto Front" },
				userAccessible = true)
	@Visualizer
	public static JComponent visualize(final PluginContext context, ParetoFront paretoFront) {
		return new ParetoVisualization(paretoFront, context);
	}

	public ParetoVisualization(ParetoFront paretoFront, final PluginContext context) {
		this(paretoFront, context, new ArrayList<AbstractParetoFrontNavigator>());
	}

	public ParetoVisualization(ParetoFront paretoFront, final PluginContext context,
			List<AbstractParetoFrontNavigator> additionalNavigators) {
		this(paretoFront, additionalNavigators);
		this.context = context;
	}

	public ParetoVisualization(ParetoFront paretoFront) {
		this(paretoFront, new ArrayList<AbstractParetoFrontNavigator>());
	}

	public ParetoVisualization(ParetoFront paretoFront, List<AbstractParetoFrontNavigator> additionalNavigators) {
		super();

		this.paretoFront = paretoFront;

		this.setChartTheme(JFreeChartCustomThemes.getPrintTheme());
		//		this.setChartTheme(JFreeChartCustomThemes.getUITopiaTheme());

		if (this.paretoFront.getFront().size() == 0) {
			this.currentTree = null;
		} else {
			this.currentTree = this.paretoFront.getFront().iterator().next();
		}

		treeVisScrollPane = new JScrollPane();
		treeVisScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		treeVisScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(treeVisScrollPane, SlickerColors.COLOR_BG_1, SlickerColors.COLOR_BG_1,
				SlickerColors.COLOR_BG_1);

		//		this.setLayout(new BorderLayout());
		//		this.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);

		navigationPanel = new ProMScrollContainer();
		//		navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.Y_AXIS));
		//		navigationPanel.setLayout(new GridBagLayout());
		//		navigationPanel.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);

		//navigationScrollPane = new JScrollPane(navigationPanel);
		//		this.add(navigationPanel, BorderLayout.EAST);
		this.setRightComponent(navigationPanel);

		//Add a navigator for each dimension
		for (int i = 0; i < this.paretoFront.getDimensions().length; i++) {
			TreeFitnessInfo dimension = this.paretoFront.getDimensions()[i];
			ParetoFrontDimensionNavigator dimNav = new ParetoFrontDimensionNavigator(this, dimension);
			navigators.add(dimNav);
		}

		//Add the 2D scatter plot
		navigators.add(new TwoDimDotPlot(this.paretoFront, this));

		//Add the 2D scatter plot with 'heat map' functionality
		navigators.add(new DotPlotWithHeatMap(this.paretoFront, this));

		//Grid dot plot has issues with size
		//navigators.add(new GridDotPlots(paretoFront, this));

		//The spider web chart
		//navigators.add(new SpiderChart(paretoFront, this));

		//add histogram plots for each dimension, after the list of navigators
		for (int i = 0; i < this.paretoFront.getDimensions().length; i++) {
			TreeFitnessInfo dimension = this.paretoFront.getDimensions()[i];
			DimensionBarChart histogram = new DimensionBarChart(this.paretoFront, this, dimension);
			navigators.add(histogram);
		}

		//Add a histogram for the pareto front fitness too
		navigators.add(new DimensionBarChart(this.paretoFront, this, ParetoFitnessEvaluator.info));

		navigators.addAll(additionalNavigators);

		addNavigators(navigators);

		//initialize with random tree from the Front
		updateTree(currentTree);

		/*-* /
		 //Provide minimum sizes for the two components in the split pane
		 Dimension minimumSize = new Dimension(100, 50);
		 treeVisScrollPane.setMinimumSize(minimumSize);
		 navigationScrollPane.setMinimumSize(minimumSize);

		 contents = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
		 //contents.setLayout(new BoxLayout(contents, BoxLayout.LINE_AXIS));

		 contents.add(treeVisScrollPane);
		 contents.add(navigationScrollPane);

		 contents.setDividerLocation(.5);

		 this.add(contents, BorderLayout.CENTER);
		 /**/
		this.setLeftComponent(getTreePanel());
		//this.add(getControlpanel(), BorderLayout.SOUTH);
	}

	private Component getTreePanel() {
		if (treePanel == null) {
			treePanel = SlickerFactory.instance().createRoundedPanel();

			treePanel.setLayout(new BorderLayout());

			treePanel.add(getControlpanel(), BorderLayout.NORTH);
			treePanel.add(treeVisScrollPane, BorderLayout.CENTER);
		}
		return treePanel;
	}

	public synchronized void addNavigators(List<AbstractParetoFrontNavigator> navigators2) {
		//Now show all navigators in the list
		Iterator<AbstractParetoFrontNavigator> it = navigators2.iterator();
		//for (AbstractParetoFrontNavigator navigator : navigators2) {
		while (it.hasNext()) {
			AbstractParetoFrontNavigator navigator = it.next();
			if (navigator instanceof AbstractParetoFrontChartNavigator) {
				AbstractParetoFrontChartNavigator chartNav = (AbstractParetoFrontChartNavigator) navigator;
				chartNav.applyChartTheme(chartTheme);
			}

			navigationPanel.addChild(navigator);
			if (!navigators.contains(navigator)) {
				navigators.add(navigator);
			}
		}
	}

	private JPanel getControlpanel() {
		if (controlpanel == null) {
			controlpanel = SlickerFactory.instance().createRoundedPanel();
			controlpanel.add(getPopoutProcessTreeButton());
			controlpanel.add(getPopoutTreeButton());
			controlpanel.add(getPrintParetoFrontButton());
			controlpanel.add(getParetoFrontSizeLabel());
			controlpanel.add(getTreeStatsOverallFitnessLabel());
		}
		return controlpanel;
	}

	private JButton getPrintParetoFrontButton() {
		if (printParetoFrontButton == null) {
			printParetoFrontButton = SlickerFactory.instance().createButton("Push Pareto front to ProM Workspace");
			printParetoFrontButton.setToolTipText("Provides the current Pareto front as an object in ProM.");

			printParetoFrontButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					String string = paretoFront.toString();
					System.out.println(string);
					StringSelection stringSelection = new StringSelection(string);
					Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
					clpbrd.setContents(stringSelection, null);

					//Also push out to ProM context
					context.getProvidedObjectManager().createProvidedObject("Pareto front from running ETM instance",
							paretoFront, ParetoFront.class, context);
					//And make favorite if we're in the GUI
					if (context instanceof UIPluginContext) {
						UIPluginContext uiPluginContext = (UIPluginContext) context;
						uiPluginContext.getGlobalContext().getResourceManager().getResourceForInstance(paretoFront)
								.setFavorite(true);
					}

				}
			});
		}
		return printParetoFrontButton;
	}

	private JButton getPopoutTreeButton() {
		if (popoutTreeButton == null) {
			popoutTreeButton = SlickerFactory.instance().createButton("Get Tree String");

			popoutTreeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					//Print tree string to console and put on clipboard
					String string = TreeUtils.toString(currentTree, paretoFront.getRegistry().getEventClasses());

					//For debug purposes, add behavior counter to string
					//string += "\n" + paretoFront.getRegistry().getFitness(currentTree).behaviorCounter.toString();

					System.out.println(string);
					StringSelection stringSelection = new StringSelection(string);
					Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
					clpbrd.setContents(stringSelection, null);

					//Also push out to ProM context
					context.getProvidedObjectManager().createProvidedObject("Process Tree from running ETM instance",
							currentTree, NAryTree.class, context);

					//Connect to XEventClasses to have readible names
					context.getConnectionManager().addConnection(
							new NAryTreeToXEventClassesConnection(currentTree, paretoFront.getRegistry()
									.getEventClasses()));
					//And make favorite if we're in the GUI
					if (context instanceof UIPluginContext) {
						UIPluginContext uiPluginContext = (UIPluginContext) context;
						uiPluginContext.getGlobalContext().getResourceManager().getResourceForInstance(currentTree)
								.setFavorite(true);
					}
				}
			});
		}
		return popoutTreeButton;
	}

	private JButton getPopoutProcessTreeButton() {
		if (popoutProcessTreeButton == null) {
			popoutProcessTreeButton = SlickerFactory.instance().createButton("Push Process Tree to ProM Workspace");
			popoutProcessTreeButton.setToolTipText("Provides the current Process Tree as an object in ProM.");

			popoutProcessTreeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					ProcessTree processTree = NAryTreeToProcessTree.convert(
							paretoFront.getRegistry().getEventClasses(),
							currentTree,
							"Process Tree obtained from ETM run <BR> Translated from: "
									+ TreeUtils.toString(currentTree, paretoFront.getRegistry().getEventClasses()));

					NAryTreeToProcessTree.addFitnessProperties(processTree, currentTree, paretoFront.getRegistry());

					try {
						Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
						Transferable transferable = new StringSelection(TreeUtils.toString(currentTree, paretoFront
								.getRegistry().getEventClasses()));
						clipboard.setContents(transferable, null);
					} catch (HeadlessException he) {
						//do nothing if headless...
					}

					//Also push out to ProM context
					context.getProvidedObjectManager().createProvidedObject("Process Tree from running ETM instance",
							processTree, ProcessTree.class, context);

					//And make favorite if we're in the GUI
					if (context instanceof UIPluginContext) {
						UIPluginContext uiPluginContext = (UIPluginContext) context;
						uiPluginContext.getGlobalContext().getResourceManager().getResourceForInstance(processTree)
								.setFavorite(true);
					}
				}
			});
		}
		return popoutProcessTreeButton;
	}

	private JLabel getParetoFrontSizeLabel() {
		if (paretoFrontSizeLabel == null) {
			paretoFrontSizeLabel = SlickerFactory.instance().createLabel("Pareto Front size: " + paretoFront.size());
			paretoFrontSizeLabel.setToolTipText("Number of process trees currently in the Pareto front.");
		}
		return paretoFrontSizeLabel;
	}

	private JLabel getTreeStatsOverallFitnessLabel() {
		if (treeStatsOverallFitnessLabel == null) {
			treeStatsOverallFitnessLabel = SlickerFactory.instance().createLabel("No tree selected yet");
			treeStatsOverallFitnessLabel
					.setToolTipText("<html>The Pareto fitness evaluates the `uniqueness' of the tree, <BR />"
							+ "f.i. 0 means it is at one of the extremes for at least one dimension.</html>");
			updateTreeStatsPane();
		}
		return treeStatsOverallFitnessLabel;
	}

	private void updateTreeStatsPane() {
		if (currentTree != null) {
			getTreeStatsOverallFitnessLabel().setText(
					String.format("Pareto Fitness: %2.6f", paretoFront.getRegistry().getOverallFitness(currentTree)));
		}
	}

	/**
	 * Updates the information on the front, but not the current tree
	 */
	public void updateFront(boolean updateVis) {
		for (AbstractParetoFrontNavigator nav : navigators) {
			nav.updateData(updateVis);
		}

		getParetoFrontSizeLabel().setText("Pareto Front size: " + paretoFront.size());

		this.revalidate();
	}

	/**
	 * Changes the currently shown tree to the provided one
	 * 
	 * @param tree
	 */
	public void updateTree(NAryTree tree) {
		//If the provided tree is null then don't update, that would be stupid
		if (tree == null)
			return;

		//Only if the new tree is really different
		if (!(tree == currentTree)) {
			//First things first: lock the new tree
			paretoFront.lockTree(tree);
			//release lock of current tree
			paretoFront.unlockTree(currentTree);
			//update current tree reference
			currentTree = tree;

			//Update current tree visualization
			//treeVisScrollPane.setViewportView(JGraphVisualisation.convertToJGraph(currentTree, paretoFront.getRegistry().getEventClasses()));
			//JGraph jGraph = JGraphVisualisation.convertToJGraph(tree, classes);
			/*-*/
			BPMNVisualization vis = new BPMNVisualization();
			treeVisScrollPane.setViewportView(vis.visualize(null,
					NAryTreeToProcessTree.convert(currentTree, paretoFront.getRegistry().getEventClasses())));
			/**/
			updateTreeStatsPane();

			for (AbstractParetoFrontNavigator nav : navigators) {
				nav.updateSelectedModel(currentTree);
			}
		}
	}

	/**
	 * @return the chartTheme
	 */
	public ChartTheme getChartTheme() {
		return chartTheme;
	}

	/**
	 * @param chartTheme
	 *            the new chartTheme to set which is automatically applied to
	 *            all charts
	 */
	public void setChartTheme(ChartTheme chartTheme) {
		this.chartTheme = chartTheme;
		for (AbstractParetoFrontNavigator navigator : navigators) {
			if (navigator instanceof AbstractParetoFrontChartNavigator) {
				AbstractParetoFrontChartNavigator chartNav = (AbstractParetoFrontChartNavigator) navigator;
				chartNav.applyChartTheme(chartTheme);
			}
		}
	}

}
