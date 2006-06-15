/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
 * (C) 1999, P�ches et Oc�ans Canada
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
package org.geotools.geometry.array;

// J2SE dependencies
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.RandomAccess;
import java.util.List;

// GeoAPI dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.geometry.PointArray;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.geometry.DirectPosition2D;


/**
 * Abstract class for {@link PointArray} implementations in a two-dimensional space.
 * This class wraps an array of positions as (<var>x</var>,<var>y</var>) ordinates.
 * While this class supports random access, users are strongly encouranged to use
 * sequential access only, as provided by the {@link #iterator} method. This is because
 * random access may be very costly for some {@code PointArray} implementations, especially
 * the ones that compress their data.
 *
 * @since 2.2
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class PointArray2D implements PointArray, Serializable {
    /**
     * Serial version for compatibility with previous version.
     */
    private static final long serialVersionUID = -4959895593401691530L;

    /**
     * Constructs a new array.
     */
    protected PointArray2D() {
    }

    /**
     * Returns the dimensionality of the coordinates in this array, which is always 2.
     */
    public final int getDimension() {
        return 2;
    }

    /**
     * Returns the coordinate reference system of this array. The default implementation returns
     * {@code null}, which means that this point array is usually included in a larger geometry
     * object and its CRS is implicitly assumed to take on the value of the containing object's
     * coordinate reference system. Subclasses may override this method if they want to specify
     * a non-null CRS.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    /**
     * Returns {@code true} if the specified CRS is compatible with the content of this array.
     */
    private boolean isCompatible(final CoordinateReferenceSystem crs) {
        if (crs != null) {
            final CoordinateReferenceSystem expected = getCoordinateReferenceSystem();
            if (expected != null) {
                return CRS.equalsIgnoreMetadata(crs, expected);
            }
        }
        return true;
    }

    /**
     * Returns the index of the first valid ordinate.
     *
     * This method is overriden by all {@code PointArray2D} subclasses in this package.
     * Note that this method is not {@code protected} in this {@code PointArray2D} class
     * because it is used only by {@link #capacity}, which is a package-private helper
     * method for {@link #toFloatArray} implementations only.
     *
     * @see #checkRange
     */
    int lower() {
        return 0;
    }

    /**
     * Returns the index after the last valid ordinate.
     *
     * This method is overriden by all {@code PointArray2D} subclasses in this package.
     * Note that this method is not {@code protected} in this {@code PointArray2D} class
     * because it is used only by {@link #capacity}, which is a package-private helper
     * method for {@link #toFloatArray} implementations only.
     *
     * @see #checkRange
     */
    int upper() {
        return 2*length();
    }

    /**
     * Returns the number of points in this array.
     */
    public abstract int length();

    /**
     * Returns an estimation of memory usage in bytes. This method is for information
     * purpose only. The memory used by this array may be shared with an other array,
     * resulting in a total memory consumption lower than the sum of {@code getMemoryUsage()}
     * return values. Furthermore, this method does not take in account the extra bytes
     * generated by Java Virtual Machine for each objects.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    public abstract long getMemoryUsage();

    /**
     * Gets a copy of the coordinates at the particular location in this {@code PointArray}.
     * If the {@code dest} argument is non-null, that object will be populated with the value
     * from the array.
     * <p>
     * <strong>Warning:</strong> the default implementation is slow. Avoid this method (use
     * the {@linkplain #iterator iterator} instead), unless the {@code PointArray} subclass
     * implements the {@link RandomAccess} interface.
     *
     * @param  index The location in the array, from 0 inclusive to the array
     *               {@linkplain #length length} exclusive.
     * @param  dest An optionnaly pre-allocated direct position. If non-null, its
     *         {@linkplain DirectPosition#getCoordinateReferenceSystem associated CRS} must be null
     *         or compatible with {@linkplain #getCoordinateReferenceSystem this array CRS}. For
     *         performance reason, it will not be verified unless assertions are enabled.
     * @return The {@code dest} argument, or a new object if {@code dest} was null.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public DirectPosition get(final int index, DirectPosition dest)
            throws IndexOutOfBoundsException
    {
        final PointIterator it = iterator(index);
        if (dest != null) {
            assert isCompatible(dest.getCoordinateReferenceSystem()) : dest;
            dest.setOrdinate(0, it.nextX());
            dest.setOrdinate(1, it.nextY());
        } else {
            dest = new DirectPosition2D(getCoordinateReferenceSystem(), it.nextX(), it.nextY());
        }
        return dest;
    }

    /**
     * Sets the point at the given index. The default implementation always throw an
     * {@link UnsupportedOperationException}.
     *
     * @param  index The location in the array, from 0 inclusive to the array
     *         {@linkplain #length length} exclusive.
     * @param  position The point to set at the given location in this array.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     * @throws UnsupportedOperationException if this array is immutable, or if this operation
     *         is not supported for some other reason (e.g. the data are compressed).
     */
    public void set(final int index, final DirectPosition position)
            throws IndexOutOfBoundsException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException(Errors.format(ErrorKeys.UNMODIFIABLE_GEOMETRY));
    }

    /**
     * Returns a view of the points in this array as a list of {@linkplain Position positions}.
     * The list is backed by this {@code PointArray}, so changes to the point array are
     * reflected in the list, and vice-versa.
     * <p>
     * Note that random access may be costly in some implementations. If the returned list
     * doesn't implement the {@link RandomAccess} interface, then consider avoiding the
     * {@link List#get(int)} method. Favor the {@linkplain List#iterator list iterator} instead.
     *
     * @return The list of positions in this array.
     */
    public List/*<Position>*/ positions() {
        if (this instanceof RandomAccess) {
            return new ListAdapter(this);
        } else {
            return new SequentialListAdapter(this);
        }
    }

    /**
     * Returns an iterator over the point coordinates.
     *
     * @param  index Index of the first point to returns in the iteration, from 0 inclusive
     *         to the array {@linkplain #length length} exclusive.
     * @return The iterator.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public abstract PointIterator iterator(final int index) throws IndexOutOfBoundsException;

    /**
     * Returns an iterator object that iterates along the point coordinates.
     * If an optional affine transform is specified, the coordinates returned
     * in the iteration are transformed accordingly.
     *
     * @see #toShape
     * @see ShapeAdapter#getPathIterator(AffineTransform)
     */
    PathIterator getPathIterator(final AffineTransform at) {
        return new ShapeAdapter.Iterator(iterator(0), at);
    }
    
    /**
     * Returns the bounding box of all <var>x</var> and <var>y</var> ordinates. If this array
     * is empty, then this method returns {@code null}. The default implementation iterates
     * through all coordinates provided by {@link PointIterator}. Subclasses should override
     * this method with a more efficient implementation.
     */
    public Rectangle2D getBounds2D() {
        float xmin = Float.POSITIVE_INFINITY;
        float xmax = Float.NEGATIVE_INFINITY;
        float ymin = Float.POSITIVE_INFINITY;
        float ymax = Float.NEGATIVE_INFINITY;
        final PointIterator it = iterator(0);
        while (it.hasNext()) {
            final float x = it.nextX();
            final float y = it.nextY();
            if (x < xmin) xmin = x;
            if (x > xmax) xmax = x;
            if (y < ymin) ymin = y;
            if (y > ymax) ymax = y;
        }
        if (xmin<=xmax && ymin<=ymax) {
            return new Rectangle2D.Float(xmin, ymin, xmax-xmin, ymax-ymin);
        } else {
            return null;
        }
    }

    /**
     * Returns an array that share the points in this array from point {@code lower} inclusive
     * to {@code upper} exclusive. If the subarray doesn't contains any point (i.e. if
     * {@code lower==upper}), then this method returns {@code null}.
     *
     * @param lower Index of the first point, inclusive.
     * @param upper Index of the last point, exclusive.
     */
    public abstract PointArray2D subarray(final int lower, final int upper);

    /**
     * Inserts all points from {@code toMerge} into {@code this} at position {@code index}.
     * If {@code reverse} is {@code true}, then the points are inserted in reverse order.
     *
     * @param  index Index where to insert the first point in this array.
     * @param  toMerge The points to insert.
     * @param  reverse {@code true} for inserting the coordinates in reverse order.
     */
    public final PointArray2D insertAt(final int index, final PointArray2D toMerge,
                                       final boolean reverse)
    {
        return toMerge.insertInto(this, index, reverse);
    }

    /**
     * Inserts all points from {@code this} into the specified array. This method is strictly
     * reserved to {@link #insertAt(int,PointArray2D,boolean)} implementation. The default
     * implementation copies the data using {@link #toFloatArray()}. Class {@link DefaultArray}
     * remplaces this implementation by a new one which avoid this copy.
     */
    PointArray2D insertInto(final PointArray2D dest, final int index, final boolean reverse) {
        final float[] array = toFloatArray();
        return dest.insertAt(index, array, 0, array.length, reverse);
    }

    /**
     * Inserts an array of (<var>x</var>,<var>y</var>) coordinates at position {@code index}.
     * If {@code reverse} is {@code true}, then the points are inserted in reverse order.
     *
     * @param  index Index where to insert the first point in this array.
     * @param  toMerge The points to insert as an array of (<var>x</var>,<var>y</var>) coordinates.
     * @param  lower Index (inclusive) of the first ordinates to copy from {@code toMerge}.
     * @param  upper Index (exclusive) of the last ordinates to copy from {@code toMerge}.
     * @param  reverse {@code true} for inserting the coordinates in reverse order.
     * @return An array which contains all data from this array, with the {@code toMerge} data
     *         inserted into. May returns {@code this} if the insertion has been applied in-place.
     */
    public abstract PointArray2D insertAt(final int index, final float toMerge[],
                                          final int lower, final int upper, final boolean reverse);

    /**
     * Reverts all coordinate points order in this array. The last point become first, and
     * the first point become last.
     *
     * @return All array which contains the points in reversed order. May returns {@code this}
     *         if the operation has been applied in-place.
     */
    public abstract PointArray2D reverse();

    /**
     * Returns an immutable copy of this array. Invoking any {@link #insertAt insertAt} or
     * {@link #reverse reverse} method on the returned array while result in a new array
     * being returned by the above-cited methods, instead of performing the operations in-place.
     * In addition, this method can optionnaly compress the data.
     *
     * @param  level The compression level, or {@code null} if no compression is wanted.
     * @return An immutable (and optionnaly compressed) copy of this array, or {@code this}
     *         if this array already meet the requirements, or {@code null} if this array
     *         doesn't contain any data.
     */
    public PointArray2D getFinal(final CompressionLevel level) {
        return length()>0 ? this : null;
    }

    /**
     * Appends (<var>x</var>,<var>y</var>) coordinates to the specified destination array.
     * The destination array will be filled starting at index {@link ArrayData#length}.
     * If {@code resolutionSquared} is greater than 0, then points that are closer than
     * {@code sqrt(resolutionSquared)} from previous one will be skiped.
     * <p>
     * <strong>Implementation note</strong><br>
     * Many implementations will compute distances using Pythagoras formulas, which is okay for
     * {@linkplain org.opengis.referencing.crs.ProjectedCRS projected CRS} but not strictly right
     * for {@linkplain org.opengis.referencing.crs.GeographicCRS geographic CRS}. This is not a real
     * problem when the {@linkplain org.geotools.display.canvas.ReferencedCanvas#getObjectiveCRS
     * objective CRS} is the same one than the {@linkplain #getCoordinateReferenceSystem data CRS},
     * since the decimation performed by this {@code toFloatArray} method target specifically the
     * rendering device. However, it may be a problem when the objective CRS is different, since
     * points that are equidistant in the data CRS may not be equidistant in the objective CRS. In
     * order to minimize the deformations induced by map projections, this method should be invoked
     * only on {@code PointArray2D} objects covering a relatively small geographic area.
     * {@code PointArray2D} objects covering a large geographic area shall be splitted into smaller
     * {@code PointArray2D} objects using the {@link #subarray subarray} method.
     *
     * @param  dest The destination array.
     * @param  resolutionSquared The minimum squared distance desired between points.
     *         A value of 0 keep all points.
     */
    public abstract void toFloatArray(ArrayData dest, float resolutionSquared);

    /**
     * Returns a copy of all (<var>x</var>,<var>y</var>) coordinates in this array.
     */
    public final float[] toFloatArray() {
        final ArrayData data = new ArrayData(2*length());
        toFloatArray(data, 0);
        final float[] array = data.getArray();
        assert array.length == data.length() * 2;
        return array;
    }

    /**
     * Used by {@link #toFloatArray} implementations in order to expand the destination array as
     * needed. This method is invoking when {@code toFloatArray} has already started to fill the
     * destination array. The {@code src} and {@code dst} arguments refer to the current position
     * in the copy loop. This method try to guess the size that the destination array should have
     * based on the proportion of the array filled up to date, and conservatively expand its guess
     * by about 12%.
     *
     * @param  src The index position in the source array.
     * @param  dst The index position in the destination array.
     * @param  offset The first element to be filled in the destination array.
     * @return A guess of the required length for completing the array filling.
     */
    final int capacity(int src, int dst, final int offset) {
        final int lower  = lower();
        final int length = upper() - lower;
        int guess;
        dst -= offset;
        src -= lower;
        assert (src & 1) == 0 : src;
        assert (dst & 1) == 0 : dst;
        if (src == 0) {
            guess = length / 8;
        } else {
            guess = (int)(dst * (long)length / src);  // Prediction of the total length required.
            guess -= dst;                             // The amount to growth.
            guess += guess/8;                         // Conservatively add some space.
        }
        guess &= ~1; // Make sure the length is even.
        return offset + Math.min(length, dst+Math.max(guess, 32));
    }

    /**
     * Returns this {@code PointArray2D} as a {@linkplain Shape shape}.  This shape is not
     * designed for map rendering. It is rather a debugging tool, as well as a convenient
     * way to draw lines in some simple context.
     *
     * @param transform An optional transform to apply on coordinates, or {@code null} if none.
     * @return The lines in this {@code PointArray2D} as a Java2D {@linkplain Shape shape}.
     */
    public final Shape toShape(final AffineTransform transform) {
        return new ShapeAdapter(this, transform);
    }

    /**
     * Returns a string representation of this array. This method is mostly for debugging
     * purpose and may change in any future Geotools version. Current implementation returns
     * the class name, the number of points and the start and end coordinates.
     */
    public final String toString() {
        final DirectPosition2D point = new DirectPosition2D();
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        final int length = length();
        buffer.append('[');
        buffer.append(length);
        buffer.append(" points");
        if (length != 0) {
            get(0, point);
            buffer.append(" (");
            buffer.append(point.x);
            buffer.append(", ");
            buffer.append(point.y);
            buffer.append(")-(");

            get(length()-1, point);
            buffer.append(point.x);
            buffer.append(", ");
            buffer.append(point.y);
            buffer.append(')');
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Compares this array with the specified one for equality. Two arrays are considered
     * identical if they contains the same coordinates in the same order. The actual array
     * implementation doesn't matter.
     */
    public final boolean equals(final PointArray2D that) {
        if (that == this) return true;
        if (that == null) return false;
        if (this.length() != that.length()) {
            return false;
        }
        final PointIterator it1 = this.iterator(0);
        final PointIterator it2 = that.iterator(0);
        while (it1.hasNext()) {
            if (!it2.hasNext() ||
                Float.floatToIntBits(it1.nextX()) != Float.floatToIntBits(it2.nextX()) ||
                Float.floatToIntBits(it1.nextY()) != Float.floatToIntBits(it2.nextY())) return false;
        }
        return !it2.hasNext();
    }

    /**
     * Compares this array with the specified object for equality. This method performs the same
     * comparaison than {@link #equals(PointArray2D)}.
     */
    public final boolean equals(final Object that) {
        return (that instanceof PointArray2D) && equals((PointArray2D) that);
    }

    /**
     * Returns a hash value for this array.
     */
    public final int hashCode() {
        final DirectPosition point = get(0, null);
        return length() ^ Float.floatToIntBits((float)point.getOrdinate(0))
                        ^ Float.floatToIntBits((float)point.getOrdinate(1));
    }

    /**
     * Checks the validity of the specified arguments.
     *
     * @param  array The array of (<var>x</var>,<var>y</var>) ordinates.
     * @param  lower Index of the first <var>x</var> ordinate to consider in {@code array}.
     * @param  upper Index after the last <var>y</var> ordinate to consider in {@code array}.
     *         The {@code upper-lower} value must be even.
     * @throws IllegalArgumentException if the {@code [lower..upper]} range is not valid.
     */
    static void checkRange(final float[] array, final int lower, final int upper)
            throws IllegalArgumentException
    {
        assert (array.length & 1) == 0 : array.length;
        assert (lower        & 1) == 0 : lower;
        assert (upper        & 1) == 0 : upper;
        if (upper < lower) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2,
                                               new Integer(lower), new Integer(upper)));
        }
        if (((upper-lower) & 1) !=0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ODD_ARRAY_LENGTH_$1,
                                               new Integer(upper-lower)));
        }
        if (lower < 0) {
            throw new ArrayIndexOutOfBoundsException(lower);
        }
        if (upper > array.length) {
            throw new ArrayIndexOutOfBoundsException(upper);
        }
    }
}
