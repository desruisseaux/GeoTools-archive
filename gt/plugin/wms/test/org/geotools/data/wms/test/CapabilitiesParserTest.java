/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.wms.test;

import java.io.File;
import java.net.URL;

import org.geotools.data.wms.CapabilitiesParser;
import org.geotools.data.wms.getCapabilities.WMT_MS_Capabilities;
import org.geotools.resources.TestData;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import junit.framework.TestCase;

/**
 * @author Richard Gould
 */
public class CapabilitiesParserTest extends TestCase {
	
	private URL getCapsURL;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File getCaps = TestData.file(this, "getCapabilities.xml");
		getCapsURL = getCaps.toURL();
	}

	public void testParseCapabilities() throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(getCapsURL);

		Element root = document.getRootElement(); //Root = "WMT_MS_Capabilities"
		WMT_MS_Capabilities capabilities = CapabilitiesParser.parseCapabilities(root);
		assertNotNull(capabilities);
		assertEquals(capabilities.getService().getName(), "OGC:WMS");
		assertEquals(capabilities.getService().getKeywordList().size(), 6);
		assertEquals(capabilities.getService().getContactInformation().getContactPersonPrimary().getContactOrganization(), "Microsoft");
		assertEquals(capabilities.getService().getFees(), "none");
		
		assertEquals(capabilities.getCapability().getRequest().getGetCapabilities().getFormats().size(), 1);
		assertEquals(capabilities.getCapability().getException().getFormats().size(), 4);
		assertEquals(capabilities.getCapability().getLayer().getTitle(), "Microsoft TerraServer Map Server");
		
		assertEquals(capabilities.getCapability().getLayer().getAttribution().getTitle(), "U.S. Geological Survey");
		assertEquals(capabilities.getCapability().getLayer().getAttribution().getLogoURL().getFormat(), "image/gif");
		assertEquals(capabilities.getCapability().getLayer().getSubLayers().size(), 3);
		
		assertEquals(capabilities.getVersion(), "1.1.1");
		assertEquals(capabilities.getUpdateSequence(), "0");
	}

}
