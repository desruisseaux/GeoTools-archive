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

// J2SE direct dependencies
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.geotools.metadata.MetadataEntity;
import org.geotools.resources.Utilities;
import org.geotools.util.CheckedHashSet;
import org.opengis.util.InternationalString;


/**
 * Location of the responsible individual or organization.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class Address extends MetadataEntity
       implements org.opengis.metadata.citation.Address
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2278687294173262546L;
    
    /**
     * State, province of the location.
     */
    private InternationalString administrativeArea;
    
    /**
     * The city of the location
     */
    private InternationalString city;
    
   /**
     * Country of the physical address.
     */
    private InternationalString country;

    /**
     * ZIP or other postal code.
     */    
    private String postalCode;

    /**
     * Address line for the location (as described in ISO 11180, Annex A).
     */
    private Set deliveryPoints = new CheckedHashSet( String.class );
    
    /**
     * Address of the electronic mailbox of the responsible organization or individual.
     */
    private Set electronicMailAddresses = new CheckedHashSet( String.class );

    /**
     * Constructs an initially empty address.
     */
    public Address() {
    }

    /**
     * Return the state, province of the location.
     * Returns <code>null</code> if unspecified.
     */
    public InternationalString getAdministrativeArea() {
        return administrativeArea;
    }
    
    /**
     * Set the state, province of the location.
     */
    public synchronized void setAdministrativeArea(final InternationalString newValue) {
        checkWritePermission();
        administrativeArea = newValue;
    }

    /**
     * Returns the city of the location
     * Returns <code>null</code> if unspecified.
     */
    public InternationalString getCity() {
        return city;
    }

   /**
     * Set the city of the location
     */
    public synchronized void setCity(final InternationalString newValue) {
        checkWritePermission();
        city = newValue;
    }

    /**
     * Returns the country of the physical address.
     * Returns <code>null</code> if unspecified.
     */
    public InternationalString getCountry() {
        return country;
    }

    /**
     * set the country of the physical address.
     */
    public synchronized void setCountry(final InternationalString newValue) {
        checkWritePermission();
        country = newValue;
    }
    
    /**
     * Returns the address line for the location (as described in ISO 11180, Annex A).
     */
    public Set getDeliveryPoints() {
        final Set deliveryPoints = this.deliveryPoints; // Avoid synchronization
        return (deliveryPoints!=null) ? deliveryPoints : Collections.EMPTY_SET;
    }
    
    /**
     * Set the address line for the location (as described in ISO 11180, Annex A).
     */
    public synchronized void setDeliveryPoints(final Set newValues) {
        checkWritePermission();
        if (deliveryPoints == null) {
            deliveryPoints = new CheckedHashSet(String.class);
        } else {
            deliveryPoints.clear();
        }
        deliveryPoints.addAll(newValues);
    }
    /**
     * Returns the address of the electronic mailbox of the responsible organization or individual.
     */
    public Set getElectronicMailAddresses() {
        final Set electronicMailAddresses = this.electronicMailAddresses; // Avoid synchronization
        return (electronicMailAddresses!=null) ? electronicMailAddresses : Collections.EMPTY_SET;
    }
    /**
     * Set the address of the electronic mailbox of the responsible organization or individual.
     */
    public synchronized void setElectronicMailAddresses(final Set newValues) {
        checkWritePermission();
        if (electronicMailAddresses == null) {
            electronicMailAddresses = new CheckedHashSet(String.class);
        } else {
            electronicMailAddresses.clear();
        }
        electronicMailAddresses.addAll(newValues);
    }
    
    /**
     * Returns ZIP or other postal code.
     * Returns <code>null</code> if unspecified.
     */
    public String getPostalCode() {
        return postalCode;
    }
    
    /**
     * Set ZIP or other postal code.
     */
    public synchronized void setPostalCode(final String newValue) {
        checkWritePermission();
        postalCode = newValue;
    }

    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
        administrativeArea      = (InternationalString) unmodifiable(administrativeArea);
        city                    = (InternationalString) unmodifiable(city);
        country                 = (InternationalString) unmodifiable(country);
        postalCode              = (String)              unmodifiable(postalCode);
        deliveryPoints          = (Set)                 unmodifiable(deliveryPoints);
        electronicMailAddresses = (Set)                 unmodifiable(electronicMailAddresses);
    }

    /**
     * Compare this address with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Address that = (Address) object;
            return Utilities.equals(this.administrativeArea,      that.administrativeArea ) &&
                   Utilities.equals(this.city,                    that.city               ) &&
                   Utilities.equals(this.country,                 that.country            ) &&
                   Utilities.equals(this.postalCode,              that.postalCode         ) &&
                   Utilities.equals(this.deliveryPoints,          that.deliveryPoints     ) &&
                   Utilities.equals(this.electronicMailAddresses, that.electronicMailAddresses);
        }
        return false;
    }

    /**
     * Returns a hash code value for this address. For performance reason, this method do
     * not uses all attributes for computing the hash code. Instead, it uses the attributes
     * that are the most likely to be unique.
     */
    public synchronized int hashCode() {
        int code = (int)serialVersionUID;
        if (postalCode              != null) code ^= postalCode             .hashCode();
        if (electronicMailAddresses != null) code ^= electronicMailAddresses.hashCode();
        return code;
    }

    /**
     * Returns a string representation of this address.
     */
    public synchronized String toString() {
        final StringBuffer buffer = new StringBuffer();
        if (deliveryPoints != null) {
            for (final Iterator it=deliveryPoints.iterator(); it.hasNext();) {
                appendLineSeparator(buffer);
                buffer.append(it.next());
            }
        }
        if (city != null) {
            appendLineSeparator(buffer);
            buffer.append(city);
            if (administrativeArea != null) {
                buffer.append(" (");
                buffer.append(administrativeArea);
                buffer.append(')');
            }
        }
        if (country != null) {
            appendLineSeparator(buffer);
            buffer.append(country);
        }
        return buffer.toString();
    }        
}
