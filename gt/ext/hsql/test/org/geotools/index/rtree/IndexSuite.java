/*
 * ProjectionTestSuite.java
 * JUnit based test
 *
 */

package org.geotools.index.rtree;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.index.rtree.memory.MemoryPageStoreTest;


/**
 *
 * @author Tommaso Nolli
 */
public class IndexSuite extends TestCase {
  
  public IndexSuite(String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("All Index Tests");

    suite.addTestSuite(org.geotools.index.rtree.fs.FileSystemPageStoreTest.class);
    suite.addTestSuite(org.geotools.index.rtree.cachefs.FileSystemPageStoreTest.class);
    suite.addTestSuite(MemoryPageStoreTest.class);

    suite.addTestSuite(RTreeTest.class);
    
    return suite;
  }
}
