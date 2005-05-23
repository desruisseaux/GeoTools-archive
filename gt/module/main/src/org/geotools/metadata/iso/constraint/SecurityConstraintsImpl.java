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
package org.geotools.metadata.iso.constraint;

// OpenGIS direct dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.SecurityConstraints;
import org.opengis.util.InternationalString;


/**
 * Handling restrictions imposed on the resource for national security or similar security concerns.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class SecurityConstraintsImpl extends ConstraintsImpl implements SecurityConstraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6412833018607679734L;;
    
    /**
     * Name of the handling restrictions on the resource.
     */
    private Classification classification;

    /**
     * Explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    private InternationalString userNote;

    /**
     * Name of the classification system.
     */
    private InternationalString classificationSystem;

    /**
     * Additional information about the restrictions on handling the resource.
     */
    private InternationalString handlingDescription;

    /**
     * Creates an initially empty security constraints.
     */
    public SecurityConstraintsImpl() {
    }

    /**
     * Creates a security constraints initialized with the specified classification.
     */
    public SecurityConstraintsImpl(final Classification classification) {
        setClassification(classification);
    }
    
    /**
     * Returns the name of the handling restrictions on the resource.
     */
    public Classification getClassification() {
        return classification;
    }
    
    /**
     * Set the name of the handling restrictions on the resource.
     */
    public synchronized void setClassification(final Classification newValue) {
        checkWritePermission();
        classification = newValue;
    }

    /**
     * Returns the explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    public InternationalString getUserNote() {
        return userNote;
    }

    /**
     * Set the explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    public synchronized void setUserNote(final InternationalString newValue) {
        checkWritePermission();
        userNote = newValue;
    }

    /**
     * Returns the name of the classification system.
     */
    public InternationalString getClassificationSystem() {
        return classificationSystem;
    }

    /**
     * Set the name of the classification system.
     */
     public synchronized void setClassificatonSystem(final InternationalString newValue) {
         checkWritePermission();
         classificationSystem = newValue;
     }

    /**
     * Returns the additional information about the restrictions on handling the resource.
     */
    public InternationalString getHandlingDescription() {
        return handlingDescription;
    }

    /**
     * Set the additional information about the restrictions on handling the resource.
     */
    public synchronized void setHandlingDescription(final InternationalString newValue) {
        checkWritePermission();
        handlingDescription = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        userNote             = (InternationalString) unmodifiable(userNote);
        classificationSystem = (InternationalString) unmodifiable(classificationSystem);
        handlingDescription  = (InternationalString) unmodifiable(handlingDescription);
    }

    /**
     * Compare this security constraints with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final SecurityConstraintsImpl that = (SecurityConstraintsImpl) object;
            return Utilities.equals(this.classification,       that.classification      ) &&
                   Utilities.equals(this.userNote,             that.userNote            ) &&
                   Utilities.equals(this.classificationSystem, that.classificationSystem) &&
                   Utilities.equals(this.handlingDescription,  that.handlingDescription );
        }
        return false;
    }

    /**
     * Returns a hash code value for this constraints.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (classification       != null)  code ^= classification      .hashCode();
        if (userNote             != null)  code ^= userNote            .hashCode();
        if (classificationSystem != null)  code ^= classificationSystem.hashCode();
        if (handlingDescription  != null)  code ^= handlingDescription .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this constraints.
     */
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (classification != null) {
            buffer.append(classification.name().replace('_', ' '));
        }
        final String useLimitation = super.toString();
        if (useLimitation!=null && useLimitation.length()!=0) {
            appendLineSeparator(buffer);
            buffer.append(useLimitation);
        }
        return buffer.toString();
    }
}
