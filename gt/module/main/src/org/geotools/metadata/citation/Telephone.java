/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.metadata.citation;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Telephone numbers for contacting the responsible individual or organization.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class Telephone extends MetadataEntity implements org.opengis.metadata.citation.Telephone {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8237503664554861494L;
    
    /**
     * Telephone number by which individuals can speak to the responsible organization or individual.
     */
    private String voice;

    /**
     * Telephone number of a facsimile machine for the responsible organization or individual.
     */
    private String facsimile;

    /**
     * Constructs a default telephone.
     */
    public Telephone() {
    }

    /**
     * Returns the telephone number by which individuals can speak to the responsible
     * organization or individual.
     */
    public String getVoice() {
        return voice;
    }

    /**
     * Set the telephone number by which individuals can speak to the responsible
     * organization or individual.
     */
    public synchronized void setVoice(final String voice) {
        checkWritePermission();
        this.voice = voice;
    }

    /**
     * Returns the telephone number of a facsimile machine for the responsible organization
     * or individual.
     */
    public String getFacsimile() {
        return facsimile;
    }

    /**
     * Set the telephone number of a facsimile machine for the responsible organization
     * or individual.
     */
    public synchronized void setFacsimile(final String facsimile) {
        checkWritePermission();
        this.facsimile = facsimile;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        voice     = (String) unmodifiable(voice);
        facsimile = (String) unmodifiable(facsimile);
    }

    /**
     * Compare this telephone with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Telephone that = (Telephone) object;
            return Utilities.equals(this.voice,     that.voice    ) &&
                   Utilities.equals(this.facsimile, that.facsimile);
        }
        return false;
    }

    /**
     * Returns a hash code value for this telephone.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (voice     != null) code ^= voice    .hashCode();
        if (facsimile != null) code ^= facsimile.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this telephone.
     */
    public String toString() {
        return String.valueOf(voice);
    }
}
