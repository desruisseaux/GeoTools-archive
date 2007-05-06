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
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.PropertySource;

import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.RenderedCoverage;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
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
public class CoverageUtilities {
    /**
     * Controlling datum shift process.
     *
     * @deprecated Will be deleted.
     */
    public final static Hints LENIENT_HINT = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

    /**
     * Do not allows instantiation of this class.
     *
     * @deprecated We will make this constructor private after we deleted
     *             the deprecated subclass, and make this class final.
     */
    protected CoverageUtilities() {
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
            return tr!=null && !tr.isIdentity();
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
            final Collection sources = coverage.getSources();
            if (sources != null) {
                for (final Iterator it=sources.iterator(); it.hasNext();) {
                    if (uses((GridCoverage) it.next(), image)) {
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
     * General purpose method used in various operations for {@link GridCoverage2D} to help
     * with taking decisions on how to treat coverages with respect to their {@link ColorModel}.
     *
     * <p>
     * The need for this method arose in consideration of the fact that applying most operations
     * on coverage whose {@link ColorModel} is an instance of {@link IndexColorModel} may lead to
     * unpredictable results depending on the applied {@link Interpolation} (think about applying
     * "Scale" with {@link InterpolationBilinear} on a non-geophysics {@link GridCoverage2D} with an
     * {@link IndexColorModel}) or more simply on the operation itself ("SubsampleAverage" cannot
     * be applied at all on a {@link GridCoverage2D} backed by an {@link IndexColorModel}).
     *
     * <p>
     * This method suggests the actions to take depending on the structure of the provided
     * {@link GridCoverage2D}, the provided {@link Interpolation} and if the operation uses
     * a filter or not (this is useful for operations like SubsampleAverage or FilteredSubsample).
     *
     * <p>
     * In general the idea is as follows: If the original coverage is backed by a
     * {@link RenderedImage} with an {@link IndexColorModel}, we have the following cases:
     *
     * <ul>
     *  <li>if the interpolation is {@link InterpolationNearest} and there is no filter involved
     *      we can apply the operation on the {@link IndexColorModel}-backed coverage with nor
     *      probs.</li>
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
     *
     * <p>
     * A special case is when we want to apply an operation on the geophysics view of a coverage that
     * does not involve high order interpolation of filters. In this case we suggest to apply the
     * operation on the non-geophysics view, which is usually much faster. Users may ignore this
     * advice.
     *
     * @param coverage to check for the action to take.
     * @param interpolation to use for the action to take.
     * @param hasFilter if the operation we will apply is going to use a filter.
     * @param hints to use when applying a certain operation.
     * @return 0 if nothing has to be done on the provided coverage, 1 if a color expansion has to be
     *         provided, 2 if we need to employ the geophysics vew of the provided coverage,
     *         3 if we suggest to  employ the non-geophysics vew of the provided coverage.
     *
     * @since 2.3.1
     *
     * @todo Consider refactoring this method into {@link org.geotools.coverage.grid.ViewType},
     *       or in some utility class related to it.
     */
    public static int prepareSourcesForGCOperation(final GridCoverage2D coverage,
            final Interpolation interpolation, final boolean hasFilter, final RenderingHints hints)
    {
        final RenderedImage sourceImage = coverage.getRenderedImage();
        boolean useNonGeoView = false;
        if (hints != null) {
            // REPLACE_NON_GEOPHYSICS_VIEW default value is 'true'.
            // useNonGeoView value is the opposite of REPLACE_NON_GEOPHYSICS_VIEW.
            useNonGeoView = Boolean.FALSE.equals(hints.get(Hints.REPLACE_NON_GEOPHYSICS_VIEW));
        }
        // the color model is indexed?
        final boolean isIndexColorModel = sourceImage.getColorModel() instanceof IndexColorModel;
        if (!isIndexColorModel) {
            return 0;// optimization
        }
        final boolean isNearestNeigborInterpolation = interpolation instanceof InterpolationNearest;
        // /////////////////////////////////////////////////////////////////////
        //
        // The projection are usually applied on floating-point values, in order
        // to gets maximal precision and to handle correctly the special case of
        // NaN values. However, we can apply the projection on integer values if
        // the interpolation type is "nearest neighbor", since this is not
        // really an interpolation.
        //
        // If this condition is met, then we verify if an "integer version" of
        // the image is available as a source of the source coverage (i.e. the
        // floating-point image is derived from the integer image, not the
        // converse).
        //
        // /////////////////////////////////////////////////////////////////////
        if (isNearestNeigborInterpolation && !hasFilter) {
            final GridCoverage2D candidate = coverage.geophysics(false);
            if (candidate != coverage) {
                final List sources = coverage.getRenderedImage().getSources();
                if (sources != null) {
                    if (sources.contains(candidate.getRenderedImage())) {
                        return 3;
                    }
                }
            }
        }

        // /////////////////////////////////////////////////////////////////////
        //
        // Do we need to explode the Palette to RGB(A)? This is needed only when
        // we have a coverage that has a geoophysiscs view which has itself
        // an IndexColorModel and we want to perform an operation that involves
        // an higher order interpolation or a filter (like with
        // SubsampleAverage).
        //
        // /////////////////////////////////////////////////////////////////////
        // do we have transforms?
        final boolean hasRenderingCategories = hasRenderingCategories(coverage);
        final boolean preprocessIndexed = isIndexColorModel
                && (!isNearestNeigborInterpolation || hasFilter);
        final boolean getGeophysics = !useNonGeoView
                && (hasRenderingCategories && preprocessIndexed);
        // this coverage is a real image with index color model, hence we need
        // to apply this operation on the expanded model.
        if (preprocessIndexed) {
            if (!getGeophysics) {
                return 1;
            } else if (getGeophysics) {
                // in this case we need to go back the geophysics view of the
                // source coverage
                return 2;
            }
        }
        return 0;
    }

    /**
     * Returns {@code true} if the provided {@link GridCoverage}
     * has {@link Category} objects twith a real transformation.
     *
     * <p>
     * Common use case for this method is understanding if a
     * {@link GridCoverage} has an accompanying Gephysiscs or non-Geophysics
     * view, which means a dicotomy between the coverage with the "real" data
     * and the coverage with the rendered version of the original data exists.
     * An example is when you have raw data whose data type is float and you
     * want to render them using a palette. You usually do this by specifying a
     * set of {@link Category} object which will map some intervals of the raw
     * data to some specific colors. The rendered version that we will create
     * using the method {@link GridCoverage2D#geophysics(false)} will be backed
     * by a RenderedImage with an IndexColorModel representing the colors
     * provided in the Categories.
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
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     *
     * @deprecated Moved to {@link FeatureUtilities#wrapGridCoverage}.
     */
    public static FeatureCollection wrapGc(final GridCoverage coverage)
            throws TransformException, SchemaException, IllegalAttributeException
    {
         return FeatureUtilities.wrapGridCoverage((GridCoverage2D) coverage);
    }

    /**
     * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
     * (temporary).
     * 
     * @param gridCoverageReader the grid coverage 
     * @return a feature with the grid coverage envelope as the geometry and the
     *         grid coverage itself in the "grid" attribute
     *
     * @deprecated Moved to {@link FeatureUtilities#wrapGridCoverageReader}.
     */
    public static FeatureCollection wrapGcReader(AbstractGridCoverage2DReader reader)
            throws TransformException, SchemaException, IllegalAttributeException
    {
        return FeatureUtilities.wrapGridCoverageReader(reader);
    }
}
