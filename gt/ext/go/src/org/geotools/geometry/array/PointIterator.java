/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
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
import java.awt.geom.Point2D;
import java.util.Iterator;

// OpenGIS dependencies
import org.opengis.util.Cloneable;


/**
 * An iterator over the points in an {@link PointArray2D}. This iterator is obtained by calls to
 * {@link PointArray2D#iterator}. Methods {@link #nextX} and {@link #nextY} provides a fast way to
 * get the floating point ordinate values. However, the above-cited methods <strong>must</strong>
 * be invoked in this order: {@link #nextX} first, and {@link #nextY} after. The iterator
 * behavior is undertermined if those methods are not invoked in this order.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Once J2SE 1.5 will be available, this class should implements {@code Iterator<Point2D>}
 *       and method {@code next()} should returns {@link Point2D}. Method {@link #clone()} should
 *       returns {@code PointIterator}.
 */
public abstract class PointIterator implements Iterator, Cloneable {
    /**
     * Default constructor.
     */
    protected PointIterator() {
    }

    /**
     * Returns if {@link #next} or {@link #nextX} has more values.
     */
    public abstract boolean hasNext();

    /**
     * Returns the next <var>x</var> value. Before to invoke this method one more time,
     * the method {@link #nextY} <strong>must</strong> be invoked.
     */
    public abstract float nextX();

    /**
     * Returns the next <var>y</var> value, and move this iterator to the next coordinate.
     * Before to invoke this method one more time, the method {@link #nextX} <strong>must</strong>
     * be invoked.
     */
    public abstract float nextY();

    /**
     * Returns the current coordinates as a {@link Point2D} object, and move this iterator to
     * the next coordinate. This method invokes {@link #nextX} followed by {@link #nextY}.
     */
    public Object next() {
        return new Point2D.Float(nextX(), nextY());
    }

    /**
     * Unsupported operation.
     */
    public final void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a copy of this iterator. This copy may be useful in order to iterate one more
     * time over the same data starting at the position of this iterator at the time it has
     * been cloned.
     */
    public final Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            // Should not happen, since we are cloneable
            throw new AssertionError(exception);
        }
    }
}
