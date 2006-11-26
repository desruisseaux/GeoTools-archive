/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.coverage.grid;

import java.awt.image.RenderedImage;

import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderCopy;
import javax.media.jai.Interpolation;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author simone
 * @since 2.3
 */
public class FilteredSubsampleTest extends GridCoverageTest {
	private static final boolean SHOW = true;

	/**
	 * The source grid coverage.
	 */
	private GridCoverage2D coverage;

	/**
	 * @param name
	 */
	public FilteredSubsampleTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Run the suit from the command line.
	 */
	public static void main(final String[] args) {

        org.geotools.util.Logging.GEOTOOLS.forceMonolineConsoleOutput(AbstractProcessor.OPERATION);
		junit.textui.TestRunner.run(FilteredSubsampleTest.class);
	}

	/**
	 * Set up common objects used for all tests.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		coverage = getExample(0);
	}

	public void testFilteredSubsample() {

		final AbstractProcessor processor = AbstractProcessor.getInstance();
		GridCoverage2D source = coverage.geophysics(true);
		final ParameterValueGroup param = processor.getOperation(
				"FilteredSubsample").getParameters();
		param.parameter("Source").setValue(source);
		param.parameter("scaleX").setValue(new Integer(2));
		param.parameter("scaleY").setValue(new Integer(2));
		param.parameter("qsFilterArray").setValue(
				FilteredSubsample.qsFilter.getDefaultValue());
		param.parameter("Interpolation").setValue(
				Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		param.parameter("BorderExtender").setValue(
				BorderExtenderCopy.createInstance(BorderExtender.BORDER_COPY));
		GridCoverage2D scaled = (GridCoverage2D) processor.doOperation(param);
		assertNotNull(scaled.getRenderedImage().getData());
		final RenderedImage image = scaled.getRenderedImage();
		scaled = scaled.geophysics(false);
		String operation = null;
		if (image instanceof RenderedOp) {
			operation = ((RenderedOp) image).getOperationName();
			AbstractProcessor.LOGGER.fine("Applied \"" + operation
					+ "\" JAI operation.");
		}
		if (SHOW) {
			Viewer.show(coverage,coverage.getName().toString());
			Viewer.show(scaled,scaled.getName().toString());
		} else {
			// Force computation
			assertNotNull(coverage.getRenderedImage().getData());
			assertNotNull(scaled.getRenderedImage().getData());
		}


		final GridCoverage2D scaledGridCoverage = (GridCoverage2D) Operations.DEFAULT
				.filteredSubsample(source, 3, 3,new float[] {0.5F, 1.0F/3.0F, 0.0F, -1.0F/12.0F },
						Interpolation
								.getInstance(Interpolation.INTERP_NEAREST),
						BorderExtender
								.createInstance(BorderExtender.BORDER_COPY));
		
		if (SHOW) {
			Viewer.show(scaledGridCoverage,scaledGridCoverage.getName().toString());
		} else {
			// Force computation
			assertNotNull(scaledGridCoverage.getRenderedImage().getData());
		}

	}
}
