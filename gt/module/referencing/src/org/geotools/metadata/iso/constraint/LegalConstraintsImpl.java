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
package org.geotools.metadata.iso.constraint;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.constraint.LegalConstraints;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Restrictions and legal prerequisites for accessing and using the resource.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class LegalConstraintsImpl extends ConstraintsImpl implements LegalConstraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6197608101092130586L;
    
    /**
     * Access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    private Collection accessConstraints;

    /**
     * Constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    private Collection useConstraints;

    /**
     * Other restrictions and legal prerequisites for accessing and using the resource.
     * This method should returns a non-null value only if {@linkplain #getAccessConstraints
     * access constraints} or {@linkplain #getUseConstraints use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    private InternationalString otherConstraints;

    /**
     * Construct an initially empty constraints.
     */
    public LegalConstraintsImpl() {
    }

    /**
     * Returns the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    public synchronized Collection getAccessConstraints() {
        return accessConstraints = nonNullCollection(accessConstraints, Restriction.class);
    }

    /**
     * Set the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    public synchronized void setAccessConstraints(final Collection newValues) {
        accessConstraints = copyCollection(newValues, accessConstraints, Restriction.class);
    }

    /**
     * Returns the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    public synchronized Collection getUseConstraints() {
        return useConstraints = nonNullCollection(useConstraints, Restriction.class);
    }

    /**
     * Set the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    public synchronized void setUseConstraints(final Collection newValues) {
        useConstraints = copyCollection(newValues, useConstraints, Restriction.class);
    }

    /**
     * Returns the other restrictions and legal prerequisites for accessing and using the resource.
     * This method should returns a non-null value only if {@linkplain #getAccessConstraints
     * access constraints} or {@linkplain #getUseConstraints use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    public InternationalString getOtherConstraints() {
        return otherConstraints;
    }

    /**
     * Set the other restrictions and legal prerequisites for accessing and using the resource.
     * This method should returns a non-null value only if {@linkplain #getAccessConstraints
     * access constraints} or {@linkplain #getUseConstraints use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    public synchronized void setOtherConstraints(final InternationalString newValue) {
        checkWritePermission();
        otherConstraints = newValue;
    }   

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        accessConstraints = (Collection)          unmodifiable(accessConstraints);
        useConstraints    = (Collection)          unmodifiable(useConstraints);
        otherConstraints  = (InternationalString) unmodifiable(otherConstraints);
    } 

    /**
     * Compare this constraints with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final LegalConstraintsImpl that = (LegalConstraintsImpl) object;
            return Utilities.equals(this.accessConstraints, that.accessConstraints) &&
                   Utilities.equals(this.useConstraints,    that.useConstraints   ) &&
                   Utilities.equals(this.otherConstraints,  that.otherConstraints );
        }
        return false;
    }

    /**
     * Returns a hash code value for this constraints.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (accessConstraints != null) code ^= accessConstraints.hashCode();
        if (useConstraints    != null) code ^= useConstraints   .hashCode();
        if (otherConstraints  != null) code ^= otherConstraints .hashCode();
        return code;
    }
}
