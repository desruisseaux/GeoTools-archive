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
import java.net.URL;
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
        serverURL = new URL(
                "http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
        featureURL = new URL(
                "http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
        brokenURL = new URL("http://afjklda.com");
    }

    /*
     * Class under test for void WebMapServer(URL)
     */
    public void testWebMapServerURL() {
        WebMapServer wms = new WebMapServer(serverURL);

        while (wms.getStatus() == WebMapServer.IN_PROGRESS) {
        }

        assertNotNull(wms.getCapabilities());
    }

    /*
     * Class under test for void WebMapServer(URL, boolean)
     */
    public void testWebMapServerURLboolean() {
        WebMapServer wms = new WebMapServer(serverURL, true);
        assertEquals(wms.getStatus(), WebMapServer.NOTCONNECTED);
        wms.getCapabilities();
        assertEquals(wms.getStatus(), WebMapServer.CONNECTED);
    }

    public void testGetStatus() {
        WebMapServer wms = new WebMapServer(serverURL, true);
        assertEquals(wms.getStatus(), WebMapServer.NOTCONNECTED);
        wms.getCapabilities();
        assertEquals(wms.getStatus(), WebMapServer.CONNECTED);
        wms = new WebMapServer(serverURL);
        assertEquals(wms.getStatus(), WebMapServer.IN_PROGRESS);
        wms = new WebMapServer(brokenURL, true);
        wms.getCapabilities();
        assertEquals(wms.getStatus(), WebMapServer.ERROR);
    }

    public void testGetCapabilities() {
        WebMapServer wms = new WebMapServer(serverURL);

        while (wms.getStatus() == WebMapServer.IN_PROGRESS) {
        }

        assertNotNull(wms.getCapabilities());
    }

    public void testIssueGetMapRequest() throws Exception {
        WebMapServer wms = new WebMapServer(serverURL);

        WMSCapabilities capabilities = wms.getCapabilities();

        GetMapRequest request = wms.createGetMapRequest();

        request.setVersion("1.1.1");

        List simpleLayers = request.getAvailableLayers();
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

        request.setLayers(simpleLayers);

        Set srss = request.getAvailableSRSs();
        request.setSRS((String) srss.iterator().next());
        request.setDimensions("400", "400");

        List formats = request.getAvailableFormats();
        request.setFormat((String) formats.get(0));

        request.setBBox("366800,2170400,816000,2460400");

        //List exceptions = request.getAvailableExceptions();
        //request.setExceptions((String) exceptions.get(0));
        GetMapResponse response = (GetMapResponse) wms.issueRequest(request,
                false);

        assertEquals(response.getContentType(), (String) formats.get(0));

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
    public void testGetProblem() {
        WebMapServer wms = new WebMapServer(brokenURL);
        wms.getCapabilities();
        assertNotNull(wms.getProblem());
    }
    
    public void testIterator() {
        WebMapServer wms = new WebMapServer(serverURL);
        wms.getCapabilities();
        
        Iterator iter = wms.iterator();
        assertNotNull(iter);
        
        while(iter.hasNext()) {
            WMSLayerCatalogEntry entry = (WMSLayerCatalogEntry) iter.next();
            assertNotNull(entry);
            assertEquals(entry.getMetadataNames()[0], WMSLayerMetadataEntity.TYPE_NAME);
            assertNotNull(entry.getDataName());
            WMSLayerMetadataEntity metadata = (WMSLayerMetadataEntity) entry.getMetadata(WMSLayerMetadataEntity.TYPE_NAME);
            assertNotNull(metadata);
            assertNotNull(metadata.getName());
        }
    }
    
    public void testServers() throws MalformedURLException{
    	URL[] servers = new URL[15];
    	servers[0] = new URL("http://wms.jpl.nasa.gov/wms.cgi?VERSION=1.1.1&SERVICE=WMS&REQUEST=GetCapabilities");
    	servers[1] = new URL("http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi?CONFIG=main&SERVICE=WMS&?VERSION=1.1.1&REQUEST=GetCapabilities");
    	servers[2] = new URL("http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
    	servers[3] = new URL("http://wms.cits.rncan.gc.ca/cgi-bin/cubeserv.cgi?VERSION=1.1.0&REQUEST=GetCapabilities");
    	servers[4] = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
    	servers[5] = new URL("http://www2.demis.nl/mapserver/Request.asp?VERSION=1.3.0&SERVICE=WMS&REQUEST=GetCapabilities");
    	servers[6] = new URL("http://datamil.udel.edu/servlet/com.esri.wms.Esrimap?servicename=DE_census2k_sf1&VERSION=1.0.0&request=capabilities");
    	//servers[7] = new URL("http://www.lifemapper.org/Services/WMS/?Service=WMS&VERSION=1.1.1&request=getcapabilities");
    	servers[8] = new URL("http://globe.digitalearth.gov/viz-bin/wmt.cgi?VERSION=1.1.0&Request=GetCapabilities");
    	//servers[9] = new URL("http://www.geographynetwork.ca/wmsconnector/com.esri.wsit.WMSServlet/Geobase_NRN_NewfoundlandAndLabrador_I_Detail?request=GetCapabilities");
    	servers[10] = new URL("http://gisdata.usgs.net/servlet/com.esri.wms.Esrimap?REQUEST=GetCapabilities&VERSION=1.3.0&SERVICE=WMS");
    	
    	for (int i = 0; i < servers.length; i++) {
    		if (servers[i] == null) {
    			continue;
    		}
    		WebMapServer wms = new WebMapServer(servers[i], true);
    		WMSCapabilities capabilities = wms.getCapabilities();
    		Exception problem = wms.getProblem();

    		if (problem == null) {
    			assertNotNull(capabilities);
    			continue;
    		}
    		if (problem instanceof IOException) {
    			continue;
    		}
    		assertTrue(false);
    	}
    }
    
}
