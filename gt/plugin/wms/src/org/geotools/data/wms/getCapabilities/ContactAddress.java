/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

/**
 * @author rgould
 *
 * The address for the contact supplying the service.
 * All fields are required.
 */
public class ContactAddress {
    /** The type of address */
    private String addressType;

    /** The street address */
    private String address;

    /** The address city */
    private String city;

    /** The state or province */
    private String stateOrProvince;

    /** The zip or postal code */
    private String postCode;

    /** The address country */
    private String country;

    /**
     * @param addressType
     * @param address
     * @param city
     * @param stateOrProvince
     * @param postCode
     * @param country
     */
    public ContactAddress(String addressType, String address, String city,
            String stateOrProvince, String postCode, String country) {
        this.addressType = addressType;
        this.address = address;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.postCode = postCode;
        this.country = country;
    }
    
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAddressType() {
        return addressType;
    }
    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getPostCode() {
        return postCode;
    }
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
    public String getStateOrProvince() {
        return stateOrProvince;
    }
    public void setStateOrProvince(String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }
}
