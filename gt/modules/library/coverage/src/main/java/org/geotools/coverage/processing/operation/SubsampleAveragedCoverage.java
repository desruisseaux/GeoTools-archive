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

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.SubsampleAverageDescriptor;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.image.ImageUtilities;
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
final class SubsampleAveragedCoverage extends GridCoverage2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5274708130300017804L;

	/**
	 * Constructor for a {@link SubsampleAveragedCoverage}.
	 * 
	 * @param image
	 * @param sourceCoverage
	 * @param actionTaken
	 */
	private SubsampleAveragedCoverage(PlanarImage image,
			GridCoverage2D sourceCoverage, int actionTaken) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				(GridSampleDimension[]) (actionTaken == 1 ? null
						: sourceCoverage.getSampleDimensions().clone()),
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
		GridCoverage2D sourceCoverage = (GridCoverage2D) parameters.parameter(
				"Source").getValue();
		RenderedImage sourceImage = sourceCoverage.getRenderedImage();
		


		// /////////////////////////////////////////////////////////////////////
		//
		// Do we need to explode the Palette to RGB(A)?
		//
		// /////////////////////////////////////////////////////////////////////
		int actionTaken = CoverageUtilities.prepareSourcesForGCOperation(sourceCoverage, interpolation, true,hints);
		switch (actionTaken) {
		case 2:
			// in this case we need to go back the geophysics view of the
			// source coverage
			sourceCoverage = sourceCoverage.geophysics(true);
			sourceImage = sourceCoverage.getRenderedImage();

			break;
		case 3:
			// we cannot accept working on the non-geophysics view because it
			// usually has an IndexColorModel.
			assert (false);
			break;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Managing Hints for coverage's layout purposes
		//
		// /////////////////////////////////////////////////////////////////////
		RenderingHints targetHints = ImageUtilities
				.getRenderingHints(sourceImage);
		if (targetHints == null) {
			targetHints = new RenderingHints(hints);
		} else if (hints != null) {
			targetHints.add(hints);
		}
		ImageLayout layout = (ImageLayout) targetHints
				.get(JAI.KEY_IMAGE_LAYOUT);
		if (layout != null) {
			layout = (ImageLayout) layout.clone();
		} else {
			layout = new ImageLayout(sourceImage);
			layout.unsetTileLayout();
			// At this point, only the color model and sample model are left
			// valids.
		}
		if ((layout.getValidMask() & (ImageLayout.TILE_WIDTH_MASK
				| ImageLayout.TILE_HEIGHT_MASK
				| ImageLayout.TILE_GRID_X_OFFSET_MASK | ImageLayout.TILE_GRID_Y_OFFSET_MASK)) == 0) {
			layout.setTileGridXOffset(layout.getMinX(sourceImage));
			layout.setTileGridYOffset(layout.getMinY(sourceImage));
			final int width = layout.getWidth(sourceImage);
			final int height = layout.getHeight(sourceImage);
			if (layout.getTileWidth(sourceImage) > width)
				layout.setTileWidth(width);
			if (layout.getTileHeight(sourceImage) > height)
				layout.setTileHeight(height);
		}
		targetHints.put(JAI.KEY_IMAGE_LAYOUT, layout);
		// it is crucial to correctly manage the Hints to control the
		// replacement of IndexColorModel. It is worth to point out that setting
		// the JAI.KEY_REPLACE_INDEX_COLOR_MODEL hint to true is not enough to
		// force the operators to do an expansion.
		// If we explicitly provide an ImageLayout built with the source image
		// where the CM and the SM are valid. those will be employed overriding
		// a the possibility to expand the color model.
		if (actionTaken != 1)
			targetHints.add(ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
		else {
			targetHints.add(ImageUtilities.REPLACE_INDEX_COLOR_MODEL);
			layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
			layout.unsetValid(ImageLayout.SAMPLE_MODEL_MASK);
		}
		

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
		targetHints.add(new RenderingHints(JAI.KEY_BORDER_EXTENDER, parameters
				.parameter("BorderExtender").getValue()));
		targetHints
				.add(new RenderingHints(JAI.KEY_INTERPOLATION, interpolation));
		final JAI processor = OperationJAI.getJAI(targetHints);

		// /////////////////////////////////////////////////////////////////////
		//
		// Preparing the resulting coverage
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D result;
		if (!processor.equals(JAI.getDefaultInstance())) {

			result = new SubsampleAveragedCoverage(processor.createNS(
					"SubsampleAverage", pbjSubsampleAverage, targetHints),
					sourceCoverage, actionTaken);
		}
		// no supplied processor
		else
			result = new SubsampleAveragedCoverage(JAI.create(
					"SubsampleAverage", pbjSubsampleAverage, targetHints),
					sourceCoverage, actionTaken);

		// now let's see what we need to do in order to clean things up
		if (actionTaken == 2)
			return result.geophysics(false);

		return result;
	}
}
