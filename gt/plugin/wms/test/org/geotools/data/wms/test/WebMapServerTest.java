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

import javax.imageio.ImageIO;

import org.geotools.data.wms.GetMapRequest;
import org.geotools.data.wms.GetMapResponse;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.getCapabilities.DCPType;
import org.geotools.data.wms.getCapabilities.Get;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;

import junit.framework.TestCase;

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
	    
	    DCPType dcpType = (DCPType) capabilities.getCapability().getRequest().getGetMap().getDcpTypes().get(0);
	    Get get = (Get) dcpType.getHttp().getGets().get(0);
	    
	    GetMapRequest request = new GetMapRequest(get.getOnlineResource());
	    request.setVersion("1.1.1");
	    request.setLayers("DRG");
	    request.setStyles("");
	    request.setSRS("EPSG:26904");
	    request.setDimensions("400", "400");
	    request.setFormat("image/jpeg");
	    request.setBBox("366800,2170400,816000,2460400");
	    
	    GetMapResponse response = WebMapServer.issueGetMapRequest(request);
	    assertEquals(response.getFormat(), "image/jpeg");
	    BufferedImage image = ImageIO.read(response.getResponse());
	    assertEquals(image.getHeight(), 400);
	}
	
	public void testGetProblem() {
		WebMapServer wms = new WebMapServer(brokenURL);
		wms.getCapabilities();
		assertNotNull(wms.getProblem());
	}

}
