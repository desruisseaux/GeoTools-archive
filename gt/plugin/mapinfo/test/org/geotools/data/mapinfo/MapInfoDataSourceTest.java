/*
 * MapInfoDataSourceTest.java
 * JUnit based test
 *
 * Created on 29 July 2002, 12:13
 */

package org.geotools.data.mapinfo;

import java.io.IOException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.DataSourceException;
import org.geotools.feature.FeatureCollection;
import org.geotools.resources.TestData;

/**
 *
 * @author iant
 */
public class MapInfoDataSourceTest extends TestCase {
    MapInfoDataStore dsMapInfo;
    boolean setup = false;
    public MapInfoDataSourceTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MapInfoDataSourceTest.class);
        
        return suite;
    }
   
    String dataFolder;
    public void testGetFeatures() throws Exception{
    	URL url;
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder!=null){
        	url = new URL("file:////"+dataFolder+"/statepop.mif"); 
        }
        else {
            // then we are being run by maven
        	url = TestData.getResource( this, "statepop.mif" );
        }
        try{
        	System.out.println("Testing ability to load "+url);
            MapInfoDataStore data = new MapInfoDataStore(url);
	
            FeatureCollection collection = data.getFeatureSource( data.getTypeNames()[0]).getFeatures().collection();
            assertEquals("Wrong number of features loaded", 49,collection.size());
        }
        catch(DataSourceException e){
            System.out.println(e);
            e.printStackTrace();
            fail("Load failed because of exception "+e.toString());
        }
        catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            fail("Load failed because of exception "+ioe.toString());
        }
        
    }
  /*
    /** Test of readMifMid method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testReadMifMid() {
        System.out.println("testReadMifMid");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of importFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testImportFeatures() {
        System.out.println("testImportFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of exportFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testExportFeatures() {
        System.out.println("testExportFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of stopLoading method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testStopLoading() {
        System.out.println("testStopLoading");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of getExtent method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testGetExtent() {
        System.out.println("testGetExtent");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
 
   
    /** Test of addFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testAddFeatures() {
        System.out.println("testAddFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of abortLoading method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testAbortLoading() {
        System.out.println("testAbortLoading");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of getBbox method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testGetBbox() {
        System.out.println("testGetBbox");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of modifyFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testModifyFeatures() {
        System.out.println("testModifyFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    /** Test of removeFeatures method, of class org.geotools.mapinfo.MapInfoDataSource.
    public void testRemoveFeatures() {
        System.out.println("testRemoveFeatures");
   
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
   
    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
   
   */
    
}
