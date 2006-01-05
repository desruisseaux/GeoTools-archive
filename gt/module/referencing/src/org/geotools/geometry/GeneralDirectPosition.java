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
import java.awt.geom.Point2D;
import java.io.Serializable;

// OpenGIS dependencies
import org.opengis.referencing.cs.AxisDirection; // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.measure.CoordinateFormat;


/**
 * Holds the coordinates for a position within some coordinate reference system. Since
 * {@code DirectPosition}s, as data types, will often be included in larger objects
 * (such as {@linkplain org.geotools.geometry.Geometry geometries}) that have references
 * to {@link CoordinateReferenceSystem}, the {@link #getCoordinateReferenceSystem} method
 * may returns {@code null} if this particular {@code DirectPosition} is included
 * in a larger object with such a reference to a {@linkplain CoordinateReferenceSystem
 * coordinate reference system}. In this case, the cordinate reference system is implicitly
 * assumed to take on the value of the containing object's {@link CoordinateReferenceSystem}.
 * <p>
 * This particular implementation of {@code DirectPosition} is said "General" because it
 * uses an {@linkplain #ordinates array of ordinates} of an arbitrary length. If the direct
 * position is know to be always two-dimensional, then {@link DirectPosition2D} may provides
 * a more efficient implementation.
 * <p>
 * Most methods in this implementation are final for performance reason.
 * 
 * @since 2.0
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see java.awt.geom.Point2D
 */
