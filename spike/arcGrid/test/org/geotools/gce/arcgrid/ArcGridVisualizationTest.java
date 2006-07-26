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

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

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
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/arcgrid/test/org/geotools/gce/arcgrid/ArcGridVisualizationTest.java $
 * @version 1.0
 */
public class ArcGridVisualizationTest extends ArcGridBaseTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		ImageIO.setUseCache(false);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				60 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1);

	}

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

	public void test(final File testFile) throws Exception {
		LOGGER.info(testFile.getAbsolutePath());
		// read in the grid coverage
		final GridCoverageReader reader = new ArcGridReader(testFile);

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

		GridCoverage2D gc = (GridCoverage2D) reader.read(null);
		gc.show();

		// printing CRS information
		LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
	}
}
