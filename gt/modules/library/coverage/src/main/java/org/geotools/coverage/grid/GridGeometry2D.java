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
package org.geotools.coverage.grid;

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.grid.GridRange;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.operation.matrix.MatrixFactory;
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Describes the valid range of grid coordinates and the math transform, in the special case
 * where only 2 dimensions are in use. By "in use", we means dimension with more than 1 pixel.
 * For example a grid size of 512&times;512&times;1 pixels can be represented by this
 * {@code GridGeometry2D} class (some peoples said 2.5D) because a two-dimensional grid
 * coordinate is enough for referencing a pixel without ambiguity. But a grid size of
 * 512&times;512&times;2 pixels can not be represented by this {@code GridGeometry2D},
 * because a three-dimensional coordinate is mandatory for referencing a pixel without
 * ambiguity.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridGeometry2D extends GeneralGridGeometry {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3989363771504614419L;

    /**
     * Helpers methods for 2D CRS creation. Will be constructed only when first needed.
     */
    private static FactoryGroup FACTORY_GROUP;

    /**
     * The offset for various pixel orientations.
     */
    private static Map/*<PixelOrientation, Point2D.Double>*/ ORIENTATIONS = new HashMap(8);
    static {
        ORIENTATIONS.put(PixelOrientation.CENTER,      new Point2D.Double( 0.0,  0.0));
        ORIENTATIONS.put(PixelOrientation.UPPER_LEFT,  new Point2D.Double(-0.5, -0.5));
        ORIENTATIONS.put(PixelOrientation.UPPER_RIGHT, new Point2D.Double( 0.5, -0.5));
        ORIENTATIONS.put(PixelOrientation.LOWER_LEFT,  new Point2D.Double(-0.5,  0.5));
        ORIENTATIONS.put(PixelOrientation.LOWER_RIGHT, new Point2D.Double( 0.5,  0.5));
    }

    /**
     * The two-dimensional part of the coordinate reference system.
     * This is usually (but not always) identical to {@link #getCoordinateReferenceSystem}.
     */
    private final CoordinateReferenceSystem crs2D;

    /**
     * The first ({@code gridDimensionX}) and second ({@code gridDimensionY}) dimensions of
     * {@linkplain #getGridRange grid range} with {@linkplain GridRange#getLength length}
     * greater than 1. Those (<var>x</var>, <var>y</var>) dimensions are usually 0 and 1
     * respectively.
     */
    public final int gridDimensionX, gridDimensionY;

    /**
     * The ({@link #gridDimensionX}, {@link #gridDimensionY}) dimensions in the envelope space.
     * They are the (<var>x</var>, <var>y</var>) dimensions after the
     * {@linkplain #getGridToCoordinateSystem grid to CRS} transform.
     * Those dimensions are usually 0 and 1 respectively.
     */
    public final int axisDimensionX, axisDimensionY;

    /**
     * A math transform mapping only the two first dimensions of {@link #gridToCRS gridToCRS}.
     */
    private final MathTransform2D gridToCRS2D;

    /**
     * The inverse of {@code gridToCRS2D}.
     */
    private final MathTransform2D gridFromCRS2D;

    /**
     * Constructs a new grid geometry identical to the specified one except for the CRS.
     * Note that this constructor just defines the CRS; it does <strong>not</strong> reproject
     * the envelope. For this reason, this constructor should not be public. It is for internal
     * use by {@link GridCoverageFactory} only.
     */
    GridGeometry2D(final GridGeometry2D gm, final CoordinateReferenceSystem crs) {
        super(gm, crs);
        gridDimensionX = gm.gridDimensionX;
        gridDimensionY = gm.gridDimensionY;
        axisDimensionX = gm.axisDimensionX;
        axisDimensionY = gm.axisDimensionY;
        gridFromCRS2D  = gm.gridFromCRS2D;
        gridToCRS2D    = gm.gridToCRS2D;
        crs2D          = createCRS2D();
    }

    /**
     * Constructs a new grid geometry from a math transform. The arguments are passed unchanged
     * to the {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridRange, MathTransform,
     * CoordinateReferenceSystem) super-class constructor}. However, they must obey to one
     * additional constraint: only two dimensions in the grid range can have a
     * {@linkplain GridRange#getLength length} larger than 1.
     *
     * @param  gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     *         The lowest valid grid coordinate is zero for {@link BufferedImage}, but may
     *         be non-zero for arbitrary {@link RenderedImage}. A grid with 512 cells can have a
     *         minimum coordinate of 0 and maximum of 512, with 511 as the highest valid index.
     * @param  gridToCRS The math transform which allows for the transformations
     *         from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     * @param  crs The coordinate reference system for the "real world" coordinates, or {@code null}
     *         if unknown. This CRS is given to the {@linkplain #getEnvelope envelope}.
     *
     * @throws MismatchedDimensionException if the math transform and the CRS doesn't have
     *         consistent dimensions.
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridRange#getLength length} larger than 1, or if the math transform
     *         can't transform coordinates in the domain of the specified grid range.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     *
     * @since 2.2
     */
    public GridGeometry2D(final GridRange           gridRange,
                          final MathTransform       gridToCRS,
                          final CoordinateReferenceSystem crs)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        super(gridRange, gridToCRS, crs);
        final int[] dimensions;
        dimensions     = new int[4];
        gridToCRS2D    = getMathTransform2D(gridToCRS, gridRange, dimensions);
        gridFromCRS2D  = inverse(gridToCRS2D);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        crs2D = createCRS2D();
    }

    /**
     * Constructs a new grid geometry from an envelope. This constructors applies the same heuristic
     * rules than the {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridRange,Envelope)
     * super-class constructor}. However, they must obey to one additional constraint: only two
     * dimensions in the grid range can have a {@linkplain GridRange#getLength length} larger than
     * 1.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     *
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridRange#getLength length} larger than 1.
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GridGeometry2D(final GridRange gridRange, final Envelope userRange)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        this(gridRange, userRange, getCoordinateSystem(userRange));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    private GridGeometry2D(final GridRange gridRange, final Envelope userRange,
                           final CoordinateSystem cs)
            throws MismatchedDimensionException
    {
        this(gridRange, userRange, reverse(cs), swapXY(cs));
    }

    /**
     * Constructs a new grid geometry from an envelope. The argument are passed unchanged to the
     * {@linkplain GeneralGridGeometry#GeneralGridGeometry(GridRange,Envelope,boolean[],boolean)
     * super-class constructor}. However, they must obey to one additional constraint:
     * only two dimensions in the grid range can have a {@linkplain GridRange#getLength length}
     * larger than 1.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     * @param reverse   Tells for each axis in <cite>user</cite> space whatever or not its
     *                  direction should be reversed. A {@code null} value reverse no axis.
     * @param swapXY    If {@code true}, then the two first axis will be interchanged.
     *
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a {@linkplain GridRange#getLength length} larger than 1.
     * @throws MismatchedDimensionException if the grid range and the CRS doesn't have
     *         consistent dimensions.
     *
     * @since 2.2
     */
    public GridGeometry2D(final GridRange gridRange,
                          final Envelope  userRange,
                          final boolean[] reverse,
                          final boolean   swapXY)
            throws IllegalArgumentException, MismatchedDimensionException
    {
        super(gridRange, userRange, reverse, swapXY);
        final int[] dimensions;
        dimensions     = new int[4];
        gridToCRS2D    = getMathTransform2D(gridToCRS, gridRange, dimensions);
        gridFromCRS2D  = inverse(gridToCRS2D);
        gridDimensionX = dimensions[0];
        gridDimensionY = dimensions[1];
        axisDimensionX = dimensions[2];
        axisDimensionY = dimensions[3];
        crs2D = createCRS2D();
    }

    /**
     * Constructs a new two-dimensional grid geometry. A math transform will be computed
     * automatically with an inverted <var>y</var> axis (i.e. {@code gridRange} and
     * {@code userRange} are assumed to have <var>y</var> axis in opposite direction).
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>down</strong>.
     * @param userRange The corresponding coordinate range in user coordinate.
     *                  Increasing <var>x</var> values goes right and
     *                  increasing <var>y</var> values goes <strong>up</strong>.
     *                  This rectangle must contains entirely all pixels, i.e.
     *                  the rectangle's upper left corner must coincide with
     *                  the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner
     *                  of the last pixel.
     */
    public GridGeometry2D(final Rectangle gridRange, final Rectangle2D userRange) {
        this(new GeneralGridRange(gridRange), getMathTransform(gridRange, userRange),
             (CoordinateReferenceSystem) null);
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    private static MathTransform getMathTransform(final Rectangle   gridRange,
                                                  final Rectangle2D userRange)
    {
        final double scaleX = userRange.getWidth()  / gridRange.getWidth();
        final double scaleY = userRange.getHeight() / gridRange.getHeight();
        final double transX = userRange.getMinX()   - gridRange.x*scaleX;
        final double transY = userRange.getMaxY()   + gridRange.y*scaleY;
        final AffineTransform tr = new AffineTransform(scaleX, 0, 0, -scaleY, transX, transY);
        tr.translate(0.5, 0.5); // Maps to pixel center
        return ProjectiveTransform.create(tr);
    }

    /**
     * Returns the math transform for two dimensions of the specified transform.
     *
     * @param  gridRange The grid range.
     * @param  transform The transform.
     * @param  axis An array of length 4 initialized to 0. This is the array where to store
     *         {@link #axisDimensionX} and {@link #axisDimensionY} values. This argument is
     *         actually a workaround for a Java language limitation (no multiple return values).
     *         If we could, we should returns directly the {@code dimensions} array computed in
     *         the body of this method.
     * @return The {@link MathTransform2D} part of {@code transform}.
     * @throws IllegalArgumentException if the 2D part is not separable.
     */
    private static MathTransform2D getMathTransform2D(final MathTransform transform,
                                                      final GridRange     gridRange,
                                                      final int[]              axis)
            throws IllegalArgumentException
    {
        axis[1] = axis[3] = 1;
        if (transform==null || transform instanceof MathTransform2D) {
            return (MathTransform2D) transform;
        }
        /*
         * Finds the axis for the two dimensional parts. We infer them from the grid range.
         * If no grid range were specified, then we assume that they are the 2 first dimensions.
         */
        final DimensionFilter filter = new DimensionFilter();
        if (gridRange != null) {
            final int dimension = gridRange.getDimension();
            for (int i=0; i<dimension; i++) {
                if (gridRange.getLength(i) > 1) {
                    filter.addSourceDimension(i);
                }
            }
        } else {
            filter.addSourceDimensionRange(0, 2);
        }
        Exception cause = null;
        int[] dimensions = filter.getSourceDimensions();
        /*
         * Select a math transform that operate only on the two dimensions choosen above.
         * If such a math transform doesn't have exactly 2 output dimensions, then select
         * the same output dimensions than the input ones.
         */
        MathTransform candidate;
        if (dimensions.length == 2) {
            System.arraycopy(dimensions, 0, axis, 0, 2);
            try {
                candidate = filter.separate(transform);
                if (candidate.getTargetDimensions() != 2) {
                    filter.clear();
                    filter.addSourceDimensions(dimensions);
                    filter.addTargetDimensions(dimensions);
                    candidate = filter.separate(transform);
                }
                dimensions = filter.getTargetDimensions();
                System.arraycopy(dimensions, 0, axis, 2, 2);
                try {
                    return (MathTransform2D) candidate;
                } catch (ClassCastException exception) {
                    cause = exception;
                }
            } catch (FactoryException exception) {
                cause = exception;
            }
        }
        IllegalArgumentException e = new IllegalArgumentException(Errors.format(
                                         ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
        e.initCause(cause); // TODO: Move in constructor's argument when we
        throw e;            //       will be allowed to compile for J2SE 1.5.
    }
    
    /**
     * Inverses the specified math transform. This method is invoked by constructors only. It wraps
     * {@link NoninvertibleTransformException} into {@link IllegalArgumentException}, since failures
     * to inverse a transform are caused by an illegal user-supplied transform.
     *
     * @throws IllegalArgumentException if the transform is non-invertible.
     */
    private static MathTransform2D inverse(final MathTransform2D gridToCRS2D)
            throws IllegalArgumentException
    {
        if (gridToCRS2D == null) {
            return null;
        } else try {
            return (MathTransform2D) gridToCRS2D.inverse();
        } catch (NoninvertibleTransformException exception) {
            final IllegalArgumentException e;
            e = new IllegalArgumentException(Errors.format(ErrorKeys.BAD_TRANSFORM_$1,
                                             Utilities.getShortClassName(gridToCRS2D)));
            e.initCause(exception); // TODO: move into contructor call with J2SE 1.5.
            throw e;
        }
    }

    /**
     * Constructs the two-dimensional CRS. This is usually identical to the user-supplied CRS.
     * However, the user is allowed to specify a wider CRS (for example a 3D one which includes
     * a time axis), in which case we infer which axis apply to the 2D image, and constructs a
     * 2D CRS with only those axis.
     *
     * @return The coordinate reference system, or {@code null} if none.
     */
    private CoordinateReferenceSystem createCRS2D() {
        if (!super.isDefined(CRS)) {
            return null;
        }
        final CoordinateReferenceSystem crs = super.getCoordinateReferenceSystem();
        if (FACTORY_GROUP == null) {
            FACTORY_GROUP = FactoryGroup.createInstance(null);
            // No need to synchronize: this is not a big deal
            // if two FactoryGroup instances are created.
        }
        final CoordinateReferenceSystem crs2D;
        try {
            crs2D = FACTORY_GROUP.separate(crs, new int[] {axisDimensionX, axisDimensionY});
        } catch (FactoryException exception) {
            final InvalidGridGeometryException e = new InvalidGridGeometryException(
                    Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2, "crs", crs.getName()));
            e.initCause(exception); // TODO: inline in the constructor with J2SE 1.5.
            throw e;
        }
        assert crs2D.getCoordinateSystem().getDimension() == 2 : crs2D;
        return crs2D;
    }

    /**
     * Returns the two-dimensional part of this grid geometry CRS. This is usually (but not
     * always) identical to the {@linkplain #getCoordinateReferenceSystem full CRS}.
     *
     * @return The coordinate reference system (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no CRS (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #CRS CRS})</code>
     *         returned {@code false}).
     *
     * @see #getCoordinateReferenceSystem
     *
     * @since 2.2
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem2D()
            throws InvalidGridGeometryException
    {
        if (crs2D != null) {
            assert isDefined(CRS);
            return crs2D;
        }
        assert !isDefined(CRS);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_CRS));
    }

    /**
     * Returns the two-dimensional bounding box for the coverage domain in coordinate reference
     * system coordinates. If the coverage envelope has more than two dimensions, only the
     * dimensions used in the underlying rendered image are returned.
     *
     * @return The bounding box in "real world" coordinates (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no envelope (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #ENVELOPE ENVELOPE})</code>
     *         returned {@code false}).
     *
     * @see #getEnvelope
     */
    public Envelope2D getEnvelope2D() throws InvalidGridGeometryException {
        if (envelope!=null && !envelope.isNull()) {
            assert isDefined(ENVELOPE);
            return new Envelope2D(crs2D,
                    envelope.getMinimum(axisDimensionX),
                    envelope.getMinimum(axisDimensionY),
                    envelope.getLength (axisDimensionX),
                    envelope.getLength (axisDimensionY));
        }
        assert !isDefined(ENVELOPE);
        throw new InvalidGridGeometryException(Errors.format(gridToCRS == null ?
                    ErrorKeys.UNSPECIFIED_TRANSFORM : ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
    }

    /**
     * Returns the two-dimensional part of the {@linkplain #getGridRange grid range}
     * as a rectangle.
     *
     * @return The grid range (never {@code null}).
     * @throws InvalidGridGeometryException if this grid geometry has no grid range (i.e.
     *         <code>{@linkplain #isDefined isDefined}({@linkplain #GRID_RANGE GRID_RANGE})</code>
     *         returned {@code false}).
     *
     * @see #getGridRange
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    public Rectangle getGridRange2D() throws InvalidGridGeometryException {
        if (gridRange != null) {
            assert isDefined(GRID_RANGE);
            return new Rectangle(gridRange.getLower (gridDimensionX),
                                 gridRange.getLower (gridDimensionY),
                                 gridRange.getLength(gridDimensionX),
                                 gridRange.getLength(gridDimensionY));
        }
        assert !isDefined(GRID_RANGE);
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.UNSPECIFIED_IMAGE_SIZE));
    }
    
    /**
     * @deprecated Renamed as {@link #getGridToCRS2D()}.
     */
    public MathTransform2D getGridToCoordinateSystem2D() throws InvalidGridGeometryException {
        return getGridToCRS2D();
    }
    
    /**
     * Returns a math transform for the two dimensional part. This is a convenience method for
     * working on horizontal data while ignoring vertical or temporal dimensions.
     *
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates, operating only on two dimensions.
     *         The returned transform is often an instance of {@link AffineTransform}, which
     *         make it convenient for interoperability with Java2D.
     * @throws InvalidGridGeometryException if a two-dimensional transform is not available
     *         for this grid geometry.
     *
     * @see #getGridToCRS
     *
     * @since 2.3
     */
    public MathTransform2D getGridToCRS2D() throws InvalidGridGeometryException {
        if (gridToCRS2D != null) {
            return gridToCRS2D;
        }
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
    }

    /**
     * Returns a math transform for the two dimensional part. This method is similar
     * to {@link #getGridToCRS2D()} except that the transform may maps a pixel corner
     * instead of pixel center.
     *
     * @param  orientation The pixel part to map. The default value is
     *         {@link PixelOrientation#CENTER CENTER}.
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates.
     * @throws InvalidGridGeometryException if a two-dimensional transform is not available
     *         for this grid geometry.
     *
     * @since 2.3
     */
    public MathTransform2D getGridToCRS2D(final PixelOrientation orientation) {
        final int xdim = (gridDimensionX < gridDimensionY) ? 0 : 1;
        return (MathTransform2D) translate(getGridToCRS2D(), orientation, xdim, xdim | 1);
    }

    /**
     * Returns a math transform mapping the specified pixel part.
     *
     * @param  orientation The pixel part to map. The default value is
     *         {@link PixelOrientation#CENTER CENTER}.
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates.
     * @throws InvalidGridGeometryException if a transform is not available
     *         for this grid geometry.
     *
     * @since 2.3
     */
    public MathTransform getGridToCRS(final PixelOrientation orientation) {
        return translate(getGridToCRS(), orientation, gridDimensionX, gridDimensionY);
    }

    /**
     * Translates the specified math transform according the specified pixel orientation.
     * The {@code gridToCRS} math transform is assumed maps the pixel centers.
     */
    private static MathTransform translate(final MathTransform gridToCRS,
                                           final PixelOrientation orientation,
                                           final int gridDimensionX, final int gridDimensionY)
    {
        if (PixelOrientation.CENTER.equals(orientation)) {
            return gridToCRS;
        }
        final Point2D.Double offset = (Point2D.Double) ORIENTATIONS.get(orientation);
        if (offset == null) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "orientation", orientation));
        }
        final int dimension = gridToCRS.getSourceDimensions();
        final Matrix matrix = MatrixFactory.create(dimension + 1);
        matrix.setElement(gridDimensionX, dimension, offset.x);
        matrix.setElement(gridDimensionY, dimension, offset.y);
        return ConcatenatedTransform.create(ProjectiveTransform.create(matrix), gridToCRS);
    }
    
    /**
     * Transforms a point using the inverse of {@link #getGridToCRS2D()}.
     *
     * @param  point The point in logical coordinate system.
     * @return A new point in the grid coordinate system.
     * @throws InvalidGridGeometryException if a two-dimensional inverse
     *         transform is not available for this grid geometry.
     * @throws CannotEvaluateException if the transformation failed.
     */
    final Point2D inverseTransform(final Point2D point) throws InvalidGridGeometryException {
        if (gridFromCRS2D != null) {
            try {
                return gridFromCRS2D.transform(point, null);
            } catch (TransformException exception) {
                throw new CannotEvaluateException(Errors.format(ErrorKeys.CANT_EVALUATE_$1,
                          AbstractGridCoverage.toString(point, Locale.getDefault()), exception));
            }
        }
        throw new InvalidGridGeometryException(Errors.format(ErrorKeys.NO_TRANSFORM2D_AVAILABLE));
    }
    
    /**
     * Returns the pixel coordinate of a rectangle containing the
     * specified geographic area. If the rectangle can't be computed,
     * then this method returns {@code null}.
     */
    final Rectangle inverseTransform(Rectangle2D bounds) {
        if (bounds!=null && gridFromCRS2D!=null) {
            try {
                bounds = org.geotools.referencing.CRS.transform(gridFromCRS2D, bounds, null);
                final int xmin = (int)Math.floor(bounds.getMinX() - 0.5);
                final int ymin = (int)Math.floor(bounds.getMinY() - 0.5);
                final int xmax = (int)Math.ceil (bounds.getMaxX() - 0.5);
                final int ymax = (int)Math.ceil (bounds.getMaxY() - 0.5);
                return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            } catch (TransformException exception) {
                // Ignore, since this method is invoked from 'GridCoverage.prefetch' only.
                // It doesn't matter if the transformation failed; 'prefetch' is just a hint.
            }
        }
        return null;
    }

    /**
     * Compares the specified object with this grid geometry for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final GridGeometry2D that = (GridGeometry2D) object;
            return this.gridDimensionX == that.gridDimensionX &&
                   this.gridDimensionY == that.gridDimensionY &&
                   this.axisDimensionX == that.axisDimensionX &&
                   this.axisDimensionY == that.axisDimensionY;
        }
        return false;
    }
}
