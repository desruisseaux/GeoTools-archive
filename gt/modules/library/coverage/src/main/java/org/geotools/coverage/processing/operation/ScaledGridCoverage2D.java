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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.MissingResourceException;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotScaleException;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.factory.Hints;
import org.geotools.image.jai.Registry;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.image.ImageUtilities;
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

	/** Lock for unsetting native acceleration. */
	private final static int[] lock = new int[1];

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
		GridCoverage2D sourceCoverage = (GridCoverage2D) parameters.parameter(
				"Source").getValue();
		RenderedImage sourceImage = sourceCoverage.getRenderedImage();

		// /////////////////////////////////////////////////////////////////////
		//
		// Do we need to explode the Palette to RGB(A)?
		//
		// /////////////////////////////////////////////////////////////////////
		int actionTaken = CoverageUtilities.prepareSourcesForGCOperation(
				sourceCoverage, interpolation, false, hints);
		switch (actionTaken) {
		case 1:
			// //
			//
			// In this case I do not require an explicit color expansion since I
			// can leverage on te fact that the scale operation with latest
			// versions of JAI is one of the opeations that perform automatic
			// color expansion.
			//
			// //
			break;
		case 2:
			// in this case we need to go back the geophysics view of the
			// source coverage
			sourceCoverage = sourceCoverage.geophysics(true);
			sourceImage = sourceCoverage.getRenderedImage();
			break;
		case 3:
			// in this case we work on the non gephysics version because it
			// should be faster than working on the geophysics one. We are going
			// to work on a single band indexed image.
			sourceCoverage = sourceCoverage.geophysics(false);
			sourceImage = sourceCoverage.getRenderedImage();
			break;
		}

		// /////////////////////////////////////////////////////////////////////
		//
		// Managing Hints, especially for output coverage's layout purposes.
		//
		// It is worthwhile to point out that layout hints for minx, miny, width
		// and height are NOT honored by the scale operation. The other
		// ImageLayout hints, like tileWidth and tileHeight, however are
		// honored.
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
		targetHints.put(JAI.KEY_BORDER_EXTENDER, parameters.parameter(
				"BorderExtender").getValue());

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
		// Preparing the parameters for the scale operation
		//
		// /////////////////////////////////////////////////////////////////////
		final int transferType = sourceImage.getSampleModel().getDataType();
		final ParameterBlock pbjScale = new ParameterBlock();
		pbjScale.add(xScale);
		pbjScale.add(yScale);
		pbjScale.add(xTrans);
		pbjScale.add(yTrans);
		pbjScale.add(interpolation);
		pbjScale.addSource(sourceImage);

		// /////////////////////////////////////////////////////////////////////
		//
		// Creating final grid coverage.
		//
		// /////////////////////////////////////////////////////////////////////
		final JAI processor = OperationJAI.getJAI(targetHints);
		PlanarImage image;
		if (!(interpolation instanceof InterpolationNearest)
				&& (transferType == DataBuffer.TYPE_FLOAT || transferType == DataBuffer.TYPE_DOUBLE)) {

			synchronized (lock) {

				/**
				 * Disables the native acceleration for the "Scale" operation.
				 * In JAI 1.1.2, the "Scale" operation on TYPE_FLOAT datatype
				 * with INTERP_BILINEAR interpolation cause an exception in the
				 * native code of medialib, which halt the Java Virtual Machine.
				 * Using the pure Java implementation instead resolve the
				 * problem.
				 * 
				 * @todo Remove this hack when Sun will fix the medialib bug.
				 *       See
				 *       http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854
				 */

				Registry.setNativeAccelerationAllowed("Scale", false);

				if (!processor.equals(JAI.getDefaultInstance()))
					image = OperationJAI.getJAI(targetHints).createNS("Scale",
							pbjScale, targetHints).getRendering();
				else
					image = JAI.create("Scale", pbjScale, targetHints)
							.getRendering();

				/**
				 * see above
				 */
				Registry.setNativeAccelerationAllowed("Scale", true);
			}

		} else

		if (!processor.equals(JAI.getDefaultInstance()))
			image = OperationJAI.getJAI(targetHints).createNS("Scale",
					pbjScale, targetHints);
		else
			image = JAI.create("Scale", pbjScale, targetHints);

		// /////////////////////////////////////////////////////////////////////
		//
		// Preparing the resulting coverage
		//
		// /////////////////////////////////////////////////////////////////////
		GridCoverage2D result;
		if (!processor.equals(JAI.getDefaultInstance()))
			result = new ScaledGridCoverage2D(image, sourceCoverage,
					actionTaken);
		// no supplied processor
		else
			result = new ScaledGridCoverage2D(image, sourceCoverage,
					actionTaken);
		// now let's see what we need to do in order to clean things up
		if (actionTaken == 2)
			return result.geophysics(false);
		if (actionTaken == 3)
			return result.geophysics(true);
		return result;

	}

	/**
	 * Creates a scaled coverage as requested.
	 * 
	 * @param image
	 *            is the source image to use for building this
	 *            {@link GridCoverage2D}.
	 * @param sourceCoverage
	 *            is the coverage that was used as the source for this
	 *            operation.
	 * @param actionTaken
	 *            is the action we took when preparing ths source image for the
	 *            scale operation.
	 */
	private ScaledGridCoverage2D(PlanarImage image,
			GridCoverage2D sourceCoverage, int actionTaken) {
		super(sourceCoverage.getName(), image, new GridGeometry2D(
				new GeneralGridRange(image), sourceCoverage.getEnvelope()),
				(GridSampleDimension[]) (actionTaken == 1 ? null
						: sourceCoverage.getSampleDimensions().clone()),
				new GridCoverage[] { sourceCoverage }, sourceCoverage
						.getProperties());
	}
}