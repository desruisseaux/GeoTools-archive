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
package org.geotools.renderer.lite.gridcoverage2d;

// J2SE dependencies
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.DefaultProcessor;
import org.geotools.coverage.processing.operation.Crop;
import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.geotools.coverage.processing.operation.Resample;
import org.geotools.coverage.processing.operation.Scale;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.image.CoverageUtilities;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.styling.RasterSymbolizer;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.expression.Literal;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A helper class for rendering {@link GridCoverage} objects. Support for grid
 * coverage SLD stylers is still limited.
 * 
 * @author Simone Giannecchini
 * @author Martin Desruisseaux
 * @author Andrea Aime
 * @author Alessio Fabiani
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/GridCoverageRenderer.java $
 * @version $Id: GridCoverageRenderer.java 18352 2006-03-01 06:13:42Z
 *          desruisseaux $
 * 
 * @task Add support for SLD styles
 */
public final class GridCoverageRenderer {
	/**
	 * This variable is use for testing purposes in order to force this
	 * {@link GridCoverageRenderer} to dump images at various steps on the disk.
	 */
	private final static boolean DEBUG = Boolean
			.getBoolean("org.geotools.renderer.lite.gridcoverage2d.debug");

	private static String debugDir;
	static {
		if (DEBUG) {
			final File tempDir = new File(System.getProperty("java.io.tmpdir"));
			if (!tempDir.exists() || !tempDir.canWrite()) {
				System.out
						.println("Unable to create debug dir, exiting application!!!");
				System.exit(1);
				debugDir = null;
			} else
				debugDir = tempDir.getAbsolutePath();
		}

	}

	/** Cached factory for the {@link Scale} operation. */
	private final static Scale scaleFactory = new Scale();

	/** Cached factory for the {@link FilteredSubsample} operation. */
	private final static FilteredSubsample filteredSubsampleFactory = new FilteredSubsample();

	/** Cached factory for the {@link Crop} operation. */
	private final static Crop coverageCropFactory = new Crop();

	/** Logger. */
	private static final Logger LOGGER = Logger
			.getLogger("org.geotools.rendering");

	static {

		// ///////////////////////////////////////////////////////////////////
		//
		// Caching parameters for performing the various operations.
		//	
		// ///////////////////////////////////////////////////////////////////
		final DefaultProcessor processor = new DefaultProcessor(
				CoverageUtilities.LENIENT_HINT);
		resampleParams = processor.getOperation("Resample").getParameters();
		scaleParams = processor.getOperation("Scale").getParameters();
		cropParams = processor.getOperation("CoverageCrop").getParameters();
		filteredSubsampleParams = processor.getOperation("FilteredSubsample")
				.getParameters();
	}

	/** The Display (User defined) CRS * */
	private final CoordinateReferenceSystem destinationCRS;

	/** Area we want to draw. */
	private final GeneralEnvelope destinationEnvelope;

	/** Size of the area we want to draw in pixels. */
	private final Rectangle destinationSize;

	private final GridToEnvelopeMapper gridToEnvelopeMapper;

	private final AffineTransform finalGridToWorld;

	private final AffineTransform finalWorldToGrid;

	private final Hints hints = new Hints(new HashMap(5));

	/** Parameters used to control the {@link Resample} operation. */
	private final static ParameterValueGroup resampleParams;

	/** Parameters used to control the {@link Scale} operation. */
	private final static ParameterValueGroup scaleParams;

	/** Parameters used to control the {@link Crop} operation. */
	private static ParameterValueGroup cropParams;

	/** Parameters used to control the {@link FilteredSubsample} operation. */
	private final static ParameterValueGroup filteredSubsampleParams;

	/** Parameters used to control the {@link Scale} operation. */
	private static final Resample resampleFactory = new Resample();

	/**
	 * Tolerance for NOT drawing a coverage.
	 * 
	 * If after a scaling a coverage has all dimensions smaller than
	 * {@link GridCoverageRenderer#MIN_DIM_TOLERANCE} we just do not draw it.
	 */
	private static final int MIN_DIM_TOLERANCE = 2;

