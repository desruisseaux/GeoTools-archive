package org.geotools.gml;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author ian
 */
public class GmlSuite extends TestCase {
    
   
    public GmlSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) throws java.io.IOException {
        junit.textui.TestRunner.run(suite());
        
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("All gmldatasource tests");
        suite.addTestSuite(GmlTest.class);
        return suite;
    }
    
  
    
}
