package org.geotools.data.wms.test;

import java.net.URL;
import java.util.Properties;

import junit.framework.TestCase;

import org.geotools.data.wms.request.AbstractRequest;
import org.geotools.data.wms.request.Request;

public class AbstractRequestTest extends TestCase {

	public void testGetFinalURL() throws Exception {
		URL badURL = new URL("http://test.com/map.php?LAYERS=Provincial Boundary");
		
		Request request = new RequestTest(badURL, null);
		
		URL finalURL = request.getFinalURL();
		String processedURL = finalURL.toExternalForm();
		assertEquals(processedURL, "http://test.com/map.php?LAYERS=Provincial+Boundary");
	}
	
	private class RequestTest extends AbstractRequest {

		/**
		 * @param onlineResource
		 * @param properties
		 */
		public RequestTest(URL onlineResource, Properties properties) {
			super(onlineResource, properties);
			// TODO Auto-generated constructor stub
		}
		
	}

}