	/**
	 * Creates a new {@link GridCoverageRenderer} object.
	 * 
	 * @param destinationCRS
	 *            the CRS of the {@link GridCoverage2D} to render.
	 * @param envelope
	 *            delineating the area to be rendered.
	 * @param screenSize
	 *            at which we want to rendere the source {@link GridCoverage2D}.
	 * @throws TransformException
	 * @throws NoninvertibleTransformException
	 * 
	 */
	public GridCoverageRenderer(final CoordinateReferenceSystem destinationCRS,
			final Envelope envelope, Rectangle screenSize)
			throws TransformException, NoninvertibleTransformException {

		this(destinationCRS, envelope, screenSize, null);

	}

	/**
	 * Creates a new {@link GridCoverageRenderer} object.
	 * 
	 * @param destinationCRS
	 *            the CRS of the {@link GridCoverage2D} to render.
	 * @param envelope
	 *            delineating the area to be rendered.
	 * @param screenSize
	 *            at which we want to rendere the source {@link GridCoverage2D}.
	 * @param java2dHints
	 *            to control this rendering process.
	 * 
	 * @throws TransformException
	 * @throws NoninvertibleTransformException
	 */
	public GridCoverageRenderer(final CoordinateReferenceSystem destinationCRS,
			final Envelope envelope, Rectangle screenSize,
			RenderingHints java2dHints) throws TransformException,
			NoninvertibleTransformException {

		// ///////////////////////////////////////////////////////////////////
		//
		// Initialize this renderer
		//
		// ///////////////////////////////////////////////////////////////////
		this.destinationSize = screenSize;
		this.destinationCRS = CRSUtilities.getCRS2D(destinationCRS);
		gridToEnvelopeMapper = new GridToEnvelopeMapper();
		gridToEnvelopeMapper.setGridType(PixelInCell.CELL_CORNER);
		gridToEnvelopeMapper
				.setGridRange(new GeneralGridRange(destinationSize));
		destinationEnvelope = new GeneralEnvelope(new ReferencedEnvelope(
				envelope, destinationCRS));
		// ///////////////////////////////////////////////////////////////////
		//
		// FINAL DRAWING DIMENSIONS AND RESOLUTION
		// I am here getting the final drawing dimensions (on the device) and
		// the resolution for this rendererbut in the CRS of the source coverage
		// since I am going to compare this info with the same info for the
		// source coverage.
		//
		// ///////////////////////////////////////////////////////////////////
		gridToEnvelopeMapper.setEnvelope(destinationEnvelope);
		finalGridToWorld = new AffineTransform(gridToEnvelopeMapper
				.createAffineTransform());
		finalWorldToGrid = finalGridToWorld.createInverse();

		// ///////////////////////////////////////////////////////////////////
		//
		// HINTS
		//
		// ///////////////////////////////////////////////////////////////////
		if (java2dHints != null)
			this.hints.add(java2dHints);
		// this prevents users from overriding leninet hint
		this.hints.add(CoverageUtilities.LENIENT_HINT);
		this.hints.add(ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
		if (!this.hints.containsKey(JAI.KEY_BORDER_EXTENDER))
			this.hints.add(ImageUtilities.EXTEND_BORDER_BY_COPYING);
	}

	/**
	 * Paint this grid coverage. The caller must ensure that
	 * <code>graphics</code> has an affine transform mapping "real world"
	 * coordinates in the coordinate system given by {@link
	 * #getCoordinateSystem}.
	 * 
	 * @param graphics
	 *            the {@link Graphics2D} context in which to paint.
	 * @param metaBufferedEnvelope 
	 * @throws FactoryException
	 * @throws TransformException
	 * @throws NoninvertibleTransformException
	 * @throws Exception
	 * @throws UnsupportedOperationException
	 *             if the transformation from grid to coordinate system in the
	 *             GridCoverage is not an AffineTransform
	 */
	public void paint(final Graphics2D graphics,
			final GridCoverage2D gridCoverage, final RasterSymbolizer symbolizer)
			throws FactoryException, TransformException,
			NoninvertibleTransformException {
//		METABUFFER SUPPORT	
//		public void paint(final Graphics2D graphics,
//		final GridCoverage2D gridCoverage, final RasterSymbolizer symbolizer,
//		GeneralEnvelope metaBufferedEnvelope)
//		throws FactoryException, TransformException,
//		NoninvertibleTransformException {


		// ///////////////////////////////////////////////////////////////////
		//
		// Getting information about the source coverage like the source CRS,
		// the source envelope and the source geometry.
		//
		// ///////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Drawing coverage ").append(
					gridCoverage.toString()).toString());
		final CoordinateReferenceSystem sourceCoverageCRS = gridCoverage
				.getCoordinateReferenceSystem2D();
		final GridGeometry2D sourceCoverageGG = (GridGeometry2D) gridCoverage
				.getGridGeometry();
		final GeneralGridRange sourceRange = (GeneralGridRange) sourceCoverageGG
				.getGridRange();
		final GeneralEnvelope sourceCoverageEnvelope = (GeneralEnvelope) gridCoverage
				.getEnvelope();

