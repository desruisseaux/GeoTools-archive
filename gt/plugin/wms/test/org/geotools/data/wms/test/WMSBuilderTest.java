/*
 * Created on Aug 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.wms.WMSBuilder;
import org.geotools.data.wms.capabilities.Capabilities;

import junit.framework.TestCase;

/**
 * @author Kefka
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSBuilderTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFinish() throws Exception {
		WMSBuilder builder = new WMSBuilder();
		builder.buildCapabilities("1.1.1");
		builder.buildService("FakeService", "Test", new URL("http://online.com"), "nothin", null);
		builder.buildGetCapabilitiesOperation(null, new URL("http://get.com"), new URL("http://post.com"));
		List formats = new ArrayList();
		formats.add( "image/jpeg" );
		builder.buildGetMapOperation( formats, new URL("http://get.com"), new URL("http://post.com"));
		builder.buildLayer("Layer1", "layer1", true, null);
		builder.buildSRS("EPSG:blah");
		builder.buildSRS("EPSG:2");
		builder.buildStyle("Style1");
		builder.buildStyle("Style2");
		builder.buildLayer("Layer2", "layer2", false, null);
		builder.buildSRS("EPSG:3");
		builder.buildStyle("Style3");
		
		Capabilities capabilities = builder.finish();
		assertEquals(capabilities.getVersion(), "1.1.1");
		assertEquals(capabilities.getService().getName(), "FakeService");
		assertEquals(capabilities.getService().getTitle(), "Test");
		assertEquals(capabilities.getRequest().getGetCapabilities().getGet(), new URL("http://get.com"));
		assertEquals(capabilities.getRequest().getGetMap().getFormats()[0], "image/jpeg");
		assertEquals(capabilities.getLayers()[0].getName(), "layer1");
		assertEquals(capabilities.getLayers()[1].getTitle(), "Layer2");
		assertEquals((String) capabilities.getLayers()[1].getSrs().get(0), "EPSG:3" );
	}

}
