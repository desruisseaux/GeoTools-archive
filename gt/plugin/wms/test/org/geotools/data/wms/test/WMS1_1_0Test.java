/*
 * Created on Sep 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;

import org.geotools.data.wms.WMS1_1_0;

import junit.framework.TestCase;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_1_0Test extends TestCase {

	private URL server;
	
	public WMS1_1_0Test() throws Exception {
		server = new URL("http://www2.dmsolutions.ca/cgi-bin/mswms_gmap?VERSION=1.1.0&REQUEST=GetCapabilities");
	}
	
	public void testGetVersion() {
	}

	public void testCreateGetCapabilitiesRequest() {
		WMS1_1_0 spec = new WMS1_1_0();
		System.out.println(spec.createGetCapabilitiesRequest(server).getClass());
	}

	public void testCreateParser() {
	}

	public void testGetName() {
	}

}
