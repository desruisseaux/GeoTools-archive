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
package org.geotools.metadata.content;

// OpenGIS dependencies
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class RangeDimension extends MetadataEntity
       implements org.opengis.metadata.content.ContentInformation
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4365956866782010460L;

    /**
     * Description of the range of a cell measurement value.
     */
    private InternationalString descriptor;

    /**
     * Construct an initially empty range dimension.
     */
    public RangeDimension() {
    }

    /**
     * Return the description of the range of a cell measurement value.
     */
    public InternationalString getDescriptor() {
        return descriptor;
    }
    
    /**
     * Set the description of the range of a cell measurement value.
     */
    public synchronized void setDescriptor(final InternationalString descriptor) {
        checkWritePermission();
        this.descriptor = descriptor;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        descriptor = (InternationalString) unmodifiable(descriptor);
    }

    /**
     * Compare this range dimension with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final RangeDimension that = (RangeDimension) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }

    /**
     * Returns a hash code value for this range dimension. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (descriptor != null) code ^= descriptor.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this range dimension
     */
    public String toString() {
        return String.valueOf(descriptor);
    }             
}
