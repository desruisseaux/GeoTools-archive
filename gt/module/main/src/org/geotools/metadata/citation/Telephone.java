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
import org.geotools.util.CheckedHashSet;
import org.geotools.util.CheckedHashMap;
import org.geotools.util.CheckedArrayList;
import org.geotools.resources.Utilities;


/**
 * Telephone numbers for contacting the responsible individual or organization.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 */
public class Telephone extends MetadataEntity implements org.opengis.metadata.citation.Telephone {
    /**
     * Telephone number by which individuals can speak to the responsible organization or individual.
     */
    private String voice;

    /**
     * Telephone number of a facsimile machine for the responsible organization or individual.
     */
    private String facsimile;

    /**
     * Telephone number by which individuals can speak to the responsible organization or individual.
     */
    public String getVoice() {
        return voice;
    }
    public String getFacsimile() {
            return facsimile;
    }
    public void setFacsimile(String facsimile) {
            this.facsimile = facsimile;
    }
    public void setVoice(String voice) {
            this.voice = voice;
    }
}
