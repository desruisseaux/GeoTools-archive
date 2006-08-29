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
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.resources.CRSUtilities;
import org.geotools.styling.RasterSymbolizer;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A helper class for rendering {@link GridCoverage} objects. Support for grid
 * coverage SLD stylers is still limited.
 * 
 * @author Martin Desruisseaux
 * @author Andrea Aime
 * @author Simone Giannecchini
 * @author Alessio Fabiani
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/render/src/org/geotools/renderer/lite/GridCoverageRenderer.java $
 * @version $Id: GridCoverageRenderer.java 18352 2006-03-01 06:13:42Z
 *          desruisseaux $
 * 
 * @task Add support for SLD stylers
 */
public final class GridCoverageRenderer {
	private final static Hints LENIENT_HINT = new Hints(
			Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

	private final static Scale scaleFactory = new Scale();

	private final static FilteredSubsample filteredSubsampleFactory = new FilteredSubsample();

	private final static Crop coverageCropFactory = new Crop();

	private static final Logger LOGGER = Logger
			.getLogger("org.geotools.rendering");

	static {

		// ///////////////////////////////////////////////////////////////////
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final DefaultProcessor processor = new DefaultProcessor(LENIENT_HINT);
		resampleParams = processor.getOperation("Resample").getParameters();
		scaleParams = processor.getOperation("Scale").getParameters();
		cropParams = processor.getOperation("CoverageCrop").getParameters();
		filteredSubsampleParams = processor.getOperation("FilteredSubsample")
				.getParameters();
	}

	/** The Display (User defined) CRS * */
	private final CoordinateReferenceSystem destinationCRS;

	private final GeneralEnvelope destinationEnvelope;

	private final Rectangle destinationSize;

	private final AffineTransform finalGridToWorld;

	private final AffineTransform finalWorldToGrid;

	private final Hints hints = new Hints(new HashMap(5));

	private final static ParameterValueGroup resampleParams;

	private final static ParameterValueGroup scaleParams;

	private static ParameterValueGroup cropParams;

	private final static ParameterValueGroup filteredSubsampleParams;

	private static final Resample resampleFactory = new Resample();

	private static final int MIN_DIM_TOLERANCE = 5;

	/**
	 * Creates a new GridCoverageRenderer object.
	 * 
	 * @param gridCoverage
	 * @param envelope
	 * @param screenSize
	 * @param scaleDenominator
	 * @throws TransformException
	 * @throws NoninvertibleTransformException
	 * 
	 */
	public GridCoverageRenderer(final CoordinateReferenceSystem destinationCRS,
			final Envelope envelope, Rectangle screenSize)
			throws TransformException, NoninvertibleTransformException {

		this(destinationCRS, envelope, screenSize, null);

	}

	public GridCoverageRenderer(final CoordinateReferenceSystem destinationCRS,
			final Envelope envelope, Rectangle screenSize,
			RenderingHints java2dHints) throws TransformException,
			NoninvertibleTransformException {

		// ///////////////////////////////////////////////////////////////////
		//
		//
		// ///////////////////////////////////////////////////////////////////
		this.destinationSize = screenSize;
		this.destinationCRS = CRSUtilities.getCRS2D(destinationCRS);
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
		finalGridToWorld = new AffineTransform((AffineTransform) GridGeometry2D
				.getTransform(new GeneralGridRange(destinationSize),
						destinationEnvelope, false));
		finalWorldToGrid = finalGridToWorld.createInverse();

		// ///////////////////////////////////////////////////////////////////
		//
		// HINTS
		//
		// ///////////////////////////////////////////////////////////////////
		this.hints.add(LENIENT_HINT);
		if (java2dHints != null)
			this.hints.add(java2dHints);
	}

	/**
	 * Paint this grid coverage. The caller must ensure that
	 * <code>graphics</code> has an affine transform mapping "real world"
	 * coordinates in the coordinate system given by {@link
	 * #getCoordinateSystem}.
	 * 
	 * @param graphics
	 *            the <code>Graphics</code> context in which to paint
	 * @throws FactoryException
	 * @throws TransformException
	 * @throws NoninvertibleTransformException
	 * @throws Exception
	 * @throws UnsupportedOperationException
	 *             if the transformation from grid to coordinate system in the
	 *             GridCoverage is not an AffineTransform
	 */
	public void paint(final Graphics2D graphics, final GridCoverage2D gc,
			final RasterSymbolizer symbolizer) throws FactoryException,
			TransformException, NoninvertibleTransformException {

		// ///////////////////////////////////////////////////////////////////
		//
		// Getting information about the source coverage.
		//
		// ///////////////////////////////////////////////////////////////////
		final GridCoverage2D gridCoverage = gc;
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Drawing coverage ").append(
					gc.toString()).toString());
		final CoordinateReferenceSystem sourceCoverageCRS = gridCoverage
				.getCoordinateReferenceSystem2D();

