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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.spatial;

// OpenGIS dependencies
import org.opengis.metadata.spatial.GeometricObjects;
import org.opengis.metadata.spatial.GeometricObjectType;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Number of objects, listed by geometric object type, used in the dataset.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class GeometricObjectsImpl extends MetadataEntity implements GeometricObjects {
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
     * Constructs an initially empty geometric objects.
     */
    public GeometricObjectsImpl() {
    }

    /**
     * Creates a geometric object initialized to the given type.
     */
    public GeometricObjectsImpl(final GeometricObjectType geometricObjectType) {
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
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this geometric objects with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeometricObjectsImpl that = (GeometricObjectsImpl) object; 
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
