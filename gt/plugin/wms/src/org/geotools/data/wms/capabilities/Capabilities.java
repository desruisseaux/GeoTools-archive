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
 * Represents a base object for a WMS getCapabilities response.
 * 
 * @author Richard Gould, Refractions Research
 */
public class Capabilities {
    private Service service;
    private Request request;
    private Layer[] layers;
    
	/**
	 * @return Returns the layers.
	 */
	public Layer[] getLayers() {
		return layers;
	}
	/**
	 * @param layers The layers to set.
	 */
	public void setLayers(Layer[] layers) {
		this.layers = layers;
	}
	/**
	 * @return Returns the request.
	 */
	public Request getRequest() {
		return request;
	}
	/**
	 * @param request The request to set.
	 */
	public void setRequest(Request request) {
		this.request = request;
	}
	/**
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
	private String version;
	
}
