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
package org.geotools.metadata.iso;

// OpenGIS dependencies
import org.opengis.metadata.FeatureTypeList;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * List of names of feature types with the same spatial representation (same as spatial attributes).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class FeatureTypeListImpl extends MetadataEntity implements FeatureTypeList {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5417914796207743856L;
    
    /**
     * Instance of a type defined in the spatial schema.
     */
    private String spatialObject;

    /**
     * Name of the spatial schema used.
     */
    private String spatialSchemaName;

    /**
     * Construct an initially empty feature type list.
     */
    public FeatureTypeListImpl() {
    }

    /**
     * Creates a feature type list initialized to the given values.
     */
    public FeatureTypeListImpl(final String spatialObject,
                           final String spatialSchemaName)
    {
        setSpatialObject    (spatialObject    );
        setSpatialSchemaName(spatialSchemaName);
    }
    
    /**
     * Instance of a type defined in the spatial schema.
     */
    public String getSpatialObject() {
        return spatialObject;
    }

    /**
     * Set the instance of a type defined in the spatial schema.
     */
    public synchronized void setSpatialObject(final String newValue) {
        checkWritePermission();
        spatialObject = newValue;
    }

    /**
     * Name of the spatial schema used.
     */
    public String getSpatialSchemaName() {
        return spatialSchemaName;
    }

    /**
     * Set the name of the spatial schema used.
     */
    public synchronized void setSpatialSchemaName(final String newValue) {
        checkWritePermission();
        spatialSchemaName = newValue;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this FeatureTypeList with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final FeatureTypeListImpl that = (FeatureTypeListImpl) object;
            return Utilities.equals(this.spatialSchemaName, that.spatialSchemaName) &&
                   Utilities.equals(this.spatialObject,     that.spatialObject    );
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (spatialObject      != null) code ^= spatialObject    .hashCode();
        if (spatialSchemaName  != null) code ^= spatialSchemaName.hashCode();
        return code;
    }
}
