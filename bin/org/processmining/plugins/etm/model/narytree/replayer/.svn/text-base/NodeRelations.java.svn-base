package org.processmining.plugins.etm.model.narytree.replayer;

import org.processmining.plugins.etm.model.narytree.NAryTree;

public class NodeRelations {

	
	private final NAryTree tree;
	private final int[][] relations;

	public NodeRelations(NAryTree tree) {
		this.tree = tree;
		
		this.relations =new int[tree.size()][tree.size()];
		
		fillRelations(0);
		
	}
	
	private void fillRelations(int node) {
		int i=node+1;
		while (i< tree.size()) {
			
			
			i= tree.getNextFast(i);
		}
	}
}
