/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
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
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.CannotCropException;
import org.geotools.coverage.processing.OperationJAI;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.InternationalString;

/**
 * This class is responsible for applying a crop operation to a source coverage
 * with a specified envelope.
 * 
 * @author Simone Giannecchini
 * 
 */
final class CroppedCoverage2D extends GridCoverage2D {
	/**
	 * 
	 */
	private static final long serialVersionUID = -501742139906901754L;

	private CroppedCoverage2D(InternationalString name,
			PlanarImage translatedImage, GridGeometry2D croppedGeometry,
			GridCoverage2D source, int actionTaken) {
		super(name.toString(), translatedImage, croppedGeometry,
				(GridSampleDimension[]) (actionTaken == 1 ? null : source
						.getSampleDimensions().clone()),
				new GridCoverage[] { source }, null);
	}

	/**
	 * Applies the band select operation to a grid coverage.
	 * 
	 * @param parameters
	 *            List of name value pairs for the parameters.
	 * @param A
	 *            set of rendering hints, or {@code null} if none.
	 * @return The result as a grid coverage.
	 */
	static GridCoverage2D create(final ParameterValueGroup parameters,
			RenderingHints hints) {

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
		// Managing Hints, especially for output coverage's layout purposes
		//
		// /////////////////////////////////////////////////////////////////////
		RenderingHints targetHints = ImageUtilities
				.getRenderingHints(sourceImage);
		if (targetHints == null) {
			targetHints = new RenderingHints(hints);
		} else if (hints != null) {
			targetHints.add(hints);
		}
		// /////////////////////////////////////////////////////////////////////
		//
		// Interpolation
		//
		// /////////////////////////////////////////////////////////////////////
		Interpolation interpolation = (Interpolation) targetHints
				.get(JAI.KEY_INTERPOLATION);
		if (interpolation == null)
			interpolation = (Interpolation) ImageUtilities.NN_INTERPOLATION_HINT
					.get(JAI.KEY_INTERPOLATION);

		// /////////////////////////////////////////////////////////////////////
		//
		// Do we need to explode the Palette to RGB(A)?
		//
		// /////////////////////////////////////////////////////////////////////
		int actionTaken = 0;

		// /////////////////////////////////////////////////////////////////////
		//
		// Layout
		//
		// /////////////////////////////////////////////////////////////////////
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
		// crop will ignore minx, miny width and height
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

		// /////////////////////////////////////////////////////////////////////
		//
		// prepare the processor to use for this operation
		//
		// /////////////////////////////////////////////////////////////////////
		final JAI processor = OperationJAI.getJAI(targetHints);
		final boolean useProvidedProcessor = !processor.equals(JAI
				.getDefaultInstance());

		try {
			// /////////////////////////////////////////////////////////////////////
			//
			// get the cropped grid geometry
			//
			// /////////////////////////////////////////////////////////////////////
			final GeneralEnvelope intersectionEnvelope = (GeneralEnvelope) parameters
					.parameter("Envelope").getValue();
			final Boolean conserveEnvelope = (Boolean) parameters
			.parameter("ConserveEnvelope").getValue();
			final GridGeometry2D croppedGeometry = getCroppedGridGeometry(
					intersectionEnvelope, sourceCoverage,conserveEnvelope.booleanValue());
			if (croppedGeometry == null) {
				throw new CannotCropException(Errors
						.format(ErrorKeys.CANT_CROP));
			}
			final GridRange croppedRange = croppedGeometry.getGridRange();
			final int xAxis = croppedGeometry.gridDimensionX;
			final int yAxis = croppedGeometry.gridDimensionY;
			final double minX = croppedRange.getLower(xAxis);
			final double minY = croppedRange.getLower(yAxis);
			final double width = croppedRange.getLength(xAxis);
			final double height = croppedRange.getLength(yAxis);

			// /////////////////////////////////////////////////////////////////////
			//
			// get the rendered image and crop it
			//
			// /////////////////////////////////////////////////////////////////////
			final ParameterBlock pbjCrop = new ParameterBlock();
			pbjCrop.addSource(sourceImage);

			// /////////////////////////////////////////////////////////////////////
			//
			// The source coverage is now selected and will not change anymore.
			// Gets the JAI instance and factories to use from the rendering
			// hints.
			//
			// /////////////////////////////////////////////////////////////////////
			// executing the crop
			pbjCrop.add(new Float(minX));
			pbjCrop.add(new Float(minY));
			pbjCrop.add(new Float(width));
			pbjCrop.add(new Float(height));
			// targetHints.add(CoverageUtilities.NOCACHE_HINT);
			final PlanarImage croppedImage;
			if (!useProvidedProcessor)
				croppedImage = JAI.create("Crop", pbjCrop, targetHints);
			else
				croppedImage = processor.createNS("Crop", pbjCrop, targetHints);

			GridCoverage2D gc = new CroppedCoverage2D(sourceCoverage.getName(),
					croppedImage, croppedGeometry, sourceCoverage, actionTaken);
			return gc;
		} catch (TransformException e) {
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP), e);
		} catch (NoninvertibleTransformException e) {
			throw new CannotCropException(Errors.format(ErrorKeys.CANT_CROP), e);
		}

		// something bad happened

	}

	/**
	 * Cropping the grid geometry to the requested area. We do not recheck for
	 * intersection here.
	 * 
	 * @param intersectionEnvelope
	 *            is the {@link GeneralEnvelope} to use for cropping the
	 *            provided {@link GridCoverage2D}.
	 * @param gridCoverage
	 *            is the {@link GridCoverage2D} to crop.
	 * @param conserveEnvelope 
	 * @return a portion of the input {@link GridCoverage2D} corresponding to
	 *         the provided {@link GeneralEnvelope}.
	 * @throws NoninvertibleTransformException
	 * @throws TransformException
	 * @throws TransformException
	 */
	private static GridGeometry2D getCroppedGridGeometry(
			GeneralEnvelope intersectionEnvelope, GridCoverage gridCoverage, boolean conserveEnvelope)
			throws NoninvertibleTransformException, TransformException {

		// ////////////////////////////////////////////////////////////////////
		//
		// Getting old GG and Envelope
		//
		// ////////////////////////////////////////////////////////////////////
		final GridGeometry2D oldGG = (GridGeometry2D) gridCoverage
				.getGridGeometry();
		final GeneralGridRange oldGR = (GeneralGridRange) oldGG.getGridRange();
		final GeneralEnvelope oldEnvelope = (GeneralEnvelope) gridCoverage
				.getEnvelope();
		final MathTransform oldMathTr = oldGG.getGridToCRS2D();
		final double scaleFactor = XAffineTransform
				.getScale((AffineTransform) oldMathTr);

		// ////////////////////////////////////////////////////////////////////
		//
		// Do we actually need to crop?
		//
		// If the intersecton envelope is empty or if the intersection envelope
		// is (almost) the same of the original envelope we just return (with
		// different return values).
		// ////////////////////////////////////////////////////////////////////
		if (intersectionEnvelope.isEmpty())
			return null;
		if (intersectionEnvelope.equals(oldEnvelope, scaleFactor / 2.0))
			return new GridGeometry2D(oldGR, oldEnvelope);

		// ////////////////////////////////////////////////////////////////////
		//
		// if I get here I have something to crop
		// using the world to grid transform for going from envelope to new grid
		// range.
		//
		// ////////////////////////////////////////////////////////////////////
		AffineTransform gridToWorld = new AffineTransform(
				(AffineTransform) oldMathTr);
		// //
		//
		// build the new range by adding a -0.5 translation to the original
		// gridtoworld transform in order to keep into
		// account translation of grid geometry constructor for respecting OGC
		// PIXEL-IS-CENTER ImageDatum assumption.
		//
		// //
		gridToWorld.translate(-0.5, -0.5);
		final MathTransform worldToGridTransform = ProjectiveTransform
				.create(gridToWorld.createInverse());
		final GeneralEnvelope finalGridRange = CRS.transform(
				worldToGridTransform, intersectionEnvelope);
		// intersection with the original range in order to avoid problems
		finalGridRange.intersect(new GeneralEnvelope(oldGR.toRectangle()));

		// ////////////////////////////////////////////////////////////////////
		//
		// Create the new GridGeometry using the old mathtransfor but rebuilding
		// the envelope.
		//
		// It is worth to point out that doing a crop the G2W transform should
		// not change while the envelope might change a little bit as a
		// consequence of the roundings of the underlying image datum which uses
		// integer factors.
		//
		// ////////////////////////////////////////////////////////////////////
		final GeneralGridRange newRange = new GeneralGridRange(finalGridRange);
		if(conserveEnvelope)
			return new GridGeometry2D(newRange,intersectionEnvelope);
		return new GridGeometry2D(newRange, oldMathTr, gridCoverage
				.getCoordinateReferenceSystem());

	}
}
