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
