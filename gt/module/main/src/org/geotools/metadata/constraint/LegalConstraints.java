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
package org.geotools.metadata.constraint;

// J2SE direct dependencies
import java.util.Set;
import java.util.Collections;

// OpenGIS direct dependencies
import org.opengis.metadata.constraint.Restriction;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;


/**
 * Restrictions and legal prerequisites for accessing and using the resource.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraivane
 */
public class LegalConstraints extends Constraints implements org.opengis.metadata.constraint.LegalConstraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6197608101092130586L;
    
    /**
     * Access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    private Set accessConstraints;

    /**
     * Constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    private Set useConstraints;

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
    public LegalConstraints() {
    }

    /**
     * Returns the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    public Set getAccessConstraints() {
        final Set accessConstraints = this.accessConstraints; // Avoid synchronization
        return (accessConstraints!=null) ? accessConstraints : Collections.EMPTY_SET;
    }

    /**
     * Set the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    public synchronized void setAccessConstraints(final Set accessConstraints) {
        checkWritePermission();
        if (this.accessConstraints == null) {
            this.accessConstraints = new CheckedHashSet(Restriction.class);
        } else {
            this.accessConstraints.clear();
        }
        this.accessConstraints.addAll(accessConstraints);
    }

    /**
     * Returns the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    public Set getUseConstraints() {
        final Set useConstraints = this.useConstraints; // Avoid synchronization
        return (useConstraints!=null) ? useConstraints : Collections.EMPTY_SET;
    }

    /**
     * Set the constraints applied to assure the protection of privacy or intellectual property, and any
     * special restrictions or limitations or warnings on using the resource.
     */
    public synchronized void setUseConstraints(Set useConstraints) {
        checkWritePermission();
        if (this.useConstraints == null) {
            this.useConstraints = new CheckedHashSet(Restriction.class);
        } else {
            this.useConstraints.clear();
        }
        this.useConstraints.addAll(useConstraints);
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
    public synchronized void setOtherConstraints(InternationalString otherConstraints) {
        checkWritePermission();
        this.otherConstraints = otherConstraints;
    }   

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        accessConstraints = (Set)                 unmodifiable(accessConstraints);
        useConstraints    = (Set)                 unmodifiable(useConstraints);
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
            final LegalConstraints that = (LegalConstraints) object;
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
