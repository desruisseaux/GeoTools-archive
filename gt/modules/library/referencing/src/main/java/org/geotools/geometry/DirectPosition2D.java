/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.geometry;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.geotools.resources.Utilities;
import org.opengis.referencing.cs.AxisDirection; // For javadoc
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;


/**
 * Holds the coordinates for a two-dimensional position within some coordinate reference system.
 * <p>
 * <strong>Note:</strong> This class inherits {@linkplain #x x} and {@linkplain #y y} fields. But
 * despite their names, they don't need to be oriented toward {@linkplain AxisDirection#EAST East}
 * and {@linkplain AxisDirection#NORTH North} respectively. The (<var>x</var>,<var>y</var>) axis
 * can have any orientation and should be understood as "ordinate 0" and "ordinate 1" values
 * instead. This is not specific to this implementation; in Java2D too, the visual axis orientation
 * depend on the {@linkplain java.awt.Graphics2D#getTransform affine transform in the graphics
 * context}.
 * <p>
 * The rational for avoiding axis orientation restriction is that other {@link DirectPosition}
 * implementation do not have such restriction, and anyway it would be hard to generalize (what
 * to do with {@linkplain AxisDirection#NORTH_EAST North-East} direction?).
 *
 * @since 2.0
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see DirectPosition1D
 * @see GeneralPosition
 * @see java.awt.geom.Point2D
 */
public class DirectPosition2D extends Point2D.Double implements DirectPosition, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 835130287438466996L;

    /**
     * The coordinate reference system for this position;
     */
    private CoordinateReferenceSystem crs;

    /**
     * Constructs a position initialized to (0,0) with a {@code null}
     * coordinate reference system.
     */
    public DirectPosition2D() {
    }

    /**
     * Constructs a position with the specified coordinate reference system.
     */
    public DirectPosition2D(final CoordinateReferenceSystem crs) {
        setCoordinateReferenceSystem(crs);
    }

    /**
     * Constructs a 2D position from the specified ordinates. Despite their name,
     * the (<var>x</var>,<var>y</var>) coordinates don't need to be oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}).
     * Those parameter names simply match the {@linkplain #x x} and {@linkplain #y y}
     * fields. See the {@linkplain DirectPosition2D class javadoc} for details.
     */
    public DirectPosition2D(final double x, final double y) {
        super(x,y);
    }

    /**
     * Constructs a 2D position from the specified ordinates in the specified CRS. Despite
     * their name, the (<var>x</var>,<var>y</var>) coordinates don't need to be oriented toward
     * ({@linkplain AxisDirection#EAST East}, {@linkplain AxisDirection#NORTH North}).
     * Those parameter names simply match the {@linkplain #x x} and {@linkplain #y y}
     * fields. The actual axis orientations are determined by the specified CRS.
     * See the {@linkplain DirectPosition2D class javadoc} for details.
     */
    public DirectPosition2D(final CoordinateReferenceSystem crs,
                            final double x, final double y)
    {
        super(x,y);
        setCoordinateReferenceSystem(crs);
    }

    /**
     * Constructs a position from the specified {@link Point2D}.
     */
    public DirectPosition2D(final Point2D point) {
        super(point.getX(), point.getY());
        if (point instanceof DirectPosition) {
            setCoordinateReferenceSystem(((DirectPosition) point).getCoordinateReferenceSystem());
        }
    }

    /**
     * Constructs a position initialized to the same values than the specified point.
     */
    public DirectPosition2D(final DirectPosition point) {
        setLocation(point);
    }

    /**
     * Returns always <code>this</code>, the direct position for this
     * {@linkplain org.opengis.geometry.coordinate.Position position}.
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
     */
    public void setCoordinateReferenceSystem(final CoordinateReferenceSystem crs) {
        AbstractDirectPosition.checkCoordinateReferenceSystemDimension(crs, 2);
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
    public final double getOrdinate(final int dimension) throws IndexOutOfBoundsException {
        switch (dimension) {
            case 0:  return x;
            case 1:  return y;
            default: throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Sets the ordinate value along the specified dimension.
     *
     * @param  dimension the dimension for the ordinate of interest.
     * @param  value the ordinate value of interest.
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
        AbstractDirectPosition.ensureDimensionMatch("position", position.getDimension(), 2);
        setCoordinateReferenceSystem(position.getCoordinateReferenceSystem());
        x = position.getOrdinate(0);
        y = position.getOrdinate(1);
    }

    /**
     * Returns a {@link Point2D} with the same coordinate as this direct position.
     */
    public Point2D toPoint2D() {
        return new Point2D.Double(x,y);
    }

    /**
     * Returns a string representation of this coordinate. The default implementation formats
     * this coordinate using a shared instance of {@link org.geotools.measure.CoordinateFormat}.
     * This is okay for occasional formatting (for example for debugging purpose). But if there
     * is a lot of positions to format, users will get better performance and more control by
     * using their own instance of {@link org.geotools.measure.CoordinateFormat}.
     */
    @Override
    public String toString() {
        return AbstractDirectPosition.toString(this);
    }

    /**
     * Returns a hash value for this coordinate. This method do <strong>not</strong> takes
     * the {@linkplain #getCoordinateReferenceSystem coordinate reference system} in account,
     * and can not be overriden on purpose. This is necessary in order to stay consistent with
     * the {@code hashCode} and {@code equals} contract defined in {@link Point2D}.
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Compares this point with the specified object for equality.
     */
    @Override
    public final boolean equals(final Object object) {
        if (!super.equals(object)) {
            return false;
        }
        if (!(object instanceof DirectPosition)) {
            // Do NOT check the CRS if the given object is an ordinary Point2D.
            // This is necessary in order to respect the contract defined in Point2D.
            return true;
        }
        return Utilities.equals(getCoordinateReferenceSystem(),
                ((DirectPosition) object).getCoordinateReferenceSystem());
    }

    /**
     * Returns a clone of this point.
     */
    @Override
    public DirectPosition2D clone() {
        return (DirectPosition2D) super.clone();
    }

    /**
     * Write this object to the specified stream. This method is necessary
     * because the super-class is not serializable.
     */
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(x);
        out.writeDouble(y);
    }

    /**
     * Read this object from the specified stream. This method is necessary
     * because the super-class is not serializable.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        x = in.readDouble();
        y = in.readDouble();
    }
}
