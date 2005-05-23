/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.content;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;


/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 * @deprecated Renamed as {@code ContentInformationImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class ContentInformation extends MetadataEntity
       implements org.opengis.metadata.content.ContentInformation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1609535650982322560L;

    /**
     * Constructs an initially empty content information.
     */
    public ContentInformation() {
    }

    /**
     * Compare this content information with the specified object for equality.
     */
    public boolean equals(final Object object) {
        if (object!=null && object.getClass().equals(getClass())) {
            return true;
        }
        return false;
    }

    /**
     * Returns a hash code value for this content information.
     */
    public int hashCode() {
        int code = (int)serialVersionUID;
        return code;
    }
}
