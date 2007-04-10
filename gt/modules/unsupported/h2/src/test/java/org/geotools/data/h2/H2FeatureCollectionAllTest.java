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
package org.geotools.data.h2;

import java.util.Iterator;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import org.geotools.data.Transaction;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.type.TypeName;


public class H2FeatureCollectionAllTest extends H2TestSupport {
    FeatureCollection all;

    protected void setUp() throws Exception {
        super.setUp();

        ContentEntry entry = dataStore.getContent().entry(dataStore, new TypeName("featureType1"));

        ContentState state = (ContentState) entry.getState(Transaction.AUTO_COMMIT);
        all = dataStore.getContent().all(state);
    }

    public void testSize() throws Exception {
        assertEquals(3, all.size());
    }

    public void testBounds() throws Exception {
        Envelope bounds = all.getBounds();
        assertEquals(1, bounds.getMinX(), 0);
        assertEquals(1, bounds.getMinY(), 0);
        assertEquals(3, bounds.getMaxX(), 0);
        assertEquals(3, bounds.getMaxY(), 0);
    }

    public void testIterator() throws Exception {
        Iterator iterator = all.iterator();

        try {
            for (int i = 1; i <= 3; i++) {
                assertTrue(iterator.hasNext());

                Feature feature = (Feature) iterator.next();

                assertEquals("" + i, feature.getID());

                assertEquals(i, ((Point) (feature.getDefaultGeometry())).getX(), 0);
                assertEquals(i, ((Point) (feature.getDefaultGeometry())).getY(), 0);

                assertEquals(i, ((Point) (feature.getAttribute("geometry"))).getX(), 0);
                assertEquals(i, ((Point) (feature.getAttribute("geometry"))).getY(), 0);

                assertEquals(i, ((Integer) feature.getAttribute("intProperty")).intValue());
                assertEquals(i + (i / 10d),
                    ((Double) feature.getAttribute("doubleProperty")).doubleValue(), 0.1);

                String stringProperty = (i == 1) ? "one" : ((i == 2) ? "two" : "three");
                assertEquals(stringProperty, (String) feature.getAttribute("stringProperty"));
            }
        } finally {
            all.close(iterator);
        }
    }
}
