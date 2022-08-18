package org.processmining.plugins.etm.ui.visualization.narytree;


import org.jgraph.graph.DefaultGraphCell;



@SuppressWarnings("serial")
public class OperatorVertex extends DefaultGraphCell{
	
	public boolean omittable;
	
	public OperatorVertex(){
		super();
	}
	
	public OperatorVertex(Object cell) {
		super(cell);
	}

}
