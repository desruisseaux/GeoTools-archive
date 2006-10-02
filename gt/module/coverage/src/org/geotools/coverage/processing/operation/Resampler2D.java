/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Développement
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

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.Warp;
import javax.media.jai.RenderedOp;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.CannotReprojectException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.WarpTransform2D;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.image.jai.Registry;
import org.geotools.util.NumberRange;

/**
 * Implementation of the {@link Resample} operation. This implementation is
 * provided as a separated class for two purpose: avoid loading this code before
 * needed and provide some way to check if a grid coverages is a result of a
 * resample operation.
 * 
 * @since 2.2
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/module/coverage/src/org/geotools/coverage/processing/operation/Resampler2D.java $
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Resampler2D extends GridCoverage2D {
	/**
	 * The logging level for defails about resampling operation applied.
	 */
	private static final Level LOGGING_LEVEL = Level.FINE;

	/**
	 * Lock for disabling the acceleration on this operation.
	 * 
	 * 
	 * In JAI 1.1.2, the "Affine" operation on TYPE_FLOAT datatype with
	 * INTERP_BILINEAR interpolation cause an exception in the native code of
	 * medialib, which halt the Java Virtual Machine. Using the pure Java
	 * implementation instead resolve the problem.
	 * 
	 * @todo Remove this hack when Sun will fix the medialib bug. See
	 *       http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854
	 */
	private final static int lock[] = new int[1];

	/**
	 * Constructs a new grid coverage for the specified grid geometry.
	 * 
	 * @param source
	 *            The source for this grid coverage.
	 * @param image
	 *            The image.
	 * @param geometry
	 *            The grid geometry (including the new CRS).
	 */
	private Resampler2D(final GridCoverage2D source, final PlanarImage image,
			final GridGeometry2D geometry) {
		super(source.getName(), image, geometry, source.getSampleDimensions(),
				new GridCoverage2D[] { source }, null);
	}

	/**
	 * Creates a new coverage with a different coordinate reference reference
	 * system. If a grid geometry is supplied, only its
	 * {@linkplain GridGeometry2D#getRange grid range} and
	 * {@linkplain GridGeometry2D#getGridToCoordinateSystem grid to CRS}
	 * transform are taken in account.
	 * 
	 * @param sourceCoverage
	 *            The source grid coverage.
	 * @param targetCRS
	 *            Coordinate reference system for the new grid coverage, or
	 *            {@code null}.
	 * @param targetGG
	 *            The target grid geometry, or {@code null} for default.
	 * @param interpolation
	 *            The interpolation to use.
	 * @param hints
	 *            The rendering hints. This is usually provided by
	 *            {@link AbstractProcessor}. This method will looks for
	 *            {@link Hints#COORDINATE_OPERATION_FACTORY} and
	 *            {@link Hints#JAI_INSTANCE} keys.
	 * @return The new grid coverage, or {@code sourceCoverage} if no resampling
	 *         was needed.
	 * @throws FactoryException
	 *             is a transformation step can't be created.
	 * @throws TransformException
	 *             if a transformation failed.
	 */
	public static GridCoverage2D reproject(GridCoverage2D sourceCoverage,
			CoordinateReferenceSystem targetCRS, GridGeometry2D targetGG,
			final Interpolation interpolation, final Hints hints)
			throws FactoryException, TransformException {
		/*
		 * Grid range and "grid to CRS" transform are the only grid geometry
		 * informations used by this method. If they are not available, this is
		 * equivalent to not providing grid geometry at all. Set to null, since
		 * null value is what the remaining code will check for.
		 */
		if (targetGG != null) {
			if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE)
					&& !targetGG.isDefined(GridGeometry2D.GRID_TO_CRS)) {
				targetGG = null;
			}
		}
		final boolean automaticGG = (targetGG == null);
		final boolean automaticGR = automaticGG
				|| !targetGG.isDefined(GridGeometry2D.GRID_RANGE);
		/*
		 * If the source coverage is already the result of a previous "Resample"
		 * operation, go up in the chain and check if a previously computed
		 * image could fits (i.e. the requested resampling may be the inverse of
		 * a previous resampling). This method may stop immediately if a
		 * suitable image is found. Otherwise, we will resample the original
		 * image instead of the user-supplied one, in order to reduce the amount
		 * of intermediate steps (and maybe give a chance to the garbage
		 * collector to collect the user-supplied image).
		 */
		boolean sameGG, sameCRS;
		GridGeometry2D sourceGG;
		final Envelope sourceEnvelope;
		final GeneralEnvelope targetEnvelope;
		CoordinateReferenceSystem sourceCRS;
		final JAI processor;
		while (true) {
			sourceGG = (GridGeometry2D) sourceCoverage.getGridGeometry(); // TODO:
			// remove
			// cast
			// with
			// J2SE
			// 1.5.
			sourceCRS = sourceCoverage.getCoordinateReferenceSystem();
			if (targetCRS == null) {
				targetCRS = sourceCRS;
			}
			sameGG = (targetGG == null || equivalent(targetGG, sourceGG));
			sameCRS = CRSUtilities.equalsIgnoreMetadata(targetCRS, sourceCRS);
			if (sameGG && sameCRS) {
				return sourceCoverage;
			}
			if (sourceCoverage instanceof Resampler2D) {
				final List sources = sourceCoverage.getSources();
				if (sources.size() != 1) {
					// Should not happen, but test anyway.
					throw new AssertionError(sources);
				}
				sourceCoverage = (GridCoverage2D) sources.get(0);
				continue;
			}
			break;
		}
		/*
		 * The projection are usually applied on floating-point values, in order
		 * to gets maximal precision and to handle correctly the special case of
		 * NaN values. However, we can apply the projection on integer values if
		 * the interpolation type is "nearest neighbor", since this is not
		 * really an interpolation.
		 * 
		 * If this condition is meets, then we verify if an "integer version" of
		 * the image is available as a source of the source coverage (i.e. the
		 * floating-point image is derived from the integer image, not the
		 * converse).
		 */
		Boolean targetGeophysics = null;
		if (interpolation instanceof InterpolationNearest) {
			final GridCoverage2D candidate = sourceCoverage.geophysics(false);
			if (candidate != sourceCoverage) {
				final List sources = sourceCoverage.getRenderedImage()
						.getSources();
				if (sources != null) {
					if (sources.contains(candidate.getRenderedImage())) {
						sourceCoverage = candidate;
						targetGeophysics = Boolean.TRUE;
					}
				}
			}
		}
		/*
		 * The source coverage is now selected and will not change anymore. Gets
		 * the JAI instance and factories to use from the rendering hints.
		 */
		if (true) {
			final Object property = (hints != null) ? hints
					.get(Hints.JAI_INSTANCE) : null;
			if (property instanceof JAI) {
				processor = (JAI) property;
			} else {
				processor = JAI.getDefaultInstance();
			}
		}
		final CoordinateOperationFactory factory = FactoryFinder
				.getCoordinateOperationFactory(hints);
		final MathTransformFactory mtFactory = FactoryFinder
				.getMathTransformFactory(hints);
		/*
		 * Computes the INVERSE of the math transform from [Source Grid] to
		 * [Target Grid]. The transform will be computed using the following
		 * path:
		 * 
		 * Target Grid --> Target CRS --> Source CRS --> Source Grid ^ ^ ^ step
		 * 1 step 2 step 3
		 * 
		 * If source and target CRS are equal, a shorter path is used. This
		 * special case is needed because 'sourceCRS' and 'targetCRS' may be
		 * null.
		 * 
		 * Target Grid --> Common CRS --> Source Grid ^ ^ step 1 step 3
		 */
		final MathTransform step1, step2, step3, allSteps, allSteps2D;
		if (sameCRS) {
			/*
			 * Note: targetGG should not be null, otherwise the code that
			 * computed 'sameCRS' should have already detected that this
			 * resample is not doing anything.
			 */
			if (!targetGG.isDefined(GridGeometry2D.GRID_TO_CRS)) {
				step1 = sourceGG.getGridToCoordinateSystem(); // Really
				// sourceGG, not
				// targetGG
				step2 = IdentityTransform.create(step1.getTargetDimensions());
				step3 = step1.inverse();
				allSteps = IdentityTransform
						.create(step1.getSourceDimensions());
				targetGG = new GridGeometry2D(targetGG.getGridRange(), step1,
						targetCRS);
			} else {
				step1 = targetGG.getGridToCoordinateSystem();
				step2 = IdentityTransform.create(step1.getTargetDimensions());
				step3 = sourceGG.getGridToCoordinateSystem().inverse();
				allSteps = mtFactory.createConcatenatedTransform(step1, step3);
				if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE)) {
					/*
					 * If the target grid range was not explicitely specified, a
					 * grid range will be automatically computed in such a way
					 * that it will maps to the same envelope (at least
					 * approximatively).
					 */
					Envelope gridRange;
					gridRange = toEnvelope(sourceGG.getGridRange());
					gridRange = CRSUtilities.transform(allSteps.inverse(),
							gridRange);
					targetGG = new GridGeometry2D(new GeneralGridRange(
							gridRange), step1, targetCRS);
				}
			}
			targetEnvelope = (GeneralEnvelope) targetGG.getEnvelope();
		} else {
			if (sourceCRS == null) {
				throw new CannotReprojectException(Errors
						.format(ErrorKeys.UNSPECIFIED_CRS));
			}
			step2 = factory.createOperation(targetCRS, sourceCRS)
					.getMathTransform();
			step3 = sourceGG.getGridToCoordinateSystem().inverse();
			sourceEnvelope = sourceCoverage.getEnvelope();
			targetEnvelope = CRSUtilities.transform(step2.inverse(),
					sourceEnvelope);
			targetEnvelope.setCoordinateReferenceSystem(targetCRS);
			/*
			 * If the target GridGeometry is incomplete, provides default values
			 * for the missing fields. Three cases may occurs: - User provided
			 * no GridGeometry at all. Then, constructs an image of the same
			 * size than the source image and set an envelope big enough to
			 * contains the projected coordinates. The transform will derivate
			 * from the grid range and the envelope. - User provided only a grid
			 * range. Then, set an envelope big enough to contains the projected
			 * coordinates. The transform will derivate from the grid range and
			 * the envelope. - User provided only a "grid to coordinate system"
			 * transform. Then, transform the projected envelope to "grid units"
			 * using the specified transform, and create a grid range big enough
			 * to hold the result.
			 */
			if (targetGG == null) {
				targetGG = new GridGeometry2D(sourceGG.getGridRange(),
						targetEnvelope);
				step1 = targetGG.getGridToCoordinateSystem();
			} else if (!targetGG.isDefined(GridGeometry2D.GRID_TO_CRS)) {
				targetGG = new GridGeometry2D(targetGG.getGridRange(),
						targetEnvelope);
				step1 = targetGG.getGridToCoordinateSystem();
			} else {
				step1 = targetGG.getGridToCoordinateSystem();
				if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE)) {
					final GeneralEnvelope gridRange;
					gridRange = CRSUtilities.transform(step1.inverse(),
							targetEnvelope);
					for (int i = gridRange.getDimension(); --i >= 0;) {
						// According OpenGIS specification, GridGeometry maps
						// pixel's center. But the bounding box was for all
						// pixels, not pixel's centers. Offset by 0.5 (use +0.5
						// for maximum too, not -0.5, since maximum is
						// exclusive).
						gridRange.setRange(i, gridRange.getMinimum(i) + 0.5,
								gridRange.getMaximum(i) + 0.5);
					}
					targetGG = new GridGeometry2D(new GeneralGridRange(
							gridRange), step1, targetCRS);
				}
			}
			/*
			 * Computes the final transform.
			 */
			if (step1.equals(step3.inverse())) {
				allSteps = step2;
			} else {
				allSteps = mtFactory.createConcatenatedTransform(mtFactory
						.createConcatenatedTransform(step1, step2), step3);
			}
		}
		allSteps2D = getMathTransform2D(allSteps, mtFactory, targetGG);
		if (!(allSteps2D instanceof MathTransform2D)) {
			// Should not happen with Geotools implementations. May happen
			// with some external implementations, but should stay unusual.
			throw new TransformException(Errors
					.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
		}
		/*
		 * Prepare the parameter block for the image to be created. Prepare also
		 * rendering hints, which contains mostly indications about tiles
		 * layout. The xmin, xmax, ymin and ymax bounds are relative to the
		 * target image.
		 */
		final PlanarImage sourceImage = PlanarImage
				.wrapRenderedImage(sourceCoverage.getRenderedImage());
		final GridRange sourceGR = sourceGG.getGridRange();
		final GridRange targetGR = targetGG.getGridRange();
		final int xAxis = targetGG.gridDimensionX;
		final int yAxis = targetGG.gridDimensionY;
		final int xmin = targetGR.getLower(xAxis);
		final int xmax = targetGR.getUpper(xAxis);
		final int ymin = targetGR.getLower(yAxis);
		final int ymax = targetGR.getUpper(yAxis);
		ParameterBlock paramBlk = new ParameterBlock().addSource(sourceImage);
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
			layout.unsetImageBounds();
			layout.unsetTileLayout();
		}
		if ((layout.getValidMask() & (ImageLayout.MIN_X_MASK
				| ImageLayout.MIN_Y_MASK | ImageLayout.WIDTH_MASK | ImageLayout.HEIGHT_MASK)) == 0) {
			layout.setMinX(targetGR.getLower(xAxis));
			layout.setMinY(targetGR.getLower(yAxis));
			layout.setWidth(targetGR.getLength(xAxis));
			layout.setHeight(targetGR.getLength(yAxis));
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
		/*
		 * Get the sample value to use for background. We will try to fetch this
		 * value from one of "no data" categories. For geophysics image, it is
		 * usually NaN. For non-geophysics image, it is usually 0.
		 */
		final GridSampleDimension[] sampleDimensions = sourceCoverage
				.getSampleDimensions();
		final double[] background = new double[sampleDimensions.length];
		for (int i = 0; i < background.length; i++) {
			final NumberRange range = sampleDimensions[i].getBackground()
					.getRange();
			final double min = range.getMinimum();
			final double max = range.getMaximum();
			if (range.isMinIncluded()) {
				background[i] = min;
			} else if (range.isMaxIncluded()) {
				background[i] = max;
			} else {
				background[i] = 0.5 * (min + max);
			}
		}
		/*
		 * If the user requests a new grid geometry with the same coordinate
		 * reference system, and if the grid geometry is equivalents to a simple
		 * extraction of a sub-area, then delegates the work to a "Crop"
		 * operation.
		 */
		String operation = null;
		if (allSteps.isIdentity()) {
			if (xmin >= sourceGR.getLower(sourceGG.gridDimensionX)
					&& xmax <= sourceGR.getUpper(sourceGG.gridDimensionX)
					&& ymin >= sourceGR.getLower(sourceGG.gridDimensionY)
					&& ymax <= sourceGR.getUpper(sourceGG.gridDimensionY)) {
				operation = "Crop";
				paramBlk = paramBlk.add((float) (xmin)).add((float) (ymin))
						.add((float) (xmax - xmin)).add((float) (ymax - ymin));
			}
		}
		/*
		 * Special case for the affine transform. Try to use the JAI "Affine"
		 * operation instead of the more general "Warp" one. JAI provides native
		 * acceleration for the affine operation. NOTE: "Affine", "Scale",
		 * "Translate", "Rotate" and similar operations ignore the 'xmin',
		 * 'ymin', 'width' and 'height' image layout. Consequently, we can't use
		 * this operation if the user provided explicitely a grid range.
		 * 
		 * Note: if the user didn't specified any grid geometry, then a yet
		 * cheaper approach is to just update the 'gridToCRS' value. We returns
		 * a grid coverage wrapping the SOURCE image with the updated grid
		 * geometry.
		 */
		if (operation == null) {
			if (allSteps instanceof AffineTransform) {
				if (automaticGG) {
					// Cheapest approach: just update 'gridToCRS'.
					GridCoverage2D targetCoverage;
					targetGG = new GridGeometry2D(sourceGG.getGridRange(),
							targetEnvelope);
					targetCoverage = new Resampler2D(sourceCoverage,
							sourceImage, targetGG);
					if (targetGeophysics != null) {
						targetCoverage = targetCoverage
								.geophysics(targetGeophysics.booleanValue());
					}
					return targetCoverage;
				}
				// More general approach: apply the affine transform.
				if (automaticGR) {
					operation = "Affine";
					final AffineTransform affine = (AffineTransform) allSteps
							.inverse();
					paramBlk = paramBlk.add(affine).add(interpolation).add(
							background);
				}
			}
		}
		if (operation == null) {
			/*
			 * General case: construct the warp transform.
			 */
			operation = "Warp";
			final Warp warp = WarpTransform2D.getWarp(sourceCoverage.getName(),
					(MathTransform2D) allSteps2D);
			paramBlk = paramBlk.add(warp).add(interpolation).add(background);
		}

		final RenderedOp targetImage;
		final PlanarImage currentRenderingOfTargetImage;
		if ((operation.equalsIgnoreCase("warp") || operation
				.equalsIgnoreCase("affine"))
				&& ((sourceCoverage.getRenderedImage().getSampleModel()
						.getDataType() == DataBuffer.TYPE_FLOAT || sourceCoverage
						.getRenderedImage().getSampleModel().getDataType() == DataBuffer.TYPE_DOUBLE) && !(interpolation instanceof InterpolationNearest))) {
			synchronized (lock) {

				/**
				 * Disables the native acceleration for the "Affine" operation.
				 * In JAI 1.1.2, the "Affine" operation on TYPE_FLOAT datatype
				 * with INTERP_BILINEAR interpolation cause an exception in the
				 * native code of medialib, which halt the Java Virtual Machine.
				 * Using the pure Java implementation instead resolve the
				 * problem.
				 * 
				 * @todo Remove this hack when Sun will fix the medialib bug.
				 *       See
				 *       http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854
				 */

				Registry.setNativeAccelerationAllowed("Affine", false);

				targetImage = processor.createNS(operation, paramBlk,
						targetHints);
				currentRenderingOfTargetImage = targetImage.getRendering();// force
				// rendering

				/**
				 * see above
				 */
				Registry.setNativeAccelerationAllowed("Affine", true);

			}
		} else {
			targetImage = processor.createNS(operation, paramBlk, targetHints);
			currentRenderingOfTargetImage = targetImage;
		}
		final Locale locale = sourceCoverage.getLocale(); // For logging
		// purpose.
		/*
		 * The JAI operation sometime returns an image with a bounding box
		 * different than what we expected. This is true especially for the
		 * "Affine" operation: the JAI documentation said explicitly that xmin,
		 * ymin, width and height image layout hints are ignored for this one.
		 * As a safety, we check the bounding box in any case. If it doesn't
		 * matches, then we will reconstruct the target grid geometry.
		 */
		final int[] lower = targetGR.getLowers();
		final int[] upper = targetGR.getUppers();
		lower[xAxis] = currentRenderingOfTargetImage.getMinX();
		lower[yAxis] = currentRenderingOfTargetImage.getMinY();
		upper[xAxis] = currentRenderingOfTargetImage.getMaxX();
		upper[yAxis] = currentRenderingOfTargetImage.getMaxY();
		final GridRange actualGR = new GeneralGridRange(lower, upper);
		if (!targetGR.equals(actualGR)) {
			MathTransform gridToCRS = targetGG.getGridToCoordinateSystem();
			targetGG = new GridGeometry2D(actualGR, gridToCRS, targetCRS);
			if (!automaticGR) {
				log(Logging.getResources(locale).getLogRecord(Level.WARNING,
						LoggingKeys.ADJUSTED_GRID_GEOMETRY_$1,
						sourceCoverage.getName().toString(locale)));
			}
		}
		/*
		 * Constructs the final grid coverage, then log a message as in the
		 * following example:
		 * 
		 * Resampled coverage "Foo" from coordinate system "myCS" (for an image
		 * of size 1000x1500) to coordinate system "WGS84" (image size
		 * 1000x1500). JAI operation is "Warp" with "Nearest" interpolation on
		 * geophysics pixels values. Background value is 255.
		 */
		GridCoverage2D targetCoverage = new Resampler2D(sourceCoverage,
				currentRenderingOfTargetImage, targetGG);
		if (targetGeophysics != null) {
			targetCoverage = targetCoverage.geophysics(targetGeophysics
					.booleanValue());
		}
		assert CRSUtilities.equalsIgnoreMetadata(targetCoverage
				.getCoordinateReferenceSystem(), targetCRS) : targetCoverage;
		assert ((GridGeometry2D) targetCoverage.getGridGeometry())
				.getGridRange2D().equals(
						currentRenderingOfTargetImage.getBounds()) : targetGG;

		if (AbstractProcessor.LOGGER.isLoggable(LOGGING_LEVEL)) {
			log(Logging
					.getResources(locale)
					.getLogRecord(
							LOGGING_LEVEL,
							LoggingKeys.APPLIED_RESAMPLE_$11,
							new Object[] {
									/* {0} */sourceCoverage.getName()
											.toString(locale),
									/* {1} */sourceCoverage
											.getCoordinateReferenceSystem()
											.getName().getCode(),
									/* {2} */new Integer(sourceImage
											.getWidth()),
									/* {3} */new Integer(sourceImage
											.getHeight()),
									/* {4} */targetCoverage
											.getCoordinateReferenceSystem()
											.getName().getCode(),
									/* {5} */new Integer(targetImage
											.getWidth()),
									/* {6} */new Integer(targetImage
											.getHeight()),
									/* {7} */targetImage.getOperationName(),
									/* {8} */new Integer(
											sourceCoverage == sourceCoverage
													.geophysics(true) ? 1 : 0),
									/* {9} */ImageUtilities
											.getInterpolationName(interpolation),
									/* {10} */background.length == 1 ? (Double
											.isNaN(background[0]) ? (Object) "NaN"
											: (Object) new Double(background[0]))
											: (Object) XArray.toString(
													background, locale) }));
		}
		return targetCoverage;
	}

	/**
	 * Returns the math transform for the two specified dimensions of the
	 * specified transform.
	 * 
	 * @param transform
	 *            The transform.
	 * @param mtFactory
	 *            The factory to use for extracting the sub-transform.
	 * @param sourceGG
	 *            The grid geometry which is the source of the <strong>transform</strong>.
	 *            This is {@code targetGG} in the {@link #reproject} method,
	 *            because the later computes a transform from target to source
	 *            grid geometry.
	 * @return The {@link MathTransform2D} part of {@code transform}.
	 * @throws FactoryException
	 *             if {@code transform} is not separable.
	 */
	private static MathTransform2D getMathTransform2D(
			final MathTransform transform,
			final MathTransformFactory mtFactory, final GridGeometry2D sourceGG)
			throws FactoryException {
		final DimensionFilter filter = new DimensionFilter(mtFactory);
		filter.addSourceDimension(sourceGG.axisDimensionX);
		filter.addSourceDimension(sourceGG.axisDimensionY);
		MathTransform candidate = filter.separate(transform);
		if (candidate instanceof MathTransform2D) {
			return (MathTransform2D) candidate;
		}
		filter.addTargetDimension(sourceGG.axisDimensionX);
		filter.addTargetDimension(sourceGG.axisDimensionY);
		candidate = filter.separate(transform);
		if (candidate instanceof MathTransform2D) {
			return (MathTransform2D) candidate;
		}
		throw new FactoryException(Errors
				.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
	}

	/**
	 * Checks if two geometries are equal, ignoring unspecified fields. If one
	 * or both geometries has no "gridToCRS" transform, then this properties is
	 * not taken in account. Same apply for the grid range.
	 * 
	 * @param range1
	 *            The first range.
	 * @param range2
	 *            The second range.
	 * @return {@code true} if the two geometries are equal, ignoring
	 *         unspecified fields.
	 */
	private static boolean equivalent(final GridGeometry2D geom1,
			final GridGeometry2D geom2) {
		if (geom1.equals(geom2)) {
			return true;
		}
		if (geom1.isDefined(GridGeometry2D.GRID_RANGE)
				&& geom2.isDefined(GridGeometry2D.GRID_RANGE)) {
			if (!geom1.getGridRange().equals(geom2.getGridRange())) {
				return false;
			}
		}
		if (geom1.isDefined(GridGeometry2D.GRID_TO_CRS)
				&& geom2.isDefined(GridGeometry2D.GRID_TO_CRS)) {
			if (!geom1.getGridToCoordinateSystem().equals(
					geom2.getGridToCoordinateSystem())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Cast the specified grid range into an envelope. This is used before to
	 * transform the envelope using
	 * {@link CTSUtilities#transform(MathTransform, Envelope)}.
	 */
	private static Envelope toEnvelope(final GridRange gridRange) {
		final int dimension = gridRange.getDimension();
		final double[] lower = new double[dimension];
		final double[] upper = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			lower[i] = gridRange.getLower(i);
			upper[i] = gridRange.getUpper(i);
		}
		return new GeneralEnvelope(lower, upper);
	}

	/**
	 * Log a message.
	 */
	static void log(final LogRecord record) {
		record.setSourceClassName("Resample");
		record.setSourceMethodName("doOperation");
		AbstractProcessor.LOGGER.log(record);
	}
}
