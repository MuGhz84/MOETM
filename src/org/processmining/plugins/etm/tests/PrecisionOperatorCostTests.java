package org.processmining.plugins.etm.tests;

import org.processmining.plugins.etm.fitness.metrics.PrecisionOperatorCosts;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;

public class PrecisionOperatorCostTests {

	public static void main(String[] args) {
		NAryTree tree = TreeUtils
				.fromString("AND( XOR( OR( OR( LEAF: C+complete , LEAF: G+complete , LEAF: B+complete , LEAF: D+complete , LEAF: D+complete ) , LEAF: F+complete ) , LEAF: A+complete , LEAF: D+complete , LEAF: G+complete ) , LEAF: A+complete )");

		double cost = PrecisionOperatorCosts.getCost(tree, 0);

		System.out.println("TREE: " + tree.toString());
		System.out.println("Cost: " + cost);
	}

}
