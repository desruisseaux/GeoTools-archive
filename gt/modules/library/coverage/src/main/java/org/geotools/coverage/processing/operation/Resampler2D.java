/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2002, Institut de Recherche pour le Dï¿½veloppement
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

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.Warp;
import javax.media.jai.WarpAffine;

import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.coverage.processing.CannotReprojectException;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.operation.CoordinateOperation;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.WarpTransform2D;
import org.geotools.resources.XArray;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.coverage.OperationStrategy;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.image.ImageUtilities;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.Envelope;


/**
 * Implementation of the {@link Resample} operation. This implementation is provided as a
 * separated class for two purpose: avoid loading this code before needed and provide some
 * way to check if a grid coverages is a result of a resample operation.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Resampler2D extends GridCoverage2D {
    /**
     * Small tolerance threshold for floating point number comparaisons.
     */
    private static final double EPS = 1E-6;

    /**
     * The logging level for defails about resampling operation applied.
     */
    private static final Level LOGGING_LEVEL = Level.FINE;

    /**
     * Constructs a new grid coverage for the specified grid geometry.
     *
     * @param source   The source for this grid coverage.
     * @param image    The image.
     * @param geometry The grid geometry (including the new CRS).
     * @param sampleDimensions The sample dimensions to be given to the new coverage.
     */
    private Resampler2D(final GridCoverage2D        source,
                        final PlanarImage           image,
                        final GridGeometry2D        geometry,
                        final GridSampleDimension[] sampleDimensions)
    {
        super(source.getName(), image, geometry, sampleDimensions,
              new GridCoverage2D[] {source}, null);
    }

    /**
     * Constructs a new grid coverage for the specified grid geometry.
     *
     * @param source   The source for this grid coverage.
     * @param image    The image.
     * @param geometry The grid geometry (including the new CRS).
     * @param actionTaken The action that was took before to computes the image.
     */
    private static GridCoverage2D create(final GridCoverage2D    source,
                                         final PlanarImage       image,
                                         final GridGeometry2D    geometry,
                                         final OperationStrategy actionTaken)
    {
        final GridSampleDimension[] sampleDimensions;
        switch (actionTaken) {
            case APPLY_COLOR_EXPANSION: {
                sampleDimensions = null;
                break;
            }
            default: {
                sampleDimensions = source.getSampleDimensions();
                break;
            }
        }
        GridCoverage2D coverage = new Resampler2D(source, image, geometry, sampleDimensions);
        switch (actionTaken) {
            case USE_NATIVE_VIEW:     coverage = coverage.geophysics(true);  break;
            case USE_GEOPHYSICS_VIEW: coverage = coverage.geophysics(false); break;
        }
        return coverage;
    }

    /**
     * Creates a new coverage with a different coordinate reference reference system. If a
     * grid geometry is supplied, only its {@linkplain GridGeometry2D#getRange grid range}
     * and {@linkplain GridGeometry2D#getGridToCRS grid to CRS} transform are taken in account.
     *
     * @param sourceCoverage The source grid coverage.
     * @param targetCRS      Coordinate reference system for the new grid coverage, or {@code null}.
     * @param targetGG       The target grid geometry, or {@code null} for default.
     * @param interpolation  The interpolation to use.
     * @param hints          The rendering hints. This is usually provided by
     *                       {@link AbstractProcessor}. This method will looks for
     *                       {@link Hints#COORDINATE_OPERATION_FACTORY} and
     *                       {@link Hints#JAI_INSTANCE} keys.
     * @return The new grid coverage, or {@code sourceCoverage} if no resampling was needed.
     * @throws FactoryException if a transformation step can't be created.
     * @throws TransformException if a transformation failed.
     */
    public static GridCoverage2D reproject(GridCoverage2D            sourceCoverage,
                                           CoordinateReferenceSystem targetCRS,
                                           GridGeometry2D            targetGG,
                                           final Interpolation       interpolation,
                                           final Hints               hints)
            throws FactoryException, TransformException
    {
        ////////////////////////////////////////////////////////////////////////////////////////
        ////                                                                                ////
        ////    ====>>  PART 1: Extracts needed informations from the parameters   <<===    ////
        ////            PART 2: Creates the MathTransform                                   ////
        ////            PART 3: Applies the JAI operation (Affine", "Warp", ...)            ////
        ////                                                                                ////
        ////////////////////////////////////////////////////////////////////////////////////////

        /*
         * Grid range and "grid to CRS" transform are the only grid geometry informations used
         * by this method. If they are not available, this is equivalent to not providing grid
         * geometry at all. Set to null, since null value is what the remaining code will check
         * for.
         */
        if (targetGG != null) {
            if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE) &&
                !targetGG.isDefined(GridGeometry2D.GRID_TO_CRS))
            {
                targetGG = null;
            }
        }
        final boolean automaticGG = (targetGG == null);
        final boolean automaticGR = automaticGG || !targetGG.isDefined(GridGeometry2D.GRID_RANGE);
        /*
         * If the source coverage is already the result of a previous "Resample" operation,
         * go up in the chain and check if a previously computed image could fits (i.e. the
         * requested resampling may be the inverse of a previous resampling). This method
         * may stop immediately if a suitable image is found. Otherwise, we will resample
         * the original image instead of the user-supplied one, in order to reduce the amount
         * of intermediate steps (and maybe give a chance to the garbage collector to collect
         * the user-supplied image).
         */
        boolean sameGG, sameCRS;
        GridGeometry2D sourceGG;
        CoordinateReferenceSystem sourceCRS, compatibleSourceCRS;
        do {
            sourceGG  = sourceCoverage.getGridGeometry();
            sourceCRS = sourceCoverage.getCoordinateReferenceSystem();
            compatibleSourceCRS = sourceCRS;
            if (targetCRS == null) {
                targetCRS = sourceCRS;
            } else if (targetCRS.getCoordinateSystem().getDimension() == 2 &&
                       sourceCRS.getCoordinateSystem().getDimension()  > 2)
            {
                // Required for 'targetCRS' to 'sourceCRS' transforms.
                compatibleSourceCRS = sourceCoverage.getCoordinateReferenceSystem2D();
            }
            sameGG  = (targetGG == null || equivalent(targetGG, sourceGG));
            sameCRS = CRS.equalsIgnoreMetadata(targetCRS, sourceCRS);
            if (sameGG && (sameCRS || CRS.equalsIgnoreMetadata(targetCRS, compatibleSourceCRS))) {
                return sourceCoverage;
            }
            if (sourceCoverage instanceof Resampler2D) {
                final List sources = sourceCoverage.getSources();
                assert sources.size() == 1 : sources;
                sourceCoverage = (GridCoverage2D) sources.get(0);
                continue;
            }
            break;
        } while (true);
        /*
         * Preparing the source image.
         */
        PlanarImage sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage());
        final OperationStrategy actionTaken = CoverageUtilities.prepareSourceForOperation(
                sourceCoverage, interpolation, false, hints);
        switch (actionTaken) {
            case APPLY_COLOR_EXPANSION: {
                // sourceCoverage is now invalid, except for envelope and CRS.
                sourceImage = new ImageWorker(sourceImage).forceComponentColorModel().getPlanarImage();
                break;
            }
            case USE_GEOPHYSICS_VIEW: {
                // In this case we need to go back the geophysics view of the source coverage.
                sourceCoverage = sourceCoverage.geophysics(true);
                sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage());
                break;
            }
            case USE_NATIVE_VIEW: {
                sourceCoverage = sourceCoverage.geophysics(false);
                sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage());
                break;
            }
        }
        assert sourceCoverage.getCoordinateReferenceSystem() == sourceCRS : sourceCoverage;
        ParameterBlock paramBlk = new ParameterBlock();
        paramBlk.addSource(sourceImage);
        /*
         * Hints management
         */
        RenderingHints targetHints = ImageUtilities.getRenderingHints(sourceImage);
        if (targetHints == null) {
            targetHints = new RenderingHints(null);
            if (hints != null) {
            	targetHints.add(hints);
            }
        } else if (hints != null) {
            targetHints.add(hints);
        }
        /*
         * The source coverage is now selected and will not change anymore. Gets
         * the JAI instance and factories to use from the rendering hints.
         */
        final JAI processor;
        if (true) {
            final Object property = (hints != null) ? hints.get(Hints.JAI_INSTANCE) : null;
            if (property instanceof JAI) {
                processor = (JAI) property;
            } else {
                processor = JAI.getDefaultInstance();
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        ////                                                                                ////
        ////            PART 1: Extracts needed informations from the parameters            ////
        ////    ====>>  PART 2: Creates the MathTransform                          <<===    ////
        ////            PART 3: Applies the JAI operation (Affine", "Warp", ...)            ////
        ////                                                                                ////
        ////////////////////////////////////////////////////////////////////////////////////////

        final CoordinateOperationFactory factory =
                ReferencingFactoryFinder.getCoordinateOperationFactory(hints);
        final MathTransformFactory mtFactory =
                ReferencingFactoryFinder.getMathTransformFactory(hints);
        /*
         * Computes the INVERSE of the math transform from [Source Grid] to [Target Grid].
         * The transform will be computed using the following path:
         *
         *      Target Grid --> Target CRS --> Source CRS --> Source Grid
         *                   ^              ^              ^
         *                 step 1         step 2         step 3
         *
         * If source and target CRS are equal, a shorter path is used. This special
         * case is needed because 'sourceCRS' and 'targetCRS' may be null.
         *
         *      Target Grid --> Common CRS --> Source Grid
         *                   ^              ^
         *                 step 1         step 3
         */
        final MathTransform step1, step2, step3, allSteps, allSteps2D;
        if (sameCRS) {
            /*
             * Note: targetGG should not be null, otherwise the code that computed 'sameCRS'
             *       should have already detected that this resample is not doing anything.
             */
            if (!targetGG.isDefined(GridGeometry2D.GRID_TO_CRS)) {
                step1    = sourceGG.getGridToCRS(); // Really sourceGG, not targetGG
                step2    = IdentityTransform.create(step1.getTargetDimensions());
                step3    = step1.inverse();
                allSteps = IdentityTransform.create(step1.getSourceDimensions());
                targetGG = new GridGeometry2D(targetGG.getGridRange(), step1, targetCRS);
            } else {
                step1    = targetGG.getGridToCRS();
                step2    = IdentityTransform.create(step1.getTargetDimensions());
                step3    = sourceGG.getGridToCRS().inverse();
                allSteps = mtFactory.createConcatenatedTransform(step1, step3);
                if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE)) {
                    /*
                     * If the target grid range was not explicitely specified, a grid range will be
                     * automatically computed in such a way that it will maps to the same envelope
                     * (at least approximatively).
                     */
                    Envelope gridRange;
                    gridRange = toEnvelope(sourceGG.getGridRange());
                    gridRange = CRS.transform(allSteps.inverse(), gridRange);
                    targetGG  = new GridGeometry2D(new GeneralGridRange(gridRange), step1, targetCRS);
                }
            }
        } else {
            if (sourceCRS == null) {
                throw new CannotReprojectException(Errors.format(ErrorKeys.UNSPECIFIED_CRS));
            }
            final Envelope        sourceEnvelope;
            final GeneralEnvelope targetEnvelope;
            final CoordinateOperation operation = factory.createOperation(sourceCRS, targetCRS);
            final boolean force2D = (sourceCRS != compatibleSourceCRS);
            step2          = factory.createOperation(targetCRS, compatibleSourceCRS).getMathTransform();
            step3          = (force2D ? sourceGG.getGridToCRS2D() : sourceGG.getGridToCRS()).inverse();
            sourceEnvelope = sourceCoverage.getEnvelope(); // Don't force this one to 2D.
            targetEnvelope = CRS.transform(operation, sourceEnvelope);
            targetEnvelope.setCoordinateReferenceSystem(targetCRS);
            /*
             * If the target GridGeometry is incomplete, provides default
             * values for the missing fields. Three cases may occurs:
             *
             * - User provided no GridGeometry at all. Then, constructs an image of the same size
             *   than the source image and set an envelope big enough to contains the projected
             *   coordinates. The transform will derivate from the grid range and the envelope.
             *
             * - User provided only a grid range.  Then, set an envelope big enough to contains
             *   the projected coordinates. The transform will derivate from the grid range and
             *   the envelope.
             *
             * - User provided only a "grid to coordinate system" transform. Then, transform the
             *   projected envelope to "grid units" using the specified transform,  and create a
             *   grid range big enough to hold the result.
             */
            if (targetGG == null) {
                final GridRange targetGR;
                targetGR = force2D ? new GeneralGridRange(sourceGG.getGridRange2D()) : sourceGG.getGridRange();
                targetGG = new GridGeometry2D(targetGR, targetEnvelope);
                step1    = targetGG.getGridToCRS();
            } else if (!targetGG.isDefined(GridGeometry2D.GRID_TO_CRS)) {
                targetGG = new GridGeometry2D(targetGG.getGridRange(), targetEnvelope);
                step1    = targetGG.getGridToCRS();
            } else {
                step1 = targetGG.getGridToCRS();
                if (!targetGG.isDefined(GridGeometry2D.GRID_RANGE)) {
                    final GeneralEnvelope gridRange;
                    gridRange = CRS.transform(step1.inverse(), targetEnvelope);
                    for (int i=gridRange.getDimension(); --i>=0;) {
                        /*
                         * According OpenGIS specification, GridGeometry maps pixel's center.
                         * But the bounding box was for all pixels, not pixel's centers. Offset
                         * by 0.5 (use +0.5 for maximum too, not -0.5, since maximum is exclusive).
                         */
                        gridRange.setRange(i, gridRange.getMinimum(i) + 0.5,
                                              gridRange.getMaximum(i) + 0.5);
                    }
                    targetGG = new GridGeometry2D(new GeneralGridRange(gridRange), step1, targetCRS);
                }
            }
            /*
             * Computes the final transform.
             */
            if (step1.equals(step3.inverse())) {
                allSteps = step2;
            } else {
                allSteps = mtFactory.createConcatenatedTransform(
                           mtFactory.createConcatenatedTransform(step1, step2), step3);
            }
        }
        allSteps2D = getMathTransform2D(allSteps, mtFactory, targetGG);
        if (!(allSteps2D instanceof MathTransform2D)) {
            // Should not happen with Geotools implementations. May happen
            // with some external implementations, but should stay unusual.
            throw new TransformException(Errors.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        ////                                                                                ////
        ////            PART 1: Extracts needed informations from the parameters            ////
        ////            PART 2: Creates the MathTransform                                   ////
        ////    ====>>  PART 3: Applies the JAI operation (Affine", "Warp", ...)   <<===    ////
        ////                                                                                ////
        ////////////////////////////////////////////////////////////////////////////////////////

        /*
         * Prepare the parameter block for the image to be created. Prepare also
         * rendering hints, which contains mostly indications about tiles layout.
         * The xmin, xmax, ymin and ymax bounds are relative to the target image.
         */
        final GridRange targetGR = targetGG.getGridRange();
        final int       xAxis    = targetGG.gridDimensionX;
        final int       yAxis    = targetGG.gridDimensionY;
        final int       xmin     = targetGR.getLower(xAxis);
        final int       xmax     = targetGR.getUpper(xAxis);
        final int       ymin     = targetGR.getLower(yAxis);
        final int       ymax     = targetGR.getUpper(yAxis);
        final GridRange sourceGR = sourceGG.getGridRange();
        final int       xminS    = sourceGR.getLower(xAxis);
        final int       xmaxS    = sourceGR.getUpper(xAxis);
        final int       yminS    = sourceGR.getLower(yAxis);
        final int       ymaxS    = sourceGR.getUpper(yAxis);
        ImageLayout layout = (ImageLayout) targetHints.get(JAI.KEY_IMAGE_LAYOUT);
        if (layout != null) {
            layout = (ImageLayout) layout.clone();
        } else {
            layout = new ImageLayout(sourceImage);
            layout.unsetImageBounds();
            layout.unsetTileLayout();
            // At this point, only the color model and sample model are left valids.
        }
        if ((layout.getValidMask() & (ImageLayout.MIN_X_MASK |
                                      ImageLayout.MIN_Y_MASK |
                                      ImageLayout.WIDTH_MASK |
                                      ImageLayout.HEIGHT_MASK)) == 0)
        {
            layout.setMinX  (targetGR.getLower (xAxis));
            layout.setMinY  (targetGR.getLower (yAxis));
            layout.setWidth (targetGR.getLength(xAxis));
            layout.setHeight(targetGR.getLength(yAxis));
        }
        if ((layout.getValidMask() & (ImageLayout.TILE_WIDTH_MASK         |
                                      ImageLayout.TILE_HEIGHT_MASK        |
                                      ImageLayout.TILE_GRID_X_OFFSET_MASK |
                                      ImageLayout.TILE_GRID_Y_OFFSET_MASK)) == 0)
        {
            Dimension size = new Dimension(layout.getWidth (sourceImage),
                                           layout.getHeight(sourceImage));
            size = ImageUtilities.toTileSize(size);
            layout.setTileGridXOffset(layout.getMinX(sourceImage));
            layout.setTileGridYOffset(layout.getMinY(sourceImage));
            layout.setTileWidth (size.width);
            layout.setTileHeight(size.height);
            SampleModel model = layout.getSampleModel(sourceImage);
            model = model.createCompatibleSampleModel(size.width, size.height);
            layout.setSampleModel(model);
        }
        /*
         * We need to correctly manage the Hints to control the replacement of IndexColorModel.
         * It is worth to point out that setting the JAI.KEY_REPLACE_INDEX_COLOR_MODEL hint to
         * Boolean.TRUE is not enough to force the operators to do an expansion. If we explicitly
         * provide an ImageLayout built with the source image where the CM and the SM are valid.
         * those will be employed overriding a the possibility to expand the color model.
         */
        switch (actionTaken) {
            case APPLY_COLOR_EXPANSION: {
                targetHints.add(ImageUtilities.REPLACE_INDEX_COLOR_MODEL);
                layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
                layout.unsetValid(ImageLayout.SAMPLE_MODEL_MASK);
                break;
            }
            default: {
                targetHints.add(ImageUtilities.DONT_REPLACE_INDEX_COLOR_MODEL);
                break;
            }
        }
        targetHints.put(JAI.KEY_IMAGE_LAYOUT, layout);
        /*
         * If the user requests a new grid geometry with the same coordinate reference system,
         * and if the grid geometry is equivalents to a simple extraction of a sub-area, then
         * delegates the work to a "Crop" operation.
         */
        String operation = null;
        if (allSteps.isIdentity() || (allSteps instanceof AffineTransform &&
                XAffineTransform.isIdentity((AffineTransform) allSteps, EPS)))
        {
            if (xmin > xminS && xmax < xmaxS &&
                ymin > yminS && ymax < ymaxS)
            {
                operation = "Crop";
                paramBlk  = paramBlk.add((float) (xmin))
                                    .add((float) (ymin))
                                    .add((float) (xmax - xmin))
                                    .add((float) (ymax - ymin));
            } else if (xmin ==xminS && xmax == xmaxS &&
                       ymin ==yminS && ymax == ymaxS)
            {
                // Optimization in case we have nothing to do, not even a crop.
                return create(sourceCoverage, sourceImage, targetGG, actionTaken);
            }
        }
        /*
         * Special case for the affine transform. Try to use the JAI "Affine" operation instead of
         * the more general "Warp" one. JAI provides native acceleration for the affine operation.
         * NOTE: "Affine", "Scale", "Translate", "Rotate" and similar operations ignore the 'xmin',
         * 'ymin', 'width' and 'height' image layout. Consequently, we can't use this operation if
         * the user provided explicitely a grid range.
         *
         * Note: if the user didn't specified any grid geometry, then a yet cheaper approach is to
         *       just update the 'gridToCRS' value. We returns a grid coverage wrapping the SOURCE
         *       image with the updated grid geometry.
         */
        double[] background = null;
        if (operation == null) {
            if (allSteps instanceof AffineTransform && automaticGR) {
                if (automaticGG) {
                    background = null;// we won't use it
                    // Cheapest approach: just update 'gridToCRS'.
                    MathTransform mtr;
                    mtr = sourceGG.getGridToCRS();
                    mtr = mtFactory.createConcatenatedTransform(mtr,  step2.inverse());
                    targetGG = new GridGeometry2D(sourceGG.getGridRange(), mtr, targetCRS);
                    /*
                     * Note: do NOT use the "GridGeometry2D(sourceGridRange, targetEnvelope)"
                     * constructor in the above line. We must give a MathTransform argument to
                     * the constructor, not an Envelope, because the later infer a MathTransform
                     * using heuristic rules. Only the constructor with a MathTransform argument
                     * is fully accurate.
                     */
                    return create(sourceCoverage, sourceImage, targetGG, actionTaken);
                }
                // More general approach: apply the affine transform.
                if (automaticGR) {
                    operation = "Affine";
                    background = CoverageUtilities.getBackgroundValues(sourceCoverage);
                    final AffineTransform affine = (AffineTransform) allSteps.inverse();
                    paramBlk = paramBlk.add(affine).add(interpolation).add(background);
                }
            } else {
                /*
                 * General case: construct the warp transform.
                 */
                operation = "Warp";
                final Warp warp;
                if (allSteps2D instanceof AffineTransform) {
                    warp = new WarpAffine((AffineTransform) allSteps2D);
                } else {
                    warp = WarpTransform2D.getWarp(sourceCoverage.getName(), (MathTransform2D) allSteps2D);
                }
                background = CoverageUtilities.getBackgroundValues(sourceCoverage);
                paramBlk = paramBlk.add(warp).add(interpolation).add(background);
            }
        }
        final RenderedOp targetImage = processor.createNS(operation, paramBlk, targetHints);
        final Locale locale = sourceCoverage.getLocale();  // For logging purpose.
        /*
         * The JAI operation sometime returns an image with a bounding box different than what we
         * expected. This is true especially for the "Affine" operation: the JAI documentation said
         * explicitly that xmin, ymin, width and height image layout hints are ignored for this one.
         * As a safety, we check the bounding box in any case. If it doesn't matches, then we will
         * reconstruct the target grid geometry.
         */
        final int[] lower = targetGR.getLower().getCoordinateValues();
        final int[] upper = targetGR.getUpper().getCoordinateValues();
        lower[xAxis] = targetImage.getMinX();
        lower[yAxis] = targetImage.getMinY();
        upper[xAxis] = targetImage.getMaxX();
        upper[yAxis] = targetImage.getMaxY();
        final GridRange actualGR = new GeneralGridRange(lower, upper);
        if (!targetGR.equals(actualGR)) {
            MathTransform gridToCRS = targetGG.getGridToCRS();
            targetGG = new GridGeometry2D(actualGR, gridToCRS, targetCRS);
            if (!automaticGR) {
                log(Logging.getResources(locale).getLogRecord(Level.WARNING,
                    LoggingKeys.ADJUSTED_GRID_GEOMETRY_$1, sourceCoverage.getName().toString(locale)));
            }
        }
        /*
         * Constructs the final grid coverage, then log a message as in the following example:
         *
         *     Resampled coverage "Foo" from coordinate system "myCS" (for an image of size
         *     1000x1500) to coordinate system "WGS84" (image size 1000x1500). JAI operation
         *     is "Warp" with "Nearest" interpolation on geophysics pixels values. Background
         *     value is 255.
         */
        GridCoverage2D targetCoverage = create(sourceCoverage, targetImage, targetGG, actionTaken);
        assert CRS.equalsIgnoreMetadata(targetCoverage.getCoordinateReferenceSystem(), targetCRS) : targetGG;
        assert targetCoverage.getGridGeometry().getGridRange2D().equals(targetImage.getBounds())  : targetGG;
        if (AbstractProcessor.LOGGER.isLoggable(LOGGING_LEVEL)) {
            log(Logging.getResources(locale).getLogRecord(LOGGING_LEVEL,
                LoggingKeys.APPLIED_RESAMPLE_$11, new Object[] {
                /*  {0} */ sourceCoverage.getName().toString(locale),
                /*  {1} */ sourceCoverage.getCoordinateReferenceSystem().getName().getCode(),
                /*  {2} */ sourceImage.getWidth(),
                /*  {3} */ sourceImage.getHeight(),
                /*  {4} */ targetCoverage.getCoordinateReferenceSystem().getName().getCode(),
                /*  {5} */ targetImage.getWidth(),
                /*  {6} */ targetImage.getHeight(),
                /*  {7} */ targetImage.getOperationName(),
                /*  {8} */ Integer.valueOf(sourceCoverage == sourceCoverage.geophysics(true) ? 1 : 0),
                /*  {9} */ ImageUtilities.getInterpolationName(interpolation),
                /* {10} */ (background != null) ? background.length == 1 ? (Double.isNaN(background[0]) ? (Object) "NaN"
                                 : (Object) Double.valueOf(background[0]))
                                 : (Object) XArray.toString(background, locale)
                                 : "No background used" }));
        }
        return targetCoverage;
    }

    /**
     * Returns the math transform for the two specified dimensions of the specified transform.
     *
     * @param  transform The transform.
     * @param  mtFactory The factory to use for extracting the sub-transform.
     * @param  sourceGG  The grid geometry which is the source of the <strong>transform</strong>.
     *                   This is {@code targetGG} in the {@link #reproject} method, because the
     *                   later computes a transform from target to source grid geometry.
     * @return The {@link MathTransform2D} part of {@code transform}.
     * @throws FactoryException If {@code transform} is not separable.
     */
    private static MathTransform2D getMathTransform2D(final MathTransform        transform,
                                                      final MathTransformFactory mtFactory,
                                                      final GridGeometry2D       sourceGG)
            throws FactoryException
    {
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
        throw new FactoryException(Errors.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
    }

    /**
     * Checks if two geometries are equal, ignoring unspecified fields. If one or both
     * geometries has no "gridToCRS" transform, then this properties is not taken in account.
     * Same apply for the grid range.
     *
     * @param  range1 The first range.
     * @param  range2 The second range.
     * @return {@code true} if the two geometries are equal, ignoring unspecified fields.
     */
    private static boolean equivalent(final GridGeometry2D geom1, final GridGeometry2D geom2) {
        if (geom1.equals(geom2)) {
            return true;
        }
        if (geom1.isDefined(GridGeometry2D.GRID_RANGE) &&
            geom2.isDefined(GridGeometry2D.GRID_RANGE))
        {
            if (!geom1.getGridRange().equals(geom2.getGridRange())) {
                return false;
            }
        }
        if (geom1.isDefined(GridGeometry2D.GRID_TO_CRS) &&
            geom2.isDefined(GridGeometry2D.GRID_TO_CRS))
        {
            if (!geom1.getGridToCRS().equals(geom2.getGridToCRS())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Casts the specified grid range into an envelope. This is used before to transform
     * the envelope using {@link CRSUtilities#transform(MathTransform, Envelope)}.
     */
    private static Envelope toEnvelope(final GridRange gridRange) {
        final int dimension = gridRange.getDimension();
        final double[] lower = new double[dimension];
        final double[] upper = new double[dimension];
        for (int i=0; i<dimension; i++) {
            lower[i] = gridRange.getLower(i);
            upper[i] = gridRange.getUpper(i);
        }
        return new GeneralEnvelope(lower, upper);
    }

    /**
     * Logs a message.
     */
    private static void log(final LogRecord record) {
        record.setSourceClassName("Resample");
        record.setSourceMethodName("doOperation");
        AbstractProcessor.LOGGER.log(record);
    }
}
