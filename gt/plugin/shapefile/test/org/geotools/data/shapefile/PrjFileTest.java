/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.shapefile;

import org.geotools.data.shapefile.prj.PrjFileReader;


/**
 * @author Ian Schneider
 * @author James Macgill
 */
public class PrjFileTest extends TestCaseSupport {
  
  static final String TEST_FILE = "cntbnd01.prj";
  
  private PrjFileReader prj = null;
  
  public PrjFileTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(PrjFileTest.class));
  }

  protected void setUp() throws Exception {
    prj = new PrjFileReader(getTestResourceChannel(TEST_FILE));
  }
  
 
  
  public void testGeneral(){
    System.out.println("tested");
  }
  
 
  
}
