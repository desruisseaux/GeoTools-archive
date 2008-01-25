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

import static org.geotools.wfs.protocol.HttpMethod.GET;
import static org.geotools.wfs.protocol.HttpMethod.POST;
import static org.geotools.wfs.protocol.WFSOperationType.DESCRIBE_FEATURETYPE;
import static org.geotools.wfs.protocol.WFSOperationType.GET_CAPABILITIES;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @URL $URL$
 */
public class WFS110ProtocolHandlerTest extends TestCase {

    WFS110ProtocolHandler protocolHandler;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests methods call this one to set up a protocolHandler to test
     * 
     * @param capabilitiesFileName
     * @param tryGzip
     * @param auth
     * @throws IOException
     */
    private void createProtocolHandler(String capabilitiesFileName, boolean tryGzip,
            Authenticator auth) throws IOException {
        InputStream stream = TestData.openStream(this, capabilitiesFileName);
        protocolHandler = new WFS110ProtocolHandler(stream, tryGzip, auth, "UTF-8");
    }

    /**
     * Test method for
     * {@link WFS110ProtocolHandler#WFS110ProtocolHandler(java.io.InputStream, boolean, java.net.Authenticator, java.lang.String)}.
     * 
     * @throws IOException
     */
    public void testWFS110ProtocolHandler() throws IOException {
        try {
            createProtocolHandler("DescribeFeatureType_States.xml", false, null);
            fail("Excpected DataSourceException as a capabilities document was not provided");
        } catch (DataSourceException e) {
            assertTrue(true);
        }
        try {
            InputStream badData = new ByteArrayInputStream(new byte[1024]);
            protocolHandler = new WFS110ProtocolHandler(badData, false, null, "UTF-8");
            fail("Excpected DataSourceException as a capabilities document was not provided");
        } catch (DataSourceException e) {
            assertTrue(true);
        }

        createProtocolHandler("geoserver_capabilities_1_1_0.xml", false, null);
        assertNotNull(protocolHandler);

        assertEquals("My GeoServer WFS", protocolHandler.getServiceTitle());
        assertEquals("This is a description of your Web Feature Server.", protocolHandler
                .getServiceAbstract());
        assertNotNull(protocolHandler.getServiceProviderUri());
        assertEquals("http://www.geoserver.org", protocolHandler.getServiceProviderUri().toString());
    }

    /**
     * Test method for
     * {@link WFS110ProtocolHandler#supports(org.geotools.wfs.protocol.WFSOperationType, org.geotools.wfs.protocol.HttpMethod)}.
     * 
     * @throws IOException
     */
    public void testSupports() throws IOException {
        createProtocolHandler("geoserver_capabilities_1_1_0.xml", false, null);
        assertTrue(protocolHandler.supports(DESCRIBE_FEATURETYPE, GET));
        // post was deliberately left off on the test capabilities file
        assertFalse(protocolHandler.supports(DESCRIBE_FEATURETYPE, POST));
    }

    /**
     * Test method for
     * {@link WFS110ProtocolHandler#getOperationURL(org.geotools.wfs.protocol.WFSOperationType, org.geotools.wfs.protocol.HttpMethod)}.
     */
    public void testGetOperationURL() throws IOException {
        createProtocolHandler("geoserver_capabilities_1_1_0.xml", false, null);
        final URL expectedGet = new URL("http://localhost:8080/geoserver/wfs/get?");
        final URL expectedPost = new URL("http://localhost:8080/geoserver/wfs/post?");
        assertEquals(expectedGet, protocolHandler.getOperationURL(GET_CAPABILITIES, GET));
        assertEquals(expectedPost, protocolHandler.getOperationURL(GET_CAPABILITIES, POST));
    }

    /**
     * Test method for {@link WFS110ProtocolHandler#getCapabilitiesTypeNames()}.
     * 
     * @throws IOException
     */
    public void testGetCapabilitiesTypeNames() throws IOException {
        createProtocolHandler("geoserver_capabilities_1_1_0.xml", false, null);
        String[] names = protocolHandler.getCapabilitiesTypeNames();
        assertNotNull(names);
        assertEquals(3, names.length);
        Set<String> typeNames = new HashSet<String>(Arrays.asList(names));
        assertTrue(typeNames.contains("topp:states"));
        assertTrue(typeNames.contains("sf:archsites"));
        assertTrue(typeNames.contains("tiger:tiger_roads"));
    }

    /**
     * Test method for
     * {@link WFS110ProtocolHandler#parseDescribeFeatureType(String)}
     * 
     * @throws IOException
     */
    public void testParseFeatureType() throws Exception {
        InputStream stream = TestData.openStream(this, "geoserver_capabilities_1_1_0.xml");
        protocolHandler = new WFS110ProtocolHandler(stream, false, null, "UTF-8") {
            @Override
            public URL getDescribeFeatureTypeURLGet(final String typeName)
                    throws MalformedURLException {
                return TestData.getResource(this, "DescribeFeatureType_States.xml");
            }
        };

        SimpleFeatureType ftype = protocolHandler.parseDescribeFeatureType("topp:states");
        assertNotNull(ftype);
    }

}
