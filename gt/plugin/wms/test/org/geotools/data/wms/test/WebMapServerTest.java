/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WebMapServerTest extends TestCase {

	URL url;
	WMT_MS_Capabilities bork;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//wms = new WebMapServer(new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties"));
		url = new URL("http://terraservice.net/ogccapabilities.ashx?version=1.1.1&request=GetCapabilties");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testWebMapServer() {
	}

	public void testGetCapabilities() throws Exception {
		bork = WebMapServer.getCapabilities(url);
	}
	
	public void testGetMapRequest() throws Exception {
	    bork = WebMapServer.getCapabilities(url);
	    
	    DCPType dcpType = (DCPType) bork.getCapability().getRequest().getGetMap().getDcpTypes().get(0);
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
	    System.out.println(response.getFormat());
	    BufferedImage image = ImageIO.read(response.getResponse());
	    System.out.println(image.getHeight());
	}
}
