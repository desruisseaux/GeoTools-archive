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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
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
import org.geotools.coverage.processing.AbstractProcessor;
import org.geotools.factory.Hints;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Logging;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.image.CoverageUtilities;
import org.geotools.util.NumberRange;


/**
 * Basic access to grid data values backed by a two-dimensional
 * {@linkplain RenderedImage rendered image}. Each band in an image is represented as a
 * {@linkplain GridSampleDimension sample dimension}.
 * <p>
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
 * <p>
 * <strong>Serialization note:</strong><br>
 * Because it is serializable, {@code GridCoverage2D} can be included as method argument or as
 * return type in <cite>Remote Method Invocation</cite> (RMI). However, the pixel data are not
 * sent during serialization. Instead, the image data are transmitted "on-demand" using socket
 * communications. This mechanism is implemented using JAI {@link SerializableRenderedImage}
 * class. While serialization (usually on server side) should work on J2SE 1.4 and above,
 * deserialization (usually on client side) of {@code GridCoverage2D} instances requires J2SE 1.5.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverage2D extends AbstractGridCoverage implements RenderedCoverage {
    /**
     * For compatibility during cross-version serialization.
     */
    private static final long serialVersionUID = 667472989475027853L;

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
     * A grid coverage using the sample dimensions {@code GridSampleDimension.inverse}.
     * This object is constructed and returned by {@link #geophysics}. Constructed when
     * first needed. May appears also in the {@link #sources} list.
     */
    private transient GridCoverage2D inverse;
    
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
        image            = coverage.image;
        gridGeometry     = coverage.gridGeometry;
        sampleDimensions = coverage.sampleDimensions;
        isGeophysics     = coverage.isGeophysics;
    }

    /**
     * Constructs a grid coverage with the specified envelope and sample dimensions. This
     * convenience constructor assumes that axis order in the supplied image matches exactly
     * axis order in the supplied CRS. In other words, if axis order in the supplied image is
     * (<var>column</var>,<var>row</var>) (which is the case for a majority of images), then
     * the CRS given to this constructor should probably have a
     * (<var>longitude</var>,<var>latitude</var>) or (<var>easting</var>,<var>northing</var>)
     * axis order.
     * <p>
     * An exception to the above rule applies for CRS using exactly the following axis order:
     * ({@link AxisDirection#NORTH NORTH}|{@link AxisDirection#SOUTH SOUTH},
     * {@link AxisDirection#EAST EAST}|{@link AxisDirection#WEST WEST}).
     * Example of such CRS is {@code EPSG:4326}. This convenience constructor swaps automatically
     * the axis order for such CRS.
     * <p>
     * The rules applied by this convenience constructor are heuristic. While we try to keep them
     * stable, some adjustments may be applied in future versions. For strict, determinist behavior,
     * use the constructor variant expecting a {@link MathTransform} argument instead of an
     * {@link Envelope}. The math transform allows full control on axis swapping and inversion.
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
     *
     * @deprecated Use {@link GridCoverageFactory} instead.
     */
    public GridCoverage2D(final CharSequence             name, final RenderedImage    image,
                          final CoordinateReferenceSystem crs, final Envelope      envelope,
                          final GridSampleDimension[]   bands, final GridCoverage[] sources,
                          final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, PlanarImage.wrapRenderedImage(image),
             new GridGeometry2D(new GeneralGridRange(image, envelope.getDimension()),
                                toEnvelope(envelope, crs)), bands, sources, properties);
    }

    /**
     * @deprecated Helper method for the above constructor. Also used by deprecated methods
     *             in {@link GridCoverageFactory}.
     */
    static Envelope toEnvelope(final Envelope e, final CoordinateReferenceSystem crs) {
        final GeneralEnvelope ge = new GeneralEnvelope(e);
        ge.setCoordinateReferenceSystem(crs);
        return ge;
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
     *
     * @deprecated Use {@link GridCoverageFactory} instead.
     */
    public GridCoverage2D(final CharSequence             name, final RenderedImage     image,
                          final CoordinateReferenceSystem crs, final MathTransform gridToCRS,
                          final GridSampleDimension[]   bands, final GridCoverage[]  sources,
                          final Map properties)
            throws MismatchedDimensionException, IllegalArgumentException
    {
        this(name, PlanarImage.wrapRenderedImage(image),
             new GridGeometry2D(new GeneralGridRange(image), gridToCRS, crs),
             bands, sources, properties);
    }

    /**
     * Constructs a grid coverage with the specified {@linkplain GridGeometry2D grid geometry} and
     * {@linkplain GridSampleDimension sample dimensions}. The {@linkplain Envelope envelope}
     * (including the {@linkplain CoordinateReferenceSystem coordinate reference system}) is
     * inferred from the grid geometry.
     * <p>
     * This constructor accepts an optional set of properties. "Properties" in <cite>Java Advanced
     * Imaging</cite> is what OpenGIS calls "Metadata". Keys are {@link String} objects
     * ({@link CaselessStringKey} are accepted as well), while values may be any {@link Object}.
     *
     * @param name         The grid coverage name.
     * @param image        The image.
     * @param gridGeometry The grid geometry (must contains an {@linkplain GridGeometry2D#getEnvelope
     *                     envelope} with its {@linkplain GridGeometry2D#getCoordinateReferenceSystem
     *                     coordinate reference system} and a "{@linkplain
     *                     GridGeometry2D#getGridToCoordinateSystem grid to CRS}" transform).
     * @param bands        Sample dimensions for each image band, or {@code null} for default sample
     *                     dimensions. If non-null, then this array's length must matches the number
     *                     of bands in {@code image}.
     * @param sources      The sources for this grid coverage, or {@code null} if none.
     * @param properties   The set of properties for this coverage, or {@code null} none.
     *
     * @throws IllegalArgumentException if the number of bands differs from the number of sample
     *         dimensions.
     *
     * @since 2.2
     */
    protected GridCoverage2D(final CharSequence             name,
                             final PlanarImage             image,
                                   GridGeometry2D   gridGeometry,
                             final GridSampleDimension[]   bands,
                             final GridCoverage[]        sources,
                             final Map                properties)
            throws IllegalArgumentException
    {
        super(name, gridGeometry.getCoordinateReferenceSystem(), sources, image, properties);
        this.image = image;
        /*
         * Wraps the user-suplied sample dimensions into instances of Grid2DSampleDimension. This
         * process will creates default sample dimensions if the user supplied null values. Those
         * default will be inferred from image type (integers, floats...) and range of values. If
         * an inconsistency is found in user-supplied sample dimensions, an IllegalArgumentException
         * is thrown.
         */
        sampleDimensions = new GridSampleDimension[image.getNumBands()];
        isGeophysics = Grid2DSampleDimension.create(name, image, bands, sampleDimensions);
        /*
         * Computes the grid range if it was not explicitly provided. The range will be inferred
         * from the image size, if needed. The envelope computation (if needed) requires a valid
         * 'gridToCRS' transform in the GridGeometry object. In any cases, the envelope must be
         * non-empty and its dimension must matches the coordinate reference system's dimension.
         */
        final int dimension = crs.getCoordinateSystem().getDimension();
        if (!gridGeometry.isDefined(GridGeometry2D.GRID_RANGE)) {
            final GridRange r = new GeneralGridRange(image, dimension);
            if (gridGeometry.isDefined(GridGeometry2D.GRID_TO_CRS)) {
                gridGeometry = new GridGeometry2D(r, gridGeometry.getGridToCoordinateSystem(), crs);
            } else {
                /*
                 * If the math transform was not explicitly specified by the user, then it will be
                 * computed from the envelope. In this case, some heuristic rules are used in order
                 * to decide if we should reverse some axis directions or swap axis. 
                 */
                gridGeometry = new GridGeometry2D(r, gridGeometry.getEnvelope());
            }
        } else {
            /*
             * Makes sure that the 'gridToCRS' transform is defined.
             * An exception will be thrown otherwise.
             */
            gridGeometry.getGridToCoordinateSystem();
        }
        this.gridGeometry = gridGeometry;
        assert gridGeometry.isDefined(GridGeometry2D.CRS        |
                                      GridGeometry2D.ENVELOPE   |
                                      GridGeometry2D.GRID_RANGE |
                                      GridGeometry2D.GRID_TO_CRS);
        /*
         * Last argument checks. The image size must be consistent with the grid range
         * and the envelope must be non-empty.
         */
        final String error = checkConsistency(image, gridGeometry);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        if (dimension <= Math.max(gridGeometry.axisDimensionX,    gridGeometry.axisDimensionY)
                             || !(gridGeometry.envelope.getLength(gridGeometry.axisDimensionX) > 0)
                             || !(gridGeometry.envelope.getLength(gridGeometry.axisDimensionY) > 0))
        {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.EMPTY_ENVELOPE));
        }
    }

    /**
     * Checks if the bounding box of the specified image is consistents with the specified
     * grid geometry. If an inconsistency has been found, then an error string is returned.
     * This string will be typically used as a message in an exception to be thrown.
     * <p>
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
                return Errors.format(ErrorKeys.BAD_GRID_RANGE_$3, label,
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
        return gridGeometry.getEnvelope();
    }

    /**
     * Returns the two-dimensional bounding box for the coverage domain in coordinate reference
     * system coordinates. If the coverage envelope has more than two dimensions, only the
     * dimensions used in the underlying rendered image are returned.
     */
    public Envelope2D getEnvelope2D() {
        return gridGeometry.getEnvelope2D();
    }

    /**
     * Returns the two-dimensional part of this grid coverage CRS. This is usually (but not
     * always) identical to the {@linkplain #getCoordinateReferenceSystem full CRS}.
     *
     * @see #getCoordinateReferenceSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem2D() {
        return gridGeometry.getCoordinateReferenceSystem2D();
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
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$2,
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
     *         or {@code null} if {@code coord} is outside coverage.
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
     * <p>
     * This method may be understood as applying the JAI's {@linkplain PiecewiseDescriptor
     * piecewise} operation with breakpoints specified by the {@link Category} objects in
     * each sample dimension. However, it is more general in that the transformation specified
     * with each breakpoint doesn't need to be linear. On an implementation note, this method
     * will really try to use the first of the following operations which is found applicable:
     * <cite>identity</cite>, {@linkplain LookupDescriptor lookup}, {@linkplain RescaleDescriptor
     * rescale}, {@linkplain PiecewiseDescriptor piecewise} and in last ressort a more general
     * (but slower) <cite>sample transcoding</cite> algorithm.
     * <p>
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
        if (!CoverageUtilities.hasTransform(sampleDimensions)) {
            return inverse = this;
        }
        synchronized (this) {
            inverse = createGeophysics(geo);
            if (inverse.inverse == null) {
                inverse.inverse = this;
            } else if (inverse.inverse != this) {
                final Locale locale = getLocale();
                throw new RasterFormatException(Errors.getResources(locale).getString(
                          ErrorKeys.COVERAGE_ALREADY_BOUND_$2,
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
        final int                   visibleBand = CoverageUtilities.getVisibleBand(image);
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
        if (LOGGER.isLoggable(AbstractProcessor.OPERATION)) {
            // Log a message using the same level than grid coverage processor.
            final int        index = operation.lastIndexOf('.');
            final String shortName = (index>=0) ? operation.substring(index+1) : operation;
            final Locale    locale = getLocale();
            final LogRecord record = Logging.getResources(locale).getLogRecord(
                                     AbstractProcessor.OPERATION,
                                     LoggingKeys.SAMPLE_TRANSCODE_$3, new Object[] {
                                     getName().toString(locale),
                                     new Integer(geo ? 1 : 0), shortName});
            record.setSourceClassName("GridCoverage");
            record.setSourceMethodName("geophysics");
            LOGGER.log(record);
        }
        final PlanarImage    view    = JAI.create(operation, param, hints);
        final GridCoverage[] sources = new GridCoverage[]{this};
        return new GridCoverage2D(getName(), view, gridGeometry, targetBands, sources, null);
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
