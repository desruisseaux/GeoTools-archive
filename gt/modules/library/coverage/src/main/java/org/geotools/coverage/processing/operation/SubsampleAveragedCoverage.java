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
import javax.media.jai.operator.SubsampleAverageDescriptor;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Subsample the provided coverage by averaging overa moving window.
 * 
 * <p>
 * Citing the JAI documentation at {@linkplain}http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/SubsampleAverageDescriptor.html
 * for the {@link SubsampleAverageDescriptor}:
 * 
 * 
 * <p>
 * The "SubsampleAverage" operation subsamples an image by averaging over a
 * moving window. The scale factors supplied to the operation are forward
 * mapping coefficients representing the geometric transformation from source to
 * destination image coordinates. For example, if both scale factors were equal
 * to 0.5, the operation would produce an output image of half the size of the
 * input image in both dimensions. Both scale factors must be in the range (0.0,
 * 1.0] or an exception will be thrown when the operation is created. The
 * default value of the vertical scale factor is the value of the horizontal
 * scale factor. If both scale factors equal 1.0, the source image is returned
 * directly.
 * 
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3
 */
final class SubsampledAverageGridCoverage2D extends GridCoverage2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5274708130300017804L;

	/**
	 * Constructor for a {@link SubsampledAverageGridCoverage2D}.
	 * @param image
	 * @param sourceCoverage
	 */
	public SubsampledAverageGridCoverage2D(PlanarImage image,
			GridCoverage2D sourceCoverage) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				sourceCoverage.getSampleDimensions(),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}

	/**
	 * Craetes a new {@link Coverage} by applying the {@link SubsampleAverage}
	 * operation to the provided source using the provided parameters and hints.
	 * 
	 * @param parameters
	 *            Parameters to control this operation.
	 * @param hints
	 *            Hints to control this operation.
	 * @return A subsample coverage.
	 */
	static Coverage create(ParameterValueGroup parameters, Hints hints) {
		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the input parameters
		//
		// /////////////////////////////////////////////////////////////////////
		final Double scaleX = (Double) parameters.parameter("scaleX")
				.getValue();
		final Double scaleY = (Double) parameters.parameter("scaleY")
				.getValue();
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
		final ParameterBlock pbjSubsampleAverage = new ParameterBlock();
		pbjSubsampleAverage.addSource(sourceImage);
		pbjSubsampleAverage.add(scaleX).add(scaleY).add(interpolation).add(
				sourceImage);

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
			return new SubsampledAverageGridCoverage2D(processor.createNS(
					"SubsampleAverage", pbjSubsampleAverage, hints),
					sourceCoverage);
		// no supplied processor
		return new SubsampledAverageGridCoverage2D(JAI.create(
				"SubsampleAverage", pbjSubsampleAverage, hints), sourceCoverage);

	}
}
