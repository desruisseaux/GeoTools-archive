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
package org.geotools.gce.geotiff;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.factory.Hints;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/*
 * GeoTools - OpenSource mapping toolkit http://geotools.org (C) 2005-2006,
 * GeoTools Project Managment Committee (PMC) This library is free software; you
 * can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; version
 * 2.1 of the License. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 */
/**
 * Testing {@link GeoTiffReader} as well as {@link MetadataDumper}.
 * 
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/test/org/geotools/gce/geotiff/GeoTiffReaderTest.java $
 */
public class GeoTiffReaderTest extends TestCase {
	private final static Logger LOGGER = Logger
			.getLogger(GeoTiffReaderTest.class.toString());

	/**
	 * Constructor for GeoTiffReaderTest.
	 * 
	 * @param arg0
	 */
	public GeoTiffReaderTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		TestRunner.run(GeoTiffReaderTest.class);

	}

	/**
	 * testReader
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws NoSuchAuthorityCodeException
	 */
	public void testReader() throws IllegalArgumentException, IOException,
			NoSuchAuthorityCodeException {

		final File file = TestData.file(GeoTiffReaderTest.class, "");
		final File files[] = file.listFiles();
		final int numFiles = files.length;
		final AbstractGridFormat format = new GeoTiffFormat();
		StringBuffer buffer;
		GridCoverage2D coverage;
		GridCoverageReader reader;
		// MetadataDumper metadataDumper;
		for (int i = 0; i < numFiles; i++) {
			buffer = new StringBuffer();
			final String path = files[i].getAbsolutePath().toLowerCase();
			if (!path.endsWith("tif") && !path.endsWith("tiff"))
				continue;

			buffer.append(files[i].getAbsolutePath()).append("\n");
			if (format.accepts(files[i])) {
				buffer.append("ACCEPTED").append("\n");

				// getting a reader
				reader = new GeoTiffReader(files[i], new Hints(
						Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));

				if (reader != null) {

					// reading the coverage
					coverage = (GridCoverage2D) reader.read(null);

					// Crs
					if (TestData.isInteractiveTest())
						buffer.append("CRS: ").append(
							coverage.getCoordinateReferenceSystem2D().toWKT())
							.append("\n");

					// display metadata
					// metadataDumper = new MetadataDumper(
					// ((GeoTiffReader) reader).getMetadata()
					// .getRootNode());
					// buffer.append("TIFF metadata: ").append(
					//							metadataDumper.getMetadata()).append("\n");
					// showing it
					if (TestData.isInteractiveTest())
						coverage.show();
					else
						coverage.getRenderedImage().getData();

				}

			} else
				buffer.append("NOT ACCEPTED").append("\n");
			LOGGER.info(buffer.toString());
		}

	}
}
