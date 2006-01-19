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
package org.geotools.metadata.iso.extent;

// OpenGIS direct dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.extent.GeographicDescription;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Description of the geographic area using identifiers.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
public class GeographicDescriptionImpl extends GeographicExtentImpl
        implements GeographicDescription
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7250161161099782176L;

    /**
     * The identifier used to represent a geographic area.
     */
    private Identifier geographicIdentifier;
    
    /**
     * Constructs an initially empty geographic description.
     */
    public GeographicDescriptionImpl() {
    }

    /**
     * Creates a geographic description initialized to the specified value.
     */
     public GeographicDescriptionImpl(final Identifier geographicIdentifier) {
         setGeographicIdentifier(geographicIdentifier);
     }
     
    /**
     * Returns the identifier used to represent a geographic area.
     */
    public Identifier getGeographicIdentifier() {
        return geographicIdentifier;
    }
    
    /**
     * Set the identifier used to represent a geographic area.
     */
    public synchronized void setGeographicIdentifier(final Identifier newValue) {
        checkWritePermission();
        geographicIdentifier = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        geographicIdentifier = (Identifier) unmodifiable(geographicIdentifier);
    }

    /**
     * Compare this GeographicDescription with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeographicDescriptionImpl that = (GeographicDescriptionImpl) object;
            return Utilities.equals(this.geographicIdentifier, that.geographicIdentifier);
        }
        return false;
    }

    /**
     * Returns a hash code value for this geographic description.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (geographicIdentifier != null) code ^= geographicIdentifier.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this geographic description.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(geographicIdentifier);
    }    
}
