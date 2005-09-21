package org.geotools.gtcatalog.wfs;

import java.net.URI;
import java.util.Map;

import org.geotools.gtcatalog.Service;
import org.geotools.gtcatalog.wfs.WFSServiceExtension;

import junit.framework.TestCase;

public class WFSCatalogTest extends TestCase {
	
	URI uri;
	WFSServiceExtension ext;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		uri = new URI("http://www.refractions.net:8080/geoserver/wfs/GetCapabilities");
		ext = new WFSServiceExtension();
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
