package org.geotools.data.arcgrid;

/*
 * GmlSuite.java
 * JUnit based test
 *
 * Created on 04 March 2002, 16:09
 */
import java.util.Iterator;

import org.geotools.data.arcgrid.ArcGridFormatFactory;
import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;

/**
 *
 * @author ian
 */
public class ServiceTest extends TestCaseSupport {
  
  final String TEST_FILE = "ArcGrid.asc";
  
  public ServiceTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(ServiceTest.class));
  }
  
  public void testIsAvailable() {      
    Iterator list = GridFormatFinder.getAvailableFormats();
    boolean found = false;
    while(list.hasNext()){
      GridFormatFactorySpi fac = (GridFormatFactorySpi)list.next();
      if(fac instanceof ArcGridFormatFactory){
        found=true;        
        break;
      }      
    }
    assertTrue("ArcGridFormatFactory not registered", found);
  }
    
}
