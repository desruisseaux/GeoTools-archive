/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2002, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage.operation;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// JAI dependencies
import javax.media.jai.ImageLayout;
import javax.media.jai.IntegerSequence;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.processing.GridCoverageProcessor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
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
import org.geotools.coverage.grid.InvalidGridGeometryException;
import org.geotools.coverage.processing.AbstractGridCoverageProcessor;
import org.geotools.coverage.processing.CannotReprojectException;
import org.geotools.coverage.processing.GridCoverageProcessor2D;
import org.geotools.coverage.processing.Operation2D;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.operation.AbstractCoordinateOperationFactory;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.WarpTransform2D;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.util.NumberRange;


/**
 * Resample a grid coverage using a different grid geometry.
 * This operation provides the following functionality:<br>
 * <br>
 * <strong>Resampling</strong><br>
 * The grid coverage can be resampled at a different cell resolution. Some implementations
 * may be able to do resampling efficiently at any resolution. This can be determined from
 * the {@link GridCoverageProcessor} metadata <code>HasArbitraryResolutions</code> keyword.
 * Also a non-rectilinear grid coverage can be accessed as rectilinear grid coverage with
 * this operation.<br>
 * <br>
 * <strong>Reprojecting</strong><br>
 * The new grid geometry can have a different coordinate system than the underlying grid
 * geometry. For example, a grid coverage can be reprojected from a geodetic coordinate
 * system to Universal Transverse Mercator coordinate system.<br>
 * <br>
 * <strong>Subsetting</strong><br>
 * A subset of a grid can be viewed as a separate coverage by using this operation with a
 * grid geometry which as the same geoferencing and a region. Grid range in the grid geometry
 * defines the region to subset in the grid coverage.<br>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Resampler2D extends GridCoverage2D {
    /**
     * Disable the native acceleration for the "Affine" operation. In JAI 1.1.2, the "Affine"
     * operation on TYPE_FLOAT datatype with INTERP_BILINEAR interpolation cause an exception
     * in the native code of medialib, which halt the Java Virtual Machine. Using the pure Java
     * implementation instead resolve the problem.
     *
     * @todo Remove this hack when Sun will fix the medialib bug. See
     *       http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906854
     */
    static {
        ImageUtilities.allowNativeAcceleration("Affine", false);
    }

    /**
     * Constructs a new grid coverage for the specified grid geometry.
     *
     * @param source       The source for this grid coverage.
     * @param image        The image.
     * @param crs          The coordinate reference system.
     * @param gridToCRS    The grid to CRS transform.
     */
    private Resampler2D(final GridCoverage2D         source,
                        final RenderedImage           image,
                        final CoordinateReferenceSystem crs,
                        final MathTransform       gridToCRS)
    {
        super(source.getName(), image, crs, gridToCRS, source.getSampleDimensions(),
              new GridCoverage2D[]{source}, null);
    }

    /**
     * Constructs a new grid coverage for the specified envelope.
     *
     * @param source       The source for this grid coverage.
     * @param image        The image.
     * @param crs          The coordinate reference system.
     * @param envelope     The grid coverage cordinates.
     */
    private Resampler2D(final GridCoverage2D         source,
                        final RenderedImage           image,
                        final CoordinateReferenceSystem crs,
                        final Envelope             envelope)
    {
        super(source.getName(), image, crs, envelope, source.getSampleDimensions(),
              new GridCoverage2D[]{source}, null);
    }
    
    /**
     * Creates a new coverage with a different coordinate reference reference system.
     *
     * @param  sourceCoverage The source grid coverage.
     * @param  targetCRS Coordinate reference system for the new grid coverage, or {@code null}.
     * @param  targetGG The target grid geometry, or {@code null} for default.
     * @param  interpolation The interpolation to use.
     * @param  hints The rendering hints. This is usually provided by {@link GridCoverageProcessor2D}.
     *         This method will looks for {@link Hints#COORDINATE_OPERATION_FACTORY}
     *         and {@link Hints#JAI_INSTANCE} keys.
     * @return The new grid coverage, or <code>sourceCoverage</code> if no resampling was needed.
     * @throws FactoryException is a transformation step can't be created.
     * @throws TransformException if a transformation failed.
     */
    public static GridCoverage2D reproject(      GridCoverage2D       sourceCoverage,
                                           final CoordinateReferenceSystem targetCRS,
                                                 GridGeometry2D             targetGG,
                                           final Interpolation         interpolation,
                                           final Hints                         hints)
            throws FactoryException, TransformException
    {
        /*
         * Gets the JAI instance and factories to use from the rendering hints.
         */
        final JAI processor;
        if (true) {
            final Object property = (hints!=null) ? hints.get(Hints.JAI_INSTANCE) : null;
            if (property instanceof JAI) {
                processor = (JAI) property;
            } else {
                processor = JAI.getDefaultInstance();
            }
        }
        final CoordinateOperationFactory factory = FactoryFinder.getCoordinateOperationFactory(hints);
        final MathTransformFactory     mtFactory = FactoryFinder.getMathTransformFactory      (hints);
        /*
         * If the source coverage is already the result of a "Resample" operation,
         * go up in the chain and check if a previously computed image could fits.
         */
        GridGeometry2D            sourceGG;  boolean sameGG;
        CoordinateReferenceSystem sourceCRS; boolean sameCRS;
        while (true) {
            sourceGG  = (GridGeometry2D) sourceCoverage.getGridGeometry(); // TODO: remove cast with J2SE 1.5.
            sourceCRS = sourceCoverage.getCoordinateReferenceSystem();
            sameGG    = (targetGG ==null || equivalent(targetGG, sourceGG));
            sameCRS   = (targetCRS==null || CRSUtilities.equalsIgnoreMetadata(targetCRS, sourceCRS));
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
         * the interpolation type is "nearest neighbor", since this is not really
         * an interpolation.
         *
         * If this condition is meets, then we verify if an "integer version" of the image
         * is available as a source of the source coverage (i.e. the floating-point image
         * is derived from the integer image, not the converse).
         */
        Boolean targetGeophysics = null;
        if (interpolation instanceof InterpolationNearest) {
            final GridCoverage2D candidate = sourceCoverage.geophysics(false);
            if (candidate != sourceCoverage) {
                final List sources = sourceCoverage.getRenderedImage().getSources();
                if (sources != null) {
                    if (sources.contains(candidate.getRenderedImage())) {
                        sourceCoverage   = candidate;
                        targetGeophysics = Boolean.TRUE;
                    }
                }
            }
        }
        /*
         * Gets the target image as a {@link RenderedOp}. The source image is
         * initially wrapped into a "Null" operation. Later, we will change
         * this "Null" operation into a "Warp" operation. We can't use "Warp"
         * now because we will know the envelope only after creating the GridCoverage.
         * Note: RenderingHints contain mostly indications about tiles layout.
         */
        final PlanarImage sourceImage = PlanarImage.wrapRenderedImage(sourceCoverage.getRenderedImage());
        final ParameterBlock paramBlk = new ParameterBlock().addSource(sourceImage);
        RenderingHints    targetHints = ImageUtilities.getRenderingHints(sourceImage);
        if (targetHints == null) {
            targetHints = hints;
        } else if (hints != null) {
            targetHints.add(hints);
        }
        final RenderedOp  targetImage = processor.createNS("Null", paramBlk, targetHints);
        GridCoverage2D targetCoverage = null;
        /*
         * Computes the INVERSE of the math transform from [Source Grid] to [Target Grid].
         * The transform will be computed using the inverse of the following path:
         *
         *      Source Grid --> Source CRS --> Target CRS --> Target Grid
         *
         * If source and target CRS are equal, a shorter path is used. This special case
         * is needed because 'targetCRS' may be null (which means "same CRS than source").
         * Note that 'sourceCRS' may be null as well.
         *
         *      Source Grid --> Common CRS --> Target Grid
         */
        final MathTransform transform, step1, step2, step3, step2x;
        if (sameCRS) {
            step2x = null;
            step2  = IdentityTransform.create(2);
            if (!GCSUtilities.hasTransform(targetGG)) {
                // TargetGG should not be null, otherwise the code above should
                // have already detected that this resample is not doing anything.
                targetGG = new GridGeometry2D(targetGG.getGridRange(),
                                              sourceGG.getGridToCoordinateSystem());
            }
        } else {
            if (sourceCRS==null || targetCRS==null) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_UNSPECIFIED_COORDINATE_SYSTEM));
            }
            final MathTransform step2r;
            step2x = factory.createOperation(sourceCRS, targetCRS).getMathTransform();
            step2r = getMathTransform2D(step2x, mtFactory, sourceGG.axisDimensionX,
                                                           sourceGG.axisDimensionY);
            /*
             * Gets the source and target envelope. It is difficult to check if the first
             * two dimensions are really independent from other dimensions.   However, if
             * we get the same 2-dimensional envelope no matter if we took in account the
             * extra dimensions or not, then we will assume that projecting the image with
             * a MathTransform2D is safe enough.
             */
            final Envelope sourceEnvelope   = sourceCoverage.getEnvelope();
            final Envelope sourceEnvelope2D = sourceCoverage.getEnvelope2D();
            final Envelope targetEnvelope   = CRSUtilities.transform(step2x, sourceEnvelope  );
            final Envelope targetEnvelope2D = CRSUtilities.transform(step2r, sourceEnvelope2D);
            if (!new Envelope2D(targetEnvelope2D).boundsEquals(targetEnvelope,
                    sourceGG.axisDimensionX, sourceGG.axisDimensionY, 1E-8))
            {
                throw new TransformException(Resources.format(
                        ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
            }
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
                targetCoverage = new Resampler2D(sourceCoverage, targetImage, targetCRS, targetEnvelope);
                targetGG = (GridGeometry2D) targetCoverage.getGridGeometry();
            }
            else if (!GCSUtilities.hasTransform(targetGG)) {
                targetGG = new GridGeometry2D(targetGG.getGridRange(), targetEnvelope, null);
            }
            else if (!GCSUtilities.hasGridRange(targetGG)) {
                final MathTransform step3x = targetGG.getGridToCoordinateSystem();
                final GeneralEnvelope gridRange = CRSUtilities.transform(step3x.inverse(), targetEnvelope);
                for (int i=gridRange.getDimension(); --i>=0;) {
                    // According OpenGIS specification, GridGeometry maps pixel's center. But
                    // the bounding box was for all pixels, not pixel's centers. Offset by
                    // 0.5 (use +0.5 for maximum too, not -0.5, since maximum is exclusive).
                    gridRange.setRange(i, gridRange.getMinimum(i)+0.5, gridRange.getMaximum(i)+0.5);
                }
                targetGG = new GridGeometry2D(GCSUtilities.toGridRange(gridRange), step3x);
            }
            step2 = step2r.inverse();
        }
        /*
         * Complete the transformation from [Target Grid] to [Source Grid]. If the target grid
         * range was not explicitely specified, a grid range will be automatically computed in
         * such a way that it will map to the same envelope (at least approximatively). We use
         * the inverse of 'transform' for this purpose, except that the transform to be used for
         * grid range may have more than 2 dimensions.
         */
        step1 = targetGG.getGridToCoordinateSystem2D();
        step3 = sourceGG.getGridToCoordinateSystem2D().inverse();
        if (step1.equals(step3.inverse())) {
            transform = step2;
        } else {
            transform = mtFactory.createConcatenatedTransform(
                        mtFactory.createConcatenatedTransform(step1, step2), step3);
        }
        if (!(transform instanceof MathTransform2D)) {
            // Should not happen with Geotools implementations. May happen
            // with some external implementations, but should stay unusual.
            throw new TransformException(Resources.format(
                                         ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
        }
        if (!GCSUtilities.hasGridRange(targetGG)) {
            final MathTransform xtr;
            final MathTransform step1x = targetGG.getGridToCoordinateSystem();
            final MathTransform step3x = sourceGG.getGridToCoordinateSystem().inverse();
            if (step1x.equals(step3x.inverse())) {
                xtr = step2x;
            } else if (step2x == null) {
                xtr = mtFactory.createConcatenatedTransform(step1x, step3x);
            } else {
                xtr = mtFactory.createConcatenatedTransform(
                      mtFactory.createConcatenatedTransform(step1x, step2x), step3x);
            }
            if (xtr != null) {
                assert getMathTransform2D(xtr, mtFactory, sourceGG.gridDimensionX,
                                          sourceGG.gridDimensionY).equals(transform) : xtr;
                Envelope envelope = GCSUtilities.toEnvelope(sourceGG.getGridRange());
                envelope = CRSUtilities.transform(xtr.inverse(), envelope);
                targetGG = new GridGeometry2D(GCSUtilities.toGridRange(envelope), step1x);
            } else {
                assert transform.isIdentity() : transform;
                targetGG = sourceGG;
            }
        }
        /*
         * If the target coverage has not been created yet, change the image bounding box in
         * order to matches the grid range. If the target coverage has already been created,
         * just make sure that the image bounding box will stay constant even when we will
         * change the "Null" operation into a "Warp" one later.
         */
        if (true) {
            final GridRange gridRange = (targetCoverage != null ? targetCoverage.getGridGeometry()
                                                                : targetGG).getGridRange();
            int layoutMask = 0;
            ImageLayout layout;
            if (hints != null) {
                layout = (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT);
                if (layout != null) {
                    layoutMask = layout.getValidMask();
                }
            }
            layout = (ImageLayout)targetImage.getRenderingHint(JAI.KEY_IMAGE_LAYOUT);
            if (layout != null) {
                layout = (ImageLayout) layout.clone();
            } else {
                layout = new ImageLayout();
            }
            if (0==(layoutMask & (ImageLayout.MIN_X_MASK |
                                  ImageLayout.MIN_Y_MASK |
                                  ImageLayout.WIDTH_MASK |
                                  ImageLayout.HEIGHT_MASK)))
            {
                layout.setMinX  (gridRange.getLower (0));
                layout.setMinY  (gridRange.getLower (1));
                layout.setWidth (gridRange.getLength(0));
                layout.setHeight(gridRange.getLength(1));
                targetImage.setRenderingHint(JAI.KEY_IMAGE_LAYOUT, layout);
            }
            if (0==(layoutMask & (ImageLayout.TILE_WIDTH_MASK |
                                  ImageLayout.TILE_HEIGHT_MASK |
                                  ImageLayout.TILE_GRID_X_OFFSET_MASK |
                                  ImageLayout.TILE_GRID_Y_OFFSET_MASK)))
            {
                layout.setTileGridXOffset(layout.getMinX(targetImage));
                layout.setTileGridYOffset(layout.getMinY(targetImage));
                final int width  = layout.getWidth (targetImage);
                final int height = layout.getHeight(targetImage);
                if (layout.getTileWidth (targetImage) > width ) layout.setTileWidth (width);
                if (layout.getTileHeight(targetImage) > height) layout.setTileHeight(height);
                targetImage.setRenderingHint(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        /*
         * If the user request a new grid geometry with the same coordinate system, and if
         * the grid geometry is equivalents to a simple extraction of a sub-area, delegate
         * the work to a "Crop" operation.
         */
        if (transform.isIdentity()) {
            final GridRange sourceGR = sourceGG.getGridRange();
            final GridRange targetGR = targetGG.getGridRange();
            final int xmin = targetGR.getLower(0);
            final int xmax = targetGR.getUpper(0);
            final int ymin = targetGR.getLower(1);
            final int ymax = targetGR.getUpper(1);
            if (xmin >= sourceGR.getLower(0) &&
                xmax <= sourceGR.getUpper(0) &&
                ymin >= sourceGR.getLower(1) &&
                ymax <= sourceGR.getUpper(1))
            {
                paramBlk.add((float) (xmin));
                paramBlk.add((float) (ymin));
                paramBlk.add((float) (xmax-xmin));
                paramBlk.add((float) (ymax-ymin));
                targetImage.setParameterBlock(paramBlk);
                targetImage.setOperationName("Crop");
            }
        }
        /*
         * Get the sample value to use for background. We will try to fetch this value from one
         * of "no data" categories. For geophysics image, it is usually NaN. For non-geophysics
         * image, it is usually 0.
         */
        final GridSampleDimension[] sampleDimensions = sourceCoverage.getSampleDimensions();
        final double[] background = new double[sampleDimensions.length];
        for (int i=0; i<background.length; i++) {
            final NumberRange range = sampleDimensions[i].getBackground().getRange();
            final double min = range.getMinimum();
            final double max = range.getMaximum();
            if (range.isMinIncluded()) {
                background[i] = min;
            } else if (range.isMaxIncluded()) {
                background[i] = max;
            } else {
                background[i] = 0.5*(min+max);
            }
        }
        /*
         * Special case for the affine transform. Try to use the JAI "Affine" operation instead of
         * the more general "Warp" one. JAI provides native acceleration for the affine operation.
         * NOTE: "Affine", "Scale", "Translate", "Rotate" and similar operations ignore the 'xmin',
         * 'ymin', 'width' and 'height' image layout. Consequently, we can't use this operation if
         * the user provided explicitely a grid geometry. We use it only for automatically generated
         * geometry (the 'if (targetGG==null)' case above), in which case the target coverage may
         * need to be replaced.
         *
         * Note: a yet cheaper approach is to just update the 'gridToCoordinateSystem' value. We
         *       are allowed to do that since it is an automatically computed one. Then, returns
         *       a grid coverage wrapping the SOURCE image with the updated grid geometry.
         */
        if (targetImage.getOperationName().equalsIgnoreCase("Null")) {
            if (transform instanceof AffineTransform) {
                if (targetCoverage != null) {
                    // Cheapest approach: just update 'gridToCoordinateSystem'.
                    MathTransform mtr = targetGG.getGridToCoordinateSystem();
                    mtr = mtFactory.createConcatenatedTransform(transform.inverse(), mtr);
                    targetGG = new GridGeometry2D(sourceGG.getGridRange(), mtr);
                    return new Resampler2D(sourceCoverage, sourceImage, targetCRS,
                                           targetGG.getGridToCoordinateSystem());
                }
                // More general approach: apply the affine transform.
                final AffineTransform affine = (AffineTransform) transform.inverse();
                paramBlk.add(affine).add(interpolation).add(background);
                targetImage.setParameterBlock(paramBlk);
                targetImage.setOperationName("Affine");
                final Rectangle targetBounds = targetGG.getGridRange2D();
                if (!targetBounds.equals(targetImage.getBounds())) {
                    if (targetCoverage == null) {
                        // "Affine" computed its own image bounds, which doesn't
                        // matches the requested bounds. Unroll the operation...
                        paramBlk.removeParameters();
                        targetImage.setOperationName("Null");
                        targetImage.setParameterBlock(paramBlk);
                    } else {
                        // ...or update the GridCoverage if we are allowed to do so.
                        targetCoverage = new Resampler2D(sourceCoverage, targetImage, targetCRS, 
                                                         targetCoverage.getEnvelope());
                        targetGG = (GridGeometry2D) targetCoverage.getGridGeometry();
                    }
                }
            }
        }
        if (targetImage.getOperationName().equalsIgnoreCase("Null")) {
            /*
             * General case: construct the warp transform.  We had to set the warp transform
             * last because the construction of 'WarpTransform' requires the geometry of the
             * target grid coverage. The trick was to initialize the target image with a null
             * operation, and change the operation here.
             */
            paramBlk.add(WarpTransform2D.getWarp(sourceCoverage.getName(), (MathTransform2D)transform));
            paramBlk.add(interpolation).add(background);
            targetImage.setParameterBlock(paramBlk); // Must be invoked before setOperationName
            targetImage.setOperationName("Warp");
        }
        /*
         * The "Warp" operation with JAI 1.1.2-rc sometime returns an image with a bounding
         * box different from what we expected. It was not the case with JAI 1.1.1 (is it a
         * regression?).  As a safety, we check the bounding box in any case. If it doesn't
         * matches, then we will reconstruct the target coverage with a new grid geometry.
         */
        if (targetGG != null) {
            final GridRange targetGR = targetGG.getGridRange();
            final int[] lower = targetGR.getLowers();
            final int[] upper = targetGR.getUppers();
            lower[0] = targetImage.getMinX();
            lower[1] = targetImage.getMinY();
            upper[0] = targetImage.getWidth()  + lower[0];
            upper[1] = targetImage.getHeight() + lower[1];
            final GridRange actualGR = new GeneralGridRange(lower, upper);
            if (!targetGR.equals(actualGR)) {
                MathTransform gridToCoordinateSystem = targetGG.getGridToCoordinateSystem();
                if (false) {
                    // With JAI 1.1.2-rc, the bounding box seems wrong but the transform seems
                    // right. Set to 'true' if the transform need an adjustement as well.
                    gridToCoordinateSystem =
                          mtFactory.createConcatenatedTransform(
                          mtFactory.createAffineTransform(
                             new GeneralMatrix(GCSUtilities.toEnvelope(actualGR),
                                               GCSUtilities.toEnvelope(targetGR))),
                                               gridToCoordinateSystem);
                }
                targetGG = new GridGeometry2D(actualGR, gridToCoordinateSystem);
                targetCoverage = null;
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
        if (targetCoverage == null) {
            targetCoverage = new Resampler2D(sourceCoverage, targetImage, targetCRS,
                                             targetGG.getGridToCoordinateSystem());
        }
        if (targetGeophysics != null) {
            targetCoverage = targetCoverage.geophysics(targetGeophysics.booleanValue());
        }
        assert CRSUtilities.equalsIgnoreMetadata(targetCoverage.getCoordinateReferenceSystem(),
                            targetCRS!=null ? targetCRS : sourceCRS) : targetCoverage;
        assert targetGG!=null || targetImage.getBounds().equals(sourceImage.getBounds());
        assert ((GridGeometry2D) targetCoverage.getGridGeometry()).getGridRange2D()
                             .equals(targetImage.getBounds()) : targetGG;

        if (AbstractGridCoverageProcessor.LOGGER.isLoggable(Level.FINE)) {
            final Locale locale = sourceCoverage.getLocale();
            log(Resources.getResources(locale).getLogRecord(Level.FINE,
                ResourceKeys.APPLIED_RESAMPLE_$11, new Object[] {
                /*  {0} */ sourceCoverage.getName().toString(locale),
                /*  {1} */ sourceCoverage.getCoordinateReferenceSystem().getName().getCode(),
                /*  {2} */ new Integer(sourceImage.getWidth()),
                /*  {3} */ new Integer(sourceImage.getHeight()),
                /*  {4} */ targetCoverage.getCoordinateReferenceSystem().getName().getCode(),
                /*  {5} */ new Integer(targetImage.getWidth()),
                /*  {6} */ new Integer(targetImage.getHeight()),
                /*  {7} */ targetImage.getOperationName(),
                /*  {8} */ new Integer(sourceCoverage == sourceCoverage.geophysics(true) ? 1 : 0),
                /*  {9} */ ImageUtilities.getInterpolationName(interpolation),
                /* {10} */ background.length==1 ? (Double.isNaN(background[0]) ? (Object) "NaN" :
                                                  (Object) new Double(background[0])) :
                                                  (Object) XArray.toString(background, locale)}));
        }
        return targetCoverage;
    }

    /**
     * Returns the math transform for the two specified dimensions of the specified transform.
     *
     * @param  transform The transform.
     * @param  mtFactory The factory to use for extracting the sub-transform.
     * @return The {@link MathTransform2D} part of <code>transform</code>.
     * @throws FactoryException if <code>transform</code> is not separable.
     */
    private static MathTransform2D getMathTransform2D(final MathTransform        transform,
                                                      final MathTransformFactory mtFactory,
                                                      final int xAxis, final int yAxis)
            throws FactoryException
    {
        final DimensionFilter filter = new DimensionFilter(mtFactory);
        filter.addSourceDimension(xAxis);
        filter.addSourceDimension(yAxis);
        MathTransform candidate = filter.separate(transform);
        if (candidate instanceof MathTransform2D) {
            return (MathTransform2D) candidate;
        }
        filter.addTargetDimension(xAxis);
        filter.addTargetDimension(yAxis);
        candidate = filter.separate(transform);
        if (candidate instanceof MathTransform2D) {
            return (MathTransform2D) candidate;
        }
        throw new FactoryException(Resources.format(ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }

    /**
     * Checks if two geometries are equal, ignoring unspecified fields. If one or both geometries
     * has no "gridToCoordinateSystem" transform, then this properties is not taken in account.
     * Same apply for the grid range.
     * 
     *
     * @param  range1 The first range.
     * @param  range2 The second range.
     * @return <code>true</code> if the two geometries are equal, ignoring unspecified fields.
     */
    private static boolean equivalent(final GridGeometry2D geom1, final GridGeometry2D geom2) {
        if (geom1.equals(geom2)) {
            return true;
        }
        try {
            if (!geom1.getGridRange().equals(geom2.getGridRange())) {
                return false;
            }
        } catch (InvalidGridGeometryException exception) {
            // One geometry doesn't have a grid range.
            // Do not compare this properties.
        }
        try {
            if (!geom1.getGridToCoordinateSystem().equals(geom2.getGridToCoordinateSystem())) {
                return false;
            }
        } catch (InvalidGridGeometryException exception) {
            // One geometry doesn't have a transform.
            // Do not compare this properties.
        }
        return true;
    }

    /**
     * Log a message.
     */
    private static void log(final LogRecord record) {
        record.setSourceClassName("GridCoverageProcessor");
        record.setSourceMethodName("doOperation(\"Resample\")");
        AbstractGridCoverageProcessor.LOGGER.log(record);
    }
    
    
    
    
    /**
     * The "Resample" operation.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static final class Operation extends Operation2D {
        /**
         * The parameter descriptor for the interpolation type.
         */
        public static final ParameterDescriptor INTERPOLATION_TYPE =
                new DefaultParameterDescriptor(
                "InterpolationType", Object.class, null, "NearestNeighbor");

        /**
         * The parameter descriptor for the coordinate reference system.
         */
        public static final ParameterDescriptor COORDINATE_REFERENCE_SYSTEM =
                new DefaultParameterDescriptor(
                "CoordinateReferenceSystem", CoordinateReferenceSystem.class, null, null);

        /**
         * The parameter descriptor for the grid geometry.
         */
        public static final ParameterDescriptor GRID_GEOMETRY =
                new DefaultParameterDescriptor(
                "GridGeometry", GridGeometry2D.class, null, null);

        /**
         * Construct a "Resample" operation.
         */
        public Operation() {
            super(new DefaultParameterDescriptorGroup("Resample",
                  new ParameterDescriptor[] {SOURCE_0, INTERPOLATION_TYPE,
                          COORDINATE_REFERENCE_SYSTEM, GRID_GEOMETRY}));
        }
        
        /**
         * Resample a grid coverage. This method is invoked by
         * {@link GridCoverageProcessor} for the "Resample" operation.
         */
        protected GridCoverage2D doOperation(final ParameterValueGroup parameters,
                                             final RenderingHints hints)
        {
            GridCoverage2D         source = (GridCoverage2D)               parameters.parameter("Source")                   .getValue();
            Interpolation          interp = ImageUtilities.toInterpolation(parameters.parameter("InterpolationType")        .getValue());
            CoordinateReferenceSystem crs = (CoordinateReferenceSystem)    parameters.parameter("CoordinateReferenceSystem").getValue();
            GridGeometry2D       gridGeom = (GridGeometry2D)               parameters.parameter("GridGeometry")             .getValue();
            GridCoverage2D       coverage; // The result to be computed below.
            if (crs == null) {
                crs = source.getCoordinateReferenceSystem();
            }
            try {
                coverage = reproject(source, crs, gridGeom, interp,
                    (hints instanceof Hints) ? (Hints) hints : new Hints(hints));
            } catch (FactoryException exception) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_CANT_REPROJECT_$1, source.getName()), exception);
            } catch (TransformException exception) {
                throw new CannotReprojectException(Resources.format(
                        ResourceKeys.ERROR_CANT_REPROJECT_$1, source.getName()), exception);
            }
            /*
             * Check if we have been able to respect the user request. We may have failed to
             * respect the user specified grid geometry because JAI may not have honored the
             * 'minX', 'minY', 'width' and 'height' image layout setting  (see documentation
             * for AffineDescriptor).  Of course, we may have failed because of a bug in our
             * implementation as well.
             */
            if (gridGeom != null) {
                boolean mismatche = false;
                final GridGeometry2D actualGeom = (GridGeometry2D) coverage.getGridGeometry();
                if (GCSUtilities.hasGridRange(gridGeom)) {
                    mismatche |= !gridGeom.getGridRange().equals(
                                actualGeom.getGridRange());
                }
                if (GCSUtilities.hasTransform(gridGeom)) {
                    mismatche |= !gridGeom.getGridToCoordinateSystem().equals(
                                actualGeom.getGridToCoordinateSystem());
                }
                if (mismatche) {
                    final Locale locale = source.getLocale();
                    log(Resources.getResources(locale).getLogRecord(Level.WARNING,
                        ResourceKeys.WARNING_ADJUSTED_GRID_GEOMETRY_$1,
                            coverage.getName().toString(locale)));
                }
            }
            return coverage;
        }
    }
}
