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
package org.geotools.metadata.spatial;

// OpenGIS direct dependencies
import org.opengis.metadata.spatial.GeometricObjectType;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * number of objects, listed by geometric object type, used in the dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 */
public class GeometricObjects extends MetadataEntity
        implements org.opengis.metadata.spatial.GeometricObjects
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8755950031078638313L;

    /**
     * Total number of the point or vector object type occurring in the dataset.
     */
    private GeometricObjectType geometricObjectType;

    /**
     * Total number of the point or vector object type occurring in the dataset.
     */
    private int geometricObjectCount;

    /**
     * Construct an initially empty geometric objects.
     */
    public GeometricObjects() {
    }

    /**
     * Creates a geometric object initialized to the given type.
     */
    public GeometricObjects(final GeometricObjectType geometricObjectType) {
        setGeometricObjectType(geometricObjectType);
    }
    
    /**
     * Total number of the point or vector object type occurring in the dataset.
     */
    public GeometricObjectType getGeometricObjectType() {
        return geometricObjectType;
    }
    
    /**
     * Set the total number of the point or vector object type occurring in the dataset.
     */
    public synchronized void setGeometricObjectType(final GeometricObjectType newValue) {
        checkWritePermission();
        geometricObjectType = newValue;
    }

    /**
     * Total number of the point or vector object type occurring in the dataset.
     */
    public int getGeometricObjectCount() {
        return geometricObjectCount;
    }
    
    /**
     * Set the total number of the point or vector object type occurring in the dataset.
     */
    public synchronized void setGeometricObjectCount(final int newValue) {
        checkWritePermission();
        geometricObjectCount = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        geometricObjectType = (GeometricObjectType) unmodifiable(geometricObjectType);
    }

    /**
     * Compare this geometric objects with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeometricObjects that = (GeometricObjects) object; 
            return  Utilities.equals(this.geometricObjectType,    that.geometricObjectType ) &&
                                    (this.geometricObjectCount == that.geometricObjectCount);
        }
        return false;
    }

    /**
     * Returns a hash code value for this object. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (geometricObjectType != null)        code ^= geometricObjectType.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(geometricObjectType);
    }            
}
