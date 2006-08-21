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
package org.geotools.coverage.processing.operation;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author Simone Giannecchini
 * 
 */
final class FilteredSubsampledGridCoverage2D extends GridCoverage2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5274708130300017804L;

	public FilteredSubsampledGridCoverage2D(PlanarImage image,
			GridCoverage2D sourceCoverage) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				sourceCoverage.getSampleDimensions(),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}

	static Coverage create(ParameterValueGroup parameters, Hints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the input parameters
		//
		// /////////////////////////////////////////////////////////////////////
		final Integer scaleX = (Integer) parameters.parameter("scaleX")
				.getValue();
		final Integer scaleY = (Integer) parameters.parameter("scaleY")
				.getValue();
		final float qsFilter[] = (float[]) parameters
				.parameter("qsFilterArray").getValue();
		final Interpolation interpolation = (Interpolation) parameters
				.parameter("Interpolation").getValue();

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the source coverage
		//
		// /////////////////////////////////////////////////////////////////////
		final GridCoverage2D sourceCoverage = (GridCoverage2D) parameters
				.parameter("Source").getValue();
		final RenderedImage sourceImage = sourceCoverage.getRenderedImage();

		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the parameters for the scale operation
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlock pbjFilteredSubsample = new ParameterBlock();
		pbjFilteredSubsample.addSource(sourceImage);
		pbjFilteredSubsample.add(scaleX).add(scaleY).add(qsFilter).add(
				interpolation).add(sourceImage);

		// /////////////////////////////////////////////////////////////////////
		//
		// preparing the new gridgeometry
		//
		// /////////////////////////////////////////////////////////////////////
		hints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, parameters
				.parameter("BorderExtender").getValue()));
		hints.add(new RenderingHints(JAI.KEY_INTERPOLATION, interpolation));
		final JAI processor = OperationJAI.getJAI(hints);
		if (!processor.equals(JAI.getDefaultInstance()))
			return new FilteredSubsampledGridCoverage2D(processor.createNS(
					"FilteredSubsample", pbjFilteredSubsample, hints),
					sourceCoverage);
		// no supplied processor
		return new FilteredSubsampledGridCoverage2D(JAI.create(
				"FilteredSubsample", pbjFilteredSubsample, hints),
				sourceCoverage);

	}
}