		// ///////////////////////////////////////////////////////////////////
		//
		// GET THE CRS MAPPING
		//
		// This step I instantiate the MathTransform for going from the source
		// crs to the desination crs.
		//
		// ///////////////////////////////////////////////////////////////////
		// math transform from source to target crs
		final MathTransform sourceCRSToDestinationCRSTransformation = StreamingRenderer
				.getMathTransform(sourceCoverageCRS, destinationCRS);
		final MathTransform destinationCRSToSourceCRSTransformation = sourceCRSToDestinationCRSTransformation
				.inverse();
		final boolean doReprojection = !sourceCRSToDestinationCRSTransformation
				.isIdentity();
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER
					.fine(new StringBuffer(
							"Transforming coverage envelope with transform ")
							.append(
									destinationCRSToSourceCRSTransformation
											.toWKT()).toString());

		// //
		//
		// Do we need reprojection?
		//
		// //
		GeneralEnvelope croppedDestinationEnvelope = null;
		if (doReprojection) {
			// //
			//
			// Let's crop the destination envelope with respect to the source
			// coverage envelope in order to do resampling if needed.
			//
			// //
			// create a new envelope
			croppedDestinationEnvelope = new GeneralEnvelope(
					destinationEnvelope);
			croppedDestinationEnvelope
					.setCoordinateReferenceSystem(destinationCRS);

			//
			// transform the source coverage envelope to the destination
			// coordinate reference system for cropping the destination envelope
			final GeneralEnvelope transformedSourceCoverageEnvelope = CRSUtilities
					.transform(sourceCRSToDestinationCRSTransformation,
							sourceCoverageEnvelope);
			transformedSourceCoverageEnvelope
					.setCoordinateReferenceSystem(destinationCRS);
			croppedDestinationEnvelope
					.intersect(transformedSourceCoverageEnvelope);

		}

