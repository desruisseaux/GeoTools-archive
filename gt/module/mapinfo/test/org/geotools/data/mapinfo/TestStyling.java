/*
 * MapInfoDataSourceTest.java
 * JUnit based test
 *
 * Created on 29 July 2002, 12:13
 */

package org.geotools.data.mapinfo;

import java.net.URL;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
/**
 *
 * @author iant
 */
public class TestStyling extends TestCase {
    // change loging level if problems occur in this test
     Logger _log = Logger.getLogger("org.geotools.MifMid");
    MapInfoDataSource dsMapInfo;
    boolean setup = false;
    String dataFolder;
    public TestStyling(java.lang.String testName) {
        super(testName);
        
        dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData/";
        }
        
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TestStyling.class);
        
        return suite;
    }
    public void setUp() throws Exception{
        if(setup) return;
        setup=true;
         String miffile = dataFolder + "baltic/basins.mif";
        dsMapInfo = new MapInfoDataSource(new URL(miffile));
    }
    
    public void testStyling() throws Exception{
        
      
        dsMapInfo.readMifMid();
    }
  
    
}
