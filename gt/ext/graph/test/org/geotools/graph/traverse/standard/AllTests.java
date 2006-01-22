package org.geotools.graph.traverse.standard;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(DepthFirstIteratorTest.class));
    addTest(new TestSuite(DirectedDepthFirstIteratorTest.class));
    addTest(new TestSuite(BreadthFirstIteratorTest.class));
    addTest(new TestSuite(DijkstraIteratorTest.class));
    addTest(new TestSuite(NoBifurcationIteratorTest.class));
    addTest(new TestSuite(BreadthFirstTopologicalIteratorTest.class));
    addTest(new TestSuite(DepthFirstTopologicalIteratorTest.class));
    
    //addTest(new TestSuite(DepthFirstTraversalTest.class));
//    addTest(new TestSuite(BreadthFirstTraversalTest.class));
//    addTest(new TestSuite(NoBifurcationTraversalTest.class));
  } 	
    
}