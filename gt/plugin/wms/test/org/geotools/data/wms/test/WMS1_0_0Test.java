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

package org.geotools.data.wms.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.geotools.data.ows.BoundingBox;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.Specification;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.resources.TestData;
import org.geotools.xml.DocumentFactory;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.handlers.DocumentHandler;
import org.geotools.xml.schema.Schema;
import org.xml.sax.SAXException;


public class WMS1_0_0Test extends TestCase {
    protected URL server;
    protected Specification spec;

    public WMS1_0_0Test() throws Exception {
        this.spec = new WMS1_0_0();
        this.server = new URL(
                "http://www2.demis.nl/mapserver/Request.asp?wmtver=1.0.0&request=getcapabilities");
    }

    public void testGetVersion() {
        assertEquals(spec.getVersion(), "1.0.0");
    }

    public void testCreateGetCapabilitiesRequest() throws Exception {
        AbstractGetCapabilitiesRequest request = spec.createGetCapabilitiesRequest(server);

        Properties properties = new Properties();

        StringTokenizer tokenizer = new StringTokenizer(request.getFinalURL()
                                                               .getQuery(), "&");

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String[] param = token.split("=");
            properties.setProperty(param[0].toUpperCase(), param[1]);
        }

        checkProperties(properties);

        WebMapServer wms = new WebMapServer(server);
        WMSCapabilities capabilities = wms.getCapabilities();

