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
package org.geotools.metadata.extent;

// OpenGIS direct dependencies
import org.geotools.resources.Utilities;
import org.opengis.metadata.Identifier;


/**
 * Description of the geographic area using identifiers.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class GeographicDescription extends GeographicExtent
       implements org.opengis.metadata.extent.GeographicDescription
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
     * Construct an initially empty geographic description.
     */
    public GeographicDescription() {
    }

    /**
     * Creates a geographic description initialized to the specified value.
     */
     public GeographicDescription(final Identifier geographicIdentifier) {
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
            final GeographicDescription that = (GeographicDescription) object;
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
