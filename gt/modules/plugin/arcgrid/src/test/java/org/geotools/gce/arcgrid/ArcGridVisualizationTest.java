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
package org.geotools.gce.arcgrid;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.test.TestData;
import org.geotools.resources.coverage.CoverageUtilities;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * <p>
 * Title: TestArcGridClass
 * </p>
 * 
 * <p>
 * Description: Testing ArcGrid ascii grids related classes.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005 Simone Giannecchini
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author <a href="mailto:simboss1@gmil.com">Simone Giannecchini (simboss)</a>
 * @source $URL$
 * @version 1.0
 */
public final class ArcGridVisualizationTest extends ArcGridTestCaseAdapter {



	/**
	 * Creates a new instance of ArcGridReadWriteTest
	 * 
	 * @param name
	 */
	public ArcGridVisualizationTest(String name) {
		super(name);

	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ArcGridVisualizationTest.class);
	}

	public void testVisualization() throws Exception {

		LOGGER.info("testing visualization of precip30min.asc");
		// read in the grid coverage
		final GridCoverageReader reader = new ArcGridReader(TestData.file(this,
				"arcgrid/precip30min.asc"));

		ParameterValueGroup params;
		params = reader.getFormat().getReadParameters();

		final GeneralEnvelope envelope = new GeneralEnvelope(new double[] {
				-180, -90 }, new double[] { 180, 90 });
		envelope.setCoordinateReferenceSystem(CRS.decode("EPSG:4326"));
		params.parameter(
				AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())
				.setValue(
						new GridGeometry2D(new GeneralGridRange(new Rectangle(
								0, 0, 400, 300)), envelope));
		GeneralParameterValue[] gpv = { params
				.parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
						.toString()) };

		GridCoverage2D gc = (GridCoverage2D) reader.read(gpv);
		
		assertTrue(CoverageUtilities.hasRenderingCategories(gc));

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
	
	/**
	 * This test tries to read GZipped ascii grids first by supplying the
	 * {@link ArcGridReader} with a {@link File} that points to a gzipped
	 * coverage, second by opening up a {@link GZIPInputStream} and asking
	 * {@link ImageIO} to wrap it with an {@link ImageInputStream}.
	 * 
	 * @throws IOException
	 */
	public void testReadFileGZip() throws IOException {
		LOGGER.info("Reading the coverage through a file");
		// get a gzipped ascii grid
		final File f = TestData.file(this, "arcgrid/spearfish.asc.gz");
		// Reading the coverage through a file
		GridCoverageReader reader = new ArcGridReader(f);
		final GridCoverage2D gc1 = (GridCoverage2D) reader.read(null);

		LOGGER.info("Reading the gzipped coverage through an ImageInputStream");
		// Reading the coverage through an ImageInputStream
		final ImageInputStream iiStream = ImageIO
				.createImageInputStream( new GZIPInputStream(
						new FileInputStream(f)));
		reader = new ArcGridReader(iiStream);
		final GridCoverage2D gc2 = (GridCoverage2D) reader.read(null);

		LOGGER.info(" Reading the gzipped coverage through an InputStream");
		// Reading the coverage through an InputStream
		reader = new ArcGridReader( new GZIPInputStream(
				new FileInputStream(f)));
		final GridCoverage2D gc3 = (GridCoverage2D) reader.read(null);
		
		LOGGER.info("Reading the gzipped coverage through a URL");
		// Reading the coverage through a URL
		reader = new ArcGridReader( f.toURL());
		final GridCoverage2D gc4 = (GridCoverage2D) reader.read(null);

		// show the coverage or try to load the data
		if (TestData.isInteractiveTest()) {
			gc1.show();
			gc2.show();
			gc3.show();
			gc4.show();
		} else {
			gc1.getRenderedImage().getData();
			gc2.getRenderedImage().getData();
			gc3.getRenderedImage().getData();
			gc4.getRenderedImage().getData();
		}
	}

}
