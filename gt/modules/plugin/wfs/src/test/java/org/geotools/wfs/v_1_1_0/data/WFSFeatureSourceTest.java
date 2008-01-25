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

import org.geotools.data.ResourceInfo;

/**
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public class WFSFeatureSourceTest extends DataTestSupport {

    private WFS_1_1_0_DataStore dataStore;

    private WFSFeatureSource statesSource;

    protected void setUp() throws Exception {
        super.setUp();
        createProtocolHandler("geoserver_capabilities_1_1_0.xml", false, null);
        dataStore = new WFS_1_1_0_DataStore(protocolHandler);
        //statesSource = dataStore.getFeatureSource("topp:states");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        dataStore = null;
        statesSource = null;
    }
    
    public void testCreate(){
        try{
            new WFSFeatureSource(dataStore, "nonExistentTypeName", protocolHandler);
            fail("Expected IOException for a non existent type name");
        }catch(IOException e){
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
     */
    public void testGetFeaturesQuery() {
        fail("Not yet implemented");
    }

}
