/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.data.ows;

import java.util.List;

/**
 * Represents a base object for a Capabilities document
 *
 * @author rgould
 */
public class Capabilities {
    private Service service;
	private String version;
    /**
     * The Service contains metadata about the OWS.
     * 
     * @return Returns the service.
     */
    public Service getService() {
        return service;
    }

    /**
     * @param service The service to set.
     */
    public void setService(Service service) {
        this.service = service;
    }

	/**
	 * The version that this Capabilities is in.
	 * 
	 * @return Returns the version.
	 */
	public String getVersion() {
	    return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
	    this.version = version;
	}
}
