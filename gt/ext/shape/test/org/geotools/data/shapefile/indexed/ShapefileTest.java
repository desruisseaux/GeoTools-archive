/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
/*
 * ShapefileTest.java
 * JUnit based test
 *
 * Created on 12 February 2002, 21:27
 */
package org.geotools.data.shapefile.indexed;

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
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import java.io.File;
import java.util.ArrayList;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends TestCaseSupport {
    final String STATEPOP = "statepop.shp";
    final String STATEPOP_IDX = "statepop.shx";
    final String POINTTEST = "pointtest.shp";
    final String POLYGONTEST = "polygontest.shp";
    final String HOLETOUCHEDGE = "holeTouchEdge.shp";
    final String EXTRAATEND = "extraAtEnd.shp";
    Lock lock = new Lock();

    public ShapefileTest(String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
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
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testPolygonHoleTouchAtEdge() throws Exception {
        loadShapes(HOLETOUCHEDGE, 1);
        loadMemoryMapped(HOLETOUCHEDGE, 1);
    }

    /**
     * It is posible for a shapefile to have extra information past the end of
     * the normal feature area, this tests checks that this situation is delt
     * with ok.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testExtraAtEnd() throws Exception {
        loadShapes(EXTRAATEND, 3);
        loadMemoryMapped(EXTRAATEND, 3);
    }

    public void testIndexFile() throws Exception {
        ShapefileReader reader1 = new ShapefileReader(getTestResourceChannel(
                    STATEPOP), lock);
        ShapefileReader reader2 = new ShapefileReader(getReadableFileChannel(
                    STATEPOP), lock);
        IndexFile index = new IndexFile(getTestResourceChannel(STATEPOP_IDX));

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
        FeatureCollection fc = source.getFeatures().collection();

        ShapefileReadWriteTest.compare(features, fc);
    }

    public void testSkippingRecords() throws Exception {
        ShapefileReader r = new ShapefileReader(getTestResourceChannel(STATEPOP),
                lock);
        int idx = 0;

        while (r.hasNext()) {
            idx++;
            r.nextRecord();
        }

        assertEquals(49, idx);
    }

    public void testShapefileReaderRecord() throws Exception {
        ShapefileReader reader = new ShapefileReader(getTestResourceChannel(
                    STATEPOP), lock);
        ArrayList offsets = new ArrayList();

        while (reader.hasNext()) {
            ShapefileReader.Record record = reader.nextRecord();
            offsets.add(new Integer(record.offset()));

            Geometry geom = (Geometry) record.shape();
            assertEquals(new Envelope(record.minX, record.maxX, record.minY,
                    record.maxY), geom.getEnvelopeInternal());
            record.toString();
        }

        reader = new ShapefileReader(getReadableFileChannel(STATEPOP), lock);

        for (int i = 0, ii = offsets.size(); i < ii; i++) {
            reader.shapeAt(((Integer) offsets.get(i)).intValue());
        }
    }

    private void loadShapes(String resource, int expected)
        throws Exception {
        ShapefileReader reader = new ShapefileReader(getTestResourceChannel(
                    resource), lock);
        int cnt = 0;

        while (reader.hasNext()) {
            reader.nextRecord().shape();
            cnt++;
        }

        assertEquals("Number of Geometries loaded incorect for : " + resource,
            expected, cnt);
    }

    private void loadMemoryMapped(String resource, int expected)
        throws Exception {
        ShapefileReader reader = new ShapefileReader(getReadableFileChannel(
                    resource), lock);
        int cnt = 0;

        while (reader.hasNext()) {
            reader.nextRecord().shape();
            cnt++;
        }

        assertEquals("Number of Geometries loaded incorect for : " + resource,
            expected, cnt);
    }
}
