/*
 * ShapefileDataSourceTest.java
 * JUnit based test
 *
 * Created on March 4, 2002, 4:00 PM
 */

package org.geotools.data.shapefile;

import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.*;
import com.vividsolutions.jts.geom.*;
import java.io.*;
import java.net.*;

import junit.framework.*;
import org.geotools.data.shapefile.*;

/**
 * @author Ian Schneider
 * @author jamesm
 */
public class ShapefileDataSourceTest extends TestCaseSupport {
  
  final static String STATE_POP = "statepop.shp";
  
  public ShapefileDataSourceTest(java.lang.String testName) {
    super(testName);
  }
  
  public static void main(java.lang.String[] args) {
    junit.textui.TestRunner.run(suite(ShapefileDataSourceTest.class));
  }
  
  private Feature[] loadFeatures(String resource,Query q) throws Exception {
    if (q == null) q = new QueryImpl();
    URL url = getTestResource(resource);
    ShapefileDataSource s = new ShapefileDataSource(url);
    return s.getFeatures(q).getFeatures();
  }
  
  
  public void testLoad() throws Exception {
    loadFeatures(STATE_POP,null);
  }
  
  public void testSchema() throws Exception {
    URL url = getTestResource(STATE_POP);
    ShapefileDataSource s = new ShapefileDataSource(url);
    FeatureType schema = s.getSchema();
    AttributeType[] types = schema.getAttributeTypes();
    assertEquals("Number of Attributes",253,types.length);
  }
  
  public void testLoadAndVerify() throws Exception {
    Feature[] features = loadFeatures(STATE_POP,null);
    
    assertEquals("Number of Features loaded",49,features.length);
    
    FeatureType schema = features[0].getSchema();
    assertNotNull(schema.getDefaultGeometry());
    assertEquals("Number of Attributes",253,schema.getAttributeTypes().length);
    assertEquals("Value of statename is wrong",features[0].getAttribute("STATE_NAME"),"Illinois");
    assertEquals("Value of land area is wrong",((Double)features[0].getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
  }
  
  
  public void testQuerySubset() throws Exception {
    QueryImpl qi = new QueryImpl();
    qi.setProperties(new AttributeTypeDefault[] {new AttributeTypeDefault("STATE_NAME",String.class)});
    Feature[] features = loadFeatures(STATE_POP,qi);
    
    assertEquals("Number of Features loaded",49,features.length);
    FeatureType schema = features[0].getSchema();
    
    assertEquals("Number of Attributes",1,schema.getAttributeTypes().length);
  }
  
  public void testQueryFill() throws Exception {
    QueryImpl qi = new QueryImpl();
    qi.setProperties(new AttributeTypeDefault[] {
      new AttributeTypeDefault("Billy",String.class),
      new AttributeTypeDefault("STATE_NAME",String.class),
      new AttributeTypeDefault("LAND_KM",Double.class),
      new AttributeTypeDefault("the_geom",Geometry.class)});
    Feature[] features = loadFeatures(STATE_POP,qi);
    assertEquals("Number of Features loaded",49,features.length);
    
    FeatureType schema = features[0].getSchema();
    assertNotNull(schema.getDefaultGeometry());
    assertEquals("Number of Attributes",4,schema.getAttributeTypes().length);
    assertEquals("Value of statename is wrong",features[0].getAttribute("STATE_NAME"),"Illinois");
    assertEquals("Value of land area is wrong",((Double)features[0].getAttribute("LAND_KM")).doubleValue(),143986.61,0.001);
    
    for (int i = 0, ii = features.length; i < ii; i++) {
      assertNull(features[i].getAttribute("Billy")); 
    }
  }
  
  public void testQuerying() throws Exception {
    URL url = getTestResource(STATE_POP);
    ShapefileDataSource s = new ShapefileDataSource(url);
    FeatureType schema = s.getSchema();
    AttributeType[] types = schema.getAttributeTypes();
    for (int i = 0, ii = types.length; i < ii; i++) {
      AttributeType[] queryAtts = new AttributeType[1];
      queryAtts[0] = types[i];
      QueryImpl q = new QueryImpl();
      q.setProperties(queryAtts);
      FeatureCollection fc = s.getFeatures(q);
      Feature[] f = fc.getFeatures();
      assertEquals("Number of Features",49,f.length);
      assertEquals("Number of Attributes",1,f[0].getAttributes().length);
      assertEquals("Attribute Name",f[0].getSchema().getAttributeType(0).getName(),queryAtts[0].getName());
      assertEquals("Attribute Type",f[0].getSchema().getAttributeType(0).getType(),queryAtts[0].getType());
      if (i % 5 == 0) System.out.print(".");
    }
  }
}
