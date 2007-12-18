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
package org.geotools.gce.ecw;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.test.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

public final class ECWTest extends AbstractECWTestCase {

	/**
	 * file name of a valid ECW sample data to be used for tests. All ECW sample
	 * data found are too large to be placed in the test-data folder by means of
	 * the SVN.
	 * 
	 * We suggest to download a valid ECW sample file from this site:
	 * ftp://www.sjrwmd.com/2004_DOQQs/rgb_ecw/
	 * 
	 * Any zipped file contains a .ECW file as well as a HTML file containing a
	 * "Spatial Reference Information" paragraph where to find useful
	 * information about Projection, in order to build a valid .prj file.
	 */
	private final static String fileName = "sampledata.ecw";

	/**
	 * Creates a new instance of ECWTest
	 * 
	 * @param name
	 */
	public ECWTest(String name) {
		super(name);
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ECWTest.class);
	}

	public void test() throws Exception {
		if (!testingEnabled())
			return;

		// read in the grid coverage
		if (fileName.equalsIgnoreCase("")) {
			LOGGER
					.info("===================================================================\n"
							+ " Warning! No valid test File has been yet specified.\n"
							+ " Please provide a valid sample in the source code and repeat this test!\n"
							+ "========================================================================");
			return;
		}
		final AbstractGridCoverage2DReader reader = new ECWReader(TestData
				.file(this, fileName));
		final ParameterValue gg = (ParameterValue) ((AbstractGridFormat) reader
				.getFormat()).READ_GRIDGEOMETRY2D.createValue();
		final GeneralEnvelope oldEnvelope = reader.getOriginalEnvelope();
//		final GeneralEnvelope cropEnvelope = new GeneralEnvelope(new double[] {
//				oldEnvelope.getLowerCorner().getOrdinate(0)
//						+ oldEnvelope.getLength(0) / 2,
//				oldEnvelope.getLowerCorner().getOrdinate(1)
//						+ oldEnvelope.getLength(1) / 2 }, new double[] {
//				oldEnvelope.getUpperCorner().getOrdinate(0),
//				oldEnvelope.getUpperCorner().getOrdinate(1) });
//		cropEnvelope.setCoordinateReferenceSystem(reader.getCrs());
//		gg.setValue(new GridGeometry2D(new GeneralGridRange(new Rectangle(0, 0,
//				1375, 942)), cropEnvelope));
		gg.setValue(new GridGeometry2D(reader.getOriginalGridRange(), oldEnvelope));
		final GridCoverage2D gc = (GridCoverage2D) reader
				.read(new GeneralParameterValue[] { gg });

		assertNotNull(gc);
//		System.out.println(oldEnvelope);
//		System.out.println(gc.getEnvelope());
//		System.out.println(cropEnvelope);
//		//NOTE: in some cases might be too restrictive
//		assertTrue(cropEnvelope.equals(gc.getEnvelope(), XAffineTransform
//				.getScale(((AffineTransform) ((GridGeometry2D) gc
//						.getGridGeometry()).getGridToCRS2D())) / 2, true));

		if (TestData.isInteractiveTest()) {
			gc.show();
		} else
			gc.getRenderedImage().getData();
		if (TestData.isInteractiveTest()) {
			// printing CRS information
			LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
			LOGGER.info(gc.getEnvelope().toString());
		}
	}
}
