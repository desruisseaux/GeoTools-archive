/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Management Committee (PMC)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.coverage;

// Images
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;

// Geometry
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// Miscellaneous
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

// JAI dependencies
import javax.media.jai.ImageFunction;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertySource;
import javax.media.jai.PropertySourceImpl;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIterFactory;
import javax.media.jai.iterator.WritableRectIter;
import javax.media.jai.operator.ImageFunctionDescriptor; // For Javadoc
import javax.media.jai.util.CaselessStringKey;           // For Javadoc
import javax.media.jai.widget.ScrollingImagePanel;

// OpenGIS dependencies
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.CommonPointRule;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.DomainObject;
import org.opengis.coverage.GeometryValuePair;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.PointOutsideCoverageException;    // For javadoc
import org.opengis.coverage.grid.GridCoverage;                // For javadoc
import org.opengis.coverage.grid.GridGeometry;                // For javadoc
import org.opengis.coverage.processing.GridCoverageProcessor; // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.Geometry;
import org.opengis.temporal.Period;
import org.opengis.util.InternationalString;
import org.opengis.util.Record;
import org.opengis.util.RecordType;

// Geotools dependencies
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.util.Logging;
import org.geotools.util.SimpleInternationalString;

// Resources
import org.geotools.io.LineWriter;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Base class of all coverage type. The essential property of coverage is to be able
 * to generate a value for any point within its domain. How coverage is represented
 * internally is not a concern. For example consider the following different internal
 * representations of coverage:
 * <p>
 * <ul>
 *   <li>A coverage may be represented by a set of polygons which exhaustively
 *       tile a plane (that is each point on the plane falls in precisely one
 *       polygon). The value returned by the coverage for a point is the value
 *       of an attribute of the polygon that contains the point.</li>
 *   <li>A coverage may be represented by a grid of values. The value returned by
 *       the coverage for a point is that of the grid value whose location is nearest
 *       the point.</li>
 *   <li>Coverage may be represented by a mathematical function. The value
 *       returned by the coverage for a point is just the return value of the function
 *       when supplied the coordinates of the point as arguments.</li>
 *   <li>Coverage may be represented by combination of these. For example,
 *       coverage may be represented by a combination of mathematical functions valid
 *       over a set of polynomials.</LI>
 * </ul>
 * 
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class AbstractCoverage extends PropertySourceImpl implements Coverage {
    /**
     * For compatibility during cross-version serialization.
     */
    private static final long serialVersionUID = -2989320942499746295L;

    /**
     * The set of default axis name.
     */
    private static final String[] DIMENSION_NAMES = { "x", "y", "z", "t" };

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
     * Constructs a coverage using the specified coordinate reference system. If
     * the coordinate reference system is {@code null}, then the subclasses
     * must override {@link #getDimension()}.
     * 
     * @param name
     *            The coverage name.
     * @param crs
     *            The coordinate reference system. This specifies the coordinate
     *            system used when accessing a coverage or grid coverage with
     *            the {@code evaluate(...)} methods.
     * @param source
     *            The source for this coverage, or {@code null} if none. Source
     *            may be (but is not limited to) a {@link PlanarImage} or an
     *            other {@code AbstractCoverage} object.
     * @param properties
     *            The set of properties for this coverage, or {@code null} if
     *            there is none. "Properties" in <cite>Java Advanced Imaging</cite>
     *            is what OpenGIS calls "Metadata". Keys are {@link String}
     *            objects ({@link CaselessStringKey} are accepted as well),
     *            while values may be any {@link Object}.
     */
    protected AbstractCoverage(final CharSequence             name,
                               final CoordinateReferenceSystem crs,
                               final PropertySource         source,
                               final Map                properties)
    {
        super(properties, source);
        this.name = SimpleInternationalString.wrap(name);
        this.crs = crs;
    }

    /**
     * Constructs a new coverage with the same parameters than the specified
     * coverage. <strong>Note:</strong> This constructor keeps a strong
     * reference to the source coverage (through {@link PropertySourceImpl}).
     * In many cases, it is not a problem since {@link GridCoverage} will
     * retains a strong reference to its source anyway.
     * 
     * @param name
     *            The name for this coverage, or {@code null} for the same than {@code coverage}.
     * @param coverage
     *            The source coverage.
     */
    protected AbstractCoverage(final CharSequence name, final Coverage coverage) {
        super(null, (coverage instanceof PropertySource) ? (PropertySource) coverage : null);
        final InternationalString n = SimpleInternationalString.wrap(name);
        if (coverage instanceof AbstractCoverage) {
            final AbstractCoverage source = (AbstractCoverage) coverage;
            this.name = (n != null) ? n : source.name;
            this.crs  = source.crs;
        } else {
            this.name = (n != null) ? n : new SimpleInternationalString(coverage.toString());
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
     * Returns the dimension of this coverage. This is a shortcut for
     * <code>{@linkplain #crs}.getCoordinateSystem().getDimension()</code>.
     */
    public final int getDimension() {
        return crs.getCoordinateSystem().getDimension();
    }

    /**
     * Returns the coordinate reference system to which the objects in its domain are
     * referenced. This is the CRS used when accessing a coverage or grid coverage with
     * the {@code evaluate(...)} methods. This coordinate reference system is usually
     * different than coordinate system of the grid. It is the target coordinate reference
     * system of the {@link GridGeometry#getGridToCRS gridToCRS} math transform.
     * <p>
     * Grid coverage can be accessed (re-projected) with new coordinate reference system
     * with the {@link GridCoverageProcessor} component. In this case, a new instance of
     * a grid coverage is created.
     *
     * @return The coordinate reference system used when accessing a coverage or
     *         grid coverage with the {@code evaluate(...)} methods.
     * 
     * @see org.geotools.coverage.grid.GeneralGridGeometry#getGridToCRS
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Returns the names of each dimension in this coverage. Typically these
     * names are "x", "y", "z" and "t". The number of items in the sequence is
     * the number of dimensions in the coverage. Grid coverages are typically
     * 2D (<var>x</var>, <var>y</var>) while other coverages may be
     * 3D (<var>x</var>, <var>y</var>, <var>z</var>) or
     * 4D (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>).
     * The {@linkplain #getDimension number of dimensions} of the coverage is
     * the number of entries in the list of dimension names.
     * <p>
     * The default implementation ask for
     * {@linkplain CoordinateSystem coordinate system} axis names, or returns
     * "x", "y"... if this coverage has no CRS.
     * 
     * @return The names of each dimension. The array's length is equals to
     *         {@link #getDimension}.
     *
     * @deprecated This information can be obtained from the underlying coordinate system.
     */
    public InternationalString[] getDimensionNames() {
        final InternationalString[] names;
        if (crs != null) {
            final CoordinateSystem cs = crs.getCoordinateSystem();
            names = new InternationalString[cs.getDimension()];
            for (int i=0; i<names.length; i++) {
                names[i] = new SimpleInternationalString(cs.getAxis(i).getName().getCode());
            }
        } else {
            names = (InternationalString[]) XArray.resize(DIMENSION_NAMES, getDimension());
            for (int i=DIMENSION_NAMES.length; i<names.length; i++) {
                names[i] = new SimpleInternationalString("dim" + (i + 1));
            }
        }
        return names;
    }

    /**
     * Returns the names of each dimension in this coverage.
     * 
     * @deprecated Replaced by {@link #getDimensionNames()}.
     */
    public final String[] getDimensionNames(final Locale locale) {
        final InternationalString[] inter = getDimensionNames();
        final String[] names = new String[inter.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = inter[i].toString(locale);
        }
        return names;
    }

    /**
     * Returns the bounding box for the coverage domain in coordinate reference
     * system coordinates. May be null if this coverage has no associated
     * coordinate reference system. For grid coverages, the grid cells are
     * centered on each grid coordinate. The envelope for a 2-D grid coverage
     * includes the following corner positions.
     * 
     * <blockquote><pre>
     *  (Minimum row - 0.5, Minimum column - 0.5) for the minimum coordinates
     *  (Maximum row - 0.5, Maximum column - 0.5) for the maximum coordinates
     * </pre></blockquote>
     * 
     * The default implementation returns the domain of validity of the CRS, if
     * there is one.
     * 
     * @return The bounding box for the coverage domain in coordinate system
     *         coordinates.
     */
    public Envelope getEnvelope() {
        return CRS.getEnvelope(crs);
    }

    /**
     * Returns the extent of the domain of the coverage. Extents may be specified in space,
     * time or space-time. The collection must contains at least one element.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     *
     * @todo Proposed default implementation: invokes {@link #getEnvelope}, extract
     *       the spatial and temporal parts and put them in a {@link Extent} object.
     */
    public Set/*<Extent>*/ getDomainExtents() {
        throw unsupported();
    }

    /**
     * Returns the set of domain objects in the domain.
     * The collection must contains at least one element.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     *
     * @todo Proposed default implementation: invokes {@link #getEnvelope}, extract the
     *       spatial and temporal parts, get the grid geometry and create on-the-fly a
     *       {@link DomainObject} for each cell.
     */
    public Set/*<? extends DomainObject>*/ getDomainElements() {
        throw unsupported();
    }

    /**
     * Returns the set of attribute values in the range. The range of a coverage shall be a
     * homogeneous collection of records. That is, the range shall have a constant dimension
     * over the entire domain, and each field of the record shall provide a value of the same
     * attribute type over the entire domain.
     * <p>
     * In the case of a {@linkplain DiscreteCoverage discrete coverage}, the size of the range
     * collection equals that of the {@linkplain #getDomainElements domains} collection. In other
     * words, there is one instance of {@link AttributeValues} for each instance of
     * {@link DomainObject}. Usually, these are stored values that are accessed by the
     * {@link #evaluate(DirectPosition,Collection) evaluate} operation.
     * <p>
     * In the case of a {@linkplain ContinuousCoverage continuous coverage}, there is a transfinite
     * number of instances of {@link AttributeValues} for each {@link DomainObject}. A few instances
     * may be stored as input for the {@link #evaluate(DirectPosition,Collection) evaluate}
     * operation, but most are generated as needed by that operation.
     * <p>
     * <B>NOTE:</B> ISO 19123 does not specify how the {@linkplain #getDomainElements domain}
     * and {@linkplain #getRangeElements range} associations are to be implemented. The relevant
     * data may be generated in real time, it may be held in persistent local storage, or it may
     * be electronically accessible from remote locations.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public Set/*<AttributeValues>*/ getRangeElements() {
        throw unsupported();
    }

    /**
     * Describes the range of the coverage. It consists of a list of attribute name/data type pairs.
     * A simple list is the most common form of range type, but {@code RecordType} can be used
     * recursively to describe more complex structures. The range type for a specific coverage
     * shall be specified in an application schema.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public RecordType getRangeType() {
        throw unsupported();
    }

    /**
     * Identifies the procedure to be used for evaluating the coverage at a position that falls
     * either on a boundary between geometric objects or within the boundaries of two or more
     * overlapping geometric objects. The geometric objects are either {@linkplain DomainObject
     * domain objects} or {@linkplain ValueObject value objects}.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public CommonPointRule getCommonPointRule() {
        throw unsupported();
    }

    /**
     * Returns the dictionary of <var>geometry</var>-<var>value</var> pairs that contain the
     * {@linkplain DomainObject objects} in the domain of the coverage each paired with its
     * record of feature attribute values. In the case of an analytical coverage, the operation
     * shall return the empty set.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public Set list() {
        throw unsupported();
    }

    /**
     * Returns the set of <var>geometry</var>-<var>value</var> pairs that contain
     * {@linkplain DomainObject domain objects} that lie within the specified geometry and period.
     * If {@code s} is null, the operation shall return all <var>geometry</var>-<var>value</var>
     * pairs that contain {@linkplain DomainObject domain objects} within {@code t}. If the value
     * of {@code t} is null, the operation shall return all <var>geometry</var>-<var>value</var>
     * pair that contain {@linkplain DomainObject domain objects} within {@code s}. In the case
     * of an analytical coverage, the operation shall return the empty set.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public Set select(Geometry arg0, Period arg1) {
        throw unsupported();
    }

    /**
     * Returns the sequence of <var>geometry</var>-<var>value</var> pairs that include the
     * {@linkplain DomainObject domain objects} nearest to the direct position and their
     * distances from the direction position. The sequence shall be ordered by distance from
     * the direct position, beginning with the record containing the {@linkplain DomainObject
     * domain object} nearest to the direct position. The length of the sequence (the number
     * of <var>geometry</var>-<var>value</var> pairs returned) shall be no greater than the
     * number specified by the parameter {@code limit}. The default shall be to return a single
     * <var>geometry</var>-<var>value</var> pair. The operation shall return a warning if the
     * last {@linkplain DomainObject domain object} in the sequence is at a distance from the
     * direct position equal to the distance of other {@linkplain DomainObject domain objects}
     * that are not included in the sequence. In the case of an analytical coverage, the operation
     * shall return the empty set.
     * <p>
     * <B>NOTE:</B> This operation is useful when the domain of a coverage does not exhaustively
     * partition the extent of the coverage. Even in that case, the first element of the sequence
     * returned may be the <var>geometry</var>-<var>value</var> pair that contains the input direct
     * position.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public List find(DirectPosition p, int limit) {
        throw unsupported();
    }

    /**
     * Returns the nearest <var>geometry</var>-<var>value</var> pair
     * from the specified direct position. This is a shortcut for
     * <code>{@linkplain #find(DirectPosition,int) find}(p,1)</code>.
     *
     * @since 2.3
     */
    public GeometryValuePair find(final DirectPosition p) {
        final List pairs = find(p, 1);
        return pairs.isEmpty() ? null : (GeometryValuePair) pairs.get(0);
    }

    /**
     * Invoked when an unsupported operation is invoked.
     */
    private static final UnsupportedOperationException unsupported() {
        throw new UnsupportedOperationException(
                "This method is currently not impemented. " +
                "It may be implemented by next version of coverage.");
    }

    /**
     * Returns a localized error message for the specified array.
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
        return Errors.format(ErrorKeys.CANT_CONVERT_FROM_TYPE_$1, text);
    }

    /**
     * Returns a set of records of feature attribute values for the specified direct position. The
     * parameter {@code list} is a sequence of feature attribute names each of which identifies a
     * field of the range type. If {@code list} is null, the operation shall return a value for
     * every field of the range type. Otherwise, it shall return a value for each field included in
     * {@code list}. If the direct position passed is not in the domain of the coverage, then an
     * exception is thrown. If the input direct position falls within two or more geometric objects
     * within the domain, the operation shall return records of feature attribute values computed
     * according to the {@linkplain #getCommonPointRule common point rule}.
     * <P>
     * <B>NOTE:</B> Normally, the operation will return a single record of feature attribute values.
     *
     * @since 2.3
     *
     * @todo Current implementation is incorrect, since it ignores the {@link #list} argument.
     */
    public Set/*<Record>*/ evaluate(final DirectPosition coord, final Set/*<String>*/ list) {
        final Set set = new HashSet();
        final Object array = evaluate(coord);
        try {
            final int length = Array.getLength(array);
            for (int index=0; index<length; index++) {
                set.add(Array.get(array, index));
            }
        } catch (IllegalArgumentException exception) {
            throw new CannotEvaluateException(formatErrorMessage(set), exception);
        }
        return set;
    }

    /**
     * Returns a sequence of boolean values for a given point in the coverage. A
     * value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the point is the same
     * as the grid coverage
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system}.
     * 
     * @param coord
     *            The coordinate point where to evaluate.
     * @param dest
     *            An array in which to store values, or {@code null} to create a
     *            new array.
     * @return The {@code dest} array, or a newly created array if {@code dest}
     *         was null.
     * @throws CannotEvaluateException
     *             if the values can't be computed at the specified coordinate.
     *             More specifically, {@link PointOutsideCoverageException} is
     *             thrown if the evaluation failed because the input point has
     *             invalid coordinates. This exception may also be throws if the
     *             coverage data type can't be converted to {@code boolean} by
     *             an identity or widening conversion. Subclasses may relax this
     *             constraint if appropriate.
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
     * Returns a sequence of byte values for a given point in the coverage. A
     * value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the point is the same
     * as the coverage {@linkplain #getCoordinateReferenceSystem coordinate
     * reference system}.
     * 
     * @param coord
     *            The coordinate point where to evaluate.
     * @param dest
     *            An array in which to store values, or {@code null} to create a
     *            new array.
     * @return The {@code dest} array, or a newly created array if {@code dest}
     *         was null.
     * @throws CannotEvaluateException
     *             if the values can't be computed at the specified coordinate.
     *             More specifically, {@link PointOutsideCoverageException} is
     *             thrown if the evaluation failed because the input point has
     *             invalid coordinates. This exception may also be throws if the
     *             coverage data type can't be converted to {@code byte} by an
     *             identity or widening conversion. Subclasses may relax this
     *             constraint if appropriate.
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
     * Returns a sequence of integer values for a given point in the coverage. A
     * value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the point is the same
     * as the coverage {@linkplain #getCoordinateReferenceSystem coordinate
     * reference system}.
     * 
     * @param coord
     *            The coordinate point where to evaluate.
     * @param dest
     *            An array in which to store values, or {@code null} to create a
     *            new array.
     * @return The {@code dest} array, or a newly created array if {@code dest}
     *         was null.
     * @throws CannotEvaluateException
     *             if the values can't be computed at the specified coordinate.
     *             More specifically, {@link PointOutsideCoverageException} is
     *             thrown if the evaluation failed because the input point has
     *             invalid coordinates. This exception may also be throws if the
     *             coverage data type can't be converted to {@code int} by an
     *             identity or widening conversion. Subclasses may relax this
     *             constraint if appropriate.
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
     * Returns a sequence of float values for a given point in the coverage. A
     * value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the point is the same
     * as the coverage
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system}.
     * 
     * @param coord
     *            The coordinate point where to evaluate.
     * @param dest
     *            An array in which to store values, or {@code null} to create a
     *            new array.
     * @return The {@code dest} array, or a newly created array if {@code dest}
     *         was null.
     * @throws CannotEvaluateException
     *             if the values can't be computed at the specified coordinate.
     *             More specifically, {@link PointOutsideCoverageException} is
     *             thrown if the evaluation failed because the input point has
     *             invalid coordinates. This exception may also be throws if the
     *             coverage data type can't be converted to {@code float} by an
     *             identity or widening conversion. Subclasses may relax this
     *             constraint if appropriate.
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
     * Returns a sequence of double values for a given point in the coverage. A
     * value for each sample dimension is included in the sequence. The default
     * interpolation type used when accessing grid values for points which fall
     * between grid cells is nearest neighbor. The CRS of the point is the same
     * as the grid coverage coordinate system.
     * 
     * @param coord
     *            The coordinate point where to evaluate.
     * @param dest
     *            An array in which to store values, or {@code null} to create a
     *            new array.
     * @return The {@code dest} array, or a newly created array if {@code dest}
     *         was null.
     * @throws CannotEvaluateException
     *             if the values can't be computed at the specified coordinate.
     *             More specifically, {@link PointOutsideCoverageException} is
     *             thrown if the evaluation failed because the input point has
     *             invalid coordinates. This exception may also be throws if the
     *             coverage data type can't be converted to {@code double} by an
     *             identity or widening conversion. Subclasses may relax this
     *             constraint if appropriate.
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
     * Returns a set of {@linkplain DomainObject domain objects} for the specified record of feature
     * attribute values. Normally, this method returns the set of {@linkplain DomainObject objects}
     * in the domain that are associated with values equal to those in the input record. However,
     * the operation may return other {@linkplain DomainObject objects} derived from those in the
     * domain, as specified by the application schema.
     * <p>
     * <B>Example:</B> The {@code evaluateInverse} operation could return a set
     * of contours derived from the feature attribute values associated with the
     * {@linkplain org.opengis.coverage.grid.GridPoint grid points} of a grid coverage.
     * <p>
     * <strong>This method is not yet implemented.</strong>
     *
     * @since 2.3
     */
    public Set evaluateInverse(Record v) {
        throw unsupported();
    }

    /**
     * Returns 2D view of this grid coverage as a renderable image. This method
     * allows interoperability with Java2D.
     * 
     * @param xAxis
     *            Dimension to use for the <var>x</var> display axis.
     * @param yAxis
     *            Dimension to use for the <var>y</var> display axis.
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
     * A view of a {@linkplain AbstractCoverage coverage} as a renderable image.
     * Renderable images allow interoperability with
     * <A HREF="http://java.sun.com/products/java-media/2D/">Java2D</A> for a
     * two-dimensional slice of a coverage (which may or may not be a
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
         * A coordinate point where to evaluate the function. The point dimension is equals
         * to the {@linkplain AbstractCoverage#getDimension coverage's dimension}. The
         * {@linkplain #xAxis x} and {@link #yAxis y} ordinates will be ignored,
         * since they will vary for each pixel to be evaluated. Other ordinates,
         * if any, should be set to a fixed value. For example a coverage may be
         * three-dimensional, where the third dimension is the time axis. In
         * such case, {@code coordinate.ord[2]} should be set to the point in
         * time where to evaluate the coverage. By default, all ordinates are
         * initialized to 0. Subclasses should set the desired values in their
         * constructor if needed.
         */
        protected final GeneralDirectPosition coordinate = new GeneralDirectPosition(getDimension());

        /**
         * Constructs a renderable image.
         * 
         * @param xAxis Dimension to use for <var>x</var> axis.
         * @param yAxis Dimension to use for <var>y</var> axis.
         */
        public Renderable(final int xAxis, final int yAxis) {
            super(null, AbstractCoverage.this);
            this.xAxis = xAxis;
            this.yAxis = yAxis;
            final Envelope envelope = getEnvelope();
            bounds = new Rectangle2D.Double(envelope.getMinimum(xAxis), envelope.getMinimum(yAxis),
                                            envelope.getLength (xAxis), envelope.getLength (yAxis));
        }

        /**
         * Returns {@code null} to indicate that no source information is
         * available.
         */
        public Vector getSources() {
            return null;
        }

        /**
         * Returns {@code true} if successive renderings with the same arguments
         * may produce different results. The default implementation returns
         * {@code false}.
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
            return (float) bounds.getWidth();
        }

        /**
         * Gets the height in coverage coordinate space.
         * 
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getHeight() {
            return (float) bounds.getHeight();
        }

        /**
         * Gets the minimum <var>X</var> coordinate of the rendering-independent image
         * data. This is the {@linkplain AbstractCoverage#getEnvelope coverage's envelope}
         * minimal value for the {@linkplain #xAxis x axis}.
         * 
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getMinX() {
            return (float) bounds.getX();
        }

        /**
         * Gets the minimum <var>Y</var> coordinate of the rendering-independent image
         * data. This is the {@linkplain AbstractCoverage#getEnvelope coverage's envelope}
         * minimal value for the {@linkplain #yAxis y axis}.
         * 
         * @see AbstractCoverage#getEnvelope
         * @see AbstractCoverage#getCoordinateReferenceSystem
         */
        public float getMinY() {
            return (float) bounds.getY();
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
         * Creates a rendered image with width {@code width} and height
         * {@code height} in pixels. If {@code width} is 0, it will be computed
         * automatically from {@code height}. Conversely, if {@code height} is
         * 0, il will be computed automatically from {@code width}.
         * 
         * The default implementation creates a render context with
         * {@link #createRenderContext} and invokes
         * {@link #createRendering(RenderContext)}.
         * 
         * @param width  The width of rendered image in pixels, or 0.
         * @param height The height of rendered image in pixels, or 0.
         * @param hints  Rendering hints, or {@code null}.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createScaledRendering(int width, int height, final RenderingHints hints) {
            final double boundsWidth  = bounds.getWidth();
            final double boundsHeight = bounds.getHeight();
            if (!(width > 0)) { // Use '!' in order to catch NaN
                if (!(height > 0)) {
                    throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
                }
                width = (int) Math.round(height * (boundsWidth / boundsHeight));
            } else if (!(height > 0)) {
                height = (int) Math.round(width * (boundsHeight / boundsWidth));
            }
            return createRendering(createRenderContext(new Rectangle(0, 0, width, height), hints));
        }

        /**
         * Creates a rendered image using a given render context. This method
         * will uses an "{@link ImageFunctionDescriptor ImageFunction}"
         * operation if possible (i.e. if the area of interect is rectangular
         * and the affine transform contains only translation and scale
         * coefficients).
         * 
         * @param context The render context to use to produce the rendering.
         * @return A rendered image containing the rendered data
         */
        public RenderedImage createRendering(final RenderContext context) {
            final AffineTransform crsToGrid = context.getTransform();
            final Shape area = context.getAreaOfInterest();
            final Rectangle gridBounds;
            if (true) {
                /*
                 * Compute the grid bounds for the coverage bounds (or the area
                 * of interest). The default implementation of Rectangle uses
                 * Math.floor and Math.ceil for computing a box which contains
                 * fully the Rectangle2D. But in our particular case, we really
                 * want to round toward the nearest integer.
                 */
                final Rectangle2D bounds = XAffineTransform.transform(crsToGrid,
                        (area != null) ? area.getBounds2D() : this.bounds, null);
                final int xmin = (int) Math.round(bounds.getMinX());
                final int ymin = (int) Math.round(bounds.getMinY());
                final int xmax = (int) Math.round(bounds.getMaxX());
                final int ymax = (int) Math.round(bounds.getMaxY());
                gridBounds = new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
            }
            /*
             * Computes some properties of the image to be created.
             */
            final Dimension       tileSize = ImageUtilities.toTileSize(gridBounds.getSize());
            final GridSampleDimension band = GridSampleDimension.wrap(getSampleDimension(VISIBLE_BAND));
            final ColorModel    colorModel = band.getColorModel(VISIBLE_BAND, getNumSampleDimensions());
            final SampleModel  sampleModel = colorModel.createCompatibleSampleModel(tileSize.width, tileSize.height);
            /*
             * If the image can be created using the ImageFunction operation, do
             * it. It allow JAI to defer the computation until a tile is really
             * requested.
             */
            final PlanarImage image;
            if ((area == null || area instanceof Rectangle2D) &&
                    crsToGrid.getShearX() == 0 && crsToGrid.getShearY() == 0)
            {
                image = JAI.create("ImageFunction", new ParameterBlock()
                        .add(this)                               // The functional description
                        .add(gridBounds.width)                   // The image width
                        .add(gridBounds.height)                  // The image height
                        .add((float) (1/crsToGrid.getScaleX()))  // The X scale factor
                        .add((float) (1/crsToGrid.getScaleY()))  // The Y scale factor
                        .add((float) crsToGrid.getTranslateX())  // The X translation
                        .add((float) crsToGrid.getTranslateY()), // The Y translation
                        new RenderingHints(JAI.KEY_IMAGE_LAYOUT, new ImageLayout()
                                .setMinX       (gridBounds.x)
                                .setMinY       (gridBounds.y)
                                .setTileWidth  (tileSize.width)
                                .setTileHeight (tileSize.height)
                                .setSampleModel(sampleModel)
                                .setColorModel (colorModel)));
            } else {
                /*
                 * Creates immediately a rendered image using a given render
                 * context. This block is run when the image can't be created
                 * with JAI's ImageFunction operator, for example because the
                 * affine transform swap axis or because there is an area of
                 * interest.
                 */
                // Clones the coordinate point in order to allow multi-thread
                // invocation.
                final GeneralDirectPosition coordinate = new GeneralDirectPosition(this.coordinate);
                final TiledImage tiled = new TiledImage(gridBounds.x, gridBounds.y,
                        gridBounds.width, gridBounds.height, 0, 0, sampleModel, colorModel);
                final Point2D.Double point2D = new Point2D.Double();
                final int numBands = tiled.getNumBands();
                final double[] samples = new double[numBands];
                final double[] padNaNs = new double[numBands];
                Arrays.fill(padNaNs, Double.NaN);
                final WritableRectIter iterator = RectIterFactory.createWritable(tiled, gridBounds);
                if (!iterator.finishedLines()) try {
                    int y = gridBounds.y;
                    do {
                        iterator.startPixels();
                        if (!iterator.finishedPixels()) {
                            int x = gridBounds.x;
                            do {
                                point2D.x = x;
                                point2D.y = y;
                                crsToGrid.inverseTransform(point2D, point2D);
                                if (area == null || area.contains(point2D)) {
                                    coordinate.ordinates[xAxis] = point2D.x;
                                    coordinate.ordinates[yAxis] = point2D.y;
                                    iterator.setPixel(evaluate(coordinate, samples));
                                } else {
                                    iterator.setPixel(padNaNs);
                                }
                                x++;
                            } while (!iterator.nextPixelDone());
                            assert (x == gridBounds.x + gridBounds.width);
                            y++;
                        }
                    } while (!iterator.nextLineDone());
                    assert (y == gridBounds.y + gridBounds.height);
                } catch (NoninvertibleTransformException exception) {
                    // TODO: give cause to constructor when we will be allowed to target J2SE 1.5.
                    final IllegalArgumentException e = new IllegalArgumentException(
                            Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$1, "context"));
                    e.initCause(exception);
                    throw e;
                }
                image = tiled;
            }
            /*
             * Add a 'gridToCRS' property to the image. This is an important
             * information for constructing a GridCoverage from this image later.
             */
            try {
                image.setProperty("gridToCRS", crsToGrid.createInverse());
            } catch (NoninvertibleTransformException exception) {
                // Can't add the property. Too bad, the image has been created
                // anyway. Maybe the user know what he is doing...
                Logging.unexpectedException("org.geotools.coverage",
                        "AbstractCoverage.Renderable", "createRendering", exception);
            }
            return image;
        }

        /**
         * Initialize a render context with an affine transform that maps the
         * coverage envelope to the specified destination rectangle. The affine
         * transform mays swap axis in order to normalize their order (i.e. make
         * them appear in the (<var>x</var>,<var>y</var>) order), so that
         * the image appears properly oriented when rendered.
         * 
         * @param gridBounds The two-dimensional destination rectangle.
         * @param hints      The rendering hints, or {@code null} if none.
         * @return A render context initialized with an affine transform from the coverage
         *         to the grid coordinate system. This transform is the inverse of
         *         {@link org.geotools.coverage.grid.GridGeometry2D#getGridToCRS2D}.
         * 
         * @see org.geotools.coverage.grid.GridGeometry2D#getGridToCRS2D
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
                    for (int i = normalized.length; --i >= 0;) {
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
         * Returns the number of elements per value at each position. This is
         * the maximum value plus 1 allowed in {@code getElements(...)} methods
         * invocation. The default implementation returns the number of sample
         * dimensions in the coverage.
         */
        public int getNumElements() {
            return getNumSampleDimensions();
        }

        /**
         * Returns all values of a given element for a specified set of coordinates.
         * This method is automatically invoked at rendering time for populating an
         * image tile, providing that the rendered image is created using the
         * "{@link ImageFunctionDescriptor ImageFunction}" operator and the image
         * type is not {@code double}. The default implementation invokes
         * {@link AbstractCoverage#evaluate(DirectPosition,float[])} recursively.
         */
        public void getElements(final float startX, final float startY,
                                final float deltaX, final float deltaY,
                                final int   countX, final int   countY, final int  element,
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
         * "{@link ImageFunctionDescriptor ImageFunction}" operator and the image
         * type is {@code double}. The default implementation invokes
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
    }

    /**
     * Display this coverage in a windows. This convenience method is used for debugging purpose.
     * The exact appareance of the windows and the tools provided may changes in future versions.
     *
     * @param  xAxis Dimension to use for the <var>x</var> display axis.
     * @param  yAxis Dimension to use for the <var>y</var> display axis.
     *
     * @deprecated Use {@link #show(String, int, int)}.
     */
    public void show(final int xAxis, final int yAxis) {
        show(null, xAxis, yAxis);
    }

    /**
     * Display this coverage in a windows. This convenience method is used for debugging purpose.
     * The exact appareance of the windows and the tools provided may changes in future versions.
     *
     * @param  title The window title, or {@code null} for default value.
     * @param  xAxis Dimension to use for the <var>x</var> display axis.
     * @param  yAxis Dimension to use for the <var>y</var> display axis.
     *
     * @since 2.3
     */
    public void show(String title, final int xAxis, final int yAxis) {
        if (title == null || (title = title.trim()).length() == 0) {
            title = String.valueOf(getName());
        }
        // In the following line, the constructor display immediately the viewer.
        new Viewer(title, getRenderableImage(xAxis, yAxis).createDefaultRendering());
    }

    /**
     * A trivial viewer implementation to be used by {@link AbstractCoverage#show(String,int,int)}
     * method.
     * <p>
     * <strong>Implementation note:</strong>
     * We use AWT Frame, not Swing JFrame, because {@link ScrollingImagePane} is an AWT
     * component. Swing is an overhead in this context without clear benefict. Note also
     * that {@code ScrollingImagePanel} includes the scroll bar, so there is no need to
     * put this component in an other {@code JScrollPane}.
     */
    private static final class Viewer extends WindowAdapter implements Runnable {
        /**
         * The frame to dispose once closed.
         */
        private final Frame frame;

        /**
         * Displays the specified image in a window with the specified title.
         */
        public Viewer(final String title, final RenderedImage image) {
            final int width  = Math.max(Math.min(image.getWidth(),  800), 24);
            final int height = Math.max(Math.min(image.getHeight(), 600), 24);
            frame = new Frame(title);
            frame.add(new ScrollingImagePanel(image, width, height));
            frame.addWindowListener(this);
            EventQueue.invokeLater(this);
        }

        /**
         * Display the window in the event queue.
         * Required because 'pack()' is invoked before 'setVisible(true)'.
         */
        public void run() {
            frame.pack();
            frame.setVisible(true);
        }

        /**
         * Invoked when the user dispose the window.
         */
        public void windowClosing(WindowEvent e) {
            frame.dispose();
        }
    }

    /**
     * Display this coverage in a windows. This convenience method is used for
     * debugging purpose. The exact appareance of the windows and the tools
     * provided may changes in future versions.
     *
     * @param  title The window title, or {@code null} for default value.
     *
     * @since 2.3
     */
    public void show(final String title) {
        show(title, 0, 1);
    }

    /**
     * Display this coverage in a windows. This convenience method is used for
     * debugging purpose. The exact appareance of the windows and the tools
     * provided may changes in future versions.
     */
    public void show() {
        show(null);
    }

    /**
     * Returns the source data for a coverage. The default implementation
     * returns an empty list.
     *
     * @deprecated No replacement.
     */
    public List getSources() {
        return Collections.EMPTY_LIST;
    }

    /**
     * List of metadata keywords for a coverage. If no metadata is available,
     * the sequence will be empty. The default implementation gets the list of
     * metadata names from the {@link #getPropertyNames()} method.
     * 
     * @return the list of metadata keywords for a coverage.
     *
     * @deprecated Use {@link #getPropertyNames()} instead.
     */
    public String[] getMetadataNames() {
        final String[] list = getPropertyNames();
        return (list != null) ? list : NO_PROPERTIES;
    }

    /**
     * Retrieve the metadata value for a given metadata name. The default
     * implementation query the {@link #getProperty(String)} method.
     * 
     * @param name Metadata keyword for which to retrieve data.
     * @return the metadata value for a given metadata name.
     * @throws MetadataNameNotFoundException
     *             if there is no value for the specified metadata name.
     *
     * @deprecated Use {@link #getProperty(String)} instead.
     */
    public String getMetadataValue(final String name) throws MetadataNameNotFoundException {
        final Object value = getProperty(name);
        if (value == java.awt.Image.UndefinedProperty) {
            throw new MetadataNameNotFoundException(Errors.format(
                    ErrorKeys.UNDEFINED_PROPERTY_$1, name));
        }
        return (value != null) ? value.toString() : null;
    }

    /**
     * Returns the default locale for logging, error messages, <cite>etc.</cite>.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns a string représentation of this coverage. This string is for
     * debugging purpose only and may change in future version.
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
        final LineWriter filter = new LineWriter(buffer, lineSeparator + "    ");
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

    /**
     * Provides a hint that a coverage will no longer be accessed from a
     * reference in user space. The results are equivalent to those that occur
     * when the program loses its last reference to this coverage, the garbage
     * collector discovers this, and finalize is called. This can be used as a
     * hint in situations where waiting for garbage collection would be overly
     * conservative. The results of referencing a coverage after a call to
     * {@code dispose()} are undefined.
     * 
     * @see PlanarImage#dispose
     */
    public void dispose() {
        // To be overriden by subclasses.
        //
        // Note: implementing this method in GridCoverage is tricky. We must ensure that:
        //
        // 1) The PlanarImage is not used by somebody else (i.e. is not a user supplied
        //    image, or the user didn't got a reference with getRenderedImage()).
        // 2) If the image is the result of a GridCoverageProcessor operation,
        //    it must removes itself from the WeakValueHashMap.
    }
}
