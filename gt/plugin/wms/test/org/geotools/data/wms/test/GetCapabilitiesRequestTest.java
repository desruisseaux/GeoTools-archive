/*
 * Created on Sep 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.geotools.data.wms.request.AbstractGetCapabilitiesRequest;

import junit.framework.TestCase;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetCapabilitiesRequestTest extends TestCase {
	
	public void testGetCapabilitiesRequest() throws Exception {
		URL testURL = new URL("http://office.refractions.net:4001/cgi-bin/mapserv?map=/opt/dra2/orthophotos/tiles.map&");
		AbstractGetCapabilitiesRequest request = new Request(testURL);
		URL finalURL = request.getFinalURL();
		
        int index = finalURL.toExternalForm().lastIndexOf("?");
        String urlWithoutQuery = null;
        urlWithoutQuery = finalURL.toExternalForm().substring(0, index);
        
        assertEquals(urlWithoutQuery, "http://office.refractions.net:4001/cgi-bin/mapserv");

        HashMap map = new HashMap();
        map.put("VERSION", "1.1.1");
        map.put("MAP", "/opt/dra2/orthophotos/tiles.map");
        map.put("REQUEST", "GetCapabilities");
        map.put("SERVICE", "WMS");
        
		StringTokenizer tokenizer = new StringTokenizer(finalURL.getQuery(), "&");

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			String[] param = token.split("=");
			
			assertEquals( (String) map.get(param[0]), param[1]);
		}
	}
	
	protected class Request extends AbstractGetCapabilitiesRequest {

		/**
		 * @param serverURL
		 */
		public Request(URL serverURL) {
			super(serverURL);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetCapabilitiesRequest#initVersion()
		 */
		protected void initVersion() {
			setProperty("VERSION", "1.1.1");
		}
		
	}

}
