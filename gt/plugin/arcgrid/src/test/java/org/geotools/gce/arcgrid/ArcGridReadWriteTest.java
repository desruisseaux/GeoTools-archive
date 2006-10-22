/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.arcgrid;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Test reading and writing arcgrid grid coverages.
 * 
 * @author rschulz
 * @source $URL$
 */
public class ArcGridReadWriteTest extends ArcGridBaseTestCase {
	private final Random generator = new Random();

	/**
	 * Creates a new instance of ArcGridReadWriteTest
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 */
	public ArcGridReadWriteTest(String name) {
		super(name);
	}

	/**
	 * 
	 * @param testParam
	 * @throws Exception
	 */
	public void test(final File testFile) throws Exception {
		testStandardReadWrite(testFile);
		directReadWrite(testFile);
	}

	private void testStandardReadWrite(final File testFile) throws Exception {
		/* Read the grid coverate */
		GridCoverageReader reader = new ArcGridReader(testFile);
		final GridCoverage2D origin = (GridCoverage2D) reader.read(null);
		reader.dispose();

		writeAndRead(origin, false, false);
		writeAndRead(origin, true, false);
		writeAndRead(origin, false, true);
		writeAndRead(origin, true, true);
	}

	private void directReadWrite(final File testFile) throws Exception {
		if (testFile.getName().indexOf("spearfish")!=-1) {
			/* Read the grid coverate */
			GRASSArcGridRaster raster = new GRASSArcGridRaster(testFile.toURL());
			raster.toString(); // only to assess it doesn't crashes
			WritableRaster wr1 = raster.readRaster();

			File tmpFile = File.createTempFile(testFile.getName(), Double
					.toString(generator.nextDouble()));
			GRASSArcGridRaster raster2 = new GRASSArcGridRaster(tmpFile.toURL());
			raster2.writeRaster(wr1, raster.getXlCorner(),
					raster.getYlCorner(), raster.getCellSize(), false);

			WritableRaster wr2 = raster.readRaster();
			compareRasters(wr1, wr2);
		} else {
			/* Read the grid coverate */
			ArcGridRaster raster = new ArcGridRaster(testFile.toURL());
			raster.toString(); // only to assess it doesn't crashes
			WritableRaster wr1 = raster.readRaster();

			File tmpFile = File.createTempFile(testFile.getName(), Double
					.toString(generator.nextDouble()));
			ArcGridRaster raster2 = new ArcGridRaster(tmpFile.toURL());
			raster2.writeRaster(wr1, raster.getXlCorner(),
					raster.getYlCorner(), raster.getCellSize(), false);

			WritableRaster wr2 = raster.readRaster();
			compareRasters(wr1, wr2);
		}
	}

	private void writeAndRead(final GridCoverage2D origin,
			final boolean compressed, final boolean grass) throws IOException,
			Exception {
		// create a temporary output file
		final File tmpFile;
		tmpFile = File.createTempFile(origin.getName().toString(), Double
				.toString(generator.nextDouble()));

		GridCoverageReader reader;
		/* write it */
		final GridCoverageWriter writer = new ArcGridWriter(tmpFile);
		ParameterValueGroup params = writer.getFormat().getWriteParameters();
		params.parameter("Compressed").setValue(compressed);
		params.parameter("GRASS").setValue(grass);
		writer.write(origin, null);

		/* read it again and compared them */
		reader = new ArcGridReader(tmpFile);
		params = reader.getFormat().getReadParameters();
		params.parameter("Compressed").setValue(compressed);
		params.parameter("GRASS").setValue(grass);
		final GridCoverage2D created = (GridCoverage2D) reader.read(null);

		// check that the original and temporary grid are the same
		compare(origin, created);
	}

	/**
	 * Compares 2 grid covareages, throws an exception if they are not the same.
	 * 
	 * @param gc1
	 * @param gc2
	 * @throws Exception
	 */
	void compare(GridCoverage gc1, GridCoverage gc2) throws Exception {
		final GeneralEnvelope e1 = (GeneralEnvelope) gc1.getEnvelope();
		final GeneralEnvelope e2 = (GeneralEnvelope) gc2.getEnvelope();

		if ((e1.getLowerCorner().getOrdinate(0) != e1.getLowerCorner()
				.getOrdinate(0))
				|| (e1.getLowerCorner().getOrdinate(1) != e1.getLowerCorner()
						.getOrdinate(1))
				|| (e1.getUpperCorner().getOrdinate(0) != e1.getUpperCorner()
						.getOrdinate(0))
				|| (e1.getUpperCorner().getOrdinate(1) != e1.getUpperCorner()
						.getOrdinate(1))) {
			throw new Exception("GridCoverage Envelopes are not equal"
					+ e1.toString() + e2.toString());
		}

		if (e1.getCoordinateReferenceSystem().toWKT().compareToIgnoreCase(
				e2.getCoordinateReferenceSystem().toWKT()) != 0) {
			throw new Exception("GridCoverage Envelopes are not equal"
					+ e1.getCoordinateReferenceSystem().toWKT()
					+ e2.getCoordinateReferenceSystem().toWKT());
		}

		final Raster r1 = ((GridCoverage2D) gc1).getRenderedImage().getData();
		final Raster r2 = ((GridCoverage2D) gc2).getRenderedImage().getData();
		compareRasters(r1, r2);
	}

	private void compareRasters(final Raster r1, final Raster r2)
			throws Exception {
		final int width = r1.getWidth();
		final int height = r1.getHeight();
		double[] values1 = null;
		double[] values2 = null;
		for (int i = r1.getMinX(); i < width; i++) {
			for (int j = r1.getMinY(); j < height; j++) {
				values1 = r1.getPixel(i, j, values1);
				values2 = r2.getPixel(i, j, values2);
				final int length = values1.length;
				for (int k = 0; k < length; k++) {
					if (!(Double.isNaN(values1[k]) && Double.isNaN(values2[k]))
							&& (values1[k] != values2[k])) {
						throw new Exception(
								"GridCoverage Values are not equal: "
										+ values1[k] + ", " + values2[k]);
					}
				}
			}
		}
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ArcGridReadWriteTest.class);
	}
}
