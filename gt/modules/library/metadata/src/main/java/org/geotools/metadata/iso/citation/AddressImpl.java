/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.Address;
import org.opengis.util.InternationalString;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Location of the responsible individual or organization.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "deliveryPoints", "city", "administrativeArea", "postalCode", "country", "electronicMailAddresses"
})
@XmlRootElement(name = "CI_Address")
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
    private Collection<String> deliveryPoints;

    /**
     * Address of the electronic mailbox of the responsible organization or individual.
     */
    private Collection<String> electronicMailAddresses;

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
    @XmlElement(name = "administrativeArea", required = false, namespace = "http://www.isotc211.org/2005/gmd")
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
    @XmlElement(name = "city", required = false, namespace = "http://www.isotc211.org/2005/gmd")
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
    @XmlElement(name = "country", required = false, namespace = "http://www.isotc211.org/2005/gmd")
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
    @XmlElement(name = "deliveryPoint", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<String> getDeliveryPoints() {
        return xmlOptional(deliveryPoints = nonNullCollection(deliveryPoints, String.class));
    }

    /**
     * Set the address line for the location (as described in ISO 11180, Annex A).
     */
    public synchronized void setDeliveryPoints(
            final Collection<? extends String> newValues)
    {
        deliveryPoints = copyCollection(newValues, deliveryPoints, String.class);
    }
    
    /**
     * Returns the address of the electronic mailbox of the responsible organization or individual.
     */
    @XmlElement(name = "electronicMailAddress", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<String> getElectronicMailAddresses() {
        return xmlOptional(electronicMailAddresses = nonNullCollection(electronicMailAddresses, String.class));
    }

    /**
     * Set the address of the electronic mailbox of the responsible organization or individual.
     */
    public synchronized void setElectronicMailAddresses(
            final Collection<? extends String> newValues)
    {
        electronicMailAddresses = copyCollection(newValues, electronicMailAddresses, String.class);
    }

    /**
     * Returns ZIP or other postal code.
     * Returns {@code null} if unspecified.
     */
    @XmlElement(name = "postalCode", required = false, namespace = "http://www.isotc211.org/2005/gmd")
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
     * Sets the {@code isMarshalling} flag to {@code true}, since the marshalling
     * process is going to be done.
     * This method is automatically called by JAXB, when the marshalling begins.
     * 
     * @param marshaller Not used in this implementation.
     */
///    private void beforeMarshal(Marshaller marshaller) {
///        isMarshalling(true);
///    }

    /**
     * Sets the {@code isMarshalling} flag to {@code false}, since the marshalling
     * process is finished.
     * This method is automatically called by JAXB, when the marshalling ends.
     * 
     * @param marshaller Not used in this implementation
     */
///   private void afterMarshal(Marshaller marshaller) {
///        isMarshalling(false);
///    }
}
