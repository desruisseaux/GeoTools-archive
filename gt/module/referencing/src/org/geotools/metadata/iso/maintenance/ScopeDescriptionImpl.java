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
package org.geotools.metadata.iso.maintenance;

// J2SE dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS dependencies
import org.opengis.metadata.maintenance.ScopeDescription;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Description of the class of information covered by the information.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class ScopeDescriptionImpl extends MetadataEntity implements ScopeDescription {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5671299759930976286L;

    /**
     * Creates an initially empty scope description.
     */
    public ScopeDescriptionImpl() {
    }

    /**
     * Returns the attributes to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getAttributes() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the features to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getFeatures() {
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the feature instances to which the information applies.
     *
     * @todo Not yet implemented.
     */
    public Set getFeatureInstances() {
        return Collections.EMPTY_SET;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compare this scope description with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final ScopeDescriptionImpl that = (ScopeDescriptionImpl) object;
            // TODO once method in ScopeDescription will be defined.
            return true;
        }
        return false;
    }

    /**
     * Returns a hash code value for this maintenance information.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        // TODO once method in ScopeDescription will be defined.
        return code;
    }

    /**
     * Returns a string representation of this maintenance information.
     *
     * @todo Provides a more elaborated implementation.
     */
    public synchronized String toString() {
        // TODO once method in ScopeDescription will be defined.
        return "";
    }
}
