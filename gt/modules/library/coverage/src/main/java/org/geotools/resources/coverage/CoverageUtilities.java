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

// J2SE and JAI dependencies
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
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

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.RenderedCoverage;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.resources.CRSUtilities;
import org.geotools.util.NumberRange;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * A set of utilities methods for the Grid Coverage package. Those methods are not really
 * rigorous; must of them should be seen as temporary implementations.
 *
 * @since 2.2
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/resources/image/CoverageUtilities.java $
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Simone Giannecchini
 * -deprecated moved to org.geotools.resources.coverage package.-
 */
public final class CoverageUtilities {
	/** Controlling datum shift process. */
	public final static Hints LENIENT_HINT = new Hints(
			Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
    /**
     * Do not allows instantiation of this class.
     */
    private CoverageUtilities() {
    }
	/**
	 * Retrieves a best guess for the sample value to use for background,
	 * inspecting the categories of the provided {@link GridCoverage2D}.
	 * 
	 * @param coverage
	 *            to use for guessing background values.
	 * @return an array of double values to use as a background.
	 */
	public  static double[] getBackgroundValues(GridCoverage2D coverage) {
		/*
		 * Get the sample value to use for background. We will try to fetch this
		 * value from one of "no data" categories. For geophysics images, it is
		 * usually NaN. For non-geophysics images, it is usually 0.
		 */
		final GridSampleDimension[] sampleDimensions = coverage
				.getSampleDimensions();
		final double[] background = new double[sampleDimensions.length];
		final int length = background.length;
		NumberRange range;
		double max;
		double min;
		for (int i = 0; i < length; i++) {
			range = sampleDimensions[i].getBackground().getRange();
			min = range.getMinimum();
			max = range.getMaximum();
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
	 * "Scale" with {@link InterpolationBilinear} on a {@link GridCoverage2D} with an
	 * {@link IndexColorModel}) or more simply on the operation itself ("SubsampleAverage" cannot
	 * be applied at all on a {@link GridCoverage2D} backed by an {@link IndexColorModel}).
	 *
	 *
	 * <p>
	 * This method suggests the actions to take depending on the structure of the provided
	 * {@link GridCoverage2D}, the provided {@link Interpolation} and if the operation uses
	 * a filter or not (this is useful for operations like SubsampleAverage or FilteredSubsample).
	 *
	 *
	 * <p>
	 * In general the idea ia as follows: If the original coverage is backed by a {@link RenderedImage}
	 * with an {@link IndexColorModel}, we have the following cases:
	 *
	 * <1>if the interpolation is {@link InterpolationNearest} and there is no filter involved we can
	 * apply the operation on the {@link IndexColorModel}-backed coverage with nor probs.
	 * <2>If the interpolations in of higeher order or there is a filter to apply we have to options:
	 * 	<a>If the coverage has a twin geophysics vew we need to go back to it and apply the operation
	 *     there.
	 *  <b>If the coverage has no geophysics view (an orthophoto with an intrisic {@link IndexColorModel}
	 *     view) we need to perform an RGB(A) color expansion before applying the operation.
	 *
	 *
	 *<p>
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
	 * 		   provided, 2 if we need to employ the geophysics vew of the provided coverage,
	 * 		   3 if we suggest to  employ the non-geophysics vew of the provided coverage
	 * @since 2.3.1
	 */
	public static int prepareSourcesForGCOperation(GridCoverage2D coverage,
			Interpolation interpolation, boolean hasFilter, RenderingHints hints) {
		RenderedImage sourceImage = coverage.getRenderedImage();
		boolean useNonGeoView = false;
		if (hints != null
				&& hints.containsKey(Hints.REPLACE_NON_GEOPHYSICS_VIEW))
			useNonGeoView = !((Boolean) hints
					.get(Hints.REPLACE_NON_GEOPHYSICS_VIEW)).booleanValue();
		// the color model is indexed?
		final boolean isIndexColorModel = sourceImage.getColorModel() instanceof IndexColorModel;
		if (!isIndexColorModel)
			return 0;// optimization
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
		// Note that if the Hint REPLACE_NON_GEOPHYSICS_VIEW is set to false
		// /////////////////////////////////////////////////////////////////////
		List sources;
		GridCoverage2D candidate;
		if (isNearestNeigborInterpolation && !hasFilter) {
			candidate = coverage.geophysics(false);
			if (candidate != coverage) {
				sources = coverage.getRenderedImage().getSources();
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
		if (preprocessIndexed)
			if (!getGeophysics) {
				return 1;
			} else if (getGeophysics) {
				// in this case we need to go back the geophysics view of the
				// source coverage
				return 2;

			}
		return 0;
	}
	/**
	 * This method is responsible for telling me if the provided
	 * {@link GridCoverage} has {@link Category} objects twith a real
	 * transformation.
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
	 *
	 *
	 *
	 * @param gridCoverage
	 *            to check for the existence of categories with tranformations
	 *            between original data and their rendered counterpart.
	 * @return false if this coverage has only a single view associated with it,
	 *         true otherwise.
	 */
	public static boolean hasRenderingCategories(final GridCoverage gridCoverage) {
		boolean retVal = false;
		// getting all the SampleDimensions of this coverage, if fany exist
		final int numSampleDimensions = gridCoverage.getNumSampleDimensions();
		if (numSampleDimensions > 0) {
			final SampleDimension[] sampleDimensions = new SampleDimension[numSampleDimensions];
			for (int i = 0; i < numSampleDimensions; i++)
				sampleDimensions[i] = gridCoverage.getSampleDimension(i);

			// do they have any transformation that is not the identity?
			return CoverageUtilities.hasTransform(sampleDimensions);
		}
		return retVal;
	}
	/**
	 * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
	 * (temporary).
	 * 
	 * @param gridCoverage
	 *            the grid coverage
	 * 
	 * @return a feature with the grid coverage envelope as the geometry and the
	 *         grid coverage itself in the "grid" attribute
	 * @throws TransformException
	 * @throws SchemaException
	 * @throws FactoryConfigurationError
	 * @throws IllegalAttributeException
	 */
	public static FeatureCollection wrapGc(GridCoverage gridCoverage)
			throws TransformException, FactoryConfigurationError,
			SchemaException, IllegalAttributeException {

		// create surrounding polygon
		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Rectangle2D rect = ((GridCoverage2D) gridCoverage)
				.getEnvelope2D();
		final CoordinateReferenceSystem

		sourceCrs = CRSUtilities.getCRS2D(((GridCoverage2D) gridCoverage)
				.getCoordinateReferenceSystem());
		final Coordinate[] coord = new Coordinate[5];
		// if (lonFirst) {
		coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
		coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
		coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
		coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
		coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());

	
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon bounds = new Polygon(ring, null, gf);

		// create the feature type
		final GeometricAttributeType geom = new GeometricAttributeType("geom",
				Polygon.class, true, 1, 1, null, sourceCrs, null);
		final AttributeType grid = AttributeTypeFactory.newAttributeType(
				"grid", GridCoverage.class);

		final AttributeType[] attTypes = { geom, grid };
		// Fix the schema name
		final String typeName = "GridCoverage";
		final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
				.newFeatureType(attTypes, typeName);

		// create the feature
		Feature feature = schema.create(new Object[] { bounds, gridCoverage });

		final FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(feature);

		return collection;
	}

	/**
	 * Wraps a grid coverage into a Feature. Code lifted from ArcGridDataSource
	 * (temporary).
	 * 
	 * @param gridCoverageReader
	 *            the grid coverage 
	 * 
	 * @return a feature with the grid coverage envelope as the geometry and the
	 *         grid coverage itself in the "grid" attribute
	 * @throws TransformException
	 * @throws SchemaException
	 * @throws FactoryConfigurationError
	 * @throws IllegalAttributeException
	 */
	public static FeatureCollection wrapGcReader(
			AbstractGridCoverage2DReader gridCoverageReader)
			throws TransformException, FactoryConfigurationError,
			SchemaException, IllegalAttributeException {

		// create surrounding polygon
		final PrecisionModel pm = new PrecisionModel();
		final GeometryFactory gf = new GeometryFactory(pm, 0);
		final Rectangle2D rect = gridCoverageReader.getOriginalEnvelope()
				.toRectangle2D();
		final CoordinateReferenceSystem sourceCrs = CRSUtilities
				.getCRS2D(gridCoverageReader.getCrs());

		// TODO hack to be removed
		// final boolean lonFirst = !GridGeometry2D.swapXY(sourceCrs
		// .getCoordinateSystem());
		final Coordinate[] coord = new Coordinate[5];
		// if (lonFirst) {
		coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
		coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
		coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
		coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
		coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
		// } else {
		// coord[0] = new Coordinate(rect.getMinY(), rect.getMinX());
		// coord[1] = new Coordinate(rect.getMaxY(), rect.getMinX());
		// coord[2] = new Coordinate(rect.getMaxY(), rect.getMaxX());
		// coord[3] = new Coordinate(rect.getMinY(), rect.getMaxX());
		// coord[4] = new Coordinate(rect.getMinY(), rect.getMinX());
		// }
		final LinearRing ring = gf.createLinearRing(coord);
		final Polygon bounds = new Polygon(ring, null, gf);

		// create the feature type
		final GeometricAttributeType geom = new GeometricAttributeType("geom",
				Polygon.class, true, 1, 1, null, sourceCrs, null);
		final AttributeType grid = AttributeTypeFactory.newAttributeType(
				"grid", AbstractGridCoverage2DReader.class);

		final AttributeType[] attTypes = { geom, grid };
		// Fix the schema name
		final String typeName = "GridCoverageReader";
		final DefaultFeatureType schema = (DefaultFeatureType) FeatureTypeBuilder
				.newFeatureType(attTypes, typeName);

		// create the feature
		Feature feature = schema.create(new Object[] { bounds,
				gridCoverageReader });

		final FeatureCollection collection = FeatureCollections.newCollection();
		collection.add(feature);

		return collection;
	}
}