		// ///////////////////////////////////////////////////////////////////
		//
		// TRANSFORM THE ENVELOPE AND GET THE CRS MAPPING
		// This step I transform the destination envelope back to the crs of
		// the requested coverage after having instantiated the resources to go
		// from coverage crs to requested crs and viceversa.
		//
		// ///////////////////////////////////////////////////////////////////

		// try {
		// ImageIO.write(gridCoverage.geophysics(false).getRenderedImage(),
		// "png", new File("c:/original.png"));
		// } catch (IOException e2) {
		// // TODO Auto-generated catch block
		// e2.printStackTrace();
		// }

		// math transform from source to target crs
		final MathTransform GCCRSToDeviceCRSTransform = StreamingRenderer
				.getMathTransform(sourceCoverageCRS, destinationCRS);
		final MathTransform deviceCRSToGCCRSTransform = GCCRSToDeviceCRSTransform
				.inverse();
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer(
					"Transforming coverage envelope with transform ").append(
					deviceCRSToGCCRSTransform.toWKT()).toString());
		// this is the destination envelope in the coverage crs which is going
		// to be used for getting the crop area to crop the source coverage
		final GeneralEnvelope destinationEnvelopeInSourceGCCRS = (!deviceCRSToGCCRSTransform
				.isIdentity()) ? CRSUtilities.transform(
				deviceCRSToGCCRSTransform, destinationEnvelope)
				: new GeneralEnvelope(destinationEnvelope);
		destinationEnvelopeInSourceGCCRS
				.setCoordinateReferenceSystem(sourceCoverageCRS);

		// /////////////////////////////////////////////////////////////////
		//
		// CROP
		// This step I aim to crop the area of the coverage we want to serve.
		// I know that drawRenderedImage takes already into account tiling but
		// there is reprojection in between and I do not want end up
		// reprojecting 2 Giga of image for using 1 Kb.
		//
		// /////////////////////////////////////////////////////////////////
		final GridCoverage2D croppedGridCoverage = getCroppedCoverage(
				gridCoverage, destinationEnvelopeInSourceGCCRS,
				sourceCoverageCRS);

		if (croppedGridCoverage == null) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER
						.fine(new StringBuffer(
								"Skipping current coverage because cropped to an empty area")
								.toString());
			return;
		}
		// nothing to render, the AOI does not overlap
		// croppedGridCoverage.prefetch(croppedGridCoverage.getEnvelope2D());

		// try {
		// ImageIO.write(croppedGridCoverage.geophysics(false)
		// .getRenderedImage(), "png", new File("c:/cropped.png"));
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

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
		if (!GCCRSToDeviceCRSTransform.isIdentity()) {
			finalGridToWorldInGCCRS = new AffineTransform(
					(AffineTransform) GridGeometry2D.getTransform(
							new GeneralGridRange(destinationSize),
							destinationEnvelopeInSourceGCCRS, false));
		} else {
			finalGridToWorldInGCCRS = new AffineTransform(finalGridToWorld);
		}
		final boolean lonFirst = !GridGeometry2D.swapXY(CRSUtilities.getCRS2D(
				sourceCoverageCRS).getCoordinateSystem());

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
		final AffineTransform croppedCoverageGridToWorldTransform = (AffineTransform) ((GridGeometry2D) croppedGridCoverage
				.getGridGeometry()).getGridToCoordinateSystem2D();
		final double actualScaleX = lonFirst ? croppedCoverageGridToWorldTransform
				.getScaleX()
				: croppedCoverageGridToWorldTransform.getShearY();
		final double actualScaleY = lonFirst ? croppedCoverageGridToWorldTransform
				.getScaleY()
				: croppedCoverageGridToWorldTransform.getShearX();
		final double scaleX = actualScaleX
				/ (lonFirst ? finalGridToWorldInGCCRS.getScaleX()
						: finalGridToWorldInGCCRS.getShearY());
		final double scaleY = actualScaleY
				/ (lonFirst ? finalGridToWorldInGCCRS.getScaleY()
						: finalGridToWorldInGCCRS.getShearX());
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Scale factors are ").append(scaleX)
					.append(" ").append(scaleY).toString());
		final GridRange range = gc.getGridGeometry().getGridRange();
		final int actualW = range.getLength(0);
		final int actualH = range.getLength(1);
		if (Math.round(actualW *scaleX) < MIN_DIM_TOLERANCE
				|| Math.round(actualH * scaleY )< MIN_DIM_TOLERANCE) {
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
			// first step for down smapling is filtered subsample which is fast.
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

			// //
			//
			// Second step is scale
			//
			// //
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Scale down with factors ").append(
						scaleX * scaleXInt).append(scaleY * scaleYInt)
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

			// // TODO: remove these when the resample bug is fixed
			// LOGGER.info("Scaled grid coverage envelope: "
			// + scaledGridCoverage.getEnvelope());
			// LOGGER.info("Scaled grid coverage CRS: "
			// + scaledGridCoverage.getCoordinateReferenceSystem());

			// ///////////////////////////////////////////////////////////////////
			//
			// REPROJECT to the requested crs.
			//
			//
			// ///////////////////////////////////////////////////////////////////
			if (!GCCRSToDeviceCRSTransform.isIdentity()) {
				preSymbolizer = resample(scaledGridCoverage, destinationCRS,
						interpolation == null ? new InterpolationBilinear()
								: interpolation);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuffer("Reprojecting to crs ")
							.append(destinationCRS.toWKT()).toString());
			} else
				preSymbolizer = scaledGridCoverage;

			// // TODO: remove these when the resample bug is fixed
			// LOGGER.info("Reprojected grid coverage envelope: "
			// + preSymbolizer.getEnvelope());
			// LOGGER.info("Reprojected grid coverage CRS: "
			// + preSymbolizer.getCoordinateReferenceSystem());
		} else {

			// ///////////////////////////////////////////////////////////////////
			//
			// REPROJECT to the requested crs
			//
			//
			// ///////////////////////////////////////////////////////////////////
			final GridCoverage2D reprojectedCoverage;
			if (!GCCRSToDeviceCRSTransform.isIdentity()) {
				reprojectedCoverage = resample(croppedGridCoverage,
						destinationCRS,
						interpolation == null ? new InterpolationBilinear()
								: interpolation);
				if (LOGGER.isLoggable(Level.FINE))
					LOGGER.fine(new StringBuffer("Reprojecting to crs ")
							.append(destinationCRS.toWKT()).toString());
			} else
				reprojectedCoverage = croppedGridCoverage;

			// ///////////////////////////////////////////////////////////////////
			//
			// SCALE UP to the needed resolution
			//
			// ///////////////////////////////////////////////////////////////////
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.fine(new StringBuffer("Scale up with factors ").append(
						scaleX ).append(scaleY)
						.toString());
			preSymbolizer = (GridCoverage2D) scale(scaleX, scaleY, 0f, 0f,
					interpolation == null ? new InterpolationBilinear()
							: interpolation, BorderExtender
							.createInstance(BorderExtender.BORDER_COPY),
					reprojectedCoverage);

		}
		//
		// try {
		// ImageIO.write(preSymbolizer.geophysics(false).getRenderedImage(),
		// "png", new File("c:/preSymbolizer.png"));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ///////////////////////////////////////////////////////////////////
		//
		// RECOLOR
		//
		//
		// ///////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine(new StringBuffer("Raster Symbolizer ")
					.toString());
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
						.getGridGeometry()).getGridToCoordinateSystem2D());
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
		final AffineTransform cloneFinalWorldToGrid = (AffineTransform) finalWorldToGrid
				.clone();
		cloneFinalWorldToGrid.concatenate(finalGCgridToWorld);
		// it should be a simple translation
		graphics.drawRenderedImage(finalImage, cloneFinalWorldToGrid);

		// final BufferedImage buf = new BufferedImage((int) destinationSize
		// .getWidth(), (int) destinationSize.getHeight(),
		// BufferedImage.TYPE_4BYTE_ABGR);
		// final Graphics2D g = (Graphics2D) buf.getGraphics();
		// g.drawRenderedImage(finalImage, cloneFinalWorldToGrid);
		// g.dispose();
		// try {
		// ImageIO.write(buf, "png", new File("c:/final.png"));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// ///////////////////////////////////////////////////////////////////
		//
		// Restore old composite
		//
		// ///////////////////////////////////////////////////////////////////
		graphics.setComposite(oldAlphaComposite);

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
			CoordinateReferenceSystem crs, final Interpolation interpolation) {

		final ParameterValueGroup param = (ParameterValueGroup) resampleParams
				.clone();
		param.parameter("source").setValue(gc);
		param.parameter("CoordinateReferenceSystem").setValue(crs);
		param.parameter("InterpolationType").setValue(interpolation);
		return (GridCoverage2D) resampleFactory.doOperation(param, hints);

	}

	/**
	 * Subsampling the provided grid coverage 2d with the provided parameters.
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
		final GridCoverage2D tempGC;
		if (!intersectionEnvelope.equals((GeneralEnvelope) oldEnvelope)) {
			// crop
			final ParameterValueGroup param = (ParameterValueGroup) cropParams
					.clone();
			param.parameter("source").setValue(gc);
			param.parameter("Envelope").setValue(intersectionEnvelope);
			tempGC = (GridCoverage2D) coverageCropFactory.doOperation(param,
					hints);
		} else
			tempGC = gc;

		return tempGC;

	}

}
