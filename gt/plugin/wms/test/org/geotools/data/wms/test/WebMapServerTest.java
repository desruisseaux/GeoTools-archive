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
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.data.wms.GetMapRequest;
import org.geotools.data.wms.GetMapResponse;
import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WebMapServerTest extends TestCase {

	URL serverURL;
	URL brokenURL;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		serverURL = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
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
	    
	    WMT_MS_Capabilities capabilities = wms.getCapabilities();
	    
	    GetMapRequest request = wms.createGetMapRequest();
	    
	    List simpleLayers = request.getAvailableLayers();
	    Iterator iter = simpleLayers.iterator();
	    while (iter.hasNext()) {
	    	SimpleLayer simpleLayer = (SimpleLayer) iter.next();
	    	Object[] styles = simpleLayer.getValidStyles().toArray();
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
	    
	    List exceptions = request.getAvailableExceptions();
	    request.setExceptions((String) exceptions.get(0));
	        
	    GetMapResponse response = wms.issueGetMapRequest(request, false);
	    
	    assertEquals(response.getFormat(), (String) formats.get(0));
	    BufferedImage image = ImageIO.read(response.getResponse());
	    assertEquals(image.getHeight(), 400);
	}
	
	public void testGetProblem() {
		WebMapServer wms = new WebMapServer(brokenURL);
		wms.getCapabilities();
		assertNotNull(wms.getProblem());
	}

}
