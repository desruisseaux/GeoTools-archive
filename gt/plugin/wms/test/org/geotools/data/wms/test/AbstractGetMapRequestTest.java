package org.geotools.data.wms.test;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.wms.request.AbstractGetMapRequest;
import org.geotools.data.wms.request.AbstractRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.Request;

public class AbstractGetMapRequestTest extends TestCase {

	public void testGetFinalURL() throws Exception {
		URL badURL = new URL("http://test.com/map.php?LAYERS=Provincial Boundary");
		
		GetMapRequest request = new RequestTest(badURL, null);
		
		request.addLayer("Provincial Boundary", "Two words");
		request.addLayer("Layer2", null);
		
		URL finalURL = request.getFinalURL();
        //System.out.println(finalURL);
		String processedURL = finalURL.toExternalForm();
		assertTrue(processedURL.indexOf("LAYERS=Layer2,Provincial+Boundary") != -1);
		assertTrue(processedURL.indexOf("STYLES=,Two+words") != -1);
        assertTrue(processedURL.indexOf("SERVICE=WMS") != -1);
	}
	
	private class RequestTest extends AbstractGetMapRequest {

		/**
		 * @param onlineResource
		 * @param properties
		 * @param availableLayers
		 * @param availableSRSs
		 * @param availableFormats
		 * @param availableExceptions
		 */
		public RequestTest(URL onlineResource, Properties properties) {
			super(onlineResource, properties);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see org.geotools.data.wms.request.AbstractGetMapRequest#initVersion()
		 */
		protected void initVersion() {
			// TODO Auto-generated method stub
			
		}

	}

}
