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
import org.opengis.util.LocalName;

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
       implements org.opengis.metadata.content.RangeDimension
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4365956866782010460L;

    /**
     * Number that uniquely identifies instances of bands of wavelengths on which a sensor
     * operates.
     */
    private LocalName sequenceIdentifier;

    /**
     * Description of the range of a cell measurement value.
     */
    private InternationalString descriptor;

    /**
     * Constructs an initially empty range dimension.
     */
    public RangeDimension() {
    }

    /**
     * Returns the number that uniquely identifies instances of bands of wavelengths
     * on which a sensor operates.
     */
    public LocalName getSequenceIdentifier() {
        return sequenceIdentifier;
    }

    /**
     * Set the number that uniquely identifies instances of bands of wavelengths
     * on which a sensor operates.
     */
    public synchronized void setSequenceIdentifier(final LocalName newValue) {
        checkWritePermission();
        sequenceIdentifier = newValue;
    }

    /**
     * Returns the description of the range of a cell measurement value.
     */
    public InternationalString getDescriptor() {
        return descriptor;
    }
    
    /**
     * Set the description of the range of a cell measurement value.
     */
    public synchronized void setDescriptor(final InternationalString newValue) {
        checkWritePermission();
        descriptor = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        sequenceIdentifier = (LocalName)           unmodifiable(sequenceIdentifier);
        descriptor         = (InternationalString) unmodifiable(descriptor);
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
            return Utilities.equals(this.sequenceIdentifier, that.sequenceIdentifier) &&
                   Utilities.equals(this.descriptor,         that.descriptor);
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
        if (sequenceIdentifier != null) code ^= sequenceIdentifier.hashCode();
        if (descriptor         != null) code ^= descriptor        .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this range dimension
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return String.valueOf(descriptor);
    }             
}
