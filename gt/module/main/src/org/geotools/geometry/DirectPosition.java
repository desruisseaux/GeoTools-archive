/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.geometry;

// J2SE dependencies
import java.util.Locale;
import java.util.Arrays;
import java.io.Serializable;
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * Holds the coordinates for a position within some coordinate reference system. Since
 * <code>DirectPosition</code>s, as data types, will often be included in larger objects
 * (such as {@linkplain org.geotools.geometry.Geometry geometries}) that have references
 * to {@link CoordinateReferenceSystem}, the {@link #getCoordinateReferenceSystem} method
 * may returns <code>null</code> if this particular <code>DirectPosition</code> is included
 * in a larger object with such a reference to a {@linkplain CoordinateReferenceSystem
 * coordinate reference system}. In this case, the cordinate reference system is implicitly
 * assumed to take on the value of the containing object's {@link CoordinateReferenceSystem}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see java.awt.geom.Point2D
 */
public class DirectPosition implements org.opengis.spatialschema.geometry.DirectPosition, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 9071833698385715524L;
    
    /**
     * The ordinates of the direct position. Consider as final.
     * The only method to modify the reference is {@link #clone}.
     * This array is read and written by {@link Envelope}.
     */
    double[] ordinates;

    /**
     * The coordinate reference system for this position, or <code>null</code>.
     */
    private CoordinateReferenceSystem crs;
    
    /**
     * Construct a position with the specified number of dimensions.
     *
     * @param  numDim Number of dimensions.
     * @throws NegativeArraySizeException if <code>numDim</code> is negative.
     */
    public DirectPosition(final int numDim) throws NegativeArraySizeException {
        ordinates = new double[numDim];
    }
    
    /**
     * Construct a position with the specified ordinates.
     * The <code>ordinates</code> array will be copied.
     */
    public DirectPosition(final double[] ordinates) {
        this.ordinates = (double[]) ordinates.clone();
    }
    
    /**
     * Construct a 2D position from the specified ordinates.
     */
    public DirectPosition(final double x, final double y) {
        ordinates = new double[] {x,y};
    }
    
    /**
     * Construct a 3D position from the specified ordinates.
     */
    public DirectPosition(final double x, final double y, final double z) {
        ordinates = new double[] {x,y,z};
    }
    
    /**
     * Construct a position from the specified {@link Point2D}.
     */
    public DirectPosition(final Point2D point) {
        this(point.getX(), point.getY());
    }
    
    /**
     * Construct a position initialized to the same values than the specified point.
     */
    public DirectPosition(final DirectPosition point) {
        ordinates = (double[]) point.ordinates.clone();
    }

    /**
     * Returns the coordinate reference system in which the coordinate is given.
     * May be <code>null</code> if this particular <code>DirectPosition</code> is included
     * in a larger object with such a reference to a {@linkplain CoordinateReferenceSystem
     * coordinate reference system}.
     *
     * @return The coordinate reference system, or <code>null</code>.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Set the coordinate reference system in which the coordinate is given.
     *
     * @param crs The new coordinate reference system, or <code>null</code>.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        checkCoordinateReferenceSystemDimension(crs, getDimension());
        this.crs = crs;
    }

    /**
     * Convenience method for checking coordinate reference system validity.
     *
     * @param  crs The coordinate reference system to check.
     * @param  minimalDimension the minimum dimension expected.
     * @throws IllegalArgumentException if the CRS dimension is not valid.
     */
    static void checkCoordinateReferenceSystemDimension(final CoordinateReferenceSystem crs,
                                                        final int minimalDimension)
    {
        if (crs != null) {
            final int dimension = crs.getCoordinateSystem().getDimension();
            if (dimension < minimalDimension) {
                throw new IllegalArgumentException(Resources.format(
                       ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3, crs.getName(Locale.getDefault()),
                                    new Integer(dimension), new Integer(minimalDimension)));
            }
        }
    }
    
    /**
     * Convenience method for checking object dimension validity.
     * This method is usually invoked for argument checking.
     *
     * @param  name The name of the argument to check.
     * @param  dimension The object dimension.
     * @param  expectedDimension The Expected dimension for the object.
     * @throws MismatchedDimensionException if the object doesn't have the expected dimension.
     */
    static void ensureDimensionMatch(final String name,
                                     final int dimension,
                                     final int expectedDimension)
        throws MismatchedDimensionException
    {
        if (dimension != expectedDimension) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_MISMATCHED_DIMENSION_$3, name,
                        new Integer(dimension), new Integer(expectedDimension)));
        }
    }

    /**
     * The length of coordinate sequence (the number of entries).
     * This may be less than or equal to the dimensionality of the 
     * {@linkplain #getCoordinateReferenceSystem() coordinate reference system}.
     *
     * @return The dimensionality of this position.
     */
    public final int getDimension() {
        return ordinates.length;
    }

    /**
     * Returns a sequence of numbers that hold the coordinate of this position in its
     * reference system.
     *
     * @return The coordinates
     */
    public double[] getCoordinates() {
        return (double[]) ordinates.clone();
    }

    /**
     * Returns the ordinate at the specified dimension.
     *
     * @param  dimension The dimension in the range 0 to {@linkplain #getDimension dimension}-1.
     * @return The coordinate at the specified dimension.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    public final double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        return ordinates[dimension];
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param dimension the dimension for the ordinate of interest.
     * @param value the ordinate value of interest.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     */
    public final void setOrdinate(int dimension, double value) throws IndexOutOfBoundsException {
        ordinates[dimension] = value;
    }

    /**
     * Set this coordinate to the specified direct position. If the specified position
     * contains a {@linkplain CoordinateReferenceSystem coordinate reference system},
     * then the CRS for this position will be set to the CRS of the specified position.
     *
     * @param  position The new position for this point.
     * @throws MismatchedDimensionException if this point doesn't have the expected dimension.
     */
    public void setLocation(final DirectPosition position) throws MismatchedDimensionException {
        ensureDimensionMatch("position", position.ordinates.length, getDimension());
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        System.arraycopy(position.ordinates, 0, ordinates, 0, ordinates.length);
    }

    /**
     * Set this coordinate to the specified {@link Point2D}.
     * This coordinate must be two-dimensional.
     *
     * @param  point The new coordinate for this point.
     * @throws MismatchedDimensionException if this coordinate point is not two-dimensional.
     */
    public void setLocation(final Point2D point) throws MismatchedDimensionException {
        if (ordinates.length != 2) {
            throw new MismatchedDimensionException(Resources.format(
                        ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(ordinates.length)));
        }
        ordinates[0] = point.getX();
        ordinates[1] = point.getY();
    }
    
    /**
     * Returns a {@link Point2D} with the same coordinate as this direct position.
     * This is a convenience method for interoperability with Java2D.
     *
     * @throws IllegalStateException if this coordinate point is not two-dimensional.
     */
    public Point2D toPoint2D() throws IllegalStateException {
        if (ordinates.length == 2) {
            return new Point2D.Double(ordinates[0], ordinates[1]);
        } else {
            throw new IllegalStateException(Resources.format(
                        ResourceKeys.ERROR_NOT_TWO_DIMENSIONAL_$1, new Integer(ordinates.length)));
        }
    }
    
    /**
     * Returns a string representation of this coordinate. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    public String toString() {
        return toString(this, ordinates);
    }
    
    /**
     * Returns a string representation of an object. The returned string is implementation
     * dependent. It is usually provided for debugging purposes.
     */
    static String toString(final Object owner, final double[] ordinates) {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(owner));
        buffer.append('[');
        for (int i=0; i<ordinates.length; i++) {
            if (i!=0) buffer.append(", ");
            buffer.append(ordinates[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }
    
    /**
     * Returns a hash value for this coordinate. This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = hashCode(ordinates);
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }

    /**
     * Returns a hash value for the specified ordinates.
     *
     * @todo Remove this method when we will alowed to use J2SE 1.5 runtime.
     */
    static int hashCode(final double[] ordinates) {
        long code = (int)serialVersionUID;
        if (ordinates != null) {
            for (int i=ordinates.length; --i>=0;) {
                code = code*31 + Double.doubleToLongBits(ordinates[i]);
            }
        }
        return (int)(code >>> 32) ^ (int)code;
    }
    
    /**
     * Returns a deep copy of this position.
     */
    public Object clone() {
        try {
            DirectPosition p = (DirectPosition) super.clone();
            p.ordinates = (double[]) p.ordinates.clone();
            return p;
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }
}
