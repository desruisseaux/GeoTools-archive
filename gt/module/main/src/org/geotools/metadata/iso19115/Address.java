package org.geotools.metadata.iso19115;

import java.util.Locale;

public class Address extends MetaData implements
		org.opengis.metadata.citation.Address {
	 String administrativeArea;
     String city;
     String country;
    
    public String getAdministrativeArea(Locale locale) {
    	return administrativeArea;
    }
	public String getAdministrativeArea() {
		return administrativeArea;
	}
	public void setAdministrativeArea(String administrativeArea) {
		this.administrativeArea = administrativeArea;
	}
	public String getCity(Locale locale) {
		return city;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCountry(Locale locale) {
		return country;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String[] getDeliveryPoints() {
		return deliveryPoints;
	}
	public void setDeliveryPoints(String[] deliveryPoints) {
		this.deliveryPoints = deliveryPoints;
	}
	public String[] getElectronicMailAddresses() {
		return electronicMailAddresses;
	}
	public void setElectronicMailAddresses(String[] electronicMailAddresses) {
		this.electronicMailAddresses = electronicMailAddresses;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
     String[] deliveryPoints;
     String[] electronicMailAddresses;
     String postalCode; 
}
