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

import java.util.Collection;

//OpenGIS dependencies
import org.opengis.metadata.citation.Telephone;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


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
    private Collection voice;

    /**
     * Telephone number of a facsimile machine for the responsible organization or individual.
     */
    private Collection facsimile;

    /**
     * Constructs a default telephone.
     */
    public TelephoneImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public TelephoneImpl(final Telephone source) {
        super(source);
    }

    /**
     * Returns the telephone number by which individuals can speak to the responsible
     * organization or individual.
     */
    public Collection getVoice() {
        return voice;
    }

    /**
     * Set the telephone number by which individuals can speak to the responsible
     * organization or individual.
     */
    public synchronized void setVoice(final Collection newValue) {
        checkWritePermission();
        voice = newValue;
    }

    /**
     * Returns the telephone number of a facsimile machine for the responsible organization
     * or individual.
     */
    public Collection getFacsimile() {
        return facsimile;
    }

    /**
     * Set the telephone number of a facsimile machine for the responsible organization
     * or individual.
     */
    public synchronized void setFacsimile(final Collection newValue) {
        checkWritePermission();
        facsimile = newValue;
    }
}
