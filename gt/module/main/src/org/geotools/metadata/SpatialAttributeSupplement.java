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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata;

// J2SE direct dependencies
import java.util.Set;
import java.util.Collections;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;

/**
 * Spatial attributes in the application schema for the feature types.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class SpatialAttributeSupplement extends MetadataEntity
        implements org.opengis.metadata.SpatialAttributeSupplement
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 273337004694210422L;

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    private Set featureTypeList;

    /** 
     * Construct an initially empty spatial attribute supplement.
     */
    public SpatialAttributeSupplement() {
    }
    
    /** 
     * Creates a spatial attribute supplement initialized to the given values.
     */
    public SpatialAttributeSupplement(final Set featureTypeList) {
        setFeatureTypeList(featureTypeList);
    }

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    public Set getFeatureTypeList() {
        final Set featureTypeList = this.featureTypeList; // Avoid synchronization
        return (featureTypeList!=null) ? featureTypeList : Collections.EMPTY_SET;
    }

    /**
     * Set information about the list of feature types with the same spatial representation.
     */
    public synchronized void setFeatureTypeList(final Set newValues) {
        checkWritePermission();
        if (featureTypeList == null) {
            featureTypeList = new CheckedHashSet(FeatureTypeList.class);
        } else {
            featureTypeList.clear();
        }
        featureTypeList.addAll(newValues);
    }

   /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        featureTypeList = (Set) unmodifiable(featureTypeList);
    }

    /**
     * Compare this spatial attribute supplement with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final SpatialAttributeSupplement that = (SpatialAttributeSupplement) object;
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
