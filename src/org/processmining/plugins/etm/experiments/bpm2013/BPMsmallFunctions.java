package org.processmining.plugins.etm.experiments.bpm2013;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.jgraph.JGraph;
import org.processmining.plugins.etm.CentralRegistry;
import org.processmining.plugins.etm.CentralRegistryConfigurable;
import org.processmining.plugins.etm.ETM;
import org.processmining.plugins.etm.experiments.CommandLineInterface;
import org.processmining.plugins.etm.experiments.StandardLogs;
import org.processmining.plugins.etm.fitness.metrics.FitnessReplay;
import org.processmining.plugins.etm.fitness.metrics.PrecisionEscEdges;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.conversion.NAryTreeToProcessTree;
import org.processmining.plugins.etm.parameters.ETMParam;
import org.processmining.plugins.ptmerge.ptmerge.PlugIn;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ptml.exporting.PtmlExportTree;
import org.processmining.processtree.visualization.tree.TreeLayoutBuilder;

public class BPMsmallFunctions {

	public static void main(String[] args) {
		//testLongAlignmentTrees();

		testPrecision();

		System.exit(0);

		/*
		 * The 4 logs used in the experiments:
		 */

		//XLog[] logs = new XLog[] { /**/BPMTestLogs.createDefaultLog(), /*- */
		//BPMTestLogs.createConfig2(), BPMTestLogs.createConfig3(), BPMTestLogs.createConfig4() /**/};

		/*
		 * For precision in configuration tests. Model: AND( XOR( A , D ) , SEQ(
		 * B, C) ) where in one log D = hidden
		 */

		/*-
		//Skip in XOR
		XLog[] logs = new XLog[] {
				LogCreator.createLog(new String[][] { { "A", "B", "C" }, { "B", "C" }, { "B", "A", "C" },
						{ "B", "C", "A" } }),
				LogCreator.createLog(new String[][] { { "A", "B", "C" }, { "D", "B", "C" }, { "B", "A", "C" },
						{ "B", "D", "C" }, { "B", "C", "A" }, { "B", "C", "D" } }) };
			/**/

		/*-
		//Hide in SEQ (AND(SEQ(A,B),SEQ(C,D))
		XLog[] logs = new XLog[] {
				LogCreator.createLog(new String[][] { { "A", "B", "C" }, { "A", "C", "B" }, { "C", "A", "B" } }),
				LogCreator
						.createLog(new String[][] { { "A", "B", "C", "D" }, { "A", "C", "B", "D" },
								{ "A", "C", "D", "B" }, { "C", "D", "A", "B" }, { "C", "A", "D", "B" },
								{ "C", "A", "B", "D" } }) };
								/**/
		XUniversalParser parser = new XUniversalParser();

		XLog[] logs = new XLog[0];
		try {
			logs = parser
					.parse(new File(
							"C:\\Users\\jbuijs\\Documents\\PhD\\Projects\\Papers\\2013-06 BPI Pareto\\data\\Case Study\\SIMPDA12_caseStudy_Anonymous.xes.gz"))
					.toArray(logs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		XLog mergedLog = logs[0];
		XLogInfo mergedLogInfo = XLogInfoFactory.createLogInfo(mergedLog);
		/**/

		//The ~perfect/goal tree:
		//NAryTree tree = perfectTree(mergedLogInfo);

		String[] strings = treeStrings();

		for (String string : strings) {

			//Temp TEST trees
			NAryTree tree = fromString(mergedLogInfo, string);

			//Evaluate!
			evaluateTree(logs, mergedLogInfo, tree);
		}
	}

	private static String[] treeStrings() {
		/*
		 * PERFECT TARGET TREES
		 */
		//Initial, not optimal, tree
		/*-* /
		String initPerfTree = "SEQ( LEAF: A+complete , AND( AND( LEAF: B+complete , SEQ( LEAF: B1+complete , LEAF: B2+complete ) ) "
				+ ", LEAF: C+complete , XOR( LEAF: D+complete , LEAF: D2+complete ) ) , XOR( LEAF: E+complete , "
				+ "LEAF: F+complete ) , LEAF: G+complete ) "
				+ "[[-, -, -, -, -, H, -, -, -, -, -, H, -, -, -, -]"
				+ "[-, -, >, -, H, -, -, -, -, -, B, -, -, -, -, -]"
				+ "[-, -, -, -, -, H, -, -, -, H, -, -, -, -, -, H]"
				+ "[-, -, -, -, H, -, -, -, -, -, -, -, -, -, -, H] ]";
		/**/

		/*-
		//All options modelled out, e.g. no config options
		String currentTestTree = "SEQ( LEAF: A+complete , XOR( AND( LEAF: B+complete , LEAF: C+complete , XOR( LEAF: D+complete , LEAF: tau ) ) , "
				+ "SEQ( LEAF: B1+complete , LEAF: B2+complete , LEAF: C+complete , LEAF: D2+complete ) , "
				+ "SEQ( LEAF: C+complete , LEAF: B+complete ) ) , "
				+ "XOR( LEAF: E+complete , LEAF: F+complete ) , XOR( LEAF: tau , LEAF: G+complete ) ) ";
		/**/

		/*-* /
		String currentTestTree = "SEQ( LEAF: A+complete , OR( LEAF: B+complete , LEAF: C+complete , LEAF: D+complete ) , " +
				"XOR( LEAF: E+complete , LEAF: F+complete ) , LEAF: G+complete )";

		String configTree = "SEQ( LEAF: A+complete , AND( SEQ( LEAF: B+complete , LEAF: B1+complete , LEAF: B2+complete ) "
				+ ", LEAF: C+complete , XOR( LEAF: D+complete , LEAF: D2+complete ) ) , XOR( LEAF: E+complete , "
				+ "LEAF: F+complete ) , LEAF: G+complete ) "
				+ "[[-, -, -, -, -, H, H, -, -, -, H, -, -, -, -]"
				+ " [-, -, >, -, H, -, -, -, -, B, -, -, -, -, -]" 
				+ " [-, -, <, -, -, H, H, -, H, -, -, -, -, -, H]"
				//+ "[-, -, -, -, H, -, -, -, -, -, -, -, -, -, -, H] " 
				+ "]";
		/**/

		/*-* /
		String currentTestTree = "SEQ( LEAF: A+complete , "
				+ "OR( AND( LEAF: B+complete , LEAF: C+complete ) , LEAF: D+complete ) , "
				+ "XOR( LEAF: E+complete , LEAF: F+complete ) , LEAF: G+complete )";

		String configTree = "SEQ( LEAF: A+complete , "
				+ "OR( AND( SEQ( LEAF: B+complete , LEAF: B1+complete , LEAF: B2+complete ) , LEAF: C+complete ) , SEQ( LEAF: D+complete , LEAF: D2+complete ) ) , "
				+ "XOR( LEAF: E+complete , LEAF: F+complete ) , LEAF: G+complete ) "
				+ "[[-, -, -, -, -, -, H, H, -, -, -, H, -, -, -, -]"
				+ " [-, -, -, >, -, H, -, -, -, -, H, -, -, -, -, -]"
				+ " [-, -, -, <, -, -, H, H, -, H, -, -, -, -, -, H]"
				+ " [-, -, -, -, -, H, -, -, -, -, -, -, -, -, -,  H] " + "]";

		String[] strings = new String[] { currentTestTree, configTree };
		/**/

		/*
		 * Precision error indicating for configuration options (e.g. hiding in
		 * sequence in parallel is bad for p).
		 */

		/*-
		//In XOR:
		String treeWithConfig = "AND( XOR( LEAF: A+complete , LEAF: D+complete ) , SEQ( LEAF: B+complete , LEAF: C+complete ) ) "
				+ "[[-,-,-,H,-,-,-][-,-,-,-,-,-,-]]";

		String treeAllNodes = "AND( XOR( LEAF: A+complete , LEAF: D+complete ) , SEQ( LEAF: B+complete , LEAF: C+complete ) ) "
				+ "[[-,-,-,-,-,-,-][-,-,-,-,-,-,-]]";

		String treeNodeRemoved = "AND( XOR( LEAF: A+complete , LEAF: tau ) , SEQ( LEAF: B+complete , LEAF: C+complete ) )"
				+ "[[-,-,-,-,-,-,-][-,-,-,-,-,-,-]]";

		String[] strings = new String[] { treeWithConfig, treeAllNodes, treeNodeRemoved };
		/**/

		/*-* /
		//In SEQ
		String treeWithConfig = "AND( SEQ( LEAF: A+complete , LEAF: B+complete ) , SEQ( LEAF: C+complete , LEAF: D+complete ) ) "
				+ "[[-,-,-,-,-,-,H][-,-,-,-,-,-,-]]";

		String treeAllNodes = "AND( SEQ( LEAF: A+complete , LEAF: B+complete ) , SEQ( LEAF: C+complete , LEAF: tau ) ) "
				+ "[[-,-,-,-,-,-,-][-,-,-,-,-,-,-]]";

		String treeNodeRemoved = "AND( SEQ( LEAF: A+complete , LEAF: B+complete ) , LEAF: C+complete ) "
				+ "[[-,-,-,-,-][-,-,-,-,-]]";

		String[] strings = new String[] { treeWithConfig, treeAllNodes, treeNodeRemoved };
		/*-*/

		String paretoLongTree = "XOR( SEQ( LOOP( LEAF: RP004 Vaststellen ontvangstbevestiging+complete , LEAF: RP003 Aanpassen ontvangstbevestiging+complete , LEAF: tau ) , LEAF: RP014 Vaststellen document 251 aanvraag vergunningvrij+complete ) , SEQ( SEQ( LOOP( LEAF: tau , OR( LEAF: RP008 Opstellen en verzenden adviesaanvraag+complete , SEQ( LEAF: RP007-3 Opstellen intern advies aanhouden Gebruik+complete , LEAF: RP007-4 Opstellen intern advies aanhouden APV/Overig+complete ) ) , LEAF: tau ) , LEAF: RP010 Bepalen noodzaak indicatie aanhouden+complete ) , XOR( LEAF: tau , XOR( SEQ( LOOP( SEQ( LOOP( LEAF: tau , LEAF: RP013 Aanpassen document 251 aanvraag vergunningvrij+complete , LEAF: tau ) , LEAF: RP014 Vaststellen document 251 aanvraag vergunningvrij+complete ) , LEAF: RP013 Aanpassen document 251 aanvraag vergunningvrij+complete , LEAF: tau ) , LEAF: RP015 Uitdraaien en verzenden document 251 vergunningvrij+complete ) , SEQ( SEQ( LEAF: RP016 Vastleggen aanhoudingsgronden+complete , LOOP( SEQ( LOOP( LEAF: RP017 Toetsen melding 295 indicatie aanhouden+complete , LEAF: RP018 Aanpassen melding 295 indicatie aanhouden+complete , LEAF: tau ) , LEAF: RP019 Vaststellen melding 295 indicatie aanhouden+complete ) , LEAF: RP018 Aanpassen melding 295 indicatie aanhouden+complete , LEAF: tau ) ) , LEAF: RP020 Uitdraaien en verzenden melding 295 aanhouden+complete ) ) ) ) )";

		String[] strings = new String[] { paretoLongTree };

		/*
		 * LONG trees
		 */

		//long:
		//String[] strings = new String[]{"LOOP( OR( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( SEQ( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7903 (Gv: 0,4524 Fr: 0,8981 Cf: 0,7083 Sm: 0,8729 Of: 0,7904 Pe: 0,5651 )"};
		//String[] strings = new String[]{"AND( LEAF: 1 , LEAF: 0)"};
		//String[] strings = new String[]{"LOOP( XOR( LEAF: 0 , SEQ( LEAF: 4 , LEAF: 7 ) ) , LEAF: 3 , LEAF: tau ) [[-, -, -, -, -, -, B, -][-, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -][-, -, -, -, H, -, B, -] ]"};

		/*-* /
		//String[] strings = new String[]{"","","","","","","","","","","","","","","","","","","",""}; 
		String[] strings = new String[] {
				"LOOP( SEQ( OR( LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, B, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,6690 (Gv: 0,4130 Fr: 0,6439 Cf: 0,6667 Sm: 0,8542 Of: 0,6690 Pe: 0,6871 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 4 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: tau , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,6825 (Gv: 0,4236 Fr: 0,7026 Cf: 0,6667 Sm: 0,8698 Of: 0,6825 Pe: 0,6100 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 7 , LEAF: 7 ) , AND( SEQ( LEAF: 1 ) ) , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 4 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, H, H, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,6859 (Gv: 0,4004 Fr: 0,7110 Cf: 0,6154 Sm: 0,8419 Of: 0,6860 Pe: 0,6105 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 3 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7295 (Gv: 0,4549 Fr: 0,7775 Cf: 0,7083 Sm: 0,8704 Of: 0,7295 Pe: 0,6107 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 8 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7526 (Gv: 0,4438 Fr: 0,8040 Cf: 0,7083 Sm: 0,8704 Of: 0,7526 Pe: 0,6325 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , LEAF: 0 ) , LOOP( LEAF: tau , LEAF: 7 , LEAF: tau ) ) , OR( LEAF: 3 , LEAF: 2 , LOOP( SEQ( LEAF: 6 ) , LEAF: tau , LEAF: tau ) ) , OR( SEQ( LEAF: 8 ) , LEAF: 7 , SEQ( LEAF: 9 ) ) ) [[-, -, -, -, -, -, H, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7605 (Gv: 0,4816 Fr: 0,8523 Cf: 0,7308 Sm: 0,9192 Of: 0,7606 Pe: 0,5510 )",
				"LOOP( SEQ( OR( OR( LEAF: 4 , LEAF: 6 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , LEAF: tau ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7632 (Gv: 0,4682 Fr: 0,8548 Cf: 0,6957 Sm: 0,8865 Of: 0,7633 Pe: 0,5616 )",
				"LOOP( OR( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( SEQ( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7903 (Gv: 0,4524 Fr: 0,8981 Cf: 0,7083 Sm: 0,8729 Of: 0,7904 Pe: 0,5651 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , XOR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 ) , OR( XOR( LEAF: 8 ) , SEQ( SEQ( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, <, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7932 (Gv: 0,4796 Fr: 0,8489 Cf: 0,6957 Sm: 0,8671 Of: 0,7933 Pe: 0,6735 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , LEAF: 0 ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, H, -, -, -, -, -, H] ]  0,7949 (Gv: 0,4915 Fr: 0,8769 Cf: 0,6522 Sm: 0,8891 Of: 0,7950 Pe: 0,6185 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, H, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,7969 (Gv: 0,4684 Fr: 0,8617 Cf: 0,6250 Sm: 0,8729 Of: 0,7970 Pe: 0,6591 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , AND( LEAF: 7 ) ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( SEQ( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8095 (Gv: 0,4524 Fr: 0,8819 Cf: 0,7200 Sm: 0,8580 Of: 0,8096 Pe: 0,6624 )",
				"LOOP( SEQ( OR( SEQ( SEQ( LEAF: 4 ) , LEAF: 7 ) , LEAF: 1 , LEAF: 0 ) , LOOP( LEAF: tau , LEAF: 7 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , XOR( LEAF: 6 ) ) , OR( XOR( LEAF: 8 ) , OR( AND( SEQ( LEAF: 9 ) ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, <, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, B, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, H, B, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8147 (Gv: 0,4421 Fr: 0,8891 Cf: 0,7308 Sm: 0,8442 Of: 0,8148 Pe: 0,6678 )",
				"LOOP( SEQ( OR( OR( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( OR( LEAF: 0 ) ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8170 (Gv: 0,4598 Fr: 0,9295 Cf: 0,6400 Sm: 0,8580 Of: 0,8172 Pe: 0,5916 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , OR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8189 (Gv: 0,4791 Fr: 0,8967 Cf: 0,7083 Sm: 0,8729 Of: 0,8191 Pe: 0,6599 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( SEQ( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8189 (Gv: 0,4791 Fr: 0,8967 Cf: 0,7083 Sm: 0,8729 Of: 0,8191 Pe: 0,6599 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( OR( LEAF: 0 ) ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8191 (Gv: 0,4598 Fr: 0,8957 Cf: 0,6400 Sm: 0,8580 Of: 0,8193 Pe: 0,6661 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, B, -][-, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8202 (Gv: 0,4789 Fr: 0,8957 Cf: 0,6250 Sm: 0,8729 Of: 0,8204 Pe: 0,6661 )",
				"LOOP( SEQ( OR( SEQ( SEQ( LEAF: 4 ) , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 7 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , XOR( LEAF: 6 ) ) , OR( XOR( LEAF: 8 ) , OR( AND( SEQ( LEAF: 9 ) ) ) ) ) [[-, -, -, -, -, -, -, -, -, -, H, -, -, -, -, <, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, H, B, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8229 (Gv: 0,4258 Fr: 0,8967 Cf: 0,7407 Sm: 0,8315 Of: 0,8230 Pe: 0,6818 )",
				"LOOP( SEQ( OR( SEQ( LEAF: 4 , LEAF: 7 ) , LEAF: 1 , OR( LEAF: 0 ) ) , LOOP( LEAF: tau , LEAF: 0 , XOR( LEAF: tau ) ) ) , OR( LEAF: 3 , LEAF: 2 , LEAF: 6 ) , OR( XOR( LEAF: 8 ) , OR( AND( LEAF: 9 ) ) ) ) [[-, -, -, -, -, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, B, -, -, -, -, -, -, H, -, -, H, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, B, -, -, B, -, -, -, -, -, -, -, -, -][-, -, -, -, -, H, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, H] ]  0,8235 (Gv: 0,4791 Fr: 0,8967 Cf: 0,6667 Sm: 0,8729 Of: 0,8237 Pe: 0,6748 )" };
				/**/

		return strings;
	}

	public static void evaluateTree(XLog[] logs, XLogInfo mergedLogInfo, NAryTree perfectTree) {
		CommandLineInterface cli = new CommandLineInterface(new String[] { "NORMAL", "C:\\temp\\" });
		ETMParam params = cli.buildETMParam(null, logs);

		ETM etmMstar = new ETM(params);

		params.getFitnessEvaluator().getFitness(perfectTree, null);

		System.out.println(TreeUtils.toString(perfectTree, mergedLogInfo.getEventClasses()));
		System.out.println(params.getCentralRegistry().getFitness(perfectTree).toString());
		System.out.println(params.getCentralRegistry().getFitness(perfectTree).behaviorCounter);
		CentralRegistryConfigurable configReg = (CentralRegistryConfigurable) params.getCentralRegistry();
		for (int c = 0; c < perfectTree.getNumberOfConfigurations(); c++) {
			NAryTree configuredTree = perfectTree.applyConfiguration(c);
			System.out.println(configReg.getRegistry(c).getFitness(configuredTree).toString());
			System.out.println(configReg.getRegistry(c).getFitness(configuredTree).behaviorCounter);
		}
	}

	public static NAryTree perfectTree(XLogInfo mergedLogInfo) {
		//Initial, not optimal, tree
		/*-* /
		String string = "SEQ( LEAF: A+complete , AND( AND( LEAF: B+complete , SEQ( LEAF: B1+complete , LEAF: B2+complete ) ) "
				+ ", LEAF: C+complete , XOR( LEAF: D+complete , LEAF: D2+complete ) ) , XOR( LEAF: E+complete , "
				+ "LEAF: F+complete ) , LEAF: G+complete ) "
				+ "[[-, -, -, -, -, H, -, -, -, -, -, H, -, -, -, -]"
				+ "[-, -, >, -, H, -, -, -, -, -, B, -, -, -, -, -]"
				+ "[-, -, -, -, -, H, -, -, -, H, -, -, -, -, -, H]"
				+ "[-, -, -, -, H, -, -, -, -, -, -, -, -, -, -, H] ]";
		/**/

		String string = "SEQ( LEAF: A+complete , AND( XOR( LEAF: B+complete , SEQ( LEAF: B1+complete , LEAF: B2+complete ) ) "
				+ ", LEAF: C+complete , XOR( LEAF: tau , LEAF: D+complete , LEAF: D2+complete ) ) , XOR( LEAF: E+complete , "
				+ "LEAF: F+complete ) , XOR( LEAF: tau , LEAF: G+complete ) ) ";

		NAryTree perfectTree = TreeUtils.fromString(string, mergedLogInfo.getEventClasses());
		/*-* /
		//4 configurations all 'not configured'
		perfectTree.addConfiguration(new Configuration(new byte[] { Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED }));
		perfectTree.addConfiguration(new Configuration(new byte[] { Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED }));
		perfectTree.addConfiguration(new Configuration(new byte[] { Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED }));
		perfectTree.addConfiguration(new Configuration(new byte[] { Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED,
				Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED, Configuration.NOTCONFIGURED }));
		/*-* /
		//Then update where necessary
		perfectTree.setNodeConfiguration(0, 5, Configuration.HIDDEN); //Hide SEQ(B1,B2)
		perfectTree.setNodeConfiguration(0, 11, Configuration.HIDDEN); //Hide D2
		//BROKEN:
		perfectTree.setNodeConfiguration(1, 4, Configuration.HIDDEN); //Conf2: Hide B
		perfectTree.setNodeConfiguration(1, 10, Configuration.BLOCKED); //disable D (e.g. always D2)
		perfectTree.setNodeConfiguration(1, 2, Configuration.SEQ); //AND to SEQ because always b1-b2-c-d 
		//BROKEN:
		perfectTree.setNodeConfiguration(2, 5, Configuration.HIDDEN); //Conf 3: hide SEQ B1 B2
		perfectTree.setNodeConfiguration(2, 9, Configuration.HIDDEN); //hide Xor D
		perfectTree.setNodeConfiguration(2, 15, Configuration.HIDDEN); //hide G
		//BROKEN:
		perfectTree.setNodeConfiguration(3, 4, Configuration.HIDDEN); //Conf 4: hide B
		perfectTree.setNodeConfiguration(3, 15, Configuration.HIDDEN); //hide G
		/**/

		return perfectTree;
	}

	private static NAryTree fromString(XLogInfo mergedLogInfo, String str) {
		NAryTree tree = TreeUtils.fromString(str, mergedLogInfo.getEventClasses());

		System.out.println("original:");
		System.out.println(str);
		System.out.println("fromString-toString:");
		System.out.println(TreeUtils.toString(tree));

		return tree;
	}

	public static void testLongAlignmentTrees() {
		XLog[] logs = new XLog[] { StandardLogs.createDefaultLog(), StandardLogs.createConfig2(),
				StandardLogs.createConfig3(), StandardLogs.createConfig4() };

		String treeStr = "OR( LEAF: G+complete , LEAF: E+complete , LEAF: C+complete , LEAF: B2+complete , OR( LEAF: B1+complete , OR( LEAF: D2+complete , LOOP( OR( LEAF: D2+complete , LEAF: E+complete , LEAF: C+complete , LEAF: B2+complete , OR( LEAF: B1+complete , OR( LOOP( LEAF: F+complete , LEAF: tau , LEAF: tau ) , LEAF: D2+complete , LEAF: tau ) ) , OR( LEAF: D+complete , LEAF: B+complete ) ) , LEAF: tau , LEAF: tau ) , LEAF: A+complete ) ) , LEAF: D+complete ) [[-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, B, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -][-, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -, -] ]";

		CentralRegistryConfigurable registry = new CentralRegistryConfigurable(XLogInfoImpl.STANDARD_CLASSIFIER,
				new Random(), logs);

		NAryTree tree = TreeUtils.fromString(treeStr, registry.getEventClasses());

		//Now calculate each configuration alignment
		for (int c = 0; c < logs.length; c++) {
			Canceller canceller = new Canceller() {

				@Override
				public boolean isCancelled() {
					return false;
				}
			};

			FitnessReplay fr = new FitnessReplay(registry, canceller, c, -1);

			System.out.println("-----------------------------------------------");
			System.out.println("Configuration " + c);

			long start, end;
			double fit;

			start = System.currentTimeMillis();
			fr.setCaching(true);
			fr.setStubborn(true);
			fr.setUseOrRows(false);
			fit = fr.getFitness(tree, null);
			end = System.currentTimeMillis();
			System.out.println("With caching:    " + (end - start) / 1000.0 + " seconds.");
			System.out.println("Stuborn sets ");
			System.out.println("Fitness:         " + fit);
			System.out.println();

			start = System.currentTimeMillis();
			fr.setCaching(true);
			fr.setStubborn(true);
			fr.setUseOrRows(true);
			fit = fr.getFitness(tree, null);
			end = System.currentTimeMillis();
			System.out.println("With caching:    " + (end - start) / 1000.0 + " seconds.");
			System.out.println("Stuborn sets + OR rows");
			System.out.println("Fitness:         " + fit);
			System.out.println();

			start = System.currentTimeMillis();
			fr.setCaching(true);
			fr.setStubborn(false);
			fr.setUseOrRows(false);
			fit = fr.getFitness(tree, null);
			end = System.currentTimeMillis();
			System.out.println("With caching:    " + (end - start) / 1000.0 + " seconds.");
			System.out.println("No stuborn sets ");
			System.out.println("Fitness:         " + fit);
			System.out.println();

			start = System.currentTimeMillis();
			fr.setCaching(false);
			fr.setStubborn(false);
			fr.setUseOrRows(false);
			fit = fr.getFitness(tree, null);
			end = System.currentTimeMillis();
			System.out.println("No caching:     " + (end - start) / 1000.0 + " seconds.");
			System.out.println("No stuborn sets ");
			System.out.println("Fitness:         " + fit);
			System.out.println();

		}
		System.out.println("Done.");
	}

	private static void testPrecision() {
		XLog log = StandardLogs.createDefaultLog();

		NAryTree tree = TreeUtils
				.fromString("SEQ( LEAF: 0 , SEQ( SEQ( LEAF: 1 , AND( LEAF: 3 , LEAF: 2 ) ) ) , XOR( LEAF: 5 , LEAF: 4 ) , OR( OR( LEAF: 6 ) ) ) ");

		CentralRegistry registry = new CentralRegistry(log, new Random());

		FitnessReplay fr = new FitnessReplay(registry, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});

		PrecisionEscEdges pe = new PrecisionEscEdges(registry);

		double frVal = fr.getFitness(tree, null);
		double peVal = pe.getFitness(tree, null);

		System.out.println("Vals: " + frVal + " and " + peVal);
	}

	public static void toCoSeNets() {
		String loggingPath = "E:\\NGridGAdl\\BPMConfMiningResults\\run01\\PTs";
		//2013-03-11 14:45 Converts NAryTrees to CoSeNets for Dennis' merge (quick code to convert discovered NAryTrees by experiments for Dennis)

		//The logs
		XLog[] logs = new XLog[] { StandardLogs.createDefaultLog(), StandardLogs.createConfig2(),
				StandardLogs.createConfig3(), StandardLogs.createConfig4() };
		XLogInfo[] infos = new XLogInfo[] { new XLogInfoImpl(logs[0], XLogInfoImpl.STANDARD_CLASSIFIER, null),
				new XLogInfoImpl(logs[1], XLogInfoImpl.STANDARD_CLASSIFIER, null),
				new XLogInfoImpl(logs[2], XLogInfoImpl.STANDARD_CLASSIFIER, null),
				new XLogInfoImpl(logs[3], XLogInfoImpl.STANDARD_CLASSIFIER, null) };

		//The five sets of four models
		NAryTree[][] trees = new NAryTree[][] {
				{ /*-NGRID10*/
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , AND( AND( LEAF: B+complete , LEAF: E+complete ) , LEAF: F+complete ) ) , LEAF: B1+complete , LEAF: B2+complete )",
										infos[0].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete , LEAF: C+complete , LEAF: D2+complete , XOR( LEAF: F+complete , SEQ( LEAF: F+complete , LEAF: G+complete ) ) ) , LEAF: B+complete , LEAF: D+complete )",
										infos[1].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( LEAF: A+complete , SEQ( LEAF: F+complete , XOR( SEQ( LEAF: B+complete , LEAF: B2+complete ) , SEQ( LEAF: B+complete , LEAF: G+complete ) ) ) )",
										infos[2].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , SEQ( LEAF: B2+complete , SEQ( XOR( LEAF: E+complete , LEAF: D2+complete ) , LEAF: C+complete ) ) ) , LEAF: D+complete )",
										infos[3].getEventClasses()) },
				{ /*-NGRID11*/
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , AND( AND( LEAF: F+complete , LEAF: B+complete ) , LEAF: E+complete ) ) , SEQ( XOR( LEAF: B1+complete , LEAF: D2+complete ) , LEAF: B2+complete ) )",
										infos[0].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , SEQ( LEAF: B2+complete , LEAF: C+complete ) ) , LEAF: D2+complete , LEAF: F+complete , XOR( LEAF: B+complete , LEAF: G+complete ) , LEAF: D+complete )",
										infos[1].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , LEAF: F+complete ) , LEAF: B+complete ) , XOR( LEAF: B2+complete , LEAF: G+complete ) ) ",
										infos[2].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , SEQ( LEAF: E+complete , LEAF: C+complete ) ) , LEAF: D+complete , XOR( LEAF: B+complete , LEAF: F+complete ) )",
										infos[3].getEventClasses()) },
				{ /*-NGRID12*/
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , AND( LEAF: E+complete , AND( LEAF: B+complete , LEAF: F+complete ) ) ) , SEQ( XOR( LEAF: D2+complete , LEAF: B1+complete ) , LEAF: B2+complete ) )",
										infos[0].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , SEQ( LEAF: C+complete , SEQ( LEAF: D2+complete , SEQ( LEAF: F+complete , LEAF: B+complete ) ) , LEAF: D+complete ) )",
										infos[1].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , LEAF: F+complete ) , LEAF: B+complete ) , XOR( LEAF: B2+complete , LEAF: G+complete ) )",
										infos[2].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , LEAF: E+complete ) , LEAF: C+complete , LEAF: D+complete , XOR( LEAF: F+complete , LEAF: B+complete ) )",
										infos[3].getEventClasses()) },
				{ /*-NGRID13*/
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , AND( AND( LEAF: B+complete , LEAF: F+complete ) , LEAF: E+complete ) ) , XOR( LEAF: D2+complete , LEAF: B1+complete ) ) , LEAF: B2+complete )",
										infos[0].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , LEAF: C+complete , LEAF: D2+complete , LEAF: F+complete , LEAF: B+complete , LEAF: D+complete )",
										infos[1].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , LEAF: F+complete ) , SEQ( LEAF: B+complete , XOR( LEAF: G+complete , LEAF: B2+complete ) ) ) ",
										infos[2].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , LEAF: E+complete ) , LEAF: C+complete , LEAF: D+complete , XOR( LEAF: F+complete , LEAF: B+complete ) )",
										infos[3].getEventClasses()) },
				{ /*-NGRID14*/
						TreeUtils
								.fromString(
										"SEQ( LEAF: A+complete , AND( LEAF: E+complete , AND( LEAF: B+complete , LEAF: F+complete ) ) , LEAF: B1+complete , LEAF: B2+complete )",
										infos[0].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( LEAF: A+complete , LEAF: B2+complete , LEAF: D2+complete , XOR( SEQ( LEAF: F+complete , SEQ( LEAF: B+complete , LEAF: D+complete ) ) , SEQ( LEAF: F+complete , LEAF: G+complete ) ) ) ",
										infos[1].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( LEAF: A+complete , SEQ( LEAF: F+complete , XOR( SEQ( LEAF: B+complete , LEAF: B2+complete ) , LEAF: G+complete ) ) )",
										infos[2].getEventClasses()),
						TreeUtils
								.fromString(
										"SEQ( SEQ( LEAF: A+complete , LEAF: B2+complete ) , LEAF: E+complete , SEQ( LEAF: D2+complete , LEAF: C+complete ) , SEQ( LEAF: D+complete , LEAF: F+complete ) , LEAF: B+complete )",
										infos[3].getEventClasses()) } };

		ProcessTree[][] processTrees = new ProcessTree[trees.length][trees[0].length];

		//Convert to PT and write to file
		for (int c = 0; c < trees.length; c++) {
			NAryTree[] treeSet = trees[c];
			for (int t = 0; t < treeSet.length; t++) {
				//write ProcessTree XML
				ProcessTree pt = NAryTreeToProcessTree.convert(infos[t].getEventClasses(), treeSet[t], "PTfromNAT-set"
						+ c + "tree" + t);

				processTrees[c][t] = pt;

				try {
					PtmlExportTree ptExport = new PtmlExportTree();
					File ptFile = new File(loggingPath + "//pt_set" + c + "_tree" + t + ".ptml");
					ptExport.exportDefault(null, pt, ptFile);
				} catch (IOException e) {
					e.printStackTrace(System.err);
					//e.printStackTrace();
				} catch (NullPointerException npe) {
					npe.printStackTrace(System.err);
				}
			}
		}

		//Merge sets together and output merge result
		for (int c = 0; c < processTrees.length; c++) {
			ProcessTree pt = processTrees[c][0];

			for (int t = 1; t < processTrees[c].length; t++) {
				try {
					PlugIn.mergeExtMap(pt, processTrees[c][t]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				PtmlExportTree ptExport = new PtmlExportTree();
				File ptFile = new File(loggingPath + "//pt_set" + c + "_Merged.ptml");
				ptExport.exportDefault(null, pt, ptFile);
			} catch (IOException e) {
				e.printStackTrace(System.err);
				//e.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace(System.err);
			}

			//TreeBPMNLayoutBuilder builder = new TreeBPMNLayoutBuilder(pt, null);
			TreeLayoutBuilder builder = new TreeLayoutBuilder(pt);
			JGraph graph = builder.getJGraph();

			//Now save the image already to a fixed dir to easy working on papers...
			try {
				//FROM JGraph manual p. 97
				//http://touchflow.googlecode.com/hg-history/75fada644b2a19c744130923cbd34747fba861a2/doc/jgraphmanual.pdf

				String imageFile = loggingPath + "//pt_set" + c + "_Merged.png";
				OutputStream out = new FileOutputStream(imageFile);
				Color bg = null; // Use this to make the background transparent
				int inset = 0;
				BufferedImage img = graph.getImage(bg, inset);
				String ext = "png";
				ImageIO.write(img, ext, out);
				out.flush();
				out.close();

				//Put on Clipboard
				/*-* /
				Toolkit tolkit = Toolkit.getDefaultToolkit();
				Clipboard clip = tolkit.getSystemClipboard();
				clip.setContents(new ImageSelection(img), null);
				/**/
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Replace with your output stream

		}
	}

	/**
	 * Instantiate the case study logs from the original files
	 * 
	 * @return
	 */
	public static ArrayList<XLog> loadCaseStudyLogs() {
		String baseDir = "C:\\Users\\jbuijs\\Documents\\PhD\\Projects\\Papers\\2013-03 BPM\\data\\CaseStudy_WABO_01_BB\\";

		String[] filenames = new String[] { "WABO1_01_BB.xes.gz", "WABO2_01_BB.xes.gz", "WABO3_01_BB.xes.gz",
				"WABO4_01_BB.xes.gz", "WABO5_01_BB.xes.gz" };

		ArrayList<XLog> logs = new ArrayList<XLog>();

		XUniversalParser parser = new XUniversalParser();

		for (String filename : filenames) {
			File file = new File(baseDir + filename);
			if (parser.canParse(file)) {
				try {
					logs.add(parser.parse(file).iterator().next());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return logs;
	}
}
