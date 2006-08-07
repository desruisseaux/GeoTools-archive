/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.metadata.iso.citation;

// OpenGIS dependencies
import org.opengis.metadata.citation.Telephone;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Telephone numbers for contacting the responsible individual or organization.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 *
 * @since 2.1
 * @source $URL$
 */
public class TelephoneImpl extends MetadataEntity implements Telephone {
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
    public TelephoneImpl() {
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
    public synchronized void setVoice(final String newValue) {
        checkWritePermission();
        voice = newValue;
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
    public synchronized void setFacsimile(final String newValue) {
        checkWritePermission();
        facsimile = newValue;
    }

    /**
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this telephone with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final TelephoneImpl that = (TelephoneImpl) object;
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
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (voice != null) {
            buffer.append("Tel: ");
            buffer.append(voice);
        }
        if (facsimile != null) {
            appendLineSeparator(buffer);
            buffer.append("Fax: ");
            buffer.append(facsimile);
        }
        return buffer.toString();
    }
}
