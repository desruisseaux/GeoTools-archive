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
import java.awt.geom.Point2D;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// OpenGIS dependencies
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.spatialschema.geometry.primitive.Point;
import org.opengis.spatialschema.geometry.geometry.Position;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;


/**
 * Holds the coordinates for a two-dimensional position within some coordinate reference system.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DirectPosition2D extends Point2D.Double implements DirectPosition, Position {
    /**
     * The coordinate reference system for this position;
     */
    private CoordinateReferenceSystem crs;
    
    /**
     * Construct a position initialized to (0,0) with a <code>null</code>
     * coordinate reference system.
     */
    public DirectPosition2D() {
    }
    
    /**
     * Construct a position with the specified coordinate reference system.
     */
    public DirectPosition2D(final CoordinateReferenceSystem crs) {
        setCoordinateReferenceSystem(crs);
    }
    
    /**
     * Construct a 2D position from the specified ordinates.
     */
    public DirectPosition2D(final double x, final double y) {
        super(x,y);
    }
    
    /**
     * Construct a position from the specified {@link Point2D}.
     */
    public DirectPosition2D(final Point2D point) {
        this(point.getX(), point.getY());
    }
    
    /**
     * Construct a position initialized to the same values than the specified point.
     */
    public DirectPosition2D(final DirectPosition point) {
        setLocation(point);
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
        org.geotools.geometry.DirectPosition.checkCoordinateReferenceSystemDimension(crs, 2);
        this.crs = crs;
    }

    /**
     * The length of coordinate sequence (the number of entries).
     * This is always 2 for <code>DirectPosition2D</code> objects.
     *
     * @return The dimensionality of this position.
     */
    public final int getDimension() {
        return 2;
    }

    /**
     * Returns a sequence of numbers that hold the coordinate of this position in its
     * reference system.
     *
     * @return The coordinates
     */
    public double[] getCoordinates() {
        return new double[] {x,y};
    }

    /**
     * Returns the ordinate at the specified dimension.
     *
     * @param  dimension The dimension in the range 0 to 1 inclusive.
     * @return The coordinate at the specified dimension.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     *
     * @todo Provides a more detailled error message.
     */
    public final double getOrdinate(int dimension) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:  return x;
            case 1:  return y;
            default: throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param dimension the dimension for the ordinate of interest.
     * @param value the ordinate value of interest.
     * @throws IndexOutOfBoundsException if the specified dimension is out of bounds.
     *
     * @todo Provides a more detailled error message.
     */
    public final void setOrdinate(int dimension, double value) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:  x=value; break;
            case 1:  y=value; break;
            default: throw new IndexOutOfBoundsException(String.valueOf(dimension));
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
        org.geotools.geometry.DirectPosition.ensureDimensionMatch("position", position.getDimension(), 2);
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        x = position.getOrdinate(0);
        y = position.getOrdinate(1);
    }

    /**
     * Returns the direct position, which is always <code>this</code>.
     * This method is implemented in order to meet the {@link Position} contract.
     *
     * @return Always <code>this</code>.
     */
    public org.opengis.spatialschema.geometry.DirectPosition getDirect() {
        return this;
    }
    
    /**
     * Returns the point, which is <code>null</code>.
     * This method is implemented in order to meet the {@link Position} contract.
     *
     * @return Always <code>null</code>.
     */
    public Point getIndirect() {
        return null;
    }
    
    /**
     * Returns a {@link Point2D} with the same coordinate as this direct position.
     */
    public Point2D toPoint2D() {
        return new Point2D.Double(x,y);
    }
    
    /**
     * Returns a string representation of this coordinate. The returned string is
     * implementation dependent. It is usually provided for debugging purposes.
     */
    public String toString() {
        return org.geotools.geometry.DirectPosition.toString(this, getCoordinates());
    }
    
    /**
     * Returns a hash value for this coordinate. This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode() {
        int code = super.hashCode();
        if (crs != null) {
            code ^= crs.hashCode();
        }
        return code;
    }
}
