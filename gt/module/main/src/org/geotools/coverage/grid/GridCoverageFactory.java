/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
 */
package org.geotools.coverage.grid;

// J2SE and JAI dependencies
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.media.jai.ImageFunction;
import javax.media.jai.util.CaselessStringKey;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.referencing.crs.DefaultGeographicCRS;


/**
 * A factory for {@linkplain GridCoverage2D grid coverage} objects.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 *
 * @since 2.1
 */
public class GridCoverageFactory {
    /**
     * Creates a default factory. Users should not need to creates instance of this class
     * directly. Invoke {@link org.opengis.coverage.FactoryFinder#getGridCoverageFactory}
     * instead.
     */
    public GridCoverageFactory() {
    }

    /**
     * Constructs a grid coverage from an image function.
     *
     * @param name         The grid coverage name.
     * @param function     The image function.
     * @param crs          The coordinate reference system. This specifies the CRS used when
     *                     accessing a grid coverage with the {@code evaluate} methods. The
     *                     number of dimensions must matches the number of dimensions of
     *                     the grid range in {@code gridGeometry}.
     * @param gridGeometry The grid geometry. The grid range must contains the expected
     *                     image size (width and height).
     * @param bands        Sample dimensions for each image band, or {@code null} for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in {@code image}.
     * @param properties The set of properties for this coverage, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the grid range's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage create
            (final CharSequence             name, final ImageFunction  function,
             final CoordinateReferenceSystem crs, final GridGeometry2D gridGeometry,
             final GridSampleDimension[]   bands, final Map            properties)
            throws MismatchedDimensionException
    {
        return new GridCoverage2D(name, function, crs, gridGeometry, bands, properties);
    }

    /**
     * Constructs a grid coverage from a raster and an envelope in
     * <var>longitude</var>,<var>latitude</var> coordinates. The coordinate system is assumed to
     * be based on {@linkplain DefaultGeographicCRS#WGS84 WGS84}. A default color palette
     * is built from the minimal and maximal values found in the raster.
     *
     * @param name     The grid coverage name.
     * @param raster   The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                 values are mapped to a transparent color.
     * @param envelope The envelope in geographic (<var>longitude</var>,<var>latitude</var>)
     *                 coordinates.
     *
     * @throws MismatchedDimensionException If the envelope's dimension is not 2.
     */
    public GridCoverage create
            (final CharSequence   name,
             final WritableRaster raster,
             final Envelope       envelope)
            throws MismatchedDimensionException
    {
        return new GridCoverage2D(name, raster, envelope);
    }

