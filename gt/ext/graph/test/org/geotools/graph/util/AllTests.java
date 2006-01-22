package org.geotools.graph.util;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(CycleDetectorTest.class));
    addTest(new TestSuite(DijkstraShortestPathFinderTest.class));
    addTest(new TestSuite(GraphPartitionerTest.class));
    addTest(new TestSuite(GraphFuserTest.class));
  } 	
}