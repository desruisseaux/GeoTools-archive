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
package org.geotools.metadata.maintenance;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;


/**
 * Description of the class of information covered by the information.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class ScopeDescription extends MetadataEntity implements org.opengis.metadata.maintenance.ScopeDescription {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5671299759930976286L;

    /**
     * Creates an initially empty scope description.
     */
    public ScopeDescription() {
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
            final ScopeDescription that = (ScopeDescription) object;
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
     */
    public synchronized String toString() {
        // TODO once method in ScopeDescription will be defined.
        return "";
    }
}
