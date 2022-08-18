package org.processmining.plugins.etm.ui.visualization.narytree;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.deckfour.xes.classification.XEventClasses;
import org.jgraph.JGraph;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;

// FIXME check all class contents
public class TreeVisualization {

	@Plugin(
			name = "Visualize Process Tree",
				returnLabels = { "Visualized Process Tree" },
				returnTypes = { JComponent.class },
				parameterLabels = { "Process Tree" },
				userAccessible = false)
	@Visualizer
	public static JComponent visualize(PluginContext context, NAryTree tree) {
		//FIXME Improve this by not returning a JComponent but a ProMJGraph such that we get the export and scroll functions

		XEventClasses classes = TreeUtils.getXEventClassesFromConnection(context, tree);

		JGraph jGraph = JGraphVisualisation.convertToJGraph(tree, classes);
		JFrame frame = new JFrame();
		frame.getContentPane().add(new JScrollPane(jGraph));
		return (JComponent) frame.getContentPane();
	}
}
