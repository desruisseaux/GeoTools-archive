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
