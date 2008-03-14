/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.resources.coverage;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.PropertySource;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.MismatchedDimensionException;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.RenderedCoverage;
import org.geotools.coverage.grid.ViewType;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.util.NumberRange;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Simone Giannecchini
 */
public final class CoverageUtilities {
    /**
     * Do not allows instantiation of this class.
     */
    private CoverageUtilities() {
    }

    /**
     * Returns a two-dimensional CRS for the given coverage. This method performs a
     * <cite>best effort</cite>; the returned CRS is not garanteed to be the most
     * appropriate one.
     *
     * @param  coverage The coverage for which to obtains a two-dimensional CRS.
     * @return The two-dimensional CRS.
     * @throws TransformException if the CRS can't be reduced to two dimensions.
     */
    public static CoordinateReferenceSystem getCRS2D(final Coverage coverage)
            throws TransformException
    {
        if (coverage instanceof GridCoverage2D) {
            return ((GridCoverage2D) coverage).getCoordinateReferenceSystem2D();
        }
        if (coverage instanceof GridCoverage) {
            final GridGeometry2D geometry =
                    GridGeometry2D.wrap(((GridCoverage) coverage).getGridGeometry());
            if (geometry.isDefined(GridGeometry2D.CRS)) {
                return geometry.getCoordinateReferenceSystem2D();
            } else try {
                return geometry.reduce(coverage.getCoordinateReferenceSystem());
            } catch (FactoryException exception) {
                // Ignore; we will fallback on the code below.
            }
        }
        return CRSUtilities.getCRS2D(coverage.getCoordinateReferenceSystem());
    }

    /**
     * Returns a two-dimensional horizontal CRS for the given coverage. This method performs a
     * <cite>best effort</cite>; the returned CRS is not garanteed to succed.
     *
     * @param  coverage The coverage for which to obtains a two-dimensional horizontal CRS.
     * @return The two-dimensional horizontal CRS.
     * @throws TransformException if the CRS can't be reduced to two dimensions.
     */
    public static CoordinateReferenceSystem getHorizontalCRS(final Coverage coverage)
            throws TransformException
    {
    	CoordinateReferenceSystem returnedCRS=null;
        if (coverage instanceof GridCoverage2D) {
        	returnedCRS= ((GridCoverage2D) coverage).getCoordinateReferenceSystem2D();
        }
        if (coverage instanceof GridCoverage) {
            final GridGeometry2D geometry =
                    GridGeometry2D.wrap(((GridCoverage) coverage).getGridGeometry());
            if (geometry.isDefined(GridGeometry2D.CRS)) {
            	returnedCRS= geometry.getCoordinateReferenceSystem2D();
            } else try {
            	returnedCRS= geometry.reduce(coverage.getCoordinateReferenceSystem());
            } catch (FactoryException exception) {
                // Ignore; we will fallback on the code below.
            }
        }
        if(returnedCRS==null)
        	returnedCRS= CRS.getHorizontalCRS(coverage.getCoordinateReferenceSystem());
        if(returnedCRS==null)
                throw new TransformException(Errors.format(
                          ErrorKeys.CANT_REDUCE_TO_TWO_DIMENSIONS_$1,
                          returnedCRS));
        return returnedCRS;
    }

    /**
     * Returns a two-dimensional envelope for the given coverage. This method performs a
     * <cite>best effort</cite>; the returned envelope is not garanteed to be the most
     * appropriate one.
     *
     * @param  coverage The coverage for which to obtains a two-dimensional envelope.
     * @return The two-dimensional envelope.
     * @throws MismatchedDimensionException if the envelope can't be reduced to two dimensions.
     */
    public static Envelope2D getEnvelope2D(final Coverage coverage)
            throws MismatchedDimensionException
    {
        if (coverage instanceof GridCoverage2D) {
            return ((GridCoverage2D) coverage).getEnvelope2D();
        }
        if (coverage instanceof GridCoverage) {
            final GridGeometry2D geometry =
                    GridGeometry2D.wrap(((GridCoverage) coverage).getGridGeometry());
            if (geometry.isDefined(GridGeometry2D.ENVELOPE)) {
                return geometry.getEnvelope2D();
            } else {
                return geometry.reduce(coverage.getEnvelope());
            }
        }
        // Following may thrown MismatchedDimensionException.
        return new Envelope2D(coverage.getEnvelope());
    }

