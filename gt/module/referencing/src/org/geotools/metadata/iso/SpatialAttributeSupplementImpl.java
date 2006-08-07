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

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.FeatureTypeList;
import org.opengis.metadata.SpatialAttributeSupplement;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Spatial attributes in the application schema for the feature types.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class SpatialAttributeSupplementImpl extends MetadataEntity
        implements SpatialAttributeSupplement
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 273337004694210422L;

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    private Collection featureTypeList;

    /** 
     * Construct an initially empty spatial attribute supplement.
     */
    public SpatialAttributeSupplementImpl() {
    }
    
    /** 
     * Creates a spatial attribute supplement initialized to the given values.
     */
    public SpatialAttributeSupplementImpl(final Collection featureTypeList) {
        setFeatureTypeList(featureTypeList);
    }

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    public synchronized Collection getFeatureTypeList() {
        return featureTypeList = nonNullCollection(featureTypeList, FeatureTypeList.class);
    }

    /**
     * Set information about the list of feature types with the same spatial representation.
     */
    public synchronized void setFeatureTypeList(final Collection newValues) {
        featureTypeList = copyCollection(newValues, featureTypeList, FeatureTypeList.class);
    }

   /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        featureTypeList = (Collection) unmodifiable(featureTypeList);
    }

    /**
     * Compare this spatial attribute supplement with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final SpatialAttributeSupplementImpl that = (SpatialAttributeSupplementImpl) object;
            return Utilities.equals(this.featureTypeList, that.featureTypeList ) ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this object.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (featureTypeList != null) code ^= featureTypeList.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(featureTypeList);
    }        
}
