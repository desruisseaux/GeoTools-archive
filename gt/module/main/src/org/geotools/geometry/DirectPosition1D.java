/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
import java.io.Serializable;

import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * Holds the coordinates for a one-dimensional position within some coordinate reference system.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DirectPosition1D implements DirectPosition, Serializable, Cloneable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3235094562875693710L;

    /**
     * The coordinate reference system for this position;
     */
    private CoordinateReferenceSystem crs;

    /**
     * The ordinate value.
     */
    public double ordinate;
    
    /**
     * Construct a position initialized to (0) with a <code>null</code>
     * coordinate reference system.
     */
    public DirectPosition1D() {
    }
    
    /**
     * Construct a position with the specified coordinate reference system.
     */
    public DirectPosition1D(final CoordinateReferenceSystem crs) {
        setCoordinateReferenceSystem(crs);
    }
    
    /**
     * Construct a 1D position from the specified ordinate.
     */
    public DirectPosition1D(final double ordinate) {
        this.ordinate = ordinate;
    }
    
    /**
     * Construct a position initialized to the same values than the specified point.
     */
    public DirectPosition1D(final DirectPosition point) {
        setLocation(point);
    }

    /**
     * Returns always <code>this</code>, the direct position for this
     * {@linkplain org.opengis.spatialschema.geometry.geometry.Position position}.
     */
    public DirectPosition getPosition() {
        return this;
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
        GeneralDirectPosition.checkCoordinateReferenceSystemDimension(crs, 1);
        this.crs = crs;
    }

    /**
     * The length of coordinate sequence (the number of entries).
     * This is always 1 for <code>DirectPosition1D</code> objects.
     *
     * @return The dimensionality of this position.
     */
    public final int getDimension() {
        return 1;
    }

    /**
     * Returns a sequence of numbers that hold the coordinate of this position in its
     * reference system.
     *
     * @return The coordinates
     */
    public double[] getCoordinates() {
        return new double[] {ordinate};
    }

    /**
     * Returns the ordinate at the specified dimension.
     *
     * @param  dimension The dimension, which must be 0.
     * @return The {@linkplain #ordinate}.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     *
     * @todo Provides a more detailled error message.
     */
    public final double getOrdinate(final int dimension) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            return ordinate;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param  dimension The dimension, which must be 0.
     * @param  value the ordinate value.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     *
     * @todo Provides a more detailled error message.
     */
    public final void setOrdinate(int dimension, double value) throws IndexOutOfBoundsException {
        if (dimension == 0) {
            ordinate = value;
        } else {
            throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
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
        GeneralDirectPosition.ensureDimensionMatch("position", position.getDimension(), 1);
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        ordinate = position.getOrdinate(0);
    }
    
    /**
     * Returns a string representation of this coordinate. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    public String toString() {
        return GeneralDirectPosition.toString(this, getCoordinates());
    }
    
    /**
     * Returns a hash value for this coordinate. This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        final long value = Double.doubleToLongBits(ordinate);
        int code = (int)value ^ (int)(value >>> 32);
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }

    /**
     * Returns a copy of this position.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable.
            throw new AssertionError(exception);
        }
    }
}