    /**
     * Constructs a grid coverage from a {@linkplain Raster raster} with the specified
     * {@linkplain Envelope envelope}.
     *
     * @param name        The grid coverage name.
     * @param raster      The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                    values are mapped to a transparent color.
     * @param crs         The coordinate reference system. This specifies the CRS used when
     *                    accessing a grid coverage with the {@code evaluate} methods. The
     *                    number of dimensions must matches the number of dimensions
     *                    of {@code envelope}.
     * @param envelope    The grid coverage cordinates. This envelope must have at least two
     *                    dimensions.   The two first dimensions describe the image location
     *                    along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                    optional and may be used to locate the image on a vertical axis or
     *                    on the time axis.
     * @param minValues   The minimal value for each bands in the raster, or {@code null}
     *                    for computing it automatically.
     * @param maxValues   The maximal value for each bands in the raster, or {@code null}
     *                    for computing it automatically.
     * @param units       The units of sample values, or {@code null} if unknow.
     * @param colors      The colors to use for values from {@code minValues} to
     *                    {@code maxValues} for each bands, or {@code null} for a
     *                    default color palette. If non-null, each arrays {@code colors[b]}
     *                    may have any length; colors will be interpolated as needed.
     * @param hints       An optional set of rendering hints, or {@code null} if none.
     *                    Those hints will not affect the grid coverage to be created.
     *                    However, they may affect the grid coverage to be returned by
     *                    <code>{@link GridCoverage2D#geophysics geophysics}(false)</code>, i.e.
     *                    the view to be used at rendering time. The optional hint
     *                    {@link Hints#SAMPLE_DIMENSION_TYPE} specifies the
     *                    {@link SampleDimensionType} to be used at rendering time, which can be
     *                    one of {@link SampleDimensionType#UNSIGNED_8BITS UNSIGNED_8BITS} or
     *                    {@link SampleDimensionType#UNSIGNED_16BITS UNSIGNED_16BITS}.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage create
            (final CharSequence             name, final WritableRaster raster,
             final CoordinateReferenceSystem crs, final Envelope       envelope,
             final double[]            minValues, final double[]       maxValues,
             final Unit                    units, final Color[][]      colors,
             final RenderingHints          hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        return new GridCoverage2D(name, raster, crs, envelope,
                    minValues, maxValues, units, colors, hints);
    }

    /**
     * Constructs a grid coverage from a {@linkplain Raster raster} with the specified
     * "{@linkplain GridGeometry#getGridToCoordinateSystem grid to coordinate system}"
     * transform.
     *
     * @param name        The grid coverage name.
     * @param raster      The data (may be floating point numbers). {@linkplain Float#NaN NaN}
     *                    values are mapped to a transparent color.
     * @param crs         The coordinate reference system. This specifies the CRS used when
     *                    accessing a grid coverage with the {@code evaluate} methods.
     * @param gridToCRS   The math transform from grid to coordinate reference system.
     * @param minValues   The minimal value for each bands in the raster, or {@code null}
     *                    for computing it automatically.
     * @param maxValues   The maximal value for each bands in the raster, or {@code null}
     *                    for computing it automatically.
     * @param units       The units of sample values, or {@code null} if unknow.
     * @param colors      The colors to use for values from {@code minValues} to
     *                    {@code maxValues} for each bands, or {@code null} for a
     *                    default color palette. If non-null, each arrays {@code colors[b]}
     *                    may have any length; colors will be interpolated as needed.
     * @param hints       An optional set of rendering hints, or {@code null} if none.
     *                    Those hints will not affect the grid coverage to be created.
     *                    However, they may affect the grid coverage to be returned by
     *                    <code>{@link GridCoverage2D#geophysics geophysics}(false)</code>, i.e.
     *                    the view to be used at rendering time. The optional hint
     *                    {@link Hints#SAMPLE_DIMENSION_TYPE} specifies the
     *                    {@link SampleDimensionType} to be used at rendering time, which can be
     *                    one of {@link SampleDimensionType#UNSIGNED_8BITS UNSIGNED_8BITS} or
     *                    {@link SampleDimensionType#UNSIGNED_16BITS UNSIGNED_16BITS}.
     *
     * @throws MismatchedDimensionException If the {@code gridToCRS} dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage create
            (final CharSequence             name, final WritableRaster raster,
             final CoordinateReferenceSystem crs, final MathTransform  gridToCRS,
             final double[]            minValues, final double[]       maxValues,
             final Unit                    units, final Color[][]      colors,
             final RenderingHints          hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        return new GridCoverage2D(name, raster, crs, gridToCRS,
                    minValues, maxValues, units, colors, hints);
    }

    /**
     * Constructs a grid coverage with the specified envelope. A default set of
     * {@linkplain SampleDimension sample dimensions} is used.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param crs          The coordinate reference system. This specifies the CRS used when
     *                     accessing a grid coverage with the {@code evaluate} methods. The
     *                     number of dimensions must matches the number of dimensions
     *                     of {@code envelope}.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     */
    public GridCoverage create
            (final CharSequence             name, final RenderedImage  image,
             final CoordinateReferenceSystem crs, final Envelope    envelope)
            throws MismatchedDimensionException
    {
        return new GridCoverage2D(name, image, crs, envelope);
    }

    /**
     * Constructs a grid coverage with the specified envelope and sample dimensions.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param crs          The coordinate reference system. This specifies the CRS used when
     *                     accessing a grid coverage with the {@code evaluate} methods. The
     *                     number of dimensions must matches the number of dimensions
     *                     of {@code envelope}.
     * @param envelope     The grid coverage cordinates. This envelope must have at least two
     *                     dimensions.   The two first dimensions describe the image location
     *                     along <var>x</var> and <var>y</var> axis. The other dimensions are
     *                     optional and may be used to locate the image on a vertical axis or
     *                     on the time axis.
     * @param bands        Sample dimensions for each image band, or {@code null} for default
     *                     sample dimensions. If non-null, then this array's length must matches
     *                     the number of bands in {@code image}.
     * @param sources      The sources for this grid coverage, or {@code null} if none.
     * @param properties The set of properties for this coverage, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the envelope's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage create
            (final CharSequence             name, final RenderedImage    image,
             final CoordinateReferenceSystem crs, final Envelope      envelope,
             final GridSampleDimension[]   bands, final GridCoverage[] sources,
             final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        return new GridCoverage2D(name, image, crs, envelope, bands, sources, properties);
    }

    /**
     * Constructs a grid coverage with the specified transform and sample dimension.
     * This is the most general constructor, the one that gives the maximum control
     * on the grid coverage to be created.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param crs          The coordinate reference system. This specifies the CRS used when
     *                     accessing a grid coverage with the {@code evaluate} methods. The
     *                     number of dimensions must matches the number of target dimensions
     *                     of {@code gridToCRS}.
     * @param gridToCRS    The math transform from grid to coordinate reference system.
     * @param bands        Sample dimensions for each image band, or {@code null} for
     *                     default sample dimensions. If non-null, then this array's length
     *                     must matches the number of bands in {@code image}.
     * @param sources      The sources for this grid coverage, or {@code null} if none.
     * @param properties The set of properties for this coverage, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     *
     * @throws MismatchedDimensionException If the transform's dimension
     *         is not the same than the coordinate system's dimension.
     * @throws IllegalArgumentException if the number of bands differs
     *         from the number of sample dimensions.
     */
    public GridCoverage create
            (final CharSequence             name, final RenderedImage     image,
             final CoordinateReferenceSystem crs, final MathTransform gridToCRS,
             final GridSampleDimension[]   bands, final GridCoverage[]  sources,
             final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        return new GridCoverage2D(name, image, crs, gridToCRS, bands, sources, properties);
    }
}
