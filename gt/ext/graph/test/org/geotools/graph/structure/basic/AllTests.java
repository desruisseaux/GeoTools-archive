package org.geotools.graph.structure.basic;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(BasicGraphableTest.class));
    addTest(new TestSuite(BasicNodeTest.class));
    addTest(new TestSuite(BasicEdgeTest.class));
    addTest(new TestSuite(BasicDirectedNodeTest.class));  
    addTest(new TestSuite(BasicDirectedEdgeTest.class));
    addTest(new TestSuite(BasicGraphTest.class));
  } 	
    
}