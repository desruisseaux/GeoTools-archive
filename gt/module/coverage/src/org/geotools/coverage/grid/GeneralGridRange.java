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
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.geom.Rectangle2D; // For javadoc
import java.io.Serializable;
import java.util.Arrays;

// OpenGIS dependencies
import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridGeometry; // For javadoc
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Defines a range of grid coverage coordinates.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeneralGridRange implements GridRange, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 1452569710967224145L;

    /**
     * Minimum and maximum grid ordinates. The first half contains minimum
     * ordinates, while the last half contains maximum ordinates.
     */
    private final int[] index;
    
    /**
     * Check if ordinate values in the minimum index are less than or
     * equal to the corresponding ordinate value in the maximum index.
     *
     * @throws IllegalArgumentException if an ordinate value in the minimum index is not
     *         less than or equal to the corresponding ordinate value in the maximum index.
     */
    private void checkCoherence() throws IllegalArgumentException {
        final int dimension = index.length/2;
        for (int i=0; i<dimension; i++) {
            final int lower = index[i];
            final int upper = index[dimension+i];
            if (!(lower <= upper)) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.BAD_GRID_RANGE_$3, new Integer(i),
                        new Integer(lower), new Integer(upper)));
            }
        }
    }
    
    /**
     * Constructs an initially empty grid range of the specified dimension.
     */
    private GeneralGridRange(final int dimension) {
        index = new int[dimension*2];
    }
    
    /**
     * Constructs one-dimensional grid range.
     *
     * @param lower The minimal inclusive value.
     * @param upper The maximal exclusive value.
     */
    public GeneralGridRange(final int lower, final int upper) {
        index = new int[] {lower, upper};
        checkCoherence();
    }
    
    /**
     * Constructs a new grid range.
     *
     * @param lower The valid minimum inclusive grid coordinate.
     *              The array contains a minimum value for each
     *              dimension of the grid coverage. The lowest
     *              valid grid coordinate is zero.
     * @param upper The valid maximum exclusive grid coordinate.
     *              The array contains a maximum value for each
     *              dimension of the grid coverage.
     *
     * @see #getLowers
     * @see #getUppers
     */
    public GeneralGridRange(final int[] lower, final int[] upper) {
        if (lower.length != upper.length) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$2,
                        new Integer(lower.length), new Integer(upper.length)));
        }
        index = new int[lower.length + upper.length];
        System.arraycopy(lower, 0, index, 0,            lower.length);
        System.arraycopy(upper, 0, index, lower.length, upper.length);
        checkCoherence();
    }
    
    /**
     * Constructs two-dimensional range defined by a {@link Rectangle}.
     */
    public GeneralGridRange(final Rectangle rect) {
        index = new int[] {
            rect.x,            rect.y,
            rect.x+rect.width, rect.y+rect.height
        };
        checkCoherence();
    }
    
    /**
     * Constructs two-dimensional range defined by a {@link Raster}.
     */
    public GeneralGridRange(final Raster raster) {
        final int x = raster.getMinX();
        final int y = raster.getMinY();
        index = new int[] {
            x,                   y,
            x+raster.getWidth(), y+raster.getHeight()
        };
        checkCoherence();
    }
    
    /**
     * Constructs two-dimensional range defined by a {@link RenderedImage}.
     */
    public GeneralGridRange(final RenderedImage image) {
        this(image, 2);
    }
    
    /**
     * Constructs multi-dimensional range defined by a {@link RenderedImage}.
     *
     * @param image The image.
     * @param dimension Number of dimensions for this grid range.
     *        Dimensions over 2 will be set to the [0..1] range.
     */
    GeneralGridRange(final RenderedImage image, final int dimension) {
        index = new int[dimension*2];
        final int x = image.getMinX();
        final int y = image.getMinY();
        index[0] = x;
        index[1] = y;
        index[dimension+0] = x+image.getWidth();
        index[dimension+1] = y+image.getHeight();
        Arrays.fill(index, dimension+2, index.length, 1);
        checkCoherence();
    }
    
    /**
     * Cast the specified envelope into a grid range. This is sometime useful after an
     * envelope has been transformed from "real world" coordinates to grid coordinates
     * using the "{@linkplain GridGeometry#getGridToCoordinateSystem grid to CRS}" transform.
     * The floating point values are rounded toward the nearest integers.
     * <p>
     * <strong>Note about rounding mode:</strong><br>
     * It would have been possible to round the {@linkplain Envelope#getMinimum minimal value}
     * toward {@linkplain Math#floor floor} and the {@linkplain Envelope#getMaximum maximal value}
     * toward {@linkplain Math#ceil ceil} in order to make sure that the grid range encompass all
     * the envelope (something similar to what <cite>Java2D</cite> does when casting
     * {@link Rectangle2D} to {@link Rectangle}). But this approach has an undesirable
     * side effect: it may changes the image {@linkplain RenderedImage#getWidth width} or
     * {@linkplain RenderedImage#getHeight height}. For example the range {@code [-0.25 ... 99.75]}
     * would be casted to {@code [-1 ... 100]}, which leads to unexpected result when using grid
     * range with image operations like "{@link javax.media.jai.operator.AffineDescriptor Affine}".
     * For avoiding such changes in size, it is necessary to use the same rounding mode for both
     * minimal and maximal values. The selected rounding mode is {@linkplain Math#round nearest
     * integer} in this implementation.
     *
     * @since 2.2
     */
    public GeneralGridRange(final Envelope envelope) {
        final int dimension = envelope.getDimension();
        index = new int[dimension * 2];
        for (int i=0; i<dimension; i++) {
            // See "note about conversion of floating point values to integers" in the JavaDoc.
            index[i            ] = (int)Math.round(envelope.getMinimum(i));
            index[i + dimension] = (int)Math.round(envelope.getMaximum(i));
        }
    }
    
    /**
     * Returns the number of dimensions.
     */
    public int getDimension() {
        return index.length/2;
    }
    
    /**
     * Returns the valid minimum inclusive grid coordinate along the specified dimension.
     *
     * @see #getLowers
     */
    public int getLower(final int dimension) {
        if (dimension < index.length/2) {
            return index[dimension];
        }
        throw new ArrayIndexOutOfBoundsException(dimension);
    }
    
    /**
     * Returns the valid maximum exclusive grid coordinate along the specified dimension.
     *
     * @see #getUppers
     */
    public int getUpper(final int dimension) {
        if (dimension >= 0) {
            return index[dimension + index.length/2];
        }
        else throw new ArrayIndexOutOfBoundsException(dimension);
    }
    
    /**
     * Returns the number of integer grid coordinates along the specified dimension.
     * This is equals to {@code getUpper(dimension)-getLower(dimension)}.
     */
    public int getLength(final int dimension) {
        return index[dimension+index.length/2] - index[dimension];
    }

    /**
     * Returns the valid minimum inclusive grid coordinates along all dimensions.
     */
    public int[] getLowers() {
        final int[] lo = new int[index.length/2];
        System.arraycopy(index, 0, lo, 0, lo.length);
        return lo;
    }

    /**
     * Returns the valid maximum exclusive grid coordinates along all dimensions.
     */
    public int[] getUppers() {
        final int[] hi = new int[index.length/2];
        System.arraycopy(index, index.length/2, hi, 0, hi.length);
        return hi;
    }
    
    /**
     * Returns a new grid range that encompass only some dimensions of this grid range.
     * This method copy this grid range's index into a new grid range, beginning at
     * dimension {@code lower} and extending to dimension {@code upper-1}.
     * Thus the dimension of the subgrid range is {@code upper-lower}.
     *
     * @param  lower The first dimension to copy, inclusive.
     * @param  upper The last  dimension to copy, exclusive.
     * @return The subgrid range.
     * @throws IndexOutOfBoundsException if an index is out of bounds.
     */
    public GeneralGridRange getSubGridRange(final int lower, final int upper) {
        final int curDim = index.length/2;
        final int newDim = upper-lower;
        if (lower<0 || lower>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                "lower", new Integer(lower)));
        }
        if (newDim<0 || upper>curDim) {
            throw new IndexOutOfBoundsException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                                "upper", new Integer(upper)));
        }
        final GeneralGridRange gridRange = new GeneralGridRange(newDim);
        System.arraycopy(index, lower,        gridRange.index, 0,      newDim);
        System.arraycopy(index, lower+curDim, gridRange.index, newDim, newDim);
        return gridRange;
    }
    
    /**
     * Returns a {@link Rectangle} with the same bounds as this {@code GeneralGridRange}.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this grid range is not two-dimensional.
     */
    public Rectangle toRectangle() throws IllegalStateException {
        if (index.length == 4) {
            return new Rectangle(index[0], index[1], index[2]-index[0], index[3]-index[1]);
        } else {
            throw new IllegalStateException(Errors.format(ErrorKeys.NOT_TWO_DIMENSIONAL_$1,
                                            new Integer(getDimension())));
        }
    }
    
    /**
     * Returns a hash value for this grid range. This value need not remain
     * consistent between different implementations of the same class.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (index != null) {
            for (int i=index.length; --i>=0;) {
                code = code*31 + index[i];
            }
        }
        return code;
    }
    
    /**
     * Compares the specified object with this grid range for equality.
     */
    public boolean equals(final Object object) {
        if (object instanceof GeneralGridRange) {
            final GeneralGridRange that = (GeneralGridRange) object;
            return Arrays.equals(this.index, that.index);
        }
        return false;
    }
    
    /**
     * Returns a string représentation of this grid range. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    public String toString() {
        final int dimension = index.length/2;
        final StringBuffer buffer=new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        for (int i=0; i<dimension; i++) {
            if (i!=0) {
                buffer.append(", ");
            }
            buffer.append(index[i]);
            buffer.append("..");
            buffer.append(index[i+dimension]);
        }
        buffer.append(']');
        return buffer.toString();
    }
}
