/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
import java.util.NoSuchElementException;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Geotools dependencies
import org.geotools.referencing.FactoryFinder;


/**
 * Root class of the Geotools default implementation of geometric object. <code>Geometry</code>
 * instances are sets of direct positions in a particular coordinate reference system.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Geometry implements org.opengis.spatialschema.geometry.Geometry, Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -601532429079649232L;

    /**
     * The default {@link CoordinateOperationFactory} to uses for {@link #transform}.
     * Will be constructed only when first requested.
     */
    private static CoordinateOperationFactory coordinateOperationFactory;

    /**
     * The coordinate reference system used in {@linkplain GeneralDirectPosition direct position}
     * coordinates.
     */
    protected final CoordinateReferenceSystem crs;

    /**
     * Construct a geometry with the specified coordinate reference system.
     *
     * @param crs The coordinate reference system used in
     *            {@linkplain GeneralDirectPosition direct position} coordinates.
     */
    public Geometry(final CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * Returns the coordinate reference system used in {@linkplain GeneralDirectPosition
     * direct position} coordinates.
     *
     * @return The coordinate reference system used in {@linkplain GeneralDirectPosition
     *         direct position} coordinates.
     *
     * @see #getCoordinateDimension
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    /**
     * Returns the dimension of the coordinates that define this <code>Geometry</code>, which must
     * be the same as the coordinate dimension of the coordinate reference system for this
     * <code>Geometry</code>.
     *
     * @return The coordinate dimension.
     *
     * @see #getDimension
     * @see #getCoordinateReferenceSystem
     */
    public int getCoordinateDimension() {
        return crs.getCoordinateSystem().getDimension();
    }

    /**
     * Returns a new <code>Geometry</code> that is the coordinate transformation of this
     * <code>Geometry</code> into the passed coordinate reference system within the accuracy
     * of the transformation.
     *
     * @param  newCRS The new coordinate reference system.
     * @return The transformed <code>Geometry</code>.
     * @throws TransformException if the transformation failed.
     */
    public org.opengis.spatialschema.geometry.Geometry
            transform(CoordinateReferenceSystem newCRS) throws TransformException
    {
        if (coordinateOperationFactory == null) {
            // No need to synchronize: this is not a problem if this method is invoked
            // twice in two different threads.
            try {
                coordinateOperationFactory = FactoryFinder.getCoordinateOperationFactory();
            } catch (NoSuchElementException exception) {
                // TODO: localize the message
                throw new TransformException("Can't transform the geometry", exception);
            }
        }
        try {
            return transform(newCRS,
                   coordinateOperationFactory.createOperation(crs, newCRS).getMathTransform());
        } catch (FactoryException exception) {
            // TODO: localize the message
            throw new TransformException("Can't transform the geometry", exception);
        }
    }

    /**
     * Returns a clone of this geometry.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
