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

// OpenGIS direct dependencies
import org.opengis.metadata.citation.Citation;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;

/**
 * Information identifying the portrayal catalogue used.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class PortrayalCatalogueReference extends MetadataEntity
        implements org.opengis.metadata.PortrayalCatalogueReference
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3095277682987563157L;

    /**
     * Bibliographic reference to the portrayal catalogue cited.
     */
    private Set portrayalCatalogueCitations;

    /**
     * Construct an initially empty portrayal catalogue reference.
     */
    public PortrayalCatalogueReference() {
    }
    
    /**
     * Creates a portrayal catalogue reference initialized to the given values.
     */
    public PortrayalCatalogueReference(final Set portrayalCatalogueCitations) {
        setPortrayalCatalogueCitations(portrayalCatalogueCitations);
    }
    
    /**
     * Bibliographic reference to the portrayal catalogue cited.
     */
    public Set getPortrayalCatalogueCitations() {
        final Set portrayalCatalogueCitations = this.portrayalCatalogueCitations; // Avoid synchronization
        return (portrayalCatalogueCitations!=null) ? portrayalCatalogueCitations : Collections.EMPTY_SET;
    }

    /**
     * Set bibliographic reference to the portrayal catalogue cited.
     */
    public synchronized void setPortrayalCatalogueCitations(Set newValues) {
        checkWritePermission();
        if (portrayalCatalogueCitations == null) {
            portrayalCatalogueCitations = new CheckedHashSet(Citation.class);
        } else {
            portrayalCatalogueCitations.clear();
        }
        portrayalCatalogueCitations.addAll(newValues);
    }

   /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        portrayalCatalogueCitations    = (Set) unmodifiable(portrayalCatalogueCitations);
    }

    /**
     * Compare this portrayal catalogue reference with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final PortrayalCatalogueReference that = (PortrayalCatalogueReference) object;
            return Utilities.equals(this.portrayalCatalogueCitations, that.portrayalCatalogueCitations ) ;
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
        if (portrayalCatalogueCitations != null) code ^= portrayalCatalogueCitations.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(portrayalCatalogueCitations);
    }        
}
