package org.processmining.plugins.etm.ui.visualization.narytree;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;

import org.deckfour.xes.classification.XEventClasses;
import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.processmining.plugins.etm.model.narytree.NAryTree;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

// FIXME Check all class contents
// FIXME TEST implementation (only made the red things go away for NAryTree use,
// didn't test)
public class JGraphVisualisation {

	public static Vector<DefaultGraphCell> vcells = new Vector<DefaultGraphCell>();
	public static HashMap<Integer, Integer> ReversedEdges = new HashMap<Integer, Integer>();
	public static DefaultGraphCell[] cells;

	public static JGraph convertToJGraph(NAryTree t) {
		return convertToJGraph(t, null);
	}

	public static JGraph convertToJGraph(NAryTree t, XEventClasses classes) {
		GraphModel model = new DefaultGraphModel();
		GraphLayoutCache view = new GraphLayoutCache(model, new MyCellViewFactory());

		JGraph graph = new JGraph(model, view);

		cells = new DefaultGraphCell[t.size() * 2 - 1];

		DepthFirstParse(t, classes, 0, 0);
		graph.getGraphLayoutCache().insert(cells);

		Object[] roots = new Object[1];
		roots[0] = cells[0];
		JGraphFacade facade = new JGraphFacade(graph, roots);
		/*
		 * JGraphLayout layout = new JGraphTreeLayout();
		 * ((JGraphTreeLayout)layout).setAlignment(SwingConstants.CENTER);
		 * ((JGraphTreeLayout)layout).setOrientation(SwingConstants.NORTH);
		 * ((JGraphTreeLayout)layout).setCombineLevelNodes(true);
		 */
		JGraphLayout layout = new JGraphHierarchicalLayout(true);
		((JGraphHierarchicalLayout) layout).setOrientation(SwingConstants.NORTH);
		((JGraphHierarchicalLayout) layout).setCompactLayout(true);

		layout.run(facade);
		Map<?, ?> nested = facade.createNestedMap(true, true);
		graph.getGraphLayoutCache().edit(nested);
		graph.setAntiAliased(true);

		return graph;
	}

	private static int DepthFirstParse(NAryTree tree, XEventClasses classes, int n, int freePos) {
		DefaultGraphCell dgc = ConvertNode(tree, classes, n);
		int mypos = freePos;
		cells[mypos] = dgc;
		int childpos = freePos + 1;
		int nextFreePos = freePos + 1;

		for (int c = 0; c < tree.nChildren(n); c++) {
			childpos = nextFreePos;
			nextFreePos = DepthFirstParse(tree, classes, tree.getChildAtIndex(n, c), nextFreePos);

			DefaultPort port0 = new DefaultPort();
			GraphConstants.setOffset(port0.getAttributes(),
					new Point2D.Double((GraphConstants.PERMILLE / (tree.nChildren(n) + 1)) * ((c) + 1),
							GraphConstants.PERMILLE));
			cells[mypos].add(port0);

			DefaultPort port1 = new DefaultPort();
			GraphConstants.setOffset(port1.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
			cells[childpos].add(port1);

			//FIXME might introduce edge with no target (in case of leafs?)
			TreeNode source = cells[mypos].getChildAt(cells[mypos].getChildCount() - 1);
			TreeNode target = cells[childpos].getChildAt(cells[childpos].getChildCount() - 1);
			if (source != null && target != null) {
				DefaultEdge edge = new DefaultEdge();

				edge.setSource(source);
				edge.setTarget(target);

				cells[nextFreePos] = edge;

				int arrow = GraphConstants.ARROW_CLASSIC;
				GraphConstants.setLineEnd(edge.getAttributes(), arrow);
				GraphConstants.setEndFill(edge.getAttributes(), true);

				nextFreePos++;
			} else {
				System.out.println("DETECTION");
			}
		}
		return nextFreePos;
	}

	/**
	 * @param n
	 *            converting a node into a cell
	 * @return the node <b>n<\b> converted to a cell
	 */
	private static DefaultGraphCell ConvertNode(NAryTree tree, XEventClasses classes, int node) {
		if (tree.isLeaf(node)) {

			short type = tree.getType(node);
			String typeString;
			if (type == NAryTree.TAU) {
				typeString = "TAU";
			} else if (classes != null) {
				typeString = classes.getByIndex(type).toString();
			} else {
				typeString = "" + type;
			}

			DefaultGraphCell cell = new DefaultGraphCell("<html>" + typeString + "</html>");
			GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(0, 0, 40, 40));
			GraphConstants.setAutoSize(cell.getAttributes(), true);
			GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
			return cell;
		} else {
			//Set the operator label
			String label = "";
			switch (tree.getType(node)) {
				case NAryTree.SEQ :
					label = "->";
					//					label = "<html>&rarr;</html>";
					break;
				case NAryTree.REVSEQ :
					label = "<-";
					//					label = "<html>&rarr;</html>";
					break;
				case NAryTree.XOR :
					label = "X";
					break;
				case NAryTree.AND :
					label = "/\\";
					break;
				case NAryTree.OR :
					label = "\\/";
					break;
				case NAryTree.ILV :
					label = "<->";
					break;
				case NAryTree.LOOP :
					label = "L";
					break;
				default :
					label = "$%@$";
					break;
			}

			int width = 20;
			int height = 20;
			//int pythagoras = (int) Math.sqrt(Math.pow(width / 2, 2) + Math.pow(height / 2, 2));
			OperatorVertex cell = new OperatorVertex(label);
			GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(0, 0, width, height));
			//GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(0, 0, pythagoras, pythagoras));
			//GraphConstants.setSize(cell.getAttributes(), new Dimension(width, height));
			GraphConstants.setAutoSize(cell.getAttributes(), false);
			GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
			return cell;
		}
	}
}
