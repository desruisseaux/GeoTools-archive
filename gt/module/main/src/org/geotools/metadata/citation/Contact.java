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
package org.geotools.metadata.citation;

// OpenGIS dependencies
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.Telephone;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;


/**
 * Information required to enable contact with the responsible person and/or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Contact extends MetadataEntity
       implements org.opengis.metadata.citation.Contact
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3283637180253117382L;
    
    /**
     * Supplemental instructions on how or when to contact the individual or organization.
     */
    private InternationalString contactInstructions;
    
    /**
     * Time period (including time zone) when individuals can contact the organization or
     * individual. 
     */
    private InternationalString hoursOfService;
    
    /**
     * On-line information that can be used to contact the individual or organization.
     */
    private OnLineResource onLineResource;
    
    /**
     * Physical and email address at which the organization or individual may be contacted.
     */
    private Address address;
    
    /**
     * Telephone numbers at which the organization or individual may be contacted.
     */
    private Telephone phone;

    /**
     * Constructs an initially empty contact.
     */
    public Contact() {
    }

    /**
     * Returns the physical and email address at which the organization or individual may be contacted.
     * Returns <code>null</code> if none.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Set the physical and email address at which the organization or individual may be contacted.
     */
    public synchronized void setAddress(final Address newValue) {
        checkWritePermission();
        address = newValue;
    }
    
    /**
     * Returns supplemental instructions on how or when to contact the individual or organization.
     * Returns <code>null</code> if none.
     */
    public InternationalString getContactInstructions() {
        return contactInstructions;
    }

    /**
     * Set supplemental instructions on how or when to contact the individual or organization.
     */
    public synchronized void setContactInstructions(final InternationalString newValue) {
        checkWritePermission();
        contactInstructions = newValue;
    }

    /**
     * Return on-line information that can be used to contact the individual or organization.
     * Returns <code>null</code> if none.
     */
    public OnLineResource getOnLineResource() {
        return onLineResource;
    }

    /**
     * Set on-line information that can be used to contact the individual or organization.
     */
    public synchronized void setOnLineResource(final OnLineResource newValue) {
        checkWritePermission();
        onLineResource = newValue;
    }

    /**
     * Returns telephone numbers at which the organization or individual may be contacted.
     * Returns <code>null</code> if none.
     */
    public Telephone getPhone() {
        return phone;
    }

    /**
     * Set telephone numbers at which the organization or individual may be contacted.
     */
    public synchronized void setPhone(final Telephone newValue) {
        checkWritePermission();
        phone = newValue;
    }    

    /**
     * Returns time period (including time zone) when individuals can contact the organization or
     * individual. 
     * Returns <code>null</code> if none.
     */
    public InternationalString getHoursOfService() {
        return hoursOfService;
    }

    /**
     * Set time period (including time zone) when individuals can contact the organization or
     * individual. 
     */
    public synchronized void setHoursOfService(final InternationalString newValue) {
        checkWritePermission();
        hoursOfService = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        contactInstructions = (InternationalString) unmodifiable(contactInstructions);
        hoursOfService      = (InternationalString) unmodifiable(hoursOfService);
        onLineResource      = (OnLineResource)      unmodifiable(onLineResource);
        address             = (Address)             unmodifiable(address);
        phone               = (Telephone)           unmodifiable(phone);
    }

    /**
     * Compare this contact with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Contact that = (Contact) object;
            return Utilities.equals(this.contactInstructions, that.contactInstructions) &&
                   Utilities.equals(this.hoursOfService,      that.hoursOfService     ) &&
                   Utilities.equals(this.onLineResource,      that.onLineResource     ) &&
                   Utilities.equals(this.address,             that.address            ) &&
                   Utilities.equals(this.phone,               that.phone              );
        }
        return false;
    }

    /**
     * Returns a hash code value for this contact. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (contactInstructions != null) code ^= contactInstructions.hashCode();
        if (address             != null) code ^= address            .hashCode();
        return code;
    }

    /**
     * Returns a string representation of this contact.
     */
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (contactInstructions != null) {
            buffer.append(contactInstructions);
        }
        if (address != null) {
            appendLineSeparator(buffer);
            buffer.append(address);
        }
        if (phone != null) {
            appendLineSeparator(buffer);
            buffer.append(phone);
        }
        if (onLineResource != null) {
            appendLineSeparator(buffer);
            buffer.append(onLineResource);
        }
        return buffer.toString();
    }    
}
