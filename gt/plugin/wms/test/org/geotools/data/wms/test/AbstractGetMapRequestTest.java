package org.geotools.data.wms.test;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.geotools.data.wms.SimpleLayer;
import org.geotools.data.wms.request.AbstractGetMapRequest;
import org.geotools.data.wms.request.AbstractRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.request.Request;

public class AbstractGetMapRequestTest extends TestCase {

	public void testGetFinalURL() throws Exception {
		URL badURL = new URL("http://test.com/map.php?LAYERS=Provincial Boundary");
		
		GetMapRequest request = new RequestTest(badURL, null, new SimpleLayer[] { }, 
				null, new String[] { } , null);
		
		SimpleLayer layer = new SimpleLayer("Provincial Boundary", "Two words");
		request.setLayers(Collections.singletonList(layer));
		
		URL finalURL = request.getFinalURL();
		String processedURL = finalURL.toExternalForm();
		assertTrue(processedURL.indexOf("LAYERS=Provincial+Boundary") != -1);
		assertTrue(processedURL.indexOf("STYLES=Two+words") != -1);
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
		public RequestTest(URL onlineResource, Properties properties, SimpleLayer[] availableLayers, Set availableSRSs, String[] availableFormats, List availableExceptions) {
			super(onlineResource, properties, availableLayers, availableSRSs,
					availableFormats, availableExceptions);
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
