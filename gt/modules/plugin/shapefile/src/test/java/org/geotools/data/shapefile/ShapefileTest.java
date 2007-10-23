/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.shapefile;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.TestData;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 *
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends TestCaseSupport {
  
  public final String STATEPOP      = "shapes/statepop.shp";
  public final String STATEPOP_IDX  = "shapes/statepop.shx";
  public final String POINTTEST     = "shapes/pointtest.shp";
  public final String POLYGONTEST   = "shapes/polygontest.shp";
  public final String HOLETOUCHEDGE = "shapes/holeTouchEdge.shp";
  public final String EXTRAATEND    = "shapes/extraAtEnd.shp";
  
  protected final Lock lock = new Lock();

  public ShapefileTest(String testName) throws IOException {
    super(testName);
  }
    
  public void testLoadingStatePop() throws Exception {
    loadShapes(STATEPOP,49);
    loadMemoryMapped(STATEPOP,49);
  }
  
  public void testLoadingSamplePointFile() throws Exception {
    loadShapes(POINTTEST, 10);
    loadMemoryMapped(POINTTEST,10);
  }
  
  public void testLoadingSamplePolygonFile() throws Exception {
    loadShapes(POLYGONTEST, 2);
    loadMemoryMapped(POLYGONTEST,2);
  }
  
  public void testLoadingTwice() throws Exception {
    loadShapes(POINTTEST, 10);
    loadShapes(POINTTEST, 10);
    loadShapes(STATEPOP,49);
    loadShapes(STATEPOP,49);
    loadShapes(POLYGONTEST, 2);
    loadShapes(POLYGONTEST, 2);
  }
  
  /**
   * It is posible for a point in a hole to touch the edge of its containing shell
   * This test checks that such polygons can be loaded ok.
   */
  public void testPolygonHoleTouchAtEdge() throws Exception {
    loadShapes(HOLETOUCHEDGE, 1);
    loadMemoryMapped(HOLETOUCHEDGE,1);
  }
  
  /**
   * It is posible for a shapefile to have extra information past the end
   * of the normal feature area, this tests checks that this situation is
   * delt with ok.
   */
  public void testExtraAtEnd() throws Exception {
    loadShapes(EXTRAATEND,3);
    loadMemoryMapped(EXTRAATEND,3);
  }
  
  public void testIndexFile() throws Exception {
    copyShapefiles(STATEPOP);
    copyShapefiles(STATEPOP_IDX);
    final ReadableByteChannel channel1 = TestData.openChannel(      STATEPOP); // Backed by InputStream
    final ReadableByteChannel channel2 = TestData.openChannel(this, STATEPOP); // Backed by File
    final ReadableByteChannel channel3 = TestData.openChannel(this, STATEPOP_IDX);
    final ShapefileReader     reader1  = new ShapefileReader(channel1, new Lock());
    final ShapefileReader     reader2  = new ShapefileReader(channel2, new Lock());
    final IndexFile           index    = new IndexFile(channel3);
    try {
        for (int i = 0; i < index.getRecordCount(); i++) {
          if (reader1.hasNext()) {
    
            Geometry g1 = (Geometry) reader1.nextRecord().shape();
            Geometry g2 = (Geometry) reader2.shapeAt(2 * (index.getOffset(i)));
            assertTrue(g1.equalsExact(g2));
            
          } else {
            fail("uneven number of records");
          }
          //assertEquals(reader1.nextRecord().offset(),index.getOffset(i));
        }
    }
    finally {
        reader1.close();
        reader2.close();
        index.close();
     }
  }
  
  public void testHolyPolygons() throws Exception {
    Geometry g = readGeometry("holyPoly");
    
    SimpleFeatureType type = DataUtilities.createType("junk", "a:MultiPolygon");
    FeatureCollection features = FeatureCollections.newCollection();
    
    SimpleFeature feature = SimpleFeatureBuilder.build(type, new Object[] {g}, null);    
    File tmpFile = getTempFile();
    tmpFile.delete();
    
    // write features
    ShapefileDataStoreFactory make = new ShapefileDataStoreFactory();
    DataStore s = make.createDataStore( tmpFile.toURL() );
    s.createSchema( type );
    String typeName = type.getTypeName();
    FeatureStore store = (FeatureStore) s.getFeatureSource( typeName );
    
    store.addFeatures( features );
    
    s = new ShapefileDataStore( tmpFile.toURL() );
    typeName = s.getTypeNames()[0];
    FeatureSource source = s.getFeatureSource( typeName );
    FeatureCollection fc = source.getFeatures(); 

    ShapefileReadWriteTest.compare(features,fc);    
  }
  
  public void testSkippingRecords() throws Exception {
    final ReadableByteChannel c = TestData.openChannel(STATEPOP);
    final ShapefileReader r = new ShapefileReader(c, new Lock());
    int idx = 0;
    while (r.hasNext()) {
        idx++;
        r.nextRecord();
    }
    assertEquals(49,idx);
    r.close();
    c.close();
  }
  
  public void testDuplicateColumnNames() throws Exception {
      File file = TestData.file(this,"bad/state.shp");
      ShapefileDataStore dataStore = new ShapefileDataStore( file.toURL() );
      FeatureSource states = dataStore.getFeatureSource();
      SimpleFeatureType schema = states.getSchema();
      assertEquals( 6, schema.getAttributeCount() );
      assertTrue(states.getCount(Query.ALL) > 0 );
  }
  
  public void testShapefileReaderRecord() throws Exception {
    final ReadableByteChannel c1 = TestData.openChannel(STATEPOP);
    ShapefileReader reader = new ShapefileReader(c1, new Lock());
    ArrayList offsets = new ArrayList();
    while (reader.hasNext()) {
      ShapefileReader.Record record = reader.nextRecord();
      offsets.add(new Integer(record.offset()));
      Geometry geom = (Geometry) record.shape();
      assertEquals(new Envelope(record.minX, record.maxX, record.minY, record.maxY), geom.getEnvelopeInternal());
      record.toString();
    }
    copyShapefiles(STATEPOP);
    reader.close();
    final ReadableByteChannel c2 = TestData.openChannel(this, STATEPOP);
    reader = new ShapefileReader(c2, new Lock());
    for (int i = 0, ii = offsets.size(); i < ii; i++) {
      reader.shapeAt( ((Integer)offsets.get(i)).intValue() ); 
    }
    reader.close();
    c2.close();
    c1.close();
  }
  
  protected void loadShapes(String resource, int expected) throws Exception {
      final ReadableByteChannel c = TestData.openChannel(resource);
      ShapefileReader reader = new ShapefileReader(c, lock );
      int cnt = 0;
      try {
          while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
          }
      }
      finally {
          reader.close();
      }
      assertEquals("Number of Geometries loaded incorect for : " + resource, expected, cnt);
  }
  
  protected void loadMemoryMapped(String resource, int expected) throws Exception {
      final ReadableByteChannel c = TestData.openChannel(resource);
      ShapefileReader reader = new ShapefileReader(c, lock);
      int cnt = 0;
      try{
          while (reader.hasNext()) {
              reader.nextRecord().shape();
              cnt++;
          }
      }finally{
          reader.close();
      }
      assertEquals("Number of Geometries loaded incorect for : " + resource, expected, cnt);
  }
}
