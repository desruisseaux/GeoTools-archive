/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.wfs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.naming.OperationNotSupportedException;

import junit.framework.TestCase;

import org.geotools.feature.IllegalAttributeException;
import org.xml.sax.SAXException;

/**
 * @author dzwiers
 * @since 0.6.0
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/wfs/test/org/geotools/data/wfs/IonicTest.java $
 */
public class IonicTest extends TestCase {

	private URL url = null;

	public IonicTest() throws MalformedURLException {
		url = new URL(
				"http://webservices.ionicsoft.com/ionicweb/wfs/BOSTON_ORA?version=1.0.0&request=getcapabilities&service=WFS");
	}

	public void testFeatureType() throws NoSuchElementException, IOException,
			SAXException {
		try {
			WFSDataStoreReadTest.doFeatureType(url, true, true, 0);
		} catch (IOException e) {
			skipHttpErrors(e);
		}
	}

	public void testFeatureReader() throws NoSuchElementException, IOException,
			IllegalAttributeException, SAXException {
		// FAILS due to Choice !!!
		try {
			WFSDataStoreReadTest.doFeatureReader(url, true, true, 0);
		} catch (IOException e) {
			skipHttpErrors(e);
		}
	}

	public void testFeatureReaderWithFilter() throws NoSuchElementException,
			OperationNotSupportedException, IllegalAttributeException,
			IOException, SAXException {
		try {
			WFSDataStoreReadTest.doFeatureReaderWithFilter(url, true, true, 0);
		} catch (IOException e) {
			skipHttpErrors(e);
		}
	}

	/**
	 * I the exception is a HTTP failure, skip the test to avoid breaking the build
	 * @param e
	 * @throws IOException
	 */
	private void skipHttpErrors(IOException e) throws IOException {
		if (e.getMessage().indexOf("Server returned HTTP response code:") == -1)
			throw e;
		System.out.println("WARNING, skipping test due to HTTP error: "
				+ e.getMessage());
	}
}
