/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.gce.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import junit.textui.TestRunner;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.test.TestData;


/**
 * TestCase subclass for testing readingb capabilities
 * 
 * @author Simone Giannecchini
 * @author Alessio Fabiani
 * @author rgould
 * @source $URL$
 */
public class WorldImageReaderTest extends WorldImageBaseTestCase {

	private WorldImageReader wiReader;

	private Logger logger = Logger.getLogger(WorldImageReaderTest.class
			.toString());

	/**
	 * Constructor for WorldImageReaderTest.
	 * 
	 * @param arg0
	 */
	public WorldImageReaderTest(String arg0) {
		super(arg0);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testRead() throws IOException {

		// set up
		Object in;

		// checking test data directory for all kind of inputs
		final File test_data_dir = TestData.file(this, null);
		final String[] fileList = test_data_dir.list(new MyFileFilter());
		final int length = fileList.length;
		for (int i = 0; i < length; i++) {
			logger.info(fileList[i]);


			// file
			in = TestData.file(this, fileList[i]);
			this.read(in);

		}

		// checking a WMS get map
//		URL url = new URL(
//				"http://wms.jpl.nasa.gov/wms.cgi?bbox=9,43,12,45&styles=&Format=image/png&request=GetMap&layers=global_mosaic&width=100&height=100&srs=EPSG:4326");
//		// checking that we have an internet connection active and that the
//		// website is up
//		if (url.openConnection() == null)
//			return;
//		this.read(url);
	}

	/**
	 * Read, test and show a coverage from the supplied source.
	 * 
	 * @param source
	 *            Object
	 * 
	 * @throws FileNotFoundException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 * @throws IllegalArgumentException
	 *             DOCUMENT ME!
	 */
	private void read(Object source) throws FileNotFoundException, IOException,
			IllegalArgumentException {

		// can we read it?
		assertTrue(new WorldImageFormat().accepts(source));

		// get a reader
		wiReader = new WorldImageReader(source);


		 // get the coverage
		 final GridCoverage2D coverage = (GridCoverage2D) wiReader.read(null);

		// test the coverage
		assertNotNull(coverage);
		assertNotNull((coverage).getRenderedImage());
		assertNotNull(coverage.getEnvelope());

		// log some information
		logger.info(coverage.getCoordinateReferenceSystem().toWKT());
		logger.info(coverage.getEnvelope().toString());

		// show it, but only if tests are interactive
		if(TestData.isInteractiveTest())
			coverage.show();
		else
			coverage.getRenderedImage().getData();
	}

	public static void main(String[] args) {
		TestRunner.run(WorldImageReaderTest.class);
	}
}
