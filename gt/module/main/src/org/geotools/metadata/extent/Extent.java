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
import org.geotools.resources.Utilities;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.TemporalExtent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.util.InternationalString;


/**
 * Information about spatial, vertical, and temporal extent.
 * This interface has three optional attributes
 * ({@linkplain #getGeographicElement geographic element},
 *  {@linkplain #getTemporalElement temporal element}, and
 *  {@linkplain #getVerticalElement vertical element}) and an element called
 *  {@linkplain #getDescription description}.
 *  At least one of the four shall be used.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Extent extends MetadataEntity implements org.opengis.metadata.extent.Extent {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7812213837337326257L;

    /**
     * Returns the spatial and temporal extent for the referring object.
     */
    private InternationalString description;

    /**
     * Provides geographic component of the extent of the referring object
     */
    private GeographicExtent geographicElement;

    /**
     * Provides temporal component of the extent of the referring object
     */
    private TemporalExtent temporalElement;

    /**
     * Provides vertical component of the extent of the referring object
     */
    private VerticalExtent verticalElement;

    /**
     * Construct an initially empty extent.
     */
    public Extent() {
    }

    /**
     * Returns the spatial and temporal extent for the referring object.
     */
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Set the spatial and temporal extent for the referring object.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Provides geographic component of the extent of the referring object
     */
    public GeographicExtent getGeographicElement() {
        return geographicElement;
    }

    /**
     * Set geographic component of the extent of the referring object
     */
    public synchronized void setGeographicElement(final GeographicExtent newValue) {
        checkWritePermission();
        geographicElement = newValue;
    }

    /**
     * Provides temporal component of the extent of the referring object
     */
    public TemporalExtent getTemporalElement() {
        return temporalElement;
    }

    /**
     * Set temporal component of the extent of the referring object
     */
    public synchronized void setTemporalElement(final TemporalExtent newValue) {
        checkWritePermission();
        temporalElement = newValue;
    }

    /**
     * Provides vertical component of the extent of the referring object
     */
    public VerticalExtent getVerticalElement() {
        return verticalElement;
    }

    /**
     * Set vertical component of the extent of the referring object
     */
    public synchronized void setVerticalElement(final VerticalExtent newValue) {
        checkWritePermission();
        verticalElement = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        description       = (InternationalString) unmodifiable(description);
        geographicElement = (GeographicExtent)    unmodifiable(geographicElement);
        temporalElement   = (TemporalExtent)      unmodifiable(temporalElement);
        verticalElement   = (VerticalExtent)      unmodifiable(verticalElement);
    }

    /**
     * Compare this extent with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Extent that = (Extent) object;
            return Utilities.equals(this.description,       that.description       ) &&
                   Utilities.equals(this.geographicElement, that.geographicElement ) &&
                   Utilities.equals(this.temporalElement,   that.temporalElement   ) &&
                   Utilities.equals(this.verticalElement,   that.verticalElement   )  ;
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (description        != null) code ^= description      .hashCode();
        if (geographicElement  != null) code ^= geographicElement.hashCode();
        if (temporalElement    != null) code ^= temporalElement  .hashCode();
        if (verticalElement    != null) code ^= verticalElement  .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this extent.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(description);
    }
}
