/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.extent;

// OpenGIS direct dependencies
import org.opengis.spatialschema.geometry.Geometry;

// Geotools dependencies
import org.geotools.metadata.extent.GeographicExtent;
import org.geotools.resources.Utilities;


/**
 * Boundary enclosing the dataset, expressed as the closed set of
 * (<var>x</var>,<var>y</var>) coordinates of the polygon. The last
 * point replicates first point.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class BoundingPolygon extends GeographicExtent
       implements org.opengis.metadata.extent.BoundingPolygon
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8174011874910887918L;

    /**
     * The sets of points defining the bounding polygon.
     */
    private Geometry polygon;

    /**
     * Construct an initially empty bounding polygon.
     */
    public BoundingPolygon() {
    }

    /**
     * Creates a bounding polygon initialized to the specified value.
     */
    public BoundingPolygon(final Geometry polygon) {
        this.polygon = polygon;
    }

    /**
     * Returns the sets of points defining the bounding polygon.
     */
    public Geometry getPolygon() {
        return polygon;
    }

    /**
     * Returns the sets of points defining the bounding polygon.
     */
    public synchronized void setPolygon(final Geometry newValue) {
        checkWritePermission();
        polygon = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        polygon = (Geometry) unmodifiable(polygon);
    }

    /**
     * Compare this bounding polygon with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final BoundingPolygon that = (BoundingPolygon) object;
            return Utilities.equals(this.polygon, that.polygon)  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this bounding polygon.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (polygon != null) code ^= polygon.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this bounding polygon.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(polygon);
    }    
}
