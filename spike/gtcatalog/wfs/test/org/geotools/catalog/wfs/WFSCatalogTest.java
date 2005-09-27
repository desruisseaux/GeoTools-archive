package org.geotools.catalog.wfs;

import java.net.URI;
import java.util.Map;

import org.geotools.catalog.Service;
import org.geotools.catalog.wfs.WFSServiceFactory;

import junit.framework.TestCase;

public class WFSCatalogTest extends TestCase {
	
	URI uri;
	WFSServiceFactory ext;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		uri = new URI("http://www.refractions.net:8080/geoserver/wfs/GetCapabilities");
		ext = new WFSServiceFactory();
	}
	
	public void testCanProcess() {
		assertTrue(ext.canProcess(uri));
	}
	
	public void testCreateParams() {
		Map params = ext.createParams(uri);
		assertNotNull(params);
		assertFalse(params.isEmpty());
	}
	
	public void testCreateService() {
		Map params = ext.createParams(uri);
		Service service = ext.createService(null,uri,params);
		assertNotNull(service);
	}
	
}
