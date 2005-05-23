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
package org.geotools.metadata;

// J2SE direct dependencies
import java.util.Collection;

// OpenGIS dependencies
import org.opengis.metadata.citation.OnLineResource;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Information describing metadata extensions.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 * @deprecated Renamed as {@code MetadataExtensionInformationImpl} in {@code org.geotools.metadata.iso} subpackage.
 */
public class MetadataExtensionInformation extends MetadataEntity
        implements org.opengis.metadata.MetadataExtensionInformation
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 573866936088674519L;

    /**
     * Information about on-line sources containing the community profile name and
     * the extended metadata elements. Information for all new metadata elements.
     */
    private OnLineResource extensionOnLineResource;

    /**
     * Provides information about a new metadata element, not found in ISO 19115, which is
     * required to describe geographic data.
     */
    private Collection extendedElementInformation;

    /**
     * Construct an initially empty metadata extension information.
     */
    public MetadataExtensionInformation() {
    }

    /**
     * Information about on-line sources containing the community profile name and
     * the extended metadata elements. Information for all new metadata elements.
     */
    public OnLineResource getExtensionOnLineResource() {
        return extensionOnLineResource;
    }

   /**
     * Set information about on-line sources.
     */
    public synchronized void setextensionOnLineResource(final OnLineResource newValue) {
        checkWritePermission();
        this.extensionOnLineResource = newValue; 
    }

    /**
     * Provides information about a new metadata element, not found in ISO 19115, which is
     * required to describe geographic data.
     */
    public synchronized Collection getExtendedElementInformation() {
        return extendedElementInformation = nonNullCollection(extendedElementInformation,
                                                              ExtendedElementInformation.class);
    }
    
    /**
     * Set information about a new metadata element.
     */
    public synchronized void setextendedElementInformation(final Collection newValues) {
        extendedElementInformation = copyCollection(newValues, extendedElementInformation,
                                                    ExtendedElementInformation.class);
    }
    
   /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        extensionOnLineResource    = (OnLineResource) unmodifiable(extensionOnLineResource);
        extendedElementInformation = (Collection)     unmodifiable(extendedElementInformation);
    }

    /**
     * Compare this metadata extension information with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final MetadataExtensionInformation that = (MetadataExtensionInformation) object;
            return Utilities.equals(extensionOnLineResource,    that.extensionOnLineResource   ) &&
                   Utilities.equals(extendedElementInformation, that.extendedElementInformation);
        }
        return false;
    }

    /**
     * Returns a hash code value for this object. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (extensionOnLineResource != null) code ^= extensionOnLineResource.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        return String.valueOf(extensionOnLineResource);
    }        
}