		// /////////////////////////////////////////////////////////////////
		//
		// CROP
		// This step I aim to crop the area of the coverage we want to serve.
		// I know that drawRenderedImage takes already into account tiling but
		// there is reprojection in between and I do not want end up
		// reprojecting 2 Giga of image for using 1 Kb.
		//
		// First I need to convert the source envelope into the destination crs.
		//
		// /////////////////////////////////////////////////////////////////
		// //
		//
		// This is the destination envelope in the coverage crs which is going
		// to be used for getting the crop area to crop the source coverage
		//
		// //
		final GeneralEnvelope destinationEnvelopeInSourceGCCRS = doReprojection ? CRSUtilities
				.transform(destinationCRSToSourceCRSTransformation,
						destinationEnvelope)
				: new GeneralEnvelope(destinationEnvelope);
		destinationEnvelopeInSourceGCCRS
				.setCoordinateReferenceSystem(sourceCoverageCRS);
		final GridCoverage2D croppedGridCoverage = getCroppedCoverage(
				gridCoverage, destinationEnvelopeInSourceGCCRS,
				sourceCoverageCRS);
		if (croppedGridCoverage == null) {
			// nothing to render, the AOI does not overlap
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER
						.fine(new StringBuffer(
								"Skipping current coverage because cropped to an empty area")
								.toString());
			return;
		}
		if (DEBUG) {
			try {
				ImageIO.write(croppedGridCoverage.geophysics(false)
						.getRenderedImage(), "tiff", new File(debugDir,
						"cropped.tiff"));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// ///////////////////////////////////////////////////////////////////
		//
		// DRAWING DIMENSIONS AND RESOLUTION
		// I am here getting the final drawing dimensions (on the device) and
		// the resolution for this renderer but in the CRS of the source
		// coverage
		// since I am going to compare this info with the same info for the
		// source coverage. The objective is to come up with the needed scale
		// factors for the original coverage in order to decide how to proceed.
		// Options are first scale then reproject or the opposite.
		//
		// In case we need to upsample the coverage first we reproject and then
		// we upsample otherwise we do the opposite in order
		//
		// ///////////////////////////////////////////////////////////////////
		AffineTransform finalGridToWorldInGCCRS;
		if (!sourceCRSToDestinationCRSTransformation.isIdentity()) {
			assert new GeneralGridRange(destinationSize)
					.equals(gridToEnvelopeMapper.getGridRange());
			gridToEnvelopeMapper.setEnvelope(destinationEnvelopeInSourceGCCRS);
			finalGridToWorldInGCCRS = new AffineTransform(gridToEnvelopeMapper
					.createAffineTransform());
		} else {
			finalGridToWorldInGCCRS = new AffineTransform(finalGridToWorld);
		}

		// ///////////////////////////////////////////////////////////////////
		//
		// SCALE and REPROJECT in the best order.
		// Let me now scale down or up to the EXACT needed SPATIAL resolution.
		// This step does not prevent from having loaded an overview of the
		// original image based on the requested scale but it complements it.
		//
		// ///////////////////////////////////////////////////////////////////
		// //
		//
		// First step is computing the needed resolution levels for this
		// coverage in its original crs to see the scale factors.
		//
		// //
		final AffineTransform croppedCoverageGridToWorldTransformations = (AffineTransform) ((GridGeometry2D) croppedGridCoverage
				.getGridGeometry()).getGridToCRS2D();
		final boolean sourceGCHasLonFirst = (XAffineTransform
				.getSwapXY(croppedCoverageGridToWorldTransformations) != -1);
		final boolean destinationHasLonFirst = (XAffineTransform
				.getSwapXY(finalGridToWorldInGCCRS) != -1);
		final double actualScaleX = sourceGCHasLonFirst ? croppedCoverageGridToWorldTransformations
				.getScaleX()
				: croppedCoverageGridToWorldTransformations.getShearY();
		final double actualScaleY = sourceGCHasLonFirst ? croppedCoverageGridToWorldTransformations
				.getScaleY()
				: croppedCoverageGridToWorldTransformations.getShearX();
		final double scaleX = actualScaleX
				/ (destinationHasLonFirst ? finalGridToWorldInGCCRS.getScaleX()
						: finalGridToWorldInGCCRS.getShearY());
		final double scaleY = actualScaleY
				/ (destinationHasLonFirst ? finalGridToWorldInGCCRS.getScaleY()
						: finalGridToWorldInGCCRS.getShearX());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Scale factors are ").append(scaleX)
					.append(" ").append(scaleY).toString());
		final int actualW = sourceRange.getLength(0);
		final int actualH = sourceRange.getLength(1);
		if (Math.round(actualW * scaleX) < MIN_DIM_TOLERANCE
				&& Math.round(actualH * scaleY) < MIN_DIM_TOLERANCE) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER
						.fine(new StringBuffer(
								"Skipping the actual coverage because one of the final dimension is null")
								.toString());
			return;
		}

