/*
 * Created on Jun 24, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
	
	private String version = "1.1.1";
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
