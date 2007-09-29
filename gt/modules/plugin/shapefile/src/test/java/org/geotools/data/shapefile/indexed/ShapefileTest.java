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
import org.geotools.TestData;


/**
 * @source $URL$
 * @version $Id$
 * @author Ian Schneider
 * @author James Macgill
 */
public class ShapefileTest extends org.geotools.data.shapefile.ShapefileTest {
    
    public ShapefileTest(String testName) throws IOException {
        super(testName);
    }

    public void testHolyPolygons() throws Exception {
        Geometry g = readGeometry("holyPoly");

        FeatureTypeFactory factory = FeatureTypeFactory.newInstance("junk");
        factory.addType(AttributeTypeFactory.newAttributeType("a",
                Geometry.class));

        SimpleFeatureType type = factory.getFeatureType();
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

        store.addFeatures( features );

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
