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
/*
 * ArcGridReadWriteTest.java
 *
 * Created on September 2, 2004, 9:26 PM
 */
package org.geotools.gce.arcgrid;

import java.awt.image.Raster;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.TestData;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Test reading and writing arcgrid grid coverages.
 * 
 * @author Daniele Romagnoli
 * @author Simone Giannecchini (simboss)
 */
public class ArcGridReadWriteTest extends ArcGridBaseTestCase {
	private final Random generator = new Random();

	final static boolean readSubSampled = true;

	final static boolean writeEsriCompressed = !true;

	final static boolean writeGrassCompressed = !true;

	final static boolean writeEsriUnCompressed = !true;

	final static boolean writeGrassUnCompressed = !true;

	/**
	 * Creates a new instance of ArcGridReadWriteTest
	 * 
	 * @param name
	 *            DOCUMENT ME!
	 */
	public ArcGridReadWriteTest(String name) {
		super(name, true);
	}

	/**
	 * 
	 * @param testParam
	 * @throws Exception
	 */
	public void test(final File testFile) throws Exception {

		// create a temporary output file
		// temporary file to use
		final File tmpFile;
		tmpFile = File.createTempFile(Long.toString(Math
				.round(100000 * generator.nextDouble())), testFile.getName());
		tmpFile.deleteOnExit();

		/** Step 1 read it */
		// read in the grid coverage
		GridCoverageReader reader = new ArcGridReader(testFile);
		// reading the coverage
		final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);

		/** Step 2 write it */
		// write grid coverage out to temp file
		final GridCoverageWriter writer = new ArcGridWriter(tmpFile);
		writer.write(gc1, null); //

		/** Step 3 read it again and compare them */
		// read the grid coverage back in from temp file
		reader = new ArcGridReader(tmpFile);
		// read it
		final GridCoverage2D gc2 = (GridCoverage2D) reader.read(null);
		// check that the original and temporary grid are the same
		compare(gc1, gc2);

		/** Step 4 Visualize coverages */
		gc1.show();
		gc2.show();

	}

	/**
	 * Compares 2 grid covareages, throws an exception if they are not the same.
	 * 
	 * @param gc1
	 *            First Grid Coverage
	 * @param gc2
	 *            Second Grid Coverage
	 * 
	 * @throws Exception
	 *             If Coverages are not equal
	 */
	void compare(GridCoverage2D gc1, GridCoverage2D gc2) throws Exception {
		final GeneralEnvelope e1 = (GeneralEnvelope) gc1.getEnvelope();
		final GeneralEnvelope e2 = (GeneralEnvelope) gc2.getEnvelope();

		/** Checking Envelopes */
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

		/** Checking CRS */
		if (e1.getCoordinateReferenceSystem().toWKT().compareToIgnoreCase(
				e2.getCoordinateReferenceSystem().toWKT()) != 0) {
			throw new Exception("GridCoverage CRS are not equal"
					+ e1.getCoordinateReferenceSystem().toWKT()
					+ e2.getCoordinateReferenceSystem().toWKT());
		}

		/** Checking values */
		final Double noData1 = new Double(getCandidateNoData(gc1));
		final Double noData2 = new Double(getCandidateNoData(gc2));
		final int minTileX1 = gc1.getRenderedImage().getMinTileX();
		final int minTileY1 = gc1.getRenderedImage().getMinTileY();
		final int width = gc1.getRenderedImage().getWidth();
		final int height = gc1.getRenderedImage().getHeight();
		final int maxTileX1 = minTileX1 + gc1.getRenderedImage().getNumXTiles();
		final int maxTileY1 = minTileY1 + gc1.getRenderedImage().getNumYTiles();
		double value1 = 0, value2 = 0;

		for (int tileIndexX = minTileX1; tileIndexX < maxTileX1; tileIndexX++)
			for (int tileIndexY = minTileY1; tileIndexY < maxTileY1; tileIndexY++) {

				final Raster r1 = gc1.getRenderedImage().getTile(tileIndexX,
						tileIndexY);
				final Raster r2 = gc2.getRenderedImage().getTile(tileIndexX,
						tileIndexY);

				for (int i = r1.getMinX(); i < width; i++) {
					for (int j = r1.getMinY(); j < height; j++) {
						value1 = r1.getSampleDouble(i, j, 0);
						value2 = r2.getSampleDouble(i, j, 0);

						if (!((noData1.compareTo(new Double(value1))) == 0 && (noData2
								.compareTo(new Double(value2))) == 0)
								&& (value1 != value2)) {
							throw new Exception(
									"GridCoverage Values are not equal: "
											+ value1 + ", " + value2);
						}

					}
				}
			}

	}

	private static double getCandidateNoData(GridCoverage2D gc) {
		// no data management
		final GridSampleDimension sd = (GridSampleDimension) gc
				.getSampleDimension(0);
		final List categories = sd.getCategories();
		final Iterator it = categories.iterator();
		Category candidate;
		double inNoData = Double.NaN;
		while (it.hasNext()) {
			candidate = (Category) it.next();
			if (candidate.getName().toString().equalsIgnoreCase(
					Vocabulary.formatInternational(VocabularyKeys.NODATA)
							.toString())) {
				inNoData = candidate.getRange().getMaximum();
			}
		}

		return inNoData;
	}

