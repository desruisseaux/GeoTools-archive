/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.gce.geotiff;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Logger;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.referencing.FactoryException;

/**
 * @author Simone Giannecchini
 * 
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/geotiff/test/org/geotools/gce/geotiff/GeoTiffVisualizationTest.java $
 */
public class GeoTiffWriterTest extends TestCase {
	private static final Logger logger = Logger
			.getLogger(GeoTiffWriterTest.class.toString());

	/**
	 * 
	 */
	public GeoTiffWriterTest() {
		super("Writer Test!");
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(GeoTiffWriterTest.class);

	}
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final JAI jaiDef = JAI.getDefaultInstance();

		// using a big tile cache
		final TileCache cache = jaiDef.getTileCache();
		cache.setMemoryCapacity(64 * 1024 * 1024);
		cache.setMemoryThreshold(0.75f);

		// setting JAI wide hints
//		jaiDef.getTileScheduler().setParallelism(40);
//		jaiDef.getTileScheduler().setPrefetchParallelism(40);
//		jaiDef.getTileScheduler().setPrefetchPriority(7);
//		jaiDef.getTileScheduler().setPrefetchPriority(7);
		
	
	}
	/**
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws UnsupportedOperationException
	 * @throws ParseException
	 * @throws FactoryException
	 */
	public void testWriter() throws IllegalArgumentException, IOException,
			UnsupportedOperationException, ParseException, FactoryException {
		final File readdir = TestData.file(GeoTiffWriterTest.class, "");
		final File writedir = new File(new StringBuffer(readdir
				.getAbsolutePath()).append("/testWriter/").toString());
		writedir.mkdir();
		final File files[] = readdir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				// are they tiff?
				if (!name.endsWith("tif") && !name.endsWith("tiff"))
					return false;

				// are they geotiff?
				return new GeoTiffFormat().accepts(new File(new StringBuffer(
						dir.getAbsolutePath()).append(File.separatorChar)
						.append(name).toString()));

			}
		});
		final int numFiles = files.length;
		final AbstractGridFormat format = new GeoTiffFormat();
		GridCoverageReader reader = null;
		GridCoverageWriter writer = null;
		GridCoverage2D gc = null;
		for (int i = 0; i < numFiles; i++) {

			logger.info(files[i].getAbsolutePath());

			// getting a reader
			reader = new GeoTiffReader(files[i], null);

			if (reader != null) {

				// reading the coverage
				gc = (GridCoverage2D) reader.read(null);
				logger.info("before");
				logger.info(gc.getCoordinateReferenceSystem().toWKT());
				logger.info(gc.getEnvelope().toString());
				logger.info(gc.getRenderedImage().toString());
				logger.info("");

				if (gc != null) {
					final File writeFile = new File(new StringBuffer(writedir
							.getAbsolutePath()).append(File.separatorChar)
							.append(gc.getName().toString()).append(".tiff")
							.toString());
					writer = format.getWriter(writeFile);
					writer.write(gc, null);

					reader = new GeoTiffReader(writeFile, null);
					gc = (GridCoverage2D) reader.read(null);

					logger.info("after");
					logger.info(gc.getCoordinateReferenceSystem().toWKT());
					logger.info(gc.getEnvelope().toString());
					logger.info("");
					if(TestData.isInteractiveTest())
						gc.show();

				}

			}

		}

	}
}
