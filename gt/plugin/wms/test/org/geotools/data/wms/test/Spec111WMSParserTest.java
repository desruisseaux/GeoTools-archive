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

import junit.framework.TestCase;

import org.geotools.data.ows.Capabilities;
import org.geotools.data.wms.Spec111WMSParser;
import org.geotools.data.wms.WMSBuilder;
import org.geotools.resources.TestData;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

/**
 * @author Richard Gould
 */
public class Spec111WMSParserTest extends TestCase {
	
	private URL getCapsURL;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File getCaps = TestData.file(this, "getCapabilities.xml");
		getCapsURL = getCaps.toURL();
	}

	public void testConstructCapabilities() throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(getCapsURL);
		
		Spec111WMSParser parser = new Spec111WMSParser();
		
		Capabilities capabilities = parser.constructCapabilities( document, new WMSBuilder() );
		assertNotNull(capabilities);
		assertEquals(capabilities.getService().getName(), "OGC:WMS");
		assertEquals(capabilities.getService().getKeywordList().length, 6);
		
		assertEquals(capabilities.getRequest().getGetCapabilities().getFormats().length, 1);
		assertEquals(capabilities.getLayers()[0].getTitle(), "Microsoft TerraServer Map Server");
		
		assertEquals(capabilities.getVersion(), "1.1.1");
	}

}
