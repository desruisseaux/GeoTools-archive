package org.geotools.graph.traverse.basic;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(SimpleGraphWalkerTest.class));
  } 	
}