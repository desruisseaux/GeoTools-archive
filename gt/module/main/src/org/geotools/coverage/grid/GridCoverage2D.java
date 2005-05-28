/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.Color;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.LogRecord;
import javax.units.Unit;

// JAI dependencies
import javax.media.jai.ImageFunction;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.NullOpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.LookupDescriptor;
import javax.media.jai.operator.PiecewiseDescriptor;
import javax.media.jai.operator.RescaleDescriptor;
import javax.media.jai.remote.SerializableRenderedImage;
import javax.media.jai.util.CaselessStringKey;  // For javadoc

// OpenGIS dependencies
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.SampleDimensionType;  // For javadoc
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import org.opengis.util.Cloneable;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.AbstractCoverage;
import org.geotools.coverage.processing.AbstractGridCoverageProcessor;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.util.NumberRange;
import org.geotools.util.WeakHashSet;


/**
 * Basic access to grid data values backed by a two-dimensional
 * {@linkplain RenderedImage rendered image}. Each band in an image is represented as a
 * {@linkplain GridSampleDimension sample dimension}.
 * <br><br>
 * Grid coverages are usually two-dimensional. However, {@linkplain #getEnvelope their envelope}
 * may have more than two dimensions. For example, a remote sensing image may be valid only over
 * some time range (the time of satellite pass over the observed area). Envelopes for such grid
 * coverage can have three dimensions: the two usual ones (horizontal extent along <var>x</var>
 * and <var>y</var>), and a third one for start time and end time (time extent along <var>t</var>).
 * However, the {@linkplain GeneralGridRange grid range} for all extra-dimension <strong>must</strong>
 * have a {@linkplain GeneralGridRange#getLength size} not greater than 1. In other words, a
 * {@code GridCoverage2D} can be a slice in a 3 dimensional grid coverage. Each slice can have an
 * arbitrary width and height (like any two-dimensional images), but only 1 voxel depth (a "voxel"
 * is a three-dimensional pixel).
 * <br><br>
 * <strong>Serialization note:</strong><br>
 * Because it is serializable, {@code GridCoverage2D} can be included as method argument or as
 * return type in <cite>Remote Method Invocation</cite> (RMI). However, the pixel data are not
 * sent during serialization. Instead, the image data are transmitted "on-demand" using socket
 * communications. This mechanism is implemented using JAI {@link SerializableRenderedImage}
 * class. While serialization (usually on server side) should work on J2SE 1.4 and above,
 * deserialization (usually on client side) of {@code GridCoverage2D} instances requires J2SE 1.5.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverage2D extends AbstractGridCoverage implements RenderedCoverage {
    /**
     * Slight number for rounding errors in floating point comparaison.
     */
    private static final float EPS = 1E-5f;

    /**
     * {@code true} if we should apply a conservative policy for the "piecewise" operation.
     * The conservative policy is to apply "piecewise" only if there is no ambiguity about what
     * the user wants.
     */
    private static final boolean CONSERVATIVE_PIECEWISE = true;

    /**
     * Axis orientation of image's coordinate systems. In most images, <var>x</var> values are
     * increasing toward the right ({@code EAST}) and <var>y</var> values are increasing
     * toward the bottom ({@code SOUTH}). This is different to many geographic coordinate
     * reference systems, which have <var>y</var> values increasing {@code NORTH}. The grid
     * coverage constructor will compare the geographic axis orientations to this
     * {@code IMAGE_ORIENTATION} and inverse the <var>y</var> axis if necessary. The axis
     * inversions are handle by {@link GridGeometry#getGridToCoordinateSystem()}.
     */
    private static final AxisDirection[] IMAGE_ORIENTATION = {
        AxisDirection.EAST,
        AxisDirection.SOUTH
    };

    /**
     * Pool of created object. Objects in this pool must be immutable.
     * Those objects will be shared among many grid coverages.
     */
    private static final WeakHashSet pool = new WeakHashSet();

    /**
     * A grid coverage using the sample dimensions {@code GridSampleDimension.inverse}.
     * This object is constructed and returned by {@link #geophysics}. Constructed when
     * first needed. May appears also in the {@link #sources} list.
     */
    private transient GridCoverage2D inverse;

    /**
     * The two-dimensional part of the coordinate reference system.
     * This is usually (but not always) identical to {@link #crs}.
     */
    private final CoordinateReferenceSystem crs2D;
    
    /**
     * The raster data.
     */
    protected transient final PlanarImage image;

    /**
     * The serialized image, as an instance of {@link SerializableRenderedImage}.
     * This image will be created only when first needed during serialization.
     */
    private RenderedImage serializedImage;
    
    /**
     * The grid geometry.
     */
    protected final GridGeometry2D gridGeometry;
    
    /**
     * The coverage's envelope. This envelope must have at least two dimensions. It may have more
     * dimensions if the coverage has some extent in other dimensions (for example a depth, or a
     * start and end time).
     */
    private final GeneralEnvelope envelope;
    
    /**
     * List of sample dimension information for the grid coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    private final GridSampleDimension[] sampleDimensions;

    /**
     * {@code true} is all sample in the image are geophysics values.
     */
    private final boolean isGeophysics;
    
    /**
     * Construct a new grid coverage with the same parameter than the specified
     * coverage. This constructor is useful when creating a coverage with
     * identical data, but in which some method has been overriden in order to
     * process data differently (e.g. interpolating them).
     *
     * @param name The name for this coverage, or {@code null} for the same than {@code coverage}.
     * @param coverage The source grid coverage.
     */
    protected GridCoverage2D(final CharSequence   name,
                             final GridCoverage2D coverage)
    {
        super(name, coverage);
        crs2D            = coverage.crs2D;
        image            = coverage.image;
        gridGeometry     = coverage.gridGeometry;
        envelope         = coverage.envelope;
        sampleDimensions = coverage.sampleDimensions;
        isGeophysics     = coverage.isGeophysics;
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
     *
     * @deprecated Replaced by a {@code create} method with the same signature in
     *             {@link GridCoverageFactory}.
     */
    public GridCoverage2D(final CharSequence             name, final ImageFunction  function,
                          final CoordinateReferenceSystem crs, final GridGeometry2D gridGeometry,
                          final GridSampleDimension[]   bands, final Map            properties)
            throws MismatchedDimensionException
    {
        this(name, getImage(function, gridGeometry),
             crs, gridGeometry, null, bands, null, properties);
    }

    /**
     * Creates an image from an image function. Translation and scale
     * factors are fetched from the grid geometry, which must have an
     * affine transform.
     *
     * @todo We could support shear in affine transform.
     * @todo Should be inlined in the above constructor if only Sun was to fix RFE #4093999
     *       ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static PlanarImage getImage(final ImageFunction  function,
                                        final GridGeometry2D gridGeometry)
    {
        final MathTransform transform = gridGeometry.getGridToCoordinateSystem2D();
        if (!(transform instanceof AffineTransform)) {
            throw new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_NOT_AN_AFFINE_TRANSFORM));
        }
        final AffineTransform at = (AffineTransform) transform;
        if (at.getShearX()!=0 || at.getShearY()!=0) {
            // TODO: We may support that in a future version.
            //       1) Create a copy with shear[X/Y] set to 0. Use the copy.
            //       2) Compute the residu with createInverse() and concatenate().
            //       3) Apply the residu with JAI.create("Affine").
            throw new IllegalArgumentException("Shear and rotation not supported");
        }
        final double xScale =  at.getScaleX();
        final double yScale =  at.getScaleY();
        final double xTrans = -at.getTranslateX()/xScale;
        final double yTrans = -at.getTranslateY()/yScale;
        final GridRange      range = gridGeometry.getGridRange();
        final ParameterBlock param = new ParameterBlock().add(function)
                                                         .add(range.getLength(0)) // width
                                                         .add(range.getLength(1)) // height
                                                         .add((float) xScale)
                                                         .add((float) yScale)
                                                         .add((float) xTrans)
                                                         .add((float) yTrans);
        return JAI.create("ImageFunction", param);
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
     *
     * @deprecated Replaced by a {@code create} method with the same signature in
     *             {@link GridCoverageFactory}.
     */
    public GridCoverage2D(final CharSequence   name,
                          final WritableRaster raster,
                          final Envelope       envelope)
            throws MismatchedDimensionException
    {
        this(name, raster, DefaultGeographicCRS.WGS84, envelope, null, null, null, null, null);
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
     *                    <code>{@link #geophysics geophysics}(false)</code>, i.e.
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
     *
     * @deprecated Replaced by a {@code create} method with the same signature in
     *             {@link GridCoverageFactory}.
     */
    public GridCoverage2D(final CharSequence             name, final WritableRaster raster,
                          final CoordinateReferenceSystem crs, final Envelope       envelope,
                          final double[]            minValues, final double[]       maxValues,
                          final Unit                    units, final Color[][]      colors,
                          final RenderingHints          hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, raster, crs, null, new GeneralEnvelope(envelope),
             Grid2DSampleDimension.create(name, raster, minValues, maxValues, units, colors, hints));
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
     *                    <code>{@link #geophysics geophysics}(false)</code>, i.e.
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
     *
     * @deprecated Replaced by a {@code create} method with the same signature in
     *             {@link GridCoverageFactory}.
     */
    public GridCoverage2D(final CharSequence             name, final WritableRaster raster,
                          final CoordinateReferenceSystem crs, final MathTransform  gridToCRS,
                          final double[]            minValues, final double[]       maxValues,
                          final Unit                    units, final Color[][]      colors,
                          final RenderingHints          hints)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, raster, crs, new GridGeometry2D(null, gridToCRS), null,
             Grid2DSampleDimension.create(name, raster, minValues, maxValues, units, colors, hints));
    }

    /**
     * Helper constructor for public constructors expecting a {@link Raster} argument.
     *
     * @todo Should be inlined in the above constructor if only Sun was to fix RFE #4093999
     *       ("Relax constraint on placement of this()/super() call in constructors").
     */
    private GridCoverage2D(final CharSequence             name,
                           final WritableRaster         raster,
                           final CoordinateReferenceSystem crs,
                           final GridGeometry2D   gridGeometry, // ONE and only one of those two
                           final GeneralEnvelope      envelope, // arguments should be non-null.
                           final GridSampleDimension[]   bands)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, PlanarImage.wrapRenderedImage(
                   new BufferedImage(bands[0].getColorModel(0, bands.length), raster, false, null)),
             crs, gridGeometry, envelope, bands, null, null);
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
     *
     * @deprecated Replaced by a {@code create} method with the same signature in
     *             {@link GridCoverageFactory}.
     */
    public GridCoverage2D(final CharSequence             name, final RenderedImage  image,
                          final CoordinateReferenceSystem crs, final Envelope    envelope)
            throws MismatchedDimensionException
    {
        this(name, image, crs, envelope, null, null, null);
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
    public GridCoverage2D(final CharSequence             name, final RenderedImage    image,
                          final CoordinateReferenceSystem crs, final Envelope      envelope,
                          final GridSampleDimension[]   bands, final GridCoverage[] sources,
                          final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, PlanarImage.wrapRenderedImage(image), crs, null,
             new GeneralEnvelope(envelope), bands, sources, properties);
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
    public GridCoverage2D(final CharSequence             name, final RenderedImage     image,
                          final CoordinateReferenceSystem crs, final MathTransform gridToCRS,
                          final GridSampleDimension[]   bands, final GridCoverage[]  sources,
                          final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, PlanarImage.wrapRenderedImage(image), crs, new GridGeometry2D(null, gridToCRS),
             null, bands, sources, properties);
    }

    /**
     * Constructs a grid coverage. This private constructor expects an envelope
     * ({@code envelope}) or a grid geometry ({@code gridGeometry}).
     * <strong>One and only one of those argument</strong> should be non-null.
     * The null arguments will be computed from the non-null argument.
     */
    private GridCoverage2D(final CharSequence             name,
                           final PlanarImage             image,
                           final CoordinateReferenceSystem crs,
                                 GridGeometry2D   gridGeometry, // ONE and only one of those two
                                 GeneralEnvelope      envelope, // arguments should be non-null.
                           final GridSampleDimension[] sdBands,
                           final GridCoverage[]        sources,
                           final Map                properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        super(name, crs, sources, image, properties);
        if ((gridGeometry==null) == (envelope==null)) {
            // Should not happen
            throw new AssertionError();
        }
        this.image = image;
        /*
         * Checks sample dimensions. The number of SampleDimensions must matches the
         * number of image's bands (this is checked by Grid2DSampleDimension.create).
         */
        sampleDimensions = new GridSampleDimension[image.getNumBands()];
        isGeophysics = Grid2DSampleDimension.create(name, image, sdBands, sampleDimensions);
        /*
         * Constructs the grid range and the envelope if they were not explicitly provided.
         * The envelope computation (if needed) requires a valid 'gridToCoordinateSystem'
         * transform in the GridGeometry. Otherwise, no transform are required. The range
         * will be inferred from the image size, if needed. In any cases, the envelope must
         * be non-empty and its dimension must matches the coordinate reference system's
         * dimension. A pool of shared envelopes will be used in order to recycle existing
         * envelopes.
         */
        final CoordinateSystem cs = crs.getCoordinateSystem();
        final GridRange gridRange;
        if (GCSUtilities.hasGridRange(gridGeometry)) {
            gridRange = gridGeometry.getGridRange();
        } else {
            gridRange = new GeneralGridRange(image, cs.getDimension());
            if (GCSUtilities.hasTransform(gridGeometry)) {
                gridGeometry = new GridGeometry2D(gridRange, gridGeometry.getGridToCoordinateSystem());
            }
        }
        if (gridGeometry != null) {
            final String error = checkConsistency(image, gridGeometry);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
            if (envelope == null) {
                envelope = new GeneralEnvelope(gridGeometry.getEnvelope());
            }
        }
        final int dimension = envelope.getDimension();
        if (dimension != cs.getDimension()) {
            throw new MismatchedDimensionException(org.geotools.resources.cts.Resources.format(
                        org.geotools.resources.cts.ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(cs.getDimension()),
                        new Integer(envelope.getDimension())));
        }
        envelope.setCoordinateReferenceSystem(crs);
        this.envelope = (GeneralEnvelope) pool.canonicalize(envelope);
        /*
         * Computes the grid geometry. The math transform will be computed from the envelope.
         * A pool of shared grid geometries will be used in order to recycle existing objects.
         *
         * Note: Should we invert some axis? For example, the 'y' axis is often inversed
         *       (since image use a downward 'y' axis). If all source grid coverages use
         *       the same axis orientations, we will reuse those orientations. Otherwise,
         *       we will use default orientations where only the 'y' axis is inversed.
         */
        if (gridGeometry == null) {
            boolean[] inverse = null;
            if (sources != null) {
                for (int i=0; i<sources.length; i++) {
                    final GridCoverage source = sources[i];
                    if (source instanceof GridCoverage2D) {
                        boolean check[] = ((GridCoverage2D) source).gridGeometry.areAxisInverted();
                        check = XArray.resize(check, dimension);
                        if (inverse != null) {
                            if (!Arrays.equals(check, inverse)) {
                                inverse = null;
                                break;
                            }
                        } else {
                            inverse = check;
                        }
                    }
                }
            }
            if (inverse == null) {
                inverse = new boolean[dimension];
                for (int i=Math.min(IMAGE_ORIENTATION.length, dimension); --i>=0;) {
                    final AxisDirection toInverse = IMAGE_ORIENTATION[i].opposite();
                    inverse[i] = toInverse.equals(cs.getAxis(1).getDirection());
                }
            }
            gridGeometry = new GridGeometry2D(gridRange, envelope, inverse);
        }
        this.gridGeometry = (GridGeometry2D) pool.canonicalize(gridGeometry);
        /*
         * Last argument checks. We do (or redo) some checks here because the grid geometry
         * may not has been available before.
         */
        final String error = checkConsistency(this.image, this.gridGeometry);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (dimension <= Math.max(gridGeometry.axisDimensionX, gridGeometry.axisDimensionY) ||
            !(this.envelope.getLength(gridGeometry.axisDimensionX) > 0) ||
            !(this.envelope.getLength(gridGeometry.axisDimensionY) > 0))
        {
            throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_EMPTY_ENVELOPE));
        }
        /*
         * Constructs the two-dimensional CRS.
         */
        try {
            crs2D = new FactoryGroup().separate(crs, new int[] {gridGeometry.axisDimensionX,
                                                                gridGeometry.axisDimensionY});
        } catch (FactoryException exception) {
            final IllegalArgumentException e =
                new IllegalArgumentException(org.geotools.resources.cts.Resources.format(
                    org.geotools.resources.cts.ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, "crs",
                    crs.getName().getClass()));
            e.initCause(exception); // TODO: inline in the constructor with J2SE 1.5.
            throw e;
        }
        assert crs2D.getCoordinateSystem().getDimension() == 2 : crs2D;
    }

    /**
     * Checks if the bounding box of the specified image is consistents with the specified
     * grid geometry. If an inconsistency has been found, then an error string is returned.
     * This string will be typically used as a message in an exception to be thrown.
     * <br><br>
     * Note that a succesful check at construction time may fails later if the image is part
     * of a JAI chain (i.e. is a {@link RenderedOp}) and its bounds has been edited (i.e the
     * image node as been re-rendered). Since {@code GridCoverage} are immutable by design,
     * we are not allowed to propagate the image change here. The {@link #getGridGeometry} method
     * will thrown an {@link IllegalStateException} in this case.
     */
    private static String checkConsistency(final RenderedImage image, final GridGeometry2D grid) {
        final GridRange range = grid.getGridRange();
        final int dimension = range.getDimension();
        for (int i=0; i<dimension; i++) {
            final int min, length;
            final Object label;
            if (i == grid.gridDimensionX) {
                min    = image.getMinX();
                length = image.getWidth();
                label  = "\"X\"";
            } else if (i == grid.gridDimensionY) {
                min    = image.getMinY();
                length = image.getHeight();
                label  = "\"Y\"";
            } else {
                min    = range.getLower(i);
                length = Math.min(Math.max(range.getUpper(i), 0), 1);
                label  = new Integer(i);
            }
            if (range.getLower(i)!=min || range.getLength(i)!=length) {
                return Resources.format(ResourceKeys.ERROR_BAD_GRID_RANGE_$3, label,
                                        new Integer(min), new Integer(min+length));
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if grid data can be edited. The default
     * implementation returns {@code true} if {@link #image} is an
     * instance of {@link WritableRenderedImage}.
     */
    public boolean isDataEditable() {
        return (image instanceof WritableRenderedImage);
    }

    /**
     * Returns information for the grid coverage geometry. Grid geometry
     * includes the valid range of grid coordinates and the georeferencing.
     *
     * @todo Use covariant return type once we are allowed to compile for J2SE 1.5.
     */
    public GridGeometry getGridGeometry() {
        final String error = checkConsistency(image, gridGeometry);
        if (error != null) {
            throw new IllegalStateException(error);
        }
        return gridGeometry;
    }

    /**
     * Returns the bounding box for the coverage domain in coordinate reference system coordinates.
     * The returned envelope have at least two dimensions. It may have more dimensions if the
     * coverage has some extent in other dimensions (for example a depth, or a start and end time).
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }

    /**
     * Returns the two-dimensional bounding box for the coverage domain in coordinate reference
     * system coordinates. If the coverage envelope has more than two dimensions, only the
     * dimensions used in the underlying rendered image are returned.
     */
    public Envelope2D getEnvelope2D() {
        return new Envelope2D(crs2D,
                envelope.getMinimum(gridGeometry.gridDimensionX),
                envelope.getMinimum(gridGeometry.gridDimensionY),
                envelope.getLength (gridGeometry.gridDimensionX),
                envelope.getLength (gridGeometry.gridDimensionY));
    }

    /**
     * Returns the two-dimensional part of this grid coverage CRS. This is usually (but not
     * always) identical to the {@linkplain #getCoordinateReferenceSystem full CRS}.
     *
     * @see #getCoordinateReferenceSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem2D() {
        return crs2D;
    }

    /**
     * Returns the number of bands in the grid coverage.
     */
    public int getNumSampleDimensions() {
        return sampleDimensions.length;
    }

    /**
     * Retrieve sample dimension information for the coverage.
     * For a grid coverage, a sample dimension is a band. The sample dimension information
     * include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated
     * with the dimension. A coverage must have at least one sample dimension.
     */
    public SampleDimension getSampleDimension(final int index) {
        return sampleDimensions[index];
    }

    /**
     * Returns all sample dimensions for this grid coverage.
     */
    public GridSampleDimension[] getSampleDimensions() {
        return (GridSampleDimension[]) sampleDimensions.clone();
    }

    /**
     * Returns the interpolation used for all {@code evaluate(...)} methods.
     * The default implementation returns {@link InterpolationNearest}.
     *
     * @return The interpolation.
     */
    public Interpolation getInterpolation() {
        return Interpolation.getInstance(Interpolation.INTERP_NEAREST);
    }

    /**
     * Return the value vector for a given point in the coverage.
     * A value for each sample dimension is included in the vector.
     */
    public Object evaluate(final DirectPosition point) throws CannotEvaluateException {
        switch (image.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE:   return evaluate(point, (byte  []) null);
            case DataBuffer.TYPE_SHORT:  // Fall through
            case DataBuffer.TYPE_USHORT: // Fall through
            case DataBuffer.TYPE_INT:    return evaluate(point, (int   []) null);
            case DataBuffer.TYPE_FLOAT:  return evaluate(point, (float []) null);
            case DataBuffer.TYPE_DOUBLE: return evaluate(point, (double[]) null);
            default: throw new CannotEvaluateException();
        }
    }
    
    /**
     * Returns a sequence of byte values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public byte[] evaluate(final DirectPosition coord, byte[] dest)
            throws CannotEvaluateException
    {
        final int[] array = evaluate(coord, (int[]) null);
        if (dest == null) {
            dest = new byte[array.length];
        }
        for (int i=0; i<array.length; i++) {
            dest[i] = (byte) array[i];
        }
        return dest;
    }
    
    /**
     * Returns a sequence of integer values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final DirectPosition coord, final int[] dest)
            throws CannotEvaluateException
    {
        return evaluate(toPoint2D(coord), dest);
    }

    /**
     * Returns a sequence of float values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final DirectPosition coord, final float[] dest)
            throws CannotEvaluateException
    {
        return evaluate(toPoint2D(coord), dest);
    }

    /**
     * Returns a sequence of double values for a given point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final DirectPosition coord, final double[] dest)
            throws CannotEvaluateException
    {
        return evaluate(toPoint2D(coord), dest);
    }

    /**
     * Converts the specified point into a two-dimensional one.
     *
     * @param  point The point to transform into a {@link Point2D} object.
     * @return The specified point as a {@link Point2D} object.
     * @throws MismatchedDimensionException if the point doesn't have the expected dimension.
     */
    private Point2D toPoint2D(final DirectPosition point) throws MismatchedDimensionException {
        final int actual   = point.getDimension();
        final int expected = crs.getCoordinateSystem().getDimension();
        if (actual != expected) {
            throw new MismatchedDimensionException(org.geotools.resources.cts.Resources.format(
                        org.geotools.resources.cts.ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
                        new Integer(actual), new Integer(expected)));
        }
        if (point instanceof Point2D) {
            return (Point2D) point;
        }
        return new Point2D.Double(point.getOrdinate(gridGeometry.axisDimensionX),
                                  point.getOrdinate(gridGeometry.axisDimensionY));
    }

    /**
     * Returns a sequence of integer values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public int[] evaluate(final Point2D coord, final int[] dest)
            throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(pointOutsideCoverage(coord));
    }
    
    /**
     * Returns a sequence of float values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public float[] evaluate(final Point2D coord, final float[] dest)
            throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(pointOutsideCoverage(coord));
    }

    /**
     * Returns a sequence of double values for a given two-dimensional point in the coverage.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest  An array in which to store values, or {@code null}.
     * @return An array containing values.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates.
     */
    public double[] evaluate(final Point2D coord, final double[] dest)
            throws CannotEvaluateException
    {
        final Point2D pixel = gridGeometry.inverseTransform(coord);
        final double fx = pixel.getX();
        final double fy = pixel.getY();
        if (!Double.isNaN(fx) && !Double.isNaN(fy)) {
            final int x = (int)Math.round(fx);
            final int y = (int)Math.round(fy);
            if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
                return image.getTile(image.XToTileX(x), image.YToTileY(y)).getPixel(x, y, dest);
            }
        }
        throw new PointOutsideCoverageException(pointOutsideCoverage(coord));
    }

    /**
     * Returns a debug string for the specified coordinate.   This method produces a
     * string with pixel coordinates and pixel values for all bands (with geophysics
     * values or category name in parenthesis). Example for a 1-banded image:
     *
     * <blockquote><pre>(1171,1566)=[196 (29.6 °C)]</pre></blockquote>
     *
     * @param  coord The coordinate point where to evaluate.
     * @return A string with pixel coordinates and pixel values at the specified location,
     *         or {@code null} if <code>coord</code> is outside coverage.
     */
    public synchronized String getDebugString(final DirectPosition coord) {
        Point2D pixel = toPoint2D(coord);
        pixel         = gridGeometry.inverseTransform(pixel);
        final int   x = (int)Math.round(pixel.getX());
        final int   y = (int)Math.round(pixel.getY());
        if (image.getBounds().contains(x,y)) { // getBounds() returns a cached instance.
            final int  numBands = image.getNumBands();
            final Raster raster = image.getTile(image.XToTileX(x), image.YToTileY(y));
            final int  datatype = image.getSampleModel().getDataType();
            final StringBuffer  buffer = new StringBuffer();
            buffer.append('(');
            buffer.append(x);
            buffer.append(',');
            buffer.append(y);
            buffer.append(")=[");
            for (int band=0; band<numBands; band++) {
                if (band!=0) {
                    buffer.append(";\u00A0");
                }
                final double sample = raster.getSampleDouble(x, y, band);
                switch (datatype) {
                    case DataBuffer.TYPE_DOUBLE: buffer.append((double)sample); break;
                    case DataBuffer.TYPE_FLOAT : buffer.append( (float)sample); break;
                    default                    : buffer.append(   (int)sample); break;
                }
                final String formatted = sampleDimensions[band].getLabel(sample, null);
                if (formatted != null) {
                    buffer.append("\u00A0(");
                    buffer.append(formatted);
                    buffer.append(')');
                }
            }
            buffer.append(']');
            return buffer.toString();
        }
        return null;
    }

    /**
     * Returns the optimal size to use for each dimension when accessing grid values.
     * The default implementation returns the image's tiles size.
     */
    public int[] getOptimalDataBlockSizes() {
        final int[] size = new int[getDimension()];
        Arrays.fill(size, 1);
        size[gridGeometry.gridDimensionX] = image.getTileWidth();
        size[gridGeometry.gridDimensionY] = image.getTileHeight();
        return size;
    }

    /**
     * Returns grid data as a rendered image.
     */
    public RenderedImage getRenderedImage() {
        return image;
    }
    
    /**
     * Returns 2D view of this grid coverage as a renderable image.
     * This method allows interoperability with Java2D.
     *
     * @param  xAxis Dimension to use for <var>x</var> axis.
     * @param  yAxis Dimension to use for <var>y</var> axis.
     * @return A 2D view of this grid coverage as a renderable image.
     */
    public RenderableImage getRenderableImage(final int xAxis, final int yAxis) {
        if (xAxis == gridGeometry.axisDimensionX  &&  yAxis == gridGeometry.axisDimensionY) {
            return new Renderable();
        } else {
            return super.getRenderableImage(xAxis, yAxis);
        }
    }

    /**
     * Display this coverage in a windows. This convenience method is used for debugging purpose.
     * The exact appareance of the windows and the tools provided may changes in future versions.
     */
    public void show() {
        show(gridGeometry.axisDimensionX, gridGeometry.axisDimensionY);
    }

    /**
     * A view of a {@linkplain GridCoverage2D grid coverage} as a renderable image. Renderable images
     * allow interoperability with <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>
     * for a two-dimensional slice of a grid coverage.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @see AbstractCoverage#getRenderableImage
     *
     * @todo Override {@link #createRendering} and use the affine transform operation.
     *       Also uses the JAI's "Transpose" operation is x and y axis are interchanged.
     */
    protected class Renderable extends AbstractCoverage.Renderable {
        /**
         * Constructs a renderable image.
         */
        public Renderable() {
            super(gridGeometry.axisDimensionX, gridGeometry.axisDimensionY);
        }
        
        /**
         * Returns a rendered image with a default width and height in pixels.
         *
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createDefaultRendering() {
            if (xAxis == gridGeometry.axisDimensionX &&
                yAxis == gridGeometry.axisDimensionY)
            {
                return getRenderedImage();
            }
            return super.createDefaultRendering();
        }
    }
    
    /**
     * Hints that the given area may be needed in the near future. Some implementations
     * may spawn a thread or threads to compute the tiles while others may ignore the hint.
     *
     * @param area A rectangle indicating which geographic area to prefetch.
     *             This area's coordinates must be expressed according the
     *             grid coverage's coordinate reference system, as given by
     *             {@link #getCoordinateReferenceSystem}.
     */
    public void prefetch(final Rectangle2D area) {
        final Point[] tileIndices=image.getTileIndices(gridGeometry.inverseTransform(area));
        if (tileIndices!=null) {
            image.prefetchTiles(tileIndices);
        }
    }

    /**
     * If {@code true}, returns the geophysics companion of this grid coverage. In a
     * <cite>geophysics grid coverage</cite>, all sample values are equals to geophysics
     * ("real world") values without the need for any transformation. In such geophysics
     * coverage, the {@linkplain SampleDimension#getSampleToGeophysics sample to geophysics}
     * transform is the identity transform for all sample dimensions. "No data" values are
     * expressed by {@linkplain Float#NaN NaN} numbers.
     * <br><br>
     * This method may be understood as applying the JAI's {@linkplain PiecewiseDescriptor
     * piecewise} operation with breakpoints specified by the {@link Category} objects in
     * each sample dimension. However, it is more general in that the transformation specified
     * with each breakpoint doesn't need to be linear. On an implementation note, this method
     * will really try to use the first of the following operations which is found applicable:
     * <cite>identity</cite>, {@linkplain LookupDescriptor lookup}, {@linkplain RescaleDescriptor
     * rescale}, {@linkplain PiecewiseDescriptor piecewise} and in last ressort a more general
     * (but slower) <cite>sample transcoding</cite> algorithm.
     * <br><br>
     * {@code GridCoverage} objects live by pair: a <cite>geophysics</cite> one (used for
     * computation) and a <cite>non-geophysics</cite> one (used for packing data, usually as
     * integers). The {@code geo} argument specifies which object from the pair is wanted,
     * regardless if this method is invoked on the geophysics or non-geophysics instance of the
     * pair. In other words, the result of {@code geophysics(b1).geophysics(b2).geophysics(b3)}
     * depends only on the value in the last call ({@code b3}).
     *
     * @param  geo {@code true} to get a grid coverage with sample values equals to geophysics
     *         values, or {@code false} to get the packed version.
     * @return The grid coverage. Never {@code null}, but may be {@code this}.
     *
     * @see GridSampleDimension#geophysics
     * @see Category#geophysics
     * @see LookupDescriptor
     * @see RescaleDescriptor
     * @see PiecewiseDescriptor
     */
    public GridCoverage2D geophysics(final boolean geo) {
        if (geo == isGeophysics) {
            return this;
        }
        if (inverse != null) {
            return inverse;
        }
        if (!GCSUtilities.hasTransform(sampleDimensions)) {
            return inverse=this;
        }
        synchronized (this) {
            inverse = createGeophysics(geo);
            if (inverse.inverse == null) {
                inverse.inverse = this;
            } else if (inverse.inverse != this) {
                final Locale locale = getLocale();
                throw new RasterFormatException(Resources.getResources(locale).getString(
                          ResourceKeys.ERROR_COVERAGE_ALREADY_BOUND_$2,
                          "geophysics", inverse.inverse.getName().toString(locale)));
            }
            return inverse;
        }
    }

    /**
     * Invoked by {@link #geophysics(boolean)} when the packed or geophysics companion of this
     * grid coverage need to be created. Subclasses may override this method in order to modify
     * the object to be created.
     *
     * @param  geo {@code true} to get a grid coverage with sample values equals to
     *         geophysics values, or {@code false} to get the packed version.
     * @return The newly created grid coverage.
     *
     * @todo IndexColorModel seems to badly choose its sample model. As of JDK 1.4-rc1, it
     *       construct a ComponentSampleModel, which is drawn very slowly to the screen. A
     *       much faster sample model is PixelInterleavedSampleModel,  which is the sample
     *       model used by BufferedImage for TYPE_BYTE_INDEXED. We should check if this is
     *       fixed in future J2SE release.
     *
     * @todo The "Piecewise" operation is disabled because javac 1.4.1_01 generate illegal
     *       bytecode. This bug is fixed in javac 1.4.2-beta. However, we still have an
     *       ArrayIndexOutOfBoundsException in JAI code...
     *
     * @todo A special case (exactly one linear relationship with one NaN value mapping
     *       exactly to the index value 0) was optimized to the "Rescale" operation in
     *       previous version. This case is very common, which make this optimization a
     *       usefull one. Unfortunatly, it had to be disabled because there is nothing
     *       in the "Rescale" preventing some real number (not NaN) to maps to 0 through
     *       the normal linear relationship. Note that the optimization worked well in
     *       previous version except for the above-cited problem. We can very easily re-
     *       enable it later if we know the range of values really stored in the image
     *       (as of JAI's "extrema" operation). If would suffice to add a check making
     *       sure that the range of transformed values doesn't contains 0.
     */
    protected GridCoverage2D createGeophysics(final boolean geo) {
        /*
         * STEP 1 - Gets the source image and prepare the target sample dimensions.
         *          As a slight optimisation, we skip the "Null" operations since
         *          such image may be the result of some "Colormap" operation.
         */
        PlanarImage image = this.image;
        while (image instanceof NullOpImage) {
            final NullOpImage op = (NullOpImage) image;
            if (op.getNumSources() != 1) {
                break;
            }
            image = op.getSourceImage(0);
        }
        final int                      numBands = image.getNumBands();
        final int                   visibleBand = GCSUtilities.getVisibleBand(image);
        final GridSampleDimension[] targetBands = (GridSampleDimension[]) sampleDimensions.clone();
        assert targetBands.length == numBands : targetBands.length;
        for (int i=0; i<targetBands.length; i++) {
            targetBands[i] = targetBands[i].geophysics(geo);
        }
        /*
         * STEP 2 - Computes the layout for the destination RenderedImage. We will use the same
         *          layout than the parent image, except for tile size if the parent image had
         *          only one big tile, and for the color model and sample model  (since we are
         *          reformating data in the process of this operation).
         */
        ImageLayout layout = ImageUtilities.getImageLayout(image);
        ColorModel  colors = targetBands[visibleBand].getColorModel(visibleBand, numBands);
        SampleModel  model = colors.createCompatibleSampleModel(layout.getTileWidth (image),
                                                                layout.getTileHeight(image));
        if (colors instanceof IndexColorModel && model.getClass().equals(ComponentSampleModel.class))
        {
            // There is the 'IndexColorModel' hack (see method description).
            final int w = model.getWidth();
            final int h = model.getHeight();
            model = new PixelInterleavedSampleModel(colors.getTransferType(), w,h,1,w, new int[1]);
        }
        layout = layout.setSampleModel(model).setColorModel(colors);
        ParameterBlock param = new ParameterBlock().addSource(image);
        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
        String operation = null; // Will be set in step 3 or 4.
        /*
         * STEP 3 - Checks if the transcoding could be done with the JAI's "Lookup" operation.
         *          This is probably the fatest operation available for 'geophysics(true)'.
         */
        try {
            final int sourceType = image.getSampleModel().getDataType();
            final int targetType = model.getDataType();
            final MathTransform1D[] transforms = new MathTransform1D[numBands];
            for (int i=0; i<numBands; i++) {
                transforms[i] = sampleDimensions[i].geophysics(false).getSampleToGeophysics();
                if (transforms[i]!=null && !geo) {
                    // We are going to convert geophysics values to packed one.
                    transforms[i] = (MathTransform1D) transforms[i].inverse();
                }
            }
            LookupTableJAI table = LookupTableFactory.create(sourceType, targetType, transforms);
            if (table != null) {
                operation = "Lookup";
                param = param.add(table);
            }
        } catch (TransformException exception) {
            // A value can't be transformed. Fallback on a more general operation.
            // REVISIT: the more general operations are likely to fail too...
        }
        /*
         * STEP 4 - Check if the transcoding could be done with a JAI's "Rescale" or "Piecewise"
         *          operations. The "Rescale" operation requires a completly linear relationship
         *          between the source and the destination sample values. The "Piecewise" operation
         *          is less strict: piecewise breakpoints are very similar to categories, but the
         *          transformation for all categories still have to be linear.
         */
        if (operation == null) try {
            boolean     canRescale   = true; // 'true' if the "Rescale"   operation can be applied.
            boolean     canPiecewise = true; // 'true' if the "Piecewise" operation can be applied.
            double[]    scales       = null; // The first  argument for "Rescale".
            double[]    offsets      = null; // The second argument for "Rescale".
            float[][][] breakpoints  = null; // The only   argument for "Piecewise".
testLinear: for (int i=0; i<numBands; i++) {
                final GridSampleDimension sd = sampleDimensions[i];
                final List        categories = sd.getCategories();
                final int      numCategories = categories.size();
                float[]    sourceBreakpoints = null;
                float[]    targetBreakpoints = null;
                double        expectedSource = Double.NaN;
                double        expectedTarget = Double.NaN;
                int jbp = 0; // Break point index (vary with j)
                for (int j=0; j<numCategories; j++) {
                    final Category category = (Category) categories.get(j);
                    MathTransform1D transform = category.geophysics(false).getSampleToGeophysics();
                    if (transform == null) {
                        // A "qualitative" category was found. Those categories maps NaN values,
                        // which need the special processing by our "SampleTranscode" operation.
                        canPiecewise = false;
                        if (false) {
                            // As a special case, the "Rescale" operation  could continue to work
                            // if the NaN value maps to 0. Unfortunatly, this optimization had to
                            // be disabled for now for the reason explained in the @todo tag in
                            // method's comments.
                            if (category.geophysics(geo).getRange().getMinimum(true) == 0) {
                                assert Double.isNaN(category.getRange().getMinimum()) : category;
                                continue;
                            }
                        }
                        canRescale = false;
                        break testLinear;
                    }
                    if (!geo) {
                        // We are going to convert geophysics values to packed one.
                        transform = (MathTransform1D) transform.inverse();
                    }
                    final double offset = transform.transform(0);
                    final double scale  = transform.derivative(Double.NaN);
                    if (Double.isNaN(scale) || Double.isNaN(offset)) {
                        // One category doesn't use a linear transformation. We can't deal with
                        // that with "Rescale" or "Piecewise". Fallback on our "SampleTranscode".
                        canRescale   = false;
                        canPiecewise = false;
                        break testLinear;
                    }
                    // Allocates arrays the first time the loop is run up to this point.
                    // Store scale and offset, and check if they still the same.
                    if (j == 0) {
                        if (i == 0) {
                            scales      = new double[numBands];
                            offsets     = new double[numBands];
                            breakpoints = new float [numBands][][];
                        }
                        sourceBreakpoints = new float[numCategories * 2];
                        targetBreakpoints = new float[numCategories * 2];
                        breakpoints[i] = new float[][] {sourceBreakpoints, targetBreakpoints};
                        offsets    [i] = offset;
                        scales     [i] = scale;
                    }
                    if (offset!=offsets[i] || scale!=scales[i]) {
                        canRescale = false;
                    }
                    // Compute breakpoints.
                    final NumberRange range = category.getRange();
                    final double    minimum = range.getMinimum(true);
                    final double    maximum = range.getMaximum(true);
                    final float   sourceMin = (float) minimum;
                    final float   sourceMax = (float) maximum;
                    final float   targetMin = (float)(minimum * scale + offset);
                    final float   targetMax = (float)(maximum * scale + offset);
                    assert sourceMin <= sourceMax : range;
                    if (Math.abs(minimum - expectedSource) <= EPS) {
                        if (Math.abs(targetMin - expectedTarget) <= EPS) {
                            // This breakpoint is identical to the previous one. Do not
                            // duplicate; overwrites the previous one since this one is
                            // likely to be more accurate.
                            jbp--;
                        } else {
                            // Found a discontinuity!!! The "piecewise" operation is not really
                            // designed for such case. The behavior between the last breakpoint
                            // and the current one may not be what the user expected.
                            assert sourceBreakpoints[jbp-1] < sourceMin : expectedSource;
                            if (CONSERVATIVE_PIECEWISE) {
                                canPiecewise = false;
                            }
                        }
                    } else if (j!=0) {
                        // Found a gap between the last category and the current one. The
                        // "piecewise" operation may not behave as the user expected  for
                        // sample values falling in this gap.
                        assert !(expectedSource > sourceMin) : expectedSource;
                        if (CONSERVATIVE_PIECEWISE) {
                            canPiecewise = false;
                        }
                    }
                    sourceBreakpoints[jbp  ] = sourceMin;
                    sourceBreakpoints[jbp+1] = sourceMax;
                    targetBreakpoints[jbp  ] = targetMin;
                    targetBreakpoints[jbp+1] = targetMax;
                    jbp += 2;
                    expectedSource = range.getMaximum(false);
                    expectedTarget = expectedSource * scale + offset;
                }
                if (false) {
                    // HACK: temporarily disabled because 'javac' 1.4.1_02 produces invalid
                    //       bytecode. This bug is fixed in 'java' 1.4.2-beta. Furthermore,
                    //       the "piecewise" operation throws an ArrayIndexOutOfBoundsException
                    //       in JAI code for an unknow reason...
                    breakpoints[i][0] = sourceBreakpoints = XArray.resize(sourceBreakpoints, jbp);
                    breakpoints[i][1] = targetBreakpoints = XArray.resize(targetBreakpoints, jbp);
                    assert XArray.isSorted(sourceBreakpoints);
                } else {
                    canPiecewise = false;
                }
            }
            if (canRescale && scales!=null) {
                operation = "Rescale";
                param = param.add(scales).add(offsets);
            } else if (canPiecewise && breakpoints!=null) {
                operation = "Piecewise";
                param = param.add(breakpoints);
            }
        } catch (TransformException exception) {
            // At least one category doesn't use a linear relation.
            // Ignore the exception and fallback on the next case.
        }
        /*
         * STEP 5 - Transcode the image sample values. The "SampleTranscode" operation is
         *          registered in the org.geotools.coverage package in the GridSampleDimension
         *          class.
         */
        if (operation == null) {
            param = param.add(sampleDimensions);
            operation = "org.geotools.SampleTranscode";
        }
        if (LOGGER.isLoggable(AbstractGridCoverageProcessor.OPERATION)) {
            // Log a message using the same level than GridCoverageProcessor.
            final int        index = operation.lastIndexOf('.');
            final String shortName = (index>=0) ? operation.substring(index+1) : operation;
            final Locale    locale = getLocale();
            final LogRecord record = Resources.getResources(locale).getLogRecord(
                                     AbstractGridCoverageProcessor.OPERATION,
                                     ResourceKeys.SAMPLE_TRANSCODE_$3, new Object[] {
                                     getName().toString(locale),
                                     new Integer(geo ? 1 : 0), shortName});
            record.setSourceClassName("GridCoverage");
            record.setSourceMethodName("geophysics");
            LOGGER.log(record);
        }
        return new GridCoverage2D(getName(), JAI.create(operation, param, hints),
                                  crs, gridGeometry, null, targetBands,
                                  new GridCoverage[]{this}, null);
    }

    /**
     * Constructs the {@link PlanarImage} from the {@linkplain SerializableRenderedImage}
     * after deserialization.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            /*
             * Set the 'image' field using reflection, because this field is final.
             * This is a legal usage for deserialization according Field.set(...)
             * documentation in J2SE 1.5.
             */
            final Field field = GridCoverage2D.class.getDeclaredField("image");
            field.setAccessible(true);
            field.set(this, PlanarImage.wrapRenderedImage(serializedImage));
        } catch (NoSuchFieldException cause) {
            InvalidClassException e = new InvalidClassException(cause.getLocalizedMessage());
            e.initCause(cause);
            throw e;
        } catch (IllegalAccessException cause) {
            InvalidObjectException e = new InvalidObjectException(cause.getLocalizedMessage());
            e.initCause(cause);
            throw e;
        }
    }

    /**
     * Serialize this grid coverage. Before serialization, a {@linkplain SerializableRenderedImage
     * serializable rendered image} is created if it was not already done.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        if (serializedImage == null) {
            RenderedImage source = image;
            while (source instanceof RenderedImageAdapter) {
                source = ((RenderedImageAdapter) source).getWrappedImage();
            }
            if (source instanceof SerializableRenderedImage) {
                serializedImage = (SerializableRenderedImage) source;
            } else {
                serializedImage = new SerializableRenderedImage(source, false, null,
                                                                "gzip", null, null);
            }
        }
        out.defaultWriteObject();
    }
}
