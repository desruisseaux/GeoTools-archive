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

import java.awt.Rectangle;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;

import junit.framework.TestCase;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.TestData;
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
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/arcgrid/test/org/geotools/gce/arcgrid/ECWVisualizationTest.java $
 * @version 1.0
 */
public class ECWVisualizationTest extends TestCase {
	private final static Logger LOGGER = Logger
			.getLogger("org.geotools.gce.ecw");

	protected void setUp() throws Exception {
		super.setUp();
		ImageIO.setUseCache(false);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);

		JAI.getDefaultInstance().getTileScheduler().setParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(50);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(5);
		JAI.getDefaultInstance().getTileScheduler().setPriority(5);

	}

	/**
	 * Creates a new instance of ArcGridReadWriteTest
	 * 
	 * @param name
	 */
	public ECWVisualizationTest(String name) {
		super(name);

	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(ECWVisualizationTest.class);
	}

	public void testVisualization() throws Exception {

		// read in the grid coverage
		final AbstractGridCoverage2DReader reader = new ECWReader(TestData
				.file(this, "spezia_wgs84_ecw.ecw"));

		ParameterValueGroup params;
		params = reader.getFormat().getReadParameters();
		final GeneralEnvelope envelope = reader.getOriginalEnvelope();
		params.parameter(ECWFormat.READ_GRIDGEOMETRY2D.getName().toString())
				.setValue(
						new GridGeometry2D(new GeneralGridRange(new Rectangle(
								0, 0, 120, 80)), envelope));
		GeneralParameterValue[] gpv = { params
				.parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
						.toString()) };

		GridCoverage2D gc = (GridCoverage2D) reader.read(gpv);
		gc.show();

		// printing CRS information
		LOGGER.info(gc.getCoordinateReferenceSystem().toWKT());
		LOGGER.info(gc.getEnvelope().toString());
	}

}
