/*
 * Created on Sep 12, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;
import java.util.Properties;

import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMS1_3_0;
import org.geotools.data.wms.WMSParser;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMS1_3_0Test extends WMS1_1_1Test{

	public WMS1_3_0Test() throws Exception {
		this.spec = new WMS1_3_0();
		this.server = new URL("http://www2.demis.nl/mapserver/Request.asp?VERSION=1.3.0&SERVICE=WMS&REQUEST=GetCapabilities");
	}

	public void testGetVersion() {
		assertEquals(spec.getVersion(), "1.3.0");
	}

	protected void checkProperties(Properties properties) {
        assertEquals(properties.get("VERSION"), "1.3.0");
        assertEquals(properties.get("REQUEST"), "GetCapabilities");
        assertEquals(properties.get("SERVICE"), "WMS");
	}
	
	protected void parserCheck(WMSParser parser) {
		assertEquals(parser.getClass(), WMS1_3_0.Parser.class);
	}
	
	public void testCreateParser() throws Exception {
		WMSCapabilities capabilities = createCapabilities("1.3.0Capabilities.xml");
		
		assertNotNull(capabilities);
	}

}