//	/**
//	 * A Simple Test Method which read an arcGrid and write it as an arcGrid GZ
//	 * compressed
//	 */
//	public void testWriteEsriCompressed() throws Exception {
//		if (writeEsriCompressed) {
//			final File rf = TestData.file(this, "spearfish_dem.asc.gz");
//			final File wf;
//			wf = File.createTempFile(Long.toString(Math
//					.round(100000 * generator.nextDouble())), rf.getName());
//			wf.deleteOnExit();
//
//			/** Step 1: Reading the coverage */
//			GridCoverageReader reader = new ArcGridReader(rf);
//			final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);
//
//			/** Step 2: Write grid coverage out to temp file */
//			final GridCoverageWriter writer = new ArcGridWriter(wf);
//
//			// setting write parameters
//			ParameterValueGroup params;
//			params = writer.getFormat().getWriteParameters();
//			params.parameter("GRASS").setValue(false);
//			params.parameter("compressed").setValue(true);
//			GeneralParameterValue[] gpv = { params.parameter("GRASS"),
//					params.parameter("compressed") };
//			writer.write(gc1, gpv);
//
//			/** Step 3: Read the just written coverage */
//			GridCoverageReader reader2 = new ArcGridReader(wf);
//			final GridCoverage2D gc2 = (GridCoverage2D) reader2.read(null);
//
//			/** Step 4: Check if the 2 coverage are equals */
//			compare(gc1, gc2);
//
//			/** Step 5: Show the new coverage */
//			gc2.show();
//		}
//	}

