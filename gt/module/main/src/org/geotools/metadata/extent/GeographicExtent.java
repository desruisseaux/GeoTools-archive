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
import org.geotools.metadata.MetadataEntity;


/**
 * Base class for geographic area of the dataset.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class GeographicExtent extends MetadataEntity
       implements org.opengis.metadata.extent.GeographicExtent
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1871146315280869971L;

    /**
     * Indication of whether the bounding polygon encompasses an area covered by the data
     * (<cite>inclusion</cite>) or an area where data is not present (<cite>exclusion</cite>).
     */
    private boolean inclusion;

   /**
     * Construct an initially empty GeographicExtent.
     */
    public GeographicExtent() {
    }

   /**
     * Indication of whether the bounding polygon encompasses an area covered by the data
     * (<cite>inclusion</cite>) or an area where data is not present (<cite>exclusion</cite>).
     *
     * @return <code>true</code> for inclusion, or <code>false</code> for exclusion.
     */    
    public boolean getInclusion() {
        return inclusion;
    }

    /**
     * Set whether the bounding polygon encompasses an area covered by the data
     * (<cite>inclusion</cite>) or an area where data is not present (<cite>exclusion</cite>).
     */
    public synchronized void setInclusion(final boolean newValue) {
        checkWritePermission();
        inclusion = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compare this GeographicExtent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeographicExtent that = (GeographicExtent) object;
            return (this.inclusion == that.inclusion);
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (inclusion) code = ~code;
        return code;
    }

    /**
     * Returns a string representation of this extent.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(inclusion);
    }
}
