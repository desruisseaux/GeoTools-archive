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
 * Represents a base object for a WMS getCapabilities response.
 */
public class WMT_MS_Capabilities {
    /** General service medadata. Required. */
	private Service service;
	
	/** 
	 * A Capability lists available request types, how exceptions may be reported,
	 * and whethe any vendor-specific capablities are defined. It also includes an
	 * optional list of map layers available from this server. 	 
	 * 
	 * Required.
	 */
	private Capability capability;
	
	private String version;
	private String updateSequence;
	
	
    /**
     * @param service
     * @param capability
     */
    public WMT_MS_Capabilities(Service service, Capability capability) {
        super();
        this.service = service;
        this.capability = capability;
    }
    public Capability getCapability() {
        return capability;
    }
    public void setCapability(Capability capability) {
        this.capability = capability;
    }
    public Service getService() {
        return service;
    }
    public void setService(Service service) {
        this.service = service;
    }
    public String getUpdateSequence() {
        return updateSequence;
    }
    public void setUpdateSequence(String updateSequence) {
        this.updateSequence = updateSequence;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
