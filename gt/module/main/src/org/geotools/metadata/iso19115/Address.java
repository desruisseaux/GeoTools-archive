package org.geotools.metadata.iso19115;

import java.util.Set;

import org.geotools.util.CheckedHashSet;
import org.opengis.util.InternationalString;

public class Address extends MetaData implements
		org.opengis.metadata.citation.Address {
    
    InternationalString administrativeArea;
    InternationalString city;
    InternationalString country;
    Set deliveryPoints = new CheckedHashSet( String.class );
    Set electronicMailAddresses = new CheckedHashSet( String.class );
    String postalCode;
    
    public InternationalString getAdministrativeArea() {
        return administrativeArea;
    }
    public void setAdministrativeArea(InternationalString administrativeArea) {
        this.administrativeArea = administrativeArea;
    }
    public InternationalString getCity() {
        return city;
    }
    public void setCity(InternationalString city) {
        this.city = city;
    }
    public InternationalString getCountry() {
        return country;
    }
    public void setCountry(InternationalString country) {
        this.country = country;
    }
    public Set getDeliveryPoints() {
        return deliveryPoints;
    }
    public void setDeliveryPoints(Set deliveryPoints) {
        this.deliveryPoints = deliveryPoints;
    }
    public Set getElectronicMailAddresses() {
        return electronicMailAddresses;
    }
    public void setElectronicMailAddresses(Set electronicMailAddresses) {
        this.electronicMailAddresses = electronicMailAddresses;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
