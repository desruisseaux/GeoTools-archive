package org.geotools.graph.structure.opt;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(OptNodeTest.class));
    addTest(new TestSuite(OptEdgeTest.class));
    addTest(new TestSuite(OptDirectedNodeTest.class));
    addTest(new TestSuite(OptDirectedEdgeTest.class));
  } 	
    
}