package org.geotools.graph.build.opt;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(OptGraphBuilderTest.class));
    addTest(new TestSuite(OptDirectedGraphBuilderTest.class));
  }   
    
    
}