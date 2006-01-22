package org.geotools.graph.io.standard;

import junit.framework.TestSuite;

public class AllTests extends TestSuite {
  public AllTests() {
    super();
    addTest(new TestSuite(BasicGraphSerializerTest.class));
    addTest(new TestSuite(DirectedGraphSerializerTest.class));
    addTest(new TestSuite(OptGraphSerializerTest.class));
    addTest(new TestSuite(OptDirectedGraphSerializerTest.class));
    
  } 	
    
    
}