/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.nio.channels.ReadableByteChannel;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.Lock;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.TestData;


/**
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends TestCaseSupport {
    final String STATEPOP      = "shapes/statepop.shp";
    final String STATEPOP_IDX  = "shapes/statepop.shx";
    final String POINTTEST     = "shapes/pointtest.shp";
    final String POLYGONTEST   = "shapes/polygontest.shp";
    final String HOLETOUCHEDGE = "shapes/holeTouchEdge.shp";
    final String EXTRAATEND    = "shapes/extraAtEnd.shp";

    private final Lock lock = new Lock();

    public ShapefileTest(String testName) throws IOException {
        super(testName);
    }

    public static void main(String[] args) {
        verbose = true;
        junit.textui.TestRunner.run(suite(ShapefileTest.class));
    }

    public void testLoadingStatePop() throws Exception {
        loadShapes(STATEPOP, 49);
        loadMemoryMapped(STATEPOP, 49);
    }

    public void testLoadingSamplePointFile() throws Exception {
        loadShapes(POINTTEST, 10);
        loadMemoryMapped(POINTTEST, 10);
    }

    public void testLoadingSamplePolygonFile() throws Exception {
        loadShapes(POLYGONTEST, 2);
        loadMemoryMapped(POLYGONTEST, 2);
    }

    public void testLoadingTwice() throws Exception {
        loadShapes(POINTTEST, 10);
        loadShapes(POINTTEST, 10);
        loadShapes(STATEPOP, 49);
        loadShapes(STATEPOP, 49);
        loadShapes(POLYGONTEST, 2);
        loadShapes(POLYGONTEST, 2);
    }

    /**
     * It is posible for a point in a hole to touch the edge of its containing
     * shell This test checks that such polygons can be loaded ok.
     */
    public void testPolygonHoleTouchAtEdge() throws Exception {
        loadShapes(HOLETOUCHEDGE, 1);
        loadMemoryMapped(HOLETOUCHEDGE, 1);
    }

    /**
     * It is posible for a shapefile to have extra information past the end of
     * the normal feature area, this tests checks that this situation is delt
     * with ok.
     */
    public void testExtraAtEnd() throws Exception {
        loadShapes(EXTRAATEND, 3);
        loadMemoryMapped(EXTRAATEND, 3);
    }

    public void testIndexFile() throws Exception {
        copyShapefiles(STATEPOP);
        copyShapefiles(STATEPOP_IDX);
        final ReadableByteChannel channel1 = TestData.openChannel(      STATEPOP); // Backed by InputStream
        final ReadableByteChannel channel2 = TestData.openChannel(this, STATEPOP); // Backed by File
        final ReadableByteChannel channel3 = TestData.openChannel(this, STATEPOP_IDX);
        final ShapefileReader     reader1  = new ShapefileReader(channel1, lock);
        final ShapefileReader     reader2  = new ShapefileReader(channel2, lock);
        final IndexFile           index    = new IndexFile(channel3);
        try{
	        for (int i = 0; i < index.getRecordCount(); i++) {
	            if (reader1.hasNext()) {
	                Geometry g1 = (Geometry) reader1.nextRecord().shape();
	                Geometry g2 = (Geometry) reader2.shapeAt(2 * (index.getOffset(i)));
	                assertTrue(g1.equalsExact(g2));
	
	                g2 = (Geometry) reader2.shapeAt(index.getOffsetInBytes(i));
	                assertTrue(g1.equalsExact(g2));
	            } else {
	                fail("uneven number of records");
	            }

            //assertEquals(reader1.nextRecord().offset(),index.getOffset(i));
        }
	    }finally{
	    	reader1.close();
	    	reader2.close();
	    	index.close();
	    }
    }

    public void testHolyPolygons() throws Exception {
        Geometry g = readGeometry("holyPoly");

        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
        factory.addType(AttributeTypeFactory.newAttributeType("a",
                Geometry.class));

        FeatureType type = factory.getFeatureType();
        FeatureCollection features = FeatureCollections.newCollection();
        features.add(type.create(new Object[] { g }));

        File tmpFile = getTempFile();
        tmpFile.delete();

        // write features
        IndexedShapefileDataStoreFactory make = new IndexedShapefileDataStoreFactory();
        DataStore s = make.createDataStore(tmpFile.toURL());
        s.createSchema(type);

        String typeName = type.getTypeName();
        FeatureStore store = (FeatureStore) s.getFeatureSource(typeName);

        store.addFeatures(DataUtilities.reader(features));

        s = new IndexedShapefileDataStore(tmpFile.toURL());
        typeName = s.getTypeNames()[0];

        FeatureSource source = s.getFeatureSource(typeName);
        FeatureCollection fc = source.getFeatures();

        FeatureIterator f1iter = features.features();
        FeatureIterator f2iter = fc.features();
        try{
        ShapefileRTreeReadWriteTest.compare(f1iter.next(), f2iter.next());
        }finally{
        	f1iter.close();
        	f2iter.close();
        }
    }

    public void testSkippingRecords() throws Exception {
        ShapefileReader r = new ShapefileReader(TestData.openChannel(STATEPOP), lock);
        int idx = 0;

        while (r.hasNext()) {
            idx++;
            r.nextRecord();
        }
        
        r.close();
        assertEquals(49, idx);
    }

    public void testShapefileReaderRecord() throws Exception {
        ShapefileReader reader = new ShapefileReader(TestData.openChannel(STATEPOP), lock);
        ArrayList offsets = new ArrayList();

        while (reader.hasNext()) {
            ShapefileReader.Record record = reader.nextRecord();
            offsets.add(new Integer(record.offset()));

            Geometry geom = (Geometry) record.shape();
            assertEquals(new Envelope(record.minX, record.maxX, record.minY,
                    record.maxY), geom.getEnvelopeInternal());
            record.toString();
        }
        reader.close();
        copyShapefiles(STATEPOP);
        reader = new ShapefileReader(TestData.openChannel(this, STATEPOP), lock);

        for (int i = 0, ii = offsets.size(); i < ii; i++) {
            reader.shapeAt(((Integer) offsets.get(i)).intValue());
        }
        reader.close();
    }

    private void loadShapes(String resource, int expected) throws Exception {
        final ReadableByteChannel c = TestData.openChannel(resource);
        ShapefileReader reader = new ShapefileReader(c, lock);
        int cnt = 0;

        while (reader.hasNext()) {
            reader.nextRecord().shape();
            cnt++;
        }
        reader.close();
        assertEquals("Number of Geometries loaded incorect for : " + resource, expected, cnt);
    }

    private void loadMemoryMapped(String resource, int expected) throws Exception {
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
