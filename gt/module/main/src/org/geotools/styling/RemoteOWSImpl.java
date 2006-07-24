/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;

public class RemoteOWSImpl extends AbstractGTComponent implements RemoteOWS {

	private String service;
	private String onlineResource;
	
	public String getService() {
		return service;
	}
	
	public void setService(String service) {
		String old = this.service;
		this.service = service;
		
		fireChildChanged("service",this.service,old);
	}
	
	public String getOnlineResource() {
		return onlineResource;
	}
	
	public void setOnlineResource(String onlineResource) {
		String old = this.onlineResource;
		this.onlineResource = onlineResource;
		
		fireChildChanged("onlineResource",this.onlineResource,old);
	}

}
