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
 * Available Distributed Computing Platforms (DCPs) are
 * listed here. At present, only HTTP is defined.
 */
public class DCPType {
    
    /** Represents available HTTP methods */
    private HTTP http;
    
    /**
     * @param http Represents available HTTP methods
     */
    public DCPType(HTTP http) {
        super();
        this.http = http;
    }
    
    public HTTP getHttp() {
        return http;
    }
    public void setHttp(HTTP http) {
        this.http = http;
    }
}
