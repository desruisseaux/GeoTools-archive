/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.io.IOException;
import java.util.Iterator;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


public abstract class JDBCFeatureCollectionTest extends JDBCTestSupport {
    FeatureCollection<SimpleFeatureType, SimpleFeature> collection;

    protected void setUp() throws Exception {
        super.setUp();

        JDBCFeatureStore source = (JDBCFeatureStore) dataStore.getFeatureSource("ft1");
        collection = source.getFeatures(); 
    }

    public void testIterator() throws Exception {
        Iterator i = collection.iterator();
        assertNotNull(i);

        int base = -1;

        for (int x = 0; x < 3; x++) {
            assertTrue(i.hasNext());

            SimpleFeature feature = (SimpleFeature) i.next();
            assertNotNull(feature);

            String fid = feature.getID();
            int id = Integer.parseInt(fid.substring(fid.indexOf('.') + 1));

            if (base == -1) {
                base = id;
            }

            assertEquals(base++, id);
            assertEquals(x,((Number)feature.getAttribute("intProperty")).intValue() );
        }

        assertFalse(i.hasNext());
        collection.close(i);
    }

    public void testBounds() throws IOException {
        ReferencedEnvelope bounds = collection.getBounds();
        assertNotNull(bounds);

        assertEquals(0d, bounds.getMinX(), 0.1);
        assertEquals(0d, bounds.getMinY(), 0.1);
        assertEquals(2d, bounds.getMaxX(), 0.1);
        assertEquals(2d, bounds.getMaxY(), 0.1);
    }

    public void testSize() throws IOException {
        assertEquals(3, collection.size());
    }

    public void testSubCollection() throws Exception {
        FilterFactory ff = dataStore.getFilterFactory();
        Filter f = ff.equals(ff.property("intProperty"), ff.literal(1));

        FeatureCollection<SimpleFeatureType, SimpleFeature> sub = collection.subCollection(f);
        assertNotNull(sub);

        assertEquals(1, sub.size());
        assertEquals(new ReferencedEnvelope(1, 1, 1, 1, CRS.decode("EPSG:4326")), sub.getBounds());

        sub.clear();
        assertEquals(2, collection.size());
    }

    public void testAdd() throws IOException {
        SimpleFeatureBuilder b = new SimpleFeatureBuilder(collection.getSchema());
        b.set("intProperty", new Integer(3));
        b.set("doubleProperty", new Double(3.3));
        b.set("stringProperty", "three");
        b.set("geometry", new GeometryFactory().createPoint(new Coordinate(3, 3)));

        SimpleFeature feature = b.buildFeature(null);
        assertEquals(3, collection.size());

        collection.add(feature);
        assertEquals(4, collection.size());

        Iterator i = collection.iterator();
        boolean found = false;

        while (i.hasNext()) {
            SimpleFeature f = (SimpleFeature) i.next();

            if (new Integer(3).equals(f.getAttribute("intProperty"))) {
                assertEquals(feature.getAttribute("doubleProperty"),
                    f.getAttribute("doubleProperty"));
                assertEquals(feature.getAttribute("stringProperty"),
                    f.getAttribute("stringProperty"));
                assertTrue(((Geometry) feature.getAttribute("geometry")).equals(
                        (Geometry) f.getAttribute("geometry")));
                found = true;
            }
        }

        assertTrue(found);

        collection.close(i);
    }

    public void testClear() throws IOException {
        collection.clear();

        Iterator i = collection.iterator();
        assertFalse(i.hasNext());

        collection.close(i);
    }
}
