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
package org.geotools.gce.gtopo30;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.zip.ZipOutputStream;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.test.TestData;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;

/**
 * Purpose of this method is testing the ability of this plugin to write the
 * complete set of files for the GTOPO30 format in a single zip package.
 * 
 * @author Simone Giannecchini
 * @source $URL$
 */
public class GT30ZipWriterTest extends GT30TestBase {
	/**
	 * DOCUMENT ME!
	 * 
	 * @param name
	 */
	public GT30ZipWriterTest(String name) {
		super(name);
	}

	/**
	 * Testing zipped-package writing capabilites.
	 * 
	 * @throws Exception
	 */
	public void test() throws Exception {
		final URL statURL = TestData.getResource(this, this.fileName + ".DEM");
		final AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory()
				.createFormat();

		final TileCache defaultInstance = JAI.getDefaultInstance()
				.getTileCache();
		defaultInstance.setMemoryCapacity(1024 * 1024 * 128);
		defaultInstance.setMemoryThreshold(1.0f);

		if (format.accepts(statURL)) {
			// get a reader
			final GridCoverageReader reader = format.getReader(statURL);

			// get a grid coverage
			final GridCoverage2D gc = ((GridCoverage2D) reader.read(null));

			final File zipFile = TestData.temp(this,"test.zip");
			final ZipOutputStream out = new ZipOutputStream(
					new FileOutputStream(zipFile));

			final GridCoverageWriter writer = format.getWriter(out);
			writer.write(gc, null);
			out.flush();
			out.close();
			gc.dispose();
		}
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run((GT30ZipWriterTest.class));
	} /*
		 * @see TestCase#setUp()
		 */
}