        assertNotNull(capabilities);
    }
    
    public void testCreateDescribeLayerRequest() throws Exception {
        try {
            spec.createDescribeLayerRequest(null);
            assertTrue("Shouldn't be able to create DescribeLayer requests for version 1.0.0", false);
        } catch (UnsupportedOperationException e) {
            
        }
    }

    protected void checkProperties(Properties properties) {
        assertEquals(properties.getProperty("REQUEST"), "capabilities");
        assertEquals(properties.getProperty("WMTVER"), "1.0.0");
    }

    public void testCreateParser() throws Exception {
        WMSCapabilities capabilities = createCapabilities(
                "1.0.0Capabilities.xml");

        assertEquals(capabilities.getVersion(), "1.0.0");
        assertEquals(capabilities.getService().getName(), "GetMap");
        assertEquals(capabilities.getService().getTitle(), "World Map");

        for (int i = 0; i < capabilities.getService().getKeywordList().length;
                i++) {
            assertEquals(capabilities.getService().getKeywordList()[i],
                "OpenGIS WMS Web Map Server".split(" ")[i]);
        }

        assertEquals(capabilities.getService().getOnlineResource(),
            new URL("http://www2.demis.nl"));
        assertEquals(capabilities.getRequest().getGetCapabilities()
                                 .getFormatStrings()[0],
            "application/vnd.ogc.wms_xml");
        assertEquals(capabilities.getRequest().getGetFeatureInfo().getGet(),
            new URL("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"));
        assertEquals(capabilities.getRequest().getGetMap().getFormatStrings().length,
            4);

        assertEquals(capabilities.getLayers().length, 21);

        Layer[] layers = capabilities.getLayers();
        assertEquals(layers[0].getTitle(), "World Map");
        assertEquals(layers[0].getParent(), null);
        assertTrue(layers[0].getSrs().contains("EPSG:4326"));
        assertTrue(layers[0].getSrs().contains("EPSG:4327"));
        assertEquals(layers[1].getTitle(), "Bathymetry");
        assertEquals(layers[1].getName(), "Bathymetry");
        assertEquals(layers[20].getTitle(), "Ocean features");
        assertEquals(layers[20].getName(), "Ocean features");
        assertEquals(layers[0].getBoundingBoxes().size(), 1);

        BoundingBox bbox = (BoundingBox) layers[1].getBoundingBoxes().get("EPSG:4326");
        assertNotNull(bbox);
    }
    
    public void testCreateGetMapRequest() throws Exception {
        CustomWMS wms = new CustomWMS(server);
        WMSCapabilities caps = wms.getCapabilities();
        GetMapRequest request = wms.createGetMapRequest();
        request.setFormat("image/jpeg");
        System.out.println(request.getFinalURL().toExternalForm());
        
        assertTrue(request.getFinalURL().toExternalForm().indexOf("JPEG") >= 0);
    }

    public void testCreateGetFeatureInfoRequest() throws Exception {
        URL featureURL = new URL("http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
        WebMapServer wms = getCustomWMS(featureURL);
        WMSCapabilities caps = wms.getCapabilities();
        assertNotNull(caps);
        assertNotNull(caps.getRequest().getGetFeatureInfo());
        
        GetMapRequest getMapRequest = wms.createGetMapRequest();

        List simpleLayers = getMapRequest.getAvailableLayers();
        Iterator iter = simpleLayers.iterator();
        while (iter.hasNext()) {
                SimpleLayer simpleLayer = (SimpleLayer) iter.next();
                Object[] styles = simpleLayer.getValidStyles().toArray();
                if (styles.length == 0) {
                        simpleLayer.setStyle("");
                        continue;
                }
                Random random = new Random();
                int randomInt = random.nextInt(styles.length);
                simpleLayer.setStyle((String) styles[randomInt]);
        }
        getMapRequest.setLayers(simpleLayers);

        getMapRequest.setSRS("EPSG:4326");
        getMapRequest.setDimensions("400", "400");
        getMapRequest.setFormat("image/png");

        getMapRequest.setBBox("-114.01268,59.4596930,-113.26043,60.0835794");
        URL url2 = getMapRequest.getFinalURL();

        GetFeatureInfoRequest request = wms.createGetFeatureInfoRequest(getMapRequest);
        request.setQueryLayers(request.getQueryableLayers());
        request.setQueryPoint(200, 200);
        request.setInfoFormat(caps.getRequest().getGetFeatureInfo().getFormatStrings()[0]);
        
        System.out.println(request.getFinalURL());

        GetFeatureInfoResponse response = (GetFeatureInfoResponse) wms.issueRequest(request);
        System.out.println(response.getContentType());
        assertTrue( response.getContentType().indexOf("text/plain") != -1 );
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream()));
        String line;

        boolean textFound = false;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
            if (line.indexOf("Wood Buffalo National Park") != -1) {
                textFound = true;
            }
        }
        assertTrue(textFound);

        
    }
    
    
    /**
     * @param featureURL
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws SAXException
     */
    protected WebMapServer getCustomWMS( URL featureURL ) throws SAXException, URISyntaxException, IOException {
        return new CustomWMS(featureURL);
    }

    protected WMSCapabilities createCapabilities( String capFile ) throws Exception {	
        File getCaps = TestData.file(this, capFile);
        URL getCapsURL = getCaps.toURL();
        Map hints = new HashMap();
        hints.put(DocumentHandler.DEFAULT_NAMESPACE_HINT_KEY, WMSSchema.getInstance());
    	Object object = DocumentFactory.getInstance(getCapsURL.openStream(), hints, Level.FINEST);
    
        Schema schema = WMSSchema.getInstance();
    	SchemaFactory.getInstance(WMSSchema.NAMESPACE);
    			
    	assertTrue("Capabilities failed to parse", object instanceof WMSCapabilities);
    	
    	WMSCapabilities capabilities = (WMSCapabilities) object;
    	return capabilities;
    }


    //forces use of 1.0.0 spec
    private class CustomWMS extends WebMapServer {

        /**
         * @param serverURL
         * @param wait
         * @throws SAXException
         * @throws URISyntaxException
         * @throws IOException
         */
        public CustomWMS( URL serverURL) throws SAXException, URISyntaxException, IOException {
            super(serverURL);
            // TODO Auto-generated constructor stub
        }
        
        protected void setupSpecifications() {
            specs = new Specification[1];
            specs[0] = new WMS1_0_0();
        }
    }
}
