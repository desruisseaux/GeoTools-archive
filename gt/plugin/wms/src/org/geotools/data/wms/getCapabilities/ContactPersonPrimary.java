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
 * The primary contact person for the service. 
 */
public class ContactPersonPrimary {
    /** The person to contact - Required */
    private String contactPerson;

    /** The organization supplying the service - Required*/
    private String contactOrganization;
    
    /**
     * @param contactPerson
     * @param contactOrganization
     */
    public ContactPersonPrimary(String contactPerson, String contactOrganization) {
        this.contactPerson = contactPerson;
        this.contactOrganization = contactOrganization;
    }
    
    public String getContactOrganization() {
        return contactOrganization;
    }
    public void setContactOrganization(String contactOrganization) {
        this.contactOrganization = contactOrganization;
    }
    public String getContactPerson() {
        return contactPerson;
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
}