		final Interpolation interpolation = (Interpolation) hints
				.get(JAI.KEY_INTERPOLATION);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Using interpolation ").append(
					interpolation).toString());
		// //
		//
		// Now if we are upsampling first reproject then scale else first scale
		// then reproject.
		//
		// //
		final GridCoverage2D preSymbolizer;
		if (scaleX * scaleY <= 1.0) {
			int scaleXInt = (int) Math.floor(1 / scaleX);
			scaleXInt = scaleXInt == 0 ? 1 : scaleXInt;
			int scaleYInt = (int) Math.floor(1 / scaleY);
			scaleYInt = scaleYInt == 0 ? 1 : scaleYInt;

			// ///////////////////////////////////////////////////////////////////
			//
			// SCALE DOWN to the needed resolution
			//
			// ///////////////////////////////////////////////////////////////////
			// //
			//
			// first step for down sampling is filtered subsample which is fast.
			// 
			// //
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER
						.fine(new StringBuffer(
								"Filtered subsample with factors ").append(
								scaleXInt).append(scaleYInt).toString());
			final GridCoverage2D preScaledGridCoverage = filteredSubsample(
					croppedGridCoverage, scaleXInt, scaleYInt,
					new InterpolationNearest(), BorderExtender
							.createInstance(BorderExtender.BORDER_COPY));
			if (DEBUG) {
				try {
					ImageIO.write(preScaledGridCoverage.geophysics(false)
							.getRenderedImage(), "tiff", new File(debugDir,
							"prescaled.tiff"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// //
			//
			// Second step is scale
			//
			// //
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Scale down with factors ")
						.append(scaleX * scaleXInt).append(scaleY * scaleYInt)
						.toString());
			final GridCoverage2D scaledGridCoverage;
			if (scaleX * scaleXInt == 1.0 && scaleY * scaleYInt == 1.0)
				scaledGridCoverage = preScaledGridCoverage;
			else
				scaledGridCoverage = scale(scaleX * scaleXInt, scaleY
						* scaleYInt, 0f, 0f,
						interpolation == null ? new InterpolationBilinear()
								: interpolation, BorderExtender
								.createInstance(BorderExtender.BORDER_COPY),
						preScaledGridCoverage);

			if (DEBUG) {
				try {
					ImageIO.write(scaledGridCoverage.geophysics(false)
							.getRenderedImage(), "tiff", new File(debugDir,
							"scaled.tiff"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// ///////////////////////////////////////////////////////////////////
			//
			// REPROJECT to the requested crs.
			//
			//
			// ///////////////////////////////////////////////////////////////////
			if (doReprojection) {
				preSymbolizer = resample(scaledGridCoverage, destinationCRS,
						interpolation == null ? new InterpolationBilinear()
								: interpolation, croppedDestinationEnvelope);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuffer("Reprojecting to crs ")
							.append(destinationCRS.toWKT()).toString());
				if (DEBUG) {
					try {
						ImageIO.write(preSymbolizer.geophysics(false)
								.getRenderedImage(), "tiff", new File(debugDir,
								"reprojected.tiff"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else
				preSymbolizer = scaledGridCoverage;

		} else {

			// ///////////////////////////////////////////////////////////////////
			//
			// REPROJECT to the requested crs
			//
			//
			// ///////////////////////////////////////////////////////////////////
			final GridCoverage2D reprojectedCoverage;
			if (doReprojection) {
				reprojectedCoverage = resample(croppedGridCoverage,
						destinationCRS,
						interpolation == null ? new InterpolationBilinear()
								: interpolation, croppedDestinationEnvelope);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuffer("Reprojecting to crs ")
							.append(destinationCRS.toWKT()).toString());
			} else
				reprojectedCoverage = croppedGridCoverage;

			if (DEBUG) {
				try {
					ImageIO.write(reprojectedCoverage.geophysics(false)
							.getRenderedImage(), "tiff", new File(
							"c:/reprojected.tiff"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// ///////////////////////////////////////////////////////////////////
			//
			// SCALE UP to the needed resolution
			//
			// ///////////////////////////////////////////////////////////////////
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Scale up with factors ").append(
						scaleX).append(scaleY).toString());
			preSymbolizer = (GridCoverage2D) scale(scaleX, scaleY, 0f, 0f,
					interpolation == null ? new InterpolationBilinear()
							: interpolation, BorderExtender
							.createInstance(BorderExtender.BORDER_COPY),
					reprojectedCoverage);
			if (DEBUG) {
				try {
					ImageIO.write(preSymbolizer.geophysics(false)
							.getRenderedImage(), "tiff", new File(debugDir,
							"scaleup.tiff"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		if (DEBUG) {

			try {
				ImageIO.write(preSymbolizer.geophysics(false)
						.getRenderedImage(), "tiff", new File(debugDir,
						"preSymbolizer.tiff"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// ///////////////////////////////////////////////////////////////////
		//
		// RECOLOR
		//
		//
		// ///////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Raster Symbolizer ").toString());
		final RasterSymbolizerSupport rsp = new RasterSymbolizerSupport(
				symbolizer);
		final float alpha = rsp.getOpacity();
		final Composite oldAlphaComposite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha));
		final GridCoverage2D recoloredGridCoverage = (GridCoverage2D) rsp
				.recolorCoverage(preSymbolizer);
		final RenderedImage finalImage = recoloredGridCoverage
				.geophysics(false).getRenderedImage();

		// ///////////////////////////////////////////////////////////////////
		//
		// DRAW ME
		// I need the grid to world transform for drawing this grid coverage to
		// the display
		//
		// ///////////////////////////////////////////////////////////////////
		final AffineTransform finalGCgridToWorld = new AffineTransform(
				(AffineTransform) ((GridGeometry2D) recoloredGridCoverage
						.getGridGeometry()).getGridToCRS2D());
		if (!(finalGCgridToWorld instanceof AffineTransform)) {
			throw new UnsupportedOperationException(
					"Non-affine transformations not yet implemented"); // TODO
		}

		// //
		//
		// I need to translate half of a pixel since in wms 1.1.1 the envelope
		// map to the corners of the raster space not to the center of the
		// pixels.
		//
		// //
		finalGCgridToWorld.translate(-0.5, -0.5); // Map to upper-left corner.

		// //
		//
		// I am going to concatenate the final world to grid transform for the
		// screen area with the grid to world transform of the input coverage.
		//
		// This way i right away position the coverage at the right place in the
		// area of interest for the device.
		//
		// //
		final AffineTransform clonedFinalWorldToGrid = (AffineTransform) finalWorldToGrid
				.clone();
		clonedFinalWorldToGrid.concatenate(finalGCgridToWorld);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("clonedFinalWorldToGrid ").append(
					clonedFinalWorldToGrid.toString()).toString());

		// it should be a simple translation TODO check
		RenderingHints oldHints = graphics.getRenderingHints();
		graphics.setRenderingHints(this.hints);
		try {
			// //
			// Drawing the Image
			// //
			graphics.drawRenderedImage(finalImage, clonedFinalWorldToGrid);
		} catch (Throwable t) {
			try {
				if (DEBUG) {
					try {
						ImageIO.write(finalImage, "tiff", new File(debugDir,
								"final0.tiff"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// /////////////////////////////////////////////////////////////
				// this is a workaround for a bug in Java2D
				// (see bug 4723021
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4723021).
				//
				// AffineTransformOp.filter throws a
				// java.awt.image.ImagingOpException: Unable to tranform src
				// image when a PixelInterleavedSampleModel is used.
				//
				// CUSTOMER WORKAROUND :
				// draw the BufferedImage into a buffered image of type ARGB
				// then perform the affine transform. THIS OPERATION WASTES
				// RESOURCES BY PERFORMING AN ALLOCATION OF MEMORY AND A COPY ON
				// LARGE IMAGES.
				// /////////////////////////////////////////////////////////////
				final BufferedImage buf = new BufferedImage((int) finalImage
						.getWidth(), (int) finalImage.getHeight(),
						BufferedImage.TYPE_4BYTE_ABGR);
				final Graphics2D g = (Graphics2D) buf.getGraphics();
				g.drawRenderedImage(finalImage, AffineTransform
						.getScaleInstance(1, 1));
				g.dispose();
				if (DEBUG) {
					try {
						ImageIO.write(buf, "tiff", new File(debugDir,
								"final1.tiff"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

				graphics.drawImage(buf, clonedFinalWorldToGrid, null);
				buf.flush();

			} catch (Throwable t1) {
				// if fthe workaround fails again, there is really nothing to do
				// :-(
				LOGGER.log(Level.WARNING, t1.getLocalizedMessage(), t1);
			}
		}
		
		// //
		// Opacity
		// //
		float opacity = ((Double) ((Literal) symbolizer.getOpacity())
				.getValue()).floatValue();

		graphics.setComposite( AlphaComposite.getInstance( AlphaComposite.DST_IN ) );
		Color c = new Color(0.0f, 0.0f, 0.0f, opacity);
		graphics.setColor(c);
		graphics.fillRect(0, 0, this.destinationSize.width, this.destinationSize.height);
		
		// ///////////////////////////////////////////////////////////////////
		//
		// Restore old composite
		//
		// ///////////////////////////////////////////////////////////////////
		graphics.setComposite(oldAlphaComposite);
		graphics.setRenderingHints(oldHints);

	}

	/**
	 * Scaling the input coverage using the provided parameters.
	 * 
	 * @param scaleX
	 * @param scaleY
	 * @param xTrans
	 * @param yTrans
	 * @param interpolation
	 * @param be
	 * @param gc
	 * @return
	 */
	private GridCoverage2D scale(final double scaleX, final double scaleY,
			float xTrans, float yTrans, final Interpolation interpolation,
			final BorderExtender be, final GridCoverage2D gc) {

		final ParameterValueGroup param = (ParameterValueGroup) scaleParams
				.clone();
		param.parameter("source").setValue(gc);
		param.parameter("xScale").setValue(new Float(scaleX));
		param.parameter("yScale").setValue(new Float(scaleY));
		param.parameter("xTrans").setValue(new Float(xTrans));
		param.parameter("yTrans").setValue(new Float(yTrans));
		param.parameter("Interpolation").setValue(interpolation);
		param.parameter("BorderExtender").setValue(be);
		return (GridCoverage2D) scaleFactory.doOperation(param, hints);

	}

	/**
	 * Reprojecting the input coverage using the provided parameters.
	 * 
	 * @param gc
	 * @param crs
	 * @param interpolation
	 * @return
	 */
	private GridCoverage2D resample(final GridCoverage2D gc,
			CoordinateReferenceSystem crs, final Interpolation interpolation,
			final GeneralEnvelope destinationEnvelope) {
		// paranoiac check
		assert CRSUtilities.equalsIgnoreMetadata(destinationEnvelope
				.getCoordinateReferenceSystem(), crs);

		final ParameterValueGroup param = (ParameterValueGroup) resampleParams
				.clone();
		param.parameter("source").setValue(gc);
		param.parameter("CoordinateReferenceSystem").setValue(crs);
		param.parameter("GridGeometry").setValue(
				new GridGeometry2D(gc.getGridGeometry().getGridRange(),
						destinationEnvelope));
		param.parameter("InterpolationType").setValue(interpolation);
		return (GridCoverage2D) resampleFactory.doOperation(param, hints);

	}

	/**
	 * Subsampling the provided {@link GridCoverage2D} with the provided
	 * parameters.
	 * 
	 * @param gc
	 * @param scaleXInt
	 * @param scaleYInt
	 * @param interpolation
	 * @param be
	 * @return
	 */
	private GridCoverage2D filteredSubsample(final GridCoverage2D gc,
			int scaleXInt, int scaleYInt, final Interpolation interpolation,
			final BorderExtender be) {
		final GridCoverage2D preScaledGridCoverage;
		if (scaleXInt == 1 && scaleYInt == 1)
			preScaledGridCoverage = gc;
		else {

			final ParameterValueGroup param = (ParameterValueGroup) filteredSubsampleParams
					.clone();
			param.parameter("source").setValue(gc);
			param.parameter("scaleX").setValue(new Integer(scaleXInt));
			param.parameter("scaleY").setValue(new Integer(scaleYInt));
			if (hints.get(JAI.KEY_INTERPOLATION) != null
					&& hints.get(JAI.KEY_INTERPOLATION).equals(
							new InterpolationNearest()))
				param.parameter("qsFilterArray").setValue(new float[] { 1.0F });
			else
				param.parameter("qsFilterArray").setValue(
						new float[] { 0.5F, 1.0F / 3.0F, 0.0F, -1.0F / 12.0F });
			param.parameter("Interpolation").setValue(interpolation);
			param.parameter("BorderExtender").setValue(be);
			preScaledGridCoverage = (GridCoverage2D) filteredSubsampleFactory
					.doOperation(param, hints);

		}
		return preScaledGridCoverage;
	}

	/**
	 * Cropping the provided coverage to the requested geographic area.
	 * 
	 * @param gc
	 * @param envelope
	 * @param crs
	 * @return
	 */
	private GridCoverage2D getCroppedCoverage(GridCoverage2D gc,
			GeneralEnvelope envelope, CoordinateReferenceSystem crs) {
		final GeneralEnvelope oldEnvelope = (GeneralEnvelope) gc.getEnvelope();
		// intersect the envelopes in order to prepare for crooping the coverage
		// down to the neded resolution
		final GeneralEnvelope intersectionEnvelope = new GeneralEnvelope(
				envelope);
		intersectionEnvelope.setCoordinateReferenceSystem(crs);
		intersectionEnvelope.intersect((GeneralEnvelope) oldEnvelope);

		// Do we have something to show? After the crop I could get a null
		// coverage which would mean nothing to show.
		if (intersectionEnvelope.isEmpty())
			return null;

		// crop
		final ParameterValueGroup param = (ParameterValueGroup) cropParams
				.clone();
		param.parameter("source").setValue(gc);
		param.parameter("ConserveEnvelope").setValue(Boolean.TRUE);
		param.parameter("Envelope").setValue(intersectionEnvelope);
		return (GridCoverage2D) coverageCropFactory.doOperation(param, hints);

	}

}
