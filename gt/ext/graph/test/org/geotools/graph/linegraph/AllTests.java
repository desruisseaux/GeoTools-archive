package org.geotools.graph.linegraph;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(LineGraphGeneratorTest.class));
    addTest(new TestSuite(DirectedLineGraphGeneratorTest.class));
    addTest(new TestSuite(OptLineGraphGeneratorTest.class));
    addTest(new TestSuite(OptDirectedLineGraphGeneratorTest.class));
  } 	
}