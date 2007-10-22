/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.lite;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.DefaultOperationMethod;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.test.TestData;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransform;

/**
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/test/org/geotools/renderer/lite/GridCoverageRendererTest.java $
 */
public class GridCoverageRendererTest extends TestCase {

	public GridCoverageRendererTest(String testName) {
		super(testName);

	}

	String FILENAME = "TestGridCoverage.jpg";

	/**
	 * Returns a {@link GridCoverage} which may be used as a "real world"
	 * example.
	 * 
	 * @param number
	 *            The example number. Numbers are numeroted from 0 to
	 *            {@link #getNumExamples()} exclusive.
	 * @return The "real world" grid coverage.
	 * @throws IOException
	 *             if an I/O operation was needed and failed.
	 * @throws ParseException
	 * @throws IllegalArgumentException
	 */
	private final GridCoverage2D getGC() throws IOException,
			IllegalArgumentException, ParseException {
		final String path;
		final Rectangle2D bounds;

		// unit = "°C";
		path = "TestGridCoverage.tif";
		final CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

		// 41°S - 5°N ; 35°E - 80°E (450 x 460 pixels)
		bounds = new Rectangle2D.Double(35, -41, 45, 46);
		final Category values = new Category("values",
				new Color[] { Color.BLACK }, new NumberRange(0, 255),
				LinearTransform1D.IDENTITY);
		final GeneralEnvelope envelope = new GeneralEnvelope(bounds);
		final RenderedImage image = ImageIO.read(TestData.getResource(this,
				path));
		final int numBands = image.getSampleModel().getNumBands();
		final GridSampleDimension[] bands = new GridSampleDimension[numBands];
		// setting bands names.
		for (int i = 0; i < numBands; i++) {

			bands[i] = new GridSampleDimension("band " + i,
					new Category[] { values }, null).geophysics(true);
		}
		final String filename = new File(path).getName();
		final GridCoverageFactory factory = org.geotools.coverage.FactoryFinder
				.getGridCoverageFactory(null);
		envelope.setCoordinateReferenceSystem(crs);
		return (GridCoverage2D) factory.create(filename, image, envelope,
				bands, null, null);
	}

	/**
	 * Returns a projected CRS for test purpose.
	 */
	private static CoordinateReferenceSystem getProjectedCRS(
			final GridCoverage2D coverage) {
		try {
			final GeographicCRS base = (GeographicCRS) coverage
					.getCoordinateReferenceSystem();
			final Ellipsoid ellipsoid = ((GeodeticDatum) base.getDatum())
					.getEllipsoid();
			final DefaultMathTransformFactory factory = new DefaultMathTransformFactory();
			final ParameterValueGroup parameters = factory
					.getDefaultParameters("Oblique_Stereographic");
			parameters.parameter("semi_major").setValue(
					ellipsoid.getSemiMajorAxis());
			parameters.parameter("semi_minor").setValue(
					ellipsoid.getSemiMinorAxis());
			parameters.parameter("central_meridian").setValue(5);
			parameters.parameter("latitude_of_origin").setValue(-5);
			final MathTransform mt;
			try {
				mt = factory.createParameterizedTransform(parameters);
			} catch (FactoryException exception) {
				fail(exception.getLocalizedMessage());
				return null;
			}
			return new DefaultProjectedCRS("Stereographic",
					new DefaultOperationMethod(mt), base, mt,
					DefaultCartesianCS.PROJECTED);
		} catch (NoSuchIdentifierException exception) {
			fail(exception.getLocalizedMessage());
			return null;
		}
	}

	public void testPaint() throws Exception {

		//
		// /////////////////////////////////////////////////////////////////
		//
		// CREATING A GRID COVERAGE
		//
		//
		// /////////////////////////////////////////////////////////////////
		final GridCoverage2D gc = getGC();

		//
		//
		// /////////////////////////////////////////////////////////////////
		//
		// MAP CONTEXT
		//
		//
		// /////////////////////////////////////////////////////////////////
		final MapContext context = new DefaultMapContext(
				DefaultGeographicCRS.WGS84);
		final Style style = getStyle();
		context.addLayer(gc, style);

		// /////////////////////////////////////////////////////////////////
		//
		// Streaming renderer
		//
		//
		// ///////////////////////////////////////////////////////////////
		final StreamingRenderer renderer = new StreamingRenderer();
		renderer.setContext(context);

		RendererBaseTest.showRender("testGridCoverage", renderer, 1000, context.getLayerBounds());

	}

	public void testReproject() throws Exception {

		// ///////////////////////////////////////////////////////////////////
		//
		// CREATING A GRID COVERAGE
		//
		//
		// // /////////////////////////////////////////////////////////////////
		final GridCoverage2D coverage = getGC();
		//
		// ///////////////////////////////////////////////////////////////////
		//
		// MAP CONTEXT
		// We want to show the context in a different CRS
		//
		//
		// /////////////////////////////////////////////////////////////////
		final MapContext context = new DefaultMapContext(
				DefaultGeographicCRS.WGS84);
		final Style style = getStyle();
		context.addLayer(coverage, style);

		// transform to a new crs
		final CoordinateReferenceSystem destCRS = getProjectedCRS(coverage);

		//
		// ///////////////////////////////////////////////////////////////////
		//
		// Streaming renderer
		//
		//
		// /////////////////////////////////////////////////////////////////
		final StreamingRenderer renderer = new StreamingRenderer();
		renderer.setContext(context);

		ReferencedEnvelope env = context.getLayerBounds();
		env = new ReferencedEnvelope(env.getMinX(), env.getMaxX(), env
				.getMinY(), env.getMaxY(), DefaultGeographicCRS.WGS84);
		final ReferencedEnvelope newbounds = env.transform(destCRS, true);

		RendererBaseTest.showRender("testGridCoverageReprojection", renderer, 1000, newbounds);

	}

	private Style getStyle() {
		StyleBuilder sb = new StyleBuilder();
		Style rasterstyle = sb.createStyle();
		RasterSymbolizer raster = sb.createRasterSymbolizer();

		rasterstyle.addFeatureTypeStyle(sb.createFeatureTypeStyle(raster));
		rasterstyle.getFeatureTypeStyles()[0]
				.setFeatureTypeName("GridCoverage");
		return rasterstyle;
	}

	public static void main(final String[] args) {
		TestRunner.run(GridCoverageRendererTest.class);
	}
}
