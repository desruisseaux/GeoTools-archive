/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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


import java.io.File;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.resources.TestData;
import org.opengis.coverage.grid.GridCoverageReader;


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
public class ArcGridVisualizationTest extends ArcGridBaseTestCase {

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
		// read in the grid coverage
		final GridCoverageReader reader = new ArcGridReader(testFile);
	
		// reading the coverage
		final GridCoverage2D gc = ((GridCoverage2D) reader.read(null));
	
		// visualizing it
		if(TestData.isInteractiveTest())
			gc.show();
	
		// printing CRS information
		System.out.println(gc.getCoordinateReferenceSystem().toWKT());
	}

}
