package org.geotools.graph.build.basic;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(BasicGraphBuilderTest.class));
    addTest(new TestSuite(BasicDirectedGraphBuilderTest.class));
  } 	
    
    
}