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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

// OpenGIS dependencies
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.referencing.operation.transform.DimensionFilter;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


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
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridGeometry2D extends GridGeometry {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1703923198093773095L;

    /**
     * Dimension for <var>x</var> and <var>y</var> values. There are the index of the two
     * first dimensions with a grid size greater than 1. <var>x</var> and <var>y</var>
     * dimensions are usually 0 and 1 respectively.
     *
     * @todo Set the values for those axis in the constructor.
     */
    final int xAxis=0, yAxis=1;

    /**
     * A math transform mapping only the two first dimensions of {@code gridToCoordinateSystem}.
     */
    private final MathTransform2D gridToCoordinateSystem2D;
    
    /**
     * The inverse of {@code gridToCoordinateSystem2D}.
     */
    private final MathTransform2D gridFromCoordinateSystem2D;

    /**
     * Constructs a new grid geometry from a math transform. The argument are passed unchanged to
     * the {@linkplain GridGeometry#GridGeometry(GridRange,MathTransform) super-class constructor}.
     * However, they must obey to one additional constraint: only two dimensions in the grid range
     * can have a width larger than 1.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or {@code null} if none.
     *        The lowest valid grid coordinate is zero for {@link BufferedImage}, but may
     *        be non-zero for arbitrary {@link RenderedImage}. A grid with 512 cells can have a
     *        minimum coordinate of 0 and maximum of 512, with 511 as the highest valid index.
     * @param gridToCoordinateSystem The math transform which allows for the transformations
     *        from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a width larger than 1.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    public GridGeometry2D(final GridRange gridRange, final MathTransform gridToCoordinateSystem)
            throws IllegalArgumentException
    {
        super(gridRange, gridToCoordinateSystem);
        gridToCoordinateSystem2D   = getMathTransform2D(gridRange, gridToCoordinateSystem);
        gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
    }
    
    /**
     * Constructs a new grid geometry. The argument are passed unchanged to the
     * {@linkplain GridGeometry#GridGeometry(GridRange,Envelope,boolean[]) super-class constructor}.
     * However, they must obey to one additional constraint: only two dimensions in the grid range
     * can have a width larger than 1.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     * @param reverse   Tells whatever or not reverse axis. A {@code null} value reverse no axis.
     * @throws IllegalArgumentException if {@code gridRange} has more than 2 dimensions with
     *         a width larger than 1.
     */
    public GridGeometry2D(final GridRange gridRange,
                          final Envelope  userRange,
                          final boolean[] reverse)
            throws IllegalArgumentException
    {
        super(gridRange, userRange, reverse);
        gridToCoordinateSystem2D   = getMathTransform2D(gridRange, gridToCoordinateSystem);
        gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
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
        this(new org.geotools.coverage.grid.GridRange(gridRange),
             getMathTransform(gridRange, userRange));
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
     * @return The {@link MathTransform2D} part of {@code transform}.
     * @throws IllegalArgumentException if the 2D part is not separable.
     */
    private static MathTransform2D getMathTransform2D(final GridRange gridRange,
                                                      MathTransform transform)
            throws IllegalArgumentException
    {
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
        final int[] dimensions = filter.getSourceDimensions();
        /*
         * Select a math transform that operate only on the two dimensions choosen above.
         * If such a math transform doesn't have exactly 2 output dimensions, then select
         * the same output dimensions than the input ones.
         */
        if (dimensions.length == 2) try {
            transform = filter.separate(transform);
            if (transform.getTargetDimensions() != 2) {
                filter.clear();
                filter.addTargetDimensions(dimensions);
                transform = filter.separate(transform);
            }
            try {
                return (MathTransform2D) transform;
            } catch (ClassCastException exception) {
                cause = exception;
            }
        } catch (FactoryException exception) {
            cause = exception;
        }
        IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                                         ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
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
    private static MathTransform2D inverse(final MathTransform2D gridToCoordinateSystem2D)
            throws IllegalArgumentException
    {
        if (gridToCoordinateSystem2D == null) {
            return null;
        } else try {
            return (MathTransform2D) gridToCoordinateSystem2D.inverse();
        } catch (NoninvertibleTransformException exception) {
            IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                                        ResourceKeys.ERROR_BAD_TRANSFORM_$1,
                                        Utilities.getShortClassName(gridToCoordinateSystem2D)));
            e.initCause(exception);
            throw e;
        }
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
     */
    public MathTransform2D getGridToCoordinateSystem2D() throws InvalidGridGeometryException {
        if (gridToCoordinateSystem2D != null) {
            return gridToCoordinateSystem2D;
        }
        throw new InvalidGridGeometryException(Resources.format(
                  ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }
    
    /**
     * Transform a point using the inverse of {@link #getGridToCoordinateSystem2D()}.
     *
     * @param  point The point in logical coordinate system.
     * @return A new point in the grid coordinate system.
     * @throws InvalidGridGeometryException if a two-dimensional inverse
     *         transform is not available for this grid geometry.
     * @throws CannotEvaluateException if the transformation failed.
     */
    final Point2D inverseTransform(final Point2D point) throws InvalidGridGeometryException {
        if (gridFromCoordinateSystem2D != null) {
            try {
                return gridFromCoordinateSystem2D.transform(point, null);
            } catch (TransformException exception) {
                throw new CannotEvaluateException(
                          Resources.format(ResourceKeys.ERROR_CANT_EVALUATE_$1,
                          AbstractGridCoverage.toString(point), exception));
            }
        }
        throw new InvalidGridGeometryException(Resources.format(
                  ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }
    
    /**
     * Returns the pixel coordinate of a rectangle containing the
     * specified geographic area. If the rectangle can't be computed,
     * then this method returns {@code null}.
     */
    final Rectangle inverseTransform(Rectangle2D bounds) {
        if (bounds!=null && gridFromCoordinateSystem2D!=null) {
            try {
                bounds = CRSUtilities.transform(gridFromCoordinateSystem2D, bounds, null);
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
}
