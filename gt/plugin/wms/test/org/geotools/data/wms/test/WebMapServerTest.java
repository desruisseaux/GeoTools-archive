/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.WMSLayerCatalogEntry;
import org.geotools.data.wms.WMSLayerMetadataEntity;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.xml.sax.SAXException;


/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WebMapServerTest extends TestCase {
    URL serverURL;
    URL brokenURL;
    private URL featureURL;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        serverURL = new URL("http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi?CONFIG=main&SERVICE=WMS&?VERSION=1.3.0&REQUEST=GetCapabilities");
        //serverURL = new URL(
         //       "http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
        featureURL = new URL(
                "http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
        brokenURL = new URL("http://afjklda.com");
    }

    /*
     * Class under test for void WebMapServer(URL)
     */
    public void testWebMapServerURL() throws Exception {
        WebMapServer wms = new WebMapServer(serverURL);

        assertNotNull(wms.getCapabilities());
    }

    public void testGetCapabilities() throws Exception {
        WebMapServer wms = new WebMapServer(serverURL);

        assertNotNull(wms.getCapabilities());
    }

    public void testIssueGetMapRequest() throws Exception {
        WebMapServer wms = new WebMapServer(serverURL);

        WMSCapabilities capabilities = wms.getCapabilities();

        GetMapRequest request = wms.createGetMapRequest();

        //request.setVersion("1.1.1");

        List simpleLayers = new ArrayList();
        Iterator iter = request.getAvailableLayers().iterator();
        int count = -1;
        while (iter.hasNext()) {

            SimpleLayer simpleLayer = (SimpleLayer) iter.next();
            
            count++;
            if (count >= 5) { 
                break;
            }
            
            simpleLayers.add(simpleLayer);
            
            Object[] styles = simpleLayer.getValidStyles().toArray();

            if (styles.length == 0) {
                simpleLayer.setStyle("");

                continue;
            }

            Random random = new Random();
            int randomInt = random.nextInt(styles.length);
            simpleLayer.setStyle((String) styles[randomInt]);

        }

        request.setLayers(simpleLayers);

        Set srss = request.getAvailableSRSs();
        request.setSRS((String) srss.iterator().next());
        request.setDimensions("400", "400");

        String format = "image/gif";
        List formats = request.getAvailableFormats();
        if (!formats.contains("image/gif")) {
            format = (String) formats.get(0);
        } 
        request.setFormat(format);

        request.setBBox("366800,2170400,816000,2460400");

        System.out.println(request.getFinalURL());
        GetMapResponse response = (GetMapResponse) wms.issueRequest(request);

        assertEquals(response.getContentType(), format);

        BufferedImage image = ImageIO.read(response.getInputStream());
        assertEquals(image.getHeight(), 400);
    }

    //TODO This test is offline pending writing of a 1.1.0 parser.

    /*public void testIssueGetFeatureInfoRequest() throws Exception {
            WebMapServer wms = new WebMapServer(featureURL, true);
            wms.getCapabilities();
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

        getMapRequest.setSRS("EPSG:42304");
        getMapRequest.setDimensions("400", "400");
        getMapRequest.setFormat("image/jpeg");

        getMapRequest.setBBox("-2.2e+06,-712631,3.0728e+06,3.84e+06");
        URL url2 = getMapRequest.getFinalURL();

            GetFeatureInfoRequest request = wms.createGetFeatureInfoRequest(getMapRequest);
            request.setQueryLayers(request.getQueryableLayers());
            request.setQueryPoint(200, 200);
            request.setInfoFormat("application/vnd.ogc.gml");
            URL url = request.getFinalURL();

            GetFeatureInfoResponse response = (GetFeatureInfoResponse) wms.issueRequest(request, false);
            assertEquals("application/vnd.ogc.gml", response.getContentType());
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getInputStream()));
    String line;

            while ((line = in.readLine()) != null) {
        System.out.println(line);
    }

    }*/
    
    public void testIterator() throws SAXException, URISyntaxException, IOException {
        WebMapServer wms = new WebMapServer(serverURL);
        wms.getCapabilities();
        
        Iterator iter = wms.entries().iterator();
        assertNotNull(iter);
        
        while(iter.hasNext()) {
            WMSLayerCatalogEntry entry = (WMSLayerCatalogEntry) iter.next();
            assertNotNull(entry);            
            assertTrue( entry.metadata().keySet().contains( WMSLayerMetadataEntity.TYPE_NAME) );
            assertNotNull(entry.getDataName());            
            WMSLayerMetadataEntity metadata = (WMSLayerMetadataEntity) entry.metadata().get(WMSLayerMetadataEntity.TYPE_NAME);
            assertNotNull(metadata);
            assertNotNull(metadata.getName());
        }
    }
    

    
}
