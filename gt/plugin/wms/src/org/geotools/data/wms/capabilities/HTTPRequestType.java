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

import java.net.URL;

/**
 * Represents a request type part of the HTTP element.
 * ie: <get> or <post>
 */
public class HTTPRequestType {
    /** URL prefix for each HTTP request method */
    private URL onlineResource;
    
    /**
     * @param onlineResource URL prefix for each HTTP request method
     */
    public HTTPRequestType(URL onlineResource) {
        super();
        this.onlineResource = onlineResource;
    }
    
    
    public URL getOnlineResource() {
        return onlineResource;
    }
    public void setOnlineResource(URL onlineResource) {
        this.onlineResource = onlineResource;
    }
}
