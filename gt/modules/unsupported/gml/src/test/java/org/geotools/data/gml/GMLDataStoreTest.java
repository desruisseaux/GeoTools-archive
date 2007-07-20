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
package org.geotools.data.gml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.vividsolutions.jts.geom.MultiLineString;
import junit.framework.TestCase;
import org.geotools.TestData;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.referencing.CRS;


/**
 * Tests for the GML Datastore.  Note:  Since GMLDatastore uses
 * FileGMLDatastore FileGMLDatastore is also tested by this  class.
 *
 * @author Jesse
 */
public class GMLDataStoreTest extends AbstractGMLTestCase {
    private GMLDataStore ds;
	private String name;

    protected void setUp() throws Exception {
    	super.setUp();
        ds = new GMLDataStore(gmlFile.getParentFile().toURI(), 10, 1000000);
        name = gmlFile.getName();
		name = name.substring(0, name.lastIndexOf('.'));    }
    
    /**
     * Test method for {@link
     * org.geotools.data.gml.GMLDataStore#getTypeNames()}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetTypeNames() throws Exception {
        assertEquals(1, ds.getTypeNames().length);
		assertEquals(name, ds.getTypeNames()[0]);
    }

    /**
     * Test method for {@link
     * org.geotools.data.gml.GMLDataStore#getSchema(java.lang.String)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetSchema() throws Exception {
        FeatureType schema = ds.getSchema(name);
        assertNotNull(schema);
        assertEquals(3, schema.getAttributeCount());
        assertEquals(MultiLineString.class,
            schema.getAttributeType("the_geom").getBinding());
        assertEquals(String.class, schema.getAttributeType("FID").getBinding());
        assertEquals(String.class, schema.getAttributeType("NAME").getBinding());
        assertEquals(CRS.decode("EPSG:4326"), schema.getPrimaryGeometry().getCoordinateSystem());
    }

    /**
     * Test method for {@link
     * org.geotools.data.AbstractDataStore#getFeatureReader(org.geotools.data.Query,
     * org.geotools.data.Transaction)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetFeatureReader() throws Exception {
        FeatureReader reader = ds.getFeatureReader(new DefaultQuery(name),
                Transaction.AUTO_COMMIT);

        try {
            assertTrue(reader.hasNext());

            Feature feature1 = reader.next();
            assertEquals("Streams.1", feature1.getID());
            assertEquals("Cam Stream", feature1.getAttribute("NAME"));
            assertEquals("111", feature1.getAttribute("FID"));
            assertTrue(feature1.getPrimaryGeometry() instanceof MultiLineString);

            feature1 = reader.next();
            assertEquals("Streams.2", feature1.getID());
            assertEquals("", feature1.getAttribute("NAME"));
            assertEquals("112", feature1.getAttribute("FID"));
            assertTrue(feature1.getPrimaryGeometry() instanceof MultiLineString);
        } finally {
            reader.close();
        }
    }

    /**
     * Test method for {@link
     * org.geotools.data.AbstractDataStore#getBounds(org.geotools.data.Query)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetBounds() throws Exception {
        //TODO
    }

    /**
     * Test method for {@link
     * org.geotools.data.AbstractDataStore#getCount(org.geotools.data.Query)}.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testGetCount() throws Exception {
        //TODO
    }
}
