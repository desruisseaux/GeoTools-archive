package org.geotools.graph.path;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(WalkTest.class));
  } 	
    
    
}