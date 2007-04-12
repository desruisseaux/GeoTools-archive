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
package org.geotools.metadata.iso.quality;

// OpenGIS dependencies
import java.util.Collection;

import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.quality.Scope;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.maintenance.ScopeDescription;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Description of the data specified by the scope.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ScopeImpl extends MetadataEntity implements Scope {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8021256328527422972L;
    
    /**
     * Hierarchical level of the data specified by the scope.
     */
    private ScopeCode level;

    /**
     * Information about the spatial, vertical and temporal extent of the data specified by the
     * scope.
     */
    private Extent extent;

    /**
     * Detailed description about the level of the data specified by the scope.
     */
    private Collection/*<ScopeDescription>*/ levelDescription;

    /**
     * Constructs an initially empty scope.
     */
    public ScopeImpl() {
    }

    /**
     * Creates a scope initialized to the given level.
     */
    public ScopeImpl(final ScopeCode level) {
        setLevel(level);
    }

    /**
     * Hierarchical level of the data specified by the scope.
     */
    public ScopeCode getLevel() {
        return level;
    } 

    /**
     * Set the hierarchical level of the data specified by the scope.
     */
    public synchronized void setLevel(final ScopeCode newValue) {
        checkWritePermission();
        level = newValue;
    }

    /**
     * Returns detailed descriptions about the level of the data specified by the scope.
     * Should be defined only if the {@linkplain #getLevel level} is not equal
     * to {@link ScopeCode#DATASET DATASET} or {@link ScopeCode#SERIES SERIES}.
     *
     * @since 2.4
     */
    public synchronized Collection getLevelDescription() {
        return levelDescription = nonNullCollection(levelDescription, ScopeDescription.class);
    }

    /**
     * Set detailed descriptions about the level of the data specified by the scope.
     *
     * @since 2.4
     */
    public synchronized void setLevelDescription(final Collection newValues) {
        levelDescription = copyCollection(newValues, levelDescription, ScopeDescription.class);
    }        

    /**
     * Information about the spatial, vertical and temporal extent of the data specified by the
     * scope.
     */
    public Extent getExtent() {
        return extent;
    }

    /**
     * Set information about the spatial, vertical and temporal extent of the data specified
     * by the scope.
     */
    public synchronized void setExtent(final Extent newValue) {
        checkWritePermission();
        extent = newValue;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        extent = (Extent) unmodifiable(extent);
    }

    /**
     * Compares this Scope with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ScopeImpl that = (ScopeImpl) object; 
            return Utilities.equals(this.level,            that.level           ) &&
                   Utilities.equals(this.levelDescription, that.levelDescription) &&
                   Utilities.equals(this.extent,           that.extent          );
        }
        return false;
    }

    /**
     * Returns a hash code value for this scope.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (level  != null) code ^= level .hashCode();
        if (extent != null) code ^= extent.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this scope.
     */
    public String toString() {
        return String.valueOf(level);
    }
}
