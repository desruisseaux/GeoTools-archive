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
package org.geotools.gce.mrsid;

import org.geotools.test.TestData;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public final class MrSIDVisualizationTest extends AbstractMrSIDTestCase {

	/**
	 * file name of a valid MrSID sample data to be used for tests. All MrSID
	 * sample data found are too large to be placed in the test-data folder by
	 * means of the SVN.
	 * 
	 * We suggest to download a valid MrSID sample file from this site:
	 * https://zulu.ssc.nasa.gov/mrsid/
	 * 
	 * For each .SID file, a .MET file exists. Use the last one to build a valid
	 * .PRJ for the sample. If you are only interested in reading/rendering
	 * capabilities, or displaying coverages as simple rasters, the .PRJ is not
	 * necessary. However, a valid .PRJ file is required anytime you need to use
	 * the sample data as a coherently GeoReferenced coverage, by means of, as
	 * an instance, uDIG.
	 */
	private final static String fileName = "";

	/**
	 * Creates a new instance of MrSIDVisualizationTest
	 * 
	 * @param name
	 */
	public MrSIDVisualizationTest(String name) {
		super(name);
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(MrSIDVisualizationTest.class);
	}

	/**
	 * Test for reading a grid coverage from a MrSID source
	 * 
	 * @throws Exception
	 */
	public void testVisualization() throws Exception {
		// read in the grid coverage
		if (fileName.equalsIgnoreCase("")) {
			LOGGER
					.info("===================================================================\n"
							+ " Warning! No valid test File has been yet specified.\n"
							+ " Please provide a valid sample in the source code and repeat this test!\n"
							+ "========================================================================");
			return;
		}
		
		final AbstractGridCoverage2DReader reader = new MrSIDReader(TestData
				.file(this, fileName));

		ParameterValueGroup params;
		params = reader.getFormat().getReadParameters();

		params.parameter(
				AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())
				.setValue(
						new GridGeometry2D(reader.getOriginalGridRange(),
								reader.getOriginalEnvelope()));
		GeneralParameterValue[] gpv = { params
				.parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
						.toString()) };

		GridCoverage2D gc = (GridCoverage2D) reader.read(gpv);

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
