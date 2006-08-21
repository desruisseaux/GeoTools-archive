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

import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.MissingResourceException;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotScaleException;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.geotools.image.jai.Registry;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterValueGroup;

/**
 * GridCoverage2D specialization for creation of a scaledd version of a source
 * coverage.
 * 
 * @author Simone Giannecchini
 */
final class ScaledGridCoverage2D extends GridCoverage2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2521916272257997635L;

	/**
	 * Creates a scaled version of a coverage with the needed scale factors,
	 * interpolation and border extender.
	 * 
	 * @param parameters
	 *            Input parameters for the creation of this coverage.
	 * @param hints
	 *            Hints for the creation of this coverage.
	 * @return Scaled version of the source coverage.
	 * @throws CannotScaleException
	 * @throws MissingResourceException
	 * @throws NoninvertibleTransformException
	 */
	static Coverage create(ParameterValueGroup parameters, Hints hints)
			throws NoninvertibleTransformException {

		// /////////////////////////////////////////////////////////////////////
		//
		// Getting the input parameters
		//
		// /////////////////////////////////////////////////////////////////////
		final Float xScale = (Float) parameters.parameter("xScale").getValue();
		final Float yScale = (Float) parameters.parameter("yScale").getValue();
		final Float xTrans = (Float) parameters.parameter("xTrans").getValue();
		final Float yTrans = (Float) parameters.parameter("yTrans").getValue();
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
		final int transferType = sourceImage.getSampleModel().getDataType();
		if (!(interpolation instanceof InterpolationNearest)
				&& (transferType == DataBuffer.TYPE_FLOAT || transferType == DataBuffer.TYPE_DOUBLE)) {

			Registry.setNativeAccelerationAllowed("Scale", false);

		} else
			Registry.setNativeAccelerationAllowed("Scale", true);

		// /////////////////////////////////////////////////////////////////////
		//
		// Preparing the parameters for the scale operation
		//
		// /////////////////////////////////////////////////////////////////////
		final ParameterBlock pbjScale = new ParameterBlock();
		pbjScale.add(xScale);
		pbjScale.add(yScale);
		pbjScale.add(xTrans);
		pbjScale.add(yTrans);
		pbjScale.add(interpolation);
		pbjScale.addSource(sourceImage);

		// /////////////////////////////////////////////////////////////////////
		//
		// Handling hints.
		// It is worthwhile to point out that layout hints for minx, miny, width
		// and height are NOT honored by the scale operation. The other
		// ImageLayout hints, like tileWidth and tileHeight, however are
		// honored.
		//
		// /////////////////////////////////////////////////////////////////////
		if (hints != null) {
			hints = (Hints) hints.clone();
			hints.put(JAI.KEY_BORDER_EXTENDER, parameters.parameter(
					"BorderExtender").getValue());
		} else {
			hints = new Hints(JAI.KEY_BORDER_EXTENDER, parameters.parameter(
					"BorderExtender").getValue());
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating final grid coverage.
		//
		// /////////////////////////////////////////////////////////////////////
		final JAI processor = OperationJAI.getJAI(hints);
		final PlanarImage image;
		if (!processor.equals(JAI.getDefaultInstance()))
			image = OperationJAI.getJAI(hints).createNS("Scale", pbjScale,
					hints);
		else
			image = JAI.create("Scale", pbjScale, hints);

		return new ScaledGridCoverage2D(image, sourceCoverage);
	}

	/**
	 * Create a scaled coverage as requested.
	 */
	private ScaledGridCoverage2D(PlanarImage image,
			GridCoverage2D sourceCoverage) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				sourceCoverage.getSampleDimensions(),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}
}