    /**
     * Retrieves a best guess for the sample value to use for background,
     * inspecting the categories of the provided {@link GridCoverage2D}.
     *
     * @param coverage to use for guessing background values.
     * @return an array of double values to use as a background.
     */
    public static double[] getBackgroundValues(GridCoverage2D coverage) {
        /*
         * Get the sample value to use for background. We will try to fetch this
         * value from one of "no data" categories. For geophysics images, it is
         * usually NaN. For non-geophysics images, it is usually 0.
         */
        final GridSampleDimension[] sampleDimensions = coverage.getSampleDimensions();
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
                background[i] = 0.5 * (min + max);
            }
        }
        return background;
    }

    /**
     * Returns {@code true} if the provided {@link GridCoverage}
     * has {@link Category} objects with a real transformation.
     * <p>
     * Common use case for this method is understanding if a {@link GridCoverage} has an
     * accompanying Geophysics or non-Geophysics view, which means a dicotomy between the
     * coverage with the "real" data and the coverage with the rendered version of the original
     * data exists. An example is when you have raw data whose data type is float and you want
     * to render them using a palette. You usually do this by specifying a set of {@link Category}
     * object which will map some intervals of the raw data to some specific colors. The rendered
     * version that we will create using the method {@link GridCoverage2D#geophysics(false)} will
     * be backed by a RenderedImage with an IndexColorModel representing the colors provided in
     * the Categories.
     *
     * @param gridCoverage
     *            to check for the existence of categories with tranformations
     *            between original data and their rendered counterpart.
     * @return {@code false} if this coverage has only a single view associated with it,
     *         {@code true} otherwise.
     */
    public static boolean hasRenderingCategories(final GridCoverage gridCoverage) {
        // getting all the SampleDimensions of this coverage, if any exist
        final int numSampleDimensions = gridCoverage.getNumSampleDimensions();
        if (numSampleDimensions == 0) {
            return false;
        }
        final SampleDimension[] sampleDimensions = new SampleDimension[numSampleDimensions];
        for (int i=0; i<numSampleDimensions; i++) {
            sampleDimensions[i] = gridCoverage.getSampleDimension(i);
        }
        // do they have any transformation that is not the identity?
        return hasTransform(sampleDimensions);
    }

    /**
     * Returns {@code true} if at least one of the specified sample dimensions has a
     * {@linkplain SampleDimension#getSampleToGeophysics sample to geophysics} transform
     * which is not the identity transform.
     */
    public static boolean hasTransform(final SampleDimension[] sampleDimensions) {
        for (int i=sampleDimensions.length; --i>=0;) {
            SampleDimension sd = sampleDimensions[i];
            if (sd instanceof GridSampleDimension) {
                sd = ((GridSampleDimension) sd).geophysics(false);
            }
            MathTransform1D tr = sd.getSampleToGeophysics();
            if (tr!=null && !tr.isIdentity()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if the specified grid coverage or any of its source
     * uses the following image.
     */
    public static boolean uses(final GridCoverage coverage, final RenderedImage image) {
        if (coverage != null) {
            if (coverage instanceof RenderedCoverage) {
                if (((RenderedCoverage) coverage).getRenderedImage() == image) {
                    return true;
                }
            }
            final Collection<GridCoverage> sources = coverage.getSources();
            if (sources != null) {
                for (final GridCoverage source : sources) {
                    if (uses(source, image)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the visible band in the specified {@link RenderedImage} or {@link PropertySource}.
     * This method fetch the {@code "GC_VisibleBand"} property. If this property is undefined,
     * then the visible band default to the first one.
     *
     * @param  image The image for which to fetch the visible band, or {@code null}.
     * @return The visible band.
     */
    public static int getVisibleBand(final Object image) {
        Object candidate = null;
        if (image instanceof RenderedImage) {
            candidate = ((RenderedImage) image).getProperty("GC_VisibleBand");
        } else if (image instanceof PropertySource) {
            candidate = ((PropertySource) image).getProperty("GC_VisibleBand");
        }
        if (candidate instanceof Integer) {
            return ((Integer) candidate).intValue();
        }
        return 0;
    }

    /**
     * @deprecated Use {@link #preferredViewForOperation} instead.
     *
     * @return 0 if nothing has to be done on the provided coverage, 1 if a color expansion has to
     *         be provided, 2 if we need to employ the geophysics view of the provided coverage,
     *         3 if we suggest to employ the non-geophysics view of the provided coverage.
     *
     * @since 2.3.1
     */
    @Deprecated
    public static int prepareSourcesForGCOperation(final GridCoverage2D coverage,
            final Interpolation interpolation, final boolean hasFilter, final RenderingHints hints)
    {
        final ViewType type = preferredViewForOperation(coverage, interpolation, hasFilter, hints);
        switch (type) {
            case SAME:         return 0;
            case PHOTOGRAPHIC: return 1;
            case GEOPHYSICS:   return 2;
            case RENDERED:
            case PACKED:
            case NATIVE:       return 3;
            default: throw new AssertionError(type);
        }
    }

    /**
     * General purpose method used in various operations for {@link GridCoverage2D} to help
     * with taking decisions on how to treat coverages with respect to their {@link ColorModel}.
     * <p>
     * The need for this method arose in consideration of the fact that applying most operations
     * on coverage whose {@link ColorModel} is an instance of {@link IndexColorModel} may lead to
     * unpredictable results depending on the applied {@link Interpolation} (think about applying
     * "Scale" with {@link InterpolationBilinear} on a non-geophysics {@link GridCoverage2D} with an
     * {@link IndexColorModel}) or more simply on the operation itself ("SubsampleAverage" cannot
     * be applied at all on a {@link GridCoverage2D} backed by an {@link IndexColorModel}).
     * <p>
     * This method suggests the actions to take depending on the structure of the provided
     * {@link GridCoverage2D}, the provided {@link Interpolation} and if the operation uses
     * a filter or not (this is useful for operations like SubsampleAverage or FilteredSubsample).
     * <p>
     * In general the idea is as follows: If the original coverage is backed by a
     * {@link RenderedImage} with an {@link IndexColorModel}, we have the following cases:
     * <p>
     * <ul>
     *  <li>if the interpolation is {@link InterpolationNearest} and there is no filter involved
     *      we can apply the operation on the {@link IndexColorModel}-backed coverage with nor
     *      problems.</li>
     *  <li>If the interpolations in of higher order or there is a filter to apply we have to
     *      options:
     *      <ul>
     *        <li>If the coverage has a twin geophysics view we need to go back to it and apply
     *            the operation there.</li>
     *        <li>If the coverage has no geophysics view (an orthophoto with an intrisic
     *            {@link IndexColorModel} view) we need to perform an RGB(A) color expansion
     *            before applying the operation.</li>
     *      </ul>
     *  </li>
     * </ul>
     * <p>
     * A special case is when we want to apply an operation on the geophysics view of a coverage
     * that does not involve high order interpolation or filters. In this case we suggest to apply
     * the operation on the non-geophysics view, which is usually much faster. Users may ignore
     * this advice.
     *
     * @param coverage The coverage to check for the action to take.
     * @param interpolation The interpolation to use for the action to take, or {@code null} if none.
     * @param hasFilter {@code true} if the operation we will apply is going to use a filter.
     * @param hints The hints to use when applying a certain operation.
     * @return {@link ViewType#SAME} if nothing has to be done on the provided coverage,
     *         {@link ViewType.PHOTOGRAPHIC} if a color expansion has to be provided,
     *         {@link ViewType#GEOPHYSICS} if we need to employ the geophysics view of
     *         the provided coverage,
     *         {@link ViewType#NATIVE} if we suggest to employ the native (usually packed) view
     *         of the provided coverage.
     *
     * @since 2.5
     *
     * @todo Move this method in {@link org.geotools.coverage.processing.Operation2D}.
     */
    public static ViewType preferredViewForOperation(final GridCoverage2D coverage,
            final Interpolation interpolation, final boolean hasFilter, final RenderingHints hints)
    {
        /*
         * Checks if the user specified explicitly the view he wants to use for performing
         * the calculations.
         */
        if (hints != null) {
            final Object candidate = hints.get(Hints.COVERAGE_PROCESSING_VIEW);
            if (candidate instanceof ViewType) {
                return (ViewType) candidate;
            }
        }
        /*
         * Tries to infer automatically the view to use.  If there is no sample dimension with
         * a "sample to geophysics" transform, then we assume that the image has no geophysics
         * meaning and would better be handled as photographic.
         */
        final RenderedImage sourceImage = coverage.getRenderedImage();
        if (sourceImage.getColorModel() instanceof IndexColorModel) {
            if (!hasRenderingCategories(coverage)) {
                return ViewType.PHOTOGRAPHIC;
            }
            // The old way to request explicitly a color expansion.
            if (hints != null && Boolean.FALSE.equals(hints.get(Hints.REPLACE_NON_GEOPHYSICS_VIEW))) {
                return ViewType.PHOTOGRAPHIC;
            }
            /*
             * If there is no filter and no interpolation, then we don't need to operate on
             * geophysics value. The packed view is usually faster. We could returns either
             * NATIVE, PACKED or SAME, which are equivalent in many cases:
             *
             *  - SAME is likely equivalent to PACKED because we checked that the color model is indexed.
             *  - NATIVE is likely equivalent to PACKED because data in NetCDF or HDF files are often packed.
             *
             * However those views differ in their behavior when the native data are geophysics
             * rather than packed (e.g. a NetCDF file with floating point values). In this case,
             * NATIVE is equivalent to GEOPHYSICS. The tradeoff of each views are:
             *
             *  - NATIVE is more accurate but slower when native data are geophysics
             *    (but as fast as other views when native data are packed).
             *
             *  - SAME is "as the user said" on the assumption that if he asked an operation on
             *    a packed view of a coverage rather than the geophysics view, he know what he
             *    is doing.
             */
            if (!hasFilter && (interpolation == null || interpolation instanceof InterpolationNearest)) {
                if (hints != null) {
                    final Object rendering = hints.get(RenderingHints.KEY_RENDERING);
                    if (RenderingHints.VALUE_RENDER_QUALITY.equals(rendering)) {
                        return ViewType.NATIVE;
                    }
                    if (RenderingHints.VALUE_RENDER_SPEED.equals(rendering)) {
                        return ViewType.SAME;
                    }
                }
                return ViewType.SAME; // Default value.
            }
            // In this case we need to go back the geophysics view of the source coverage.
            return ViewType.GEOPHYSICS;
        }
        /*
         * The operations are usually applied on floating-point values, in order
         * to gets maximal precision and to handle correctly the special case of
         * NaN values. However, we can apply some operation on integer values if
         * the interpolation type is "nearest neighbor", since this is not
         * really an interpolation.
         *
         * If this condition is met, then we verify if an "integer version" of
         * the image is available as a source of the source coverage (i.e. the
         * floating-point image is derived from the integer image, not the
         * converse).
         */
        if (!hasFilter && (interpolation == null || interpolation instanceof InterpolationNearest)) {
            final GridCoverage2D candidate = coverage.view(ViewType.NATIVE);
            if (candidate != coverage) {
                final List<RenderedImage> sources = coverage.getRenderedImage().getSources();
                if (sources != null && sources.contains(candidate.getRenderedImage())) {
                    return ViewType.NATIVE;
                }
            }
        }
        return ViewType.SAME;
    }

    /**
     * The preferred view in which to returns the coverage after the operation.
     * This method returns a view that match the current state of the given coverage.
     *
     * @param  coverage The source coverage <strong>before</strong> the operation.
     * @return The suggested view, or {@link ViewType#SAME} if this method doesn't
     *         have any suggestion.
     *
     * @since 2.5
     *
     * @todo Move this method in {@link org.geotools.coverage.processing.Operation2D}.
     */
    public static ViewType preferredViewAfterOperation(final GridCoverage2D coverage) {
        final Set<ViewType> views = coverage.getViewTypes();
        // Most restrictive views first, less restrictive last.
        if (views.contains(ViewType.GEOPHYSICS)) {
            return ViewType.GEOPHYSICS;
        }
        if (views.contains(ViewType.RENDERED)) {
            return ViewType.RENDERED;
        }
        if (views.contains(ViewType.PACKED)) {
            return ViewType.PACKED;
        }
        if (views.contains(ViewType.PHOTOGRAPHIC)) {
            return ViewType.PHOTOGRAPHIC;
        }
        return ViewType.SAME;
    }

    /**
     * Checks if the transformation is a pure scale/translate instance (using the
     * provided tolerance factor)
     *
     * @param transform The {@link MathTransform} to check.
     * @param EPS The tolerance factor.
     * @return {@code true} if the provided transformation is a simple scale and translate,
     *         {@code false} otherwise.
     */
    public static boolean isScaleTranslate(final MathTransform transform, final double EPS) {
        if (!(transform instanceof AffineTransform)) {
            return false;
        }
        final AffineTransform at = (AffineTransform) transform;
        final double scale = Math.abs(XAffineTransform.getRotation(at));
        return scale < EPS; // This is enough for returning 'false' if 'scale' is NaN.
//      return Math.abs(at.getShearX()) < EPS && Math.abs(at.getShearY()) < EPS;
    }
    
    /**
     * This method is responsible for computing the resolutions for the provided grid 2 world
     * transformation
     *
     * <P>
     * It is worth to note that the returned resolution array is of length of 2.
     * 
     * @param raster2Model
     *            The grid to world transformation
     */
    public final static double[] getResolution(AffineTransform raster2model){
        double[] requestedRes = null;
        if ((raster2model != null)) {
            requestedRes = new double[2];
            requestedRes[0] = XAffineTransform.getScaleX0(raster2model);
            requestedRes[1] = XAffineTransform.getScaleY0(raster2model);
        }
        return requestedRes;
    }
}
