package org.processmining.plugins.etm.ui.visualization.narytree;


import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.io.Serializable;

import org.jgraph.JGraph;
import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphContext;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

@SuppressWarnings("serial")
public class OperatorView extends VertexView{

	protected static OperatorRenderer renderer = new OperatorRenderer();
	
	public OperatorView(){
		super();
	}
	
	public OperatorView(Object arg0){
		super(arg0);
	}
	
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public Point2D getPerimeterPoint(EdgeView edge, Point2D source, Point2D p){
		if(getRenderer() instanceof OperatorRenderer){
			return ((OperatorRenderer)getRenderer()).getPerimeterPoint(this, source, p);
		}
		else{
			return super.getPerimeterPoint(edge, source, p);
		}
		
	}
	
	public CellHandle getHandle(GraphContext arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unused")
	public static class OperatorRenderer extends VertexRenderer implements CellViewRenderer, Serializable {
		
		public void paint(Graphics g){
			// lets draw a circle
			g.setColor(this.bordercolor);
			g.drawOval(0, 0, this.getWidth() - 1, this.getHeight() - 1);
			
			
			g.setColor(Color.BLACK);
			g.drawString(this.getText(), (this.getWidth() - 7) / 2 - this.getText().length() / 2, (this.getHeight() + 7) / 2);
		}
		
		public Component getRendererComponent(JGraph graph, OperatorView view, boolean selected, boolean focus, boolean preview) {
			CellViewRenderer cvr = view.getRenderer();
			if(cvr != null){
				return cvr.getRendererComponent(graph, view, selected, focus, preview);
			}
			return null;
		}

		public Point2D getPerimeterPoint(VertexView view, Point2D source, Point2D p){
			/*
			 * TODO: assumption source is source of edge, point is center of circle???
			 */
			return this.getLocation();
			//return super.getPerimeterPoint(view, source, p);
		}
		
	}
}
