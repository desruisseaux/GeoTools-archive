/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
import java.io.Serializable;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * Describes the valid range of grid coordinates and the math
 * transform to transform grid coordinates to real world coordinates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridGeometry implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8740895616121262893L;
    
    /**
     * The valid coordinate range of a grid coverage, or <code>null</code> if none. The lowest
     * valid grid coordinate is zero for {@link BufferedImage}, but may be non-zero for arbitrary
     * {@link RenderedImage}. A grid with 512 cells can have a minimum coordinate of 0 and maximum
     * of 512, with 511 as the highest valid index.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    private final GridRange gridRange;
    
    /**
     * The math transform (usually an affine transform), or <code>null</code> if none.
     * This math transform maps pixel center to "real world" coordinate using the
     * following line:
     *
     * <pre>gridToCoordinateSystem.transform(pixels, point);</pre>
     */
    private final MathTransform gridToCoordinateSystem;
    
    /**
     * A math transform mapping only the two first dimensions of
     * <code>gridToCoordinateSystem</code>, or <code>null</code>
     * if such a "sub-transform" is not available.
     */
    private final MathTransform2D gridToCoordinateSystem2D;
    
    /**
     * The inverse of <code>gridToCoordinateSystem2D</code>.
     */
    private final MathTransform2D gridFromCoordinateSystem2D;
    
    /**
     * Construct a new grid geometry from a math transform.
     *
     * @param gridRange The valid coordinate range of a grid coverage, or <code>null</code> if
     *        none. The lowest valid grid coordinate is zero for {@link BufferedImage}, but may
     *        be non-zero for arbitrary {@link RenderedImage}. A grid with 512 cells can have a
     *        minimum coordinate of 0 and maximum of 512, with 511 as the highest valid index.
     * @param gridToCoordinateSystem The math transform which allows for the transformations
     *        from grid coordinates (pixel's <em>center</em>) to real world earth coordinates.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    public GridGeometry(final GridRange gridRange, final MathTransform gridToCoordinateSystem) {
        this.gridRange                  = gridRange;
        this.gridToCoordinateSystem     = gridToCoordinateSystem;
        this.gridToCoordinateSystem2D   = getMathTransform2D(gridToCoordinateSystem);
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
        if (gridRange!=null && gridToCoordinateSystem!=null) {
            final int dimRange  = gridRange.getDimension();
            final int dimSource = gridToCoordinateSystem.getSourceDimensions();
            final int dimTarget = gridToCoordinateSystem.getTargetDimensions();
            if (dimRange != dimSource) {
                throw new MismatchedDimensionException(format(dimRange, dimSource));
            }
            if (dimRange != dimTarget) {
                throw new MismatchedDimensionException(format(dimRange, dimTarget));
            }
        }
    }
    
    /**
     * Construct a new grid geometry. An affine transform will be computed automatically
     * from the specified envelope.  The <code>inverse</code> argument tells whatever or
     * not an axis should be inversed. Callers will typically set <code>inverse[1]</code>
     * to <code>true</code> in order to inverse the <var>y</var> axis.
     *
     * @param gridRange The valid coordinate range of a grid coverage.
     * @param userRange The corresponding coordinate range in user coordinate.
     *                  This rectangle must contains entirely all pixels, i.e.
     *                  the rectangle's upper left corner must coincide with
     *                  the upper left corner of the first pixel and the rectangle's
     *                  lower right corner must coincide with the lower right corner
     *                  of the last pixel.
     * @param inverse   Tells whatever or not inverse axis. A <code>null</code> value
     *                  inverse no axis.
     */
    public GridGeometry(final GridRange gridRange,
                        final Envelope  userRange,
                        final boolean[] inverse)
    {
        this.gridRange = gridRange;
        /*
         * Check arguments validity. Dimensions must match.
         */
        final int dimension = gridRange.getDimension();
        final int userDim   = userRange.getDimension();
        if (userDim != dimension) {
            throw new MismatchedDimensionException(format(dimension, userDim));
        }
        if (inverse!=null && inverse.length!=dimension) {
            throw new MismatchedDimensionException(format(dimension, inverse.length));
        }
        /*
         * Prepare elements for the 2D sub-transform. Those
         * elements will be set during the matrix setup below.
         */
        double scaleX = 1;
        double scaleY = 1;
        double transX = 0;
        double transY = 0;
        /*
         * Setup the multi-dimensional affine transform for use with OpenGIS.
         * According OpenGIS specification, transforms must map pixel center.
         * This is done by adding 0.5 to grid coordinates.
         */
        final GeneralMatrix matrix = new GeneralMatrix(dimension+1);
        matrix.setElement(dimension, dimension, 1);
        for (int i=0; i<dimension; i++) {
            double scale = userRange.getLength(i) / gridRange.getLength(i);
            double trans;
            if (inverse==null || !inverse[i]) {
                trans = userRange.getMinimum(i);
            } else {
                scale = -scale;
                trans = userRange.getMaximum(i);
            }
            trans -= scale * (gridRange.getLower(i)-0.5);
            matrix.setElement(i, i,         scale);
            matrix.setElement(i, dimension, trans);
            /*
             * Keep two-dimensional components for the AffineTransform.
             */
            switch (i) {
                case 0: scaleX=scale; transX=trans; break;
                case 1: scaleY=scale; transY=trans; break;
            }
        }
        gridToCoordinateSystem = ProjectiveTransform.create(matrix);
        if (gridToCoordinateSystem instanceof MathTransform2D) {
            gridToCoordinateSystem2D = (MathTransform2D) gridToCoordinateSystem;
        } else {
            gridToCoordinateSystem2D = (MathTransform2D) ProjectiveTransform.create(
                    new AffineTransform(scaleX, 0, 0, scaleY, transX, transY));
        }
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
    }
    
    /**
     * Construct a new two-dimensional grid geometry. A math transform will
     * be computed automatically with an inverted <var>y</var> axis (i.e.
     * <code>gridRange</code> and <code>userRange</code> are assumed to
     * have <var>y</var> axis in opposite direction).
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
    public GridGeometry(final Rectangle gridRange, final Rectangle2D userRange) {
        final double scaleX = userRange.getWidth()  / gridRange.getWidth();
        final double scaleY = userRange.getHeight() / gridRange.getHeight();
        final double transX = userRange.getMinX()   - gridRange.x*scaleX;
        final double transY = userRange.getMaxY()   + gridRange.y*scaleY;
        final AffineTransform tr = new AffineTransform(scaleX, 0, 0, -scaleY, transX, transY);
        tr.translate(0.5, 0.5); // Map to pixel center
        this.gridRange                  = new org.geotools.coverage.grid.GridRange(gridRange);
        this.gridToCoordinateSystem2D   = (MathTransform2D) ProjectiveTransform.create(tr);
        this.gridToCoordinateSystem     = gridToCoordinateSystem2D;
        this.gridFromCoordinateSystem2D = inverse(gridToCoordinateSystem2D);
    }
    
    /**
     * Inverse the specified math transform.
     */
    private static MathTransform2D inverse(final MathTransform2D gridToCoordinateSystem2D)
            throws IllegalArgumentException
    {
        if (gridToCoordinateSystem2D != null) {
            try {
                return (MathTransform2D) gridToCoordinateSystem2D.inverse();
            } catch (NoninvertibleTransformException exception) {
                IllegalArgumentException e = new IllegalArgumentException(Resources.format(
                                            ResourceKeys.ERROR_BAD_TRANSFORM_$1,
                                            Utilities.getShortClassName(gridToCoordinateSystem2D)));
                e.initCause(exception);
                throw e;
            }
        }
        return null;
    }

    /**
     * Format an error message for mismatched dimension.
     */
    private static String format(final int dim1, final int dim2) {
        return org.geotools.resources.cts.Resources.format(
               org.geotools.resources.cts.ResourceKeys.ERROR_MISMATCHED_DIMENSION_$2,
               new Integer(dim1), new Integer(dim2));
    }
    
    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        if (gridToCoordinateSystem != null) {
            return gridToCoordinateSystem.getSourceDimensions();
        }
        return getGridRange().getDimension();
    }

    /**
     * Returns the bounding box of "real world" coordinates for this grid geometry. This
     * envelope is the {@linkplain #getGridRange grid range} {@linkplain #getGridToCoordinateSystem
     * transformed} to the "real world" coordinate system.
     *
     * @return The bounding box in "real world" coordinates.
     * @throws InvalidGridGeometryException if the envelope can't be computed.
     *
     * @see #getGridRange
     * @see #getGridToCoordinateSystem
     */
    public Envelope getEnvelope() throws InvalidGridGeometryException {
        final int dimension = getDimension();
        final GeneralEnvelope envelope = new GeneralEnvelope(dimension);
        for (int i=0; i<dimension; i++) {
            // According OpenGIS specification, GridGeometry maps pixel's center.
            // We want a bounding box for all pixels, not pixel's centers. Offset by
            // 0.5 (use -0.5 for maximum too, not +0.5, since maximum is exclusive).
            envelope.setRange(i, gridRange.getLower(i)-0.5, gridRange.getUpper(i)-0.5);
        }
        final MathTransform gridToCoordinateSystem = getGridToCoordinateSystem();
        try {
            return CRSUtilities.transform(gridToCoordinateSystem, envelope);
        } catch (TransformException exception) {
            throw new InvalidGridGeometryException(Resources.format(
                    ResourceKeys.ERROR_BAD_TRANSFORM_$1,
                    Utilities.getShortClassName(gridToCoordinateSystem)), exception);
        }
    }
    
    /**
     * Returns the valid coordinate range of a grid coverage. The lowest valid grid coordinate is
     * zero for {@link BufferedImage}, but may be non-zero for arbitrary {@link RenderedImage}. A
     * grid with 512 cells can have a minimum coordinate of 0 and maximum of 512, with 511 as the
     * highest valid index.
     *
     * @return The grid range (never <code>null</code>).
     * @throws InvalidGridGeometryException if this grid geometry has no grid range.
     *
     * @see RenderedImage#getMinX
     * @see RenderedImage#getMinY
     * @see RenderedImage#getWidth
     * @see RenderedImage#getHeight
     */
    public GridRange getGridRange() throws InvalidGridGeometryException {
        if (gridRange != null) {
            return gridRange;
        }
            throw new InvalidGridGeometryException(Resources.format(
                      ResourceKeys.ERROR_UNSPECIFIED_IMAGE_SIZE));
    }
    
    /**
     * Returns the math transform which allows  for the transformations from grid coordinates to
     * real world earth coordinates. The transform is often an affine transformation. The coordinate
     * reference system of the real world coordinates is given by
     * {@link org.geotools.cv.Coverage#getCoordinateReferenceSystem}. If no math transform is
     * available, this method returns <code>null</code>.
     * <br><br>
     * <strong>Note:</strong> OpenGIS requires that the transform maps <em>pixel centers</em>
     * to real world coordinates. This is different from some other systems that map pixel's
     * upper left corner.
     *
     * @return The transform (never <code>null</code>).
     * @throws InvalidGridGeometryException if this grid geometry has no transform.
     */
    public MathTransform getGridToCoordinateSystem() throws InvalidGridGeometryException {
        if (gridToCoordinateSystem != null) {
            return gridToCoordinateSystem;
        }
            throw new InvalidGridGeometryException();
    }
    
    /**
     * Returns a math transform for the first two dimensions, if such a transform exists.
     * This is a convenient method for working on horizontal data while ignoring vertical
     * or temporal dimensions. This method will succed only if the first two dimensions
     * are separable from extra dimensions (i.e. the transformation from
     * (<var>x<sub>0</sub></var>,&nbsp;<var>x<sub>1</sub></var>,&nbsp;<var>x<sub>2</sub></var>...) to
     * (<var>y<sub>0</sub></var>,&nbsp;<var>y<sub>1</sub></var>,&nbsp;<var>y<sub>2</sub></var>...)
     * must be such that <var>y<sub>0</sub></var> and <var>y<sub>1</sub></var> depend only on
     * <var>x<sub>0</sub></var> and <var>x<sub>1</sub></var>).
     *
     * @return The transform which allows for the transformations from grid coordinates
     *         to real world earth coordinates, operating only on the first two dimensions.
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
     * Returns the math transform for the two first dimensions of the specified transform.
     * This method is usefull for extracting the transformation caused by the head CS in
     * a {@link org.geotools.cs.CompoundCoordinateSystem}, assuming that this head CS is
     * a {@link org.geotools.cs.HorizontalCoordinateSystem}.
     *
     * @param  transform The transform.
     * @param  mtFactory The factory to use for extracting the sub-transform.
     * @return The {@link MathTransform2D} part of <code>transform</code>, or <code>null</code>
     *         if none.
     */
    private static MathTransform2D getMathTransform2D(MathTransform transform) {
        if (transform==null || transform instanceof MathTransform2D) {
            return (MathTransform2D) transform;
        }
//        final MathTransformFactory factory = FactoryFinder.getMathTransformFactory();
//        final IntegerSequence  inputDimensions = JAIUtilities.createSequence(0, 1);
//        final IntegerSequence outputDimensions = new IntegerSequence();
//// TODO
//        try {
//            transform = factory.(transform, inputDimensions, outputDimensions);
//        } catch (FactoryException exception) {
//            // A MathTransform2D is not mandatory. Just tell that we have none.
//            return null;
//        }
//        if (JAIUtilities.containsAll(outputDimensions, 0, 2)) {
//            transform = factory.createFilterTransform(transform, inputDimensions);
//            return (MathTransform2D) transform;
//        }
        return null;
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
                          GridCoverage.toString(new DirectPosition2D(point)), exception));
            }
        }
        throw new InvalidGridGeometryException(Resources.format(
                  ResourceKeys.ERROR_NO_TRANSFORM2D_AVAILABLE));
    }
    
    /**
     * Returns the pixel coordinate of a rectangle containing the
     * specified geographic area. If the rectangle can't be computed,
     * then this method returns <code>null</code>.
     */
    final Rectangle inverseTransform(Rectangle2D bounds) {
        if (bounds!=null && gridFromCoordinateSystem2D!=null) {
            try {
                bounds = CRSUtilities.transform(gridFromCoordinateSystem2D, bounds, null);
                final int xmin = (int)Math.floor(bounds.getMinX() - 0.5);
                final int ymin = (int)Math.floor(bounds.getMinY() - 0.5);
                final int xmax = (int)Math.ceil(bounds.getMaxX() - 0.5);
                final int ymax = (int)Math.ceil(bounds.getMaxY() - 0.5);
                return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
            } catch (TransformException exception) {
                // Ignore, since this method is invoked from 'GridCoverage.prefetch' only.
                // It doesn't matter if the transformation failed; 'prefetch' is just a hint.
            }
        }
        return null;
    }
    
    /**
     * Try to guess which axis are inverted in this grid geometry. If
     * this method can't make the guess, it returns <code>null</code>.
     *
     * @return An array with length equals  to the number of dimensions in
     *         the "real world" coordinate system, or <code>null</code> if
     *         if this array can't be deduced.
     */
    final boolean[] areAxisInverted() {
        final Matrix matrix;
        try {
            // Try to get the affine transform, assuming it is
            // insensitive to location (thus the 'null' argument).
            matrix = gridToCoordinateSystem.derivative(null);
        } catch (NullPointerException exception) {
            // The approximate affine transform is location-dependent.
            // We can't guess axis orientation from this.
            return null;
        } catch (Exception exception) {
            // Some other error occured. We didn't expected it,
            // but it will not prevent 'GridCoverage' to work.
            Utilities.unexpectedException("org.geotools.gcs", "MathTransform",
                                          "derivative", exception);
            return null;
        }
        final int numCol = matrix.getNumCol();
        final boolean[] inverse = new boolean[matrix.getNumRow()];
        for (int j=0; j<inverse.length; j++) {
            for (int i=0; i<numCol; i++) {
                final double value = matrix.getElement(j,i);
                if (i==j) {
                    inverse[j] = (value < 0);
                } else if (value!=0) {
                    // Matrix is not diagonal. Can't guess axis direction.
                    return null;
                }
            }
        }
        return inverse;
    }
    
    /**
     * Returns a hash value for this grid geometry.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (gridToCoordinateSystem != null) {
            code += gridToCoordinateSystem.hashCode();
        }
        if (gridRange != null) {
            code += gridRange.hashCode();
        }
        return code;
    }
    
    /**
     * Compares the specified object with
     * this grid geometry for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof GridGeometry) {
            final GridGeometry that = (GridGeometry) object;
            return Utilities.equals(this.gridRange,              that.gridRange             ) &&
                   Utilities.equals(this.gridToCoordinateSystem, that.gridToCoordinateSystem);
        }
        return false;
    }
    
    /**
     * Returns a string repr�sentation of this grid range.
     * The returned string is implementation dependent. It
     * is usually provided for debugging purposes.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(gridRange);
        buffer.append(", ");
        buffer.append(gridToCoordinateSystem);
        buffer.append(']');
        return buffer.toString();
    }
}
