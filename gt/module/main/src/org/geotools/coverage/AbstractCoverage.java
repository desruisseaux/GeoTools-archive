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
package org.geotools.coverage;

// Images
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.ParameterBlock;

// Geometry
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Miscellaneous
import java.util.Map;
import java.util.Vector;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.Collections;
import java.lang.reflect.Array;
import java.io.IOException;
import java.io.StringWriter;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.TiledImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.ImageFunction;
import javax.media.jai.PropertySource;
import javax.media.jai.PropertySourceImpl;
import javax.media.jai.util.CaselessStringKey;           // For Javadoc
import javax.media.jai.operator.ImageFunctionDescriptor; // For Javadoc
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;                // For javadoc
import org.opengis.coverage.grid.GridGeometry;                // For javadoc
import org.opengis.coverage.processing.GridCoverageProcessor; // For javadoc
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.util.SimpleInternationalString;

// Resources
import org.geotools.io.LineWriter;
import org.geotools.resources.XArray;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.geometry.XAffineTransform;


/**
 * Base class of all coverage type. The essential property of coverage is to be able
 * to generate a value for any point within its domain.  How coverage is represented
 * internally is not a concern. For example consider the following different internal
 * representations of coverage:
 *
 *  <ul>
 *    <li>A coverage may be represented by a set of polygons which exhaustively
 *        tile a plane (that is each point on the plane falls in precisely one polygon).
 *        The value returned by the coverage for a point is the value of an attribute of
 *        the polygon that contains the point.</li>
 *    <li>A coverage may be represented by a grid of values.
 *        The value returned by the coverage for a point is that of the grid value
 *        whose location is nearest the point.</li>
 *    <li>Coverage may be represented by a mathematical function.
 *        The value returned by the coverage for a point is just the return value
 *        of the function when supplied the coordinates of the point as arguments.</li>
 *    <li>Coverage may be represented by combination of these.
 *        For example, coverage may be represented by a combination of mathematical
 *        functions valid over a set of polynomials.</LI>
 * </ul>
 *
 * {@linkplain org.geotools.coverage.grid.GridCoverage2D Grid coverages} are typically 2D while
 * other coverages may be 3D or 4D. The dimension of grid coverage may be queried in many ways:
 *
 * <ul>
 *   <li><code>{@link #getCoordinateReferenceSystem}.getCoordinateSystem().getDimension();</code></li>
 *   <li><code>{@link #getDimensionNames}.length;</code></li>
 *   <li><code>{@link #getDimension};</code></li>
 * </ul>
 *
 * All those methods should returns the same number. Note that the dimension
 * of grid coverage <strong>is not the same</strong> than the number of sample dimensions.
 * The later may be better understood as the number of bands for 2D grid coverage.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractCoverage extends PropertySourceImpl implements Coverage {
    /**
     * The set of default axis name.
     */
    private static final String[] DIMENSION_NAMES = {"x", "y", "z", "t"};

    /**
     * The sequence of string to returns when there is no metadata.
     */
    private static final String[] NO_PROPERTIES = new String[0];

    /**
     * The sample dimension to make visible by {@link #getRenderableImage}.
     */
    private static final int VISIBLE_BAND = 0;

    /**
     * The coverage name.
     */
    private final InternationalString name;
    
    /**
     * The coordinate reference system, or {@code null} if there is none.
     */
    protected final CoordinateReferenceSystem crs;
    
    /**
     * Constructs a coverage using the specified coordinate reference system. If the
     * coordinate reference system is {@code null}, then the subclasses must
     * override {@link #getDimension()}.
     *
     * @param name The coverage name.
     * @param crs The coordinate reference system. This specifies the coordinate
     *        system used when accessing a coverage or grid coverage with the
     *        {@code evaluate(...)} methods.
     * @param source The source for this coverage, or {@code null} if none.
     *        Source may be (but is not limited to) a {@link PlanarImage} or an
     *        other {@code AbstractCoverage} object.
     * @param properties The set of properties for this coverage, or {@code null} if there is none.
     *        "Properties" in <cite>Java Advanced Imaging</cite> is what OpenGIS calls "Metadata".
     *        Keys are {@link String} objects ({@link CaselessStringKey} are accepted as well),
     *        while values may be any {@link Object}.
     */
    protected AbstractCoverage(final CharSequence             name,
                               final CoordinateReferenceSystem crs,
                               final PropertySource         source,
                               final Map                properties)
    {
        super(properties, source);
        this.name = SimpleInternationalString.wrap(name);
        this.crs  = crs;
    }
    
    /**
     * Constructs a new coverage with the same parameters than the specified coverage.
     * <strong>Note:</strong> This constructor keeps a strong reference to the source
     * coverage (through {@link PropertySourceImpl}). In many cases, it is not a problem
     * since {@link GridCoverage} will retains a strong reference to its source anyway.
     *
     * @param name The name for this coverage, or {@code null} for the same than {@code coverage}.
     * @param coverage The source coverage.
     */
    protected AbstractCoverage(final CharSequence name, final Coverage coverage) {
        super(null, (coverage instanceof PropertySource) ? (PropertySource) coverage : null);
        final InternationalString n = SimpleInternationalString.wrap(name);
        if (coverage instanceof AbstractCoverage) {
            final AbstractCoverage source = (AbstractCoverage) coverage;
            this.name = (n!=null) ? n : source.name;
            this.crs  = source.crs;
        } else {
            this.name = (n!=null) ? n : new SimpleInternationalString(coverage.toString());
            this.crs  = coverage.getCoordinateReferenceSystem();
        }
    }
    
    /**
     * Returns the coverage name. The default implementation returns the name
     * specified at construction time.
     */
    public InternationalString getName() {
        return name;
    }

    /**
     * Returns the source data for a coverage. The default implementation returns an empty list.
     */
    public List getSources() {
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Returns the coordinate reference system. This specifies the CRS used when accessing a
     * coverage or grid coverage with the {@code evaluate(...)} methods. It is also the coordinate
     * reference system of the coordinates used with the math transform
     * {@link GridGeometry#getGridToCoordinateSystem}. A grid coverage can be accessed
     * (re-projected) with new coordinate reference system with the {@link GridCoverageProcessor}
     * component. In this case, a new instance of a grid coverage is created.
     * <br><br>
     * Note: If a coverage does not have an associated coordinate reference system, the returned
     * value will be {@code null}. The {@link GridGeometry#getGridToCoordinateSystem}) attribute
     * should also be {@code null} if the coordinate reference system is {@code null}.
     *
     * @return The coordinate reference system, or {@code null} if this coverage
     *         does not have an associated CRS.
     *
     * @see GridGeometry#getGridToCoordinateSystem
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }
    
    /**
     * Returns the bounding box for the coverage domain in coordinate reference system coordinates.
     * May be null if this coverage has no associated coordinate reference system.
     * For grid coverages, the grid cells are centered on each grid coordinate.
     * The envelope for a 2-D grid coverage includes the following corner positions.
     *
     * <blockquote><pre>
     * (Minimum row - 0.5, Minimum column - 0.5) for the minimum coordinates
     * (Maximum row - 0.5, Maximum column - 0.5) for the maximum coordinates
     * </pre></blockquote>
     *
     * The default implementation returns the domain of validity of the CRS, if there is one.
     *
     * @return The bounding box for the coverage domain in coordinate system coordinates.
     */
    public Envelope getEnvelope() {
        return CRSUtilities.getEnvelope(crs);
    }
    
    /**
     * Returns the dimension of the grid coverage. The default implementation
     * returns the dimension of the underlying {@linkplain CoordinateReferenceSystem
     * coordinate reference system}.
     *
     * @return The number of dimensions of this coverage.
     */
    public int getDimension() {
        return crs.getCoordinateSystem().getDimension();
    }
    
    /**
     * Returns the names of each dimension in this coverage. Typically these names
     * are "x", "y", "z" and "t". The number of items in the sequence is the number
     * of dimensions in the coverage. Grid coverages are typically 2D (<var>x</var>,
     * <var>y</var>) while other coverages may be 3D (<var>x</var>, <var>y</var>,
     * <var>z</var>) or 4D (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>).
     * The {@linkplain #getDimension number of dimensions} of the coverage is the
     * number of entries in the list of dimension names.
     *
     * The default implementation ask for {@linkplain CoordinateSystem coordinate system} axis
     * names, or returns "x", "y"... if this coverage has no CRS.
     *
     * @return The names of each dimension. The array's length is equals to {@link #getDimension}.
     */
    public InternationalString[] getDimensionNames() {
        if (crs != null) {
            final CoordinateSystem cs = crs.getCoordinateSystem();
            final InternationalString[] names = new InternationalString[cs.getDimension()];
            for (int i=0; i<names.length; i++) {
                names[i] = new SimpleInternationalString(cs.getAxis(i).getName().getCode());
            }
            return names;
        } else {
            final InternationalString[] names = (InternationalString[])
                    XArray.resize(DIMENSION_NAMES, getDimension());
            for (int i=DIMENSION_NAMES.length; i<names.length; i++) {
                names[i] = new SimpleInternationalString("dim"+(i+1));
            }
            return names;
        }
    }

    /**
     * Returns the names of each dimension in this coverage.
     *
     * @deprecated Replaced by {@link #getDimensionNames()}.
     */
    public final String[] getDimensionNames(final Locale locale) {
        final InternationalString[] inter = getDimensionNames();
        final String[] names = new String[inter.length];
        for (int i=0; i<names.length; i++) {
            names[i] = inter[i].toString(locale);
        }
        return names;
    }

    /**
     * Returns a localized error message the specified array.
     */
    private static String formatErrorMessage(final Object array) {
        String text = "<null>";
        if (array != null) {
            Class type = array.getClass();
            if (type.isArray()) {
                type = type.getComponentType();
            }
            text = Utilities.getShortName(type);
        }
        return Resources.format(ResourceKeys.ERROR_CANT_CONVERT_FROM_TYPE_$1, text);
    }
    
    /**
     * Returns a sequence of boolean values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The CRS of the point is the same as the grid
     * coverage {@linkplain #getCoordinateReferenceSystem coordinate reference system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates. This exception may also be
     *         throws if the coverage data type can't be converted to <code>boolean</code> by an
     *         identity or widening conversion. Subclasses may relax this constraint if appropriate.
     */
    public boolean[] evaluate(final DirectPosition coord, boolean[] dest)
            throws CannotEvaluateException
    {
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            if (dest == null) {
                dest = new boolean[length];
            }
            for (int i=0; i<length; i++) {
                dest[i] = Array.getBoolean(array, i);
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(array), exception);
        }
        return dest;
    }

    /**
     * Returns a sequence of byte values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the
     * point is the same as the coverage {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates. This exception may also be
     *         throws if the coverage data type can't be converted to <code>byte</code> by an
     *         identity or widening conversion. Subclasses may relax this constraint if appropriate.
     */
    public byte[] evaluate(final DirectPosition coord, byte[] dest)
            throws CannotEvaluateException
    {
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            if (dest == null) {
                dest = new byte[length];
            }
            for (int i=0; i<length; i++) {
                dest[i] = Array.getByte(array, i);
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(array), exception);
        }
        return dest;
    }

    /**
     * Returns a sequence of integer values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the
     * point is the same as the coverage {@linkplain #getCoordinateReferenceSystem
     * coordinate reference system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates. This exception may also be
     *         throws if the coverage data type can't be converted to <code>int</code> by an
     *         identity or widening conversion. Subclasses may relax this constraint if appropriate.
     */
    public int[] evaluate(final DirectPosition coord, int[] dest)
            throws CannotEvaluateException
    {
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            if (dest == null) {
                dest = new int[length];
            }
            for (int i=0; i<length; i++) {
                dest[i] = Array.getInt(array, i);
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(array), exception);
        }
        return dest;
    }

    /**
     * Returns a sequence of float values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The CRS of the point is the same as the coverage
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system}.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates. This exception may also be
     *         throws if the coverage data type can't be converted to <code>float</code> by an
     *         identity or widening conversion. Subclasses may relax this constraint if appropriate.
     */
    public float[] evaluate(final DirectPosition coord, float[] dest)
            throws CannotEvaluateException
    {
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            if (dest == null) {
                dest = new float[length];
            }
            for (int i=0; i<length; i++) {
                dest[i] = Array.getFloat(array, i);
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(array), exception);
        }
        return dest;
    }
    
    /**
     * Returns a sequence of double values for a given point in the coverage.
     * A value for each sample dimension is included in the sequence. The default interpolation
     * type used when accessing grid values for points which fall between grid cells is
     * nearest neighbor. The CRS of the point is the same as the grid coverage
     * coordinate system.
     *
     * @param  coord The coordinate point where to evaluate.
     * @param  dest An array in which to store values, or {@code null} to create a new array.
     * @return The {@code dest} array, or a newly created array if {@code dest} was null.
     * @throws CannotEvaluateException if the values can't be computed at the specified coordinate.
     *         More specifically, {@link PointOutsideCoverageException} is thrown if the evaluation
     *         failed because the input point has invalid coordinates. This exception may also be
     *         throws if the coverage data type can't be converted to <code>double</code> by an
     *         identity or widening conversion. Subclasses may relax this constraint if appropriate.
     */
    public double[] evaluate(DirectPosition coord, double[] dest)
            throws CannotEvaluateException
    {
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            if (dest == null) {
                dest = new double[length];
            }
            for (int i=0; i<length; i++) {
                dest[i] = Array.getDouble(array, i);
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(array), exception);
        }
        return dest;
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
        return new Renderable(xAxis, yAxis);
    }




    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////     RenderableImage / ImageFunction     ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * A view of a {@linkplain AbstractCoverage coverage} as a renderable image. Renderable images
     * allow interoperability with <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>
     * for a two-dimensional slice of a coverage (which may or may not be a
     * {@linkplain org.geotools.coverage.grid.GridCoverage2D grid coverage}).
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @see AbstractCoverage#getRenderableImage
     */
    protected class Renderable extends PropertySourceImpl implements RenderableImage, ImageFunction {
        /**
         * The two dimensional view of the coverage's envelope.
         */
        private final Rectangle2D bounds;

        /**
         * Dimension to use for <var>x</var> axis.
         */
        protected final int xAxis;

        /**
         * Dimension to use for <var>y</var> axis.
         */
        protected final int yAxis;

        /**
         * A coordinate point where to evaluate the function. The point dimension is equals to
         * the {@linkplain AbstractCoverage#getDimension coverage's dimension}. The {@linkplain #xAxis
         * x} and {@link #yAxis y} ordinates will be ignored, since they will vary for each pixel
         * to be evaluated. Other ordinates, if any, should be set to a fixed value. For example
         * a coverage may be three-dimensional, where the third dimension is the time axis. In
         * such case, <code>coordinate.ord[2]</code> should be set to the point in time where
         * to evaluate the coverage. By default, all ordinates are initialized to 0. Subclasses
         * should set the desired values in their constructor if needed.
         */
        protected final GeneralDirectPosition coordinate = new GeneralDirectPosition(getDimension());

        /**
         * Constructs a renderable image.
         *
         * @param  xAxis Dimension to use for <var>x</var> axis.
         * @param  yAxis Dimension to use for <var>y</var> axis.
         */
        public Renderable(final int xAxis, final int yAxis) {
            super(null, AbstractCoverage.this);
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            final Envelope envelope = getEnvelope();
            bounds = new Rectangle2D.Double(envelope.getMinimum(xAxis),
                                            envelope.getMinimum(yAxis),
                                            envelope.getLength (xAxis),
                                            envelope.getLength (yAxis));
        }

        /**
         * Returns {@code null} to indicate that no source information is available.
         */
        public Vector getSources() {
            return null;
        }

        /**
         * Returns {@code true} if successive renderings with the same arguments may
         * produce different results. The default implementation returns {@code false}.
         *
         * @see org.geotools.coverage.grid.GridCoverage2D#isDataEditable
         */
        public boolean isDynamic() {
            return false;
        }

        /**
         * Returns {@code false} since values are not complex.
         */
        public boolean isComplex() {
            return false;
        }

        /**
         * Gets the width in coverage coordinate space.
         *
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getWidth() {
            return (float)bounds.getWidth();
        }
        
        /**
         * Gets the height in coverage coordinate space.
         *
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getHeight() {
            return (float)bounds.getHeight();
        }
        
        /**
         * Gets the minimum <var>X</var> coordinate of the rendering-independent image data.
         * This is the {@linkplain AbstractCoverage#getEnvelope coverage's envelope} minimal value
         * for the {@linkplain #xAxis x axis}.
         *
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getMinX() {
            return (float)bounds.getX();
        }
        
        /**
         * Gets the minimum <var>Y</var> coordinate of the rendering-independent image data.
         * This is the {@linkplain AbstractCoverage#getEnvelope coverage's envelope} minimal value
         * for the {@linkplain #yAxis y axis}.
         *
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getMinY() {
            return (float)bounds.getY();
        }
        
        /**
         * Returns a rendered image with a default width and height in pixels.
         *
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createDefaultRendering() {
            return createScaledRendering(512, 0, null);
        }
        
        /**
         * Creates a rendered image with width <code>width</code> and height <code>height</code>
         * in pixels. If <code>width</code> is 0, it will be computed automatically from
         * <code>height</code>. Conversely, if <code>height</code> is 0, il will be computed
         * automatically from <code>width</code>.
         *
         * The default implementation creates a render context with {@link #createRenderContext}
         * and invokes {@link #createRendering(RenderContext)}.
         *
         * @param  width  The width of rendered image in pixels, or 0.
         * @param  height The height of rendered image in pixels, or 0.
         * @param  hints  Rendering hints, or {@code null}.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createScaledRendering(int width, int height,
                                                   final RenderingHints hints)
        {
            final double boundsWidth  = bounds.getWidth();
            final double boundsHeight = bounds.getHeight();
            if (!(width > 0)) { // Use '!' in order to catch NaN
                if (!(height > 0)) {
                    throw new IllegalArgumentException(Resources.format(
                             ResourceKeys.ERROR_UNSPECIFIED_IMAGE_SIZE));
                }
                width = (int)Math.round(height * (boundsWidth/boundsHeight));
            } else if (!(height > 0)) {
                height = (int)Math.round(width * (boundsHeight/boundsWidth));
            }
            return createRendering(createRenderContext(new Rectangle(0, 0, width, height), hints));
        }
        
        /**
         * Creates a rendered image using a given render context. This method will uses
         * an "{@link ImageFunctionDescriptor ImageFunction}" operation if possible
         * (i.e. if the area of interect is rectangular and the affine transform contains
         * only translation and scale coefficients).
         *
         * @param  context The render context to use to produce the rendering.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createRendering(final RenderContext context) {
            final AffineTransform crsToGrid = context.getTransform();
            final Shape area = context.getAreaOfInterest();
            final Rectangle gridBounds;
            if (true) {
                /*
                 * Compute the grid bounds for the coverage bounds (or the area of interest).
                 * The default implementation of Rectangle uses Math.floor and Math.ceil for
                 * computing a box which contains fully the Rectangle2D. But in our particular
                 * case, we really want to round toward the nearest integer.
                 */
                final Rectangle2D bounds = XAffineTransform.transform(crsToGrid,
                                           (area!=null) ? area.getBounds2D() : this.bounds, null);
                final int xmin = (int)Math.round(bounds.getMinX());
                final int ymin = (int)Math.round(bounds.getMinY());
                final int xmax = (int)Math.round(bounds.getMaxX());
                final int ymax = (int)Math.round(bounds.getMaxY());
                gridBounds = new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            }
            /*
             * Computes some properties of the image to be created.
             */
            final Dimension       tileSize = ImageUtilities.toTileSize(gridBounds.getSize());
            final GridSampleDimension band = GridSampleDimension.wrap(getSampleDimension(VISIBLE_BAND));
            final ColorModel    colorModel = band.getColorModel(VISIBLE_BAND, getNumSampleDimensions());
            final SampleModel  sampleModel = colorModel.createCompatibleSampleModel(
                                                           tileSize.width, tileSize.height);
            /*
             * If the image can be created using the ImageFunction operation, do it.
             * It allow JAI to defer the computation until a tile is really requested.
             */
            final PlanarImage image;
            if ((area==null || area instanceof Rectangle2D) &&
                crsToGrid.getShearX()==0 && crsToGrid.getShearY()==0)
            {
                image = JAI.create("ImageFunction",
                       new ParameterBlock()
                                .add(this)                           // The functional description
                                .add(gridBounds.width)                     // The image width
                                .add(gridBounds.height)                    // The image height
                                .add((float)(1/crsToGrid.getScaleX()))     // The X scale factor
                                .add((float)(1/crsToGrid.getScaleY()))     // The Y scale factor
                                .add((float)   crsToGrid.getTranslateX())  // The X translation
                                .add((float)   crsToGrid.getTranslateY()), // The Y translation
                       new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout()
                                .setMinX       (gridBounds.x)
                                .setMinY       (gridBounds.y)
                                .setTileWidth  (tileSize.width)
                                .setTileHeight (tileSize.height)
                                .setSampleModel(sampleModel)
                                .setColorModel (colorModel)));
            } else {
                /*
                 * Creates immediately a rendered image using a given render context. This block
                 * is run when the image can't be created with JAI's ImageFunction operator, for
                 * example because the affine transform swap axis or because there is an area of
                 * interest.
                 */
                // Clones the coordinate point in order to allow multi-thread invocation.
                final GeneralDirectPosition coordinate = new GeneralDirectPosition(this.coordinate);
                final TiledImage tiled = new TiledImage(gridBounds.x, gridBounds.y,
                                                        gridBounds.width, gridBounds.height,
                                                        0, 0, sampleModel, colorModel);
                final Point2D.Double point2D = new Point2D.Double();
                final int     numBands = tiled.getNumBands();
                final double[] samples = new double[numBands];
                final double[] padNaNs = new double[numBands];
                Arrays.fill(padNaNs, Double.NaN);
                final WritableRectIter iterator = RectIterFactory.createWritable(tiled, gridBounds);
                if (!iterator.finishedLines()) try {
                    int y=gridBounds.y; do {
                        iterator.startPixels();
                        if (!iterator.finishedPixels()) {
                            int x=gridBounds.x; do {
                                point2D.x = x;
                                point2D.y = y;
                                crsToGrid.inverseTransform(point2D, point2D);
                                if (area==null || area.contains(point2D)) {
                                    coordinate.ordinates[xAxis] = point2D.x;
                                    coordinate.ordinates[yAxis] = point2D.y;
                                    iterator.setPixel(evaluate(coordinate, samples));
                                } else {
                                    iterator.setPixel(padNaNs);
                                }
                                x++;
                            }
                            while (!iterator.nextPixelDone());
                            assert(x == gridBounds.x + gridBounds.width);
                            y++;
                        }
                    }
                    while (!iterator.nextLineDone());
                    assert(y == gridBounds.y + gridBounds.height);
                }
                catch (NoninvertibleTransformException exception) {
                    final IllegalArgumentException e= new IllegalArgumentException("RenderContext");
                    e.initCause(exception);
                    throw e;
                }
                image = tiled;
            }
            /*
             * Add a 'gridToCoordinateSystem' property to the image. This is an important
             * information for constructing a GridCoverage from this image later.
             */
            try {
                image.setProperty("gridToCoordinateSystem", crsToGrid.createInverse());
            } catch (NoninvertibleTransformException exception) {
                // Can't add the property. Too bad, the image has been created anyway.
                // Maybe the user know what he is doing...
                Utilities.unexpectedException("org.geotools.coverage",
                        "AbstractCoverage.Renderable", "createRendering", exception);
            }
            return image;
        }
        
        /**
         * Initialize a render context with an affine transform that maps the coverage envelope
         * to the specified destination rectangle. The affine transform mays swap axis in order
         * to normalize their order (i.e. make them appear in the (<var>x</var>,<var>y</var>)
         * order), so that the image appears properly oriented when rendered.
         *
         * @param  gridBounds The two-dimensional destination rectangle.
         * @param  hints The rendering hints, or {@code null} if none.
         * @return A render context initialized with an affine transform from the coverage
         *         to the grid coordinate system. This transform is the inverse of
         *         {@link org.geotools.coverage.grid.GridGeometry2D#getGridToCoordinateSystem2D}.
         *
         * @see org.geotools.coverage.grid.GridGeometry2D#getGridToCoordinateSystem2D
         */
        protected RenderContext createRenderContext(final Rectangle2D gridBounds,
                                                    final RenderingHints hints)
        {
            final GeneralMatrix matrix;
            final GeneralEnvelope srcEnvelope = new GeneralEnvelope(bounds);
            final GeneralEnvelope dstEnvelope = new GeneralEnvelope(gridBounds);
            if (crs != null) {
                final CoordinateSystem cs = crs.getCoordinateSystem();
                final AxisDirection[] axis = new AxisDirection[] {
                    cs.getAxis(xAxis).getDirection(),
                    cs.getAxis(yAxis).getDirection()
                };
                final AxisDirection[] normalized = (AxisDirection[]) axis.clone();
                if (false) {
                    // Normalize axis: Is it really a good idea?
                    // We should provide a rendering hint for configuring that.
                    Arrays.sort(normalized);
                    for (int i=normalized.length; --i>=0;) {
                        normalized[i] = normalized[i].absolute();
                    }
                }
                normalized[1] = normalized[1].opposite(); // Image's Y axis is downward.
                matrix = new GeneralMatrix(srcEnvelope, axis, dstEnvelope, normalized);
            } else {
                matrix = new GeneralMatrix(srcEnvelope, dstEnvelope);
            }
            return new RenderContext(matrix.toAffineTransform2D(), hints);
        }

        /**
         * Returns the number of elements per value at each position. This is the maximum
         * value plus 1 allowed in <code>getElements(...)</code> methods invocation. The
         * default implementation returns the number of sample dimensions in the coverage.
         */
        public int getNumElements() {
            return getNumSampleDimensions();
        }

        /**
         * Returns all values of a given element for a specified set of coordinates.
         * This method is automatically invoked at rendering time for populating an
         * image tile, providing that the rendered image is created using the
         * "{@link ImageFunctionDescriptor ImageFunction}" operator and the image
         * type is not <code>double</code>. The default implementation invokes
         * {@link AbstractCoverage#evaluate(DirectPosition,float[])} recursively.
         */
        public void getElements(final float startX, final float startY,
                                final float deltaX, final float deltaY,
                                final int   countX, final int   countY, final int element,
                                final float[] real, final float[] imag)
        {
            int index = 0;
            float[] buffer = null;
            // Clones the coordinate point in order to allow multi-thread invocation.
            final GeneralDirectPosition coordinate = new GeneralDirectPosition(this.coordinate);
            coordinate.ordinates[1] = startY;
            for (int j=0; j<countY; j++) {
                coordinate.ordinates[0] = startX;
                for (int i=0; i<countX; i++) {
                    buffer = evaluate(coordinate, buffer);
                    real[index++] = buffer[element];
                    coordinate.ordinates[0] += deltaX;
                }
                coordinate.ordinates[1] += deltaY;
            }
        }

        /**
         * Returns all values of a given element for a specified set of coordinates.
         * This method is automatically invoked at rendering time for populating an
         * image tile, providing that the rendered image is created using the
         * "{@link ImageFunctionDescriptor ImageFunction}" operator and the image type
         * is <code>double</code>. The default implementation invokes
         * {@link AbstractCoverage#evaluate(DirectPosition,double[])} recursively.
         */
        public void getElements(final double startX, final double startY,
                                final double deltaX, final double deltaY,
                                final int    countX, final int    countY, final int element,
                                final double[] real, final double[] imag)
        {
            int index = 0;
            double[] buffer = null;
            // Clones the coordinate point in order to allow multi-thread invocation.
            final GeneralDirectPosition coordinate =  new GeneralDirectPosition(this.coordinate);
            coordinate.ordinates[1] = startY;
            for (int j=0; j<countY; j++) {
                coordinate.ordinates[0] = startX;
                for (int i=0; i<countX; i++) {
                    buffer = evaluate(coordinate, buffer);
                    real[index++] = buffer[element];
                    coordinate.ordinates[0] += deltaX;
                }
                coordinate.ordinates[1] += deltaY;
            }
        }
    }

    /**
     * List of metadata keywords for a coverage. If no metadata is available, the sequence
     * will be empty. The default implementation gets the list of metadata names from the
     * {@link #getPropertyNames()} method.
     *
     * @return the list of metadata keywords for a coverage.
     */
    public String[] getMetadataNames() {
        final String[] list = getPropertyNames();
        return (list != null) ? list : NO_PROPERTIES;
    }

    /**
     * Retrieve the metadata value for a given metadata name. The default implementation query
     * the {@link #getProperty(String)} method.
     *
     * @param name Metadata keyword for which to retrieve data.
     * @return the metadata value for a given metadata name.
     * @throws MetadataNameNotFoundException if there is no value for the specified metadata name.
     */
    public String getMetadataValue(final String name) throws MetadataNameNotFoundException {
        final Object value = getProperty(name);
        if (value == java.awt.Image.UndefinedProperty) {
            throw new MetadataNameNotFoundException(Resources.format(
                    ResourceKeys.ERROR_UNDEFINED_PROPERTY_$1, name));
        }
        return (value!=null) ? value.toString() : null;
    }

    /**
     * Returns the default locale for logging, error messages, <cite>etc.</cite>.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Provides a hint that a coverage will no longer be accessed from a reference in user space.
     * The results are equivalent to those that occur when the program loses its last reference to
     * this coverage, the garbage collector discovers this, and finalize is called. This can be
     * used as a hint in situations where waiting for garbage collection would be overly
     * conservative. The results of referencing a coverage after a call to <code>dispose()</code>
     * are undefined.
     *
     * @see PlanarImage#dispose
     */
    public void dispose() {
        // To be overriden by subclasses.
        // Note: implementing this method in GridCoverage is tricky. We must ensure that:
        //       1) The PlanarImage is not used by somebody else (i.e. is not a user supplied
        //          image, or the user didn't got a reference with getRenderedImage()).
        //       2) If the image is the result of a GridCoverageProcessor operation, it must
        //          removes itself from the WeakValueHashMap.
    }
    
    /**
     * Returns a string représentation of this coverage. This string is
     * for debugging purpose only and may change in future version.
     */
    public String toString() {
        final StringWriter buffer = new StringWriter();
        buffer.write(Utilities.getShortClassName(this));
        buffer.write("[\"");
        buffer.write(String.valueOf(getName()));
        buffer.write('"');
        final Envelope envelope = getEnvelope();
        if (envelope != null) {
            buffer.write(", ");
            buffer.write(envelope.toString());
        }
        if (crs != null) {
            buffer.write(", ");
            buffer.write(Utilities.getShortClassName(crs));
            buffer.write("[\"");
            buffer.write(crs.getName().getCode());
            buffer.write("\"]");
        }
        buffer.write(']');
        final String lineSeparator = System.getProperty("line.separator", "\n");
        final LineWriter filter = new LineWriter(buffer, lineSeparator+"    ");
        final int n = getNumSampleDimensions();
        try {
            filter.write(lineSeparator);
            for (int i=0; i<n; i++) {
                filter.write(getSampleDimension(i).toString());
            }
            filter.flush();
        } catch (IOException exception) {
            // Should not happen
            throw new AssertionError(exception);
        }
        return buffer.toString();
    }
}
