/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data.wms.capabilities;

/**
 * @author rgould
 *
 * Represents information about a contact person for the service.
 * 
 * All fields are optional.
 */
public class ContactInformation {
    
    /** The primary contact person */
    private ContactPersonPrimary contactPersonPrimary;
    
    /** The position title for the contact person */
    private String contactPosition;
    
    /** The address for the contact supplying the service */
    private ContactAddress contactAddress;
    
    /** Contact phone number. */
    private String contactVoiceTelephone;

    /** The contact Fax number. */
    private String contactFacsimileTelephone;

    /** E-mail address for the contact */
    private String contactElectronicMailAddress;

    
    public ContactAddress getContactAddress() {
        return contactAddress;
    }
    public void setContactAddress(ContactAddress contactAddress) {
        this.contactAddress = contactAddress;
    }
    public String getContactElectronicMailAddress() {
        return contactElectronicMailAddress;
    }
    public void setContactElectronicMailAddress(
            String contactElectronicMailAddress) {
        this.contactElectronicMailAddress = contactElectronicMailAddress;
    }
    public ContactPersonPrimary getContactPersonPrimary() {
        return contactPersonPrimary;
    }
    public void setContactPersonPrimary(
            ContactPersonPrimary contactPersonPrimary) {
        this.contactPersonPrimary = contactPersonPrimary;
    }
    public String getContactPosition() {
        return contactPosition;
    }
    public void setContactPosition(String contactPosition) {
        this.contactPosition = contactPosition;
    }
    public String getContactFacsimileTelephone() {
        return contactFacsimileTelephone;
    }
    public void setContactFacsimileTelephone(String contactFacsimileTelephone) {
        this.contactFacsimileTelephone = contactFacsimileTelephone;
    }
    public String getContactVoiceTelephone() {
        return contactVoiceTelephone;
    }
    public void setContactVoiceTelephone(String contactVoiceTelephone) {
        this.contactVoiceTelephone = contactVoiceTelephone;
    }
}
