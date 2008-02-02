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

import org.geotools.data.ResourceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.test.TestData;
import org.geotools.wfs.protocol.ConnectionFactory;
import org.geotools.wfs.protocol.DefaultConnectionFactory;

/**
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/wfs/src/test/java/org/geotools/wfs/v_1_1_0/data/WFSFeatureSourceTest.java $
 */
public class WFSFeatureSourceTest extends DataTestSupport {

    private WFS_1_1_0_DataStore dataStore;

    private WFSFeatureSource statesSource;

    protected void setUp() throws Exception {
        super.setUp();
        createTestProtocolHandler(DataTestSupport.GEOS_CAPABILITIES);
        dataStore = new WFS_1_1_0_DataStore(protocolHandler);
        statesSource = dataStore.getFeatureSource("topp:states");
    }

    private void createTestProtocolHandler(String capabilitiesFileName) throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        ConnectionFactory cf = new DefaultConnectionFactory();
        protocolHandler = new WFS110ProtocolHandler(stream, cf, Integer.valueOf(0)) {
            @Override
            public URL getDescribeFeatureTypeURLGet(final String typeName)
                    throws MalformedURLException {
                if (GEOS_STATES_FEATURETYPENAME.equals(typeName)) {
                    String schemaLocation = DataTestSupport.GEOS_STATES_SCHEMA;
                    URL url = TestData.getResource(this, schemaLocation);
                    assertNotNull(url);
                    return url;
                }
                throw new IllegalArgumentException("unknown typename: " + typeName);
            }
        };
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dataStore = null;
        statesSource = null;
    }

    public void testCreate() throws IOException {
        try {
            new WFSFeatureSource(dataStore, "nonExistentTypeName", protocolHandler);
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
     * Test method for
     * {@link WFSFeatureSource#getFeatures(org.geotools.data.Query)}.
     * 
     * @throws IOException
     */
    public void testGetFeaturesQuery() throws IOException {
        FeatureCollection features = statesSource.getFeatures();
        assertNotNull(features);
    }

}
