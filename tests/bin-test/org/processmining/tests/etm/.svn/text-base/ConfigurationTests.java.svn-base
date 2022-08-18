package org.processmining.tests.etm;

import junit.framework.TestCase;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.junit.Test;
import org.processmining.plugins.etm.model.narytree.NAryTree;
import org.processmining.plugins.etm.model.narytree.TreeUtils;
import org.processmining.plugins.etm.model.narytree.test.LogCreator;

public class ConfigurationTests extends TestCase {

	private static XEventClasses classes;

	static {
		XLog log = LogCreator.createInterleavedLog(1, "A", "B", "C", "D", "E");
		XLogInfo info = XLogInfoFactory.createLogInfo(log, XLogInfoImpl.NAME_CLASSIFIER);
		classes = info.getEventClasses();
	}

	/**
	 * test no blocking (base test, e.g. if this fails maybe something else
	 * changed and all 'real' tests will fail too)
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testNoConfiguration() throws Throwable {
		testTree("SEQ( LEAF: A , LEAF: B ) [ ]", "SEQ( LEAF: A , LEAF: B ) [ ]");
	}

	/**
	 * test no blocking (base test, e.g. if this fails maybe something else
	 * changed and all 'real' tests will fail too)
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockXORChild() throws Throwable {
		testTree("XOR( LEAF: A , LEAF: B ) [[-, B, -] ]", "XOR( LEAF: B ) [ ]");
	}

	/**
	 * test no blocking (base test, e.g. if this fails maybe something else
	 * changed and all 'real' tests will fail too)
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockORChild() throws Throwable {
		testTree("OR( LEAF: A , LEAF: B ) [[-, B, -] ]", "OR( LEAF: B ) [ ]");
	}

	/**
	 * blocking a direct SEQ child
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockInSeqDirect() throws Throwable {
		testTree("SEQ( LEAF: A , LEAF: B ) [[-, B, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * blocking a nested SEQ child
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockInSeqNested() throws Throwable {
		testTree("SEQ( LEAF: A , SEQ( LEAF: B , LEAF: C ) ) [[-, -, -, B, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * blocking a direct AND child
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockInANDDirect() throws Throwable {
		testTree("AND( LEAF: A , LEAF: B ) [[-, B, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * blocking a nested AND child
	 * 
	 * @throws Throwable
	 */
	@Test
	public static void testBlockInANDNested() throws Throwable {
		testTree("AND( LEAF: A , SEQ( LEAF: B , LEAF: C ) ) [[-, -, -, B, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * Test blocking of the DO of a LOOP
	 */
	@Test
	public static void testBlockLOOPDo() {
		testTree("LOOP( LEAF: A , LEAF: B , LEAF: C ) [[-, B, -, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * Test blocking of the REDO of a LOOP
	 */
	@Test
	public static void testBlockLOOPRedo() {
		testTree("LOOP( LEAF: A , LEAF: B , LEAF: C ) [[-, -, B, -] ]", "SEQ( LEAF: A , LEAF: C ) [ ]");
	}

	/**
	 * Test blocking of the REDO of a LOOP with bigger subtrees
	 */
	@Test
	public static void testBlockLOOPRedoSubtree() {
		testTree(
				"LOOP( SEQ( LEAF: A , XOR( LEAF: D , LEAF: E ) ) , LEAF: B , SEQ( LEAF: C , AND( LEAF: E , LEAF: D ) ) ) [[-, -, -, -, -, -, B, -, -, -, -, -] ]",
				"SEQ( SEQ( LEAF: A , XOR( LEAF: D , LEAF: E ) ) , SEQ( LEAF: C , AND( LEAF: E , LEAF: D ) ) ) [ ]");
	}

	/**
	 * Test blocking of the EXIT of a LOOP
	 */
	@Test
	public static void testBlockLOOPExit() {
		testTree("LOOP( LEAF: A , LEAF: B , LEAF: C ) [[-, -, -, B] ]", "LEAF: tau [ ]");
	}

	/**
	 * Test blocking of only child of operator with cascading effect
	 */
	@Test
	public static void testBlockOnlyChild() {
		testTree("OR( LEAF: A , AND( LEAF: B ) , LEAF: C ) [[-, -, -, B, -] ]", "OR( LEAF: A , LEAF: C ) [ ]");
	}
	
	/**
	 * Test blocking of only child of operator with cascading effect
	 */
	@Test
	public static void testBlockOnlyChildCascade() {
		testTree("SEQ( LEAF: A , AND( LEAF: B ) , LEAF: C ) [[-, -, -, B, -] ]", "LEAF: tau [ ]");
	}

	/**
	 * Utility method that tests if applying configuration 0 in an instantiation
	 * of configuredTree results in the expectedResult string. Assumes leaf
	 * classes A...E
	 * 
	 * @param configuredTree
	 * @param expectedResult
	 */
	public static void testTree(String configuredTree, String expectedResult) {
		//From string to tree
		NAryTree tree = TreeUtils.fromString(configuredTree, classes);

		//Apply configuration
		NAryTree treeConfigured = tree.applyConfiguration(0);

		//To string
		String result = TreeUtils.toString(treeConfigured, classes);

		//Test if its what we expected
		org.junit.Assert.assertEquals(result.trim(), expectedResult);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(ConfigurationTests.class);
	}

}