public class GeneralDirectPosition implements DirectPosition, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 9071833698385715524L;

    /**
     * The format for {@link #toString}. Will be created only when first needed.
     */
    private static CoordinateFormat format;

    /**
     * The ordinates of the direct position.
     */
    public final double[] ordinates;

    /**
     * The coordinate reference system for this position, or {@code null}.
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs a position using the specified coordinate reference system.
     * The number of dimensions is inferred from the coordinate reference system.
     *
     * @since 2.2
     */
    public GeneralDirectPosition(final CoordinateReferenceSystem crs) {
        this(crs.getCoordinateSystem().getDimension());
        this.crs = crs;
    }

    /**
     * Constructs a position with the specified number of dimensions.
     *
     * @param  numDim Number of dimensions.
     * @throws NegativeArraySizeException if {@code numDim} is negative.
     */
    public GeneralDirectPosition(final int numDim) throws NegativeArraySizeException {
        ordinates = new double[numDim];
    }

    /**
     * Constructs a position with the specified ordinates.
     * The {@code ordinates} array will be copied.
     */
    public GeneralDirectPosition(final double[] ordinates) {
        this.ordinates = (double[]) ordinates.clone();
    }

    /**
     * Constructs a 2D position from the specified ordinates. Despite their name, the
     * (<var>x</var>,<var>y</var>) coordinates don't need to be oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}).
     * See the {@link DirectPosition2D} javadoc for details.
     */
    public GeneralDirectPosition(final double x, final double y) {
        ordinates = new double[] {x,y};
    }

    /**
     * Constructs a 3D position from the specified ordinates. Despite their name, the
     * (<var>x</var>,<var>y</var>,<var>z</var>) coordinates don't need to be oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North},
     * {@linkplain AxisDirection#UP Up}).
     */
    public GeneralDirectPosition(final double x, final double y, final double z) {
        ordinates = new double[] {x,y,z};
    }

    /**
     * Constructs a position from the specified {@link Point2D}.
     */
    public GeneralDirectPosition(final Point2D point) {
        this(point.getX(), point.getY());
    }

    /**
     * Constructs a position initialized to the same values than the specified point.
     *
     * @since 2.2
     */
    public GeneralDirectPosition(final DirectPosition point) {
        ordinates = (double[]) point.getCoordinates(); // Should already be cloned.
        crs = point.getCoordinateReferenceSystem();
    }

    /**
     * Returns always {@code this}, the direct position for this
     * {@linkplain org.opengis.spatialschema.geometry.geometry.Position position}.
     */
    public DirectPosition getPosition() {
        return this;
    }

    /**
     * Returns the coordinate reference system in which the coordinate is given.
     * May be {@code null} if this particular {@code DirectPosition} is included
     * in a larger object with such a reference to a {@linkplain CoordinateReferenceSystem
     * coordinate reference system}.
     *
     * @return The coordinate reference system, or {@code null}.
     */
    public final CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Set the coordinate reference system in which the coordinate is given.
     *
     * @param crs The new coordinate reference system, or {@code null}.
     * @throws MismatchedDimensionException if the specified CRS doesn't have the expected
     *         number of dimensions.
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs)
            throws MismatchedDimensionException
    {
        checkCoordinateReferenceSystemDimension(crs, getDimension());
        this.crs = crs;
    }

    /**
     * Convenience method for checking coordinate reference system validity.
     *
     * @param  crs The coordinate reference system to check.
     * @param  expected the dimension expected.
     * @throws MismatchedDimensionException if the CRS dimension is not valid.
     */
    static void checkCoordinateReferenceSystemDimension(final CoordinateReferenceSystem crs,
                                                        final int expected)
            throws MismatchedDimensionException
    {
        if (crs != null) {
            final int dimension = crs.getCoordinateSystem().getDimension();
            if (dimension != expected) {
                throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3,
                          crs.getName().getCode(), new Integer(dimension), new Integer(expected)));
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
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3,
                        name, new Integer(dimension), new Integer(expectedDimension)));
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
     * @return A copy of the {@linkplain #ordinates coordinates}.
     */
    public final double[] getCoordinates() {
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
     *
     * @since 2.2
     */
    public final void setLocation(final DirectPosition position) throws MismatchedDimensionException {
        ensureDimensionMatch("position", position.getDimension(), ordinates.length);
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        for (int i=0; i<ordinates.length; i++) {
            ordinates[i] = position.getOrdinate(i);
        }
    }

    /**
     * Set this coordinate to the specified direct position. This method is identical to
     * {@link #setLocation(DirectPosition)}, but is slightly faster in the special case
     * of an {@code GeneralDirectPosition} implementation.
     *
     * @param  position The new position for this point.
     * @throws MismatchedDimensionException if this point doesn't have the expected dimension.
     */
    public final void setLocation(final GeneralDirectPosition position) throws MismatchedDimensionException {
        ensureDimensionMatch("position", position.ordinates.length, ordinates.length);
        setCoordinateReferenceSystem(position.crs);
        System.arraycopy(position.ordinates, 0, ordinates, 0, ordinates.length);
    }

    /**
     * Set this coordinate to the specified {@link Point2D}.
     * This coordinate must be two-dimensional.
     *
     * @param  point The new coordinate for this point.
     * @throws MismatchedDimensionException if this coordinate point is not two-dimensional.
     */
    public final void setLocation(final Point2D point) throws MismatchedDimensionException {
        if (ordinates.length != 2) {
            throw new MismatchedDimensionException(Errors.format(ErrorKeys.NOT_TWO_DIMENSIONAL_$1,
                                                   new Integer(ordinates.length)));
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
        if (ordinates.length != 2) {
            throw new IllegalStateException(Errors.format(ErrorKeys.NOT_TWO_DIMENSIONAL_$1,
                                            new Integer(ordinates.length)));
        }
        return new Point2D.Double(ordinates[0], ordinates[1]);
    }

    /**
     * Returns a string representation of this coordinate. The default implementation formats
     * this coordinate using a shared instance of {@link org.geotools.measure.CoordinateFormat}.
     * This is okay for occasional formatting (for example for debugging purpose). But if there
     * is a lot of positions to format, users will get better performance and more control by
     * using their own instance of {@link org.geotools.measure.CoordinateFormat}.
     */
    public String toString() {
        return toString(this);
    }

    /**
     * Format the specified position.
     * <strong>NOTE:</strong> This convenience method uses a shared instance of
     * {@link CoordinateFormat}. This is okay for occasional formatting. But if
     * a lot of position needs to be formatted, it is more efficient to use an
     * other instance of {@link CoordinateFormat}.
     */
    static synchronized String toString(final DirectPosition position) {
        if (format == null) {
            format = new CoordinateFormat();
        }
        format.setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        return format.format(position);
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
        return new GeneralDirectPosition(ordinates);
    }
}
