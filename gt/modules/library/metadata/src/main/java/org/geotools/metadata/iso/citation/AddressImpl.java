/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le D�veloppement
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

// J2SE direct dependencies
import java.util.Collection;
import java.util.Iterator;

// OpenGIS dependencies
import org.opengis.metadata.citation.Address;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Location of the responsible individual or organization.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class AddressImpl extends MetadataEntity implements Address {
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
    private Collection deliveryPoints;
    
    /**
     * Address of the electronic mailbox of the responsible organization or individual.
     */
    private Collection electronicMailAddresses;

    /**
     * Constructs an initially empty address.
     */
    public AddressImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public AddressImpl(final Address source) {
        super(source);
    }

    /**
     * Return the state, province of the location.
     * Returns {@code null} if unspecified.
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
     * Returns {@code null} if unspecified.
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
     * Returns {@code null} if unspecified.
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
    public synchronized Collection getDeliveryPoints() {
        return deliveryPoints = nonNullCollection(deliveryPoints, String.class);
    }
    
    /**
     * Set the address line for the location (as described in ISO 11180, Annex A).
     */
    public synchronized void setDeliveryPoints(final Collection newValues) {
        deliveryPoints = copyCollection(newValues, deliveryPoints, String.class);
    }
    /**
     * Returns the address of the electronic mailbox of the responsible organization or individual.
     */
    public synchronized Collection getElectronicMailAddresses() {
        return electronicMailAddresses = nonNullCollection(electronicMailAddresses, String.class);
    }

    /**
     * Set the address of the electronic mailbox of the responsible organization or individual.
     */
    public synchronized void setElectronicMailAddresses(final Collection newValues) {
        electronicMailAddresses = copyCollection(newValues, electronicMailAddresses, String.class);
    }
    
    /**
     * Returns ZIP or other postal code.
     * Returns {@code null} if unspecified.
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
}
