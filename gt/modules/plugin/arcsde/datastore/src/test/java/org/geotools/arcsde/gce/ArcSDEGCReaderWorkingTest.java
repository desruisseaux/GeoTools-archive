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
package org.geotools.arcsde.gce;

import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.geotools.arcsde.ArcSDERasterFormatFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridRange2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.parameter.Parameter;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.GeneralParameterValue;

/**
 * Tests the functionality of the ArcSDE raster-display package to read rasters from an ArcSDE database
 * 
 * @author Saul Farber, (based on ArcSDEPoolTest by Gabriel Roldan)
 * @source $URL$
 * @version $Id$
 */
public class ArcSDEGCReaderWorkingTest extends TestCase {

	/**
	 * Creates a new ArcSDEConnectionPoolTest object.
	 * 
	 */
	public ArcSDEGCReaderWorkingTest(String name) throws Exception {
		super(name);
	}

	public void testWorkingExample() throws Exception {

		String realWorldUrl = "sde://massgis:massgis@env-fp-phoenix:5151/gis#GISDATA.IMG_IMPERVIOUSSURFACE";

		GridCoverage2D gc;
		Format f = new ArcSDERasterFormatFactory().createFormat();
		AbstractGridCoverage2DReader r = (AbstractGridCoverage2DReader) ((AbstractGridFormat) f).getReader(realWorldUrl);

		GeneralParameterValue[] requestParams = new Parameter[1];

		// requestParams[0] = new
		// Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D,statewideRealWorldExampleRes);
		GridRange imageRange = new GridRange2D(0, 0, 128, 128);
		ReferencedEnvelope env = new ReferencedEnvelope(33000.5, 248000.45, 774000.5, 983400.45, r.getCrs());
		GridGeometry2D gg2d = new GridGeometry2D(imageRange, env);
		requestParams[0] = new Parameter(AbstractGridFormat.READ_GRIDGEOMETRY2D, gg2d);
		gc = (GridCoverage2D) r.read(requestParams);
		assertNotNull(gc);
		
		ImageIO.write(gc.geophysics(true).getRenderedImage(), "PNG", new File("/tmp/" + Thread.currentThread().getStackTrace()[1].getMethodName() + ".png"));
	}
}
