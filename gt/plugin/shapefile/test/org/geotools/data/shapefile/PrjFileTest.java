/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */

package org.geotools.data.shapefile;

import junit.framework.*;
import java.net.*;
//import cmp.LEDataStream.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.util.ArrayList;
import org.geotools.data.shapefile.dbf.*;
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
  
  public void testRewrite(){
      String fixed = prj.rewrite("PROJCS[\"NAD_1983_StatePlane_Florida_North_FIPS_0903_Feet\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Lambert_Conformal_Conic\"],PARAMETER[\"False_Easting\",1968500.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",-84.5],PARAMETER[\"Standard_Parallel_1\",29.58333333333333],PARAMETER[\"Standard_Parallel_2\",30.75],PARAMETER[\"Latitude_Of_Origin\",29.0],UNIT[\"Foot_US\",0.3048006096012192]]");
      System.out.println("fixed" + fixed);
      
  }
  
  public void testGeneral(){
    System.out.println("tested");
  }
  
 
  
}
