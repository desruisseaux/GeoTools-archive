/*
 * MapInfoDataSourceTest.java
 * JUnit based test
 *
 * Created on 29 July 2002, 12:13
 */

package org.geotools.data.mapinfo;

import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.geotools.feature.Feature;

/**
 *
 * @author iant
 */
public class LoadTest extends TestCaseSupport {
    // change loging level if problems occur in this test
    Logger _log = Logger.getLogger("MifMid");
    MapInfoDataSource dsMapInfo;
    boolean setup = false;
    String dataFolder;
    public LoadTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LoadTest.class);
        
        return suite;
    }
    public void setUp() throws Exception{
        if(setup) return;
        setup=true;
        URL url = this.getTestResource("statepop.mif");
        dsMapInfo = new MapInfoDataSource(url);
    }
    
    public void testLoad() throws Exception{        
        // Load file
        Vector objects = dsMapInfo.readMifMid();
        System.out.println("Read "+objects.size()+" features");
        assertEquals("Wrong number of features read ",49,objects.size());
        assertEquals("First feature name is wrong","Illinois",((Feature)objects.get(0)).getAttribute("STATE_NAME"));
        
    }
    
     public void testSomeQuotes() throws Exception{
        URL url = this.getTestResource("de-dk-nl.mif");
        dsMapInfo = new MapInfoDataSource(url);
        // Load file
        Vector objects = dsMapInfo.readMifMid();
        System.out.println("Read "+objects.size()+" features");
        assertEquals("Wrong number of features read ",4,objects.size());
        assertEquals("First feature name is wrong","Denmark",((Feature)objects.get(0)).getAttribute("NAME"));
        assertEquals("First feature id is wrong",8.0,((Double)((Feature)objects.get(0)).getAttribute("ID")).doubleValue(),0);
        
    }
    
     public void testDelimInQuotes() throws Exception{
        URL url = this.getTestResource("delimInQuotes.mif");
        dsMapInfo = new MapInfoDataSource(url);
        // Load file
        Vector objects = dsMapInfo.readMifMid();
        System.out.println("Read "+objects.size()+" features");
        assertEquals("Wrong number of features read ",4,objects.size());
        assertEquals("First feature name is wrong","Denmark, DK",((Feature)objects.get(0)).getAttribute("NAME"));
        assertEquals("First feature id is wrong",8.0,((Double)((Feature)objects.get(0)).getAttribute("ID")).doubleValue(),0);
    }
   
    
    
    
}
