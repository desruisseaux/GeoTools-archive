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

package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.test.TestData;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.DefaultConnectionFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

/**
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/WFSFeatureSourceTest.java $
 */
public class WFSFeatureSourceTest extends DataTestSupport {

    private WFS_1_1_0_DataStore geosStore;

    private WFSFeatureSource statesSource;

    private WFS_1_1_0_DataStore cubewerxStore;

    private WFSFeatureSource govUnitsSource;

    protected void setUp() throws Exception {
        super.setUp();
        protocolHandler = createTestProtocolHandler(GEOS_CAPABILITIES);
        geosStore = new WFS_1_1_0_DataStore(protocolHandler);
        statesSource = geosStore.getFeatureSource(GEOS_STATES_FEATURETYPENAME);

        WFS110ProtocolHandler cubewerxProtocolHandler = createTestProtocolHandler(CUBEWERX_CAPABILITIES);
        cubewerxStore = new WFS_1_1_0_DataStore(cubewerxProtocolHandler);
        govUnitsSource = cubewerxStore.getFeatureSource(CUBEWERX_GOVUNITCE_FEATURETYPENAME);
    }

    private WFS110ProtocolHandler createTestProtocolHandler(final String capabilitiesFileName)
            throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        ConnectionFactory cf = new DefaultConnectionFactory();

        WFS110ProtocolHandler protocolHandler = new WFS110ProtocolHandler(stream, cf, Integer
                .valueOf(0)) {
            @Override
            public URL getDescribeFeatureTypeURLGet(final String typeName)
                    throws MalformedURLException {
                String schemaLocation;
                if (GEOS_STATES_FEATURETYPENAME.equals(typeName)) {
                    schemaLocation = GEOS_STATES_SCHEMA;
                } else if (CUBEWERX_GOVUNITCE_FEATURETYPENAME.equals(typeName)) {
                    schemaLocation = CUBEWERX_GOVUNITCE_SCHEMA;
                } else {
                    throw new IllegalArgumentException("unknown typename: " + typeName);
                }
                URL url = TestData.getResource(this, schemaLocation);
                assertNotNull(url);
                return url;
            }
        };
        return protocolHandler;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        geosStore = null;
        statesSource = null;
    }

    public void testCreate() throws IOException {
        try {
            new WFSFeatureSource(geosStore, "nonExistentTypeName", protocolHandler);
            fail("Expected IOException for a non existent type name");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link WFSFeatureSource#getInfo()}.
     */
    public void testGetInfo() {
        ResourceInfo info = statesSource.getInfo();
        assertNotNull(info.getBounds());
        assertFalse(info.getBounds().isEmpty());
    }

    /**
     * Test method for {@link WFSFeatureSource#getBounds()}.
     * 
     * @throws IOException
     */
    public void testGetBounds() throws IOException {
        assertNotNull(statesSource.getBounds());
        assertEquals(statesSource.getInfo().getBounds(), statesSource.getBounds());
    }

    /**
     * Test method for
     * {@link WFSFeatureSource#getBounds(org.geotools.data.Query)}.
     */
    public void testGetBoundsQuery() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link WFSFeatureSource#getCount(org.geotools.data.Query)}.
     */
    public void testGetCount() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link WFSFeatureSource#getFeatures()}.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testGetFeatures() throws IOException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = statesSource.getFeatures();
        assertNotNull(features);
        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());
        try {
            Feature feature;
            while (iterator.hasNext()) {
                feature = iterator.next();
                assertNotNull(feature);
            }
        } finally {
            features.close(iterator);
        }
    }

    /**
     * Test method for
     * {@link WFSFeatureSource#getFeatures(org.geotools.data.Query)}.
     * 
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void testGetFeaturesQueryProperties() throws IOException {
        String[] properties = { "the_geom" };
        testGetFeaturesQueryProperties(statesSource, GEOS_STATES_FEATURETYPENAME, properties);

        properties = new String[] { "geometry" };
        testGetFeaturesQueryProperties(govUnitsSource, CUBEWERX_GOVUNITCE_FEATURETYPENAME,
                properties);
    }

    @SuppressWarnings("unchecked")
    private void testGetFeaturesQueryProperties(final FeatureSource<SimpleFeatureType, SimpleFeature> source, final String typeName,
            final String[] propertyNames) throws IOException {

        Query query = new DefaultQuery(typeName, Filter.INCLUDE, propertyNames);
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);
        assertNotNull(features);

        SimpleFeatureType contentType = features.getSchema();
        final int expectedPropertyCount = propertyNames.length;
        Collection<PropertyDescriptor> properties = contentType.getProperties();
        assertEquals(expectedPropertyCount, properties.size());

        Iterator<SimpleFeature> iterator = features.iterator();
        assertTrue(iterator.hasNext());
        try {
            Feature feature;
            while (iterator.hasNext()) {
                feature = iterator.next();
                assertNotNull(feature);
                assertEquals(expectedPropertyCount, feature.getProperties().size());
            }
        } finally {
            features.close(iterator);
        }
    }
}
