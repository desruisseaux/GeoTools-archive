/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.xml.wfs;

import java.util.HashSet;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author Norman Barker To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class WFSCapability {
    private Set requests;
    private String vendorSpecificCapabilities;

    /**
     * DOCUMENT ME!
     *
     * @return Returns the request.
     */
    public Set getRequests() {
        return requests;
    }

    /**
     * DOCUMENT ME!
     *
     * @param request The request to set.
     */
    public void setRequests(Set request) {
        this.requests = request;
    }
    
    public void addRequest(Request request){
        if(requests == null)
            requests = new HashSet();
        requests.add(request);
    }

    /**
     * DOCUMENT ME!
     *
     * @return Returns the vendorSpecificCapabilities.
     */
    public String getVendorSpecificCapabilities() {
        return vendorSpecificCapabilities;
    }

    /**
     * DOCUMENT ME!
     *
     * @param vendorSpecificCapabilities The vendorSpecificCapabilities to set.
     */
    public void setVendorSpecificCapabilities(String vendorSpecificCapabilities) {
        this.vendorSpecificCapabilities = vendorSpecificCapabilities;
    }
}
