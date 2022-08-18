package org.processmining.plugins.etm.ui.visualization.narytree;


import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

@SuppressWarnings({ "unused", "serial" })
public class MyCellViewFactory extends DefaultCellViewFactory implements CellViewFactory{

	public CellView createView(GraphModel model, Object cell) {
		CellView view = null;
		if(model.isPort(cell)){
			view = createPortView(cell);
		}
		else if(model.isEdge(cell)){
			view = createEdgeView(cell);
		}
		else{
			view = createVertexView(cell);
		}
		
		return view;
	}

	protected VertexView createVertexView(Object cell) {
		if(cell instanceof OperatorVertex){
			return new OperatorView(cell);
		}
		return new VertexView(cell);
	}

	protected EdgeView createEdgeView(Object cell) {
		return new EdgeView(cell);
	}

	protected PortView createPortView(Object cell) {
		return new PortView(cell);
	}

}