//	/**
//	 * A Simple Test Method which read an arcGrid and write it as a GRASS Ascii
//	 * Grid GZ compressed
//	 */
//	public void testWriteGrassCompressed() throws Exception {
//		if (writeGrassCompressed) {
//			final File rf = TestData.file(this, "spearfish_dem.asc.gz");
//			final File wf;
//			wf = File.createTempFile(Long.toString(Math
//					.round(100000 * generator.nextDouble())), rf.getName());
//			wf.deleteOnExit();
//			/** Step 1: Reading the coverage */
//			GridCoverageReader reader = new ArcGridReader(rf);
//			final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);
//
//			/** Step 2: Write grid coverage out to temp file */
//			final GridCoverageWriter writer = new ArcGridWriter(wf);
//
//			// setting write parameters
//			ParameterValueGroup params;
//			params = writer.getFormat().getWriteParameters();
//			params.parameter("GRASS").setValue(true);
//			params.parameter("compressed").setValue(true);
//			GeneralParameterValue[] gpv = { params.parameter("GRASS"),
//					params.parameter("compressed") };
//			writer.write(gc1, gpv);
//
//			/** Step 3: Read the just written coverage */
//			GridCoverageReader reader2 = new ArcGridReader(wf);
//			final GridCoverage2D gc2 = (GridCoverage2D) reader2.read(null);
//
//			/** Step 4: Check if the 2 coverage are equals */
//			compare(gc1, gc2);
//
//			/** Step 5: Show the new coverage */
//			gc2.show();
//		}
//	}

	/**
	 * A Simple Test Method which read an arcGrid and write it as a GRASS Ascii
	 * Grid
	 */
	public void testWriteGrassUnCompressed() throws Exception {
		if (writeGrassUnCompressed) {
			final File rf = TestData.file(this, "vandem.asc");
			final File wf;
			wf = File.createTempFile(Long.toString(Math
					.round(100000 * generator.nextDouble())), rf.getName());
			wf.deleteOnExit();

			/** Step 1: Reading the coverage */
			GridCoverageReader reader = new ArcGridReader(rf);
			final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);

			/** Step 2: Write grid coverage out to temp file */
			final GridCoverageWriter writer = new ArcGridWriter(wf);

			// setting write parameters
			ParameterValueGroup params;
			params = writer.getFormat().getWriteParameters();
			params.parameter("GRASS").setValue(true);
			params.parameter("compressed").setValue(false);
			GeneralParameterValue[] gpv = { params.parameter("GRASS"),
					params.parameter("compressed") };
			writer.write(gc1, gpv);

			/** Step 3: Read the just written coverage */
			GridCoverageReader reader2 = new ArcGridReader(wf);
			final GridCoverage2D gc2 = (GridCoverage2D) reader2.read(null);

			/** Step 4: Check if the 2 coverage are equals */
			compare(gc1, gc2);

			/** Step 5: Show the new coverage */
			gc2.show();
		}

	}

	/**
	 * A Simple Test Method which read an arcGrid and write it as an ArcGrid
	 */
	public void testWriteEsriUnCompressed() throws Exception {
		if (writeEsriUnCompressed) {
			final File rf = TestData.file(this, "spearfish_dem.arx");
			final File wf;
			wf = File.createTempFile(Long.toString(Math
					.round(100000 * generator.nextDouble())), rf.getName());
			wf.deleteOnExit();

			/** Step 1: Reading the coverage */
			GridCoverageReader reader = new ArcGridReader(rf);
			final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);

			/** Step 2: Write grid coverage out to temp file */
			final GridCoverageWriter writer = new ArcGridWriter(wf);

			// setting write parameters
			ParameterValueGroup params;
			params = writer.getFormat().getWriteParameters();
			params.parameter("GRASS").setValue(false);
			params.parameter("compressed").setValue(false);
			GeneralParameterValue[] gpv = { params.parameter("GRASS"),
					params.parameter("compressed") };
			writer.write(gc1, gpv);

			/** Step 3: Read the just written coverage */
			GridCoverageReader reader2 = new ArcGridReader(wf);
			final GridCoverage2D gc2 = (GridCoverage2D) reader2.read(null);

			/** Step 4: Check if the 2 coverage are equals */
			compare(gc1, gc2);

			/** Step 5: Show the new coverage */
			gc2.show();
		}

	}

//	/**
//	 * A Simple Test Method which read an arcGrid with subsampling enabled
//	 */
//	public void testReadSubSampled() throws Exception {
//		if (readSubSampled) {
//			final File rf = TestData.file(this, "spearfish_dem.arx");
//			final File wf = File.createTempFile(Long.toString(Math
//					.round(100000 * generator.nextDouble())), rf.getName());
//			wf.deleteOnExit();
//
//			/** Step 1: Reading the coverage */
//			GridCoverageReader reader = new ArcGridReader(rf);
//			ParameterValueGroup params;
//			params = reader.getFormat().getReadParameters();
//			params.parameter("sourceXSubsampling").setValue(2);
//			params.parameter("sourceYSubsampling").setValue(2);
//			GeneralParameterValue[] gpv = {
//					params.parameter("sourceXSubsampling"),
//					params.parameter("sourceYSubsampling") };
//
//			final GridCoverage2D gc1 = (GridCoverage2D) reader.read(gpv);
//			gc1.show();
//
//			/** Step 2: Write grid coverage out to temp file */
//			final GridCoverageWriter writer = new ArcGridWriter(wf);
//
//			// setting write parameters
//			ParameterValueGroup params2;
//			params2 = writer.getFormat().getWriteParameters();
//			params2.parameter("GRASS").setValue(true);
//			params2.parameter("compressed").setValue(false);
//			GeneralParameterValue[] gpv2 = { params2.parameter("GRASS"),
//					params2.parameter("compressed") };
//			writer.write(gc1, gpv2);
//
//			/** Step 3: Read the just written coverage */
//			GridCoverageReader reader2 = new ArcGridReader(wf);
//			final GridCoverage2D gc2 = (GridCoverage2D) reader2.read(null);
//
//			/** Step 4: Check if the 2 coverage are equals */
//			if(params2.parameter("GRASS").booleanValue())
//				compare(gc1, gc2);
//
//
//			/** Step 5: Show the new coverage */
//			gc2.show();
//			LOGGER.info(gc1.toString());
//			LOGGER.info(gc2.toString());
//		}
//	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ArcGridReadWriteTest.class);
	}

	/**
	 * @see org.geotools.gce.arcgrid.ArcGridBaseTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		ImageIO.setUseCache(false);

	}
}
